package com.yonyou.loadfiledata;

import com.yonyou.base.AbstractFlowExecuteClass;
import com.yonyou.base.ResponseBody;


public class LoadDataExecuteClass extends AbstractFlowExecuteClass {


	@Override
	public ResponseBody process(String flow_id) {
		// TODO Auto-generated method stub
		
		return new LoadDataService().loadData(flow_id);
	}

}
