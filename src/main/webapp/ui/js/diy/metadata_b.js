$(document).ready(function(){
	var $link_column_a = $('#link_column');
//    var $city= $('#city');
//    var $region= $('#region');

    // 初始禁用
    //$link_column.mask();
   // $region.mask();
 	// 省级联市
	$link_column_a.eovacombo({onChange: function (oldValue, newValue) {
    	//$link_column.eovacombo().setValue("");
    	console.log('默认设置下拉为空！');
    	newValue = $("input[name='link_table']")[0].value;
    	console.log('主表名:'+newValue);
        //var url = '/widget/comboJson?exp=SELECT field_code cn FROM bs_metadata_b WHERE dr = 0';
        //console.log('加载url:'+url);
        //$link_column.eovacombo({url : url}).reload();
    	$link_column_a.eovacombo({exp : 'selectBs_metadata_bByDr0,' + newValue}).reload();
    }});
});