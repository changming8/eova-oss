$(document).ready(function(){
	var $flow_id=$("#flow_id");
	$flow_id.eovafind({
		onChange: function (oldValue, newValue) {
			$.ajax({
				url:"/dataRelationMaintenance/queryDataFlowById/"+newValue,
				type:"get",
				dataType:"json",
				success:function(data){
					$("input[name='flow_code']").val(data[0].flow_code);
					$("input[name='flow_name']").val(data[0].flow_name);
					$("input[name='flowtype_code']").val(data[0].flowtype_code);
					$("input[name='flowtype_id']").val(data[0].flowtype_id);
					$("input[name='flowtype_name']").val(data[0].flowtype_name);
				}
			})
		}
	})
	
})