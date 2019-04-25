$(document).ready(function() {
	// debugger;
	// this;
	// var para = window.location.search.split("=")[1];
	// $("input[name='flow_id']").val("dsfsd");
	// $('#flow_id').val("dsfsd");
	// console.log($('#flow_id').val());
});
window.onload = function() {
	debugger;
	var para = window.location.search.split("=")[1];
	if ("undefined" == typeof para)
		return;
	$("input[name='flow_id']").val(para);
	$("input[name='flow_id']").mask();
	// $('#flow_id').val("para");
	var $table_code = $('#table_code');
	$("#table_id").mask();
	$("#table_name").mask();
	$table_code.eovafind({
		onChange : function(oldValue, newValue) {
			$.syncPost('/FlowTableStatusDefController/multipleFind', {
					"code":newValue
			}, function(result, status) {
				debugger;
				if (result.success) {
					var res = result.data[0];
					$("input[name='table_id']").val(res.id);
					$("input[name='table_name']").val(res.data_name);
					$.slideMsg($.I18N('操作成功'));
				} else {
					$.alert($, result.msg);
				}
			});
		}
	});
}