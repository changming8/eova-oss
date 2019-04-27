package com.yonyou.controller;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.base.ResponseBody;
import com.yonyou.loadfiledata.LoadDataService;
import com.yonyou.sqlflow.SqlFlowService;
import com.yonyou.util.UUID;

public class SqlFlowController extends BaseController {

	public void copy(){
		Object j = keepPara("rows").getAttr("rows");
		JSONArray jsonlist = JSONArray.parseArray(j.toString());
		JSONObject json = (JSONObject) jsonlist.get(0);
		// 先查询 先复制以时间戳为结尾复制到FTP注册表中
	    String metadataSql = "select * from bs_sql_flow where id ='" + json.getString("id") + "'";
	    List<Record> metadataList = Db.use(xx.DS_EOVA).find(metadataSql);
	    String id = UUID.getUnqionPk();
	    Record record = new Record();
		record = metadataList.get(0).remove("ID");
		record.set("ID", id);
		String code = record.get("sql_code").toString();
		record.remove("sql_code");
		String serialCode = code + "_" + System.currentTimeMillis();
		record.set("sql_code",serialCode);
		// 插入
    	Db.use(xx.DS_EOVA).save("bs_sql_flow", record);
    	renderJson(Easy.sucess());
	}
	
	public void test() {
		SqlFlowService lds=new SqlFlowService();
		String	id=getSelectValue("id");
		System.out.println(id);
		ResponseBody rb=lds.sqlExcute(id);
		System.err.println(JSON.toJSONString(rb));
		if(rb.getStatus()==1) {
			renderJson(Easy.error(rb.getMes()));
		}else {
			renderJson(Easy.sucess());
		}
	}
}
