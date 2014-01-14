<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<title>配置信息详情</title>
	<script src="<c:url value='/js/prototype_for_validation.js'/>" type="text/javascript"></script>
    <script src="<c:url value='/js/effects.js'/>" type="text/javascript"></script>
	<script src="<c:url value='/js/validation_cn.js'/>" type="text/javascript"></script>
	<style type="text/css">
		body td {
			color: #333;
			font-family: Arial, Helvetica, sans-serif;
			font-size: 10pt;
		}
		.validation-advice {
			margin: 0px 0;
			padding: 0px;
			margin-left: 10px;
			color : #FF3300;
			font-weight: bold;
			display: inline;
		}
		</style>
</head>
<body>
<c:url var="updateConfigUrl" value="/admin.do" >
    <c:param name="method" value="updateConfig" />
</c:url>
<c:import url="/jsp/common/message.jsp"/>
<form action="${updateConfigUrl}" method="post" id="update-form">
    <table align='center'>
        <tr>
            <td>dataId:</td>
            <td>
               <input readonly="true" type='text' name='dataId' value="${configInfo.dataId}" width="256" class="required max-length-128" />
            </td>
        </tr>
        <tr>
            <td>group:</td>
            <td> <input type='text' readonly="true" name='group' value="${configInfo.group}" width="256" class="required max-length-128"/></td>
        </tr>
         <tr>
            <td colspan="2">content:</td>
         </tr> 
         <tr>  
            <td colspan="2">
               <textarea cols="100" rows="20" name="content" class="required max-length-65535"><c:out value="${configInfo.content}"/></textarea>
            </td>
        </tr>
        <tr> 
            <td colspan="2"><input type="submit" value="更改"/>
        </tr>
    </table>
 </form>
 <!-- 上传表单 -->
 <c:url var="reuploadUrl" value="/admin.do" >
    <c:param name="method" value="reupload" />
</c:url>
<form action="${reuploadUrl}" method="post" enctype="multipart/form-data" id="upload-form">
        <table align='center'>
        <tr>
            <td colspan="2">
               <input type='hidden' name='dataId' width="256" class="required max-length-128" value="${configInfo.dataId}"/>
            </td>
        </tr>
        <tr>
             <td colspan="2"> <input type='hidden' name='group' width="256" class="required max-length-128" value="${configInfo.group}"/></td>
        </tr>
         <tr>
            <td>重新上传配置文件:</td>
            <td>
                <input type="file" name="contentFile" class="required"/>&nbsp;&nbsp;<input type="submit" value="上传"/>
            </td>
         </tr> 
 </form>
<script type="text/javascript">
    	new Validation('update-form',{immediate:true}); 
    	new Validation('upload-form',{immediate:true}); 
  </script>  
</body>
</html>