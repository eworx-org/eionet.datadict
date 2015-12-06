<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Vocabulary">

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFoldersActionBean">
                        <stripes:param name="folderId" value="${actionBean.vocabularyFolder.folderId}" />
                        <stripes:param name="expand" value="true" />
                        <stripes:param name="expanded" value="" />
                                Back to set
                    </stripes:link>
                </li>
                <c:if test="${not empty actionBean.user}">
                  <c:if test="${not actionBean.vocabularyFolder.siteCodeType}">
                      <li>
                          <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="add">
                              <stripes:param name="copyId" value="${actionBean.vocabularyFolder.id}" />
                              Create new copy
                          </stripes:link>
                      </li>
                  </c:if>
                  <c:if test="${actionBean.userWorkingCopy}">
                  <li>
                      <a href="#" id="addNewConceptLink">Add new concept</a>
                  </li>
                  <li>
                      <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="edit">
                          <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                          <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                          <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                          Edit vocabulary
                      </stripes:link>
                  </li>
                  <li>
                      <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkIn">
                          <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                          <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                          <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                          <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                          Check in
                      </stripes:link>
                  </li>
                  <li>
                      <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="undoCheckOut">
                          <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                          <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                          <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                          Undo checkout
                      </stripes:link>
                  </li>
                  </c:if>
                  <c:if test="${not actionBean.vocabularyFolder.workingCopy}">
                  <li>
                      <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="checkOut">
                          <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                          <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                          <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                          <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                          Check out
                      </stripes:link>
                  </li>
                  </c:if>
                </c:if>
            </ul>
        </div>

        <h1>Vocabulary: <em><c:out value="${actionBean.vocabularyFolder.label}" /></em></h1>

        <c:if test="${actionBean.vocabularyFolder.workingCopy && actionBean.vocabularyFolder.siteCodeType}">
            <div class="note-msg">
                <strong>Notice</strong>
                <p>
                For checked out site codes, vocabulary concepts are not visible. To view them, see the
                <stripes:link href="/services/siteCodes">site codes page</stripes:link>.
                </p>
            </div>
        </c:if>

        <c:if test="${actionBean.checkedOutByUser}">
            <div class="note-msg">
                <strong>Note</strong>
                <p>You have a
                    <stripes:link beanclass="${actionBean['class'].name}" event="viewWorkingCopy">
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}"/>
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}"/>
                        working copy
                    </stripes:link> of this vocabulary!</p>
            </div>
        </c:if>

        <c:if test="${not actionBean.vocabularyFolder.draftStatus && not actionBean.vocabularyFolder.workingCopy}">
        <c:url var="rdfIconUrl" value="/images/rdf-icon.gif" />
        <c:url var="csvIconUrl" value="/images/csv_icon_sm.gif" />
        <c:url var="codelistIconUrl" value="/images/inspire_icon.gif" />
        <c:url var="jsonIconUrl" value="/images/json_file_icon.gif" />
        <div id="createbox" style="clear:right">
            <table id="outputsmenu">
                <tr>
                    <td style="width:73%">Get RDF output of this vocabulary</td>
                    <td style="width:27%">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="rdf" title="Export RDF">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            <img src="${rdfIconUrl}" alt="" />
                        </stripes:link>
                    </td>
                </tr>
                <tr>
                    <td style="width:73%">Get CSV output of this vocabulary</td>
                    <td style="width:27%">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="csv" title="Export CSV">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            <img src="${csvIconUrl}" alt="" />
                        </stripes:link>
                    </td>
                </tr>
                <tr>
                    <td style="width:73%">Get XML output in INSPIRE codelist format</td>
                    <td style="width:27%">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="codelist" title="Export XML in INSPIRE codelist format">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            <img src="${codelistIconUrl}" alt="" />
                        </stripes:link>
                    </td>
                </tr>
                <tr>
                    <td style="width:73%">Get JSON-LD output of this vocabulary</td>
                    <td style="width:27%">
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="json" title="Export JSON">
                            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                            <img src="${jsonIconUrl}" alt="" />
                        </stripes:link>
                    </td>
                </tr>
            </table>
        </div>
        </c:if>

        <!-- Vocabulary folder -->
        <div id="outerframe" style="padding-top:20px">
            <table class="datatable">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Folder
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.folderName} (${actionBean.vocabularyFolder.folderLabel})" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.identifier}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Label
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.label}" />
                    </td>
                </tr>
                <c:if test="${not empty actionBean.vocabularyFolder.baseUri}">
                  <tr>
                      <th scope="row" class="scope-row simple_attr_title">
                          Base URI
                      </th>
                      <td class="simple_attr_value">
                          <c:out value="${actionBean.vocabularyFolder.baseUri}" />
                      </td>
                  </tr>
                </c:if>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Registration status
                    </th>
                    <td class="simple_attr_value">
                        <fmt:setLocale value="en_GB" />
                        <fmt:formatDate pattern="dd MMM yyyy HH:mm:ss" value="${actionBean.vocabularyFolder.dateModified}" var="dateFormatted"/>
                        <c:out value="${actionBean.vocabularyFolder.regStatus}"/>
                        <c:if test="${not empty actionBean.userName && actionBean.userWorkingCopy}">
                            <span class="caution" title="Checked out on ${dateFormatted}">(Working copy)</span>
                        </c:if>
                        <c:if test="${not empty actionBean.userName && actionBean.checkedOutByOther}">
                            <span class="caution">(checked out by <em>${actionBean.vocabularyFolder.workingUser}</em>)</span>
                        </c:if>
                        <c:if test="${not empty actionBean.userName && empty actionBean.vocabularyFolder.workingUser || actionBean.checkedOutByUser}">
                            <span style="color:#A8A8A8;font-size:0.8em">(checked in by ${actionBean.vocabularyFolder.userModified} on ${dateFormatted})</span>
                        </c:if>
                        <c:if test="${empty actionBean.userName}">
                            <span>${dateFormatted}</span>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Type
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.type.label}" />
                    </td>
                </tr>
                <!-- Simple attributes -->
                <c:forEach var="attributeValues" items="${actionBean.vocabularyFolder.attributes}">
                    <c:set var="attrMeta" value="${attributeValues[0]}"/>
                    <c:if test="${not empty attrMeta.value}">
                      <tr>
                          <th scope="row" class="scope-row simple_attr_title">${attrMeta.label}</th>
                          <td class="simple_attr_value">
                              <ul class="stripedmenu">
                                <c:forEach var="attr" items="${attributeValues}" varStatus="innerLoop">
                                  <li>
                                    <span style="white-space:pre-wrap"><c:out value="${attr.value}" /></span>
                                  </li>
                                </c:forEach>
                              </ul>
                          </td>
                      </tr>
                    </c:if>
                </c:forEach>
            </table>
        </div>

        <c:if test="${actionBean.userWorkingCopy}">
            <jsp:include page="newConceptInc.jsp" />
        </c:if>

        <!-- Vocabulary concepts search -->
        <h2>Vocabulary concepts</h2>
        <stripes:form method="get" id="searchForm" beanclass="${actionBean['class'].name}">
            <div id="searchframe">
                <stripes:hidden name="vocabularyFolder.folderName" />
                <stripes:hidden name="vocabularyFolder.identifier" />
                <stripes:hidden name="vocabularyFolder.workingCopy" />
                <table class="datatable">
                    <colgroup>
                        <col style="width:10em;"/>
                        <col />
                        <col style="width:10em;"/>
                        <col />
                        <col />
                    </colgroup>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title" title="Text to filter from label, notation and definition">
                            <label for="filterText"><span style="white-space:nowrap;">Filtering text</span></label>
                        </th>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="30" name="filter.text" id="filterText"/>
                        </td>
                        <th scope="row" class="scope-row simple_attr_title" title="Concept's status">
                            <label for="status"><span style="white-space:nowrap;">Status</span></label>
                        </th>
                        <td class="simple_attr_value" style="padding-right: 5em;">
                            <stripes:select name="filter.conceptStatusInt" id="status">
                                <stripes:option value="255" label="All concepts"/>
                                <stripes:options-collection collection="<%=eionet.meta.dao.domain.StandardGenericStatus.valuesAsList()%>" label="label" value="value"/>
                            </stripes:select>
                        </td>
                        <td>
                            <stripes:submit name="view" value="Search" class="mediumbuttonb"/>
                        </td>
                    </tr>
                </table>
            </div>
        </stripes:form>

        <%-- Vocabulary concepts --%>
        <div>
        <display:table name="actionBean.vocabularyConcepts" class="datatable" id="concept"
            style="width:80%" requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/view"
            excludedParams="view vocabularyFolder.identifier vocabularyFolder.folderName">
            <display:setProperty name="basic.msg.empty_list" value="No vocabulary concepts found." />
            <display:setProperty name="paging.banner.placement" value="both" />
            <display:setProperty name="paging.banner.item_name" value="concept" />
            <display:setProperty name="paging.banner.items_name" value="concepts" />

            <display:column title="Id" class="${actionBean.vocabularyFolder.numericConceptIdentifiers ? 'number' : ''}" style="width: 10%" media="html">
                <c:choose>
                    <c:when test="${!concept.status.accepted}">
                        <span style="text-decoration:line-through"><c:out value="${concept.identifier}" /></span>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${concept.identifier}" />
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column title="Preferred label" media="html">
                <c:choose>
                    <c:when test="${not actionBean.vocabularyFolder.workingCopy}">
                        <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${concept.identifier}/view" title="${concept.label}">
														<stripes:param name="facet" value="HTML Representation"/> <!-- Discourage people from copy-paste of the link -->
                            <dd:attributeValue attrValue="${concept.label}" attrLen="40"/>
                        </stripes:link>
                    </c:when>
                    <c:otherwise>
                        <stripes:link href="/vocabularyconcept/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/${concept.identifier}/view" title="${concept.label}">
                            <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                            <dd:attributeValue attrValue="${concept.label}" attrLen="40"/>
                        </stripes:link>
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column title="Status" escapeXml="false" style="width: 15%">
                <dd:attributeValue attrValue="${concept.status.label}"/>
            </display:column>
            <display:column title="Status Modified" escapeXml="false" style="width: 15%">
                <fmt:formatDate value="${concept.statusModified}" pattern="dd.MM.yyyy"/>
            </display:column>
            <display:column title="Notation" escapeXml="true" property="notation" style="width: 10%"/>
        </display:table>
        </div>
    <%-- The section that displays versions of this vocabulary. --%>

    <c:if test="${not empty actionBean.vocabularyFolderVersions}">
        <h2>Other versions of this vocabulary</h2>
        <display:table name="${actionBean.vocabularyFolderVersions}" class="datatable" id="item" style="width:80%">
            <display:column title="Label">
                <c:choose>
                    <c:when test="${item.draftStatus && empty actionBean.user}">
                        <span class="link-folder" style="color:gray;">
                            <c:out value="${item.label}"/>
                        </span>
                    </c:when>
                    <c:otherwise>
                        <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" class="link-folder">
                            <stripes:param name="vocabularyFolder.folderName" value="${item.folderName}" />
                            <stripes:param name="vocabularyFolder.identifier" value="${item.identifier}" />
                            <c:if test="${item.workingCopy}">
                                <stripes:param name="vocabularyFolder.workingCopy" value="${item.workingCopy}" />
                            </c:if>
                            <c:out value="${item.label}"/>
                        </stripes:link>
                    </c:otherwise>
                </c:choose>
                <c:if test="${item.workingCopy && actionBean.userName==item.workingUser}">
                    <span title="Your working copy" class="checkedout"><strong>*</strong></span>
                </c:if>
            </display:column>
            <display:column title="Status"><c:out value="${item.regStatus}"/></display:column>
            <display:column title="Last modified">
                <fmt:formatDate value="${item.dateModified}" pattern="dd.MM.yy HH:mm:ss"/>
            </display:column>
        </display:table>
    </c:if>

    </stripes:layout-component>

</stripes:layout-render>
