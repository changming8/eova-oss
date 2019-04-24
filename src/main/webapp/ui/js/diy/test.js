$(document).ready(function() {
	// debugger;
	// this;
	// var para = window.location.search.split("=")[1];
	// $("input[name='flow_id']").val("dsfsd");
	// $('#flow_id').val("dsfsd");
	// console.log($('#flow_id').val());
});
window.onload = function() {
	var para = window.location.search.split("=")[1];
	if ("undefined" == typeof para)
		return;
	$("input[name='flow_id']").val(para);
	$("input[name='flow_id']").mask()
	// $('#flow_id').val("para");

}