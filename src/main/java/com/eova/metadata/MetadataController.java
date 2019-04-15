package com.eova.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.yonyou.util.UUID;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.aop.AopContext;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.eova.common.utils.io.FileUtil;
import com.eova.core.menu.config.MenuConfig;
import com.eova.i18n.I18NBuilder;
import com.eova.model.EovaLog;
import com.eova.model.Menu;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.template.single.SingleAtom;
import com.eova.template.single.SingleIntercept;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

/**
 * 自定义元数据按钮控制器
 * 
 * @author jaker
 * @date 2019-4-13
 */
public class MetadataController extends BaseController {
	
	final Controller ctrl = this;
	/** 自定义拦截器 **/
	protected SingleIntercept intercept = null;
	
	public void copy() throws Exception {

		Object j = keepPara("rows").getAttr("rows");
		JSONArray jsonlist = JSONArray.parseArray(j.toString());
		JSONObject json = (JSONObject) jsonlist.get(0);
		System.out.print(json.getString("id"));
		// 复制元数据
		// 先查询 先复制以时间戳为结尾复制到元数据主表中 子表直接复制
		String metadataSql = "select * from bs_metadata where dr=0 and  id ='" + json.getString("id")+"'";
		String metadatadetailSql = "select * from bs_metadata_detail where dr=0 and  metadata_id ='"+ json.getString("id")+"'";
		List<Record> metadataList = Db.use(xx.DS_EOVA).find(metadataSql);
		List<Record> metadataDetailList = Db.use(xx.DS_EOVA).find(metadatadetailSql);
		String id = UUID.getUnqionPk();
		Record record = new Record();
		record = metadataList.get(0).remove("ID");
		record.set("ID", id);
		String code = record.get("DATA_CODE").toString();
		record.remove("DATA_CODE");
		String serialCode = code + "_" + System.currentTimeMillis();
		record.set("DATA_CODE", serialCode);
		// 插入
		Db.use(xx.DS_EOVA).save("bs_metadata", record);
		// 复制子表字段信息

		for (int i = 0; i < metadataDetailList.size(); i++) {
			metadataDetailList.get(i).set("METADATA_ID", id);
			metadataDetailList.get(i).set("ID", UUID.getUnqionPk());
		}
		Db.use(xx.DS_EOVA).batchSave("bs_metadata_detail", metadataDetailList, 30);
		renderJson(Easy.sucess());
	}

	public void createTable() throws Exception {
		// 获取元数据table名称 和字段属性信息 拼接建表语句
		Object j = keepPara("rows").getAttr("rows");
		JSONArray jsonlist = JSONArray.parseArray(j.toString());
		JSONObject json = (JSONObject) jsonlist.get(0);
		System.out.print(json.getString("id"));

		// 获取key获取数据库类型字段 从MYSQL_DATEBASE_TYPE获取
		String columnSql = "select* from bs_metadata_detail where dr=0 and  metadata_id ='" + json.getString("id")+"'";
		List<Record> columnDetailList = Db.use(xx.DS_EOVA).find(columnSql);
		StringBuffer tempColumnSql = new StringBuffer(" CREATE TABLE ");
		String tableName = json.getString("data_code");
		tempColumnSql.append(tableName + " ( ");
		for (int i = 0; i < columnDetailList.size(); i++) {
			String columnName = columnDetailList.get(i).get("FIELD_CODE");
			String fieldType = columnDetailList.get(i).get("FIELD_TYPE");
			String length = columnDetailList.get(i).get("FIELD_LENGTH");
			boolean keyFlag = columnDetailList.get(i).get("KEY_FLAG");
			// 默认值
			boolean nullFlag = columnDetailList.get(i).get("NULL_FLAG");// 空值标识——0：能为空；1：不能为空
			tempColumnSql.append(columnName + " " + fieldType);
			if (null != length && !"".equals(length.trim())) {
				tempColumnSql.append(" (" + length + ")");
			}
			if (keyFlag) {
				tempColumnSql.append(" not null primary key");
			} else if (nullFlag) {
				tempColumnSql.append(" not null");
			} else if ("TIMESTAMP".equals(fieldType)) {
				tempColumnSql.append(" null");// mysql中TIMESTAMP类型默认为非空
			}
			if (i != columnDetailList.size() - 1) {
				tempColumnSql.append(" , ");
			}
		}
		tempColumnSql.append(" )");

		// 建标动作
		// 存在的确认有没有数据,无数据才可以drop 重新执行建标 否则返回异常提示
		try {
			if (checkExit(tableName)) {
				// 存在的话 查询是否有数据
				String countSql = "select count(*) as COUNT from " + tableName;
				List<Record> countList = Db.use(xx.DS_EOVA).find(countSql);
				if (Integer.valueOf(countList.get(0).get("count").toString()) > 0) {
					renderJson(Easy.fail("表已存在数据,无法重新创建"));
					return;
				} else {
					// 执行 drop
					Db.use(xx.DS_EOVA).update(" DROP TABLE " + tableName);
					Db.use(xx.DS_EOVA).update(tempColumnSql.toString());
					renderJson(Easy.sucess());
				}
			} else {
				Db.use(xx.DS_EOVA).update(tempColumnSql.toString());
				renderJson(Easy.sucess());
			}
		} catch (Exception e) {
			renderJson(Easy.fail("保存失败, 请联系管理员"));
		}
		//renderJson(Easy.sucess());
	}
	
	public void saveBatch() throws Exception {
		//获取到所有行修改休息 做批量修改和新增 如果主键非空修改, 其余新增操作
		Object j = keepPara("rows").getAttr("rows");
		String pid= keepPara("rows").getPara("pid");
		JSONArray jsonlist = JSONArray.parseArray(j.toString());
		List<Record> insertRecord = new ArrayList<Record>();
		List<Record> updateRecord = new ArrayList<Record>();
		for(int i=0;i<jsonlist.size();i++) {
			JSONObject obj =jsonlist.getJSONObject(i);
			if(obj.getBoolean("key_flag")!= null && obj.getBoolean("key_flag")) {
				obj.put("key_flag", "1");
			}else {
				obj.put("key_flag", "0");
			}
			if(obj.getBoolean("null_flag")) {
				obj.put("null_flag", "1");
			}else {
				obj.put("null_flag", "0");
			}
			
			Record re = new Record();
			Map<String, Object> map = obj;
			re.setColumns(map);
			re.remove("pk_val");
			if(obj.getString("id") ==null || obj.getString("id").equals("")) {
				//新增
				re.set("id", UUID.getUnqionPk());
				re.set("metadata_id", pid);
				insertRecord.add(re);
			}else {
				//修改
				updateRecord.add(re);
			}
		}
		Db.use(xx.DS_EOVA).batchSave("bs_metadata_detail", insertRecord, 50);
		Db.use(xx.DS_EOVA).batchUpdate("bs_metadata_detail", updateRecord, 50);
		renderJson(Easy.sucess());
	}
	
	public void importXls() {
		String menuCode = this.getPara(0);
		setAttr("menuCode", menuCode);
		render("/eova/metadata/dialog/import.html");
	}
	
	public void doImportXls() throws Exception {
		
		String menuCode = "bs_metadata_detail";
		
		// 获取元数据
		Menu menu = Menu.dao.findByCode(menuCode);
		MenuConfig config = menu.getConfig();
		String objectCode = config.getObjectCode();
		
		final MetaObject object = sm.meta.getMeta(objectCode);
		
		intercept = TemplateUtil.initIntercept(menu.getBizIntercept());

		// 默认上传到/temp 临时目录
		final UploadFile file = getFile("upfile", "/temp");
		if (file == null) {
			uploadCallback(false, I18NBuilder.get("上传失败，文件不存在"));
			return;
		}
		
		// 获取文件后缀
		String suffix = FileUtil.getFileType(file.getFileName());
		if (!suffix.equals(".xls")) {
			uploadCallback(false, I18NBuilder.get("请导入.xls格式的Excel文件"));
			return;
		}
		//object.set("code", "bs_metadata_detail");
		//object.set("table_name", "bs_metadata_detail");
		// 事务(默认为TRANSACTION_READ_COMMITTED)
		SingleAtom atom = new SingleAtom(file.getFile(), object, intercept, ctrl);
		
		boolean flag = Db.use(object.getDs()).tx(atom);

		if (!flag) {
			atom.getRunExp().printStackTrace();
			uploadCallback(false, atom.getRunExp().getMessage());
			return;
		}

		// 记录导入日志
		EovaLog.dao.info(this, EovaLog.IMPORT, object.getStr("code"));

		// 导入成功之后
		if (intercept != null) {
			try {
				AopContext ac = new AopContext(ctrl, atom.getRecords());
				intercept.importSucceed(ac);
			} catch (Exception e) {
				e.printStackTrace();
				uploadCallback(false, e.getMessage());
				return;
			}
		}

		uploadCallback(true, I18NBuilder.get("导入成功"));
	}
	// ajax 上传回调
	public void uploadCallback(boolean succeed, String msg) {
		renderHtml("<script>parent.callback(\"" + msg + "\", " + succeed + ");</script>");
	}
	public boolean checkExit(String tableName) {
		String tableSql = "select * from information_schema.tables where table_name='" + tableName + "'";
		List<Record> columnDetailList = Db.use(xx.DS_EOVA).find(tableSql);
		if (columnDetailList.size() > 0) {
			return true;
		}
		return false;
	}
}