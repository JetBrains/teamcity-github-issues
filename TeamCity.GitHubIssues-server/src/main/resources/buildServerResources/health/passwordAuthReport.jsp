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

<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="bs" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include-internal.jsp"%>
<%--@elvariable id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.BuildTypeSuggestedItem"--%>
<c:set var="suggestedTrackers" value="${healthStatusItem.additionalData['suggestedTrackers']}"/>
<%--@elvariable id="project" type="jetbrains.buildServer.serverSide.SProject"--%>
<c:set var="project" value="${healthStatusItem.additionalData['project']}"/>
<c:set var="name" value="${healthStatusItem.additionalData['name']}"/>
<c:set var="id" value="${healthStatusItem.additionalData['id']}"/>
<c:set var="type" value="${healthStatusItem.additionalData['type']}"/>

<c:set var="hash">editTracker=${type}&providerId=${id}</c:set>

<c:url var="url" value="/admin/editProject.html?init=1&projectId=${project.externalId}&tab=issueTrackers&#${hash}"/>

Github issue tracker <a href="${url}"><bs:out value="${name}"/></a> uses deprecated username/password authentication.
Consider using access token instead