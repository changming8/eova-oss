package com.yonyou.util;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * 
 * @author changjr
 *
 */
/**
 * @version:（版本，具体版本信息自己来定）
 * @Description: （对类进行功能描述）
 * @author: changjr
 * @date: datedate{time}
 */
public class LockUtils {
	/*
	 * @Description:获取PK锁 成功返回true 失败false
	 * 
	 * @param pk
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public synchronized static boolean pkLock(String pk) {

		boolean flag = checkLockExist(pk);
		if (flag) {
			return false;
		}
		Redis.use("eova").set(pk, "");
		return true;

	}

	/**
	 * 检查pk锁是否存在
	 */
	public synchronized static boolean checkLockExist(String pk) {

		return Redis.use("eova").exists(pk);

	}

	/*
	 * @Description:释放PK锁 成功返回true 失败false
	 * 
	 * @param pk
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public synchronized static boolean unpkLock(String pk) {
		if (!checkLockExist(pk)) {
			return false;
		}
		Redis.use("eova").del(pk);
		return true;

	}

	/*
	 * @Description: 批量获取表锁
	 * 
	 * @param tableNames
	 * 
	 * @return 全部成功返回true 否则返回false
	 */
	public synchronized static boolean lockTable(List<String> tableNames) {
		for (String table : tableNames) {
			if (checkLockExist(table)) {
				return false;
			}
		}
		for (String table : tableNames) {
			Redis.use("eova").set(table, "");
		}
		return true;

	}

	/*
	 * @Description: 批量解锁 删除给定的一个 key 不存在的 key 会被忽略。
	 * 
	 * @param tableNames
	 * 
	 * @return
	 */
	public synchronized static boolean unLockTable(List<String> tableNames) {
		for (String table : tableNames) {
			Redis.use("eova").del(table);
		}
		return true;
		
	}
}
