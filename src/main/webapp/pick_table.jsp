<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,eionet.util.sql.ConnectionUtil"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!private static final String ATTR_PREFIX = "attr_";%>
<%!private Vector selected=null;%>


<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");

    ServletContext ctx = getServletContext();

    Connection conn = null;
    DDUser user = SecurityUtil.getUser(request);

    try { // start the whole page try block

    conn = ConnectionUtil.getConnection();

    String short_name = request.getParameter("short_name");
    String idfier = request.getParameter("idfier");
    String full_name = request.getParameter("full_name");
    String definition = request.getParameter("definition");

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

    String searchType=request.getParameter("SearchType");

    String tableLink="";

    String sel = request.getParameter("selected");

    String backUrl = "search_table.jsp?ctx=popup&amp;selected=" + sel;
    String id=null;
    selected= new Vector();
    if (sel!=null && sel.length()>0){
        int i=sel.indexOf("|");
        while (i>0){
            id = sel.substring(0, i);
            sel = sel.substring(i+1);
            selected.add(id);
            i=sel.indexOf("|");
        }
    }

    if (sel==null) sel="";

    DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
    searchEngine.setUser(user);

    String srchType = request.getParameter("search_precision");
    String oper="=";
    if (srchType != null && srchType.equals("free"))
        oper=" match ";
    if (srchType != null && srchType.equals("substr"))
        oper=" like ";

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

    Vector dsTables = searchEngine.getDatasetTables(params, short_name, idfier, full_name, definition, oper);

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <%@ include file="headerinfo.jsp" %>
        <title>Meta</title>
        <script type="text/javascript">
        // <![CDATA[
            function pickTable(id, i, name) {
                if (window.confirm("You have selected to copy data elements and table structure from the '"+name+ "' table. \nPlease confirm or press cancel.")){
                    if (opener && !opener.closed) {
                        if (window.opener.pickTable(id, name)==true)  //window opener should have function pickTABLE with 2 params - tbl id & tbl name
                                                                 // and if it returns true, then the popup window is closed,
                                                                 // otherwise multiple selection is allowed
                            closeme();
                        hideRow(i);
                    } else {
                        alert("You have closed the main window.\n\nNo action will be taken.")
                    }
                }
            }
            function hideRow(i){
                var t = document.getElementById("tbl");
                var row = t.getElementsByTagName("TR")[i+1];
                row.style.display = "none";
            }
            function closeme(){
                window.close()
            }
        // ]]>
        </script>
    </head>

    <body class="popup">

    <div id="pagehead">
        <a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
        <div id="networktitle">Eionet</div>
        <div id="sitetitle">${ddfn:getProperty("app.displayName")}</div>
        <div id="sitetagline">This service is part of Reportnet</div>
    </div>

    <div id="workarea">
        <h1>Tables from latest versions of datasets in any status</h1>
        
        <div id="drop-operations">
            <ul>
                <li class="close"><a href="javascript:window.close();">Close</a></li>
                <li class="back"><a href="<%=backUrl%>">Back to search</a></li>
            </ul>
        </div>

        <% if (dsTables == null || dsTables.isEmpty()) { %>
            <p class='not-found'>No results matching the search criteria were found.</p>
        <% } else { %>
            <h2 class="results">Total results: <%=dsTables.size()%></h2>

            <table class="datatable results">
                <thead>
                    <tr>
                        <th>Short name</th>
                        <th>Full name</th>
                        <th>Dataset</th>
                        <th>Dataset status</th>
                    </tr>
                </thead>
                <tbody>
                     <%
                        int c=0;
                        DElemAttribute attr = null;

                        for (int i=0; i<dsTables.size(); i++) {
                            DsTable table = (DsTable)dsTables.get(i);
                            String table_id = table.getID();
                            String table_name = table.getShortName();
                            String ds_id = table.getDatasetID();
                            String ds_name = null;
                            if (ds_id!=null){
                                Dataset ds = (Dataset) searchEngine.getDataset(ds_id);
                                ds_name = ds.getShortName();
                            }

                            if (table_name == null) table_name = "unknown";
                            if (table_name.length() == 0) table_name = "empty";

                            if (ds_name == null || ds_name.length() == 0) ds_name = "unknown";

                            //String fullName = table.getName();
                            String tblName = "";

                            Vector attributes = searchEngine.getAttributes(table_id, "T");

                            for (int j=0; j<attributes.size(); j++){
                                attr = (DElemAttribute)attributes.get(j);
                                if (attr.getName().equalsIgnoreCase("Name"))
                                    tblName = attr.getValue();
                            }

                            tblName = tblName.length()>60 && tblName != null ? tblName.substring(0,60) + " ..." : tblName;

                            String zebraClass = (i + 1) % 2 != 0 ? "odd" : "even";
                            c++;
                        %>

                        <tr class="<%=zebraClass%>">
                            <td>
                                <a href="#" onclick="pickTable(<%=table_id%>, <%=c%>, '<%=table_name%>')">
                                    <%=Util.processForDisplay(table_name)%>
                                </a>
                            </td>
                            <td><%=Util.processForDisplay(tblName)%></td>
                            <td><%=Util.processForDisplay(ds_name)%></td>
                             <td>
                                <dd:datasetRegStatus value="<%=table.getDstStatus()%>" />
                            </td>
                        </tr>

                        <% } %>
                    </tbody>
                </table>
            <% } %>
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
