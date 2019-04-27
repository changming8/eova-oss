$(document).ready(function() {
	var $table_id=$("#table_id");
	$table_id.eovafind({
		onChange: function (oldValue, newValue) {
			$.ajax({
				url:"/statusFlow/queryMetadataById/"+newValue,
				type:"get",
				dataType:"json",
				success:function(data){
					console.info(data);
					$("#physical_table input").val(data[0].code);
				}
			})
		}
	})
	
});