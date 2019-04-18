package com.yonyou.quartz;

import java.util.List;

import org.quartz.JobExecutionContext;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * Job任务核心执行活动
 * 
 * @author changjr 2019年4月16日14:17:53
 */
public class CoreExecute {

	public void exe(JobExecutionContext context) {
		String name = this.getClass().getName();
		List<Record> res = Db.find("select id from eova_task where clazz = ?", context.getJobInstance().getClass().getName());
		String taskid = res.get(0).getStr("id");
		System.out.println(taskid);
		List<Record> flows = Db.use(xx.DS_MAIN)
				.find("select * from bs_data_flow where dr =0 and task_id = ? and task_state = 1", taskid);
		String sql = "select t.flowtype_executionclass ,b.flow_id from bs_flow_type t inner  join bs_data_flow_b b on t.id = b.flowtype_id where b.pid =?";
		System.out.println(flows);
		for (Record r : flows) {
			List<Record> records = Db.find(sql, r.getStr("id"));
			if (!records.isEmpty()) {
				Thread t = new Thread(new ExecuteThread(records));
				t.start();
			}

		}

	}

}
