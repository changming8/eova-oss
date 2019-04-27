package com.yonyou.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.aop.AopContext;
import com.eova.common.Easy;
import com.eova.common.base.BaseController;
import com.eova.common.render.XlsRender;
import com.eova.common.utils.xx;
import com.eova.common.utils.db.DsUtil;
import com.eova.common.utils.io.FileUtil;
import com.eova.config.EovaConfig;
import com.eova.core.menu.config.MenuConfig;
import com.eova.core.meta.ColumnMeta;
import com.eova.engine.EovaExp;
import com.eova.i18n.I18NBuilder;
import com.eova.model.EovaLog;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.template.single.SingleAtom;
import com.eova.template.single.SingleIntercept;
import com.eova.widget.WidgetManager;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.activerecord.tx.TxConfig;
import com.jfinal.upload.UploadFile;
import com.yonyou.model.Metadata;
import com.yonyou.model.MetadataDetail;
import com.yonyou.util.UUID;

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
	protected Metadata metadata = new Metadata();
	
	public void copy() throws Exception {

		Object j = keepPara("rows").getAttr("rows");
		JSONArray jsonlist = JSONArray.parseArray(j.toString());
		JSONObject json = (JSONObject) jsonlist.get(0);
		// 复制元数据
		// 先查询 先复制以时间戳为结尾复制到元数据主表中 子表直接复制
		//Metadata meta= new Metadata();
		List<Record> metadataList = metadata.findMetadataById(json.getString("id"));
		List<Record> metadataDetailList = metadata.findMetadataBodyById(json.getString("id"));
		String id = UUID.getUnqionPk();
		Record record = new Record();
		record = metadataList.get(0).remove("ID");
		record.set("ID", id);
		String table_code = record.get("table_code").toString();
		String code = record.get("code").toString();
		record.remove("table_code");
		long t = System.currentTimeMillis();
		String serialCode = table_code + "_" + t;
		String coden = code+"_"+t;
		record.set("table_code", serialCode);
		record.set("code", coden);
		record.set("create_status", 0);
		// 保存元数据主表
		metadata.save(record);
		// 复制子表字段信息
		for (int i = 0; i < metadataDetailList.size(); i++) {
			metadataDetailList.get(i).set("pid", id);
			metadataDetailList.get(i).set("ID", UUID.getUnqionPk());
		}
		//批量保存元数据子表字段信息
		metadata.batchSave(metadataDetailList,50);
		renderJson(Easy.sucess());
	}

	public void createTable() throws Exception {
		// 获取元数据table名称 和字段属性信息 拼接建表语句
		Object j = keepPara("rows").getAttr("rows");
		JSONArray jsonlist = JSONArray.parseArray(j.toString());
		JSONObject json = (JSONObject) jsonlist.get(0);
		//视图直接返回不能创表表信息
		if(json.getString("storage_type").equals("视图")) {
			renderJson(Easy.fail("视图无法建表"));
			return;
		}
		final Object[] objs = new Object[2];
		
		
		// 获取key获取数据库类型字段 从MYSQL_DATEBASE_TYPE获取
		List<Record> columnDetailList = metadata.findMetadataBodyById(json.getString("id"));
		StringBuilder tempColumnSql = new StringBuilder(" CREATE TABLE ");
		String tableName = json.getString("table_code");
		objs[0]=tableName;
		tempColumnSql.append(tableName + " ( ");
		for (int i = 0; i < columnDetailList.size(); i++) {
			String columnName = columnDetailList.get(i).get("field_code");
			String fieldType = columnDetailList.get(i).get("field_type");
			String length = columnDetailList.get(i).get("field_length");
			String fieldName = columnDetailList.get(i).get("field_name");//字段描述
			String def_value = columnDetailList.get(i).get("def_value");//默认值
			Boolean auto = columnDetailList.get(i).get("enable_auto");//列是否自增模式 
			boolean unique = false;//唯一约束
			if(null !=columnDetailList.get(i).get("unique_constrain")) {
				unique = columnDetailList.get(i).get("unique_constrain");
			}
			boolean keyFlag = false;
			if(null != columnDetailList.get(i).get("key_flag")) {
				keyFlag = columnDetailList.get(i).get("key_flag");
			}
			
			boolean nullFlag = false;
			if(null != columnDetailList.get(i).get("null_flag")) {
				nullFlag = columnDetailList.get(i).get("null_flag");// 空值标识——0：能为空；1：不能为空
			}
			
			tempColumnSql.append(columnName + " " + fieldType);
			if (null != length && !"".equals(length.trim())) {
				tempColumnSql.append(" (" + length + ")");
			}
			if (keyFlag) {
				tempColumnSql.append(" PRIMARY KEY NOT NULL ");
			}  else if (nullFlag) {
				tempColumnSql.append(" NOT NULL");
			}  
			if(null!= auto&&auto) {
				tempColumnSql.append(" AUTO_INCREMENT ");
			}
			if (unique) {
				tempColumnSql.append(" UNIQUE ");
			}  
			if(null != def_value&&!def_value.equals("")){
				tempColumnSql.append(" DEFAULT ").append("'").append(def_value).append("'");
			}  
			if(fieldName!= null&&!fieldName.equals("")) {
				tempColumnSql.append(" COMMENT '").append(fieldName).append("' ");
			}  
			if ("TIMESTAMP".equals(fieldType)) {
				tempColumnSql.append(" null");// mysql中TIMESTAMP类型默认为非空
			}
			if (i != columnDetailList.size() - 1) {
				tempColumnSql.append(" , ");
			}
		}
		tempColumnSql.append(" )");
		objs[1]=tempColumnSql.toString();
		// 建表动作
		// 存在的确认有没有数据,无数据才可以drop 重新执行建标 否则返回异常提示
		try {
			if (checkExit(tableName)) {
				// 存在的话 查询是否有数据
				//String countSql = "select count(*) as COUNT from " + tableName;
				List<Record> countList = metadata.findCountyByTableName(tableName);
				if (countList.size() > 0) {
					renderJson(Easy.fail("表已存在数据,无法重新创建"));
					return;
				} else {
					// 执行 drop  回滚测试
					boolean succeed = Db.tx(new IAtom() {
						@Override
						public boolean run() throws SQLException {
							metadata.dropTableByName(objs[0].toString());
							metadata.createTableBySql(objs[1].toString());
							return true;
						}
					});
					if(!succeed){
						renderJson(Easy.fail("创建失败,请检查字段是否含有数据库关键字!"));
						return;
					}
				}
			} else {
				metadata.createTableBySql(tempColumnSql.toString());
			}
			//更新建表状态
			metadata.updateCreateTableStatue(json.getString("id"));
			renderJson(Easy.sucess());
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
			renderJson(Easy.fail("保存失败, 请联系管理员"));
		}
	}

	public void saveBatch() throws Exception {
		// 获取到所有行修改休息 做批量修改和新增 如果主键非空修改, 其余新增操作
		Object j = keepPara("rows").getAttr("rows");
		String pid = keepPara("rows").getPara("pid");
		JSONArray jsonlist = JSONArray.parseArray(j.toString());
		List<Record> insertRecord = new ArrayList<Record>();
		List<Record> updateRecord = new ArrayList<Record>();
		
		//获取数据里字段大小写类型 转换字段类型大小写问题
		String  database_type= keepPara("rows").getPara("type");
		//转参数大小写
		for (int i = 0; i < jsonlist.size(); i++) {
			JSONObject obj = jsonlist.getJSONObject(i);
			if (obj.getBoolean("key_flag") != null && obj.getBoolean("key_flag")) {
				obj.put("key_flag", "1");
			} else {
				obj.put("key_flag", "0");
			}
			if (null!= obj.getBoolean("null_flag") && obj.getBoolean("null_flag")) {
				obj.put("null_flag", "1");
			} else {
				obj.put("null_flag", "0");
			}

			Record re = new Record();
			Map<String, Object> map = obj;
			re.setColumns(map);
			re.remove("pk_val");
			re.remove("link_column_val");
			re.remove("link_table_val");
			String field_type =re.getStr("field_type");
			if(database_type.equals("2")) {
				re.set("field_type", field_type.toLowerCase());
			}else if(database_type.equals("1")) {
				re.set("field_type", field_type.toUpperCase());
			}
			if (obj.getString("id") == null || obj.getString("id").equals("")) {
				// 新增
				re.set("id", UUID.getUnqionPk());
				re.set("pid", pid);
				insertRecord.add(re);
			} else {
				// 修改
				updateRecord.add(re);
			}
		}
		metadata.batchSave(insertRecord, 50);
		metadata.batchUpdate(updateRecord, 50);
		renderJson(Easy.sucess());
	}

	public void importXls() {
		String menuCode = this.getPara(0);
		setAttr("menuCode", menuCode);
		render("/eova/metadata/dialog/import.html");
	}
	//挑战页面
	public void demo() {
		String menuCode = this.getPara(0);
		setAttr("menuCode", menuCode);
		render("/eova/tabs2_demo.html");
	}
	//挑战页面
	public void table() {
		String menuCode = this.getPara(0);
		setAttr("menuCode", menuCode);
		render("/eova/table_1.html");
	}
	// 导入页面
	public void imports() {
		setAttr("dataSources", EovaConfig.getDataSources());
		render("/eova/metadata/import.html");
	}
	public void export() throws Exception {
		//导出
		String pid = getPara("pid");
		String menuCode = getPara("menuCode");

		MetaObject object = sm.meta.getMeta(menuCode);
		Menu menu = Menu.dao.findByCode(menuCode);

		//intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());

		// 构建查询
		List<Object> parmList = new ArrayList<Object>();
		String sql = WidgetManager.buildQuerySQL(ctrl, menu, object, null, parmList, true);
		sql= sql+"	where  pid="+pid;
		// 转换SQL参数
		Object[] paras = new Object[parmList.size()];
		parmList.toArray(paras);
		List<Record> data = Db.use(object.getDs()).find("select *" + sql, paras);
		
		// 查询后置任务
//		if (intercept != null) {
//			AopContext ac = new AopContext(ctrl, data);
//			ac.object = object;
//			intercept.queryAfter(ac);
//		}

		List<MetaField> fields = object.getFields();
		
		// 根据表达式将数据中的值翻译成汉字
		//WidgetManager.convertValueByExp(this, fields, data);

		Iterator<MetaField> it = fields.iterator();
		while (it.hasNext()) {
			MetaField f = it.next();
			if (!f.getBoolean("is_show")) {
				it.remove();
			}
		}

		render(new XlsRender(data, fields, object));
		
	}
	public void doImportXls() throws Exception {

		String menuCode = "bs_metadata_b";

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
		// object.set("code", "bs_metadata_b");
		// object.set("table_name", "bs_metadata_b");
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

	// 导入元数据
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void doImports() {

		String ds = getPara("ds");
		String type = getPara("type");
		String table = getPara("table");
		String name = getPara("name");
		String code = getPara("code");
		String pk = getPara("pk");

		if (xx.isOneEmpty(ds, type, table, name, code)) {
			renderJson(new Easy("参数都必须填写！"));
			return;
		}

		MetaObject o = MetaObject.dao.getByCode(code);
		if (o != null) {
			renderJson(new Easy(String.format("对象编码[%s]已经被其它对象使用了，请修改对象编码！", code)));
			return;
		}

		// 导入元数据
		String msg = importMeta(ds, type, table, name, code, pk);
		if (!xx.isEmpty(msg)) {
			renderJson(new Easy(msg));
			return;
		}

		renderJson(new Easy());
	}

	/**
	 * 导入元数据
	 *
	 * @param ds    数据源
	 * @param type  表还是视图
	 * @param table 表名
	 * @param name  对象名
	 * @param code  对象编码
	 * @param pk    主键名
	 */
	public String importMeta(String ds, String type, String table, String name, String code, String pk) {
		// table自动获取主键
		if (type.equalsIgnoreCase(DsUtil.TABLE)) {
			pk = DsUtil.getPkName(ds, table);
			if (xx.isEmpty(pk)) {
				return "表的主键不能为空，请为当前表设置主键！";
			}
		}
		// 导入元数据主表  保存table主信息 到主表 
		//importMetaObject(ds, type, table, name, code, pk);
		//check 元数据是否存在,存在更改编码_1
		List<Record> countList = metadata.findMetadataBodyByDataCode(code);
		if(countList.size()>0) {
			code = code+"_1";
		}
		Metadata metadata = new Metadata();
		String id = UUID.getUnqionPk();
		metadata.set("id", id);
		metadata.set("table_code", code);
		metadata.set("code", code);
		metadata.set("table_name", name);
		metadata.set("data_disname", name);
		metadata.set("data_resource", ds);
		metadata.set("dr", 0);
		//metadata.set("data_type", type);
		boolean bo = metadata.save();
		if(bo) {
			// 导入元字段到子表
			importMetaField(ds, table, code,id);
			
			
		}
		// 云端人工智能预处理元数据
		// EovaCloud.buildMeta(code);

		return null;
	}

	/**
	 * 导入元字段
	 *
	 * @param code  对象编码
	 * @param list  字段元数据
	 * @param ds    数据源
	 * @param table 表名
	 */
	private void importMetaField(String ds, String table, String code,String pid) {
		JSONArray list = DsUtil.getColumnInfoByConfigName(ds, table);

		for (int i = 0; i < list.size(); i++) {
			JSONObject o = list.getJSONObject(i);
			// 获取每个字段的属性 批量保存到业务数据表中
			ColumnMeta col = new ColumnMeta(ds, table, o);
			MetadataDetail metadataDetail = new MetadataDetail(code, col);
			metadataDetail.set("pid", pid);
			metadataDetail.save();
			//autoBindDict(table, code, o.getString("REMARKS"), mi.getEn());
		}
	}

	// 查找表结构表头
	public void find() {
		String ds = getPara(0);
		String type = getPara(1);
		// 根据表达式手工构建Eova_Object
		MetaObject eo = MetaObject.dao.getTemplate();
		eo.put("data_source", ds);
		// 第1列名
		eo.put("pk_name", "table_name");
		// 第2列名
		eo.put("cn", "table_name");

		// 根据表达式手工构建Eova_Item
		List<MetaField> eis = new ArrayList<MetaField>();
		eis.add(EovaExp.buildItem(1, "table_name", "编码", false));
		eis.add(EovaExp.buildItem(2, "table_name", "表名", true));

		setAttr("objectJson", JsonKit.toJson(eo));
		setAttr("fieldsJson", JsonKit.toJson(eis));
		setAttr("itemList", eis);
		setAttr("pk", "pk_name");

		setAttr("action", "/metadata/findJson/" + ds + '-' + type);
		setAttr("isPaging", false);

		render("/eova/widget/find/find.html");
	}

	// 查找表结构数据
	public void findJson() {

		// 获取数据库
		String ds = getPara(0);
		String type = getPara(1);

		// 用户过滤
		String schemaPattern = null;
		// Oracle需要根据用户名过滤表
		if (xx.isOracle()) {
			schemaPattern = DsUtil.getUserNameByConfigName(ds);
		}

		// 表名过滤
		String tableNamePattern = getPara("query_table_name");
		if (!xx.isEmpty(tableNamePattern)) {
			tableNamePattern = "%" + tableNamePattern + "%";
		}

		List<String> tables = DsUtil.getTableNamesByConfigName(ds, type, schemaPattern, tableNamePattern);
		JSONArray tableArray = new JSONArray();
		for (String tableName : tables) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("table_name", tableName);
			tableArray.add(jsonObject);
		}
		// 将分页数据转换成JSON
		String json = JsonKit.toJson(tableArray);
		json = "{\"total\":" + tableArray.size() + ",\"rows\":" + json + "}";
		renderJson(json);
	}
	
	// ajax 上传回调
	public void uploadCallback(boolean succeed, String msg) {
		renderHtml("<script>parent.callback(\"" + msg + "\", " + succeed + ");</script>");
	}

	public boolean checkExit(String tableName) {
		List<Record> columnDetailList = metadata.findTableByTableName(tableName);
		if (columnDetailList.size() > 0) {
			return true;
		}
		return false;
	}
}