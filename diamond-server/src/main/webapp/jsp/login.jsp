 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<title>Diamond管理后台登录</title>
</head>
<body>
<c:import url="/jsp/common/message.jsp"/>
<div align='center'>
<c:url var="url" value="/login.do" >
    <c:param name="method" value="login" />
</c:url>
<form method='post' action="${url}">
    <table>
        <tr>
            <td>用户名:</td>
            <td><input type='text' name="username" /></td>
        </tr>
        <tr>
            <td>密码:</td>
            <td><input type='password' name="password" /></td>
        </tr>
        <tr>
            <td colspan="2" align='center'>
                <input type="submit" value="登录" />
            </td>
        </tr>
    </table>
</form>
</div>
</body>
</html>
