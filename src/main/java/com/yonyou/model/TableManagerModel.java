package com.yonyou.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;

/**
 * @version:（版本，具体版本信息自己来定）
 * @Description: 流程表状态工具类
 * @author: changjr
 * @date: 2019年4月25日19:05:46
 */
public class TableManagerModel extends BaseModel<TableManagerModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4496747956028367146L;

	public static TableManagerModel dao = new TableManagerModel();

	/*
	 * @Description:
	 * 
	 * @param flow_id
	 * 
	 * @return
	 */
	public List<Record> findByFlowID(String flow_id) {
		List<Record> list = Db.use(xx.DS_EOVA).find("select * from bs_tablestatus_def where dr = 0 and flow_id = ?",
				flow_id);
		return list;
	}
	/**
	 * @Description: 返回flow_id相关联的表信息
	* @param flow_id
	* @return
	 */
	public List<String> findTableByFlowID(String flow_id) {
		List<Record> list = Db.use(xx.DS_EOVA).find("select table_code from bs_tablestatus_def where dr = 0 and flow_id = ?",
				flow_id);
		List<String> tables =new ArrayList<>();
		for(Record r : list) {
			tables.add(r.getStr("table_code"));
		}
		return tables;
	}

}
