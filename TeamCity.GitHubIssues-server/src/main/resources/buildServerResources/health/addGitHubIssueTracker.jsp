<%--suppress XmlPathReference --%>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include-internal.jsp"%>
<jsp:useBean id="buildType" type="jetbrains.buildServer.serverSide.SBuildType" scope="request"/>
<%--@elvariable id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.BuildTypeSuggestedItem"--%>
<c:set var="type" value="${healthStatusItem.additionalData['type']}"/>
<c:set var="repoUrl" value="${healthStatusItem.additionalData['repoUrl']}"/>
<c:set var="suggestedName" value="${healthStatusItem.additionalData['suggestedName']}"/>
<div class="suggestionItem">
  There is an attached VCS root in <admin:editBuildTypeLink buildTypeId="${buildType.externalId}"><c:out value="${buildType.fullName}"/></admin:editBuildTypeLink> that points to GitHub.
  Do you want to use GitHub issue tracker as well?

  <div class="suggestionAction">
    <%--suppress XmlPathReference --%>
    <c:url var="url" value="/admin/editProject.html?init=1&projectId=${buildType.projectExternalId}&tab=issueTrackers&#addTracker=${type}&repoUrl=${util:urlEscape(repoUrl)}&suggestedName=${util:urlEscape(suggestedName)}"/>
    <a class="addNew" href="${url}">Use GitHub issue tracker</a>
  </div>
</div>
