<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="ctx"
	value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
<title>合同</title>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />

<link href="${ctx}/css/bootstrap.css" rel="stylesheet" />

<script src="${ctx}/js/jquery.min.js"></script>
<script src="${ctx}/js/bootstrap.js"></script>
<style>
<style>
.leftBG{
    PADDING-LEFT: 3px;
    BACKGROUND: url("${ctx}/img/sideBG.jpg") #8fbde7 no-repeat left top;
}
.rightBG{
    PADDING-RIGHT: 3px;
    BACKGROUND: url("${ctx}/img/sideBG.jpg") no-repeat right top;
    min-width: 1200px;
}
.mainCont{
	PADDING-BOTTOM: 10px;
    MARGIN: 0px auto;
    PADDING-LEFT: 0px;
    WIDTH: 100%;
    PADDING-RIGHT: 0px;
    BACKGROUND: url("${ctx}/img/backcolor.jpg") #fff repeat-x 50% top;
    PADDING-TOP: 10px;
}
.rightCont{
	BORDER-BOTTOM: #b6d2ee 1px solid;
    BORDER-LEFT: #b6d2ee 1px solid;
    PADDING-BOTTOM: 20px;
    LINE-HEIGHT: 26px;
    MARGIN: 5px auto;
    MIN-HEIGHT: 800px;
    PADDING-LEFT: 20px;
    WIDTH: 90%;
    min-width: 900px;
    PADDING-RIGHT: 20px;
    BACKGROUND: #fff;
    HEIGHT: auto !important;
    BORDER-TOP: #b6d2ee 1px solid;
    BORDER-RIGHT: #b6d2ee 1px solid;
    PADDING-TOP: 20px;
}
.contentBig{
    width: 94%;
    margin: 0 auto;
    top: 10px;
    position: relative;
    padding: 0px 0px 0px 0px;
    min-width: 700px;
    text-align: center;
    BACKGROUND-COLOR: #cccccc;
}
.btnContainers{
	width:700px;
	margin:0px auto 10px;
	height:40px;
	text-align: center;
}
.btnContainers:after{
	content: '\0020';
	display: block;
	height: 0;
	font-size: 0;
	clear: both;
	overflow: hidden;
	visibility: hidden	
}
</style>
<script type="text/javascript">
	var _rootPath = "${ctx}";
	function change_height(){
		var real_height = document.body.clientHeight - 163;
		document.getElementById('real_page_height').style.height = real_height + "px";
	}
	function iFrameHeight() {   
		var ifm= document.getElementById("printFrame");   
		var subWeb = document.frames ? document.frames["printFrame"].document : ifm.contentDocument;   
		if(ifm != null && subWeb != null) {
		   ifm.height = subWeb.body.scrollHeight;
		   ifm.width = subWeb.body.scrollWidth;
		}   
	}   
</script>
</head>

<body style="font-family: 'Arial Unicode MS'">
	<div class="leftBG">
		<div class="rightBG">
			<div class="mainCont">
				<div class="rightCont">
					<div class="contentBig">
						<div class="btnContainers"></div>
						<iframe align="top" frameborder="0" height="780" name="printFrame" id="printFrame" scrolling="auto" width="700" src="${ctx}/mobile/order/show?id=${orderId }&sessionId=${sessionId }" onLoad="iFrameHeight();"></iframe>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>