package com.yonyou.sqlflow;

import com.yonyou.base.AbstractFlowExecuteClass;
import com.yonyou.base.ResponseBody;


public class SqlFlowExecuteClass extends AbstractFlowExecuteClass {


	@Override
	public ResponseBody process(String flow_id) {
		// TODO Auto-generated method stub
		
		return new SqlFlowService().sqlExcute(flow_id);
	}

}
