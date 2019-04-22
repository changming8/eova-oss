	var $analysis_rule = $('#analysis_rule');//固定式or解析式
	var $search_path = $('#search_path');//FTP路径
    var $file_name= $('#file_name');//文件名
    var $response_file= $('#response_file'); //响应文件
    var $file_type= $('#file_type');//文件类型
    var $day_rule= $('#day_rule');//日期解析规则
    var $search_path1= $('#search_path1');//搜索路径1
    var $search_path2= $('#search_path2');//搜索路径2
    var $file_name1= $('#file_name1');//对方系统标识
    var $file_name2= $('#file_name2');//业务标识
    var $file_name4= $('#file_name4');//文件日期格式
    var $file_name6= $('#file_name6');//
    var $description_file= $('#description_file');//DESC or DESC.OK or DESC.ERR

$(document).ready(function(){
    
    // 初始禁用
    $search_path.mask();
    $file_name.mask();
    $response_file.mask();
    $file_type.mask();
    $day_rule.mask();
    $search_path1.mask();
    $search_path2.mask();
    $file_name1.mask();
    $file_name2.mask();
    $file_name4.mask();
    $file_name6.mask();
    $description_file.mask();
    
});

window.onload=function(){
	var type = $('input[name="analysis_rule"]').val();
	if (type == "") {
    	$search_path.mask();
        $file_name.mask();
        $response_file.mask();
        $file_type.mask();
        $day_rule.mask();
        $search_path1.mask();
        $search_path2.mask();
        $file_name1.mask();
        $file_name2.mask();
        $file_name4.mask();
        $file_name6.mask();
        $('input[name="search_path"]').val('');
        $('input[name="file_name"]').val('');
        $('input[name="response_file"]').val('');
        $('input[name="file_type"]').val('');
        $('input[name="search_path1"]').val('');
        $('input[name="file_name1"]').val('');
        $('input[name="file_name2"]').val('');
        //$file_type.eovacombo().setValue("");
        $day_rule.eovacombo().setValue("");
        $search_path2.eovacombo().setValue("");
        $file_name4.eovacombo().setValue("");
        $file_name6.eovacombo().setValue("");
        $description_file.eovacombo().setValue("");
    }
    if (type == "1") {
        $search_path.unmask();
        $file_name.unmask();
        $response_file.mask();
        $file_type.mask();
        $day_rule.mask();
        $search_path1.mask();
        $search_path2.mask();
        $file_name1.mask();
        $file_name2.mask();
        $file_name4.mask();
        $file_name6.mask();
        $('input[name="response_file"]').val('');
        $('input[name="file_type"]').val('');
        $('input[name="search_path1"]').val('');
        $('input[name="file_name1"]').val('');
        $('input[name="file_name2"]').val('');
        //$file_type.eovacombo().setValue("");
        $day_rule.eovacombo().setValue("");
        $search_path2.eovacombo().setValue("");
        $file_name4.eovacombo().setValue("");
        $file_name6.eovacombo().setValue("");
        $description_file.eovacombo().setValue("");
    }
    if (type == "2") {
    	$search_path.mask();
        $file_name.mask();
        $('input[name="search_path"]').val('');
        $('input[name="file_name"]').val('');
        $response_file.unmask();
        $file_type.unmask();
        $analytical_rule.unmask();
        $search_path1.unmask();
        $search_path2.unmask();
        $file_name1.unmask();
        $file_name2.unmask();
        $file_name4.unmask();
        $file_name6.unmask();
        $description_file.unmask();
    }
	};



// 解析规则改变事件
$analysis_rule.eovacombo({onChange: function (oldValue, newValue) {
    if (newValue == "") {
    	$search_path.mask();
        $file_name.mask();
        $response_file.mask();
        $file_type.mask();
        $analytical_rule.mask();
        $search_path1.mask();
        $search_path2.mask();
        $file_name1.mask();
        $file_name2.mask();
        $file_name4.mask();
        $file_name6.mask();
        $('input[name="search_path"]').val('');
        $('input[name="file_name"]').val('');
        $('input[name="response_file"]').val('');
        $('input[name="file_type"]').val('');
        $('input[name="search_path1"]').val('');
        $('input[name="file_name1"]').val('');
        $('input[name="file_name2"]').val('');
        //$file_type.eovacombo().setValue("");
        $day_rule.eovacombo().setValue("");
        $search_path2.eovacombo().setValue("");
        $file_name4.eovacombo().setValue("");
        $file_name6.eovacombo().setValue("");
        $description_file.eovacombo().setValue("");
    }
    if (newValue == "1") {
        $search_path.unmask();
        $file_name.unmask();
        $response_file.mask();
        $file_type.mask();
        $day_rule.mask();
        $search_path1.mask();
        $search_path2.mask();
        $file_name1.mask();
        $file_name2.mask();
        $file_name4.mask();
        $file_name6.mask();
        $('input[name="response_file"]').val('');
        $('input[name="file_type"]').val('');
        $('input[name="search_path1"]').val('');
        $('input[name="file_name1"]').val('');
        $('input[name="file_name2"]').val('');
        //$file_type.eovacombo().setValue("");
        $day_rule.eovacombo().setValue("");
        $search_path2.eovacombo().setValue("");
        $file_name4.eovacombo().setValue("");
        $file_name6.eovacombo().setValue("");
        $description_file.eovacombo().setValue("");
    }
    if (newValue == "2") {
    	$search_path.mask();
        $file_name.mask();
        $('input[name="search_path"]').val('');
        $('input[name="file_name"]').val('');
        $response_file.unmask();
        $file_type.unmask();
        $day_rule.unmask();
        $search_path1.unmask();
        $search_path2.unmask();
        $file_name1.unmask();
        $file_name2.unmask();
        $file_name4.unmask();
        $file_name6.unmask();
        $description_file.unmask();
    }
}});