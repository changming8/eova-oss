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

public class DataRelationMaintenanceController extends BaseController{
	
	final Controller ctrl = this;

	/** 元对象业务拦截器 **/
	protected MetaObjectIntercept intercept = null;

	/** 异常信息 **/
	private String errorInfo = "";
	
	public void dataRelationMaintenance() {
		render("/eova/dataRelationMaintenance/dataRelationMaintenance.html");
	}
	public void dataFlow() {
		render("/eova/dataflow/dataFlow.html");
	}
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
			sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true)+" where type='template' and sid='"+para1+"' and stable='"+para2+"'";
		}else if("bs_style_b".equals(where)) {
			sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true)+" where pid='"+para1+"' and isshow=0" ;
		}else if("select".equals(where)) {
			sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true)+" where type='template' and sid!='"+para1+"' and stable!='"+para2+"'" ;
		} /*
			 * else if("selectSearchSalve".equals(where)) { sql =
			 * WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras,
			 * true)+" where pid='"+para1+"' and isshow=0" ; }
			 */
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
}
