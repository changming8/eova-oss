package com.yonyou.util;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.eova.common.utils.xx;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.yonyou.base.LockObject;

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
	public synchronized static boolean pkLock(String pk,String detail) {

		boolean flag = checkLockExist(pk);
		if (flag) {
			return false;
		}
		LockObject obj =new LockObject();
		obj.setDetail(detail);
		obj.setLockDate(DateUtil.findSystemDateString());
//		Redis.use("PKLOCK").set(pk, obj);
		Redis.use(xx.DS_EOVA).hset("PKLOCK", pk, obj);
		return true;

	}

	/**
	 * 检查pk锁是否存在
	 */
	public synchronized static boolean checkLockExist(String pk) {

//		return Redis.use("PKLOCK").exists(pk);
		return Redis.use(xx.DS_EOVA).hexists("PKLOCK", pk);

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
//		Redis.use("PKLOCK").del(pk);
		Redis.use(xx.DS_EOVA).hdel("PKLOCK", pk);
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
			if (checktableLockExist(table)) {
				return false;
			}
		}
		for (String table : tableNames) {
			Redis.use(xx.DS_EOVA).set(table, "");
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
			Redis.use(xx.DS_EOVA).del(table);
		}
		return true;
		
	}
	/**
	 * 检查pk锁是否存在
	 */
	public synchronized static boolean checktableLockExist(String table) {

		return Redis.use(xx.DS_EOVA).exists(table);

	}
}
