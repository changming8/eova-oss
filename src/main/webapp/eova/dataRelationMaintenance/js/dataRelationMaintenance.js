var ids="";
var columns_field=[];
var master=0;
var table_name="";
var master_columns=[];
column("bs_md_def");
table("dataRelationMaintenance","/grid/query/bs_md_def-bs_md_def",null);
function column(code){
	$.ajax({
		url:"/meta/fields/"+code,
		type:"get",
		dataType:"json",
		async:false,
		success:function(data){
			//console.info(data);
			splice_column(data)
		}
	});
}
function splice_column(data){
	var fields=[];
	for(var i=0;i<data.length;i++){
		var isTrue=false;
		if(data[i].is_show==0){
			isTrue=true;
		}
		var field={
			"field":data[i].en,
			"title":data[i].cn,
			"width":data[i].width,
			"hidden":isTrue
		}
		fields.push(field);
	}
	columns_field.push(fields);
}
function table(tableName,url,master_){
	$("#"+tableName).datagrid({
	    url:url,
	    columns:columns_field,
	    fitColumns:true,
	    striped:true,
	    idField:"id",
	    pagination:true,
	    singleSelect:true,
	    rownumbers:true,
	    pageNumber:1,
	    pageSize:10,
	    pageList:[10,20,30,40],
	    onLoadSuccess:function(data){
	    	columns_field.splice(0,columns_field.length);
	    	//console.info(columns_field);
	    	++master;
	    	if(master==1){
	    		column("bs_style");
	    		table("master","/dataRelationMaintenance/queryBsStyle/bs_style-bs_style-"+null+"-"+null,null);
	    	}
	    	if("master_"==master_){
	    		column(table_name);
	    		table_master_("master","/dataRelationMaintenance/queryBsStyle/"+table_name+"-"+table_name+"-",null);
	    	}
	    },
	    onCheck:function(rowIndex,rowData){
	    	$("#master_").css("display","block");
		    var id=rowData.id;
		    table_name=rowData.md_table;
		    isTrue=true;
		    ids=id;
		    column("bs_style");
	    	table_master("master","/dataRelationMaintenance/queryBsStyle/bs_style-bs_style-"+id+"-"+table_name);
	    }
	});
}
var isTrue=true;
function table_master(tableName,url){
	$("#"+tableName).datagrid({
	    url:url,
	    columns:columns_field,
	    fitColumns:true,
	    striped:true,
	    idField:"id",
	    pagination:true,
	    singleSelect:true,
	    rownumbers:true,
	    pageNumber:1,
	    pageSize:10,
	    pageList:[10,20,30,40],
	    onLoadSuccess:function(data){
	    	columns_field.splice(0,columns_field.length);
	    	//console.info(columns_field);
	    	if(data.rows.length<1){
	    		return;
	    	}
	    	if(isTrue){
		    	var id=data.rows[0].id;
		    	column("bs_style_b");
		    	table("master","/dataRelationMaintenance/queryBsStyle/bs_style_b-bs_style_b-"+id,"master_");
		    	isTrue=false;
	    	}
	    	
	    },
	    onCheck:function(rowIndex,rowData){
	    	
	    }
	});
}
function table_master_(tableName,url){
	$("#"+tableName).datagrid({
	    url:url,
	    columns:columns_field,
	    fitColumns:true,
	    striped:true,
	    idField:"id",
	    pagination:true,
	    singleSelect:true,
	    rownumbers:true,
	    pageNumber:1,
	    pageSize:10,
	    pageList:[10,20,30,40],
	    onLoadSuccess:function(data){
	    	columns_field.splice(0,columns_field.length);
	    	//console.info(columns_field);
	    	console.info(data);
	    	
	    },
	    onCheck:function(rowIndex,rowData){
	    	
	    }
	});
}