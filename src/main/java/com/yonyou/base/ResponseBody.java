package com.yonyou.base;
/**
 * 流程执行类 返回信息 
 * @author changjr
 *2019年4月17日10:59:47
 */
public class ResponseBody {
	public int status;
	public String mes;

	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getMes() {
		return mes;
	}
	public void setMes(String mes) {
		this.mes = mes;
	}

}
