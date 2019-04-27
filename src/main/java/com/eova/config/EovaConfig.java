
package com.eova.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.beetl.core.GroupTemplate;
import org.beetl.ext.jfinal3.JFinal3BeetlRenderFactory;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.wall.WallFilter;
import com.eova.aop.MetaObjectIntercept;
import com.eova.aop.eova.EovaIntercept;
import com.eova.cloud.AuthCloud;
import com.eova.common.plugin.quartz.QuartzPlugin;
import com.eova.common.utils.xx;
import com.eova.core.IndexController;
import com.eova.core.auth.AuthController;
import com.eova.core.button.ButtonController;
import com.eova.core.dict.DictController;
import com.eova.core.menu.MenuController;
import com.eova.core.meta.MetaController;
import com.eova.core.task.TaskController;
import com.eova.core.type.Convertor;
import com.eova.core.type.MysqlConvertor;
import com.eova.core.type.OracleConvertor;
import com.eova.core.type.SqlServerConvertor;
import com.eova.ext.jfinal.EovaDbPro;
import com.eova.ext.jfinal.EovaOracleDialect;
import com.eova.handler.UrlBanHandler;
import com.eova.interceptor.AuthInterceptor;
import com.eova.interceptor.LoginInterceptor;
import com.eova.model.Button;
import com.eova.model.EovaLog;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.model.Role;
import com.eova.model.RoleBtn;
import com.eova.model.Task;
import com.eova.model.User;
import com.eova.model.Widget;
import com.eova.service.ServiceManager;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.masterslave.MasterSlaveController;
import com.eova.template.office.OfficeController;
import com.eova.template.single.SingleController;
import com.eova.template.singlechart.SingleChartController;
import com.eova.template.singletree.SingleTreeController;
import com.eova.template.treetogrid.TreeToGridController;
import com.eova.widget.WidgetController;
import com.eova.widget.form.FormController;
import com.eova.widget.grid.GridController;
import com.eova.widget.tree.TreeController;
import com.eova.widget.treegrid.TreeGridController;
import com.eova.widget.upload.UploadController;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.config.Routes.Route;
import com.jfinal.json.MixedJsonFactory;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import com.jfinal.plugin.activerecord.IDbProFactory;
import com.jfinal.plugin.activerecord.dialect.AnsiSqlDialect;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
import com.jfinal.plugin.activerecord.dialect.SqlServerDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.druid.DruidStatViewHandler;
import com.jfinal.plugin.druid.IDruidStatViewAuth;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.template.Engine;
import com.oss.model.Address;
import com.oss.model.Orders;
import com.oss.model.UserInfo;
import com.oss.model.Users;
import com.yonyou.controller.MetadataController;

public class EovaConfig extends JFinalConfig {

	public static boolean isDevMode = true;

	/** EOVA所在数据库的类型 **/
	public static String EOVA_DBTYPE = "mysql";
	/** 数据库命名规则-是否自动小写 **/
	public static boolean isLowerCase = true;
	/** 数据类型转换器 **/
	public static Convertor convertor = new MysqlConvertor();

	/** Eova配置属性 **/
	protected static Map<String, String> props = new HashMap<String, String>();
	/** 数据源列表 **/
	protected static Set<String> dataSources = new HashSet<String>();
	/** Eova表达式集合 **/
	protected static Map<String, String> exps = new HashMap<String, String>();
	/** URI授权集合<角色ID,URI> **/
	protected static Map<Integer, Set<String>> authUris = new HashMap<Integer, Set<String>>();
	/** 字段角色授权<字段,角色集合> **/
	protected static Map<String, Set<String>> authFields = new HashMap<String, Set<String>>();
	/** ActiveRecord Map **/
	private static HashMap<String, ActiveRecordPlugin> arps = new HashMap<>();

	/**全局查询拦截器**/
	private static EovaIntercept eovaIntercept = null;
	/**默认元对象业务拦截器**/
	private static MetaObjectIntercept defaultMetaObjectIntercept = null;

	private long startTime = 0;

	private GroupTemplate gt = null;
	
	/**
	 * 系统启动之后
	 */
	@Override
	public void afterJFinalStart() {
		// Load Cost Time
		xx.costTime(startTime);

		EovaInit.initSource();

		{
			Boolean isInit = xx.getConfigBool("initPlugins", true);
			if (isInit) {
				EovaInit.initPlugins();
			}
		}
		{
			Boolean isInit = xx.getConfigBool("initSql", false);
			if (isInit && EOVA_DBTYPE.equals(JdbcUtils.MYSQL)) {
				EovaInit.initCreateSql();
			}
		}

		// 初始化配置Eova业务
		configEova();
	}

	/**
	 * 系统停止之前
	 */
	@Override
	public void beforeJFinalStop() {
	}

	/**
	 * 配置常量
	 */
	@Override
	public void configConstant(Constants me) {
		startTime = System.currentTimeMillis();

		System.out.println("Starting Eova 1.6 -> The most easy development platform");

		System.err.println("Config Constants Starting...");
		me.setEncoding("UTF-8");
		// 初始化配置
		EovaInit.initConfig(props);
		// 开发模式
		isDevMode = xx.getConfigBool("devMode", true);
		me.setDevMode(isDevMode);
		// POST内容最大500M(安装包上传)
		me.setMaxPostSize(1024 * 1024 * 500);
		me.setError500View("/eova/500.html");
		me.setError404View("/eova/404.html");

		// 设置静态根目录为上传根目录
		me.setBaseUploadPath(xx.getConfig("static_root"));
		// 注册JSON工厂
		me.setJsonFactory(MixedJsonFactory.me());
		me.setJsonDatePattern("yyyy-MM-dd");

		// 设置主视图为Beetl
		JFinal3BeetlRenderFactory rf = new JFinal3BeetlRenderFactory();
		rf.config();
		me.setRenderFactory(rf);
		gt = rf.groupTemplate;

		// 初始化配置
		exp();// Eova表达式
		authUri();// URI授权
		authField();// 字段角色授权
	}

	/**
	 * 配置路由
	 */
	@Override
	public void configRoute(Routes me) {
		System.err.println("Config Routes Starting...");

		// 业务模版
		me.add("/" + TemplateConfig.SINGLE_GRID, SingleController.class);
		me.add("/" + TemplateConfig.SINGLE_TREE, SingleTreeController.class);
		me.add("/" + TemplateConfig.SINGLE_CHART, SingleChartController.class);
		me.add("/" + TemplateConfig.MASTER_SLAVE_GRID, MasterSlaveController.class);
		me.add("/" + TemplateConfig.TREE_GRID, TreeToGridController.class);
		me.add("/" + TemplateConfig.OFFICE, OfficeController.class);
		// 组件
		me.add("/widget", WidgetController.class);
		me.add("/upload", UploadController.class);
		me.add("/form", FormController.class);
		me.add("/grid", GridController.class);
		me.add("/tree", TreeController.class);
		me.add("/treegrid", TreeGridController.class);
		// Eova业务
		me.add("/meta", MetaController.class);
		me.add("/menu", MenuController.class);
		me.add("/button", ButtonController.class);
		me.add("/auth", AuthController.class);
		me.add("/task", TaskController.class);
		me.add("/dict", DictController.class);
		me.add("/metadata", MetadataController.class);
		// LoginInterceptor.excludes.add("/cloud");

		// 自定义路由
		route(me);

		// 如果有自定义，将不再注册系统默认实现
		boolean flag = false;
		for (Route x : me.getRouteItemList()) {
			if (x.getControllerKey().equals("/")) {
				flag = true;
			}
		}
		if (!flag)
			me.add("/", IndexController.class);
	}

	@Override
	public void configEngine(Engine me) {
		// me.addSharedFunction("/WEB-INF/_layout/pager.html");
		
		// 设置全局变量
		final String STATIC = props.get("domain_static");
		final String CDN = props.get("domain_cdn");
		final String IMG = props.get("domain_img");
		final String FILE = props.get("domain_file");

		Map<String, Object> sharedVars = new HashMap<String, Object>();
		if (!xx.isEmpty(STATIC))
			sharedVars.put("STATIC", STATIC);
		else
			sharedVars.put("STATIC", "");
		if (!xx.isEmpty(CDN))
			sharedVars.put("CDN", CDN);
		if (!xx.isEmpty(IMG))
			sharedVars.put("IMG", IMG);
		if (!xx.isEmpty(FILE))
			sharedVars.put("FILE", FILE);

		sharedVars.put("APP", AuthCloud.getEovaApp());
		// sharedVars.put("I18N", I18NBuilder.I18N);

		gt.setSharedVars(sharedVars);
		
		// Load Template Const
		PageConst.init(sharedVars);
	}

	/**
	 * 配置插件
	 */
	@Override
	public void configPlugin(Plugins plugins) {
		System.err.println("Config Plugins Starting...");

		// 注册数据源
		regDataSource(plugins);

		// 配置数据转换器
		switch (EOVA_DBTYPE) {
		case JdbcUtils.MYSQL:
			convertor = new MysqlConvertor();
			break;
		case JdbcUtils.ORACLE:
			convertor = new OracleConvertor();
			break;
		case JdbcUtils.SQL_SERVER:
			convertor = new SqlServerConvertor();
			break;
		}

		// 自定义插件
		plugin(plugins);

		// eova model mapping
		mappingEova(arps.get(xx.DS_EOVA));

		// diy model mapping
		mapping(arps);

		// 初始化ServiceManager
		ServiceManager.init();

		// 配置EhCachePlugin插件
		plugins.add(new EhCachePlugin());

		// 配置Quartz
		boolean isQuartz = xx.getConfigBool("isQuartz", true);
		if (isQuartz) {
			QuartzPlugin quartz = new QuartzPlugin();
			plugins.add(quartz);
		}
	}

	/**
	 * 注册数据源(支持多数据源)
	 * @param plugins
	 */
	private void regDataSource(Plugins plugins) {
		// 多数据源支持
		String datasource = xx.getConfig("db.datasource");
		if (xx.isEmpty(datasource)) {
			throw new RuntimeException("数据源配置项不存在,请检查配置/default/jdbc.config 配置项[db.datasource]");
		}
		for (String ds : datasource.split(",")) {
			ds = ds.trim();

			String url = xx.getConfig(ds + ".url");
			String user = xx.getConfig(ds + ".user");
			String pwd = xx.getConfig(ds + ".pwd");

			DruidPlugin dp = initDruidPlugin(url, user, pwd);
			ActiveRecordPlugin arp = initActiveRecordPlugin(url, ds, dp);
			LogKit.info("load data source:" + url + " > " + user);

			dataSources.add(ds);
			arps.put(ds, arp);
			plugins.add(dp).add(arp);
		}
	}

	/**
	 * 配置全局拦截器
	 */
	@Override
	public void configInterceptor(Interceptors me) {
		System.err.println("Config Interceptors Starting...");
		// JFinal.me().getServletContext().setAttribute("EOVA", "简单才是高科技");
		// 登录验证
		me.add(new LoginInterceptor());
		// 权限验证拦截
		me.add(new AuthInterceptor());
	}

	/**配置Eova业务**/
	public void configEova() {
	}

	/**
	 * 配置处理器
	 */
	@Override
	public void configHandler(Handlers me) {
		System.err.println("Config Handlers Starting...");
		// 添加DruidHandler
		DruidStatViewHandler dvh = new DruidStatViewHandler("/druid", new IDruidStatViewAuth() {
			public boolean isPermitted(HttpServletRequest request) {
				User user = (User) request.getSession().getAttribute(EovaConst.USER);
				if (user == null) {
					return false;
				}
				return user.isAdmin();
			}
		});
		me.add(dvh);
		// 过滤禁止访问资源
		me.add(new UrlBanHandler(".*\\.(html|tag)", false));
	}

	/**
	 * Eova Data Source Model Mapping
	 *
	 * @param arp
	 */
	private void mappingEova(ActiveRecordPlugin arp) {
		arp.addMapping("eova_object", MetaObject.class);
		arp.addMapping("eova_field", MetaField.class);
		arp.addMapping("eova_button", Button.class);
		arp.addMapping("eova_menu", Menu.class);
		arp.addMapping("eova_user", User.class);
		arp.addMapping("eova_role", Role.class);
		arp.addMapping("eova_role_btn", RoleBtn.class);
		arp.addMapping("eova_log", EovaLog.class);
		arp.addMapping("eova_task", Task.class);
		arp.addMapping("eova_widget", Widget.class);
		//
		arp.addMapping("user_info", UserInfo.class);
		arp.addMapping("users", Users.class);
		arp.addMapping("address", Address.class);
		arp.addMapping("orders", Orders.class);
	}

	/**
	 * Diy Data Source Model Mapping
	 * @param arps 数据源key->ActiveRecordPlugin
	 */
	protected void mapping(HashMap<String, ActiveRecordPlugin> arps) {
	}

	/**
	 * Custom Route
	 *
	 * @param me
	 */
	protected void route(Routes me) {
	}

	/**
	 * Custom Plugin
	 *
	 * @param plugins
	 * @return
	 */
	protected void plugin(Plugins plugins) {
	}

	/**
	 * init Druid
	 *
	 * @param url JDBC
	 * @param username 数据库用户
	 * @param password 数据库密码
	 * @return
	 */
	protected DruidPlugin initDruidPlugin(String url, String username, String password) {
		// 设置方言
		WallFilter wall = new WallFilter();
		String dbType = null;
		try {
			dbType = JdbcUtils.getDbType(url, JdbcUtils.getDriverClassName(url));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		wall.setDbType(dbType);

		DruidPlugin dp = new DruidPlugin(url, username, password);
		dp.addFilter(new StatFilter());
//		dp.addFilter(wall);
		return dp;

	}

	/**
	 * init ActiveRecord
	 *
	 * @param url JDBC
	 * @param ds DataSource
	 * @param dp Druid
	 * @return
	 */
	protected ActiveRecordPlugin initActiveRecordPlugin(String url, String ds, IDataSourceProvider dp) {
		// 提升事务级别保证事务回滚
		int lv = xx.getConfigInt("db.transaction_level", Connection.TRANSACTION_REPEATABLE_READ);
		// DB默认命名规则
		boolean isLowerCase = xx.getConfigBool("db.islowercase", true);
		// 是否输出SQL日志
		boolean isShowSql = xx.getConfigBool("db.showsql", true);

		ActiveRecordPlugin arp = new ActiveRecordPlugin(ds, dp);

		// 自定义Eova专用Db代理
		arp.setDbProFactory(new IDbProFactory() {
			public DbPro getDbPro(String configName) {
				return new EovaDbPro(configName);
			}
		});

		String dbType = "";
		// 获取DB类型
		try {
			dbType = JdbcUtils.getDbType(url, JdbcUtils.getDriverClassName(url));
			if (ds.equalsIgnoreCase(xx.DS_EOVA)) {
				// Eova的数据库类型
				EOVA_DBTYPE = dbType;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		// 方言选择
		Dialect dialect;
		if (JdbcUtils.MYSQL.equalsIgnoreCase(dbType) || JdbcUtils.H2.equalsIgnoreCase(dbType)) {
			dialect = new MysqlDialect();
		} else if (JdbcUtils.ORACLE.equalsIgnoreCase(dbType)) {
			dialect = new EovaOracleDialect();
			((DruidPlugin) dp).setValidationQuery("select 1 FROM DUAL");
		} else if (JdbcUtils.POSTGRESQL.equalsIgnoreCase(dbType)) {
			dialect = new PostgreSqlDialect();
		} else if (JdbcUtils.SQL_SERVER.equalsIgnoreCase(dbType)) {
			dialect = new SqlServerDialect();
		} else {
			// 默认使用标准SQL方言
			dialect = new AnsiSqlDialect();
		}

		arp.setTransactionLevel(lv);
		arp.setContainerFactory(new CaseInsensitiveContainerFactory(isLowerCase));
		arp.setShowSql(isShowSql);
		arp.setDialect(dialect);

		return arp;
	}

	/**
	 * Eova Expression Mapping
	 *
	 * @param exps
	 */
	protected void exp() {
		// Eova 系统功能需要的Exp
		exps.put("selectEovaFieldByObjectCode", "select en Field,cn Name from eova_field where object_code = ?;ds=eova");
		exps.put("selectEovaRole", "select id id,name cn from eova_role;ds=eova");
	}

	/**
	 * URI授权配置
	 */
	protected void authUri() {

		// 公有授权白名单
		HashSet<String> uris = new HashSet<String>();
		uris.add("/widget/**");
		uris.add("/upload/**");
		uris.add("/meta/object/**");
		uris.add("/meta/fields/**");
		authUris.put(0, uris);

	}

	/**
	 * Field授权配置
	 */
	protected void authField() {
		// 系统角色字段授权
		addAuthField("eova_role_code.lv->1,2");// 解释:eova_role_code对象的lv字段 只有角色1和角色2 可见
	}

	/**
	 * 添加字段授权规则<br>
	 * 语法:元对象编码.元字段英文名->角色1ID,角色2ID
	 * 
	 * @param auth
	 */
	protected static void addAuthField(String auth) {
		String[] ss = auth.split("->");
		String key = ss[0];
		String b = ss[1];

		Set<String> set = authFields.get(key);
		if (set == null) {
			set = new HashSet<>();
		}
		set.addAll(Arrays.asList(b.split(",")));
		authFields.put(key, set);
	}

	public static EovaIntercept getEovaIntercept() {
		return eovaIntercept;
	}

	public static void setEovaIntercept(EovaIntercept eovaIntercept) {
		EovaConfig.eovaIntercept = eovaIntercept;
	}

	public static MetaObjectIntercept getDefaultMetaObjectIntercept() {
		return defaultMetaObjectIntercept;
	}

	public static void setDefaultMetaObjectIntercept(MetaObjectIntercept defaultMetaObjectIntercept) {
		EovaConfig.defaultMetaObjectIntercept = defaultMetaObjectIntercept;
	}

	public static Map<String, String> getProps() {
		return props;
	}

	public static Set<String> getDataSources() {
		return dataSources;
	}

	public static Map<String, String> getExps() {
		return exps;
	}

	public static Map<Integer, Set<String>> getAuthUris() {
		return authUris;
	}

	public static Map<String, Set<String>> getAuthFields() {
		return authFields;
	}

}