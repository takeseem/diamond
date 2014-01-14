 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<title>Diamond配置信息管理</title>
<script type="text/javascript">
   function confirmForDelete(){
       return window.confirm("你确认要删除该配置信息吗??");  
   }
   function queryConfigInfo(method){
       document.all.queryForm.method.value=method;
       document.all.queryForm.submit();       
   }
  
</script>
</head>
<c:url var="adminUrl" value="/admin.do" >
</c:url>
<c:if test="${method==null}">
    <c:set var="method" value="listConfig"/>
</c:if>

<body>
<c:import url="/jsp/common/message.jsp"/>
<center><h1><strong>配置信息管理</strong></h1></center>
 <p align='center'>
       <form name="queryForm" action="${adminUrl}">
       <table align='center'>
           <tr>
               <td>dataId:</td>
               <td><input type="text" name="dataId"/></td>
               <td>组名:</td>
               <td><input type="text" name="group"/></td>
               <td>
                  <input type='hidden' name="pageNo" value='1'/>
                  <input type='hidden' name="method" value='${method}'/>
                  <input type='hidden' name="pageSize" value='15'/>
                  <input type='button' value='查询' onclick="queryConfigInfo('listConfig');"/>
                  <input type='button' value='模糊查询' onclick="queryConfigInfo('listConfigLike');"/></td>
           </tr>
       </table>
       </form>
    </p>
   <p align='center'>
     <c:if test="${page!=null}">
      <table border='1' width="800">
          <tr>
              <td>dataId</td>
              <td>组名</td>
              <td>操作</td>
          </tr>
          <c:forEach items="${page.pageItems}" var="configInfo">
            <tr>
               <td name="tagDataID">
                  <c:out value="${configInfo.dataId}"/>
               </td>
              <td name="tagGroup">
                  <c:out value="${configInfo.group}" escapeXml="false"/>
               </td>
              <c:url var="getConfigInfoUrl" value="/admin.do" >
                  <c:param name="method" value="detailConfig" />
                  <c:param name="group" value="${configInfo.group}" />
                  <c:param name="dataId" value="${configInfo.dataId}" />
              </c:url>
               <c:url var="deleteConfigInfoUrl" value="/admin.do" >
                  <c:param name="method" value="deleteConfig" />
                  <c:param name="id" value="${configInfo.id}" />
              </c:url>
               <c:url var="saveToDiskUrl" value="/notify.do" >
                  <c:param name="method" value="notifyConfigInfo" />
                   <c:param name="group" value="${configInfo.group}" />
                  <c:param name="dataId" value="${configInfo.dataId}" />
              </c:url>
              <c:url var="previewUrl" value="/config.co" >
                  <c:param name="group" value="${configInfo.group}" />
                  <c:param name="dataId" value="${configInfo.dataId}" />
              </c:url>
              <td>
                 <a href="${getConfigInfoUrl}">编辑</a>&nbsp;&nbsp;&nbsp;
                 <a href="${deleteConfigInfoUrl}" onclick="return confirmForDelete();">删除</a>&nbsp;&nbsp;&nbsp;
                 <a href="${saveToDiskUrl}" target="_blank">保存磁盘</a>
                 <a href="${previewUrl}" target="_blank">预览</a>
              </td>
            </tr>
          </c:forEach>
       </table>
       <p align='center'>
          总页数:<c:out value="${page.pagesAvailable}"/>&nbsp;&nbsp;当前页:<c:out value="${page.pageNumber}"/>
          &nbsp;&nbsp;&nbsp;&nbsp;
          <c:url var="nextPage" value="/admin.do" >
             <c:param name="method" value="${method}" />
             <c:param name="group" value="${group}" />
             <c:param name="dataId" value="${dataId}" />
             <c:param name="pageNo" value="${page.pageNumber+1}" />
             <c:param name="pageSize" value="15" />
         </c:url>
         <c:url var="prevPage" value="/admin.do" >
             <c:param name="method" value="${method}" />
             <c:param name="group" value="${group}" />
             <c:param name="dataId" value="${dataId}" />
             <c:param name="pageNo" value="${page.pageNumber-1}" />
             <c:param name="pageSize" value="15" />
         </c:url>
         <c:url var="firstPage" value="/admin.do" >
             <c:param name="method" value="${method}" />
             <c:param name="group" value="${group}" />
             <c:param name="dataId" value="${dataId}" />
             <c:param name="pageNo" value="1" />
             <c:param name="pageSize" value="15" />
         </c:url>
         <c:url var="lastPage" value="/admin.do" >
             <c:param name="method" value="${method}" />
             <c:param name="group" value="${group}" />
             <c:param name="dataId" value="${dataId}" />
             <c:param name="pageNo" value="${page.pagesAvailable}" />
             <c:param name="pageSize" value="15" />
         </c:url>
         <a href="${firstPage}">首页</a>&nbsp;&nbsp; 
          <c:choose>
             <c:when  test="${page.pageNumber==1 && page.pagesAvailable>1}">
               <a href="${nextPage}">下一页</a>  &nbsp; &nbsp;
             </c:when>
             <c:when  test="${page.pageNumber>1 && page.pagesAvailable==page.pageNumber}">
               <a href="${prevPage}">上一页</a>  &nbsp; &nbsp;
             </c:when>
             <c:when  test="${page.pageNumber==1 && page.pagesAvailable==1}">
             </c:when>
             <c:otherwise>
                <a href="${prevPage}">上一页</a>  &nbsp; &nbsp;
                <a href="${nextPage}">下一页</a>  
             </c:otherwise>
          </c:choose>
          <a href="${lastPage}">末页</a>&nbsp;&nbsp; 
       </p>
     </c:if>
  </p>
  <p align='center'>
    <a href="<c:url value='/jsp/admin/config/new.jsp' />">添加配置信息</a>  &nbsp;&nbsp;&nbsp;&nbsp; <a href=" <c:url value='/jsp/admin/config/upload.jsp' />">上传配置信息</a>
  </p>
</body>
</html>