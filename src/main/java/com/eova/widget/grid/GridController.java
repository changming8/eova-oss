/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.widget.grid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.render.XlsRender;
import com.eova.common.utils.xx;
import com.eova.config.EovaConfig;
import com.eova.config.PageConst;
import com.eova.model.EovaLog;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.eova.widget.WidgetUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * Grid组件
 * 
 * @author Jieven
 * 
 */
public class GridController extends BaseController {

	final Controller ctrl = this;

	/** 元对象业务拦截器 **/
	protected MetaObjectIntercept intercept = null;

	/** 异常信息 **/
	private String errorInfo = "";

	/**
	 * 导出查询
	 * 
	 * @throws Exception
	 */
	public void export() throws Exception {
		String objectCode = getPara(0);
		String menuCode = getPara(1);

		MetaObject object = sm.meta.getMeta(objectCode);
		Menu menu = Menu.dao.findByCode(menuCode);

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());

		// 构建查询
		List<Object> parmList = new ArrayList<Object>();
		String sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, parmList, true);

		// 转换SQL参数
		Object[] paras = new Object[parmList.size()];
		parmList.toArray(paras);
		List<Record> data = Db.use(object.getDs()).find("select *" + sql, paras);
		
		// 查询后置任务
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, data);
			ac.object = object;
			intercept.queryAfter(ac);
		}

		List<MetaField> fields = object.getFields();
		
		// 根据表达式将数据中的值翻译成汉字
		WidgetManager.convertValueByExp(this, fields, data);

		Iterator<MetaField> it = fields.iterator();
		while (it.hasNext()) {
			MetaField f = it.next();
			if (!f.getBoolean("is_show")) {
				it.remove();
			}
		}

		render(new XlsRender(data, fields, object));
	}

	/**
	 * 数据查询
	 *     //页面开启是否翻译exp 表达式内容 直接显示内容 编辑时可翻译
	 * @throws Exception
	 */
	public void query() throws Exception {

		String objectCode = getPara(0);
		String menuCode = getPara(1);
		int pageNumber = getParaToInt(PageConst.PAGENUM, 1);
		int pageSize = getParaToInt(PageConst.PAGESIZE, 100000);

		MetaObject object = sm.meta.getMeta(objectCode);
		Menu menu = Menu.dao.findByCode(menuCode);

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());

		// 构建查询
		List<Object> paras = new ArrayList<Object>();
		String select =  "select " + WidgetManager.buildSelect(object, RID());
		String sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true);
		Page<Record> page = Db.use(object.getDs()).paginate(pageNumber, pageSize, select, sql, xx.toArray(paras));

		// 查询后置任务
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, page.getList());
			ac.object = object;
			intercept.queryAfter(ac);
		}

		// 备份Value列，然后将值列转换成Key列
		WidgetUtil.copyValueColumn(page.getList(), object.getPk(), object.getFields());
		// 根据表达式将数据中的值翻译成汉字  根据object自定义是否开启exp表达式解析,默认打开 需要时手动关闭
		if(null != object.get("is_enable_exp")&&object.getBoolean("is_enable_exp")==true) {
			WidgetManager.convertValueByExp(this, object.getFields(), page.getList());
		}
		
		// 构建JSON数据
		StringBuilder sb = new StringBuilder(String.format("{\"total\":%s,\"rows\": %s}", page.getTotalRow(), JsonKit.toJson(page.getList())));

		// Footer
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, page.getList());
			ac.object = object;
			Kv footer = intercept.queryFooter(ac);
			if (footer != null) {
				sb.insert(sb.length() - 1, String.format(",\"footer\":[%s]", footer.toJson()));
			}
		}

		renderJson(sb.toString());
	}

	/**
	 * 新增
	 * @throws Exception 
	 */
	public void add() throws Exception {
		String objectCode = getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);

		String json = getPara("rows");
		final List<Record> records = getRecordsByJson(json, object.getFields(), object.getPk());

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());
		// 事务
		Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					for (Record record : records) {

						AopContext ac = new AopContext(ctrl, record);
						ac.object = object;

						// 新增前置任务
						if (intercept != null) {
							String msg = intercept.addBefore(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
						if (xx.isEmpty(object.getTable())) {
							throw new Exception("视图暂时不支持Grid 单元格编辑，请使用Form模式！");
						}
						Db.use(object.getDs()).save(object.getTable(), object.getPk(), record);
						EovaLog.dao.info(ctrl, EovaLog.ADD, object.getStr("code"));
						// 新增后置任务
						if (intercept != null) {
							String msg = intercept.addAfter(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
					}

				} catch (Exception e) {
					errorInfo = TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}
		});

		// 新增成功之后
		if (intercept != null) {
			try {
				AopContext ac = new AopContext(this, records);
				ac.object = object;
				
				String msg = intercept.addSucceed(ac);
				if (!xx.isEmpty(msg)) {
					errorInfo = msg;
				}
			} catch (Exception e) {
				errorInfo = TemplateUtil.buildException(e);
			}
		}

		if (!xx.isEmpty(errorInfo)) {
			renderJson(Easy.fail(errorInfo));
			return;
		}

		renderJson(new Easy());
	}

	/**
	 * 删除
	 * @throws Exception 
	 */
	public void delete() throws Exception {
		deleteOrHide(true);
	}
	
	/**
	 * 隐藏
	 * @throws Exception 
	 */
	public void hide() throws Exception {
		deleteOrHide(false);
	}

	/**
	 * 删除或者隐藏
	 * @throws Exception
	 */
	private void deleteOrHide(final boolean isDel) throws Exception {
		String objectCode = getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);

		String json = getPara("rows");

		final List<Record> records = getRecordsByJson(json, object.getFields(), object.getPk());

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());
		// 事务
		Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					for (Record record : records) {

						AopContext ac = new AopContext(ctrl, record);
						ac.object = object;

						// 删除前置任务
						if (intercept != null && isDel) {
							String msg = intercept.deleteBefore(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
						if (!xx.isEmpty(object.getTable())) {
							String pk = object.getPk();
							String pkValue = record.get(pk).toString();
							if (isDel) {
								// 删除数据
								Db.use(object.getDs()).deleteById(object.getTable(), pk, pkValue);
							} else {
								// 隐藏数据
								String hideFieldName = xx.getConfig("hide_field_name", "is_hide");
								String sql = String.format("update %s set %s = 1 where %s = ?", object.getTable(), hideFieldName, pk);
								Db.use(object.getDs()).update(sql, pkValue);
							}
							EovaLog.dao.info(ctrl, EovaLog.DELETE, object.getStr("code") + "[" + pkValue + "]");
						} else {
							// 视图无法自动删除，请自定义元对象业务拦截完成删除逻辑！
							// MetaObjectIntercept.deleteBefore();
						}
						// 删除后置任务
						if (intercept != null && isDel) {
							String msg = intercept.deleteAfter(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
					}
				} catch (Exception e) {
					errorInfo = TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}
		});

		// 删除成功之后
		if (intercept != null && isDel) {
			try {
				AopContext ac = new AopContext(this, records);
				ac.object = object;
				
				String msg = intercept.deleteSucceed(ac);
				if (!xx.isEmpty(msg)) {
					errorInfo = msg;
				}
			} catch (Exception e) {
				errorInfo = TemplateUtil.buildException(e);
			}
		}

		if (!xx.isEmpty(errorInfo)) {
			renderJson(Easy.fail(errorInfo));
			return;
		}

		renderJson(new Easy());
	}

	/**
	 * 更新
	 * @throws Exception 
	 */
	public void update() throws Exception {

		String objectCode = getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);

		String json = getPara("rows");

		final List<Record> records = getRecordsByJson(json, object.getFields(), object.getPk());

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());
		// 事务
		Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					for (Record record : records) {

						AopContext ac = new AopContext(ctrl, record);
						ac.object = object;

						// 修改前置任务
						if (intercept != null) {
							String msg = intercept.updateBefore(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
						if (xx.isEmpty(object.getTable())) {
							throw new Exception("视图暂时不支持Grid单元格编辑，请使用Form模式！");
						}
						Db.use(object.getDs()).update(object.getTable(), object.getPk(), record);
						EovaLog.dao.info(ctrl, EovaLog.UPDATE, object.getStr("code") + "[" + record.get(object.getPk()) + "]");
						// 修改后置任务
						if (intercept != null) {
							String msg = intercept.updateAfter(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
					}

				} catch (Exception e) {
					errorInfo = TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}
		});


		// 修改成功之后
		if (intercept != null) {
			try {
				AopContext ac = new AopContext(this, records);
				ac.object = object;
				
				String msg = intercept.updateSucceed(ac);
				if (!xx.isEmpty(msg)) {
					errorInfo = msg;
				}
			} catch (Exception e) {
				errorInfo = TemplateUtil.buildException(e);
			}
		}

		if (!xx.isEmpty(errorInfo)) {
			renderJson(Easy.fail(errorInfo));
			return;
		}

		renderJson(new Easy());
	}

	/**
	 * 更新Grid列宽度
	 * 
	 * @throws Exception
	 */
	public void updateWidths() throws Exception {
		String objectCode = getPara(0);

		if (objectCode == null) {
			renderJson(Easy.fail("元对象缺失,当前组件无法调整列宽!"));
			return;
		}

		String widths = getPara(1);

		String[] ss = widths.split(",");
		
		int i = 0;
		List<MetaField> fields = MetaField.dao.queryShowFieldByObjectCode(objectCode);
		for (MetaField x : fields) {
			x.set("width", ss[i]);
			x.update();
			i++;
		}

		renderJson(Easy.sucess());
	}

	/**
	 * json转List
	 * 
	 * @param json
	 * @param pkName
	 * @return
	 */
	private static List<Record> getRecordsByJson(String json, List<MetaField> items, String pkName) {
		List<Record> records = new ArrayList<Record>();

		List<JSONObject> list = JSON.parseArray(json, JSONObject.class);
		for (JSONObject o : list) {
			Map<String, Object> map = JSON.parseObject(o + "", new TypeReference<Map<String, Object>>() {
			});
			Record re = new Record();
			re.setColumns(map);
			// 将Text翻译成Value,然后删除val字段
			for (MetaField x : items) {
				String en = x.getEn();// 字段名
				String exp = x.getStr("exp");// 表达式
				Object value = re.get(en);// 值

				if (!xx.isEmpty(exp)) {
					String valField = en + "_val";
					// 获取值列中的值
					value = re.get(valField);
					// 获得值之后删除值列防止持久化报错
					re.remove(valField);
				}

				re.set(en, EovaConfig.convertor.convert(x, value));
			}
			// 删除主键备份值列
			re.remove("pk_val");
			// 删除Orcle分页产生的rownum_
			if (xx.isOracle()) {
				re.remove("rownum_");
			}
			records.add(re);
		}

		return records;
	}

	public static void main(String[] args) {

		String sl = "[{'id':1,'loginId':'111'},{'id':2,'loginId':'222'}]";
		List<JSONObject> list = JSON.parseArray(sl, JSONObject.class);
		for (JSONObject o : list) {
			Map<String, Object> map = JSON.parseObject(o + "", new TypeReference<Map<String, Object>>() {
			});
			Record re = new Record();
			re.setColumns(map);
			System.out.println(re.toJson());
		}
	}

}