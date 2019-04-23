package com.yonyou.model;

import java.util.List;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 数据清洗子表保存
 * @author jaker
 *
 */
public class CleanBody extends BaseModel<CleanBody> {

	private static final long serialVersionUID = 1064291771401662738L;

	public static final CleanBody dao = new CleanBody();

	
	//批量保存
	public int[] batchSave(List<Record> list,int count) {
		return Db.use(xx.DS_EOVA).batchSave("bs_clean_flow_b", list, count);
	}
}