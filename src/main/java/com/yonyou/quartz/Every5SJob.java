package com.yonyou.quartz;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;

/**
 * 每分钟执行
 * @author changjr
 *
 */
public class Every5SJob extends AbsJob {

	@Override
	protected void process(JobExecutionContext context) {
		System.out.println("每5S任务");
		// context.getJobDetail().getJobDataMap().get("xx参数");
		List<String> flows =new ArrayList<>();
//		for (String flow : flows) {
//			try {
//				Class<?> forName = Class.forName("com.yonyou.util.quartz.warn");
//				Object newInstance = forName.newInstance();
//				Method method = forName.getMethod("executeBusiness", Class.forName("java.util.Map"));
//				method.invoke(newInstance, map);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}
}
