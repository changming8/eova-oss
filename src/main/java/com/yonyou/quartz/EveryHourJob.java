package com.yonyou.quartz;

import org.quartz.JobExecutionContext;

/**
 * 每小时执行
 * @author changjr
 *
 */
public class EveryHourJob extends AbsJob {

	@Override
	protected void process(JobExecutionContext context) {
		System.out.println("每小时任务");
		new CoreExecute().exe(context);
	}

}
