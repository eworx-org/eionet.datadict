<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private Vector elems=null;%>
<%!private ServletContext ctx=null;%>
<%!private Vector mAttributes=null;%>

<%@ include file="history.jsp" %>

<%!

private String getAttributeIdByName(String name){
	

		for (int i=0; i<mAttributes.size(); i++){
		DElemAttribute attr = (DElemAttribute)mAttributes.get(i);
        if (attr.getShortName().equalsIgnoreCase(name))
        	return attr.getID();
	}
        
    return null;
}
private String getAttributeValue(DataElement elem, String name){
	
	String id = getAttributeIdByName(name);
	if (elem == null) return null;
	DElemAttribute attr = elem.getAttributeById(id);
	if (attr == null) return null;
	return attr.getValue();
}

private boolean isIn(Vector elems, String id){
	
	for (int i=0; id!=null && i<elems.size(); i++){
		
		Object o = elems.get(i);
		Class oClass = o.getClass();
		if (oClass.getName().endsWith("Hashtable")) continue;
		
		DataElement elem = (DataElement)o;
        if (elem.getID().equalsIgnoreCase(id))
        	return true;
	}
        
    return false;
}

%>

<%

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

XDBApplication.getInstance(getServletContext());
AppUserIF user = SecurityUtil.getUser(request);
if (request.getMethod().equals("POST")){
	if (user == null){
		%>
			<html>
			<body>
				<h1>Error</h1><b>Not authorized to post any data!</b>
			</body>
			</html>
		<%
		return;
	}
}

String disabled = user == null ? "disabled" : "";

//check if table id is specified
String tableID = request.getParameter("table_id");
if (tableID == null || tableID.length()==0){ %>
	<b>Table ID is missing!</b> <%
	return;
}

String dsID = request.getParameter("ds_id");
if (dsID == null || dsID.length()==0){ %>
	<b>Dataset ID is missing!</b> <%
	return;
}

String dsIdf = request.getParameter("ds_idf");
if (dsIdf == null || dsIdf.length()==0){ %>
	<b>Dataset Identifier is missing!</b> <%
	return;
}

String dsName = request.getParameter("ds_name");
if (dsName == null || dsName.length()==0) dsName = "unknown";

ctx = getServletContext();

//handle the POST
if (request.getMethod().equals("POST")){
	
	if (!SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u")){ %>
		<b>Not allowed!</b> <%
	}
	
	Connection userConn = null;
	DsTableHandler tblHandler = null;
	DataElementHandler elmHandler = null;
	String link_elm = request.getParameter("link_elm");
	try{
		try{
			userConn = user.getConnection();
			if (link_elm!=null && link_elm.length()!=0){
				tblHandler = new DsTableHandler(userConn, request, ctx);
				tblHandler.setUser(user);
				tblHandler.execute();
			}
			else{
				elmHandler = new DataElementHandler(userConn, request, ctx);
				elmHandler.setUser(user);
				elmHandler.execute();
			}
		}
		catch (Exception e){
			if (tblHandler!=null) tblHandler.cleanup();
			if (elmHandler!=null) elmHandler.cleanup();
			%>
			<html><body><b><%=e.toString()%></b></body></html> <%
			return;
		}
	}
	finally{
		try { if (userConn!=null) userConn.close();
		} catch (SQLException e) {}
	}
	
	if (link_elm==null || link_elm.length()==0){
		
		System.out.println();
		
		String mode = request.getParameter("mode");	
		if (mode.equals("add") || mode.equals("copy")){
			response.sendRedirect("data_element.jsp?mode=view&delem_id=" + elmHandler.getLastInsertID());
		}
		else{
			String redirUrl = currentUrl;
			String newTblID = elmHandler.getNewTblID();
			if (newTblID!=null)
				redirUrl = "dstable.jsp?mode=view&table_id=" + newTblID;
			response.sendRedirect(redirUrl);
		}
		
		return;
	}
}


//handle the GET & elm linkage POST

Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

try { // start the whole page try block

conn = pool.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

DsTable dsTable = searchEngine.getDatasetTable(tableID);
if (dsTable == null){ %>
	<b>Table was not found!</b> <%
	return;
}


searchEngine.setUser(user);
elems = searchEngine.getDataElements(null, null, null, null, tableID);
mAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);

Dataset dataset = searchEngine.getDataset(dsTable.getDatasetID());
if (dataset==null){ %>
	<b>Could not associate the table with any dataset!</b><%
	return;
}

dsName = dataset.getShortName();

// JH180803
// if the table is not a working copy, its complex attributes cannot be edited.
// so here we set the falg it is a working copy or not
boolean isWorkingCopy = dsTable.isWorkingCopy();

String tableName = dsTable.getShortName();
if (tableName == null) tableName = "unknown";

boolean hasGIS = false;
for (int i=0; elems!=null && i<elems.size(); i++){
	DataElement elm = (DataElement)elems.get(i);
	if (elm.getGIS()!=null){
		hasGIS = true;
		break;
	}
}

int colCount = hasGIS ? 5 : 4;
	
%>

<html>
<head>
	<title>Meta</title>
	<meta http-equiv="Content-Type" content="text/html"/>
	<link href="eionet_new.css" rel="stylesheet" type="text/css"/>
</head>

<script language="javascript" src='script.js'></script>
<script language="javascript" src='dynamic_table.js'></script>
<script language="javascript" src='modal_dialog.js'></script>

<script language="javascript">
		function submitForm(mode){
			
			if (mode=="delete"){
				var b = confirm("This will remove the selected elements from this table. Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
				openNoYes("yesno_dialog.html", "Do you want to update the dataset definition's CheckInNo with this deletion?", delDialogReturn,100, 400);
				return;
			}
			
			if (mode=="add"  && document.forms["form1"].elements["idfier"].value==""){
				alert("Identifier cannot be empty!");
				return;
			}
			
			if (mode=="add" && hasWhiteSpace("idfier")){
				alert("Identifier cannot contain any white space!");
				return;
			}
			
			if (mode=="add" && !validForXMLTag(document.forms["form1"].elements["idfier"].value)){
				alert("Identifier must start with a letter or underscore to be valid for usage as an XML tag!");
				return;
			}
				
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function delDialogReturn(){
			var v = dialogWin.returnValue;
			if (v==null || v=="" || v=="cancel") return;
			
			document.forms["form1"].elements["upd_version"].value = v;
			deleteReady();
		}
		
		function deleteReady(){
			document.forms["form1"].elements["mode"].value = "delete";
			document.forms["form1"].submit();
		}
		
		function hasWhiteSpace(input_name){
			
			var elems = document.forms["form1"].elements;
			if (elems == null) return false;
			for (var i=0; i<elems.length; i++){
				var elem = elems[i];
				if (elem.name == input_name){
					var val = elem.value;
					if (val.indexOf(" ") != -1) return true;
				}
			}
			
			return false;
		}

		
		function saveChanges(){
			tbl_obj.insertNumbers("pos_");
			submitForm("edit_tblelems");
		}
		function clickLink(sUrl){
			if (getChanged()==1){
				if(!confirm("This link leads you to the next page, but you have changed the order of elements.\n Are you sure you want to loose the changes?"))
					return;
			}
			window.location=sUrl;
		}
		function start() {
			tbl_obj=new dynamic_table("tbl"); //create dynamic_table object
		}

		//call to dynamic table methods. Originated from buttons or click on tr.
		function sel_row(o){
			tbl_obj.selectRow(o);
		}
		function moveRowUp(){
			tbl_obj.moveup();
			setChanged();
		}
		function moveRowDown(){
			tbl_obj.movedown();
			setChanged();
		}
		function moveFirst(){
			tbl_obj.movefirst();
			setChanged();
		}
		function moveLast(){
			tbl_obj.movelast();
			setChanged();
		}
		function setChanged(){
			document.forms["form1"].elements["changed"].value = 1;
		}
		function getChanged(){
			return document.forms["form1"].elements["changed"].value;
		}
		
		var pickMode = "";
		function copyElem(){
			
			if (!validForXMLTag(document.forms["form1"].elements["idfier"].value)){
				alert("Identifier must start with a letter or underscore to be valid for usage as an XML tag!");
				return;
			}
			
			if (hasWhiteSpace("idfier")){
				alert("Identifier cannot contain any white space!");
				return;
			}

			pickMode = "copy";
			var url="search.jsp?ctx=popup&noncommon";
			wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
			if (window.focus){
				wAdd.focus();
			}
		}
		function linkElem(){
			
			pickMode = "link";
			var url="search.jsp?ctx=popup&common";
			wLink = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
			if (window.focus){
				wLink.focus();
			}
		}
		function pickElem(id){
			if (pickMode=="copy"){
				document.forms["form1"].copy_elem_id.value=id;
				document.forms["form1"].mode.value="copy";
				submitForm('copy');
			}
			else if (pickMode=="link"){
				document.forms["common_elm_link_form"].link_elm.value=id;
				document.forms["common_elm_link_form"].submit();
			}
			else
				alert("Unknown pick mode: " + pickMode);

			return true;
		}
		function goToAddForm(){
			
			if (!validForXMLTag(document.forms["form1"].elements["idfier"].value)){
				alert("Identifier must start with a letter or underscore to be valid for usage as an XML tag!");
				return;
			}
			
			if (hasWhiteSpace("idfier")){
				alert("Identifier cannot contain any white space!");
				return;
			}
			
			var url = "data_element.jsp?mode=add&table_id=<%=tableID%>&ds_id=<%=dsID%>";
			identifier = document.forms["form1"].elements["idfier"].value;
			elem_type = document.forms["form1"].elements["type"].value;
			
			url +="&idfier=" + identifier;
			url +="&type=" + elem_type;
			document.location.assign(url);
		}
		
		function validForXMLTag(str){
			
			if (str!=null && str.length>0){
				var ch = str.charCodeAt(0);
				if (ch==95 || (ch>=65 && ch<=90) || (ch>=97 && ch<=122))
					return true;
			}
			
			return false;
		}
</script>
	
<body onload="start()">
<%@ include file="header.htm" %>
<table border="0">
    <tr valign="top">
        <td nowrap="nowrap" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></p>
        </td>
        <td>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Table elements"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
<div style="margin-left:30">
			
<form name="form1" method="POST" action="tblelems.jsp">

	<!-- page title & the add new part -->
	
	<table width="500" cellspacing="0" cellpadding="0">
	
		<!-- title row & pagehelp -->
		
		<tr>
			<td>
				<span class="head00">
					Elements in
					<span class="title2">
						<a href="dstable.jsp?mode=view&amp;table_id=<%=tableID%>&amp;ds_id=<%=dsID%>&amp;ds_name=<%=dsName%>">
							<%=Util.replaceTags(tableName)%>
						</a>
					</span>
					table,
					<span class="title2">
						<a href="dataset.jsp?ds_id=<%=dsID%>&amp;mode=view">
							<%=Util.replaceTags(dsName)%>
						</a>
					</span>
					dataset.
				</span>
			</td>
			
			<td align="right">
				<a target="_blank" href="help.jsp?screen=table_elements&area=pagehelp" onclick="pop(this.href)">
					<img src="images/pagehelp.jpg" border="0" alt="Get some help on this page" />
				</a>
			</td>
		</tr>
		
		<tr height="10"><td colspan="2"></td></tr>
	
		<%
		// set the flag indicating if the top namespace is in use
		VersionManager verMan = new VersionManager(conn, searchEngine, user);
		String topWorkingUser = verMan.getWorkingUser(dsTable.getParentNs());
		boolean topFree = topWorkingUser==null ? true : false;
		
		boolean dsLatest = dataset==null ? false : verMan.isLatestDst(dataset.getID(), dataset.getIdentifier());
		
		boolean dstPrm = user!=null && SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + dsIdf, "u");
		if (user != null && topFree && dstPrm){ %>
		
			<tr>
				<td colspan="2">
					<table width="auto">
						<tr>
							<td align="right">
								<span class="barfont">Identifier:</span>
							</td>
							<td align="right">
								<a target="_blank" href="help.jsp?screen=dataset&area=identifier" onclick="pop(this.href)">
									<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
								</a>
							</td>
							<td style="padding-left:5">
								<input type="text" class="smalltext" width="10" name="idfier"/>
							</td>
							<td align="right">
								<input type="button" class="smallbutton" value="Add" onclick="goToAddForm()"
									   title="Define a new element into this table, give it the Identifier on the left."/>
							</td>
							<td align="right">
								<input type="button" class="smallbutton" value="Copy" onclick="copyElem()"
									   title="Define a new element into this table by copying an existing element in some other table, give it the Identifier on the left."/>
							</td>
							<td align="right">
								<input type="button" class="smallbutton" value="Link" onclick="linkElem()"
									   title="Link this table with a common element."/>
							</td>
						</tr>
						<tr>
							<td align="right"><span class="barfont">Type:</span></td>
							<td>
								<a target="_blank" href="help.jsp?screen=element&area=type" onclick="pop(this.href)">
									<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
								</a>
							</td>
							<td style="padding-left:5" colspan="4">
								<select name="type" class="small">
									<option selected value="CH2">Quantitative</option>
									<option value="CH1">Fixed values (codes)</option>
								</select>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<%
		}
		%>
		
		<tr height="10"><td colspan="2"></td></tr>
	</table>

		
	<!-- here's gonna be a table consisting of two columns -->
	<!-- the first column contains the table of elements,  -->
	<!-- the second one contains the ordering buttons      -->
	
	<table width="auto" cellspacing="0"  border="0">
		<tr>
		
			<!-- table of elements -->
			
			<td>
				<table width="auto" cellspacing="0" id="tbl">
				
					<thead>
				
					<!-- Delete & Save buttons -->
				
					<%
					boolean dispDelete = user!=null && topFree && dsLatest && dstPrm && elems.size()>0;
					boolean dispSave   = user!=null && topFree && dsLatest && elems.size()>1 && dstPrm;
					if (dispDelete || dispSave){ %>
						<tr>
							<td colspan="<%=String.valueOf(colCount)%>">
								<%
								if (dispDelete){ %>
									<input type="button" value="Remove selected" class="smallbutton" onclick="submitForm('delete')"/> <%
								}
								
								if (dispSave){ %>
									<input type="button" <%=disabled%> value="Save order" class="smallbutton" onclick="saveChanges()" title="save the new order of elements"/><%
								}
								%>
							</td>
						</tr><%
					}
					%>
					
					<!-- column headers -->

					<tr>
						<th align="right" style="padding-right:10">&nbsp;</th> <!-- checkboxes column -->
						
						<th align="left" style="padding-left:5;padding-right:10;border-left:0">
							<table width="100%">
								<tr>
									<td align="right" width="50%">
										<b>Short name</b>
									</td>
									<td align="left" width="50%">
										<a target="_blank" href="help.jsp?screen=dataset&area=short_name" onclick="pop(this.href)">
											<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
										</a>
									</td>
								</tr>
							</table>
						</th>
						
						<%
						if (hasGIS){ %>
							<th align="left" style="padding-right:10">
								<table width="100%">
									<tr>
										<td align="right" width="50%">
											<b>GIS</b>
										</td>
										<td align="left" width="50%">
											<a target="_blank" href="help.jsp?screen=element&area=GIS" onclick="pop(this.href)">
												<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
											</a>
										</td>
									</tr>
								</table>
							</th><%
						}
						%>
						
						<th align="left" style="padding-right:10">
							<table width="100%">
								<tr>
									<td align="right" width="50%">
										<b>Datatype</b>
									</td>
									<td align="left" width="50%">
										<a target="_blank" href="help.jsp?attrshn=Datatype&amp;attrtype=SIMPLE" onclick="pop(this.href)">
											<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
										</a>
									</td>
								</tr>
							</table>
						</th>
						
						<th align="left" style="padding-right:10; border-left:0; border-right:1px solid #FF9900">
							<table width="100%">
								<tr>
									<td align="right" width="50%">
										<b>Element type</b>
									</td>
									<td align="left" width="50%">
										<a target="_blank" href="help.jsp?screen=element&area=type" onclick="pop(this.href)">
											<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
										</a>
									</td>
								</tr>
							</table>
						</th>
					</tr>
					
					</thead>
					<tbody id="tbl_body">
					
					<%
					
					Hashtable types = new Hashtable();
					types.put("CH1", "Fixed values");
					types.put("CH2", "Quantitative");
					
					int maxPos = 0;
					
					// the elements display loop
					boolean hasMarkedElems = false;
					boolean hasForeignKeys = false;
					boolean hasCommonElms = false;
					for (int i=0; elems!=null && i<elems.size(); i++){
						
						DataElement elem = (DataElement)elems.get(i);
						
						String gis = elem.getGIS()!=null ?  gis = elem.getGIS() : "no GIS";
						String delem_name=elem.getShortName();
						if (delem_name.length() == 0) delem_name = "empty";
						String elemType = (String)types.get(elem.getType());
						String datatype = getAttributeValue(elem, "Datatype");		
						if (datatype == null) datatype="";
						String max_size = getAttributeValue(elem, "MaxSize");		
						if (max_size == null) max_size="";
						int posInTable = Integer.parseInt(elem.getPositionInTable());
						if (posInTable > maxPos) maxPos = posInTable;
						boolean elmCommon = elem.getNamespace()==null || elem.getNamespace().getID()==null;
						
						String elemLink = null;
						if (!elmCommon)
							elemLink = "data_element.jsp?mode=view&delem_id=" + elem.getID() + "&ds_id=" + dsID + "&table_id=" + tableID;
						else
							elemLink = "data_element.jsp?mode=view&delem_id=" + elem.getID();
						
						// see if the element is part of any foreign key relations
						Vector _fks = searchEngine.getFKRelationsElm(elem.getID(), dataset.getID());
						boolean fks = (_fks!=null && _fks.size()>0) ? true : false;
						
						String elemDefinition = elem.getAttributeValueByShortName("Definition");
						String workingUser = verMan.getWorkingUser(elem.getNamespace().getID(),
						    											elem.getIdentifier(), "elm");
						String ifDisabled = workingUser==null ? "" : "disabled";
						
						if (fks) hasForeignKeys = true;
						if (elmCommon) hasCommonElms = true;
					%>
						
						<!-- element row -->
						
						<tr id="<%=elem.getID()%>" onclick="tbl_obj.selectRow(this);" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
		
							<td align="right" style="padding-right:10" bgcolor="#f0f0f0">
								<%
								if (user!=null && dstPrm){
									if ((workingUser==null || elmCommon) && topFree && dsLatest){
										String name = elmCommon ? "linkelm_id" : "delem_id";
										%>
										<input onclick="tbl_obj.clickOtherObject();"
												type="checkbox"
												style="height:13;width:13" name="<%=name%>" value="<%=elem.getID()%>"/>
										<%
									}
								}
								%>
							</td>
					
							<td align="left" style="padding-left:5;padding-right:10">
								<%
								// red asterisk
								if (workingUser!=null){ %>
									<font title="<%=workingUser%>" color="red">* </font><%
									hasMarkedElems = true;
								}
									
								// short name
								if (elemDefinition!=null){ %>
									<a title="<%=elemDefinition%>" href="<%=elemLink%>"><%=Util.replaceTags(elem.getShortName())%></a><%
								}
								else { %>
									<a href="<%=elemLink%>"><%=Util.replaceTags(delem_name)%></a><%
								}
								
								// common elm indicator
								if (elmCommon){ %>
									<span class="commonelm"><sup>C</sup></span><%
								}
								
								// FK inidcator
								if (fks){ %>
									&nbsp;
									<span class="barfont">
										<a href="foreign_keys.jsp?delem_id=<%=elem.getID()%>&amp;delem_name=<%=elem.getShortName()%>&amp;ds_id=<%=dsID%>">
											<b><i>(FK)</i></b>
										</a>
									</span><%
								}
								%>
							</td>
							
							<%
							if (hasGIS){ %>
								<td align="left" style="padding-right:10">
									<%=gis%>
								</td><%
							}
							%>
							
							<td align="left" style="padding-right:10">
								<%=datatype%>
							</td>
							
							<td align="left" style="padding-right:10">
								<% if (elem.getType().equals("CH1")){ %>
									<a href="javascript:clickLink('fixed_values.jsp?mode=view&amp;delem_id=<%=elem.getID()%>&amp;delem_name=<%=elem.getShortName()%>')"><%=elemType%></a>
								<%} else{ %>
									<%=elemType%>
								<% } %>
							</td>
							
							<input type="hidden" name="pos_id" value="<%=elem.getID()%>" size="5">
							<input type="hidden" name="oldpos_<%=elem.getID()%>" value="<%=elem.getPositionInTable()%>" size="5">
							<input type="hidden" name="pos_<%=elem.getID()%>" value="0" size="5">
						</tr>
						<%
					} // end elements display loop
					%>
					
					<tr height="10">
						<td width="100%" class="barfont" colspan="<%=String.valueOf(colCount)%>"></td>
					</tr>
					
					<%
					// explanations about red asterisks, fks and c-signs
					if (user!=null && elems!=null && elems.size()>0 && hasMarkedElems){%>
						<tr height="10">
							<td width="100%" class="barfont" colspan="<%=String.valueOf(colCount)%>">
								(a red wildcard stands for checked-out element)
							</td>
						</tr><%
					}
					if (user!=null && elems!=null && elems.size()>0 && hasForeignKeys){%>
						<tr height="10">
							<td width="100%" class="barfont" colspan="<%=String.valueOf(colCount)%>">
								(the <u><b><i>(FK)</i></b></u> link indicates the element participating in a foreign key relation)
							</td>
						</tr><%
					}
					if (elems!=null && elems.size()>0 && hasCommonElms){%>
						<tr height="10">
							<td width="100%" class="barfont" colspan="<%=String.valueOf(colCount)%>">
								(the <span class="commonelm"><sup>C</sup></span> sign marks a common element)
							</td>
						</tr><%
					}
					%>
					
					</tbody>
				</table>
			</td>
			
			<!-- ordering buttons -->
			
			<%
			if (user!=null && topFree && dsLatest && elems.size()>1 && dstPrm){ %>
				<td align="left" style="padding-right:10" valign="center" height="10">
					<table cellspacing="2" cellpadding="2" border="0">
						<tr>
							<td>
								<a href="javascript:moveFirst()"><img src="images/move_first.gif" border="0" title="move selected row to top"/></a>
							</td>
						</tr>
						<tr>
							<td>
								<a href="javascript:moveRowUp()"><img src="images/move_up.gif" border="0" title="move selected row up"/></a>
							</td>
						</tr>
						<tr>
							<td>
								<img src="images/dot.gif"/>
							</td>
						</tr>
						<tr>
							<td>
								<a href="javascript:moveRowDown()"><img src="images/move_down.gif" border="0" title="move selected row down"/></a>			
							</td>
						<tr>
							<td>
								<a href="javascript:moveLast()"><img src="images/move_last.gif" border="0" title="move selected row last"/></a>			
							</td>
						</tr>
					</table>
				</td><%
			}
			%>
		</tr>
		
	</table>
	
	<%
	if (hasGIS){ %>
		<p>
		NB! Note that in table view this table is split into two:<br/>
		one for GIS elements (i.e. elements) and one for non-GIS elements<br/>
		(i.e. Metadata elements).
		</p><%
	}
	%>
		
	
	<input type="hidden" name="mode" value="delete"/>
	<input type="hidden" name="ds_id" value="<%=dsID%>"/>
	<input type="hidden" name="ds_name" value="<%=dsName%>"/>
	<input type="hidden" name="ds_idf" value="<%=dsIdf%>"/>
	<input type="hidden" name="table_id" value="<%=tableID%>"/>
	<input type="hidden" name="changed" value="0"/>
	<input type="hidden" name="copy_elem_id" value=""/>
	
	<input type="hidden" name="upd_version" value="false"/>
	
</form>

<form name="common_elm_link_form" method="POST" action="tblelems.jsp">
	<input type="hidden" name="link_elm" value=""/>
	<input type="hidden" name="mode" value="add"/>
	<input type="hidden" name="table_id" value="<%=tableID%>"/>
	<input type="hidden" name="ds_id" value="<%=dsID%>"/>
	<input type="hidden" name="ds_name" value="<%=dsName%>"/>
	<input type="hidden" name="ds_idf" value="<%=dsIdf%>"/>
	<input type="hidden" name="elmpos" value="<%=maxPos+1%>"/>
</form>

</div>
</body>
</html>

<%
// end the whole page try block
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e) {}
}
%>
