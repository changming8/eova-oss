package com.yonyou.controller;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.common.base.BaseController;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.model.MdDefModel;
import com.yonyou.util.UUID;

public class MdDEFController extends BaseController {
	public void init() {
		System.out.println("Test.............");
		String[] type = new String[] { "template", "output", "input", "mobile" };
//		存放 映射关系定义里面维护的 对照表名 和 被对照表名
		List<String> tableList = new ArrayList<>();
		List<String> bodytable = new ArrayList<>();
		String params = getPara("rows");
		JSONObject obj = (((JSONArray) JSONObject.parse(params)).getJSONObject(0));
		String id = obj.getString("id");
//		元数据定义 信息
		MetaObject mo = MetaObject.dao.getByCode("bs_md_def");
//		元数据 列明细 信息
		List<MetaField> fields = MetaField.dao.queryFields("bs_md_def");

		List<Record> hlist = MdDefModel.dao.getDefInfoById(id);
//		对照表
		String source = hlist.get(0).getStr("md_table");
		tableList.add(source);
//		获取被对照表-begin
		List<Record> blist = Db.find("select * from bs_md_def_b where dr= 0 and pid = ?", id);
		for (Record r : blist) {
			tableList.add(r.getStr("dest_table"));
		}
//		获取被对照表-end
//		进行保存 样式主表 begin
		List<Record> slist = new ArrayList<>();
		for (String t : tableList) {
			if (checkHasInit(t, id)) {
				continue;
			}
//			每一种表保存四个模板
			for (int i = 0; i < 4; i++) {
				Record record = new Record();
				record.set("id", UUID.getUnqionPk());
				record.set("sid", id);
				record.set("md_table", t);
				record.set("type", type[i]);
				record.set("smeta", "主数据关系定义");
				slist.add(record);
			}
			Db.batchSave("bs_style", slist, 1000);
			initStyleBody(t, id, type);
			slist.clear();
		}

//		进行保存 样式主表 end
//		进行保存 样式主表 begin
//		for (String body : tableList) {
//			initStyleBody(body, id, type);
//		}
//		进行保存 样式主表 end
		System.out.println(obj.get("id"));
	}

	/**
	 * 检查是否被已经初始化
	 * 
	 * @param mate
	 * @param sid
	 * @return
	 * @throws BussnissException
	 */
	private Boolean checkHasInit(String meta, String sid) {
		List res = Db.find("select * from bs_style where dr = 0 and sid = ? and md_table = ?", sid, meta);
		return !res.isEmpty();
	}

	private void initStyleBody(String meta, String sid, String[] type) {
		List<Record> slist = new ArrayList<>();
//		List<MetaField> fields = MetaField.dao.queryFields(meta);
		String sql = "select * from bs_metadata_b b where b.metadata_id = (select a.id from bs_metadata a where a.data_code = ? and dr = 0) and dr = 0"; 
		List<Record> fields = Db.find(sql, meta);
		for (String t : type) {
//			获取 pid
			String pid = this.queryId(meta, sid, t);
			for (int i = 0; i < fields.size(); i++) {
				Record record = new Record();
				record.set("id", UUID.getUnqionPk());
				record.set("pid", pid);
				record.set("col_code", fields.get(i).getStr("field_code"));
				record.set("col_name", fields.get(i).getStr("field_name"));
				record.set("col_align", "align");
				record.set("col_length", fields.get(i).getStr("field_length"));

				record.set("col_decimal", 0);
				record.set("isshow", "0");
				record.set("input_type", fields.get(i).getStr("input_type"));
				record.set("input_format", fields.get(i).getStr("input_format"));
				record.set("pid", pid);
				slist.add(record);
			}
		}

		Db.batchSave("bs_style_b", slist, 1000);
	}

	/**
	 * 主子分别保存时 获取主表的主键
	 * 
	 * @param mateName
	 * @param sid
	 * @param type
	 * @return
	 */
	private String queryId(String meta, String sid, String type) {

		List<Record> res = Db.find("select id from bs_style where dr = 0 and sid = ? and md_table = ? and type = ?", sid,
				meta, type);
		return res.get(0).getStr("id");
	}
}
