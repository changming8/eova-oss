package com.yonyou.quartz;

import java.util.List;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.base.ResponseBody;
import com.yonyou.model.TableManagerModel;
import com.yonyou.util.DateUtil;
import com.yonyou.util.ExcuteClass;
import com.yonyou.util.LockUtils;
import com.yonyou.util.TableStatusLockUtils;
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
//			执行flow_id加锁_begin
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
				record.set("message", "流程ID: " + r.getStr("flow_id") + " 获取锁失败！请检查!");
				Db.use(xx.DS_EOVA).save("bs_data_flow_log", record);
				break;
			}
//			执行flow_id加锁_end

//			执行flow_id相关联表加锁_begin	
			List<String> tables = TableManagerModel.dao.findTableByFlowID(r.getStr("flow_id"));
			boolean tflag = LockUtils.lockTable(tables,
					r.getStr("flowtype_code") + "-" + r.getStr("flowtype_executionclass") + r.getStr("flowtype_name"));
			if (!tflag) {
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
				record.set("message", "流程ID: " + r.getStr("flow_id") + "相关表信息 " + tables + " 获取PK锁失败！请检查!");
				Db.use(xx.DS_EOVA).save("bs_data_flow_log", record);
				break;
			}
//			执行flow_id相关联表加锁_end	
//			按照流程进行表状态锁
			boolean tsflag = TableStatusLockUtils.lockFlowTable(r.getStr("flow_id"));
			if (!tsflag) {

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
				record.set("message", "流程ID: " + r.getStr("flow_id") + "相关表信息 " + tables + " 获取表状态锁失败！请检查!");
				Db.use(xx.DS_EOVA).save("bs_data_flow_log", record);
//				释放锁table
				LockUtils.unLockTable(tables);
//				
				break;

			}
			ResponseBody res = (ResponseBody) ExcuteClass.excuet(r.getStr("flowtype_executionclass"), null, "process",
					r.getStr("flow_id"));

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
//				异常更新表状态
				TableStatusLockUtils.unLockTableStatus_ERR(r.getStr("flow_id"));
//				释放锁table
				LockUtils.unLockTable(tables);

//				释放锁flow_id
				LockUtils.unpkLock(r.getStr("flow_id"));
				record.set("endtime", endTime);
				record.set("status", "执行失败");
				record.set("message", res.getMes());
				Db.use(xx.DS_EOVA).save("bs_data_flow_log", record);
				break;
			}
//			正常更新表状态
			TableStatusLockUtils.unLockTableStatus_OK(r.getStr("flow_id"));
//			释放锁table
			LockUtils.unLockTable(tables);

//			释放锁flow_id
			LockUtils.unpkLock(r.getStr("flow_id"));
			record.set("endtime", endTime);
			record.set("status", "执行成功");
			record.set("message", res.getMes());
			Db.use(xx.DS_EOVA).save("bs_data_flow_log", record);
		}
	}

}
