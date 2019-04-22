package com.eova.metadata;

import java.util.List;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;

public class MetadataBodyIntercept extends MetaObjectIntercept {

	@Override
	public String updateBefore(AopContext ac) throws Exception {
		
		//获取主表主键 查询字段类型是大小写 返回
		String id = ac.record.getStr("pid");
		String metadataSql = "select * from bs_metadata where id ='" + id + "'";
		List<Record> metadataList = Db.use(xx.DS_EOVA).find(metadataSql);
		String database_type = metadataList.get(0).get("database_type");
		String field_type =ac.record.getStr("field_type");
		if(database_type.equals("2")) {
			//转小写
			ac.record.set("field_type", field_type.toLowerCase());
		}else if(database_type.equals("1")) {
			//转大写
			ac.record.set("field_type", field_type.toUpperCase());
		}
		return super.updateBefore(ac);
	}
	
	@Override
	public String addBefore(AopContext ac) throws Exception {
		
		//获取主表主键 查询字段类型是大小写 返回
		String id = ac.record.getStr("pid");
		String metadataSql = "select * from bs_metadata where id ='" + id + "'";
		List<Record> metadataList = Db.use(xx.DS_EOVA).find(metadataSql);
		String database_type = metadataList.get(0).get("database_type");
		String field_type =ac.record.getStr("field_type");
		if(database_type.equals("2")) {
			//转小写
			ac.record.set("field_type", field_type.toLowerCase());
		}else if(database_type.equals("1")) {
			//转大写
			ac.record.set("field_type", field_type.toUpperCase());
		}
		ac.record.set("id", UUID.getUnqionPk());
		return super.addBefore(ac);
	}
}
