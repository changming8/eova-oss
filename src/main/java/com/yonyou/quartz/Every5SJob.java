package com.yonyou.quartz;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.webapp.WebAppContext.Context;
import org.quartz.JobExecutionContext;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.youyou.util.ExcuteClass;

/**
 * 每分钟执行
 * 
 * @author changjr
 *
 */
public class Every5SJob extends AbsJob {

	@Override
	protected void process(JobExecutionContext context) {
		System.out.println("每5S任务");
		String name = this.getClass().getName();
		List<Record> res = Db.find("select id from eova_task where clazz = ?", name);
		String taskid = res.get(0).getStr("id");
		System.out.println(taskid);
		List<Record> flows = Db.use(xx.DS_EOVA)
				.find("select * from bs_data_flow where dr =0 and task_id = ? and task_state = 1", taskid);
		String sql = "select t.flowtype_executionclass ,b.flow_id from bs_flow_type t inner  join bs_data_flow_b b on t.id = b.flow_type_id where b.p_id =?";
		System.out.println(flows);
		for (Record r : flows) {
			List<Record> records = Db.find(sql, r.getStr("id"));
			if (!records.isEmpty()) {
				Thread t = new Thread(new ExecuteThread(records), "Thread");
				t.start();
			}

		}

	}
}
