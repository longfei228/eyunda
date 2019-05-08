$(document).ready(function(){
    
    $('input[type=checkbox],input[type=radio],input[type=file]').uniform();

    // Form Validation
    $("#frmSaveUpDownPort").validate({
        rules:{
            startPortNo:{
                required:true
            }
        },
        errorClass: "help-inline",
        errorElement: "span",
        highlight:function(element, errorClass, validClass) {
            $(element).parents('.control-group').addClass('error');
        },
        unhighlight: function(element, errorClass, validClass) {
            $(element).parents('.control-group').removeClass('error');
            $(element).parents('.control-group').addClass('success');
        }
    });

    // 添加
    $("#btnAdd").live("click", function() {
    	$("#editId").val("0");
    	$("#weight").val("0");
    	$("#startPortCity").val("11");
    	$("#startPortCity").trigger("change");

    	$("#editDialog").modal("show");
    });

    // 导入
    $("#importExcel").live("click", function() {
    	$("#importExcelDialog").modal("show");
    });

    // 修改
    $(".btnEdit").live("click", function() {
    	$("#editId").val($(this).attr("idVal"));
    	$("#weight").val($(this).attr("weightVal"));

    	$("#startPortCity").val($(this).attr("startPortCityVal"));

    	_stp = $(this).attr("startPortNoVal");
    	$("#startPortCity").trigger("change");
        
        $("#editDialog").modal("show");
    });

    // 删除
    $(".btnDelete").live("click", function() {
        $("#delId").val($(this).attr("idVal"));
        $("#deleteDialog").modal("show");
    });
    
    // 保存港口信息
    $(".btnSaveUpDownPort").live("click", function() {
    	if($("#frmSaveUpDownPort").valid())
        $.ajax({
          method : "GET",
          data : $("#frmSaveUpDownPort").formSerialize(),
          url : _rootPath+"/manage/ship/upDownPort/save",
          datatype : "json",
          success : function(data) {
            var returnCode = $(data)[0].returnCode;
            var message = $(data)[0].message;
            if (returnCode == "Failure") {
              alert(message);
              return false;
            } else {
              var params = "?nonsense＝0";
              var t = $("#frmSaveUpDownPort").serializeArray();
              $.each(t, function() {
                params+="&"+this.name+"="+this.value;
              });
              window.location.href = _rootPath + "/manage/ship/upDownPort" + params;
            }
          }
        });
    });

    // 删除港口信息
    $(".btnDeleteUpDownPort").live("click", function() {
        $.ajax({
          method : "GET",
          data : $("#frmDeleteUpDownPort").formSerialize(),
          url : _rootPath+"/manage/ship/upDownPort/delete",
          datatype : "json",
          success : function(data) {
            var returnCode = $(data)[0].returnCode;
            var message = $(data)[0].message;
            if (returnCode == "Failure") {
              alert(message);
              return false;
            } else {
              var params = "?nonsense＝0";
              var t = $("#frmDeleteUpDownPort").serializeArray();
              $.each(t, function() {
                params+="&"+this.name+"="+this.value;
              });
              window.location.href = _rootPath + "/manage/ship/upDownPort" + params;
            }
          }
        });
    });

	// 起运港改变
    $("#startPortCity").change(function(){
      $.ajax({
            method : "GET",
            data : {portCityCode : $(this).val()},
            url : _rootPath+"/port/getCityCodePorts",
            datatype : "json",
            success : function(data) {
                var returnCode = $(data)[0].returnCode;
                var message = $(data)[0].message;
                if (returnCode == "Failure") {
                    alert(message);
                    return false;
                } else {
                    var s = "";
                    s += "    <select id=\"startPortNo\" name=\"startPortNo\" style=\"width: 120px;\" >";
                    $.each($(data)[0].portDatas, function(i, portData) {
                    	if (portData.portNo==_stp){
                    		s += "      <option value=\""+portData.portNo+"\" selected > "+portData.portName+"</option>";
                    	}else{
                    		s += "      <option value=\""+portData.portNo+"\" > "+portData.portName+"</option>";
                    	}
                    });
                    s += "    </select>";
                    
                    $("#startPortName").html("");
                    $("#startPortName").html(s);
                    
                    $("#startPortNo").select2();
                    
                    return true;
                }
            }
        });

    });
    
    // 终止港改变
    $("#endPortCity").change(function(){
      $.ajax({
            method : "GET",
            data : {portCityCode : $(this).val()},
            url : _rootPath+"/port/getCityCodePorts",
            datatype : "json",
            success : function(data) {
                var returnCode = $(data)[0].returnCode;
                var message = $(data)[0].message;
                if (returnCode == "Failure") {
                    alert(message);
                    return false;
                } else {
                    var s = "";
                    s += "    <select id=\"endPortNo\" name=\"endPortNo\" style=\"width: 120px;\" >";
                    $.each($(data)[0].portDatas, function(i, portData) {
                    s += "      <option value=\""+portData.portNo+"\" > "+portData.portName+"</option>";
                    });
                    s += "    </select>";
                    
                    $("#endPortName").html("");
                    $("#endPortName").html(s);
                    
                    $("#endPortNo").select2();

                    return true;
                }
            }
        });

    });
    
    // 添加新港口
    $(".btnAddPort").click(function() {
        $.ajax({
            method : "GET",
            data : {para : 1},
            url : _rootPath+"/port/addNewPort",
            datatype : "json",
            success : function(data) {
                var returnCode = $(data)[0].returnCode;
                var message = $(data)[0].message;
                if (returnCode == "Failure") {
                    alert(message);
                    return false;
                } else {
                    //读入数据，并填入
                    $("#portNo").val("");
                    $("#portName").val("");

                    var s = "";
                    s += "<div class=\"control-group\">";
                    s += "  <label class=\"control-label\">港口城市：</label>";
                    s += "  <div class=\"controls\">";
                    s += "    <select id=\"portCityCode\" name=\"portCityCode\" style=\"width:280px;\">";
                    $.each($(data)[0].bigAreas, function(i, bigArea) {
                    s += "      <optgroup label=\""+bigArea.description+"\">";
                    $.each(bigArea.portCities, function(i, portCity) {
                    s += "      <option value=\""+portCity.code+"\"> "+portCity.description+"</option>";
                    });
                    s += "      </optgroup>";
                    });
                    
                    s += "    </select>";
                    s += "  </div>";
                    s += "</div>";

                    $("#selContent").html(s);
                    
                    $("#addPortDialog").modal("show");
                }
            }
        });
        return true;
    });

    // 保存起运港新港口信息
    $(".btnSaveNewPortInfo").live("click", function() {
        $.ajax({
          method : "GET",
          data : $("#frmNewPortInfo").formSerialize(),
          url : _rootPath+"/port/saveNewPort",
          datatype : "json",
          success : function(data) {
            var returnCode = $(data)[0].returnCode;
            var message = $(data)[0].message;
            if (returnCode == "Failure") {
              alert(message);
              return false;
            } else {
              window.location.reload();
              return true;
            }
          }
        });
    });

});