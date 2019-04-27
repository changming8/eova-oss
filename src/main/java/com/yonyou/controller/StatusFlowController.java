package com.yonyou.controller;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;

public class StatusFlowController extends BaseController {

	public void copy(){
		Object j = keepPara("rows").getAttr("rows");
		JSONArray jsonlist = JSONArray.parseArray(j.toString());
		JSONObject json = (JSONObject) jsonlist.get(0);
		// 先查询 先复制以时间戳为结尾复制到FTP注册表中
	    String metadataSql = "select * from bs_status_flow where id ='" + json.getString("id") + "'";
	    List<Record> metadataList = Db.use(xx.DS_EOVA).find(metadataSql);
	    String id = UUID.getUnqionPk();
	    Record record = new Record();
		record = metadataList.get(0).remove("ID");
		record.set("ID", id);
		String code = record.get("stauts_code").toString();
		record.remove("stauts_code");
		String serialCode = code + "_" + System.currentTimeMillis();
		record.set("stauts_code",serialCode);
		// 插入
    	Db.use(xx.DS_EOVA).save("bs_status_flow", record);
    	renderJson(Easy.sucess());
	}
	
	
	public void queryMetadataById() {
		String id=getPara(0);
		String sql="select * from bs_metadata where id="+id+"";
		List<Record> record=Db.use(xx.DS_EOVA).find(sql);
		renderJson(record);
	}
}
