package com.yonyou.model;

import java.util.List;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.FileStatus;

public class FileManagerModel extends BaseModel<FileManagerModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4496747956028367146L;

	public static FileManagerModel dao = new FileManagerModel();

	/**
	 * 更新数据文件状态 fileName 文件名称 status 稳健状态
	 * 
	 * @throws Exception
	 **/

	public int updataFileStatus(String fileName, String status) throws Exception {

		int flag = 0;

		flag = Db.update("update bs_filemanager set status = ? where dataname =?", status, fileName);
		if (status.equals(FileStatus.FINISH)) {
			UpdateDescFileStatus(fileName);
		}
		return flag;

	}

	/**
	 * 更新数据DESC文件状态 DescName 文件名称 status 文件状态
	 * 
	 * @throws Exception
	 **/

	public int updataDescStatus(String DescName, String status) throws Exception {

		int flag = 0;

		flag = Db.update("update bs_filemanager set status = ? where allname =?", status, DescName);
		return flag;

	}

	/**
	 * 检查数据文件是否存在 文件名
	 */
	public boolean checkFileExist(String fileName) throws Exception {
		String sql = "select 1 from bs_filemanager where  dataname = ?";
		List<Record> res = Db.find(sql, fileName);
		return !res.isEmpty();

	}

	/**
	 * 检查Desc文件是否存在 文件名
	 */
	public boolean checkDescExist(String descName) throws Exception {
		String sql = "select 1 from bs_filemanager where  allname = ?";
		List<Record> res = Db.find(sql, descName);
		return !res.isEmpty();

	}

	/**
	 * 查询desc文件所有状态的数据文件 descName 文件名称 status 对应的状态 返回 对应状态的文件名
	 */
	public List<Record> queryDataFileByDesc(List<String> descNames, String status) throws Exception {
		String sql = "select * from bs_filemanager where did in (select id from bs_filemanager where  allname " + List2WhereIn(descNames)
				+ " and status = ? and did is null )";
		List<Record> res = Db.find(sql, status);
		return res;
	}

	public int UpdateDescFileStatus(String fileName) throws Exception {
		int flag = 0;
		String sql = "select 1 from bs_filemanager where allname =(select allname from bs_filemanager where dataname = ?) and status <> '2' and did is not null";
		List<Record> res = Db.find(sql, fileName);
		if (res.isEmpty()) {
			String sql1 = "update bs_filemanager set status = '2'"
					+ " where allname =(select * from (SELECT allname FROM bs_filemanager WHERE dataname = ? ) a  ) and did is null";
			flag = Db.update(sql1, fileName);
		}
		return flag;

	}

	private String List2WhereIn(List<String> descNames) {
		if (descNames.isEmpty()) {
			return "<> 1";
		}
		String sql = "in ('";
		for (String s : descNames) {
			sql = sql + s + "',";
		}
		sql = xx.delEnd(sql.toString(), ",") + ")";
		return sql;

	}
}
