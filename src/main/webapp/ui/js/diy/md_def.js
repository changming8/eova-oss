// JS基础在VIP教程-第三部 开发原生技能(Web开发基础补习)中讲解
// JS基础不好的同学可以乘机成为VIP用户补一下课:http://www.eova.cn/help
$(document).ready(function() {
	debugger;
	var $desttable_code = $('#desttable_code');
	var $md_column = $('#md_column');
	var $desttable_id = $('#desttable_id');
	var $desttable_name = $('#desttable_name');
	var $mdtable_code = $('#mdtable_code');
	var $destfield_code = $('#destfield_code');
	$mdtable_code.eovafind({onChange: function (oldValue, newValue,rows) {
		debugger;
		$("input[name='mdtable_id']").val(rows[0].id);
		$("input[name='mdtable_name']").val(rows[0].table_name);
    }});
	
	
	var para = window.location.search.split("=")[1];
	if ("undefined" == typeof para)
		return;
	var pid = para.split(":")[1];



	$destfield_code.mask();
	$desttable_code.eovafind({onChange: function (oldValue, newValue,row) {
        	$dest_column.mask();
    		$("input[name='desttable_id']").val(rows[0].id);
    		$("input[name='desttable_name']").val(rows[0].field_name);
    }});
	
	
	$destfield_code.eovafind({onChange: function (oldValue, newValue,row) {
        if (newValue == "") {
        	$dest_column.mask();
    		$("input[name='desttable_id']").val(rows[0].id);
    		$("input[name='desttable_name']").val(rows[0].field_name);
            return;
        }
        
        $dest_column.unmask();
        $dest_column.eovafind({exp : 'md_dest_column_ref,' + newValue});
    }});
	
	$md_column.eovafind({
		exp : 'md_ref,' + pid
	});

});