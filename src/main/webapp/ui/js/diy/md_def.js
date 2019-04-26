// JS基础在VIP教程-第三部 开发原生技能(Web开发基础补习)中讲解
// JS基础不好的同学可以乘机成为VIP用户补一下课:http://www.eova.cn/help
$(document).ready(function() {
	debugger;
	var str = this.URL.split(":");
	var pid = str[str.length - 1];

	var $dest_table = $('#dest_table');
	var $md_column = $('#md_column');
	var $dest_column = $('#dest_column');

	$dest_column.mask();
	$dest_table.eovafind({onChange: function (oldValue, newValue) {
		$dest_column.eovafind().setValue("");
        if (newValue == "") {
        	$dest_column.mask();
            return;
        }
        
        $dest_column.unmask();
        $dest_column.eovafind({exp : 'md_dest_column_ref,' + newValue});
    }});
	
	$md_column.eovafind({
		exp : 'md_ref,' + pid
	});

});