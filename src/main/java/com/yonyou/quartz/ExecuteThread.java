package com.yonyou.quartz;

import java.util.List;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.base.ResponseBody;
import com.yonyou.util.DateUtil;
import com.yonyou.util.ExcuteClass;
import com.yonyou.util.LockUtils;
import com.yonyou.util.UUID;

/**
 * 流程执行类
 * 
 * @author changjr
 *
 */
public class ExecuteThread implements Runnable {

	private List<Record> record;

	public ExecuteThread(List<Record> record) {
		super();
		this.record = record;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (Record r : this.record) {
			Record record = new Record();
//			开始时间;
			String startTime = DateUtil.findSystemDateString();
//			执行加锁
			boolean flag = LockUtils.pkLock(r.getStr("flow_id"),
					r.getStr("flowtype_code") + "-" + r.getStr("flowtype_executionclass") + r.getStr("flowtype_name"));
			if (!flag) {
				record.set("id", UUID.getUnqionPk());
				record.set("pid", r.getStr("pid"));
				record.set("flow_id", r.getStr("flow_id"));
				record.set("flow_code", r.getStr("flow_id"));
				record.set("flow_code", r.getStr("flow_code"));
				record.set("flow_name", r.getStr("flow_name"));
				record.set("flow_sort", r.getStr("flow_sort"));
				record.set("flowtype_code", r.getStr("flowtype_code"));
				record.set("runclass", r.getStr("flowtype_executionclass"));
				record.set("flowtype_name", r.getStr("flowtype_name"));
				record.set("starttime", startTime);
				record.set("endtime", DateUtil.findSystemDateString());
				record.set("status", "执行失败");
				record.set("message", "获取锁失败！请检查!");
				Db.use(xx.DS_EOVA).save("bs_data_flow_log", record);
				break;
			}

			ResponseBody res = (ResponseBody) ExcuteClass.excuet(r.getStr("flowtype_executionclass"), null, "process",
					r.getStr("flow_id"));
//			释放锁
			LockUtils.unpkLock(r.getStr("flow_id"));
//			结束时间
			String endTime = DateUtil.findSystemDateString();
			record.set("id", UUID.getUnqionPk());
			record.set("pid", r.getStr("pid"));
			record.set("flow_id", r.getStr("flow_id"));
			record.set("flow_code", r.getStr("flow_id"));
			record.set("flow_code", r.getStr("flow_code"));
			record.set("flow_name", r.getStr("flow_name"));
			record.set("flow_sort", r.getStr("flow_sort"));
			record.set("flowtype_code", r.getStr("flowtype_code"));
			record.set("runclass", r.getStr("flowtype_executionclass"));
			record.set("flowtype_name", r.getStr("flowtype_name"));
			record.set("starttime", startTime);
			if (res.getStatus() != 0) {
				record.set("endtime", endTime);
				record.set("status", "执行失败");
				record.set("message", res.getMes());
				Db.use(xx.DS_EOVA).save("bs_data_flow_log", record);
				break;
			}
			record.set("endtime", endTime);
			record.set("status", "执行成功");
			record.set("message", res.getMes());
			Db.use(xx.DS_EOVA).save("bs_data_flow_log", record);
		}
	}

}
