/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.oss.model;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.eova.config.EovaConfig;
import com.eova.core.meta.ColumnMeta;
import com.yonyou.util.UUID;
/**
 * 元数据
 * @author jaker
 *
 */
public class Metadata extends BaseModel<Metadata> {

	private static final long serialVersionUID = 1064291771401662738L;

	public static final Metadata dao = new Metadata();
	
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