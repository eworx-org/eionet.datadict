<%@page contentType="text/html" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!static int iPageLen=0;%>
<%!final static String TYPE_SEARCH="SEARCH";%>
<%!final static String attrCommonElms="common_elms";%>
<%!final static String oSearchUrlAttrName="elms_search_url";%>

<%!private int reqno = 0;%>

<%@ include file="history.jsp" %>

<%!class c_SearchResultEntry implements Comparable {
    public String oID;
    public String oType;
    public String oShortName;
    public String oDsName;
    public String oTblName;
    public String oNs;
    public String oDsIdf;
    public String topWorkingUser = null;
    public String status = null;
    public String checkInNo = null;

    private String oCompStr=null;
    private int iO=0;
    
    public c_SearchResultEntry(String _oID,String _oType,String _oShortName,String _oDsName,String _oTblName, String _oNs) {
	    
            oID	= _oID==null ? "" : _oID;
            oType  = _oType==null ? "" : _oType;
            oShortName	= _oShortName==null ? "" : _oShortName;
            oDsName	= _oDsName==null ? "" : _oDsName;
            oTblName= _oTblName==null ? "" : _oTblName;
            oNs	= _oNs==null ? "" : _oNs;
    		
	};
    
    public void setComp(int i,int o) {
        switch(i) {
            case 2: oCompStr=oType; break;
            case 3: oCompStr=status; break;
            default: oCompStr=oShortName; break;
            }
        iO=o;
        }
    
    public String toString() {
        return oCompStr;
    }

    public int compareTo(Object oC1) {
        return iO*oCompStr.compareToIgnoreCase(oC1.toString());
    }
}%>

<%!class c_SearchResultSet {
    private boolean isSorted=false;
    private int iSortColumn=0;
    private int iSortOrder=0;
    public boolean isAuth = false;

    public Vector oElements;
    public boolean SortByColumn(Integer oCol,Integer oOrder) {
        if ((iSortColumn!=oCol.intValue()) || (iSortOrder!=oOrder.intValue())) {
            for(int i=0; i<oElements.size(); i++) {
                c_SearchResultEntry oEntry=(c_SearchResultEntry)oElements.elementAt(i); 
                oEntry.setComp(oCol.intValue(),oOrder.intValue());
            }
            Collections.sort(oElements);
            return true;
        }
        return false;
    }
}%>

<%
	// The following if block tries to identify if a login has happened in which
	// case it will redirect the response to the query string in session. This
	// happens regardless of weather it's a sorting request or search request.
	AppUserIF user = SecurityUtil.getUser(request);
	c_SearchResultSet rs = (c_SearchResultSet)session.getAttribute(attrCommonElms);
	if (rs!=null){
		if (rs.isAuth && user==null || !rs.isAuth && user!=null){
			session.removeAttribute(attrCommonElms);
			response.sendRedirect((String)session.getAttribute(oSearchUrlAttrName));
		}
	}

	// get search type
	String searchType=request.getParameter("SearchType");
	
	// see if popup
	boolean popup = request.getParameter("ctx")!=null && request.getParameter("ctx").equals("popup");
	
	// get sorting info
	Integer oSortCol=null;
    Integer oSortOrder=null;
    try {
    	oSortCol=new Integer(request.getParameter("sort_column"));
        oSortOrder=new Integer(request.getParameter("sort_order"));
    }
    catch(Exception e){
    	oSortCol=null;
        oSortOrder=null;
    }
    
    // declare some global stuff
    Connection conn = null;
    Vector dataElements = null;
    DDSearchEngine searchEngine = null;
	
    // start the whole page try block
	try {
	
		// if in search mode
		if (searchType != null && searchType.equals(TYPE_SEARCH)){
			
			// remove the cached result set
			session.removeAttribute(attrCommonElms);
			
	       	// get the DB connection and set up search engine
			ServletContext ctx = getServletContext();
			conn = XDBApplication.getInstance(ctx).getDBPool().getConnection();
			searchEngine = new DDSearchEngine(conn, "", ctx);
			searchEngine.setUser(user);
		
			// get statical search parameters
			String type = request.getParameter("type");
			String short_name = request.getParameter("short_name");
			String idfier = request.getParameter("idfier");
		
			String searchPrecision = request.getParameter("search_precision");
			String oper="=";
			if (searchPrecision != null && searchPrecision.equals("free"))
				oper=" match ";
			if (searchPrecision != null && searchPrecision.equals("substr"))
				oper=" like ";
			
			String parWrkCopies = request.getParameter("wrk_copies");
			boolean wrkCopies = (parWrkCopies!=null && parWrkCopies.equals("true")) ? true : false;
		
			// get dynamical search parameters
			Vector params = new Vector();	
			Enumeration parNames = request.getParameterNames();
			while (parNames.hasMoreElements()){
				String parName = (String)parNames.nextElement();
				if (!parName.startsWith(ATTR_PREFIX))
					continue;
			
				String parValue = request.getParameter(parName);
				if (parValue.length()==0)
					continue;
				
				DDSearchParameter param =
					new DDSearchParameter(parName.substring(ATTR_PREFIX.length()), null, oper, "=");
			
	            if (oper!= null && oper.trim().equalsIgnoreCase("like"))
					param.addValue("'%" + parValue + "%'");
				else
					param.addValue("'" + parValue + "'");
				params.add(param);
			}
			
			// all set up for search, do it
			dataElements = searchEngine.getCommonElements(params, type, short_name, idfier, wrkCopies, oper);
			
		} // end if in search mode

%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet_new.css">
    <script language="JavaScript" src='script.js'></script>
	<script language="JavaScript">
    	function showSortedList(clmn,ordr) {
    		if ((document.forms["sort_form"].elements["sort_column"].value != clmn)
       			|| (document.forms["sort_form"].elements["sort_order"].value != ordr)) {
        		document.forms["sort_form"].elements["sort_column"].value=clmn;
		    	document.forms["sort_form"].elements["sort_order"].value=ordr;
        		document.forms["sort_form"].submit();
    		}
		}
		
		<%
		if (popup){ %>
			function pickElem(elmID, rowIndex){
				// make sure the opener exists and is not closed
				if (opener && !opener.closed) {
					// if the opener has pickElem(elmID) function at it returns true, close this popup
					// else don't close it (multiple selection might be wanted)
					if (window.opener.pickElem(elmID)==true)  
						closeme();
					else
						hideRow(rowIndex);
				}
				else 
					alert("You have closed the main window!\n\nNo action will be taken.")
			}
			function hideRow(i){
				var t = document.getElementById("tbl");
				var row = t.getElementsByTagName("TR")[i+1];
				row.style.display = "none";
			}
			function closeme(){
				window.close()
			}
			<%
		}
		%>
    </script>
</head>

<%
if (popup){ %>	

	<body class="popup">
	
	<div class="popuphead">
		<h1>Data Dictionary</h1>
		<hr/>
		<div align="right">
			<form name="close" action="javascript:window.close()">
				<input type="submit" class="smallbutton" value="Close"/>
			</form>
		</div>
	</div>
	<div><%
}
else{ %>
	<body>
	<%@ include file="header.htm" %>
	<table border="0">
	    <tr valign="top">
	        <td nowrap="true" width="125">
	            <p><center>
	                <%@ include file="menu.jsp" %>
	            </center></p>
	        </td>
	        <td>
	            <jsp:include page="location.jsp" flush='true'>
	                <jsp:param name="name" value="Search results"/>
	                <jsp:param name="back" value="true"/>
	            </jsp:include>
	            
				<div style="margin-left:30"><%
}
            
			if (searchType != null && searchType.equals(TYPE_SEARCH)){
        	    if (dataElements == null || dataElements.size()==0){
	        	    %>
	            	<b>No results found!</b>
	            	<%
	    	        if (user==null){ %>
	    	        	<br/>
	    	        		This might be due to fact that you have not been authorized and there are<br/>
	    	        		no datasets at the moment ready to be published for non-authorized users.<br/>
	    	        		Please go to the <a href="datasets.jsp?SearchType=SEARCH">list of datasets</a>
							to see which of them are in which status!
	    	        	<br/><%
    	        	}
    	        	%>
	            	</div></TD></TR></table></body></html>
	            	<%
	            	return;
            	}
            }
            %>
            
			<span class="head00">Search results</span><br/><br/>
		
			<table width="700" cellspacing="0" border="0" cellpadding="2">
			
			<%
			boolean userHasEditRights = user!=null &&
										(SecurityUtil.hasPerm(user.getUserName(), "/elements" , "u") ||
										SecurityUtil.hasPerm(user.getUserName(), "/elements" , "i"));
			int colSpan = userHasEditRights ? 4 : 3;
			%>
		
			<!-- the tab row -->
		
			<tr>
				<td align="right" colspan="<%=colSpan%>">
					<a target="_blank" href="help.jsp?screen=elements&area=pagehelp" onclick="pop(this.href)">
						<img src="images/pagehelp.jpg" border=0 alt="Get some help on this page">
					</a><br/>
				</td>
			</tr>
			
			<!-- the table itself -->
		
			<tr>
				<th width="35%">
					<jsp:include page="thsortable.jsp" flush="true">
			            <jsp:param name="title" value="Element"/>
			            <jsp:param name="mapName" value="Element"/>
			            <jsp:param name="sortColNr" value="1"/>
			            <jsp:param name="help" value="help.jsp?screen=elements&area=element"/>
			        </jsp:include>
				</th>
				<th width="20%">
					<jsp:include page="thsortable.jsp" flush="true">
			            <jsp:param name="title" value="Type"/>
			            <jsp:param name="mapName" value="Type"/>
			            <jsp:param name="sortColNr" value="2"/>
			            <jsp:param name="help" value="help.jsp?screen=element&area=type"/>
			        </jsp:include>
				</th>
				<%
				if (userHasEditRights){ %>
					<th width="20%">
						<table width="100%">
							<tr>
								<td align="right" width="50%">
									<b>CheckInNo</b>
								</td>
								<td align="left" width="50%">
									<a target="_blank" href="help.jsp?screen=dataset&area=check_in_no" onclick="pop(this.href)">
										<img border="0" src="images/icon_questionmark.jpg" width="16" height="16"/>
									</a>
								</td>
							</tr>
						</table>
					</th><%
				}
				%>
				<th width="25%" style="border-right: 1 solid #FF9900">
					<jsp:include page="thsortable.jsp" flush="true">
			            <jsp:param name="title" value="Status"/>
			            <jsp:param name="mapName" value="Status"/>
			            <jsp:param name="sortColNr" value="3"/>
			            <jsp:param name="help" value="help.jsp?screen=dataset&area=regstatus"/>
			        </jsp:include>
				</th>
			</tr>
				
			<%
			int displayed = 0;
			if (searchType != null && searchType.equals(TYPE_SEARCH)){

				// init the VersionManager
				VersionManager verMan = new VersionManager(conn, searchEngine, user);
			
				// set up the search result set
				c_SearchResultSet oResultSet=new c_SearchResultSet();
				oResultSet.isAuth = user!=null;
	        	oResultSet.oElements=new Vector(); 
	        	session.setAttribute(attrCommonElms,oResultSet);
	        	
	        	// search results processing loop
	        	for (int i=0; i<dataElements.size(); i++){
		        	
					// set up the element
		        	DataElement dataElement = (DataElement)dataElements.get(i);
					
					String delem_id = dataElement.getID();
					String delem_name = dataElement.getShortName();
					if (delem_name == null) delem_name = "unknown";
					if (delem_name.length() == 0) delem_name = "empty";
					String delem_type = dataElement.getType();
					if (delem_type == null) delem_type = "unknown";
					
					String displayType = "unknown";
					if (delem_type.equals("CH1")){
						displayType = "Fixed values";
					}
					else if (delem_type.equals("CH2")){
						displayType = "Quantitative";
					}
					
					String status = dataElement.getStatus();
					String checkInNo = dataElement.getVersion();
					
					String workingUser = verMan.getWorkingUser(null, dataElement.getIdentifier(), "elm");
					
					c_SearchResultEntry oEntry = new c_SearchResultEntry(delem_id,
               															 displayType,
                														 delem_name,
                														 null,
                														 null,
                														 null);                															 
					oEntry.status = status;
					oEntry.checkInNo = checkInNo;
					oEntry.topWorkingUser = workingUser;
					
					oResultSet.oElements.add(oEntry);
					String styleClass  = i % 2 != 0 ? "search_result_odd" : "search_result";
					
					%>
				
					<tr>
						<td width="35%" class="<%=styleClass%>">
							<%
							if (!popup){ %>
								<a href="data_element.jsp?delem_id=<%=delem_id%>&amp;type=<%=delem_type%>&amp;mode=view">
									<%=Util.replaceTags(delem_name)%>
								</a><%
							}
							else{ %>
								<a href="javascript:pickElem(<%=dataElement.getID()%>, <%=displayed+1%>)">
		    						<%=Util.replaceTags(delem_name)%>
		    					</a><%
	    					}
	    					
	    					if (userHasEditRights && workingUser!=null){ %>
	    						<font title="<%=workingUser%>" color="red">*</font><%
	    					}
	    					
							%>
						</td>
						<td width="20%" class="<%=styleClass%>">
							<%=displayType%>
						</td>
						<%
						if (userHasEditRights){ %>
							<td width="20%" class="<%=styleClass%>">
								<%=checkInNo%>
							</td><%
						}
						%>
						<td width="25%" class="<%=styleClass%>" style="border-right: 1 solid #C0C0C0">
							<%=status%>
						</td>
					</tr><%
					displayed++;
				}
				%>
               	<tr><td colspan="<%=colSpan%>">&nbsp;</td></tr>
				<tr><td colspan="<%=colSpan%>">Total results: <%=dataElements.size()%></td></tr><%
			}
			else{
				// No search - return from another result set or a total stranger...
                c_SearchResultSet oResultSet=(c_SearchResultSet)session.getAttribute(attrCommonElms);
                if (oResultSet==null) {
                    %><P>This page has experienced a time-out. Try searching again.<%
                }
                else {
                    if ((oSortCol!=null) && (oSortOrder!=null))
                        oResultSet.SortByColumn(oSortCol,oSortOrder);
                    
                    c_SearchResultEntry oEntry;
                    for (int i=0;i<oResultSet.oElements.size();i++) {
                    oEntry=(c_SearchResultEntry)oResultSet.oElements.elementAt(i);
                    
                    String styleClass  = i % 2 != 0 ? "search_result_odd" : "search_result";

                    %>
						<tr>
							<td width="35%" class="<%=styleClass%>">
								<%
								if (!popup){ %>
									<a href="data_element.jsp?delem_id=<%=oEntry.oID%>&amp;type=<%=oEntry.oType%>&amp;mode=view">
										<%=Util.replaceTags(oEntry.oShortName)%>
									</a><%
								}
								else{ %>
									<a href="javascript:pickElem(<%=oEntry.oID%>, <%=displayed+1%>)">
			    						<%=Util.replaceTags(oEntry.oShortName)%>
			    					</a><%
								}
								
								if (userHasEditRights && oEntry.topWorkingUser!=null){ %>
									<font title="<%=oEntry.topWorkingUser%>" color="red">*</font><%
								}
								%>
							</td>						
							<td width="20%" class="<%=styleClass%>">
								<%=oEntry.oType%>
							</td>
							<%
							if (userHasEditRights){ %>
								<td width="20%" class="<%=styleClass%>">
									<%=oEntry.checkInNo%>
								</td><%
							}
							%>
							<td width="25%" class="<%=styleClass%>" style="border-right: 1 solid #C0C0C0">
								<%=oEntry.status%>
							</td>
						</tr>
						<%
						displayed++;
                	}
                	%>
                	<tr><td colspan="<%=colSpan%>">&nbsp;</td></tr>
					<tr><td colspan="<%=colSpan%>">Total results: <%=oResultSet.oElements.size()%></td></tr><%
                }

            }
			%>
			
		</table>
		
		<form name="sort_form" action="common_elms.jsp" method="GET">
			<input name='sort_column' type='hidden' value='<%=(oSortCol==null)? "":oSortCol.toString()%>'/>
        	<input name='sort_order' type='hidden' value='<%=(oSortOrder==null)? "":oSortOrder.toString()%>'/>
			<input name='SearchType' type='hidden' value='NoSearch'/>
		</form>
		
			</div>
			
		</TD>
</TR>
</table>
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