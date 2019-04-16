package com.youyou.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExcuteClass {
	
	/**
	 * 通过反射调用指定类指定方法，重载的方法及构造函数参数数量必须不同
	 * @param excuteClassName 全类名 如：com.youyou.util.ExcuteClass
	 * @param argsValue 有参构造方法参数值数组，无参构造可直接传null
	 * @param methodName 指定方法名
	 * @param objValue 可变数量方法参数值
	 * @return 返回值
	 */
	public static Object excuet(String excuteClassName, Object[] argsValue,String methodName, Object ...objValue) {//Class<?>[] classes, 
		
        Object returnValue = null;
        Class<?> clazz = null;
        Object obj = null;
		try {
			clazz = Class.forName(excuteClassName);
			if ( argsValue == null ) {
				Object[] obje = {};
				argsValue = obje;
			}
			//获取到所有构造函数
	        Constructor<?>[] con=clazz.getDeclaredConstructors();
	        for (int i = 0; i < con.length; i++) {
	        	//构造函数参数数量做为判断依据
				if ( argsValue.length == con[i].getParameterCount() ) {
					obj = con[i].newInstance( argsValue );
					Method[]  methods = clazz.getMethods();
					for (int j = 0; j < methods.length; j++) {
						if (methods[j].getName().equals(methodName) && methods[j].getParameterCount()==objValue.length) {
							Method meth=clazz.getMethod( methodName, methods[j].getParameterTypes());
							returnValue = meth.invoke(obj, objValue);
							break;
						}
					}
					break;
				}
			}
		} catch (InstantiationException | IllegalAccessException e2) {
			e2.printStackTrace();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return returnValue;
	}
}
