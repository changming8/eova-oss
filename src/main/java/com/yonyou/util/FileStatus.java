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
    /**
     * 下载中
     */
    String DOWNLOAD_UPDATING="001";
    /**
     * 下载成功
     */
    String DOWNLOAD_FINISH="002";
    /**
     * 下载成功
     */
    String DOWNLOAD_FAIL="003";
    /**
     * 上传完成
     */
    String UPLOAD_FINISH="102";
}
