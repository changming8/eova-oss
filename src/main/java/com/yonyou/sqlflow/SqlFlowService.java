package com.yonyou.sqlflow;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.base.ResponseBody;

/**
 * 自由sql执行类，可调用存储过程
 * @author changming
 */
public class SqlFlowService {
	
	/**
	 * 自由sql执行方法
	 * @param id 流id
	 * @return ResponseBody
	 */
	public ResponseBody sqlExcute( String id ) {
		
		List<Record> listProduct = Db.use(xx.DS_EOVA).find("select * from bs_sql_flow where id ='" + id + "'");
		
		StringBuffer sql = new StringBuffer();
		String sql_context1 = listProduct.get(0).getStr("sql_context1");
		if ( sql_context1 == null || "".equals(sql_context1)) 
			return getResponseBody("执行sql 为空，任务终止！",id,1,1);
		String sql_context2 = listProduct.get(0).getStr("sql_context2");
		if ( sql_context2 == null || "".equals(sql_context2)) 
			sql.append(sql_context1);
		else
			sql.append(sql_context1).append(sql_context2);
		
		String sql_type = listProduct.get(0).getStr("sql_type");
		if ( sql_type == null || "".equals(sql_type)) 
			return getResponseBody("sql类型 为空，任务终止！",id,1,1);
		Connection conn = null;
		try {
			conn = Db.use( xx.DS_MAIN).getConfig().getConnection();
			excuteSql( sql.toString(), sql_type, conn );
		} catch (SQLException e) {
			return getResponseBody("sql脚本执行异常，任务终止！",id,1,1);
		} finally {
			try {
				if ( conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}
		return getResponseBody("自由sql执行成功！",id,1,1);
	}
	
	/**
	 * 构建ResponseBody
	 * @param mes 消息内容
	 * @param Objectid 数据主键
	 * @param status 执行状态
	 * @param objecttype 功能类型
	 * @return
	 */
	private static ResponseBody getResponseBody(String mes, String Objectid, int status, int objecttype ) {
		ResponseBody spbody = new ResponseBody();
		spbody.setStatus(status);
		spbody.setMes(mes);
		spbody.setObjectid(Objectid);
		spbody.setObjecttype(objecttype);
		return spbody;
	}
	
	/**
	 * 
	 * @param scripts 存储过程调用 scripts传入"proc()";
	 * @param type
	 * @param conn
	 * @throws SQLException
	 */
	public static void excuteSql(String scripts, String type, Connection conn) throws SQLException{
		
		if  ( type.equals("1")) {
			PreparedStatement pstm = null;
			try {
				pstm = conn.prepareStatement(scripts);
				pstm.execute();
			} catch (SQLException e) {
				e.printStackTrace();
				throw e;
			} finally {
				if ( pstm != null)
					pstm.close(); 
			}
		} else if ( type.equals("2")) {
			CallableStatement cstm = null;
			scripts = "{CALL "+scripts+"}"; //调用存储过程 
			try {
				cstm = conn.prepareCall(scripts); //实例化对象cstm 
				cstm.execute(); // 执行存储过程 
			} catch (SQLException e) {
				e.printStackTrace();
				throw e;
			} finally {
				if ( cstm != null)
					cstm.close(); 
			}
		}
	}
}
