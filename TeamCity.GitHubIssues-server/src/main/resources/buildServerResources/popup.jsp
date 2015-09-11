<%@ include file="/include.jsp"%>
<jsp:useBean id="issue" scope="request" type="jetbrains.buildServer.issueTracker.IssueEx"/>
<bs:issueDetailsPopup issue="${issue}" priorityClass="${fn:toLowerCase(issue.priority)}" popupClass="bugzilla"/>
