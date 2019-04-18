$(document).ready(function(){
	var $link_table = $('#link_table');
    var $link_column= $('#link_column');
    // 初始禁用
    $link_column.mask();
 	//
    $link_table.eovafind({onChange: function (oldValue, newValue) {
    	$link_column.eovacombo().setValue("");
    	//newValue = $("input[name='link_table']")[0].value;
    	console.log('选中值:'+newValue);
    	if (newValue == "") {
    		$link_column.mask();
    		return;
        }
    	$link_column.unmask();
    	console.log('默认设置下拉为空！');
    	console.log('主表名:'+newValue);
        var url = '/widget/comboJson?exp=SELECT field_code ID FROM bs_metadata_b WHERE metadata_id in(SELECT id from bs_metadata where data_code="'+newValue+'" )';
        console.log('加载url:'+url);
        $link_column.eovacombo({url : url}).reload();
    }});
});