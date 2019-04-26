package com.yonyou.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.model.MdDefModel;
import com.yonyou.util.UUID;

public class MdStyleController extends BaseController {

	public void lineSave() {
		try {
			Map row=  (Map) JSONArray.parse(getPara("param"));
			Record record =new Record();
			record.setColumns(row);
			record.remove("pk_val");
			Db.use(xx.DS_EOVA).update("bs_style_b", record);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		renderJson(Easy.sucess());
	}

}
