<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.util.sql.ConnectionUtil,eionet.util.*"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!final static String POPUP="popup";%>


<%!

private Vector attrs = null;
private Vector def_attrs = null;
private Vector attr_ids = null;
ServletContext ctx = null;
private String sel_attr = null;
private Hashtable inputAttributes=null;

private String getAttributeIdByName(String name){

    for (int i=0; i<attrs.size(); i++){
        DElemAttribute attr = (DElemAttribute)attrs.get(i);
        if (attr.getName().equalsIgnoreCase(name))
            return attr.getID();
    }

    return null;
}

private String getAttributeNameById(String id){

    for (int i=0; i<attrs.size(); i++){
        DElemAttribute attr = (DElemAttribute)attrs.get(i);
        if (attr.getID().equals(id))
            return attr.getShortName();
    }

    return null;
}

private String setDefaultAttrs(String name){

    String id = getAttributeIdByName(name);
    if (id!=null)
        def_attrs.add(id);

    return null;
}
%>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");

    DDUser user = SecurityUtil.getUser(request);

    ctx = getServletContext();

    Connection conn = null;

    try { // start the whole page try block

    conn = ConnectionUtil.getConnection();

    DDSearchEngine searchEngine = new DDSearchEngine(conn, "");

    attrs = searchEngine.getDElemAttributes();
    if (attrs == null) attrs = new Vector();

    attr_ids = new Vector();
    def_attrs = new Vector();

    setDefaultAttrs("Name");
    setDefaultAttrs("Definition");
    setDefaultAttrs("Keywords");

    String attrID = null;
    String attrValue = null;
    String attrName = null;
    StringBuffer collect_attrs = new StringBuffer();
    HashSet displayedCriteria = new HashSet();

    String sel_attr = request.getParameter("sel_attr");
    String sel_type = request.getParameter("sel_type");
    String short_name = request.getParameter("short_name");
    String idfier = request.getParameter("idfier");
    String full_name = request.getParameter("full_name");
    String definition = request.getParameter("definition");
    String search_precision = request.getParameter("search_precision");
    String contextParam = request.getParameter("ctx");


    String submitForm=null;
    if (contextParam != null && contextParam.equals(POPUP))
        submitForm = "pick_table.jsp";
    else
        submitForm = "search_results_tbl.jsp";

    if (sel_attr == null) sel_attr="";
    if (sel_type == null) sel_type="";
    if (short_name == null) short_name="";
    if (idfier == null) idfier="";
    if (full_name == null) full_name="";
    if (definition == null) definition="";
    if (search_precision == null) search_precision="substr";

    ///get inserted attributes
    String input_attr;
    inputAttributes = new Hashtable();
    for (int i=0; i<attrs.size(); i++){
        DElemAttribute attribute = (DElemAttribute)attrs.get(i);
        String attr_id = attribute.getID();

        input_attr = request.getParameter("attr_" + attr_id);
        if (input_attr!=null){
            inputAttributes.put(attr_id, input_attr);
            attr_ids.add(attr_id);
        }
    }
    if (contextParam == null || !contextParam.equals(POPUP)){
        %><%@ include file="history.jsp"%><%
    }

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Search tables - Data Dictionary</title>
    <script type="text/javascript">
    // <![CDATA[
        attrWindow=null;

        function submitForm(action){

            document.forms["form1"].action=action;
            document.forms["form1"].submit();
        }

        function selAttr(id, type){
            document.forms["form1"].sel_attr.value=id;
            document.forms["form1"].sel_type.value=type;
            submitForm('search_table.jsp');

        }

        function onLoad(){
            <%
                if (search_precision != null){
                %>
                    var sPrecision = '<%=search_precision%>';
                    var o = document.forms["form1"].search_precision;
                    for (i=0; o!=null && i<o.length; i++){
                        if (o[i].value == sPrecision){
                            o[i].checked = true;
                            break;
                        }
                    }
                <%
                }
            %>
        }
    // ]]>
    </script>
</head>

<%
boolean isPopup = (contextParam == null || !contextParam.equals(POPUP))==false;
if (!isPopup){
    %>
    <body onload="onLoad()">
    <div id="container">
    <jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Search tables"/>
        <jsp:param name="helpscreen" value="search_table"/>
    </jsp:include>
    <c:set var="currentSection" value="tables" />
    <%@ include file="/pages/common/navigation.jsp" %><%
}
else {
    %>
    <body class="popup" onload="onLoad()">
    <div id="pagehead">
        <a href="/"><img src="images/eea-print-logo.gif" alt="Logo" id="logo" /></a>
        <div id="networktitle">Eionet</div>
        <div id="sitetitle">${ddfn:getProperty("app.displayName")}</div>
        <div id="sitetagline">This service is part of Reportnet</div>
    </div>
    <%
}
%>

<div id="workarea">
    <h1>Search tables</h1>

    <%
      if (isPopup) {
          %>
          <div id="drop-operations">
            <ul>
                <li class="close"><a href="javascript:window.close();">Close</a></li>
                <li class="help"><a class="helpButton" href="help.jsp?screen=search_table&amp;area=pagehelp" title="Get some help on this page">Page help</a></li>
            </ul>
    </div><%
      }
      %>

        <br/>
        <form id="form1" action="search_results_tbl.jsp" method="get">
            <div id="filters">
                <table class="filter">
                    <tr>
                        <td class="label">
                            <label for="txtShortName">Short name</label>
                            <a class="helpButton" href="help.jsp?screen=dataset&amp;area=short_name"></a>
                        </td>
                        <td class="input">
                            <input type="text" class="smalltext" size="59" name="short_name" value="<%=Util.processForDisplay(short_name)%>" id="txtShortName" />
                        </td>
                    </tr>

                    <tr>
                        <td class="label">
                            <label for="txtIdentifier">Identifier</label>
                            <a class="helpButton" href="help.jsp?screen=dataset&amp;area=identifier"></a>
                        </td>
                        <td class="input">
                            <input type="text" class="smalltext" size="59" name="idfier" value="<%=idfier%>" id="txtIdentifier" />
                        </td>
                    </tr>

                    <!--tr align="top">
                        <td align="right" style="padding-right:10">
                            <a href="javascript:openFulltName()"><span class="help">?</span></a>&nbsp;
                            <span class="mainfont"><b>Full name</b></span>&nbsp;&nbsp;
                        </td>
                        <td colspan="2">
                            <input type="text" class="smalltext" size="40" name="full_name" value="<%=full_name%>"/>
                        </td>
                    </tr>
                    <tr align="top">
                        <td align="right" style="padding-right:10">
                            <a href="javascript:openDefinition()"><span class="help">?</span></a>&nbsp;
                            <span class="mainfont"><b>Definition</b></span>&nbsp;&nbsp;
                        </td>
                        <td colspan="2">
                            <input type="text" class="smalltext" size="40" name="definition" value="<%=definition%>"/>
                        </td>
                    </tr-->

                    <%
                    // get default attributes, which are always on the page (defined above)
                    if (def_attrs!=null) {
                        for (int i=0; i < def_attrs.size(); i++){
                            attrID = (String)def_attrs.get(i);
                            attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";

                            attrName = getAttributeNameById(attrID);

                            if (inputAttributes.containsKey(attrID)) inputAttributes.remove(attrID);

                            if (attrID!=null){
                                collect_attrs.append(attrID + "|");
                                displayedCriteria.add(attrID);
                                %>
                                <tr>
                                    <td class="label">
                                        <label for="txtFilterAttr_<%=attrID%>"><%=Util.processForDisplay(attrName)%></label>
                                        <a class="helpButton" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE"></a>
                                    </td>
                                    <td class="input">
                                        <input type="text" class="smalltext" name="attr_<%=attrID%>" size="59"  value="<%=Util.processForDisplay(attrValue, true)%>" id="txtFilterAttr_<%=attrID%>" />
                                    </td>
                                </tr>
                                <%
                            }
                        }
                    }
                    // get attributes selected from picked list (get the ids from url)
                    if (attr_ids!=null){
                        for (int i=0; i < attr_ids.size(); i++){
                            attrID = (String)attr_ids.get(i);

                            if (!inputAttributes.containsKey(attrID)) continue;
                            if (sel_type.equals("remove") && attrID.equals(sel_attr)) continue;

                            attrName = getAttributeNameById(attrID);

                            attrValue = inputAttributes.containsKey(attrID) ? (String)inputAttributes.get(attrID) : "";
                            if (attrValue == null) attrValue="";
                            collect_attrs.append(attrID + "|");
                            displayedCriteria.add(attrID);
                            %>
                            <tr>
                                <td class="label">
                                    <label for="txtAddedAttr_<%=attrID%>"><%=Util.processForDisplay(attrName)%></label>
                                    <a class="helpButton" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE"></a>
                                </td>
                                <td class="input">
                                    <input type="text" class="smalltext" name="attr_<%=attrID%>" size="59"  value="<%=Util.processForDisplay(attrValue, true)%>" id="txtAddedAttr_<%=attrID%>" />
                                    <a class="deleteButton" href="javascript:selAttr(<%=attrID%>, 'remove');" title="Remove attribute from search criteria"></a>
                                </td>
                            </tr>
                            <%
                        }
                    }
                    // add the last selection
                    if (sel_type!=null && sel_attr!=null){
                        if (sel_type.equals("add")){
                            attrID = sel_attr;
                            collect_attrs.append(attrID + "|");
                            displayedCriteria.add(attrID);
                            attrName = getAttributeNameById(attrID);
                            %>
                            <tr>
                                <td class="label">
                                    <label for="txtAddedAttr_<%=attrID%>"><%=Util.processForDisplay(attrName)%></label>
                                    <a class="helpButton" href="help.jsp?attrid=<%=attrID%>&amp;attrtype=SIMPLE"></a>
                                </td>
                                <td class="input">
                                    <input type="text" class="smalltext" name="attr_<%=attrID%>" size="59" value="" id="txtAddedAttr_<%=attrID%>" />
                                    <a class="deleteButton" href="javascript:selAttr(<%=attrID%>, 'remove');" title="Remove attribute from search criteria"></a>
                                </td>
                            </tr>
                            <%
                        }
                    }

                    Vector addCriteria = new Vector();
                    for (int i=0; attrs!=null && i<attrs.size(); i++) {

                        DElemAttribute attribute = (DElemAttribute)attrs.get(i);
                        if (!attribute.displayFor("TBL"))
                            continue;

                        if (!displayedCriteria.contains(attribute.getID())){
                            Hashtable hash = new Hashtable();
                            hash.put("id", attribute.getID());
                            hash.put("name", attribute.getShortName());
                            addCriteria.add(hash);
                        }
                    }

                    if (addCriteria.size()>0 && !isPopup) {
                        %>
                        <tr>
                            <td></td>
                            <td class="input">
                                <select name="add_criteria" id="add_criteria" onchange="selAttr(this.options[this.selectedIndex].value, 'add')">
                                    <option value="">Add criteria</option>
                                    <%
                                    for (int i=0; i<addCriteria.size(); i++) {
                                        Hashtable hash = (Hashtable)addCriteria.get(i);
                                    %>
                                        <option value="<%=hash.get("id")%>"><%=hash.get("name")%></option><%
                                    }
                                    %>
                                </select>
                            </td>
                        </tr><%
                    }
                    %>

                    <tr>
                        <td class="label">Search method</td>
                        <td class="input bordered">
                            <input type="radio" name="search_precision" id="ssubstr" value="substr" checked="checked" /><label for="ssubstr">Substring search</label>
                            <input type="radio" name="search_precision" id="sexact" value="exact" /><label for="sexact">Exact search</label>
                        </td>
                    </tr>
                </table>

                <p class="actions">
                    <input class="mediumbuttonb" type="submit" value="Search" onclick="submitForm('<%=submitForm%>')"/>
                    <input class="mediumbuttonb" type="reset" value="Reset"/>
                </p>

                <div style="display:none">
                    <input type="hidden" name="sel_attr" value=""/>
                    <input type="hidden" name="sel_type" value=""/>
                    <input type="hidden" name="type" value="TBL"/>
                    <!-- collect all the attributes already used in criterias -->
                    <input type="hidden" name="collect_attrs" value="<%=Util.processForDisplay(collect_attrs.toString(), true)%>"/>
                </div>
            </div>
        </form>
    </div> <!-- workarea -->
    <% if (!isPopup){ %>
        </div> <!-- container -->
        <%@ include file="footer.jsp" %>
    <% } %>
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
