
package com.yonyou.config;

import java.util.HashMap;

import com.eova.common.utils.xx;
import com.eova.config.EovaConfig;
import com.eova.interceptor.LoginInterceptor;
import com.eova.user.UserController;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.oss.OSSController;
import com.oss.global.BaseMetaObjectIntercept;
import com.oss.global.GlobalEovaIntercept;
import com.oss.model.Address;
import com.oss.model.Metadata;
import com.oss.model.MetadataDetail;
import com.oss.model.Orders;
import com.oss.model.UserInfo;
import com.oss.model.Users;
import com.oss.product.ProductController;
import com.oss.test.TestController;
import com.yonyou.controller.DataRelationMaintenanceController;
import com.yonyou.controller.MdDEFController;
import com.yonyou.model.FileManagerModel;
import com.yonyou.quartz.controller.QuartzController;

/**
 * 甜橙金融 配置文件 后续将OSS演示文件 移除 暂时保留演示功能
 * 
 * @author changjr
 *
 */
public class OFConfig extends EovaConfig {

	/**
	 * 自定义路由
	 *
	 * @param me
	 */
	@Override
	protected void route(Routes me) {
		// 自定义的路由配置往这里加。。。
		me.add("/user", UserController.class);
//		me.add("/eovamain", UserController.class);
		me.add("/", OSSController.class);
		me.add("/test", TestController.class);
		me.add("/product", ProductController.class);
		me.add("/mddef", MdDEFController.class);
		me.add("/dataRelationMaintenance", DataRelationMaintenanceController.class);
		me.add("/flow", QuartzController.class);
		// 排除不需要登录拦截的URI 语法同SpringMVC拦截器配置 @see
		// com.eova.common.utils.util.AntPathMatcher
		LoginInterceptor.excludes.add("/test/**");

		LoginInterceptor.excludes.add("/init");
		LoginInterceptor.excludes.add("/code");
		// LoginInterceptor.excludes.add("/xxxx/**");

	}

	/**
	 * 自定义Main数据源Model映射
	 *
	 * @param arp
	 */
	@Override
	protected void mapping(HashMap<String, ActiveRecordPlugin> arps) {
		// 获取主数据源的ARP
		ActiveRecordPlugin main = arps.get(xx.DS_MAIN);
		// 自定义业务Model映射往这里加
		main.addMapping("user_info", UserInfo.class);
		main.addMapping("users", Users.class);
		main.addMapping("address", Address.class);
		main.addMapping("orders", Orders.class);
		
		main.addMapping("bs_filemanager", FileManagerModel.class);
		main.addMapping("bs_metadata", Metadata.class);
		main.addMapping("bs_metadata_b", MetadataDetail.class);
		// 获取其它数据源的ARP
		// ActiveRecordPlugin xxx = arps.get("xxx");
	}

	/**
	 * 自定义插件
	 */
	@Override
	protected void plugin(Plugins plugins) {
		// 添加需要的插件
	}

	/**
	 * 自定义表达式(主要用于级联)
	 */
	@Override
	protected void exp() {
		super.exp();
		// 区域级联查询
		exps.put("selectAreaByLv2AndPid", "select id ID,name CN from area where lv = 2 and pid = ?");
		exps.put("selectAreaByLv3AndPid", "select id ID,name CN from area where lv = 3 and pid = ?");
		exps.put("selectEovaMenu", "select id,parent_id pid, name, iconskip from eova_menu;ds=eova");
		exps.put("selectEovaMenu", "select id,parent_id pid, name, iconskip from eova_menu;ds=eova");
//		主数据列 参照联动 参照联动
		String sql = "select field_code 编码 ,field_name 名称 from bs_metadata_b where metadata_id =( select id from bs_metadata where data_code  =( select md_table from bs_md_def where id = ? )) and  (unique_constraint = 1 or  key_flag = 1)";
		exps.put("md_ref", sql);
		exps.put("md_dest_column_ref", "select  field_code 编码 ,field_name 名称  from bs_metadata_b where metadata_id = (select id from bs_metadata where data_code = ? ) and (unique_constraint = 1 or  key_flag = 1) ");
		exps.put("bs_metadata_column_ref", "select  field_code 编码 ,field_name 名称  from bs_metadata_b where metadata_id = (select id from bs_metadata where data_code = ? ) and (key_flag =1 or unique_constraint =1)");
		// 用法，级联动态在页面改变SQL和参数
		// $xxx.eovacombo({exp : 'selectAreaByLv2AndPid,aaa,10'}).reload();
		// $xxx.eovafind({exp : 'selectAreaByLv2AndPid,aaa,10'});
		// $xxx.eovatree({exp : 'selectAreaByLv2AndPid,10'}).reload();
	}

	@Override
	protected void authUri() {
		super.authUri();

		// 放行所有角色,所有URI(我是小白,我搞不明白URI配置,请使用这招,得了懒癌也可以这样搞后果自负.)
		// authUris.put(0, new HashSet<String>(){
		// {
		// add("/**/**");
		// }
		// });

		// 放行指定角色
		// authUris.put(角色ID, new HashSet<String>(){
		// {
		// add("/xxx/**");
		// URI配置语法咋么写?
		// @see AntPathMatcher
		// }
		// });

	}

	@Override
	public void configEova() {
		/*
		 * 自定义Eova全局拦截器 全局的查询拦截,可快速集中解决系统的查询数据权限,严谨,高效!
		 */
		setEovaIntercept(new GlobalEovaIntercept());
		/*
		 * 默认元对象业务拦截器:未配置元对象业务拦截器会默认命中此拦截器 自定义元对象拦截器时自行考虑是否需要继承默认拦截器
		 */
		setDefaultMetaObjectIntercept(new BaseMetaObjectIntercept());
	}

}