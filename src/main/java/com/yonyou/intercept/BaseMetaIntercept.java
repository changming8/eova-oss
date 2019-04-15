package com.yonyou.intercept;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.yonyou.util.UUID;

/**
 * 通用新增拦截器 处理主键生成
 * @author changjr
 *
 */
public class BaseMetaIntercept extends MetaObjectIntercept {
	@Override
	public String addBefore(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		ac.record.set("id", UUID.getUnqionPk());
		return super.addBefore(ac);
	}

}
