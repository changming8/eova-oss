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
 * 元数据
 * @author jaker
 *
 */
public class Metadata extends BaseModel<Metadata> {

	private static final long serialVersionUID = 1064291771401662738L;

	public static final Metadata dao = new Metadata();
	
	//查询元数据主表根据主键
	public List<Record> findMetadataById(String id){
		return  Db.use(xx.DS_EOVA).find("select * from bs_metadata where dr=0 and  id = ?", id);
	}
	
	//根据pid 查询子表数据
	public List<Record> findMetadataBodyById(String pid){
		return  Db.use(xx.DS_EOVA).find("select * from bs_metadata_b where dr=0 and  pid = ?", pid);
	}
	
	//根据code 获取元数据 
	public List<Record> findMetadataBodyByDataCode(String code){
		return  Db.use(xx.DS_EOVA).find("select * from bs_metadata where dr = 0 and data_code = ?", code);
	}
	
	//根据表是否存在某个schema
	public List<Record> findTableByTableName(String tableName){
		return  Db.use(xx.DS_EOVA).find("select * from information_schema.tables where table_schema = 'fidata'  and  table_name = ?", tableName);
	}
	
	//批量保存子表
	public int[] batchSave(List<Record> list,int count) {
		return Db.use(xx.DS_EOVA).batchSave("bs_metadata_b", list, count);
	}
	
	//批量更新
	public int[] batchUpdate(List<Record> list,int count) {
		return Db.use(xx.DS_EOVA).batchUpdate("bs_metadata_b", list, count);
	}
	//查询元数据是否存数据行
	public List<Record> findCountyByTableName(String tableName){
		return  Db.use(xx.DS_EOVA).find("select count(*) as COUNT from ?", tableName);
	}
	//drop table   删除表
	public int dropTableByName(String tableName) {
		return Db.update("DROP TABLE ?", tableName);
	}
	
	//create table  创建表
	public int createTableBySql(String sql) {
		return Db.update(sql);
	}
	
	//更新建表状态 
	public int updateCreateTableStatue(String id) {
		return Db.update("update bs_metadata set create_status = 1 where id = ?",id);
	}
	
	
	
	public boolean save (Record record) {
		return Db.use(xx.DS_EOVA).save("bs_metadata", record);
	}
	public Metadata() {
		
	}
	public Metadata(String objectCode, ColumnMeta col) {

		if (EovaConfig.isLowerCase) {
			col.name = col.name.toLowerCase();
		}
		//默认设置数据库字段值  列名称对应
		this.set("id", UUID.getUnqionPk());
		this.set("data_code", objectCode);
		this.set("data_name", col.name);
		this.set("data_disname", col.name);
		this.set("data_resource", col.ds);
		this.set("data_type", col.dataType);
		this.set("code", objectCode);
		//this.set("is_required", !col.isNull);
		//this.set("data_type_name", col.dataTypeName);
		//this.set("data_size", col.dataSize);
		//this.set("data_decimal", col.dataDecimal);
		//this.set("defaulter", col.defaultValue);
		//this.set("is_auto", col.isAuto);

		// 默认值
		String defaulter = this.getStr("defaulter");
		if (xx.isEmpty(defaulter)) {
			this.set("defaulter", "");
		} else {
			// 清除Mysql函数,不能作为字符串长传入.如果缺省值应在DB中自动自动执行.
			if (defaulter.indexOf("(") != -1 && defaulter.indexOf(")") != -1) {
				this.set("defaulter", "");
			}
		}
	}
}