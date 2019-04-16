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
		System.out.println("每日任务");
		new CoreExecute().exe(context);
	}

}
