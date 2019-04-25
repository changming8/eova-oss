$(document).ready(function() {
	var dest_code = $('#dest_code');
	var $mdd_id = $('#mdd_id');
	// 初始禁用
	// $mdd_id.mask();
	// $("#link_column ")
	var fid = $('input[name=field_id]').val();
	console.log('列主键:' + fid);
	dest_code.eovafind({
		exp : 'bs_md_def_b_ref,' + fid
	});

	dest_code.eovafind({
		onChange : function(oldValue, newValue) {
			// $link_column.eovafind().setValue("");
			var btable_name = $('#dest_code input[type=text]').val();
			// 调用后台重新查询 赋予值
			var zcode = $('#dest_code input[name=dest_code]').val();
			console.log('子表引用表:' + zcode);
			$.ajax({
				url : "/mddef/queryDataFlowById/" + btable_name + "-" + zcode,
				type : "get",
				dataType : "json",
				success : function(data) {
					$("#dest_id input[name=dest_id]").val(data[0].bid);
					$("#mdd_id input[name=mdd_id]").val(data[0].mdd_code);
					$("#mdd_id").attr("value",data[0].id);
					$("input[name=dest_code]").val(btable_name);
				}
			})
		}
	});
});