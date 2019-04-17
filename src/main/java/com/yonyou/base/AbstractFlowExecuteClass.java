package com.yonyou.base;

/**
 * 流程注册类 的 基类 （约束作用）
 * @author changjr
 *
 */
public abstract class AbstractFlowExecuteClass {
	/**
	 *  流程注册基类 入口方法(规范)
	 *
	 * @param flow_id 活动ID(bs_data_flow_b表的flow_id字段)
	 * 返回值 字符串类型
	 */
	public abstract ResponseBody process(String flow_id);
}
