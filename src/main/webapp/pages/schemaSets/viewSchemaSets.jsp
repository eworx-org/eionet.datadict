<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Schema sets">

    <stripes:layout-component name="contents">

        <h1>Schema sets</h1>
        
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><a href="${pageContext.request.contextPath}/schemaSet.action?add=">Add schema set</a></li>
            </ul>
        </div>

        <stripes:form action="/schemaSets.action" method="post">
            <display:table name="actionBean.schemaSetsResult" class="sortable" id="item" requestURI="/schemaSets.action">

                <c:if test="${actionBean.deletePermission}">
                <display:column title="" sortable="true" sortName="sortName" sortProperty="identifier">
                    <stripes:checkbox name="selected" value="${item.id}" />
                </display:column>
                </c:if>

                <display:column title="Identifier" sortable="true" sortName="sortName" sortProperty="identifier">
                    <stripes:link href="/schemaSet.action">
                        <stripes:param name="schemaSet.id" value="${item.id}" />
                        <c:out value="${item.identifier}" />
                    </stripes:link>
                </display:column>
                <display:column title="Reg. status" sortable="true" sortProperty="reg_status">
                    <c:out value="${item.regStatus}" />
                </display:column>
            </display:table>

            <c:if test="${actionBean.deletePermission && not empty actionBean.schemaSetsResult.list}">
                <stripes:submit name="delete" value="Delete" />
            </c:if>

        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>