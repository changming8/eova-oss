/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.oss.model;

import java.util.List;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.eova.config.EovaConfig;
import com.eova.core.meta.ColumnMeta;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;
/**
 * 元数据子表
 * @author jaker
 * 
 */
public class MetadataDetail extends BaseModel<MetadataDetail> {

	private static final long serialVersionUID = 1064291771401662738L;

	public static final MetadataDetail dao = new MetadataDetail();
	
	
	//获取元数据子表引用关系列 
	public List<Record> findMetadataBodLinkyById(String id){
		return  Db.use(xx.DS_EOVA).find("select id as field_id ,field_code,field_name from bs_metadata_b where link_status = 1 and pid = ? ", id);
	}
	
	public MetadataDetail() {
		
	}
	public MetadataDetail(String objectCode, ColumnMeta col) {

		if (EovaConfig.isLowerCase) {
			col.name = col.name.toLowerCase();
		}
		//默认设置数据库字段值  列名称对应
		this.set("id", UUID.getUnqionPk());
		this.set("field_code", col.name);//列编码
		this.set("field_name", col.remarks);//列名称
		this.set("field_type", col.dataTypeName);//列数据类型
		this.set("field_length", col.dataSize);//列长度
		this.set("dr", 0);
		if(col.isNull) {
			this.set("null_flag", 0);//允许为空 0允许1 不能为空
		}else {
			this.set("null_flag", 1);
		}
		if(col.name.equals("id")) {
			this.set("key_flag", 1);//主键 1  非主键 0
		}else {
			this.set("key_flag", 0);
		}
		
		
		//this.set("sort", col.dataType);//排序
		this.set("summary", col.remarks);//描述
		this.set("def_value", col.defaultValue);//默认值
		//this.set("unique_constraint", col.dataType);//唯一索引
		
		// 默认值
		String defaulter = this.getStr("def_value");
		if (xx.isEmpty(defaulter)) {
			this.set("def_value", "");
		} else {
			// 清除Mysql函数,不能作为字符串长传入.如果缺省值应在DB中自动自动执行.
			if (defaulter.indexOf("(") != -1 && defaulter.indexOf(")") != -1) {
				this.set("def_value", "");
			}
		}
	}
}