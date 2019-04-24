/**
 * 
 */
package com.yonyou.base;

import java.io.Serializable;

/** @version:（版本，具体版本信息自己来定） 
* @Description: （对类进行功能描述） 
* @author: changjr
* @date: 2019年4月22日17:37:19  
*/
public class LockObject  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String detail;
	private String lockDate;
	private String userName;
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getLockDate() {
		return lockDate;
	}
	public void setLockDate(String lockDate) {
		this.lockDate = lockDate;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	

}
