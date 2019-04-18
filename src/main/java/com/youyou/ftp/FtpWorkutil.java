package com.youyou.ftp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;


public class FtpWorkutil {

	/**
	 * 获取目录名
	 * @param ftp_id
	 * @return
	 */
	public String getWorkPath(String work_directory_id) {
		String sql = "select working_path from bs_working_directory where  id ='" + work_directory_id + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		String workName = "";
		for (int i = 0; i < list.size(); i++) {
			workName = list.get(i).get("working_path");
		}
		return workName;
	}
	
	
	/**
	 * 获取FTP信息
	 * @param ftp_id
	 * @return
	 */
	public List<Record> getFtpPath(String ftp_id) {
		String sql = "select ftp_path,ftp_port,ftp_address,ftp_username,ftp_password from bs_ftp_registry where  id ='" + ftp_id + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;

	}
}
