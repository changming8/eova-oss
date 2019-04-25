/**
 * 
 */
package com.yonyou.controller;

import java.sql.SQLException;
import java.util.ArrayList;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.eova.model.EovaLog;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;

/**
 * @version:（版本，具体版本信息自己来定）
 * @Description: 自定义form按钮处理
 * @author: changjr
 * @date: date{time}
 */
public class DIYFormController extends BaseController {

	final Controller ctrl = this;

	/** 自定义拦截器 **/
	protected MetaObjectIntercept intercept = null;

	/** 异常信息 **/
	private String errorInfo = "";

	/** 当前操作的主对象 **/
	private final Record record = new Record();

	public void add() throws Exception {
		String objectCode = this.getPara(0);
		String[] params  = getParaMap().get("query_flowid");
		MetaObject object = sm.meta.getMeta(objectCode);

		// 字段禁用默认对新增无效
		for (MetaField mf : object.getFields()) {
			mf.put("is_disable", false);
		}

		// 构建关联参数值
		Record fixed = WidgetManager.getRef(this);

		// 业务拦截
		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl);
			ac.object = object;
			ac.fixed = fixed;
			intercept.addInit(ac);
		}
		object.getFields();
		for(MetaField field :object.getFields()) {
				field.set("defaulter", "0");
			
		}

		setAttr("fixed", fixed);
		setAttr("object", object);
		render("/eova/widget/form/add.html");
	}

	public void doAdd() throws Exception {

		String objectCode = this.getPara(0);

		final MetaObject object = sm.meta.getMeta(objectCode);

		// 获取当前操作数据
		WidgetManager.buildData(this, object, record, object.getPk(), true);

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());
		// 事务(默认为TRANSACTION_READ_COMMITTED)
		Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
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

					if (!xx.isEmpty(object.getTable())) {
						// 剥离虚拟字段
						Record t = WidgetManager.peelVirtual(record);
						Db.use(object.getDs()).save(object.getTable(), object.getPk(), record);
						// 还原虚拟字段
						record.setColumns(t);
					} else {
						// update view
						// WidgetManager.operateView(TemplateConfig.ADD, object, record);
						// 视图无法自动操作，请自定义元对象业务拦截完成持久化逻辑！
					}

					// 新增后置任务
					if (intercept != null) {
						String msg = intercept.addAfter(ac);
						if (!xx.isEmpty(msg)) {
							errorInfo = msg;
							return false;
						}
					}
				} catch (Exception e) {
					errorInfo = TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}

		});

		// 记录新增日志
		EovaLog.dao.info(this, EovaLog.ADD, object.getStr("code"));

		// 新增成功之后
		if (intercept != null) {
			try {
				ArrayList<Record> records = new ArrayList<Record>();
				records.add(record);

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

	public void update() throws Exception {

		// 获取关联参数
		Record fixed = WidgetManager.getRef(this);

		AopContext ac = new AopContext(ctrl);
		ac.fixed = fixed;

		// 初始化数据
		MetaObject object = buildFormData(true, ac);
		// 业务拦截
		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());
		if (intercept != null) {
			intercept.updateInit(ac);
		}

		setAttr("fixed", fixed);

		render("/eova/widget/form/update.html");
	}

	public void doUpdate() throws Exception {

		String objectCode = this.getPara(0);

		final MetaObject object = sm.meta.getMeta(objectCode);

		// 获取当前操作数据
		WidgetManager.buildData(this, object, record, object.getPk(), false);
		final Object pkValue = record.get(object.getPk());

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());
		// 事务(默认为TRANSACTION_READ_COMMITTED)
		boolean flag = Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
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

					if (!xx.isEmpty(object.getTable())) {
						// 剥离虚拟字段
						Record t = WidgetManager.peelVirtual(record);
						Db.use(object.getDs()).update(object.getTable(), object.getPk(), record);
						// 还原虚拟字段
						record.setColumns(t);
					} else {
						// update view
						// WidgetManager.operateView(TemplateConfig.UPDATE, object, record);
						// 视图无法自动操作，请自定义元对象业务拦截完成持久化逻辑！
					}

					// 修改后置任务
					if (intercept != null) {
						String msg = intercept.updateAfter(ac);
						if (!xx.isEmpty(msg)) {
							errorInfo = msg;
						}
					}
				} catch (Exception e) {
					errorInfo = TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}

		});

		// 记录新增日志
		EovaLog.dao.info(this, EovaLog.UPDATE, object.getStr("code") + "[" + pkValue + "]");

		// 修改成功之后
		if (intercept != null) {
			try {
				ArrayList<Record> records = new ArrayList<Record>();
				records.add(record);

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

	public void detail() throws Exception {
		AopContext ac = new AopContext(ctrl);
		MetaObject object = buildFormData(false, ac);
		// 业务拦截
		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());
		if (intercept != null) {
			intercept.updateInit(ac);
		}
		render("/eova/widget/form/detail.html");
	}

	/**
	 * 构建对象数据
	 */
	private MetaObject buildFormData(boolean isEdit, AopContext ac) {
		String objectCode = this.getPara(0);
		// 获取主键的值
		Object pkValue = getPara(1);

		MetaObject object = sm.meta.getMeta(objectCode);
		ac.object = object;

		// 根据主键获取对象
		Record record = Db.use(object.getDs()).findById(object.getView(), object.getPk(), pkValue);

		setAttr("record", record);
		setAttr("object", object);
		ac.record = record;

		return object;
	}

}
