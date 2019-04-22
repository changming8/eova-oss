package com.yonyou.intercept;

import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.xx;
import com.eova.template.common.util.TemplateUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;

/**
 * 通用新增拦截器 处理主键生成
 * 
 * @author changjr
 *
 */
public class BaseMetaIntercept extends MetaObjectIntercept {
	@Override
	public String addBefore(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		ac.record.set("id", UUID.getUnqionPk());
		return super.addBefore(ac);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eova.aop.MetaObjectIntercept#deleteAfter(com.eova.aop.AopContext)
	 */
	@Override
	public String deleteAfter(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		String menucode = ac.ctrl.getPara();
//		获取菜单绑定的 主子映射关系
		List<Record> record = Db.use(xx.DS_EOVA).find("select * from eova_menu where code = ?", menucode);
		if (record.isEmpty()) {
			return super.deleteAfter(ac);
		}
		MetaObjectIntercept intercept = null;
		JSONObject json = (JSONObject) JSONObject.parse(record.get(0).getStr("config"));
//		主子 主表映射 名称
		String objectField = json.getString("objectField");
//		主表元数据编码
		String objectCode = json.getString("objectCode");
//		子表欧巴元数据编码聚合
		JSONArray objects = json.getJSONArray("objects");
//		主表外键
		JSONArray fields = json.getJSONArray("fields");
		String id = ac.record.get(objectField);
		for (int i = 0; i < objects.size(); i++) {
//			获取业务拦拦截器（待扩展）
			intercept = TemplateUtil.initMetaObjectIntercept(objects.getString(i));

			String sql = "delete from " + objects.getString(i) + " where " + fields.getString(i) + " =? ";
			Db.use(xx.DS_EOVA).update(sql, id);

		}
		return super.deleteAfter(ac);
	}
}
