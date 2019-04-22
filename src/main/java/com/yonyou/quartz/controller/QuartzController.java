package com.yonyou.quartz.controller;
/**
 * 流程立即执行
 * @author changjr
 * 2019年4月19日17:15:59
 */

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.quartz.ExecuteThread;

public class QuartzController extends BaseController {
	/**
	 * 立即执行
	 */
	public void now() {
		String params = getPara("rows");
		JSONObject obj = (((JSONArray) JSONObject.parse(params)).getJSONObject(0));
		String id = obj.getString("id");

		List<Record> flows = Db.use(xx.DS_EOVA)
				.find("select * from bs_data_flow where dr =0 and id = ? and task_state = 1", id);
		String sql = "select t.flowtype_executionclass ,b.flow_id ,b.flow_code,b.flow_name,b.flow_sort,b.pid ,t.flowtype_code,t.flowtype_name  from bs_flow_type t inner  join bs_data_flow_b b on t.id = b.flowtype_id where b.pid =?  order by b.flow_sort";
		System.out.println(flows);
		for (Record r : flows) {
			List<Record> records = Db.use(xx.DS_EOVA).find(sql, r.getStr("id"));
			if (!records.isEmpty()) {
				Thread t = new Thread(new ExecuteThread(records), r.getStr("flow_code"));
//				立即执行
				t.run();
			}

		}
		renderJson(Easy.sucess());
	}

}
