package com.yonyou.util;

public interface FileStatus {
	/**
	 * 保存
	 */
	String FREE = "0";
	/**
	 * 更新中
	 */
	String UPDATING = "1";
	/**
	 * 完成
	 */
	String FINISH = "2";
	/**
	 * 失败
	 */
    String FAIL = "4";
}
