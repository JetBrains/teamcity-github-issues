<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jetbrains.buildServer.issueTracker.github.GitHubConstants" %>
<%@ page import="jetbrains.buildServer.issueTracker.IssueTrackerConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  ~ Copyright 2000-2022 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<c:set var="name" value="<%=GitHubConstants.PARAM_NAME%>"/>
<c:set var="repository" value="<%=GitHubConstants.PARAM_REPOSITORY%>"/>
<c:set var="authType" value="<%=GitHubConstants.PARAM_AUTH_TYPE%>"/>
<c:set var="username" value="<%=GitHubConstants.PARAM_USERNAME%>"/>
<c:set var="password" value="<%=GitHubConstants.PARAM_PASSWORD%>"/>
<c:set var="accessToken" value="<%=GitHubConstants.PARAM_ACCESS_TOKEN%>"/>
<c:set var="pattern" value="<%=GitHubConstants.PARAM_PATTERN%>"/>
<c:set var="tokenId" value="<%=IssueTrackerConstants.PARAM_TOKEN_ID%>" />


<c:set var="authAnonymous" value="<%=GitHubConstants.AUTH_ANONYMOUS%>"/>
<c:set var="authLoginPassword" value="<%=GitHubConstants.AUTH_LOGINPASSWORD%>"/>
<c:set var="authAccessToken" value="<%=GitHubConstants.AUTH_ACCESSTOKEN%>"/>
<c:set var="authGitHubApp" value="<%=IssueTrackerConstants.AUTH_STORED_TOKEN%>" />