package com.yonyou.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author changjr
 *
 */
public class LockUtils {
//	存放锁状态
	private static ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();

	/*
	 * @Description:获取PK锁 成功返回true 失败throw Exception
	 * 
	 * @param pk
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public synchronized static boolean pkLock(String pk) throws Exception {
		boolean flag = checkLockExist(pk);
		if (flag) {
			throw new Exception("锁以存在!");
		}
		lockMap.put(pk, "");
		return true;
	}

	/**
	 * 检查pk锁是否存在
	 */
	public synchronized static boolean checkLockExist(String pk) {

		return lockMap.containsKey(pk);

	}
	/*
	 * @Description:释放PK锁 成功返回true 失败throw Exception
	 * 
	 * @param pk
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public synchronized static boolean unpkLock(String pk) throws Exception {
		if (!checkLockExist(pk)) {
			throw new Exception("释放的锁不存在!");
		}
		lockMap.remove(pk);
		return true;

	}
}
