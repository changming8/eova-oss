package com.yonyou.controller;

import java.sql.SQLException;
import java.util.List;

import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;

/**
 * 
 * @author tanglibing
 *	接口数据浏览
 */
public class InterfaceDataBrowsingController extends BaseController{
	
	String[] smetas;//=new String[] {"query","input","output","mobile"};
	Record re;
	String metadata_id;
	
	public InterfaceDataBrowsingController() {}
	
	public InterfaceDataBrowsingController(String metadata_id,String[] smetas) {
		this.metadata_id=metadata_id;
		this.smetas=smetas;
	}
	
	/*
	 * 页面跳转
	 */
	public void forwordHtml() {
		render("/eova/interfaceDataBrowsing/interfaceDataBrowsing.html");
	}
	
	/*
	 * 查询接口数据类别
	 */
	public void queryClass() {
		List<Record> record=Db.use(xx.DS_EOVA).find("select id,sy_regeist_name from bs_system_registry");
		renderJson(record);
	}
	/*
	 * 接口数据表
	 */
	public void queryClassTable() {
		String classId=getPara(0);
		List<Record> list=Db.use(xx.DS_EOVA).find("select id,data_code,data_name from bs_metadata where syregeist_id='"+classId+"'");
		renderJson(list);
	}
	/*
	 * 初始化模板
	 */
	public void initTemplate() {
		String metadata_id=getPara(0);
		String[] type=new String[] {"query","input","output","mobile"};
		renderJson(new InterfaceDataBrowsingController(metadata_id,type).queryTemplateIsInit("数据浏览"));
	}
	/*
	 * 查询模板是否已被初始化
	 */
	public String queryTemplateIsInit(String smeta) {
		
		String bs_metadata_is="select id,data_code,data_name from bs_metadata where id='"+metadata_id+"'";
		Record queryData=Db.use(xx.DS_EOVA).findFirst(bs_metadata_is);
		
		if(queryData==null) 
		return "{\"message\":\"接口表结构可能已被删除\"}";
		
		String bs_style_is="select count(1) from bs_style"
				+ " where sid='"+queryData.getStr("id")+"' and "
				+ " md_table='"+queryData.getStr("data_code")+"' and "
				+ " smeta='"+smeta+"'";
		
		int isTrue=Db.use(xx.DS_EOVA).queryInt(bs_style_is);
		if(isTrue>0)
		return "{\"message\":\"接口模板已被初始化\"}";

		re=new Record();
		re.set("dr", 0);
		re.set("sid", queryData.getStr("id"));
		re.set("md_table", queryData.getStr("data_code"));
		re.set("smeta", smeta);
		
		boolean temp=false;
		temp=Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				// TODO Auto-generated method stub
				boolean isStyle=false;
				for(String sm:smetas) {
					re.set("id", UUID.getUnqionPk());
					re.set("type", sm);
					isStyle= Db.use(xx.DS_EOVA).save("bs_style", re);
				}
				if(!isStyle) {
					return false;
				}
				
				return true;
			}
		});
		
		if(!temp)
		return "{\"message\":\"接口模板数据校验失败\"}";
		
		return "{\"message\":\"接口模板初始化成功\"}";
	}

	
	
}
