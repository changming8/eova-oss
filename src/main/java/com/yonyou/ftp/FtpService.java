package com.yonyou.ftp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
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
		Translate4DESC translate4desc = new Translate4DESC();
		String sqlStringProduct = "select * from bs_ftp_flow where id ='" + id
				+ "'";
		List<Record> listProduct = Db.use(xx.DS_EOVA).find(sqlStringProduct);
		ArrayList<String> list = new ArrayList<String>();
		List<String> listTxt = new ArrayList<String>();
		ResponseBody responseBody = new ResponseBody();
		for (int i = 0; i < listProduct.size(); i++) {
			String ftp_config_name = listProduct.get(i).get("ftp_config_name");
			String ftp_id = listProduct.get(i).get("ftp_id");// ftpID
			String work_directory_id = listProduct.get(i).get(
					"workdirectory_id");// 工作目录ID
			String distinguish = listProduct.get(i).get("distinguish");// 大小写
			String standard_type = listProduct.get(i).get("standard_type");// 规范类型
			int analytical_rule = listProduct.get(i).get("analytical_rule");// 解析规则
			String search_path = listProduct.get(i).get("search_path");// 搜索路径
			String file_name = listProduct.get(i).get("file_name");
			String search_path1 = listProduct.get(i).get("search_path1");// 搜索路径1
			String search_path2 = listProduct.get(i).get("search_path2");// 搜索路径2
			String file_name1 = listProduct.get(i).get("file_name1");
			String file_name2 = listProduct.get(i).get("file_name2");
			String file_name3 = listProduct.get(i).get("file_name3");
			String file_name4 = listProduct.get(i).get("file_name4");
			String file_name5 = listProduct.get(i).get("file_name5");
			String file_name6 = listProduct.get(i).get("file_name6");
			String description_file = listProduct.get(i)
					.get("description_file");
			// 获取ftp信息
			System.out.println("ftp_id:" + ftp_id);
			List<Record> ftpList = ServiceUtil.dao.getFtpById(ftp_id);
			String ftp_path = "";// 路径
			int ftp_port = 0;// 端口
			String ftp_address = "";// 地址
			String ftp_username = "";// 帐号
			String ftp_password = "";// 密码
			for (int q = 0; q < ftpList.size(); q++) {
				ftp_path = ftpList.get(q).get("ftp_path");
				ftp_port = ftpList.get(q).get("ftp_port");
				ftp_address = ftpList.get(q).get("ftp_address");
				ftp_username = ftpList.get(q).get("ftp_username");
				ftp_password = ftpList.get(q).get("ftp_password");
			}
			System.out.println("地址:" + ftp_address + "端口：" + ftp_port + "帐号："
					+ ftp_username + "密码：" + ftp_password);
			// 判断固定、解析
			if (standard_type.equals("2")) {
				// 获取全路径信息
				list = directoryName(ftp_id, work_directory_id, distinguish,
						standard_type, analytical_rule, search_path1,
						search_path2, file_name1, file_name2, file_name3,
						file_name4, file_name5, file_name6, description_file);
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
					boolean yn;
					try {
						yn = FileManagerModel.dao.checkDescExist(fielName);
					} catch (Exception e1) {
						e1.printStackTrace();
						responseBody.setStatus(1);
						responseBody.setMes("查询" + fielName + "文件失败");
						return responseBody;
					}
					if (!yn) {
						// 下载Desc
						int rt = 0;
							try {
								rt = new FTPClientFactory(ftp_address, ftp_port,
										ftp_username, ftp_password).downLoadFile(
												ftp_path+"/", fielName, directoryName);
							} catch (Exception e) {
								e.printStackTrace();
								responseBody.setStatus(1);
								responseBody.setMes("IP为:" + ftp_address + "的FTP连接失败");
								return responseBody;
							}
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
									// 下载txt
										try {
											if (new FTPClientFactory(ftp_address,
													ftp_port, ftp_username,
													ftp_password).downLoadFile(ftp_path+"/",
															txtName, directoryName) == 0) {
												try {
													FileManagerModel.dao
															.updataFileStatus(
																	txtName,
																	FileStatus.FAIL);
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
											responseBody.setMes("IP为:" + ftp_address + "的FTP连接失败");
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
							System.out.println("下载失败!!!!!!");
							// 下载失败
							responseBody.setStatus(1);
							responseBody.setMes("下载DESC文件失败:"+fielName);
							return responseBody;
						}
					}
				}
			}else{
				//固定式
				boolean yn;
				try {
					yn = FileManagerModel.dao.checkDescExist(file_name);
				} catch (Exception e) {
					e.printStackTrace();
					responseBody.setStatus(1);
					responseBody.setMes(e.getClass().getName());
					return responseBody;
				}
				if (!yn) {
					// 下载Desc
					int rt = 0;
					try {
						rt = new FTPClientFactory(ftp_address, ftp_port,
								ftp_username, ftp_password).downLoadFile(
										ftp_path, file_name, search_path);
					} catch (Exception e) {
						e.printStackTrace();
						responseBody.setStatus(1);
						responseBody.setMes(e.getClass().getName());
						return responseBody;
					}
					if (rt == 1) {
						try {
							//获取desc中的txt
							listTxt = translate4desc.execute(search_path+file_name);
						} catch (Exception e) {
							e.printStackTrace();
							responseBody.setStatus(1);
							responseBody.setMes(e.getClass().getName());
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
							} catch (Exception e) {
								e.printStackTrace();
								responseBody.setStatus(1);
								responseBody.setMes(e.getClass().getName());
								return responseBody;
							}
							if (up >= 1) {
								// 下载txt
								try {
									if (new FTPClientFactory(ftp_address,
											ftp_port, ftp_username,
											ftp_password).downLoadFile(ftp_path,
													txtName, search_path) == 0) {
										try {
											FileManagerModel.dao
													.updataFileStatus(
															txtName,
															FileStatus.FAIL);
										} catch (Exception e) {
											e.printStackTrace();
											responseBody.setStatus(1);
											responseBody.setMes(e.getClass().getName());
											return responseBody;
										}
									} else {
										//下载完成更新状态
										FileManagerModel.dao.updataFileStatus(
												txtName, FileStatus.FINISH);
									}
								} catch (Exception e) {
									e.printStackTrace();
									responseBody.setStatus(1);
									responseBody.setMes(e.getClass().getName());
									return responseBody;
								}
							} else {
								try {
									FileManagerModel.dao.updataFileStatus(
											txtName, FileStatus.FAIL);
								} catch (Exception e) {
									e.printStackTrace();
									responseBody.setStatus(1);
									responseBody.setMes(e.getClass().getName());
									return responseBody;
								}
							}
						}
					} else if (rt == 0) {
						System.out.println("下载失败!!!!!!");
						// 下载失败
						responseBody.setStatus(1);
						responseBody.setMes("下载DESC文件失败:"+file_name);
						return responseBody;
					}
				}
			}
		}
		responseBody.setStatus(0);
		responseBody.setMes("下载成功"+ new Date());
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
			int analytical_rule, String search_path1, String search_path2,
			String file_name1, String file_name2, String file_name3,
			String file_name4, String file_name5, String file_name6,
			String description_file) {
		ArrayList<String> listWorkDirectory = new ArrayList<String>();
		ArrayList<String> list1 = new ArrayList<String>();
		Date date = new Date();
		// 获取日期
		listWorkDirectory = date(file_name4, date, analytical_rule);
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
