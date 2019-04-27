package com.yonyou.statusFlow;

import java.util.List;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.base.ResponseBody;

public class StatusFlowService {

	public ResponseBody statusFlowExcute(String flowId) {
		List<Record> listProduct = Db.use(xx.DS_EOVA).find("select * from bs_status_flow where id =?",flowId);
		for (Record record : listProduct) {
			
		}
		return null;
	}
	
	/**
	 * 构建ResponseBody
	 * @param mes 消息内容
	 * @param Objectid 数据主键
	 * @param status 执行状态
	 * @param objecttype 功能类型
	 * @return
	 */
	private static ResponseBody getResponseBody(String mes, String Objectid, int status, int objecttype ) {
		ResponseBody spbody = new ResponseBody();
		spbody.setStatus(status);
		spbody.setMes(mes);
		spbody.setObjectid(Objectid);
		spbody.setObjecttype(objecttype);
		return spbody;
	}
}
