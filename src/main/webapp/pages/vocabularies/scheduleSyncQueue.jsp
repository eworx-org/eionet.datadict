<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Schedule Vocabulary Synchronization" currentSection="vocabularies">

    <stripes:layout-component name="head">
        <style>
body {font-family: "Lato", sans-serif;}

ul.tab {
    list-style-type: none;
    margin: 0;
    padding: 0;
    overflow: hidden;
    border: 1px solid #ccc;
    background-color: #f1f1f1;
}

/* Float the list items side by side */
ul.tab li {float: left;}

/* Style the links inside the list items */
ul.tab li a {
    display: inline-block;
    color: black;
    text-align: center;
    padding: 14px 16px;
    text-decoration: none;
    transition: 0.3s;
    font-size: 17px;
}

/* Change background color of links on hover */
ul.tab li a:hover {
    background-color: #ddd;
}

/* Create an active/current tablink class */
ul.tab li a:focus, .active {
    background-color: #ccc;
}

/* Style the tab content */
.tabcontent {
    display: none;
    padding: 6px 12px;
    border: 1px solid #ccc;
    border-top: none;
}
</style>
        <script type="text/javascript">


            (function ($) {
              

             $(document).ready(function() {
  //  $('#scheduledTask').dataTable();
 //   $('#scheduleTasksHistory').dataTable();
// Get the element with id="defaultOpen" and click on it
document.getElementById("defaultOpen").click();
        $(".dataTables_filter").css('margin-bottom', '20px');
     $("#scheduledTask").css('width', '100%');
     $("#scheduleTasksHistory").css('width', '100%');
     $(".dataTables_length select").css('margin-left','15px');
} );

            })(jQuery);



        </script>
        <script>jQuery.noConflict();</script>
<script>
function openDataView(evt, cityName) {
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(cityName).style.display = "block";
    evt.currentTarget.className += " active";
}
</script>
    </stripes:layout-component>



    <stripes:layout-component name="contents">
        <h1>Scheduled Synchronizations Queue</h1>

<ul class="tab">
  <li><a href="javascript:void(0)" class="tablinks" onclick="openDataView(event, 'scheduledSynchronizationJobs')" id="defaultOpen">Current Scheduled Jobs</a></li>
  <li><a href="javascript:void(0)" class="tablinks" onclick="openDataView(event, 'ScheduledJobsHistory')"> Scheduled Jobs History</a></li>
</ul>

      <%-- Vocabulary concepts --%>
        <div id="scheduledSynchronizationJobs" class="tabcontent">
        <display:table name="actionBean.asyncTaskEntries" class="datatable results" id="scheduledTask"
            style="width:100%" requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view#scheduledSynchronizationJobs"
             >
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No scheduled Jobs found.</p>" />
            <display:setProperty name="paging.banner.item_name" value="scheduledTask" />
            <display:setProperty name="paging.banner.items_name" value="scheduledTasks" />

            <display:column title="Task Id" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${scheduledTask.taskId}"/>
                </display:column>
        
                <display:column title="Execution Status" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${scheduledTask.executionStatus}"/>
                </display:column>
               <display:column title="Execution Start Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${scheduledTask.startDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column> 
              <display:column title="Execution End Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${scheduledTask.endDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column>  
            <display:column title="Next Scheduled Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${scheduledTask.scheduledDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column>
        
        </display:table>
        </div>
        <div id="ScheduledJobsHistory" class="tabcontent">
        <display:table name="actionBean.asyncTaskEntriesHistory" class="datatable results" id="pastScheduledTask"
            style="width:100%" requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view#ScheduledJobsHistory"
             >
            <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No scheduled Jobs found.</p>" />
            <display:setProperty name="paging.banner.item_name" value="pastScheduledTask" />
            <display:setProperty name="paging.banner.items_name" value="pastScheduledTasks" />

            <display:column title="Task Id" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${pastScheduledTask.taskId}"/>
                </display:column>
        
                <display:column title="Execution Status" escapeXml="false" style="width: 15%">
                    <dd:attributeValue attrValue="${pastScheduledTask.executionStatus}"/>
                </display:column>
               <display:column title="Execution Start Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${pastScheduledTask.startDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column> 
              <display:column title="Execution End Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${pastScheduledTask.endDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column>  
            <display:column title="Next Scheduled Date" escapeXml="false" style="width: 15%">
                    <fmt:formatDate value="${pastScheduledTask.scheduledDate}" pattern="dd.MM.yyyy hh:mm"/>
                </display:column>
        
        </display:table>
        </div>
        
    </stripes:layout-component>
</stripes:layout-render>
