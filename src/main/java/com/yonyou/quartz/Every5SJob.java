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
import com.yonyou.util.ExcuteClass;

/**
 * 每5s执行
 * 
 * @author changjr
 *
 */
public class Every5SJob extends AbsJob {

	@Override
	protected void process(JobExecutionContext context) {
		System.out.println("每5s任务");
		new CoreExecute().exe(context);
	}
}
