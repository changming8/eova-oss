/**
 * 
 */
package com.yonyou.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.core.menu.config.MenuConfig;
import com.eova.model.Button;
import com.eova.model.Menu;
import com.eova.model.MetaObject;
import com.eova.model.User;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.template.single.SingleIntercept;
import com.eova.widget.WidgetManager;
import com.eova.widget.WidgetUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;
import com.yonyou.base.LockObject;
import com.yonyou.quartz.ExecuteThread;

/**
 * @version:（版本，具体版本信息自己来定）
 * @Description: （对类进行功能描述）
 * @author: changjr
 * @date: date{time}
 */
public class FlowTableStatusDefController extends BaseController {

	final Controller ctrl = this;

	/** 自定义拦截器 **/
	protected SingleIntercept intercept = null;

	public void list() {

		String menuCode = "tablestatus_def";

		// 获取元数据
		Menu menu = Menu.dao.findByCode(menuCode);
		MenuConfig config = menu.getConfig();
		String objectCode = "tablestatus_def";
		MetaObject object = MetaObject.dao.getByCode(objectCode);
		if (object == null) {
			throw new RuntimeException("元对象不存在,请检查是否存在?元对象编码=" + objectCode);
		}

		// 根据权限获取功能按钮
		User user = this.getSessionAttr(EovaConst.USER);
		List<Button> btnList = Button.dao.queryByMenuCode(menuCode, user.getRid());

		// 是否需要显示快速查询
		setAttr("isQuery", MetaObject.dao.isExistQuery(objectCode));

		setAttr("menu", menu);
		setAttr("btnList", btnList);
		setAttr("object", object);

		render("/eova/DIY/tablestatusdef.html");
	}

	/**
	 * 基于redis查询
	 * 
	 * @throws Exception
	 */
	public void query() throws Exception {
		/** 元对象业务拦截器 **/
		MetaObjectIntercept intercept = null;

		/** 异常信息 **/
		String errorInfo = "";
		String objectCode = getPara(0);
		String menuCode = getPara(1);
		String[] params = getParaMap().get("query_flowid");
		int pageNumber = getParaToInt(PageConst.PAGENUM, 1);
		int pageSize = getParaToInt(PageConst.PAGESIZE, 100000);

		MetaObject object = sm.meta.getMeta(objectCode);
		Menu menu = Menu.dao.findByCode(menuCode);

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());

		// 构建查询
		List<Object> paras = new ArrayList<Object>();
		String select = "select " + WidgetManager.buildSelect(object, RID());
		String sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true);
		if (params != null && params.length > 0) {
			sql = sql + " where flow_id = '" + params[0] + "'";
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
		// 根据表达式将数据中的值翻译成汉字 根据object自定义是否开启exp表达式解析,默认打开 需要时手动关闭
		if (null != object.get("is_enable_exp") && object.getBoolean("is_enable_exp") == true) {
			WidgetManager.convertValueByExp(this, object.getFields(), page.getList());
		}

		// 构建JSON数据
		StringBuilder sb = new StringBuilder(
				String.format("{\"total\":%s,\"rows\": %s}", page.getTotalRow(), JsonKit.toJson(page.getList())));

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
	 * 解锁
	 */
	public void unlock() {
		String params = getPara("rows");
		JSONObject obj = (((JSONArray) JSONObject.parse(params)).getJSONObject(0));
		String id = obj.getString("id");
		Redis.use(xx.DS_EOVA).hdel("PKLOCK", id);
		renderJson(Easy.sucess());
	}
	
//	参照多选联动
	public void multipleFind() {
		Easy res =new Easy();
		try {
			Map<String, String[]> param = getParaMap();
			String code = param.get("code")[0];
			List<Record> records = Db.use(xx.DS_EOVA).find("select * from bs_metadata where  dr = 0 and data_code = ?", code);
			
			res.setData(records);
			renderJson(res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res.setMsg("操作失败");
			renderJson(res);
		}
	}
}
