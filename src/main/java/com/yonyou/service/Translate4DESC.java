package com.yonyou.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.FileStatus;
import com.yonyou.util.UUID;

public class Translate4DESC {
	public List<String> execute(String fileName) throws Exception {
		List<String> context = new ArrayList<>();
			context = readFile(fileName);
			saveInfo4DESC(context, fileName);

		return context;
	}

	/**
	 * 文件输入流
	 * 
	 * @throws FileNotFoundException
	 */
	public List<String> readFile(String fileName) throws FileNotFoundException {

//		存储DESC文件解析内容
		List<String> context = new ArrayList<>();
		FileReader reader = null;
		BufferedReader br = null;
//		if (!file.exists()) {
//			throw new FileNotFoundException();
//		}
		try {
			reader = new FileReader(fileName);
			br = new BufferedReader(reader);
			String line;
			while ((line = br.readLine()) != null) {
				// 一次读入一行数据
				context.add(line);
//				System.out.println(line);
			}
			br.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return context;
	}

	public List<String> saveInfo4DESC(List<String> context, String fileName) throws Exception {

//		fileName = "D:\\soft\\M002-COMPANY001-FMP-20190313-1-001-A.DESC";

		String[] arr = fileName.split("\\\\");
		String descName = arr[arr.length - 1];
		String[] values = descName.split("-");
//		系统编码
		String sys_code = values[0];
//		接口编码
		String itf_code = values[1];
//		平台编码
		String plat_code = values[2];
//		日期编码
		String date_code = values[3];
//		文件总数
		String count = String.valueOf(context.size() - 1);
//		传输类型（全量更新增量）
		String type = values[values.length - 1].replaceAll(".DESC", "");
		String allname = descName;
//获取字段序列长度  如果大于4000 则分段
		int len = context.get(0).getBytes("UTF-8").length;

		if (len > 4000) {

		}
		String field_1 = context.get(0);
		String field_2 = "null";
		String table = "BS_FILEMANAGE";
		String did = null;
		List<Record> slist = new ArrayList<>();
		for (int i = 0; i < context.size(); i++) {
			if (i == 0) {
				Record record =new Record();
				did = UUID.getUnqionPk();
				record.set("id",did);
				record.set("sys_code", sys_code);
				record.set("itf_code", itf_code);
				record.set("plat_code", plat_code);
				record.set("plat_code", plat_code);
				record.set("date_code", date_code);
				record.set("count", count);
				record.set("type", type);
				record.set("allname", allname);
				record.set("field_1", field_1);
				record.set("field_2", field_2);
				record.set("plat_code", plat_code);
				record.set("date_code", date_code);
				 Db.save("bs_manage", record);
				System.out.println(did);
			} else {
				Record record =new Record();
				String[] txt = context.get(i).split(",");
				Map<String, Object> fileManage = new HashMap<>();
				record.set("id",UUID.getUnqionPk());
				record.set("sys_code", sys_code);
				record.set("itf_code", itf_code);
				record.set("plat_code", plat_code);
				record.set("date_code", date_code);
				record.set("count", count);
				record.set("type", type);
				record.set("allname", allname);
				record.set("field_1", field_1);
				record.set("field_2", field_2);
				record.set("did", did);
				record.set("dataname", txt[0]);
				record.set("datasize", txt[1]);
				record.set("filenum", i);
				slist.add(record);
				System.out.println(did);
			}
		}
		Db.batchSave("bs_filemamage", slist, 1000);
		return context;
	}

	/**
	 * 更新数据文件状态 fileName 文件名称 status 稳健状态
	 * @throws Exception 
	 **/

	public int updataFileStatus(String fileName, String status) throws Exception {

		int flag = 0;

		Db.update("update bs_filemanage set status = ? where dataname =?", status,fileName);
		if (status.equals(FileStatus.FINISH)) {
		UpdateDescFileStatus(fileName);
	}
		return flag;

	}

	/**
	 * 检查数据文件是否存在 文件名
	 */
	public boolean checkFileExist(String fileName) throws Exception{
		String sql = "select 1 from bs_filemanage where  dataname = ?";
		List<Record> res = Db.find(sql, fileName);
		return !res.isEmpty();

	}

	/**
	 * 检查Desc文件是否存在 文件名
	 */
	public boolean checkDescExist(String descName) throws Exception{
		String sql = "select 1 from bs_filemanage where  allname = ?";
		List<Record> res = Db.find(sql, descName);
		return !res.isEmpty();

	}

	/**
	 * 查询desc文件所有状态的数据文件 descName 文件名称 status 对应的状态 返回 对应状态的文件名
	 */
	public List<Record> queryFileByStatus(String descName, String status) throws Exception{
		String sql = "select dataname from bs_filemanage where  allname = ? and status = ? and did is not null";
		List<Record> res = Db.find(sql, descName,status);
		return res;
	}

	public int UpdateDescFileStatus(String fileName) throws Exception {
		int flag = 0;
		String sql = "select 1 from bs_filemanage where allname =(select allname from bs_filemanage where dataname = ?) and status = '0' and did is not null";
		List<Record> res = Db.find(sql, fileName);
		if (res.isEmpty()) {
			String sql1 = "update bs_filemanage set status = '1'"
					+ "where allname =(select allname from bs_filemanage where dataname = ? ) and did is null";
			flag = Db.update(sql1, fileName);
		}
		return flag;

	}
}
