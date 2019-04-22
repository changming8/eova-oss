package com.yonyou.intercept;

import java.util.List;

import com.eova.aop.AopContext;
import com.eova.common.utils.xx;
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
			Db.use(xx.DS_MAIN).find("SELECT * from " + table);
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
		String sql = "create table " + "mid_" + table
				+ "(id VARCHAR(20) primary key,md_column VARCHAR(30),mdid VARCHAR(200),dest_column VARCHAR(30),destid VARCHAR(200),dest_table  VARCHAR(30))";

		try {
			Db.use(xx.DS_MAIN).update(sql);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "创建中间表出错" + "mid_" + table + "请检查！";
		}
		return super.addAfter(ac);
	}

	@Override
	public String deleteAfter(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		String table = ac.record.getStr("mid_table");
		try {
			String sql = "drop table " + "mid_"+table + "";
			Db.use(xx.DS_MAIN).update(sql);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "删除中间表出错"+"mid_"+table +"请检查";
		}

		return super.deleteAfter(ac);
	}

	@Override
	public String deleteBefore(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		try {
			String table = ac.record.get("mid_table");
			List<Record> record = Db.use(xx.DS_MAIN).find("select * from " + "mid_" + table);
			if (!record.isEmpty()) {
				return "存储关系表数据非空,不允许删除主数据定义！";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "中间表异常请检查！";
		}
		return super.deleteBefore(ac);
	}
}
