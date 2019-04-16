package com.yonyou.quartz;

import java.util.List;

import com.jfinal.plugin.activerecord.Record;
import com.youyou.util.ExcuteClass;

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
			Boolean flag = (Boolean) ExcuteClass.excuet(r.getStr("flowtype_executionclass"), null, "process",
					r.getStr("flow_id"));
			if (!flag) {
				break;
			}
		}
	}

}
