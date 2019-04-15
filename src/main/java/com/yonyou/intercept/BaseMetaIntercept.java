package com.yonyou.intercept;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.yonyou.util.UUID;


public class BaseMetaIntercept extends MetaObjectIntercept {
	@Override
	public String addBefore(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		ac.record.set("id", UUID.getUnqionPk());
		return super.addBefore(ac);
	}

}
