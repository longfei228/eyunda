<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<c:set var="ctx"
	value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
<title>易运达 - 首页</title>
<meta http-equiv="X-UA-Compatible" content="IE=9">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<meta name="description" content="" />
<meta name="author" content="guoqiang" />

<link href="${ctx}/css/bootstrap.css" rel="stylesheet" />
<link href="${ctx}/css/bootstrap-responsive.css" rel="stylesheet" />
<link href="${ctx}/css/font-awesome.css" rel="stylesheet" />
<link href="${ctx}/css/common.css" rel="stylesheet" />

<script src="${ctx}/js/jquery-v.min.js"></script>
<script src="${ctx}/js/bootstrap.js"></script>

<script type="text/javascript">
	var _rootPath = "${ctx}";
</script>
<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
  <script src="${ctx}/js/html5.js"></script>
<![endif]-->
<style>
#changeList{
	margin:15px 15px 0px 0px;
}
#changeList .cl{
	display: inline-block;
	width: 12px;
	height: 12px;
	background: #34538b;
	font-size: 0;
	font-family: 'Lucida Grande', Verdana;
}
#changeList .on{
	background: #a0b3d6;
}

#changeList .h{
	height: 2px;
	width: 12px;
	background: white;
	position: absolute;
	margin-top: 5px;
}
#changeList .v{
	width: 2px;
	height: 12px;
	background: white;
	position: absolute;
	margin-left: 5px;
}
#layoutV{
	margin-left:10px;
}

.TickerNews{
	width: 100%;
	height: 30px;
	line-height: 30px;
}
.ti_wrapper{
	width: 100%;
	position: relative;
	overflow: hidden;
	height: 30px;
}
.ti_slide{
	width: 30000px;
	position: relative;
	left: 0;
	top: 0;
}
.ti_content{
	width: 8000px;
	position: relative;
	float:left;
}
.ti_news{
	float:left;
}
.ti_news a{
	display: block;
	margin-right: 10px;
}
</style>

</head>

<body>
	<!-- section header -->
	<jsp:include page="portal-head.jsp"></jsp:include>
	<nav class="breadcrumb" style="margin-bottom:10px;">
		<i class="fa-television fa"></i>
		<a class="crumbs" title="返回首页" href="${ctx }/portal/home/shipHome">首页</a>
		<i class="fa fa-angle-right"></i>
		${title }
	</nav>
	<!-- section content -->
	<div id="content" class="site-content">
		<div class="clear"></div>
		<!-- content-area -->
		<div id="primary" class="content-area">
			<div class="line-one">
				<div class="cat-box">
					<h3 class="cat-title">
						<a href="###" title="最新${title }信息"><i class="fa fa-bars"></i>${title }
						</a>
						<span style="float:right" id="changeList">
							<a href="javascript:void(0);" class="cl" id="layoutH">
                    		<span class="h"></span>
                    		</a>
                    		<a href="javascript:void(0);" class="cl on" id="layoutV">
                    			<span class="h"></span>
                        		<span class="v"></span>
                    		</a>
                    	</span>
					</h3>
					<div class="clear"></div>
					<div class="cat-site" style="margin:0px;">
					  <c:forEach varStatus="status" var="showModel" items="${pageData.result}">
			              <c:if test="${status.index != fn:length(pageData.result) - 1}">
			                <div class="one-line">
			              </c:if>
			              <c:if test="${status.index == fn:length(pageData.result) - 1}">
			                <div class="one-line no-border">
			              </c:if>
			                <div class="row-fluid">
			                    <div class="s-head span1" style="width:80px;height:100px;float:left">
			                    <a href="${ctx}/portal/home/${infoUrl }=${showModel.id}">
			                      <c:if test="${empty showModel.logo}">
			                        <img src="${ctx}/img/shipImage/${showModel.logo}.jpg" alt="" style="width:80px;height:80px;" class="thumbnail"/>
			                      </c:if>
			                      <c:if test="${!empty showModel.logo}">
			                        <img src="${showModel.logo}" alt="" style="width:80px;height:80px;" class="thumbnail"/>
			                      </c:if>
			                      </a>
			                    </div>
			                    <div class="one-line-info span10 fa-pull-right" style="width: 87%;margin-right: -80px;padding-left: 10px;padding-top:5px;">
			                      <div class="one-line-title row-fluid">
			                        <div class="span6 adj-height-40" style="line-height:30px;">
			                        <a href="${ctx}/portal/home/${infoUrl }=${showModel.id}">
			                        ${showModel.name}
			                        </a>
			                        </div>
			                        <div class="span6 to-right adj-height-40" style="line-height:30px;">${showModel.num}</div>
			                      </div>
			                        <div class="TickerNews">
									    <div class="ti_wrapper">
									        <div class="ti_slide">
									            <div class="ti_content">
									                <div class="ti_news"><a href="${ctx}/portal/home/${infoUrl }=${showModel.id}">${showModel.descrip }</a></div>
												 </div>
									        </div>
									    </div>
									</div> 
									<div class="fees">
										<c:if test="${!empty showModel.dayfee}">
											<span style="float:left">${showModel.dayfee}</span>
										</c:if>
										<c:if test="${!empty showModel.oilfee}">
										<span style="float:right">${showModel.oilfee}</span>
										</c:if>
										<div class="clear"></div>
									</div>
			                    </div>
			                </div>
			              </div>
			              <div class="clear"></div>
			            </c:forEach>
					</div>
					<div class="clear"></div>
				</div>
			</div>
			<jsp:include page="pager.jsp"></jsp:include>
		</div>
		<!-- widget-area -->
		<div id="sidebar" class="widget-area">
			<jsp:include page="./notice.jsp"></jsp:include>

			<jsp:include page="./customerDownload.jsp"></jsp:include>
		</div>
		<div class="clear"></div>
	</div>
	<!-- section footer -->
	<jsp:include page="portal-foot.jsp"></jsp:include>
	<!-- javascript
    ================================================== -->
	<script src="${ctx}/js/jquery.divSelect.js"></script>
	<script src="${ctx}/js/jquery.tickerNews.js"></script>
	<script type="text/javascript">
	$(function(){
		var timer = !1;
		_Ticker = $(".TickerNews").newsTicker();
		_Ticker.on("mouseenter",function(){
			var __self = this;
			timer = setTimeout(function(){
				__self.pauseTicker();
			},200);
		});
		_Ticker.on("mouseleave",function(){
			clearTimeout(timer);
			if(!timer) return !1;
			this.startTicker();
		});
	});
	</script>
</body>
</html>