<%@page contentType="text/html;charset=UTF-8" import="java.util.*,com.tee.uit.help.Helps,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%@ include file="history.jsp" %>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.txt" %>
    <title>Documentation</title>
</head>
<body>
<div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Documentation"/>
    </jsp:include>
    <%@ include file="nmenu.jsp" %>
    <div id="workarea">
        <div id="outerframe">
            <div id="innerframe">
                    <%=Helps.get("doc1", "text")%>
            </div>                
      </div>                
    </div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.txt" %>                                
</body>
</html>
