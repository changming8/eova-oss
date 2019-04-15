package com.yonyou.quartz;

import org.quartz.JobExecutionContext;
/**
 * 每天
 * @author changjr
 *
 */
public class EveryDayJob extends AbsJob {

	@Override
	protected void process(JobExecutionContext context) {
		// JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		// String strData = dataMap.getString("type");
		System.out.println("每日任务");
	}

}
