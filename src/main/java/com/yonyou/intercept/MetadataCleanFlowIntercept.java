package com.yonyou.intercept;

import java.util.List;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.jfinal.plugin.activerecord.Record;
import com.oss.model.MetadataDetail;
import com.yonyou.model.CleanBody;
import com.yonyou.util.UUID;

/**
 * 数据清洗新增拦截器,处理保存元数据后自动保存子表中有引用关系的字段
 * 
 * @author jaker
 *
 */
public class MetadataCleanFlowIntercept extends MetaObjectIntercept {
	
	protected MetadataDetail metadataDetail = new MetadataDetail();
	protected CleanBody cleanBody = new CleanBody();
	@Override
	public String addSucceed(AopContext ac) throws Exception {
//		// 获取子表中  有引用关系的字段 保存到数据清洗子表中 
//		List<Record>  cleanBodyList =  metadataDetail.findMetadataBodLinkyById(ac.records.get(0).getStr("table_id"));
//		//保存数据清洗子表信息
//		for(int i=0;i<cleanBodyList.size();i++) {
//			cleanBodyList.get(i).set("id", UUID.getUnqionPk());
//			cleanBodyList.get(i).set("pid", ac.records.get(0).getStr("id"));
//		}
//		//批量保存
//		cleanBody.batchSave(cleanBodyList, 50);
		return super.addSucceed(ac);
	}
	
	@Override
	public String addBefore(AopContext ac) throws Exception {
		ac.record.set("id", UUID.getUnqionPk());
		return super.addBefore(ac);
	}

}
