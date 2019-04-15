package com.yonyou.util;

public class UUID {
	private static int flowNum = 000;

	public static String getUnqionPk() {
		if (flowNum > 999) {
			flowNum = 0;
		}
		String flow = String.format("%02d", flowNum++);
		String id = DateUtil.getNowTimestamp() + flow;
		return id;

	}
//
//	public static void main(String[] args) {
//		System.out.println(UUID.getUnqionPk());
//	}

}
