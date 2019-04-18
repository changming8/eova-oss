/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.widget;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eova.aop.eova.EovaContext;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.eova.common.utils.db.SqlUtil;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.core.menu.config.TreeConfig;
import com.eova.engine.DynamicParse;
import com.eova.engine.EovaExp;
import com.eova.i18n.I18NBuilder;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.model.User;
import com.eova.service.sm;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * EOVA 控件
 *
 * @author Jieven
 *
 */
public class WidgetController extends BaseController {

	/**
	 * 查找Dialog
	 */
	public void toFind() {
		render("/eova/widget/find/find.html");
	}

	/**
	 * 查找框Dialog
	 */
	public void find() {

		List<Object> parmList = new ArrayList<Object>();

		String url = "";
		String exp = getPara("exp");
		String code = getPara("code");
		String field = getPara("field");
		boolean isMultiple = getParaToBoolean("multiple", false);
		// 自定义表达式
		if (xx.isEmpty(exp)) {
			// 根据表达式获取ei
			MetaField ei = MetaField.dao.getByObjectCodeAndEn(code, field);
			exp = ei.getStr("exp");
			url += "code=" + code + "&field=" + field;
		} else {
			url += "exp=" + exp;
			// 预处理表达式
			try {
				String[] strs = exp.split(",");
				if (strs.length > 0) {
					exp = EovaConfig.getExps().get(strs[0]);
					for (int i = 1; i < strs.length; i++) {
						parmList.add(getSqlParam(strs[i]));
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("预处理自定义查找框表达式异常，Exp=" + exp);
			}
		}

		// 动态解析变量和逻辑运算
		exp = buildExp(exp);

		// 根据表达式构建元数据
		EovaExp se = new EovaExp(exp);
		MetaObject mo = se.getObject();
		List<MetaField> mfs = se.getFields();
		I18NBuilder.models(mfs, "cn");
		if (isMultiple) {
			mo.set("is_single", false);
		}
		// mo.set("is_celledit", true);
		// for (MetaField mf : mfs) {
		// mf.set("is_edit", true);
		// mf.put("editor", "eovatext");
		// }
		//		if (exp.endsWith("tree")) {
		//			url = "/widget/findTree?" + url;
		//		} else {
		//		}
		url = "/widget/findJson?" + url;
		setAttr("action", url);
		// 用于Grid呈现
		setAttr("objectJson", JsonKit.toJson(mo));
		setAttr("fieldsJson", JsonKit.toJson(mfs));
		// 用于query条件
		setAttr("itemList", mfs);
		setAttr("pk", se.pk);

		toFind();
	}

	/**
	 * Find Dialog Grid Get JSON
	 */
	public void findJson() {

		String exp = getPara("exp");
		String code = getPara("code");
		String en = getPara("field");

		try {
			List<Object> paras = new ArrayList<Object>();

			exp = buildExp(paras, exp, code, en);
			// 动态解析变量和逻辑运算
			exp = buildExp(exp);

			// 解析表达式
			EovaExp se = new EovaExp(exp);

			// 获取分页参数
			int pageNumber = getParaToInt(PageConst.PAGENUM, 1);
			int pageSize = getParaToInt(PageConst.PAGESIZE, 15);

			String sql = WidgetManager.buildExpSQL(this, se, paras);

			Page<Record> page = Db.use(se.ds).paginate(pageNumber, pageSize, se.simpleSelect, sql, xx.toArray(paras));
			
			I18NBuilder.records(page.getList(), se.cn);
			
			// 将分页数据转换成JSON
			String json = JsonKit.toJson(page.getList());
			json = "{\"total\":" + page.getTotalRow() + ",\"rows\":" + json + "}";
			renderJson(json);
		} catch (Exception e) {
			e.printStackTrace();
			renderJson(Easy.fail("查找框查询数据异常:" + e.getMessage()));
		}
	}

	/**
	 * Find get CN by value
	 */
	public void findCnByEn() {

		String value = getPara("val");
		
		String code = getPara("code");
		String field = getPara("field");
		String exp = getPara("exp");
		List<Object> paras = new ArrayList<Object>();
		// 构建表达式
		exp = buildExp(paras, exp, code, field);
		// 动态解析变量和逻辑运算
		exp = buildExp(exp);

		// 解析表达式
		EovaExp se = new EovaExp(exp);
		List<Record> txts = null;
		//元数据开始exp属性后 编辑操作同不解析exp表达式 
		MetaObject object = sm.meta.getMeta(code);
		if(null != object.get("is_enable_exp")&&object.getBoolean("is_enable_exp")==true) {
			
			String ds = se.ds;

			// 查询本次所有翻译值
			StringBuilder sb = new StringBuilder();
			if (!xx.isEmpty(value)) {
				sb.append(se.pk);
				sb.append(" in(");
				// 根据当前页数据value列查询外表name列
				for (String id : value.split(",")) {
					// TODO There might be a sb injection risk warning
					sb.append(xx.format(id)).append(",");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
			}
			// System.out.println(sb.toString());

			// 根据表达式查询获得翻译的值
			String sql = WidgetManager.addWhere(se, sb.toString());
			txts = Db.use(ds).find(sql, xx.toArray(paras));
		}		
		
		// 没有翻译值，直接返回原值
		if (xx.isEmpty(txts)) {
			JSONObject json = new JSONObject();
			json.put("code", 0);
			json.put("data", value);
			renderJson(json.toJSONString());
			return;
		}
		
		I18NBuilder.records(txts, se.cn);

		JSONObject json = new JSONObject();
		json.put("code", 1);
		json.put("text_field", se.cn);// 文本字段名
		json.put("data", JsonKit.toJson(txts));// 翻译字典数据
		renderJson(json.toJSONString());
	}

	/**
	 * Combo Load Data Get JSON
	 */
	public void comboJson() {
		String objectCode = getPara(0);
		String en = getPara(1);
		String exp = getPara("exp");

		try {
			List<Object> paras = new ArrayList<Object>();
			// 构建表达式
			exp = buildExp(paras, exp, objectCode, en);
			// 动态解析变量和逻辑运算
			exp = buildExp(exp);

			// 解析表达式
			EovaExp se = new EovaExp(exp);

			String sql = se.sql;

			// 全局数据拦截条件
			if (EovaConfig.getEovaIntercept() != null) {
				EovaContext ec = new EovaContext(this);
				ec.exp = se;
				String condition = EovaConfig.getEovaIntercept().filterExp(ec);
				sql = SqlUtil.addCondition(sql, condition);
			}
			// 缓存配置
			String cache = se.getPara("cache");
			List<Record> list = null;
			if (xx.isEmpty(cache)) {
				list = Db.use(se.ds).find(sql, xx.toArray(paras));
			} else {
				list = Db.use(se.ds).findByCache(cache, sql, sql, xx.toArray(paras));
			}

			I18NBuilder.records(list, "cn");

			renderJson(list);
		} catch (Exception e) {
			e.printStackTrace();
			renderJson(Easy.fail("下拉框查询数据异常:" + e.getMessage()));
		}
	}

	/**
	 * ComboTree Load Data Get JSON
	 */
	public void comboTreeJson() {

		String objectCode = getPara(0);
		String en = getPara(1);
		String exp = getPara("exp");

		try {
			List<Object> paras = new ArrayList<Object>();

			// 构建表达式
			exp = buildExp(paras, exp, objectCode, en);
			// 动态解析变量和逻辑运算
			exp = buildExp(exp);
			// 解析表达式
			EovaExp se = new EovaExp(exp);

			String sql = se.sql;

			// 全局数据拦截条件
			if (EovaConfig.getEovaIntercept() != null) {
				EovaContext ec = new EovaContext(this);
				ec.exp = se;
				String condition = EovaConfig.getEovaIntercept().filterExp(ec);
				sql = SqlUtil.addCondition(sql, condition);
			}

			// 缓存配置
			String cache = se.getPara("cache");
			List<Record> list = null;
			if (xx.isEmpty(cache)) {
				list = Db.use(se.ds).find(sql, xx.toArray(paras));
			} else {
				list = Db.use(se.ds).findByCache(cache, sql, sql, xx.toArray(paras));
			}
			TreeConfig treeConfig = new TreeConfig();
			treeConfig.setIdField("id");
			treeConfig.setTreeField("name");
			treeConfig.setParentField("pid");
			treeConfig.setRootPid(se.getPara("root", "0"));// 获取表达式自定义参数中rootid,默认为0
			// treeConfig.setIconField("icon"); TODO 暂不支持自定义Tree图标

			// 有条件时，自动方向查找父节点数据
			if (!xx.isEmpty(sql.toLowerCase().concat("where"))) {
				// 向上查找父节点数据
				WidgetManager.findParent(treeConfig, se.ds, se.select, se.table, se.pk, list, list);
			}
			renderJson(list);
		} catch (Exception e) {
			e.printStackTrace();
			renderJson(Easy.fail("下拉树查询数据异常:" + e.getMessage()));
		}
	}

	/**
	 * 获取表达式
	 * @param parmList SQL动态参数
	 * @param exp 自定义表达式
	 * @param objectCode 元对象编码
	 * @param field 元字段名
	 * @return
	 */
	private String buildExp(List<Object> parmList, String exp, String objectCode, String field) {
		if (xx.isEmpty(exp)) {
			// 根据表达式获取exp
			MetaField ei = MetaField.dao.getByObjectCodeAndEn(objectCode, field);
			exp = ei.getStr("exp");
		} else {
			exp = exp.trim();
			// 预处理表达式
			try {
				String[] strs = exp.split(",");
				if (strs.length > 0) {
					exp = EovaConfig.getExps().get(strs[0]);
					if (xx.isEmpty(exp)) {
						System.err.println(String.format("无法获取到表达式,请检查表达式配置,表达式Key=%s,添加新的表达式后重启服务才能生效!", strs[0]));
						throw new RuntimeException();
					}
					for (int i = 1; i < strs.length; i++) {
						parmList.add(getSqlParam(strs[i]));
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("预处理自定义查找框表达式异常，Exp=" + exp);
			}
		}
		return exp;
	}

	/**
	 * 获取SQL参数，优先Integer，不能转就当String
	 *
	 * @param str
	 * @return
	 */
	private static Object getSqlParam(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return str;
		}
	}

	private String buildExp(String exp) {
		return DynamicParse.buildSql(exp, (User) this.getSessionAttr(EovaConst.USER));
	}

}