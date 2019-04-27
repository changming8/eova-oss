package com.yonyou.controller;

import java.util.List;

import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class DateCleanController extends BaseController {

	
	//获取数据清洗主表数据返回
	public void queryDataCleanById() {
		String id = getPara(0);
		String sql = "SELECT  table_id , dest_table from bs_clean_flow where id  ='"+id+"'";
		List<Record> record=Db.use(xx.DS_EOVA).find(sql);
		renderJson(record);
	}
}
