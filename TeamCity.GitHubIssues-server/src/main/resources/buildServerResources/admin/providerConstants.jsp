<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jetbrains.buildServer.issueTracker.github.GitHubConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="name" value="<%=GitHubConstants.PARAM_NAME%>"/>
<c:set var="repository" value="<%=GitHubConstants.PARAM_REPOSITORY%>"/>
<c:set var="authType" value="<%=GitHubConstants.PARAM_AUTH_TYPE%>"/>
<c:set var="username" value="<%=GitHubConstants.PARAM_USERNAME%>"/>
<c:set var="password" value="<%=GitHubConstants.PARAM_PASSWORD%>"/>
<c:set var="accessToken" value="<%=GitHubConstants.PARAM_ACCESS_TOKEN%>"/>
<c:set var="pattern" value="<%=GitHubConstants.PARAM_PATTERN%>"/>


<c:set var="authAnonymous" value="<%=GitHubConstants.AUTH_ANONYMOUS%>"/>
<c:set var="authLoginPassword" value="<%=GitHubConstants.AUTH_LOGINPASSWORD%>"/>
<c:set var="authAccessToken" value="<%=GitHubConstants.AUTH_ACCESSTOKEN%>"/>


