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
import com.eova.common.utils.util.StringUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.base.ResponseBody;
import com.yonyou.model.FileManagerModel;
import com.yonyou.util.FileCharsetDetector;
import com.yonyou.util.FileUtil;
import com.yonyou.util.ServiceUtil;

/**
 * 数据文件load到数据库指定表环节
 * @author changming
 */
public class LoadDataService {

	/**
	 * load数据文件
	 * @param id
	 * @return
	 */
	public ResponseBody loadData(String id) {
		
		List<Record> listProduct = Db.use(xx.DS_EOVA).find("select * from bs_load_flow where id ='" + id + "'");//拼接DESC文件路径和文件名数据
		//DESC对应数据库表
		String tableId = listProduct.get(0).get("table_id");
		if (StringUtils.isEmpty(tableId)) {
			return getResponseBody("DESC文件对应表名为空,任务终止",1);
		}
		
		ServiceUtil su = new ServiceUtil();
		List<Record> listMdata = su.getBsMetadata(tableId);
		if(listMdata == null || listMdata.size() == 0) 
			return getResponseBody("获取BsMetadata元数据为空,任务终止",1);
		String tableName = listMdata.get(0).get("table_code");
		if (StringUtils.isEmpty(tableName)) 
			return getResponseBody("获取BsMetadata元数据为空,任务终止",1);
		
		ResponseBody responseBody = new ResponseBody();
		
		//解析规则
		String analysis_rule = listProduct.get(0).get("analysis_rule");
		if (analysis_rule.equals("1")) {//固定式
			// 获取全路径信息
			List<String> list = null;
			try {
				list = getDESCName( listProduct );
				if (list ==null || list.size() == 0) {
					return getResponseBody("未获取到DESC文件名",1);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return getResponseBody("获取DESC文件名异常",1);
			} 
			
			for (String str : list) {
				//responseBody = loadEveryFile( str, tableName);
			}
			return getResponseBody("暂不支持固定式",1);
		} else if(analysis_rule.equals("2")) {//解析式
			// 获取全路径信息
			List<String> list = null;
			try {
				list = getDESCName( listProduct );
				if (list ==null || list.size() == 0) {
					return getResponseBody("未获取到DESC文件名",1);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return getResponseBody("获取DESC文件名异常",1);
			}
			FileManagerModel fmm = new FileManagerModel();
			
			try {
				List<Record> descList = fmm.queryDescFileByDesc(list);
				if(null == descList || descList.size() == 0)
					return getResponseBody("查询DESC文件为空",1);
				for(Record data : descList) {
					responseBody = loadEveryFile(data.getStr("allname"), tableName);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				return getResponseBody("查询desc文件信息失败",1);
			}
		}
		return responseBody;
	}
	
	public static boolean exportData2File( String tableName) {
		
		boolean flag = false;
		
		return flag;
	}
	
	/**
	 * 对文件进行相关校验，校验成功后load到指定数据库表
	 * @param fielName DESC文件名(不包含路径)
	 * @param table_name 描述文件对应表名
	 * @return 日志对象
	 */
	private static ResponseBody loadEveryFile( String fileName, String table_name) {
		
		ResponseBody responseBody = new ResponseBody();
		List<Record> dataList = null;
		FileManagerModel fmm = new FileManagerModel();
		FileCharsetDetector detector = new FileCharsetDetector();
		try {
			dataList = fmm.queryDataFileByDesc(fileName);
			if (dataList ==null || dataList.size() == 0) {
				return getResponseBody("未获取到DESC文件名",1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return getResponseBody("根据DESC文件获取数据文件异常",1);
		} 
		
		for( Record data : dataList ) {
			if("002".equals(data.get("status"))) {
				//文件id
				String dataname = data.getStr("dataname");
				String fileId = data.getStr("id");
				if (StringUtils.isEmpty(dataname)) {
					return getResponseBody("数据文件名为空",1);
				}
				try {
					fmm.updataFileStatus(fileName, "201");//更新数据为导入中状态
				} catch (Exception e1) {
					e1.printStackTrace();
					return getResponseBody("更新"+ fileName +"数据文件状态失败",1);
				}
				//路径
				String workpath = data.getStr( "workpath" );
				if (StringUtils.isEmpty(workpath)) {
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("工作目录为空",1);
				}
				//字段序列
				String fields = data.getStr( "field_1" );
				if (StringUtils.isEmpty(fields)) {
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("字段序列为空",1);
				}
				//字段序列>4000拼field_2
				String field_2 = data.getStr( "field_2" );
				if (StringUtils.isEmpty(field_2)) 
					fields += field_2;
				//校验路径规范
				StringBuffer fileFullPath = new StringBuffer();
				if (workpath.lastIndexOf("/")==workpath.length()-1)
					fileFullPath.append(workpath).append(dataname);
				else
					fileFullPath.append(workpath).append(File.separator).append(dataname);
				
				Connection conn = null;
				try {//切到业务数据库
					conn = Db.use(xx.DS_MAIN).getConfig().getConnection();
				} catch (SQLException e) {
					e.printStackTrace();
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("获取数据库连接失败",1);
				}
				//校验字段序列
				if (!checkColumns(table_name, fields, conn)) {
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("字段序与数据库表列不匹配",1);
				}
				String fileFullName = fileFullPath.toString();
				//数据文件转UTF-8
				try {
					if ( !"UTF-8".equals(detector.guessFileEncoding(new File(fileFullName), 2))) {
						// 数据文件转UTF-8 编码
						detector.convert(fileFullName, "UTF-8", "TXT");
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("字符集转换：数据文件不存在",1);
				} catch (IOException e) {
					e.printStackTrace();
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("字符集转换：文件处理IO异常",1);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("字符集转换：转换异常",1);
				} 
				// 数据文件换行符替换为linux换行符
				/*try {
					FileUtil.fixSymbol(fileFullName);
				} catch (IOException e) {
					e.printStackTrace();
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("换行符替换：数据文件换行符异常",1);
				}*/
				// 数据文件去Bom头
				try {
					FileUtil.trimBom(fileFullName);
				} catch (IOException e) {
					e.printStackTrace();
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("处理数据文件Bom头异常",1);
				}
				// 数据导入
				int nData = 0;
				try {
					File f = new File(fileFullName);
					nData = LoadFileData.loadLocalFile(f.getAbsolutePath(), table_name, fields, conn);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("数据Load:" + fileFullName + "数据文不存在",1);
				} catch (SQLException e) {
					e.printStackTrace();
					try {
						fmm.updataFileStatus(fileName, "203");//更新为导失败状态
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return getResponseBody("数据Load:SQL异常" + fileFullName + "数据文不存在",1);
				}
				try {
					fmm.updataFileStatus(fileName, "202");//更新为导完成状态
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				responseBody.setStatus(0);
				if (responseBody.getMes()!=null)
					responseBody.setMes(responseBody.getMes()+"\n数据文件load成功，实际处理"+ nData + "条数据" + new Date());
				else
					responseBody.setMes("数据文件load成功，实际处理"+ nData + "条数据" + new Date());
				if (responseBody.getObjectid()!=null)
					responseBody.setObjectid(responseBody.getObjectid()+","+fileId);
				else 
					responseBody.setObjectid(fileId);
				responseBody.setObjecttype(1);
			} else if ("202".equals(data.get("status"))) {
				return getResponseBody("数据状态已导入完成",0);
			} else {
				return getResponseBody("数据状态不为下载完成或者导入完成",1);
			}
		}
		if(0 == responseBody.getStatus()) {
			try {
				fmm.updataDescStatus(fileName, "202");//更新desc文件状态为导入中
			} catch (Exception e1) {
				e1.printStackTrace();
				return getResponseBody("数据导入成功，但更新"+ fileName +"desc文件状态失败",1);
			}
		} else {
			try {
				fmm.updataDescStatus(fileName, "203");//更新desc文件状态为导入中
			} catch (Exception e1) {
				e1.printStackTrace();
				return getResponseBody("数据导入失败，更新"+ fileName +"desc文件状态失败",1);
			}
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
	public static List<String> date(String file_name4, Date date,
			int analytical_rule) {
		List<String> list = new ArrayList<String>();
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

	private List<String> getDESCName(List<Record> list) throws Exception{
		ServiceUtil serviceUtil = new ServiceUtil();
		if ( list ==null || list.size()==0)
			throw new Exception();
		//获取解析方式
		String analysis_rule = list.get(0).get("analysis_rule");
		if ( analysis_rule ==null || "".equals( analysis_rule ))
			throw new Exception();
		
		List<String> resuts = new ArrayList<String>();
		//连接符
		String concatws = "-";
		//获取取数据天数
		String day_rule = list.get(0).get("day_rule");
		if (StringUtils.isEmpty( day_rule ))
			throw new Exception();
		//日期格式
		String file_name4 = list.get(0).get("file_name4");
		if (StringUtils.isEmpty( file_name4 ))
			throw new Exception();
		//文件名前缀
		String file_name1 = list.get(0).get("file_name1");
		if (StringUtils.isEmpty( file_name1 ))
			throw new Exception();
		String file_name2 = list.get(0).get("file_name2");
		if (StringUtils.isEmpty( file_name2 ))
			throw new Exception();
		String file_name3 = list.get(0).get("file_name3");
		if (StringUtils.isEmpty( file_name3 ))
			throw new Exception();
		//文件名后缀
		String file_name5 = list.get(0).get("file_name5");
		if (StringUtils.isEmpty( file_name5 ))
			throw new Exception();
		String file_name6 = list.get(0).get("file_name6");
		if (StringUtils.isEmpty( file_name6 ))
			throw new Exception();
		String description_file = list.get(0).get("description_file");
		if (StringUtils.isEmpty( description_file ))
			throw new Exception();
		
		List<Record> description = serviceUtil.getBsDicts(description_file);
		if(null == description || description.size() == 0)
			throw new Exception();
		// 大小写
		String distinguish = list.get(0).get("distinguish");// 大小写
		if (StringUtils.isEmpty( distinguish ))
			throw new Exception();
		//获取文件名日期段
		List<String> dateNames = date( file_name4, new Date(), Integer.parseInt(day_rule));
		
		for ( String dateName : dateNames) {
			StringBuilder descname = new StringBuilder();
			descname.append(file_name1).append(concatws).append(file_name2).append(concatws)
				.append(file_name3).append(concatws).append(dateName).append(concatws)
				.append(file_name5).append(concatws).append(file_name6).append(description.get(0).getStr("name"));
			if (distinguish.equals("1")) {
				resuts.add(descname.toString().toUpperCase());
			}
			else if(distinguish.equals("2")) {
				resuts.add(descname.toString().toLowerCase());
			}
		}
		return resuts;
	}
	
	/**
     * 校验DESC文件中字段序列也数据库表相应是否一致
     * @param tableName 表名
     * @param columns 表名
     * @param conn 数据库连接
     * @return 校验结果
     */
	private static boolean checkColumns(String tableName, String columns, Connection conn ) {

		PreparedStatement pStemt = null;
        boolean flag = false;
		try {
			conn = Db.use(xx.DS_MAIN).getConfig().getConnection();
	        String tableSql = "select " + columns + " from " + tableName + " limit 1";
			pStemt = conn.prepareStatement(tableSql);
			pStemt.executeQuery();
			flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
			try {
				pStemt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
        return flag;
    }
	
	/**
	 * 构建ResponseBody
	 * @param mes 消息内容
	 * @param Objectid 数据主键
	 * @param status 执行状态
	 * @param objecttype 功能类型
	 * @return
	 */
	private static ResponseBody getResponseBody(String mes, int status) {
		ResponseBody spbody = new ResponseBody();
		spbody.setStatus(status);
		spbody.setMes(mes);
		return spbody;
	}
}
