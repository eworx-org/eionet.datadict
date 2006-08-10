<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.meta.exports.schema.*,eionet.util.Util,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/import", "x")){ %>
		<b>Not allowed!</b><%
		return;
	}
	
	String mode = request.getParameter("mode");
	if (mode==null || mode.length()==0)
		mode = "DST";
	else if (!mode.equals("FXV")){ %>
		<b>Unknown mode!</b><%
	}
	
	String delem_id = request.getParameter("delem_id");
	if (mode.equals("FXV")){
		if (delem_id==null || delem_id.length()==0){%>
			<b>Missing data element ID in fixed values import mode!</b><%
		}
	}
	
	String elmName = request.getParameter("short_name");
	if (elmName==null || elmName.length()==0) elmName = "?";
%>
<html>
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Data Dictionary</title>
	<script language="javascript" src='script.js' type="text/javascript"></script>
	<script language="javascript" type="text/javascript">
	// <![CDATA[

	function submitForm(){

		var radio = "";
		var type = "";

		for (var i=0; i<document.forms["Upload"].elements.length; i++){
			var o = document.forms["Upload"].elements[i];
			if (o.name == "fileORurl"){
				if (o.checked == true){
					radio = o.value;
					//break;

				}
			}
				
			if (o.name == "type"){
				type = o.value;
				//break;
			}
		}
		
		var url = document.forms["Upload"].elements["url_input"].value;
		var file = document.forms["Upload"].elements["file_input"].value;
		var ok = true;

		if (radio == "url"){
			if (url == ""){
				alert("URL is not specified, there is nothing to import!");
				ok = false;
			}
		}
		
		if (radio == "file"){
			if (file == ""){
				alert("File location is not specified, there is nothing to import!");
				ok = false;
			}
		}

		if (ok == true){
			
			var qryStr = "?fileORurl=" + radio + "&url_input=" + url + "&type=" + type;
			<%
			if (mode.equals("FXV")){ %>
				qryStr = qryStr + "&delem_id=<%=delem_id%>";<%
			}
			%>
			
			document.forms["Upload"].action = document.forms["Upload"].action + qryStr;
			//alert(document.forms["Upload"].action);
			document.forms["Upload"].submit();
		}
	}
	// ]]>
	</script>
</head>
<body>
	<jsp:include page="nlocation.jsp" flush='true'>
		<jsp:param name="name" value="Import"/>
		<jsp:param name="back" value="true"/>
	</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
	<div id="operations">
		<ul>
				<li class="help"><a target="_blank" href="help.jsp?screen=import&amp;area=pagehelp" onclick="pop(this.href);return false;" title="Get some help on this page">Page help</a></li>
		</ul>
	</div>

	<h1>Import data</h1>

	<p>
	This is a function enabling you to import data definitions from the <b>XML files generated by</b>
	the data definer's helper <b>Import tool</b>. To find out more about that tool,
	contact the Data Dictionary's owner.
	</p>
	<p>
	You can import the file either from a URL or a location on your local file system.
	</p>
					
		<%
		if (mode.equals("FXV")){ %>
			<p>
					<span style="color:red">
						You have chosen to import fixed values (i.e. codes)<br/> for the
						<a href="data_element.jsp?mode=view&amp;delem_id=<%=delem_id%>"><%=Util.replaceTags(elmName)%></a> element!
					</span>
			</p><%
		}
		%>
				
				<form name="Upload" action="Import" method="post" enctype="multipart/form-data">
				
				<input type="hidden" name="type" value="<%=mode%>"/>

				<table width="auto" cellspacing="0">
					<tr>
						<td align="left" style="padding-right:5">
							<input type="radio" name="fileORurl" value="file" checked="checked"/>&#160;File:</td>
						<td align="left">
							<input type="file" class="smalltext" name="file_input" size="40"/>
						</td>
					</tr>
					<tr>
						<td align="left" style="padding-right:5">
							<input type="radio" class="smalltext" name="fileORurl" value="url"/>&#160;URL:
						</td>
						<td align="left">
							<input type="text" class="smalltext" name="url_input" size="52"/>
						</td>
					</tr>
					<tr style="height:10px;"><td colspan="2"></td></tr>
					<tr>
						<td></td>
						<td align="left">
							<% if (user!=null){ %>									
								<input name="SUBMIT" type="button" class="mediumbuttonb" value="Import" onclick="submitForm()" onkeypress="submitForm()"/>&#160;&#160;
							<%}%>
								<input name="RESET" type="reset" class="mediumbuttonb" value="Clear"/>
						</td>
					</tr>
				</table>
			</form>	
</div>
</body>
</html>
