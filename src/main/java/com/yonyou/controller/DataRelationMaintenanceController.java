package com.yonyou.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.base.BaseController;
import com.eova.common.utils.xx;
import com.eova.config.PageConst;
import com.eova.model.Menu;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.eova.widget.WidgetUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;
import com.jfinal.upload.UploadFile;
import com.yonyou.util.UUID;

import jxl.Cell;
import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Alignment;
import jxl.write.Label;
import jxl.write.VerticalAlignment;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * 
 * @author tanglibing
 *	数据关系定义
 */
public class DataRelationMaintenanceController extends BaseController {

	final Controller ctrl = this;

	/** 元对象业务拦截器 **/
	protected MetaObjectIntercept intercept = null;

	/*
	 * 页面跳转
	 */
	public void dataRelationMaintenance() {
		render("/eova/dataRelationMaintenance/dataRelationMaintenance.html");
	}

	public void dataFlow() {
		render("/eova/dataflow/dataFlow.html");
	}
	
	/*
	 * 数据流程queryById
	 */
	public void queryDataFlowById() {
		String flow_id=getPara(0);
		List<Record> record=Db.use(xx.DS_EOVA).find("select * from v_bs_flow where id='"+flow_id+"'");
		renderJson(record);
	}
	/*
	 * dataRelationMaintenance大部分查询
	 * 
	 */
	public void queryBsStyle() throws Exception {
		String objectCode = getPara(0);
		String menuCode = getPara(1);
		String where = getPara(2);
		String para1 = getPara(3);
		String para2 = getPara(4);
		int pageNumber = getParaToInt(PageConst.PAGENUM, 1);
		int pageSize = getParaToInt(PageConst.PAGESIZE, 100000);

		MetaObject object = sm.meta.getMeta(objectCode);
		Menu menu = Menu.dao.findByCode(menuCode);

		intercept = TemplateUtil.initMetaObjectIntercept(object.getBizIntercept());

		// 构建查询
		List<Object> paras = new ArrayList<Object>();
		String select = "select " + WidgetManager.buildSelect(object, RID());
		String sql = "";
		if ("table_master_col".equals(where)) {
			sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true)
					+ " where type='template' and sid='" + para1 + "' and md_table='" + para2 + "'";
		} else if ("bs_style_b".equals(where)) {
			sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true) + " where pid='" + para1
					+ "' and isshow=0";
		} else if ("select".equals(where)) {
			sql = WidgetManager.buildQuerySQL(ctrl, menu, object, intercept, paras, true)
					+ " where type='template' and sid='" + para1 + "' and md_table!='" + para2 + "'";
		}
		Page<Record> page = Db.use(object.getDs()).paginate(pageNumber, pageSize, select, sql, xx.toArray(paras));

		// 查询后置任务
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, page.getList());
			ac.object = object;
			intercept.queryAfter(ac);
		}

		// 备份Value列，然后将值列转换成Key列
		WidgetUtil.copyValueColumn(page.getList(), object.getPk(), object.getFields());
		// 根据表达式将数据中的值翻译成汉字
		WidgetManager.convertValueByExp(this, object.getFields(), page.getList());

		// 构建JSON数据
		StringBuilder sb = new StringBuilder(
				String.format("{\"total\":%s,\"rows\": %s}", page.getTotalRow(), JsonKit.toJson(page.getList())));

		// Footer
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, page.getList());
			ac.object = object;
			Kv footer = intercept.queryFooter(ac);
			if (footer != null) {
				sb.insert(sb.length() - 1, String.format(",\"footer\":[%s]", footer.toJson()));
			}
		}

		renderJson(sb.toString());
	}

	/*
	 * 新增数据到关系存储表
	 */
	public void insertMasterSlaveContrast() {
		String objectCode = getPara(0);
		String masterId = getPara(1);
		String slaveId = getPara(2);
		String masterCol = getPara(3);
		String slaveCol = getPara(4);
		String slaveTable = getPara(5);
		if ("".equals(objectCode)) {
			renderJson("{\"message\":\"对照数据异常\"}");
			return;
		}
		Record record = new Record();
		record.set("mdid", masterId);
		record.set("md_column", masterCol);
		record.set("dest_table", slaveTable);
		record.set("dest_column", slaveCol);
		String[] slaveIds = slaveId.split(",");
		for (int i = 0; i < slaveIds.length; i++) {
			record.set("id", UUID.getUnqionPk());
			record.set("destid", slaveIds[i]);
			Db.use(xx.DS_MAIN).save(objectCode, record);
		}
		renderJson("{\"message\":\"保存成功\"}");
	}

	/*
	 * 对照查询
	 */
	public void queryMaster() {
		String objectCode = getPara(0);
		List<String> record = Db.use(xx.DS_MAIN)
				.query("SELECT column_name FROM information_schema.COLUMNS WHERE table_name='" + objectCode + "'");

		StringBuffer sb = new StringBuffer();
		for (String re : record) {
			sb.append(re + ",");
		}
		String tempCol = sb.substring(0, sb.length() - 1);
		String sql = "select " + tempCol + " from " + objectCode;
		List<Record> recordData = Db.use(xx.DS_MAIN).find(sql);
		renderJson(recordData);
	}

	/*
	 * 对照条件查询
	 */
	public void queryWhere() {
		String objectCode = getPara(0);
		String para1 = getPara(1);
		String para2 = getPara(2);

		String sql = "select * from " + objectCode + " where " + para2 + " like '%" + para1 + "%'";
		List<Record> record = Db.use(xx.DS_MAIN).find(sql);
		renderJson(record);
	}

	/*
	 * 启用对照
	 */
	public void startUpContrast() {
		String mid_table = getPara(0);
		String masterId = getPara(1);
		String dest_table = getPara(2);

		String sql_query_ids = "select destid from " + mid_table + " where mdid='" + masterId + "' and dest_table='"
				+ dest_table + "'";
		List<String> slaveIds = Db.use(xx.DS_MAIN).query(sql_query_ids);
		if (slaveIds.size() < 1) {
			renderJson("");
			return;
		}
		StringBuffer sql_query = new StringBuffer();
		sql_query.append("select * from " + dest_table + " where id in(");
		for (String slave : slaveIds) {
			sql_query.append("'" + slave + "',");
		}
		String sql = sql_query.substring(0, sql_query.lastIndexOf(",")).toString() + ")";
		List<Record> queryData = Db.use(xx.DS_MAIN).find(sql);
		renderJson(queryData);
	}

	/*
	 * 过滤对照
	 */
	public void queryFilterContrast() {
		String slaveObject = getPara(0);
		String increment = getPara(1);
		String queryValue = getPara(2);
		String queryColumn = getPara(3);

		String tempTable = "select destid from " + increment + " where dest_table='" + slaveObject + "'";
		List<String> filterId = Db.use(xx.DS_MAIN).query(tempTable);
		StringBuffer sb = new StringBuffer();
		for (String fi : filterId) {
			sb.append("'" + fi + "',");
		}
		String filterIds = null;
		if (sb.length() > 0) {
			filterIds = sb.substring(0, sb.length() - 1);
			sb.delete(0, sb.length());
		}
		List<String> record = Db.use(xx.DS_MAIN)
				.query("SELECT column_name FROM information_schema.COLUMNS WHERE table_name='" + slaveObject + "'");

		for (String re : record) {
			sb.append(re + ",");
		}
		String tempCol = sb.substring(0, sb.length() - 1);
		String sql;
		if (!(queryColumn == null))
			sql = "select " + tempCol + " from " + slaveObject + " where id not in(" + filterIds + ")" + " and "
					+ queryColumn + "='" + queryValue + "'";
		else if (filterIds != null)
			sql = "select " + tempCol + " from " + slaveObject + " where id not in(" + filterIds + ")";
		else
			sql = "select " + tempCol + " from " + slaveObject + "";
		List<Record> recordData = Db.use(xx.DS_MAIN).find(sql);
		renderJson(recordData);
	}

	/*
	 * 删除映射关系
	 */
	public void deleteModal() {
		String increment = getPara(0);
		String masterId = getPara(1);
		String slaveIds = getPara(2);
		String slaveTable = getPara(3);

		String[] slaveId = slaveIds.split(",");
		StringBuffer sb = new StringBuffer();
		for (String s : slaveId) {
			sb.append("'" + s + "',");
		}
		String sql = "delete from " + increment + " where mdid='" + masterId + "' and dest_table='" + slaveTable + "'";
		String slave_id = sb.substring(0, sb.lastIndexOf(","));
		int isTrue = Db.delete(sql + " and destid in(" + slave_id + ")");
		if (isTrue > 0) {
			renderJson("{\"message\":\"删除成功\"}");
			return;
		}
		renderJson("{\"message\":\"删除失败\"}");
	}

	/*
	 * 导出映射
	 */
	@SuppressWarnings("deprecation")
	public void exportExcel(OutputStream os, String increment, String exportMaster, String exportSlave)
			throws IOException, WriteException {
		// 创建工作簿
		WritableWorkbook wb = null;
		try {
			wb = Workbook.createWorkbook(os);
			WritableFont codeFont = new WritableFont(WritableFont.createFont("宋体"), 12);// 设置字体、格式
			WritableCellFormat codeCF = new WritableCellFormat(codeFont);
			codeCF.setVerticalAlignment(VerticalAlignment.CENTRE);// 上下居中
			codeCF.setAlignment(Alignment.CENTRE);// 居中
			// 设置列宽
			CellView cellView = new CellView();
			cellView.setSize(10 * 550);
			// 添加工作表并设置Sheet的名字
			WritableSheet sheet = wb.createSheet(exportSlave, 0);
			// 记录当前索引
			int row = 0;
			// 主映射表头
			String masterTableId = Db.use(xx.DS_EOVA)
					.queryStr("select id from bs_metadata where data_code='" + exportMaster + "'");
			List<String> masterColCNName = Db.use(xx.DS_EOVA)
					.query("select field_name from bs_metadata_b where pid='" + masterTableId + "'");
			for (int i = 0; i < masterColCNName.size(); i++) {
				Label sortLabel = new Label(i, 0, masterColCNName.get(i));
				sortLabel.setCellFormat(codeCF);
				sheet.addCell(sortLabel);
				row++;
			}
			// master
			StringBuffer sb = new StringBuffer();
			List<String> masterColENName = Db.use(xx.DS_EOVA)
					.query("select field_code from bs_metadata_b where pid='" + masterTableId + "'");
			for (int i = 0; i < masterColENName.size(); i++) {
				sb.append(masterColENName.get(i) + ",");
			}
			String masterQueryCol = sb.substring(0, sb.lastIndexOf(","));
			List<Record> masterData = Db.use(xx.DS_MAIN).find("select " + masterQueryCol + " from " + exportMaster);
			for (int i = 0; i < masterData.size(); i++) {
				Record record = masterData.get(i);
				for (int j = 0; j < masterColENName.size(); j++) {
					Label sortLabel6 = new Label(j, (i + 1), record.getStr(masterColENName.get(j)));
					sortLabel6.setCellFormat(codeCF);
					sheet.addCell(sortLabel6);
				}
			}

			// 映射表id信息
			Label mdid = new Label((row + 1), 0, "mdid");
			mdid.setCellFormat(codeCF);
			sheet.addCell(mdid);
			Label destid = new Label((row + 2), 0, "destid");
			destid.setCellFormat(codeCF);
			sheet.addCell(destid);

			final String[] tempCol = { "mdid", "destid" };
			// 查询当前映射
			List<Record> queryMapping = Db.use(xx.DS_MAIN)
					.find("select mdid,destid from " + increment + " where dest_table='" + exportSlave + "'");
			for (int i = 0; i < queryMapping.size(); i++) {
				Record record = queryMapping.get(i);
				for (int j = 0; j < 2; j++) {
					Label sortLabel6 = new Label((row + 1) + j, (i + 1), record.getStr(tempCol[j]));
					sortLabel6.setCellFormat(codeCF);
					sheet.addCell(sortLabel6);
				}
			}
			row = row + 3;
			// slave
			sb.delete(0, sb.length());
			String slaveTableId = Db.use(xx.DS_EOVA)
					.queryStr("select id from bs_metadata where data_code='" + exportSlave + "'");
			List<String> slaveColCNName = Db.use(xx.DS_EOVA)
					.query("select field_name from bs_metadata_b where pid='" + slaveTableId + "'");

			for (int i = 0; i < slaveColCNName.size(); i++) {
				Label sortLabel5 = new Label((row + 1) + i, 0, slaveColCNName.get(i));
				sortLabel5.setCellFormat(codeCF);
				sheet.addCell(sortLabel5);
			}
			List<String> slaveColENName = Db.use(xx.DS_EOVA)
					.query("select field_code from bs_metadata_b where pid='" + slaveTableId + "'");
			for (int i = 0; i < slaveColENName.size(); i++) {
				sb.append(slaveColENName.get(i) + ",");
			}
			String slaveQueryCol = sb.substring(0, sb.lastIndexOf(","));
			List<Record> slaveData = Db.use(xx.DS_MAIN).find("select " + slaveQueryCol + " from " + exportSlave);
			for (int i = 0; i < slaveData.size(); i++) {
				Record record = slaveData.get(i);
				for (int j = 0; j < slaveColENName.size(); j++) {
					Label sortLabel6 = new Label((row + 1) + j, (i + 1), record.getStr(slaveColENName.get(j)));
					sortLabel6.setCellFormat(codeCF);
					sheet.addCell(sortLabel6);
				}
			}
			wb.write();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 关闭文件
			if (wb != null)
				wb.close();
		}
	}

	/*
	 * 导出执行前
	 */
	public void excelBefore() {
		String increment = getPara(0);
		String exportSlave = getPara(1);
		String exportMaster = getPara(2);
		render(new excelRender(increment, exportSlave, exportMaster));
	}

	/*
	 * 导入excel数据
	 */
	public void excelImport() throws BiffException, IOException {
		UploadFile fi = getFile();
		File file = new File(fi.getUploadPath());
		FileInputStream fio = null;
		try {
			fio = new FileInputStream(file.getPath() + "\\" + fi.getOriginalFileName());
			Workbook wk = Workbook.getWorkbook(fio);
			Sheet[] sheet = wk.getSheets();

			String destTableName = sheet[0].getName();
			StringBuffer sb = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();

			Cell c = sheet[0].findLabelCell("mdid");
			Cell c2 = sheet[0].findLabelCell("destid");
			int rows = sheet[0].getRows();
			for (int i = 0; i < rows; i++) {
				if ((i + 1) >= rows) {
					break;
				}
				Cell cell1 = sheet[0].getCell(c.getColumn(), (i + 1));
				Cell cell2 = sheet[0].getCell(c2.getColumn(), (i + 1));
				if (cell1 == null || "".equals(cell1) || cell2 == null || "".equals(cell2)) {
					continue;
				}
				sb.append(cell1.getContents() + ",");
				sb2.append(cell2.getContents() + ",");
			}
			String[] mdid = sb.substring(0, sb.lastIndexOf(",")).split(",");
			String[] destid = sb2.substring(0, sb2.lastIndexOf(",")).split(",");
			String tableName = fi.getOriginalFileName().substring(0, fi.getOriginalFileName().lastIndexOf("."));

			Record record = new Record();

			record.set("md_column", "id");
			record.set("dest_table", destTableName);
			record.set("dest_column", "id");
			for (int i = 0; i < mdid.length; i++) {
				int count = Db.use(xx.DS_MAIN).queryInt("select count(1) from " + tableName + " where dest_table='"
						+ destTableName + "' and " + "mdid='" + mdid[i] + "' and destid='" + destid[i] + "'");
				if (count > 0) {
					continue;
				}
				record.set("id", UUID.getUnqionPk());
				record.set("mdid", mdid[i]);
				record.set("destid", destid[i]);
				Db.use(xx.DS_MAIN).save(tableName, record);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fio != null) {
				fio.close();
			}
		}
		renderJson("{\"message\":\"导入成功\"}");
	}

	class excelRender extends Render {
		private final String CONTENT_TYPE = "application/msexcel;charset=" + getEncoding();

		private final String increment;
		private final String exportSlave;
		private final String exportMaster;

		private final String fileName;

		public excelRender(String increment, String exportSlave, String exportMaster) {
			this.increment = increment;
			this.exportSlave = exportSlave;
			this.exportMaster = exportMaster;

			fileName = increment + ".xls";
		}

		@Override
		public void render() {
			response.reset();
			try {
				response.setHeader("Content-disposition",
						"attachment; filename=" + URLEncoder.encode(fileName, getEncoding()));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			response.setContentType(CONTENT_TYPE);
			OutputStream os = null;
			try {
				os = response.getOutputStream();
				exportExcel(os, increment, exportMaster, exportSlave);
				// ExcelUtil.createExcel(os, data, items, object);
			} catch (Exception e) {
				throw new RenderException(e);
			} finally {
				try {
					if (os != null) {
						os.flush();
						os.close();
					}
				} catch (IOException e) {
					LogKit.error(e.getMessage(), e);
				}

			}
		}
	}
}
