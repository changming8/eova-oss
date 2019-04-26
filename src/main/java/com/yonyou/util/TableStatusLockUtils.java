package com.yonyou.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.model.TableManagerModel;

/**
 * @version:（版本，具体版本信息自己来定）
 * @Description: 流程表锁工具类
 * @author: changjr
 * @date: 2019年4月25日19:24:47
 */
public class TableStatusLockUtils {
	/*
	 * @Description:获取流程表状态信息 
	 * 
	 * @param flow_id
	 * 
	 * @return
	 */
	public static boolean lockFlowTable(String flow_id) {
		List<Record> list = TableManagerModel.dao.findByFlowID(flow_id);
		List<String> where = new ArrayList<>();

		List<Record> save = new ArrayList<>();
		for (Record r : list) {
			where.add(r.getStr("table_code"));
		}
		Map<String, String> t_s = TableStatusLockUtils.queryTableStatus(where);
		for (Record r : list) {
			String s = r.getStr("table_code");
			if (!t_s.containsKey(s)) {
				Db.use(xx.DS_EOVA).update(
						"INSERT INTO bs_table_status (table_id,table_code,table_name status) VALUES (?,?,?, ?)",
						r.getStr("table_id"), s, r.getStr("table_name"), "000");
				continue;
			} else {
				if (s != t_s.get(s)) {
					return false;
				}
			}
			Record record = new Record();
			record.set("table_code", s);
			record.set("status", r.getStr("initstatus_id"));
			save.add(record);
		}

		Db.use(xx.DS_EOVA).batchUpdate("bs_table_status", "table_code", save, 1000);
		return true;
	}

	/**
	 * @Description:流程正常执行结束 解锁方法
	 * 
	 * @param flow_id
	 * 
	 * @return
	 */
	public static boolean unLockTableStatus_OK(String flow_id) {

		List<Record> list = TableManagerModel.dao.findByFlowID(flow_id);
		List<String> where = new ArrayList<>();

		List<Record> save = new ArrayList<>();
		for (Record r : list) {
			where.add(r.getStr("table_code"));
		}
		Map<String, String> t_s = TableStatusLockUtils.queryTableStatus(where);
		for (Record r : list) {
			String s = r.getStr("table_code");
			if (!t_s.containsKey(s)) {
				Db.use(xx.DS_EOVA).update(
						"INSERT INTO bs_table_status (table_id,table_code,table_name status) VALUES (?,?,?, ?)",
						r.getStr("table_id"), s, r.getStr("table_name"), "000");
				continue;
			}
			Record record = new Record();
			record.set("table_code", s);
			record.set("status", r.getStr("successstatus_id"));
			save.add(record);
		}

		Db.use(xx.DS_EOVA).batchUpdate("bs_table_status", "table_code", save, 1000);
		return true;
	

	}

	/**
	 * @Description:流程异常执行结束 解锁方法
	 * 
	 * @param flow_id
	 * 
	 * @return
	 */
	public static boolean unLockTableStatus_ERR(String flow_id) {


		List<Record> list = TableManagerModel.dao.findByFlowID(flow_id);
		List<String> where = new ArrayList<>();

		List<Record> save = new ArrayList<>();
		for (Record r : list) {
			where.add(r.getStr("table_code"));
		}
		Map<String, String> t_s = TableStatusLockUtils.queryTableStatus(where);
		for (Record r : list) {
			String s = r.getStr("table_code");
			if (!t_s.containsKey(s)) {
				Db.use(xx.DS_EOVA).update(
						"INSERT INTO bs_table_status (table_id,table_code,table_name status) VALUES (?,?,?, ?)",
						r.getStr("table_id"), s, r.getStr("table_name"), "000");
				continue;
			}
			Record record = new Record();
			record.set("table_code", s);
			record.set("status", r.getStr("failestatus_id"));
			save.add(record);
		}

		Db.use(xx.DS_EOVA).batchUpdate("bs_table_status", "table_code", save, 1000);
		return true;
	

	

	}

	/**
	 * @Description:
	 * 
	 * @param table_code
	 * 
	 * @param status
	 * 
	 * @return
	 */
	public int updataTableStatus(String table_code, String status) {
		int count = Db.use(xx.DS_EOVA).update("update bs_table_status set status = ? where dr = 0 and table_code = ? ",
				status, table_code);
		return count;
	}

	/**
	 * @Description:
	 * 
	 * @param table_code
	 * 
	 * @return
	 */
	public static String queryTableStatusByCode(String table_code) {
		List<Record> list = Db.use(xx.DS_EOVA)
				.find("select status from bs_table_status where dr = 0 and  table_code = ? ", table_code);
		if (list.isEmpty()) {
			return null;
		}

		return list.get(0).getStr("status");

	}

	public static List<Record> queryTableStatus() {
		List<Record> list = Db.use(xx.DS_EOVA).find("select table_code , status from bs_table_status where dr = 0  ");
		return list;
	}

	/**
	 * @Description:
	 * 
	 * @param where
	 * 
	 * @return
	 */
	public static Map<String, String> queryTableStatus(List<String> where) {
		List<Record> list = Db.find("select * from bs_table_status where table_code ?", List2WhereIn(where));
		Map<String, String> tablestatus = new HashMap<>();
		for (Record r : list) {
			tablestatus.put(r.getStr("table_code"), r.getStr("status"));

		}
		return tablestatus;
	}

	
	private static String List2WhereIn(List<String> where) {
		if (where.isEmpty()) {
			return "<> 1";
		}
		String sql = "in ('";
		for (String s : where) {
			sql = sql + s + "',";
		}
		sql = xx.delEnd(sql.toString(), ",") + ")";
		return sql;
	}
}
