$(document).ready(function() {
	var $linkfield_code = $('#linkfield_code');
	var $table_id = $('#table_id');
	var $mdd_code = $('#mdd_code');
	var $dest_table = $('#dest_table');
	
	// 初始禁用
	$linkfield_code.mask();
	$dest_table.mask();
	$mdd_code.eovafind({
		onChange : function(oldValue, newValue,rows) {
			if (newValue == "") {
				$linkfield_code.mask();
				return;
		    }
			$dest_table.unmask();
			//设置主数据名称
			$("input[name=mdd_name]").val(rows[0].mdd_name);
			//初始化
			var mdd_id = $("input[name=mdd_code]").val();
			$dest_table.eovafind({
				exp : 'bs_md_def_b_ref_a,'+mdd_id
			});
			
		}
	});
	
	
	//映射字段初始化 加载映射表的字段返回   bs_md_def_b_ref_a  
	
	$linkfield_code.eovafind({
		onChange : function(oldValue, newValue) {
			var	table = $("input[name=linkfield_code]").val();
			var column = $('#linkfield_code input[type=text]').val();
			$("input[name=linkfield_code]").val(column);
			$("input[name=linkfield_id]").val(table);
		}
	});
	
	/*****备份******/
	$dest_table.eovafind({
		onChange : function(oldValue, newValue,rows) {
			console.log("映射表:"+rows[0].desttable_code);
			$('#dest_table input[type=text]').val(rows[0].desttable_code);
			$("input[name=dest_column]").val(rows[0].destfield_code);
		}
	});
	/***********/
	
	$table_id.eovafind({
		onChange : function(oldValue, newValue,rows) {
			console.log(rows[0].table_name);
			$('#table_name').val(rows[0].table_name);
			$("input[name=table_name]").val(rows[0].table_name);
			$linkfield_code.unmask();
			//初始化
			var metadata_id = $("input[name=table_id]").val();
			$linkfield_code.eovafind({
				exp : 'bs_metadata_column_link_ref,'+metadata_id
			});
		}
	});
});