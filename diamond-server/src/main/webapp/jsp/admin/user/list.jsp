 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<title>Diamond配置信息管理</title>
<script type="text/javascript">
   function confirmForDelete(){
       return window.confirm("你确认要删除该用户吗??");  
   }
   
   function changePassword(user,link){
       var newPass=window.prompt("请输入新密码：");
       if(newPass==null||newPass.length==0)
         return false;
       link.href=link.href+"&password="+newPass;
       return window.confirm("你确认要将"+user+"的密码修改为"+newPass+"吗??");  
   }
  
</script>
</head>
<body>
<c:import url="/jsp/common/message.jsp"/>
<center><h1><strong>用户管理</strong></h1></center>
   <p align='center'>
     <c:if test="${userMap!=null}">
      <table border='1' width="800">
          <tr>
              <td>用户名</td>
              <td>密码</td>
              <td>操作</td>
          </tr>
          <c:forEach items="${userMap}" var="user">
            <tr>
               <td>
                  <c:out value="${user.key}"/>
               </td>
              <td>
                  <c:out value="${user.value}" />
               </td>
              <c:url var="changePasswordUrl" value="/admin.do" >
                  <c:param name="method" value="changePassword" />
                  <c:param name="userName" value="${user.key}" />
              </c:url>
              <c:url var="deleteUserUrl" value="/admin.do" >
                  <c:param name="method" value="deleteUser" />
                  <c:param name="userName" value="${user.key}" />
                  <c:param name="password" value="${user.value}" />
              </c:url>
              <td>
                 <a href="${changePasswordUrl}" onclick="return changePassword('${user.key}',this);">修改密码</a>&nbsp;&nbsp;&nbsp;
                 <a href="${deleteUserUrl}" onclick="return confirmForDelete();">删除</a>&nbsp;&nbsp;&nbsp;
              </td>
            </tr>
          </c:forEach>
       </table>
    </c:if>
  </p>
  <p align='center'>
    <a href="<c:url value='/jsp/admin/user/new.jsp' />">添加用户</a> &nbsp;&nbsp;&nbsp;&nbsp;<a href="<c:url value='/admin.do?method=reloadUser' />">重新加载用户信息</a>
  </p>
</body>
</html>