package com.yonyou.ftp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.base.ResponseBody;
import com.yonyou.model.FileManagerModel;
import com.yonyou.service.Translate4DESC;
import com.yonyou.util.FileStatus;
import com.yonyou.util.ServiceUtil;

public class FtpService {

	/**
	 * FTP流
	 * 
	 * @param id
	 * @return
	 */
	public ResponseBody File_Name(String id) {
		List<Record> listProduct = ServiceUtil.dao.getBsFtpFlow(id);
		ArrayList<String> list = new ArrayList<String>();
		ResponseBody responseBody = new ResponseBody();
		for (int i = 0; i < listProduct.size(); i++) {
			String ftpId = listProduct.get(i).get("ftp_id");// ftpID
			String workDirectoryId = listProduct.get(i).get(
					"workdirectory_id");// 工作目录ID
			String distinguish = listProduct.get(i).get("distinguish");// 大小写
			String standardType = listProduct.get(i).get("standard_type");// 规范类型
			String day_rule = listProduct.get(i).get("day_rule");// 解析规则
			String searchPath = listProduct.get(i).get("search_path");// 搜索路径
			String fileName = listProduct.get(i).get("file_name");
			String searchPath1 = listProduct.get(i).get("search_path1");// 搜索路径1
			String searchPath2 = listProduct.get(i).get("search_path2");// 搜索路径2
			String fileName1 = listProduct.get(i).get("file_name1");
			String fileName2 = listProduct.get(i).get("file_name2");
			String fileName3 = listProduct.get(i).get("file_name3");
			String fileName4 = listProduct.get(i).get("file_name4");
			String fileName5 = listProduct.get(i).get("file_name5");
			String fileName6 = listProduct.get(i).get("file_name6");
			String descriptionFile = listProduct.get(i).get("description_file");
			// 获取ftp信息
			System.out.println("ftp_id:" + ftpId);
			List<Record> ftpList = ServiceUtil.dao.getFtpById(ftpId);
			String ftpPath = "";// 路径
			int ftpPort = 0;// 端口
			String ftpAddress = "";// 地址
			String ftpUsername = "";// 帐号
			String ftpPassword = "";// 密码
			for (int q = 0; q < ftpList.size(); q++) {
				ftpPath = ftpList.get(q).get("ftp_path");
				ftpPort = ftpList.get(q).get("ftp_port");
				ftpAddress = ftpList.get(q).get("ftpAddress");
				ftpUsername = ftpList.get(q).get("ftpUsername");
				ftpPassword = ftpList.get(q).get("ftp_password");
			}
			System.out.println("地址:" + ftpAddress + "端口：" + ftpPort + "帐号："
					+ ftpUsername + "密码：" + ftpPassword);
			// 判断固定、解析
			if (standardType.equals("2")) {
				// 获取全路径信息
				list = directoryName(ftpId, workDirectoryId, distinguish,
						standardType, day_rule, searchPath1,
						searchPath2, fileName1, fileName2, fileName3,
						fileName4, fileName5, fileName6, descriptionFile);
				for (String str : list) {
					// 文件名
					String fielName = str.substring(str.lastIndexOf("/") + 1);
					// 目录名
					String directoryName = str.substring(0,
							str.lastIndexOf("/") + 1);
					// 全路径
					String workDirectoryName = Capitalization(directoryName,
							distinguish) + fielName;
					System.out.println("文件名:"+fielName);
					System.out.println("目录名:"+directoryName);
					System.out.println(workDirectoryName);
					//公共下载方法
					downloadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword,
							ftpPath, fileName, directoryName, workDirectoryName);
				}
			}else{
				//公共下载方法
				downloadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword,
						ftpPath, fileName, searchPath, searchPath + fileName);
			}
		}
		responseBody.setStatus(0);
		responseBody.setMes("下载成功"+ new Date());
		return responseBody;
	}

	/**
	 * 根据指定信息下载
	 * @param ftpAddress ftp地址
	 * @param ftpPort  ftp端口号
	 * @param ftpUsername ftp账号
	 * @param ftpPassword  ftp 密码
	 * @param ftpPath ftp路径
	 * @param fielName 文件名
	 * @param directoryName 目录名
	 * @param workDirectoryName 全路径
	 * @return
	 */
	public ResponseBody downloadFile(String ftpAddress,int ftpPort,String ftpUsername,String ftpPassword,String ftpPath,String fielName,String directoryName,
			String workDirectoryName){
		ResponseBody responseBody=new ResponseBody();
		boolean yn;
		try {
			yn = FileManagerModel.dao.checkDescExist(fielName);
		} catch (Exception e) {
			e.printStackTrace();
			responseBody.setStatus(1);
			responseBody.setMes(e.getClass().getName());
			return responseBody;
		}
		//文件不存在不能下载
		if(!yn){
			Translate4DESC translate4desc = new Translate4DESC();
			List<String> listTxt = new ArrayList<String>();
			int rt = 0;
				try {
					rt = new FTPClientFactory(ftpAddress, ftpPort,
							ftpUsername, ftpPassword).downLoadFile(
									ftpPath+"/", fielName, directoryName);
				} catch (Exception e) {
					e.printStackTrace();
					responseBody.setStatus(1);
					responseBody.setMes("IP为:" + ftpAddress + "的FTP连接失败");
					return responseBody;
				}
			//判断连接是否成功
			if (rt == 1) {
				try {
					//获取desc中的txt
					listTxt = translate4desc.execute(workDirectoryName);
				} catch (Exception e) {
					e.printStackTrace();
					responseBody.setStatus(1);
					responseBody.setMes("获取"+workDirectoryName +"中的TXT文件失败");
					return responseBody;
				}
	
				for (int j = 1; j < listTxt.size(); j++) {
					String[]  strs=listTxt.get(j).split(",");
					String txtName = strs[0];
					int up = 0;
					try {
						// 下载前更新状态
						up = FileManagerModel.dao.updataFileStatus(
								txtName, FileStatus.UPDATING);
					} catch (Exception e1) {
						e1.printStackTrace();
						responseBody.setStatus(1);
						responseBody.setMes(txtName + "下载前更新状态失败");
						return responseBody;
					}
					if (up >= 1) {
							try {
								if (new FTPClientFactory(ftpAddress,
										ftpPort, ftpUsername,
										ftpPassword).downLoadFile(ftpPath+"/",
												txtName, directoryName) == 0) {
									try {
										FileManagerModel.dao.updataFileStatus(txtName,FileStatus.FAIL);
									} catch (Exception e) {
										e.printStackTrace();
										responseBody.setStatus(1);
										responseBody.setMes(txtName + "下载失败时更新状态失败");
										return responseBody;
									}
								} else {
									//下载完成更新状态
									try {
										FileManagerModel.dao.updataFileStatus(
												txtName, FileStatus.FINISH);
									} catch (Exception e) {
										e.printStackTrace();
										responseBody.setStatus(1);
										responseBody.setMes(txtName + "下载完成更新状态失败");
										return responseBody;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								responseBody.setStatus(1);
								responseBody.setMes("IP为:" + ftpAddress + "的FTP连接失败");
								return responseBody;
							}
					} else {
						try {
							FileManagerModel.dao.updataFileStatus(
									txtName, FileStatus.FAIL);
						} catch (Exception e) {
							e.printStackTrace();
							responseBody.setStatus(1);
							responseBody.setMes(txtName + "下载前更新状态失败,修改失败状态时失败");
							return responseBody;
						}
					}
				}
			} else if (rt == 0) {
				responseBody.setStatus(1);
				responseBody.setMes("下载DESC文件失败:"+fielName);
				return responseBody;
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
			String day_rule, String search_path1, String search_path2,
			String file_name1, String file_name2, String file_name3,
			String file_name4, String file_name5, String file_name6,
			String description_file) {
		ArrayList<String> listWorkDirectory = new ArrayList<String>();
		ArrayList<String> list1 = new ArrayList<String>();
		Date date = new Date();
		// 获取日期
		listWorkDirectory = date(file_name4, date, day_rule);
		// 获取目录名
		List<Record> listDirectory = ServiceUtil.dao.getWorkDirectoryById(work_directory_id);
		String workName = "";
		for (int i = 0; i < listDirectory.size(); i++) {
			workName = listDirectory.get(i).get("working_path");
		}
		if (standard_type.equals("2")) {
			for (String str : listWorkDirectory) {
				String directoryName = "";
				String search_path = str.substring(0, 6);
				directoryName = workName + search_path1 + "/"
						+ search_path + "/" + file_name1 + "-" + file_name2
						+ "-" + file_name3 + "-" + str + "-" + file_name5 + "-"
						+ file_name6 + description_file;
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
}
