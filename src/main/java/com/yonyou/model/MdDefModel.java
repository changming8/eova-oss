package com.yonyou.model;

import java.util.List;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class MdDefModel extends BaseModel<MdDefModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -813267423393265922L;

	public static MdDefModel dao = new MdDefModel();

	public List<Record> getDefInfoById(String id) {
//		List<MdDefModel>  list = MdDefModel.dao.queryByCache("select * from bs_md_def where dr = 0 and id = ?", id);
		List<Record> list = Db.use(xx.DS_EOVA).find("select * from bs_md_def where dr = 0 and id = ?", id);
		return list;
	}
}
