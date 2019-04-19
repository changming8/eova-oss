package com.yonyou.intercept;

import java.util.List;

import com.eova.aop.AopContext;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 主数据定义拦截器
 * 
 * @author changjr
 *
 */
public class MdDefIntercept extends BaseMetaIntercept {
	@Override
	public String addBefore(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		String table = ac.record.getStr("mid_table");
		try {
			Db.find("SELECT * from "+table);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return super.addBefore(ac);
		}

		return table + "已存在！";
	}

	@Override
	public String addAfter(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		String table = ac.record.get("mid_table");
		String sql = "create table " + table
				+ "(id VARCHAR(20) primary key,md_column VARCHAR(30),mdid VARCHAR(200),dest_column VARCHAR(30),destid VARCHAR(200),dest_table  VARCHAR(30))";
		Db.update(sql);
		return super.addAfter(ac);
	}

	@Override
	public String deleteAfter(AopContext ac) throws Exception {
		// TODO Auto-generated method stub

		String table = ac.record.getStr("mid_table");
		String sql = "drop table " + table + "";
		Db.update(sql);

		return super.deleteAfter(ac);
	}

	@Override
	public String deleteBefore(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		String table = ac.record.get("mid_table");
		List<Record> record = Db.find("select * from " + table);
		if (!record.isEmpty()) {
			return "存储关系表数据非空,不允许删除主数据定义！";
		}
		return super.deleteBefore(ac);
	}
}
