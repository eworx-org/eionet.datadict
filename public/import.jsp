<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.meta.exports.schema.*,com.tee.xmlserver.*"%>

<%
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
	<title>Data Dictionary</title>
	<META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
	<link type="text/css" rel="stylesheet" href="eionet.css">
	<script language="JavaScript" src='script.js'></script>
	<SCRIPT LANGUAGE="JavaScript">

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
	</SCRIPT>
</head>
<body>
<%@ include file="header.htm"%>

<table border="0">
    <tr valign="top">
		<td nowrap="true" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></P>
        </TD>
        <TD>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Import"/>
            </jsp:include>
            
            	<div style="margin-left:30">
            
				<table width="500">
					<tr height="20"><td colspan="2">&nbsp;</td></tr>
					<tr>
						<td>
							<span class="head00">Import data</span>
						</td>
						<td align="right">
							<a target="_blank" href="help.jsp?screen=import&area=pagehelp" onclick="pop(this.href)">
								<img src="images/pagehelp.jpg" border=0 alt="Get some help on this page" />
							</a>
						</td>
					</tr>
					<tr height="10"><td colspan="2">&nbsp;</td></tr>
					<tr>
						<td colspan="2">
							This is a function enabling you to import data definitions from the <b>XML files generated by</b>
							the data definer's helper <b>Import tool</b>. To find out more about that tool,
							contact the Data Dictionary's owner.<br/>
							You can import the file either from a URL or a location on your local file system.
						</td>
					</tr>
					
					<%
					if (mode.equals("FXV")){ %>
						<tr height="10"><td colspan="2"></td></tr>
						<tr>
							<td colspan="2">
								<font color="red">
									You have chosen to import fixed values (i.e. codes)<br/> for the
									<a href="data_element.jsp?mode=view&amp;delem_id=<%=delem_id%>"><%=elmName%></a> element!
								</font>
							</td>
						</tr><%
					}
					%>
				</table>
				
				<FORM NAME="Upload" ACTION="Import" METHOD="POST" ENCTYPE="multipart/form-data">
				
				<input type="hidden" name="type" value="<%=mode%>"></input>

				<table width="auto" cellspacing="0">
					<tr>
						<td align="left" style="padding-right:5">
							<input type="radio" name="fileORurl" value="file" checked="true"></input>&#160;File:</td>
						<td align="left">
							<input type="file" class="smalltext" name="file_input" size="40"/>
						</td>
					</tr>
					<tr>
						<td align="left" style="padding-right:5">
							<input type="radio" class="smalltext" name="fileORurl" value="url"></input>&#160;URL:
						</td>
						<td align="left">
							<input type="text" class="smalltext" name="url_input" size="52"></input>
						</td>
					</tr>
					<tr height="10"><td colspan="2"></td></tr>
					<tr>
						<td></td>
						<td align="left">
							<% if (user!=null){ %>									
								<input name="SUBMIT" type="button" class="mediumbuttonb" value="Import" onclick="submitForm()" onkeypress="submitForm()"></input>&#160;&#160;
							<%}%>
								<input name="RESET" type="reset" class="mediumbuttonb" value="Clear"></input>
						</td>
					</tr>
				</table>
			</FORM>	
		</TD>
	</tr>
</table>
</div>
</center>
</body>
</html>
