$(document).ready(function() {
	var $linkfield_code = $('#linkfield_code');
	var $table_id = $('#table_id');
	var $mdd_code = $('#mdd_code');
	
	// 初始禁用
	$linkfield_code.mask();
	$mdd_code.eovafind({
		onChange : function(oldValue, newValue) {
			if (newValue == "") {
				$linkfield_code.mask();
				return;
		    }
			$linkfield_code.unmask();
			//初始化 
			var mdd_id = $("input[name=mdd_code]").val();
			$linkfield_code.eovafind({
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
	
//	dest_code.eovafind({
//		onChange : function(oldValue, newValue) {
//			// $link_column.eovafind().setValue("");
//			var btable_name = $('#dest_code input[type=text]').val();
//			// 调用后台重新查询 赋予值
//			var zcode = $('#dest_code input[name=dest_code]').val();
//			console.log('子表引用表:' + zcode);
//			$.ajax({
//				url : "/mddef/queryDataFlowById/" + btable_name + "-" + zcode,
//				type : "get",
//				dataType : "json",
//				success : function(data) {
//					$("#dest_id input[name=dest_id]").val(data[0].bid);
//					$("#mdd_id input[name=mdd_id]").val(data[0].mdd_code);
//					$("#mdd_id").attr("value",data[0].id);
//					$("input[name=dest_code]").val(btable_name);
//				}
//			})
//		}
//	});
});