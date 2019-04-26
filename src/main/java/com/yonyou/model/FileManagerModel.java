package com.yonyou.model;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;

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

		flag = Db.use(xx.DS_EOVA).update("update bs_filemanager set status = ? where dataname =?", status, fileName);
		if (true) {
			UpdateDescFileStatus(fileName,status);
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

		flag = Db.use(xx.DS_EOVA).update("update bs_filemanager set status = ? where allname =?", status, DescName);
		return flag;

	}

	/**
	 * 检查数据文件是否存在 文件名
	 */
	public boolean checkFileExist(String fileName) throws Exception {
		String sql = "select 1 from bs_filemanager where  dataname = ?";
		List<Record> res = Db.use(xx.DS_EOVA).find(sql, fileName);
		return !res.isEmpty();

	}

	/**
	 * 检查Desc文件是否存在 文件名 下载判断
	 */
	public boolean checkDescExist(String descName) throws Exception {
		String sql = "select 1 from bs_filemanager where  allname = ? and transtype is null";
		List<Record> res = Db.use(xx.DS_EOVA).find(sql, descName);
		return !res.isEmpty();

	}
	
	/**
	 * 检查Desc文件是否存在 文件名 上传
	 */
	public boolean checkDescExistOfType(String descName,String type) throws Exception {
		String sql = "select 1 from bs_filemanager where  allname = ? and transtype=?";
		List<Record> res = Db.use(xx.DS_EOVA).find(sql, descName,type);
		return !res.isEmpty();

	}

	/**
	 * 查询desc文件所有状态的数据文件 descName 文件名称 status 对应的状态 返回 对应状态的文件名
	 */
	public List<Record> queryDataFileByDesc( String descName, String status) throws Exception {
		String sql = "select * from bs_filemanager where did in (select id from bs_filemanager where  allname " + descName
				+ " and status = ? and did is null )";
		List<Record> res = Db.use(xx.DS_EOVA).find(sql, status);
		return res;
	}

	private  int UpdateDescFileStatus(String fileName,String status ) throws Exception {
		int flag = 0;
		String sql = "select 1 from bs_filemanager where allname =(select allname from bs_filemanager where dataname = ?) and status <> ? and did is not null";
		List<Record> res = Db.use(xx.DS_EOVA).find(sql, fileName,status);
		if (res.isEmpty()) {
			String sql1 = "update bs_filemanager set status = ?"
					+ " where allname =(select * from (SELECT allname FROM bs_filemanager WHERE dataname = ? ) a  ) and did is null";
			flag = Db.use(xx.DS_EOVA).update(sql1, status,fileName);
			
		}
		return flag;

	}
	
	/**
	 * 解析式上传成功，存储数据表示上传成功
	 * @return
	 */
	public  boolean SaveDescAnalysisFile(String DESCName,String ftpPath) {

//		fileName = "D:\\soft\\M002-COMPANY001-FMP-20190313-1-001-A.DESC.OK";
		String workpath = DESCName.substring(0, DESCName.lastIndexOf("/"));
		String descName = DESCName.substring(DESCName.lastIndexOf("/") + 1);
		String[] values = descName.split("-");
//		系统编码
		String sys_code = values[0];
//		接口编码
		String itf_code = values[1];
//		平台编码
		String plat_code = values[2];
//		日期编码
		String date_code = values[3];
//		传输类型（全量更新增量）
		String type = values[values.length - 1].split("\\.")[0];
		String allname = descName;

		Record record = new Record();

		record.set("id", UUID.getUnqionPk());
		record.set("sys_code", sys_code);
		record.set("itf_code", itf_code);
		record.set("plat_code", plat_code);
		record.set("plat_code", plat_code);
		record.set("date_code", date_code);
		record.set("status", "2");
		record.set("type", type);
		record.set("allname", allname);
		record.set("plat_code", plat_code);
		record.set("date_code", date_code);
		record.set("workpath", workpath);
		record.set("transtype", "2");
		record.set("ftppath", ftpPath);
		boolean flag = Db.use(xx.DS_EOVA).save("bs_filemanager", record);
		return flag;

	}
	
	/**
	 * 固定式上传成功，存储数据表示上传成功
	 * @return
	 */
	public  boolean SaveDescFixedFile(String DESCName,String ftpPath) {

//		fileName = "D:\\soft\\M002-COMPANY001-FMP-20190313-1-001-A.DESC.OK";
		String workpath = DESCName.substring(0, DESCName.lastIndexOf("/"));
		String descName = DESCName.substring(DESCName.lastIndexOf("/") + 1);
		Record record = new Record();
		record.set("id", UUID.getUnqionPk());
		record.set("status", "2");
		record.set("allname", descName);
		record.set("workpath", workpath);
		record.set("transtype", "2");
		record.set("ftppath", ftpPath);
		boolean flag = Db.use(xx.DS_EOVA).save("bs_filemanager", record);
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
