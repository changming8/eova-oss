package com.yonyou.statusFlow;

import com.yonyou.base.AbstractFlowExecuteClass;
import com.yonyou.base.ResponseBody;

public class StatusFlowExecuteClass extends AbstractFlowExecuteClass{

	@Override
	public ResponseBody process(String flow_id) {
		// TODO Auto-generated method stub
		return new StatusFlowService().statusFlowExcute(flow_id);
	}

}
