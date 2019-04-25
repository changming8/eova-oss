package com.yonyou.ftp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;
import com.yonyou.base.ResponseBody;
import com.yonyou.model.FileManagerModel;
import com.yonyou.util.FileStatus;
import com.yonyou.util.LockUtils;
import com.yonyou.util.ServiceUtil;

public class FtpService {

	/**
	 * FTP流
	 * 
	 * @param id
	 * @return
	 */
	public ResponseBody File_Name(String id) {
		Date date = new Date();
		List<Record> listProduct = ServiceUtil.dao.getBsFtpFlow(id);
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> list2 = new ArrayList<String>();
		ResponseBody responseBody = new ResponseBody(0, "操作成功");
		for (int i = 0; i < listProduct.size(); i++) {
			String ftpId = listProduct.get(i).get("ftp_id");// ftpID
			String flowtypeId = listProduct.get(i).get("flowtype_id");
			String workDirectoryId = listProduct.get(i).get("workdirectory_id");// 工作目录ID
			String directoryName = ServiceUtil.dao.getWorkDirectoryById(workDirectoryId).get(0).getStr("working_path");// 目标目录
			String distinguish = listProduct.get(i).get("distinguish");// 大小写
			String analysis_rule = listProduct.get(i).get("analysis_rule");// 解析规则 // 固定式，解析式
			String day_rule = listProduct.get(i).get("day_rule");// 日期解析规则
			String searchPath = listProduct.get(i).get("search_path");// 搜索路径
			String fileName = listProduct.get(i).get("file_name");
			String ftpflowAttribute = listProduct.get(i).get("ftpflow_attribute");// 上传-下载
			String searchPath1 = listProduct.get(i).get("search_path1");// 搜索路径1
			String searchPath2 = listProduct.get(i).get("search_path2");// 搜索路径2
			String fileName1 = listProduct.get(i).get("file_name1");
			String fileName2 = listProduct.get(i).get("file_name2");
			String fileName3 = listProduct.get(i).get("file_name3");
			String fileName4 = listProduct.get(i).get("file_name4");
			String fileName5 = listProduct.get(i).get("file_name5");
			String fileName6 = listProduct.get(i).get("file_name6");
			String descriptionFileId = listProduct.get(i).get("description_file");
			String descriptionFile = ServiceUtil.dao.getBsDicts(descriptionFileId).get(0).getStr("name");
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
				ftpAddress = ftpList.get(q).get("ftp_address");
				ftpUsername = ftpList.get(q).get("ftp_username");
				ftpPassword = ftpList.get(q).get("ftp_password");
			}
			System.out.println("地址:" + ftpAddress + "端口：" + ftpPort + "帐号：" + ftpUsername + "密码：" + ftpPassword);
			responseBody.setObjectid(id);
			responseBody.setFlowtypeId(flowtypeId);
			// 上传
			if (ftpflowAttribute.equals("1")) {
				if (analysis_rule.equals("2")) {
					// 解析式
					// DESC.ERR&DESC.OK需要读取两个文件
					if (descriptionFile.equals(".DESC.ERR&DESC.OK")) {
						list = directoryName(distinguish, analysis_rule, day_rule, searchPath1, searchPath2, fileName1, fileName2,
								fileName3, fileName4, fileName5, fileName6, ".DESC.ERR", date, ftpPath, directoryName);
						list2 = directoryName(distinguish, analysis_rule, day_rule, searchPath1, searchPath2, fileName1, fileName2,
								fileName3, fileName4, fileName5, fileName6, ".DESC.OK", date, ftpPath, directoryName);
						// 上传DESC.ERR
						for (String str : list) {
							String path[] = str.split(",");
							// 文件名
							String fielName = path[0].substring(path[0].lastIndexOf("/") + 1);
							// 当前ftp路径
							String ftpFilepath = path[0].substring(0, path[0].lastIndexOf("/") + 1);
							// 源目录
							String filepath = directoryName + "/" + fielName;
							//判断文件是否上传过,防止重复上传
							boolean flag=false;
							try {
								flag = FileManagerModel.dao.checkDescExistOfType(fielName,"2");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(!flag) {
								uploadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword, fielName, ftpFilepath, filepath,
									"1",responseBody);
							}
						}
						// 上传DESC.OK
						for (String str : list2) {
							String path[] = str.split(",");
							// 文件名
							String fielName = path[0].substring(path[0].lastIndexOf("/") + 1);
							// 当前ftp路径
							String ftpFilepath = path[0].substring(0, path[0].lastIndexOf("/") + 1);
							// 大小写转换
							fielName = Capitalization(fielName, distinguish);
							// 源目录
							String filepath = directoryName + "/" + fielName;
							//判断文件是否上传过,防止重复上传
							boolean flag=false;
							try {
								flag = FileManagerModel.dao.checkDescExistOfType(fielName,"2");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(!flag) {
								uploadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword, fielName, ftpFilepath, filepath,
									"1",responseBody);
							}
						}

					} else {
						list = directoryName(distinguish, analysis_rule, day_rule, searchPath1, searchPath2, fileName1,
								fileName2, fileName3, fileName4, fileName5, fileName6, descriptionFile, date, ftpPath,
								directoryName);
						for (String str : list) {
							String path[] = str.split(",");
							// 文件名
							String fielName = path[0].substring(path[0].lastIndexOf("/") + 1);
							// 当前ftp路径
							String ftpFilepath = path[0].substring(0, path[0].lastIndexOf("/") + 1);
							// 大小写转换
							fielName = Capitalization(fielName, distinguish);
							// 源目录
							String filepath = directoryName + "/" + fielName;
							//判断文件是否上传过,防止重复上传
							boolean flag=false;
							try {
								flag = FileManagerModel.dao.checkDescExistOfType(fielName,"2");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(!flag) {
							uploadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword, fielName, ftpFilepath, filepath,"1",
									responseBody);
							}
						}
					}
				} else {
					fileName = Capitalization(fileName, distinguish);
					//判断文件是否上传过
					boolean flag=false;
					try {
						flag = FileManagerModel.dao.checkDescExistOfType(fileName,"2");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(!flag) {
						uploadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword, fileName, ftpPath + "/" + searchPath+"/",
							directoryName + "/" + fileName,"2", responseBody);
					}
				}

			} else {
				// 判断固定、解析
				if (analysis_rule.equals("2")) {
					// DESC.ERR&DESC.OK需要读取两个文件
					if (descriptionFile.equals(".DESC.ERR&DESC.OK")) {
						// 获取全路径信息
						list = directoryName(distinguish, analysis_rule, day_rule, searchPath1, searchPath2, fileName1,
								fileName2, fileName3, fileName4, fileName5, fileName6, ".DESC.ERR", date, ftpPath,
								directoryName);
						// 获取全路径信息
						list2 = directoryName(distinguish, analysis_rule, day_rule, searchPath1, searchPath2, fileName1,
								fileName2, fileName3, fileName4, fileName5, fileName6, ".DESC.OK", date, ftpPath,
								directoryName);
						for (String str : list) {
							str = str.split(",")[0];
							// 文件名
							String fielName = str.substring(str.lastIndexOf("/") + 1);
							fielName = Capitalization(fielName, distinguish);
							System.out.println("文件名:" + str);
							System.out.println(str);
							boolean yn;
							try {
								yn = FileManagerModel.dao.checkDescExist(fielName);
							} catch (Exception e) {
								e.printStackTrace();
								responseBody.setStatus(1);
								responseBody.setMes(e.getClass().getName());
								return responseBody;
							}
							if (!yn) {
								// 公共下载方法
								downloadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword, fielName, directoryName+"/",
										str, responseBody);
							}
						}
						for (String str : list2) {
							str = str.split(",")[0];
							// 文件名
							String fielName = str.substring(str.lastIndexOf("/") + 1);
							fielName = Capitalization(fielName, distinguish);
							System.out.println("文件名:" + str);
							System.out.println(str);
							boolean yn;
							try {
								yn = FileManagerModel.dao.checkDescExist(fielName);
							} catch (Exception e) {
								e.printStackTrace();
								responseBody.setStatus(1);
								responseBody.setMes(e.getClass().getName());
								return responseBody;
							}
							if (!yn) {
								// 公共下载方法
								downloadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword, fielName, directoryName+"/",
										str, responseBody);
							}
						}
					} else {
						// 获取全路径信息
						list = directoryName(distinguish, analysis_rule, day_rule, searchPath1, searchPath2, fileName1,
								fileName2, fileName3, fileName4, fileName5, fileName6, descriptionFile, date, ftpPath,
								directoryName);
						for (String str : list) {
							str = str.split(",")[0];
							// 文件名
							String fielName = str.substring(str.lastIndexOf("/") + 1);
							fielName = Capitalization(fielName, distinguish);
							System.out.println("文件名:" + str);
							System.out.println(str);
							boolean yn;
							try {
								yn = FileManagerModel.dao.checkDescExist(fielName);
							} catch (Exception e) {
								e.printStackTrace();
								responseBody.setStatus(1);
								responseBody.setMes(e.getClass().getName());
								return responseBody;
							}
							if (!yn) {
								// 公共下载方法
								downloadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword, fielName, directoryName+"/",
										str, responseBody);
							}
						}
					}

				} else {
					boolean yn;
					try {
						fileName = Capitalization(fileName, distinguish);
						yn = FileManagerModel.dao.checkDescExist(fileName);
					} catch (Exception e) {
						e.printStackTrace();
						responseBody.setStatus(1);
						responseBody.setMes(e.getClass().getName());
						return responseBody;
					}
					if (!yn) {
						// 公共下载方法
						downloadFile(ftpAddress, ftpPort, ftpUsername, ftpPassword, fileName, directoryName+"/",
								ftpPath + "/" + searchPath+"/" + fileName, responseBody);
					}
				}
			}

		}
		return responseBody;
	}

	/**
	 * 根据指定信息上传
	 * 
	 * @param ftpAddress        ftp地址
	 * @param ftpPort           ftp端口号
	 * @param ftpUsername       ftp账号
	 * @param ftpPassword       ftp 密码
	 * @param ftpPath           ftp路径
	 * @param fielName          文件名
	 * @param directoryName     目标目录
	 * @param workDirectoryName 源路径，包含文件名
	 * @param type 上传类型 1-解析式 2-固定式
	 * @return
	 */
	public void uploadFile(String ftpAddress, int ftpPort, String ftpUsername, String ftpPassword, String fielName,
			String directoryName, String workDirectoryName,String type, ResponseBody responseBody) {
		String dirPath = workDirectoryName.substring(0, workDirectoryName.lastIndexOf("/") + 1);
		try {
			if (new FTPClientFactory(ftpAddress, ftpPort, ftpUsername, ftpPassword).sendFile(directoryName,
					dirPath, fielName)) {
				//上传成功记录数据，解析式和固定式用不同方法处理
				if(type.equals("1")) {
					FileManagerModel.dao.SaveDescAnalysisFile(FTPClientFactory.trimPath(workDirectoryName),
							FTPClientFactory.trimPath(directoryName));
				}else {
					FileManagerModel.dao.SaveDescFixedFile(FTPClientFactory.trimPath(workDirectoryName),
							FTPClientFactory.trimPath(directoryName));
				}
				responseBody.setMes(directoryName + "上传文件成功,文件名:" + fielName);
				return;
			} else {
				responseBody.setStatus(1);
				responseBody.setMes(directoryName + "上传文件失败,文件名:" + fielName);
				return;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseBody.setStatus(1);
			responseBody.setMes(directoryName+"文件上传时失败,文件名:" + fielName);
			return;
		}

	}

	/**
	 * 根据指定信息下载
	 * 
	 * @param ftpAddress        ftp地址
	 * @param ftpPort           ftp端口号
	 * @param ftpUsername       ftp账号
	 * @param ftpPassword       ftp 密码
	 * @param ftpPath           ftp路径
	 * @param fielName          文件名
	 * @param directoryName     目标目录
	 * @param workDirectoryName 源路径，包含文件名
	 * @return
	 */
	public void downloadFile(String ftpAddress, int ftpPort, String ftpUsername, String ftpPassword, String fielName,
			String directoryName, String workDirectoryName, ResponseBody responseBody) {
		String dirPath = workDirectoryName.substring(0, workDirectoryName.lastIndexOf("/") + 1);
		List<String> listTxt = new ArrayList<String>();
		boolean rt = false;
		try {
			rt = new FTPClientFactory(ftpAddress, ftpPort, ftpUsername, ftpPassword).downLoadFile(dirPath, fielName,
					directoryName);
		} catch (Exception e) {
			e.printStackTrace();
			responseBody.setStatus(1);
			responseBody.setMes("IP为:" + ftpAddress + "的FTP连接失败,文件名:"+directoryName + fielName);
			return;
		}
		// 判断连接是否成功
		if (rt) {
			try {
//				if(!LockUtils.pkLock(responseBody.getId(),responseBody.getFlowtypeId())) {
//					responseBody.setStatus(1);
//					responseBody.setMes("资源PK锁被占用："+fielName);
//					return;
//				}
				// 获取desc中的txt
				listTxt = new FTPClientFactory(ftpAddress, ftpPort, ftpUsername, ftpPassword)
						.readFile(workDirectoryName);
//				LockUtils.unpkLock(responseBody.getId()+responseBody.getFlowtypeId());
			} catch (Exception e) {
				e.printStackTrace();
				responseBody.setStatus(1);
				responseBody.setMes("读取" + workDirectoryName + "中的TXT文件流信息失败" );
				return;
			}

			for (int j = 1; j < listTxt.size(); j++) {
				String[] strs = listTxt.get(j).split(",");
				String txtName = strs[0];
				int up = 0;
				try {
					// 下载前更新状态
					up = FileManagerModel.dao.updataFileStatus(txtName, FileStatus.UPDATING);
				}  catch (Exception e) {
					e.printStackTrace();
					responseBody.setStatus(1);
					responseBody.setMes(txtName + "下载前更新状态失败,父级文件名:"+workDirectoryName );
					return;
				}
				if (up >= 1) {
					try {
						if (!new FTPClientFactory(ftpAddress, ftpPort, ftpUsername, ftpPassword)
								.downLoadFile(dirPath + "/", txtName, directoryName)) {
							try {
								FileManagerModel.dao.updataFileStatus(txtName, FileStatus.FAIL);
							} catch (Exception e) {
								e.printStackTrace();
								responseBody.setStatus(1);
								responseBody.setMes(txtName + "下载失败时更新状态失败,文件名:" + fielName);
								return;
							}
						} else {
							// 下载完成更新状态
							try {
								FileManagerModel.dao.updataFileStatus(txtName, FileStatus.FINISH);
							} catch (Exception e) {
								e.printStackTrace();
								responseBody.setStatus(1);
								responseBody.setMes(txtName + "下载完成更新状态失败,文件名:" + fielName);
								return;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						responseBody.setStatus(1);
						responseBody.setMes("IP为:" + ftpAddress + "的FTP连接失败,文件名:" + fielName);
						return;
					}
				} else {
					try {
						FileManagerModel.dao.updataFileStatus(txtName, FileStatus.FAIL);
					} catch (Exception e) {
						e.printStackTrace();
						responseBody.setStatus(1);
						responseBody.setMes(txtName + "下载前更新状态失败,修改失败状态时失败,文件名:" + fielName);
						return;
					}
				}
			}
		} else {
			responseBody.setStatus(1);
			responseBody.setMes("下载DESC文件失败,文件名:" + fielName);
			return;
		}
	}

	/**
	 * 转换时间
	 * 
	 * @param file_name4
	 * @param date
	 * @param analytical_rule
	 * @return
	 */
	public static ArrayList<String> date(String file_name4, Date date, int analytical_rule) {
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
	 * @param analysis_rule    // 解析规则 // 固定式，解析式
	 * @param day_rule
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

	public ArrayList<String> directoryName(String distinguish, String analysis_rule, String day_rule,
			String search_path1, String search_path2, String file_name1, String file_name2, String file_name3,
			String file_name4, String file_name5, String file_name6, String description_file, Date date, String ftpPath,
			String directoryName) {
		ArrayList<String> listWorkDirectory = new ArrayList<String>();
		ArrayList<String> list1 = new ArrayList<String>();
		// 获取日期
		listWorkDirectory = date(file_name4, date, Integer.parseInt(day_rule));
		if (analysis_rule.equals("2")) {
			for (String str : listWorkDirectory) {
				String ftpdirectoryName2 = "";// ftp路径
				String directoryName2 = "";// 本地路径
				String search_path = formatDate(str, file_name4, search_path2);
				ftpdirectoryName2 = ftpPath + "/" + search_path1 + "/" + search_path + "/" + file_name1 + "-"
						+ file_name2 + "-" + file_name3 + "-" + str + "-" + file_name5 + "-" + file_name6 
						+ description_file;
				directoryName2 = directoryName + "/" + file_name1 + "-" + file_name2 + "-" + file_name3 + "-" + str
						+ "-" + file_name5 + "-" + file_name6 + description_file;
				list1.add(ftpdirectoryName2 + "," + directoryName2);
			}

		}
		return list1;
	}

	/**
	 * 日期格式化
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public String formatDate(String date, String dateformat, String format) {
		SimpleDateFormat df1 = new SimpleDateFormat(dateformat);
		SimpleDateFormat df2 = new SimpleDateFormat(format);
		try {
			Date d = df1.parse(date);
			return df2.format(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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
		FtpService fs = new FtpService();
		fs.File_Name("20190419175418763002");
	}
}
