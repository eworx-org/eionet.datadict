<%@page contentType="text/html" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.util.*,com.tee.xmlserver.*"%>

<%

ServletContext ctx = getServletContext();
XDBApplication xdbapp = XDBApplication.getInstance(ctx);
DBPoolIF pool = xdbapp.getDBPool();
Connection conn = pool.getConnection();

AppUserIF user = SecurityUtil.getUser(request);
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);	
searchEngine.setUser(user);

Vector datasets = searchEngine.getDatasets(null, null, null, null, null, false);
Vector releasedDatasets = new Vector();
for (int i=0; datasets!=null && i<datasets.size(); i++){
	Dataset dst = (Dataset)datasets.get(i);
	String status = dst.getStatus();
	if (status!=null && status.equals("Released"))
		releasedDatasets.add(dst);
}

%>

<table border="0" width="100%" cellspacing="0" cellpadding="3" bordercolorlight="#C0C0C0" bordercolordark="#C0C0C0" style="border: 1 solid #FF9900">
		<tr>
			<td width="100%" valign="top" align="left" colspan="3">
				<b>Released data definitions</b>
			</td>
		</tr>
		
		<%
		for (int i=0; i<releasedDatasets.size(); i++){
			Dataset dst = (Dataset)releasedDatasets.get(i);
			
			String name = dst.getName();
			if (name==null) name = dst.getShortName();
			if (name==null) name = dst.getIdentifier();
			
			String date = dst.getDate();
			date = date==null ? "" : eionet.util.Util.releasedDate(Long.parseLong(date));
			%>
			<tr>				
				<td width="70%" valign="top" align="left">
					<a href="dataset.jsp?mode=view&amp;ds_id=<%=dst.getID()%>">
						<%=name%>
					</a>
				</td>
				<td width="23%" valign="top" align="left">
					<%=date%>
				</td>
				<td width="7%" valign="top" align="center">
					<a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;obj_id=<%=dst.getID()%>">
						<img src="images/icon_pdf.jpg" border="0" valign="middle" width="17" height="18">
					</a>
				</td>
			</tr>
			<%
		}
		
		if (releasedDatasets.size()==0){
			%>
			<tr>
				<td width="100%" valign="top" align="left" colspan="3">
					No released dataset definitions found at the moment!
				</td>
			</tr>
			<%
		}
		else{
			%>
			<tr>
				<td width="100%" valign="top" align="right" colspan="3">
					[<a href="datasets.jsp?SearchType=SEARCH">More...</a>]
				</td>
			</tr>
			<%
		}
		%>
		
</table>