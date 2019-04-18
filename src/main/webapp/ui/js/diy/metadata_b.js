$(document).ready(function(){
	var $link_table = $('#link_table');
    var $link_column= $('#link_column');
    // 初始禁用
    $link_column.mask();
 	//
    $link_table.eovafind({onChange: function (oldValue, newValue) {
    	//$link_column.eovafind().setValue("");
    	$("#link_column input[type=text]").each(function () {
    		console.log(this.value);
    		this.value='';
    		
    	})
    	console.log('选中值:'+newValue);
    	if (newValue == "") {
    		$link_column.mask();
    		return;
        }
    	$link_column.unmask();
    	console.log('默认设置下拉为空！');
    	console.log('主表名:'+newValue);
        $link_column.eovafind({exp : 'bs_metadata_column_ref,' + newValue});
    }});
});