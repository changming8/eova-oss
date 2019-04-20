package com.yonyou.controller;

import java.util.ArrayList;
import java.util.List;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.eova.config.PageConst;
import com.eova.model.Menu;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.eova.widget.WidgetUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;

public class DataRelationMaintenanceController extends BaseController{
	
	final Controller ctrl = this;

	/** 元对象业务拦截器 **/
	protected MetaObjectIntercept intercept = null;

	/*
	 * 页面跳转
	 */
	public void dataRelationMaintenance() {
		render("/eova/dataRelationMaintenance/dataRelationMaintenance.html");
	}
	public void dataFlow() {
		render("/eova/dataflow/dataFlow.html");
	}
	/*
	 * dataRelationMaintenance大部分查询
	 * 
	 */
	public void queryBsStyle() throws Exception {
		String objectCode=getPara(0);
		String menuCode=getPara(1);
		String where=getPara(2);
		String para1=getPara(3);
		String para2=getPara(4);
		int pageNumber = getParaToInt(PageConst.PAGENUM, 1);
		int pageSize = getParaToInt(PageConst.PAGESIZE, 100000);

		MetaObject object = sm.meta.getMeta(objectCode);
		Menu menu = Menu.dao.findByCode(menuCode);

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());

		// 构建查询
		List<Object> paras = new ArrayList<Object>();
		String select =  "select " + WidgetManager.buildSelect(object, RID());
		String sql="";
		if("table_master_col".equals(where)) {
			sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true)+" where type='template' and sid='"+para1+"' and md_table='"+para2+"'";
		}else if("bs_style_b".equals(where)) {
			sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true)+" where pid='"+para1+"' and isshow=0" ;
		}else if("select".equals(where)) {
			sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true)+" where type='template' and sid='"+para1+"' and md_table!='"+para2+"'" ;
		}
		Page<Record> page = Db.use(object.getDs()).paginate(pageNumber, pageSize, select, sql, xx.toArray(paras));

		// 查询后置任务
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, page.getList());
			ac.object = object;
			intercept.queryAfter(ac);
		}

		// 备份Value列，然后将值列转换成Key列
		WidgetUtil.copyValueColumn(page.getList(), object.getPk(), object.getFields());
		// 根据表达式将数据中的值翻译成汉字
		WidgetManager.convertValueByExp(this, object.getFields(), page.getList());
		
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
	/*
	 * 新增数据到关系存储表
	 */
	public void insertMasterSlaveContrast() {
		String objectCode=getPara(0);
		String masterId=getPara(1);
		String slaveId=getPara(2);
		String masterCol=getPara(3);
		String slaveCol=getPara(4);
		String slaveTable=getPara(5);
		if("".equals(objectCode)) {
			renderJson("{\"message\":\"对照数据异常\"}");
			return;
		}
		Record record = new Record();
		record.set("mdid", masterId);
		record.set("md_column", masterCol);
		record.set("dest_table", slaveTable);
		record.set("dest_column", slaveCol);
		String[] slaveIds=slaveId.split(",");
		for(int i=0;i<slaveIds.length;i++) {
			record.set("id",UUID.getUnqionPk());
			record.set("destid",slaveIds[i]);
			Db.use(xx.DS_EOVA).save(objectCode, record);
		}
		renderJson("{\"message\":\"保存成功\"}");
	}
	/*
	 * 对照查询
	 */
	public void queryMaster() {
		String objectCode=getPara(0);
		List<String> record=Db.use(xx.DS_EOVA).query("SELECT column_name FROM information_schema.COLUMNS WHERE table_name='"+objectCode+"'");
		
		StringBuffer sb=new StringBuffer();
		for(String re : record) {
			sb.append(re+",");
		}
		String tempCol=sb.substring(0,sb.length()-1);
		String sql="select "+tempCol+" from "+objectCode;
		List<Record> recordData=Db.find(sql);
		renderJson(recordData);
	}
	/*
	 * 对照条件查询
	 */
	public void queryWhere() {
		String objectCode=getPara(0);
		String para1=getPara(1);
		String para2=getPara(2);
		
		String sql="select * from "+objectCode+" where "+para2+" like '%"+para1+"%'";
		List<Record> record=Db.find(sql);
		renderJson(record);
	}
	/*
	 * 启用对照
	 */
	public void startUpContrast() {
		String mid_table=getPara(0);
		String masterId=getPara(1);
		String dest_table=getPara(2);
		
		String sql_query_ids="select destid from "+mid_table+" where mdid='"+masterId+"' and dest_table='"+dest_table+"'";
		List<String> slaveIds=Db.query(sql_query_ids);
		if(slaveIds.size()<1) {
			renderJson("");
			return;
		}
		StringBuffer sql_query=new StringBuffer();
		sql_query.append("select * from "+dest_table+" where id in(");
		for(String slave:slaveIds) {
			sql_query.append("'"+slave+"',");
		}
		String sql=sql_query.substring(0,sql_query.lastIndexOf(",")).toString()+")";
		List<Record> queryData=Db.find(sql);
		renderJson(queryData);
	}
}
