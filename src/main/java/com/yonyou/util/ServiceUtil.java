package com.yonyou.util;

import java.util.List;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 基础信息接口
 * @author yancy
 *
 */
public class ServiceUtil extends BaseModel<ServiceUtil> {
	
	private static final long serialVersionUID = 1064291771401662738L;

    public static final ServiceUtil dao = new ServiceUtil();

    
    
	/**
	 * 根据ID获取FTP信息
	 * @param ftpId
	 * @return
	 */
	public List<Record> getFtpById(String ftpId) {
		String sql = "select * from bs_ftp_registry where  id ='" + ftpId + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	/**
	 * 根据ID获取FTP信息
	 * @param ftpCode
	 * @return
	 */
	public List<Record> getFtpByCode(String ftpCode) {
		String sql = "select * from bs_ftp_registry where  ftp_code ='" + ftpCode + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	/**
	 * 根据ID获取工作目录信息
	 * @param workDirectoryId
	 * @return
	 */
	public List<Record> getWorkDirectoryById(String workDirectoryId) {
		String sql = "select * from bs_working_directory where  id ='" + workDirectoryId + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	
	/**
	 * 根据Code获取工作目录信息
	 * @param workDirectoryCode
	 * @return
	 */
	public List<Record> getWorkDirectoryByCode(String workDirectoryCode) {
		String sql = "select * from bs_working_directory where  working_code ='" + workDirectoryCode + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	/**
	 * 根据ID获取数据源注册信息
	 * @param dataSourceId
	 * @return
	 */
	public List<Record> getDatasourceById(String dataSourceId) {
		String sql = "select * from bs_datasource_registry where  id ='" + dataSourceId + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	
	/**
	 * 根据Code获取数据源注册信息
	 * @param dataSourceCode
	 * @return
	 */
	public List<Record> getDatasourceByCode(String dataSourceCode) {
		String sql = "select * from bs_datasource_registry where  ds_code ='" + dataSourceCode + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	/**
	 * 根据ID获取数据库字典信息
	 * @param dataDictionaryId
	 * @return
	 */
	public List<Record> getDataDictionaryById(String dataDictionaryId) {
		String sql = "select * from bs_data_dictionary where  id ='" + dataDictionaryId + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	
	/**
	 * 根据Code获取数据库字典信息
	 * @param dataDictionaryCode
	 * @return
	 */
	public List<Record> getDataDictionaryCode(String dataDictionaryCode) {
		String sql = "select * from bs_data_dictionary where  ds_code ='" + dataDictionaryCode + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	
	/**
	 * 根据ID获取系统注册信息
	 * @param systemRegistryId
	 * @return
	 */
	public List<Record> getSystemRegistryById(String systemRegistryId) {
		String sql = "select * from bs_system_registry where  id ='" + systemRegistryId + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	
	/**
	 * 根据Code获取系统注册信息
	 * @param systemRegistryCode
	 * @return
	 */
	public List<Record> getSystemRegistryCode(String systemRegistryCode) {
		String sql = "select * from bs_system_registry where  sy_regeist_code ='" + systemRegistryCode + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	/**
	 * 根据ID获取流程类型信息
	 * @param flowTypeId
	 * @return
	 */
	public List<Record> getFlowTypeById(String flowTypeId) {
		String sql = "select * from bs_flow_type where  id ='" + flowTypeId + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	
	/**
	 * 根据Code获取流程类型信息
	 * @param flowTypeCode
	 * @return
	 */
	public List<Record> gettFlowTypeCode(String flowTypeCode) {
		String sql = "select * from bs_flow_type where  flowtype_code ='" + flowTypeCode + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	/**
	 * 根据ID获取任务流信息
	 * @param flowTypeId
	 * @return
	 */
	public List<Record> getFlowFlagById(String flowTypeId) {
		String sql = "select * from bs_flow_flag where  id ='" + flowTypeId + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	
	/**
	 * 根据Code获取任务流信息
	 * @param flowTypeCode
	 * @return
	 */
	public List<Record> gettFlowFlagCode(String flowTypeCode) {
		String sql = "select * from bs_flow_flag where  flowtask_code ='" + flowTypeCode + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	/**
	 * 根据ID获取FTP流信息
	 * @param flowTypeCode
	 * @return
	 */
	public List<Record> getBsFtpFlow(String id) {
		String sql = "select * from bs_ftp_flow where  id ='" + id + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
	
	/**
	 * 根据ID获取描述信息
	 * @param flowTypeCode
	 * @return
	 */
	public List<Record> getBsDicts(String id) {
		String sql = "select * from dicts  where id ='" + id + "'";
		List<Record> list = Db.use(xx.DS_EOVA).find(sql);
		return list;
	}
	
}
