<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="View scheduled jobs" currentSection="vocabularies">
    <stripes:layout-component name="contents">
        <h1>View scheduled Task History </h1>
          <div id="drop-operations">
            <ul>
                <c:if test="${not empty actionBean.user && ddfn:userHasPermission(actionBean.user, '/vocabularies', 'i')}">
                    <li class="back">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="viewScheduledJobs"> 
                            Back to scheduled jobs
                        </stripes:link>
                    </li>
                </c:if>
            </ul>
        </div>
        <display:table name="actionBean.scheduledTaskHistoryViews" class="datatable results" id="pastScheduledTask"
                       style="width:100% !important" requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view#ScheduledJobsHistory">
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No historical data for this Entry exist.</p>" />
            <display:setProperty name="paging.banner.item_name" value="pastScheduledTask" />
            <display:setProperty name="paging.banner.items_name" value="pastScheduledTasks" />
            <display:column title="Task Type" escapeXml="false" style="width: 10%">
                <dd:attributeValue attrValue="${pastScheduledTask.type}"/>
            </display:column>
            <display:column title="Last Execution Status" escapeXml="false" style="width: 15%">
                <dd:attributeValue attrValue="${pastScheduledTask.details.executionStatus}"/>
            </display:column>
            <display:column title="Last Execution Start Date" escapeXml="false" style="width: 10%">
                <fmt:formatDate value="${pastScheduledTask.details.startDate}" pattern="dd.MM.yyyy hh:mm"/>
            </display:column>
            <display:column title="Last Execution End Date" escapeXml="false" style="width: 10%">
                <fmt:formatDate value="${pastScheduledTask.details.endDate}" pattern="dd.MM.yyyy hh:mm"/>
            </display:column>
            <display:column title="Schedule Interval And Unit" escapeXml="false" style="width: 10%">
                <dd:attributeValue attrValue="${pastScheduledTask.taskParameters['scheduleInterval']} ${pastScheduledTask.taskParameters['scheduleIntervalUnit']}(S)"/>
            </display:column>
            <display:column title="Additional Task Details" escapeXml="false" style="width: 15% !important">
                <c:out value="${pastScheduledTask.additionalDetails}" />
            </display:column>     
            <display:column title="Actions" escapeXml="false" style="width: 10%">
                <stripes:form id="scheduleVocabularySync" method="post" beanclass="${actionBean['class'].name}">
                    <stripes:param name="scheduledTaskHistoryId" value="${pastScheduledTask.asyncTaskExecutionEntryHistoryId}" />
                    <stripes:param name="scheduledTaskId" value="${pastScheduledTask.details.taskId}" />
                    <stripes:submit value="Details" name="viewScheduledTaskHistoryDetails" class="mediumbuttonb"/>
                </stripes:form>
            </display:column>
        </display:table>
    </stripes:layout-component>
</stripes:layout-render>
