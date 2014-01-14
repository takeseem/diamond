<%@page contentType="text/html;charset=GBK" isErrorPage="true"%>
<html>
<head><title>出现错误</title>
<script type="text/javascript">
  function displayErrorInfo()
  {
      var errorInfo=document.getElementById("errorInfo");
      errorInfo.style.display=(errorInfo.style.display=="none"?"":"none");
  }
</script>
</head>
<body>
     <p>服务器出现内部错误，请联系管理员</p>
     <p><a onclick="displayErrorInfo();" href="#">查看异常信息</a></p>
     <div id="errorInfo" style="display:none"><%=exception.getMessage()%></div>
</body>
</html>