package com.youyou.ftp;

import com.yonyou.base.AbstractFlowExecuteClass;
import com.yonyou.base.ResponseBody;


public class FtpExecuteClass extends AbstractFlowExecuteClass {


	@Override
	public ResponseBody process(String flow_id) {
		// TODO Auto-generated method stub
		
		return new FtpService().File_Name(flow_id);
	}

}
