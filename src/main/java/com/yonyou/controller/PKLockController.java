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
public class PKLockController extends BaseController {

	final Controller ctrl = this;

	/** 自定义拦截器 **/
	protected SingleIntercept intercept = null;

	public void list() {

		String menuCode = "pk_lock";

		// 获取元数据
		Menu menu = Menu.dao.findByCode(menuCode);
		MenuConfig config = menu.getConfig();
		String objectCode = "lock_log";
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

		render("/eova/PKLOCK/pklock.html");
	}

	/**
	 * 数据查询 //页面开启是否翻译exp 表达式内容 直接显示内容 编辑时可翻译
	 * 
	 * @throws Exception
	 */
	public void query() throws Exception {

		List<Record> list = new ArrayList<>();
		Map<Object, Object> map = Redis.use(xx.DS_EOVA).hgetAll("PKLOCK");
		for (Object obj : map.keySet()) {
			Record record = new Record();
			LockObject lock = (LockObject) map.get(obj);
			record.set("id", obj.toString());
			record.set("detail", lock.getDetail());
			record.set("date", lock.getLockDate());
			list.add(record);
		}
		Page page = new Page<>(list, 10000, 1, 1, list.size());
		// 构建JSON数据
		StringBuilder sb = new StringBuilder(
				String.format("{\"total\":%s,\"rows\": %s}", page.getTotalRow(), JsonKit.toJson(page.getList())));

		renderJson(sb.toString());
	}

	/**
	 * 立即执行
	 */
	public void unlock() {
		String params = getPara("rows");
		JSONObject obj = (((JSONArray) JSONObject.parse(params)).getJSONObject(0));
		String id = obj.getString("id");
		Redis.use(xx.DS_EOVA).hdel("PKLOCK", id);
		renderJson(Easy.sucess());
	}
}
