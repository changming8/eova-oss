/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.metadata;



import com.eova.aop.AopContext;
import com.eova.common.utils.xx;
import com.eova.template.single.SingleIntercept;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;

public class MetadataIntercept extends SingleIntercept {

	@Override
	public void importBefore(AopContext ac) throws Exception {
		//新增默认主表描述 返回id到子表 
		String mid = insertMetadata();
		for (Record record : ac.records) {
			//处理主键 更换主键
			record.set("id", UUID.getUnqionPk());
			record.set("metadata_id", mid);
		}
	}

	public String insertMetadata() {
		StringBuilder sb = new StringBuilder();
		sb.append("system_").append(System.currentTimeMillis());
		String id = UUID.getUnqionPk();
		String sql = " INSERT INTO  eova.cd_metadata set  id= '"+id+"' ,data_code='"+sb.toString()+"', data_name='"+sb.toString()+"' ;";
		Db.use(xx.DS_EOVA).update(sql);
		return id;
	}
}