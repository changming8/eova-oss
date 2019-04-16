package com.yonyou.intercept;

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
	public String addAfter(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		String table = ac.record.get("mid_table");
		String sql = "create table " + table + "(id VARCHAR(20) primary key,mdid VARCHAR(20),destid VARCHAR(20))";
		Db.update(sql);
		return super.addAfter(ac);
	}

	@Override
	public String deleteSucceed(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		for (Record r : ac.records) {
			String table = ac.record.get("mid_table");
			String sql = "drop table " + table + "";
			Db.update(sql, table);
		}

		return super.deleteSucceed(ac);
	}
}
