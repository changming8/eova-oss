package com.yonyou.quartz.test;

import com.yonyou.base.AbstractFlowExecuteClass;
/**
 * 流程实现 样式参照
 * @author changjr
 *2019年4月16日14:31:29
 */
public class Test extends AbstractFlowExecuteClass{

	@Override
	public boolean process(String flow_id) {
		// TODO Auto-generated method stub
		System.out.println("流程正在执行 参数为:"+flow_id);
		return true;
	}


}
