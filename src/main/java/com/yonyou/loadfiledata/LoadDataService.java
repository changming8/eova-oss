package com.yonyou.loadfiledata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.base.ResponseBody;
import com.yonyou.model.FileManagerModel;
import com.yonyou.util.ServiceUtil;
import com.yonyou.util.FileCharsetDetector;
import com.yonyou.util.FileUtil;

/**
 * 数据文件load到数据库指定表环节
 * @author changming
 */
public class LoadDataService {

	/**
	 * 数据库表load数据文件
	 * @param id
	 * @return
	 */
	public ResponseBody loadData(String id) {
		
		List<Record> listProduct = Db.use(xx.DS_MAIN).find("select * from bs_loaddata_flow where id ='" + id + "'");//拼接DESC文件路径和文件名数据
		ResponseBody responseBody = new ResponseBody();
		
		//拼接DESC文件名
		String ftp_id = listProduct.get(0).get("ftp_id");// ftpID
		String work_directory_id = listProduct.get(0).get("workdirectory_id");// 工作目录ID
		String distinguish = listProduct.get(0).get("distinguish");// 大小写
		String standard_type = listProduct.get(0).get("standard_type");// 规范类型
		int analytical_rule = listProduct.get(0).get("analytical_rule");// 解析规则
		String file_name = listProduct.get(0).get("file_name");
		String search_path1 = listProduct.get(0).get("search_path1");// 搜索路径1
		String search_path2 = listProduct.get(0).get("search_path2");// 搜索路径2
		String file_name1 = listProduct.get(0).get("file_name1");
		String file_name2 = listProduct.get(0).get("file_name2");
		String file_name3 = listProduct.get(0).get("file_name3");
		String file_name4 = listProduct.get(0).get("file_name4");
		String file_name5 = listProduct.get(0).get("file_name5");
		String file_name6 = listProduct.get(0).get("file_name6");
		String description_file = listProduct.get(0).get("description_file");
		String table_name = listProduct.get(0).get("table_code");
		
		if (standard_type.equals("2")) {
			// 获取全路径信息
			ArrayList<String> list = directoryName(ftp_id, work_directory_id, distinguish,
					standard_type, analytical_rule, search_path1,
					search_path2, file_name1, file_name2, file_name3,
					file_name4, file_name5, file_name6, description_file);
			for (String str : list) {
				// DESC文件名
				String fielName = str.substring(str.lastIndexOf("/") + 1);
				
				responseBody = loadEveryFile( fielName, table_name );
			}
		} else {
			responseBody = loadEveryFile( file_name, table_name );
		}
		
		return responseBody;
	}
	
	/**
	 * 对文件进行相关校验，校验成功后load到指定数据库表
	 * @param fielName DESC文件名(不包含路径)
	 * @param table_name 描述文件对应表名
	 * @return 日志对象
	 */
	private static ResponseBody loadEveryFile( String fielName, String table_name ) {
		
		ResponseBody responseBody = new ResponseBody();
		List<Record> dataList = null;
		FileManagerModel fmm = new FileManagerModel();
		FileCharsetDetector detector = new FileCharsetDetector();
		try {
			dataList = fmm.queryDataFileByDesc( fielName, "2");
		} catch (Exception e) {
			e.printStackTrace();
			responseBody.setStatus(1);
			responseBody.setMes("根据DESC文件获取数据文件异常");
			return responseBody;
		}
		for( Record data : dataList ) {
			String dataname = data.getStr("dataname");
			if ( dataname == null||"".equals(dataname)) {
				responseBody.setStatus(1);
				responseBody.setMes("文件名为空");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			}
			String workpath = data.getStr( "workpath" );
			if ( workpath == null || "".equals(workpath)) {
				responseBody.setStatus(1);
				responseBody.setMes("工作目录为空");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			}
			String fields = data.getStr( "field_1" );
			if ( fields == null || "".equals(fields)) {
				responseBody.setStatus(1);
				responseBody.setMes("字段序列为空");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			}
			String field_2 = data.getStr( "field_2" );
			if ( field_2 != null && !"".equals(field_2)) 
				fields += field_2;
			StringBuffer fileFullPath = new StringBuffer();
			if (workpath.lastIndexOf("/")==workpath.length())
				fileFullPath.append(workpath).append(dataname);
			else
				fileFullPath.append(workpath).append("/").append(dataname);
			
			Connection conn = null;
			try {
				conn = Db.use(xx.DS_MAIN).getConfig().getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
				responseBody.setStatus(1);
				responseBody.setMes("获取数据库连接失败");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			}	
			if (!checkColumns(table_name, fields, conn)) {
				responseBody.setStatus(1);
				responseBody.setMes("字段序与数据库表列不匹配");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			}
			String fileFullName = fileFullPath.toString();
			try {
				if ( !"UTF-8".equals(detector.guessFileEncoding(new File(fileFullName), 2))) {
					// 数据文件转UTF-8 编码
					detector.convert(fileFullName, "UTF-8", "TXT");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				responseBody.setStatus(1);
				responseBody.setMes("字符集转换：数据文件不存在");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			} catch (IOException e) {
				e.printStackTrace();
				responseBody.setStatus(1);
				responseBody.setMes("字符集转换：文件处理IO异常");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			} catch (Exception e) {
				e.printStackTrace();
				responseBody.setStatus(1);
				responseBody.setMes("字符集转换：转换异常");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			}
			// 替换为linux换行符
			try {
				FileUtil.fixSymbol(fileFullName);
			} catch (IOException e) {
				e.printStackTrace();
				responseBody.setStatus(1);
				responseBody.setMes("换行符替换：数据文件换行符异常");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			}
			// 文件去Bom头
			try {
				FileUtil.trimBom(fileFullName);
			} catch (IOException e) {
				e.printStackTrace();
				responseBody.setStatus(1);
				responseBody.setMes("处理数据文件Bom头异常");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			}
			// 数据导入
			int nData = 0;
			try {
				nData = LoadFileData.loadLocalFile(fileFullName, table_name, fields, conn);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				responseBody.setStatus(1);
				responseBody.setMes( "数据Load:" + fileFullName + "数据文不存在");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			} catch (SQLException e) {
				e.printStackTrace();
				responseBody.setStatus(1);
				responseBody.setMes( "数据Load:SQL异常");
				responseBody.setObjectid(data.getStr("id"));
				responseBody.setObjecttype(1);
				return responseBody;
			}
			responseBody.setStatus(0);
			responseBody.setMes("数据文件load成功，实际处理"+ nData + "条数据" + new Date());
			responseBody.setObjectid(data.getStr("id"));
			responseBody.setObjecttype(1);
		}
		return responseBody;
	}

	/**
	 * 转换时间
	 * 
	 * @param file_name4
	 * @param date
	 * @param analytical_rule
	 * @return
	 */
	public static ArrayList<String> date(String file_name4, Date date,
			int analytical_rule) {
		ArrayList<String> list = new ArrayList<String>();
		SimpleDateFormat df1 = new SimpleDateFormat(file_name4);
		for (int i = 0; i < analytical_rule + 1; i++) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_MONTH, -i);
			date = calendar.getTime();
			String date1 = df1.format(date);
			list.add(date1);
		}
		return list;
	}

	/**
	 * 解析IUA
	 * 
	 * @param file_name6
	 * @return
	 */
	public String IUA(String file_name6) {
		String file_name6a = "";
		if (file_name6.equals("1")) {
			file_name6a = "I";
		} else if (file_name6.equals("2")) {
			file_name6a = "U";
		} else if (file_name6.equals("3")) {
			file_name6a = "A";
		}
		return file_name6a;
	}

	/**
	 * 解析目录名
	 * 
	 * @param distinguish
	 * @param standard_type
	 * @param analytical_rule
	 * @param search_path1
	 * @param search_path2
	 * @param file_name1
	 * @param file_name2
	 * @param file_name3
	 * @param file_name4
	 * @param file_name5
	 * @param file_name6
	 * @param description_file
	 * @return
	 */

	public ArrayList<String> directoryName(String ftp_id,
			String work_directory_id, String distinguish, String standard_type,
			int analytical_rule, String search_path1, String search_path2,
			String file_name1, String file_name2, String file_name3,
			String file_name4, String file_name5, String file_name6,
			String description_file) {
		ArrayList<String> listWorkDirectory = new ArrayList<String>();
		ArrayList<String> list1 = new ArrayList<String>();
		Date date = new Date();
		// 获取日期
		listWorkDirectory = date(file_name4, date, analytical_rule);
		// IUA
		String file_name6a = IUA(file_name6);
		// 获取目录名
		List<Record> listDirectory = new ServiceUtil().getWorkDirectoryById(work_directory_id);
		String workName =  listDirectory.get(0).get("working_path");
		if (standard_type.equals("2")) {
			for (String str : listWorkDirectory) {
				String directoryName = "";
				String search_path = str.substring(0, 6);
				directoryName = workName + search_path1 + "/"
						+ search_path + "/" + file_name1 + "-" + file_name2
						+ "-" + file_name3 + "-" + str + "-" + file_name5 + "-"
						+ file_name6a + description_file;
				list1.add(directoryName);
			}
		}
		return list1;
	}

	/**
	 * 大小写转换
	 * 
	 * @return
	 */
	public static String Capitalization(String str, String distinguish) {
		String capitalization = str;
		if (distinguish.equals("1")) {
			// 大写转换
			capitalization = str.toUpperCase();
		} else if (distinguish.equals("2")) {
			// 小写转换
			capitalization = str.toLowerCase();
		}
		return capitalization;
	}
	
	public static void main(String[] args) {
		System.out.println("123".lastIndexOf("3"));
	}
	
	/**
     * 获取表中所有字段名称
     * @param tableName 表名
     * @return
     */
	private static boolean checkColumns(String tableName, String columns, Connection conn ) {

		PreparedStatement pStemt = null;
        boolean flag = false;
		try {
			conn = Db.use(xx.DS_MAIN).getConfig().getConnection();
	        String tableSql = "select "+columns+" from " + tableName + " limit 1";
			pStemt = conn.prepareStatement(tableSql);
			pStemt.executeQuery();
			flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
            return flag;
        } finally{
			try {
				pStemt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
        return flag;
    }
}
