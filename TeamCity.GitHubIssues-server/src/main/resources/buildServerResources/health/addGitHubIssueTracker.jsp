<%@ include file="/include-internal.jsp"%>
<jsp:useBean id="buildType" type="jetbrains.buildServer.serverSide.SBuildType" scope="request"/>
<%--@elvariable id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.BuildTypeSuggestedItem"--%>
<c:set var="type" value="${healthStatusItem.additionalData['type']}"/>
<c:set var="repoUrl" value="${healthStatusItem.additionalData['repoUrl']}"/>
<div class="suggestionItem">
  There is an attached VCS root in <admin:editBuildTypeLink buildTypeId="${buildType.externalId}"><c:out value="${buildType.fullName}"/></admin:editBuildTypeLink> that points to GitHub.
  Do you want to use GitHub issue tracker as well?

  <div class="suggestionAction">
    <c:url var="url" value="/admin/editProject.html?projectId=${buildType.projectExternalId}&tab=issueTrackers&#add=add&addTracker=${type}&repoUrl=${util:urlEscape(repoUrl)}"/>
    <a class="addNew" href="${url}">Use GitHub issue tracker</a>
  </div>
</div>
