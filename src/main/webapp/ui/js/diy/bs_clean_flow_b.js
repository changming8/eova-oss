var business_tabl_id; // 业务表主键
var link_table;// 连接表
$(document).ready(function() {
	var destfield_code = $('#destfield_code');
	var mdfield_code = $('#mdfield_code');
	
	
	var id = $('input[name=pid]').val();// 主表id 来获取业务表和目标映射表 重置下啦字段
	
	$.ajax({
		url : "/dataClean/queryDataCleanById/" + id,
		type : "get",
		dataType : "json",
		success : function(data) {
			// 初始化 业务表参照查询 
			destfield_code.eovafind({ exp : 'bs_clean_bus_table_column_ref,' + data[0].table_id +","+id});
			//初始化 目标表字段参照查询
			mdfield_code.eovafind({ exp : 'bs_clean_link_table_column_ref,' + data[0].linkfield_id+","+id });
		}
	});
	
	destfield_code.eovafind({
		onChange : function(oldValue, newValue) {
			var column = $('#destfield_code input[type=text]').val();
			$("input[name=destfield_code]").val(column);
		}
	});
	mdfield_code.eovafind({
		onChange : function(oldValue, newValue) {
			var column = $('#mdfield_code input[type=text]').val();
			$("input[name=mdfield_code]").val(column);
		}
	});
});