$(document).ready(function(){
	var $standard_type = $('#standard_type');//固定式or解析式
	var $search_path = $('#search_path');//搜索路径
    var $file_name= $('#file_name');//文件名
    var $response_file= $('#response_file'); //响应文件
    var $file_type= $('#file_type');//文件类型
    var $analytical_rule= $('#analytical_rule');//解析规则
    var $search_path1= $('#search_path1');//搜索路径1
    var $search_path2= $('#search_path2');//搜索路径2
    var $file_name1= $('#file_name1');//对方系统标识
    var $file_name2= $('#file_name2');//业务标识
    var $file_name4= $('#file_name4');//文件日期格式
    var $file_name6= $('#file_name6');//
    
    // 初始禁用
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
    
 // 解析规则改变事件
    $standard_type.eovacombo({onChange: function (oldValue, newValue) {
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
            $analytical_rule.eovacombo().setValue("");
            $search_path2.eovacombo().setValue("");
            $file_name4.eovacombo().setValue("");
            $file_name6.eovacombo().setValue("");
        }
        if (newValue == "1") {
            $search_path.unmask();
            $file_name.unmask();
            $response_file.mask();
            $file_type.mask();
            $analytical_rule.mask();
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
            $analytical_rule.eovacombo().setValue("");
            $search_path2.eovacombo().setValue("");
            $file_name4.eovacombo().setValue("");
            $file_name6.eovacombo().setValue("");
        }
        if (newValue == "2") {

        	debugger;
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
        }
    }});
});