package com.eova.metadata;


/**
 * 自定义元数据拦截器
 * 
 * @author jaker
 * @date 2019-4-22
 */
import com.eova.aop.AopContext;
import com.eova.common.utils.xx;
import com.eova.template.single.SingleIntercept;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.yonyou.util.UUID;

public class MetadataIntercept extends SingleIntercept {

	@Override
	public void importBefore(AopContext ac) throws Exception {
		//新增默认主表描述 返回id到子表 
		String mid = UUID.getUnqionPk();
		insertMetadata(mid);
		for (Record record : ac.records) {
			//处理主键 更换主键
			record.set("id", UUID.getUnqionPk());
			record.set("pid", mid);
		}
	}

	public String insertMetadata(String id) {
		StringBuilder sb = new StringBuilder();
		sb.append("system_").append(System.currentTimeMillis());
		String sql = " INSERT INTO  bs_metadata set  id= '"+id+"' ,data_code='"+sb.toString()+"', data_name='"+sb.toString()+"',code= '"+sb.toString()+"' ,dr=0;";
		Db.use(xx.DS_EOVA).update(sql);
		return id;
	}
}