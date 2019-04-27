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

import com.eova.common.utils.xx;
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

//		fileName = "D:\\soft\\M002-COMPANY001-FMP-20190313-1-001-A.DESC.OK";
		String workpath = fileName.substring(0, fileName.lastIndexOf("/"));
		String descName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
//		String descName = arr[arr.length - 1];
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
		String field_1 = context.get(0);
		String field_2 = "null";
		if (len > 4000) {
			field_1 = context.get(0).substring(0, 4000);
			field_1 = context.get(0).substring(4000, context.get(0).length());
		} else {
			field_1 = context.get(0);
			field_2 = "null";
		}
		String table = "bs_filemanager";
		String did = null;
		List<Record> slist = new ArrayList<>();
		for (int i = 0; i < context.size(); i++) {
			if (i == 0) {
				Record record = new Record();
				did = UUID.getUnqionPk();
				record.set("id", did);
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
				record.set("workpath", workpath);
				Db.use(xx.DS_EOVA).save("bs_filemanager", record);
				System.out.println(did);
			} else {
				Record record = new Record();
				String[] txt = context.get(i).split(",");
				Map<String, Object> fileManage = new HashMap<>();
				record.set("id", UUID.getUnqionPk());
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
				record.set("workpath", workpath);
				slist.add(record);
				System.out.println(did);
			}
		}
		Db.use(xx.DS_EOVA).batchSave("bs_filemanager", slist, 1000);
		return context;
	}

	public boolean saveDesc_ERR_OK(String DESCName) throws Exception {

//		fileName = "D:\\soft\\M002-COMPANY001-FMP-20190313-1-001-A.DESC.OK";
		String workpath = DESCName.substring(0, DESCName.lastIndexOf("/"));
		String descName = DESCName.substring(DESCName.lastIndexOf("/") + 1, DESCName.length());
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
		record.set("type", type);
		record.set("allname", allname);
		record.set("plat_code", plat_code);
		record.set("date_code", date_code);
		record.set("workpath", workpath);
		boolean flag = Db.use(xx.DS_EOVA).save("bs_filemanager", record);
		return flag;

	}
	
}
