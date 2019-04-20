package com.yonyou.loadfiledata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.plugin.activerecord.Db;

/**
 * 通过数据文件load数据到Mysql
 * @author changming
 * @version v.0.1
 */
public class LoadFileData {
	
	private static Logger logger = LoggerFactory.getLogger(LoadFileData.class);

	/**
	  * 数据文件在数据库服务器上
	  * 通过mysql客户端命令 show variables like '%sceure%' 获取 secure_file_priv路径，数据文件必须在此路径下
	 * @param filePath 文件全路径（包含文件名）
	 * @param tableName 数据库表名
	 * @param 表列序列以","逗号分割 如 colum1,colum2,colum3,colum4,colum5
	 * @return 载入数据量
	 */
	public static int loadFile(String filePath, String tableName, String colums, Connection conn ) {
		
		int result = 0;
//		Connection con = null;
		PreparedStatement pstmt = null;
		com.mysql.jdbc.PreparedStatement mysqlStatement = null;
	   	try {
			String sql = "LOAD DATA INFILE '"+filePath+"' INTO TABLE "+tableName+" FIELDS TERMINATED BY '`' ENCLOSED BY '\"' "+"("+colums+")";
			pstmt = conn.prepareStatement(sql);
			if (pstmt.isWrapperFor(com.mysql.jdbc.Statement.class)) {
				mysqlStatement = pstmt.unwrap(com.mysql.jdbc.PreparedStatement.class);
				result = mysqlStatement.executeUpdate(); 
			}
	   	} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				mysqlStatement.close();
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	/**
	 * 数据文件在应用部署服务器上
	 * @param filePath 文件全路径（包含文件名）
	 * 如果是windows操作系统路径为"d:\\\\source\\\\aaaaaaa.txt"
	 * linux操作系统路径为"/ssource/aaaaaa.txt"
	 * @param tableName 数据库表名
	 * @param 表列序列以","逗号分割 如 colum1,colum2,colum3,colum4,colum5
	 * @return 载入数据量
	 */
	public static int loadLocalFile(String filePath, String tableName, String colums, Connection conn ) throws FileNotFoundException,SQLException {
		
		int result = 0;
		com.mysql.jdbc.PreparedStatement mysqlStatement = null;
		PreparedStatement statement = null;
		InputStream dataStream = null;
		String sql = "LOAD DATA LOCAL INFILE '"+filePath+"' INTO TABLE "+tableName+"("+colums+")";
		File file = new File(filePath);
		dataStream = new FileInputStream(file);
		statement = conn.prepareStatement(sql);
		if (statement.isWrapperFor(com.mysql.jdbc.Statement.class)) {
			mysqlStatement = statement.unwrap(com.mysql.jdbc.PreparedStatement.class);
			mysqlStatement.setLocalInfileInputStream(dataStream);
			result = mysqlStatement.executeUpdate(); 
		}
		mysqlStatement.close();
		statement.close();
		return result;
	}
}
