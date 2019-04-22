package com.yonyou.intercept;

import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.xx;
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
		List<Record> record = Db.use(xx.DS_EOVA).find("select * from eova_menu where code = ?", menucode);
		JSONObject json = (JSONObject) JSONObject.parse(record.get(0).getStr("config"));
		String objectField = json.getString("objectField");
		String objectCode = json.getString("objectCode");
		JSONArray objects = json.getJSONArray("objects");
		JSONArray fields = json.getJSONArray("fields");
		return super.deleteAfter(ac);
	}
}
