<%@ page import="jetbrains.buildServer.web.util.SessionUser" %>
<%@ include file="/include.jsp"%>
<%@ include file="providerConstants.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="oauth" tagdir="/WEB-INF/tags/oauth" %>

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

<jsp:useBean id="providerType" scope="request" type="jetbrains.buildServer.issueTracker.github.GitHubIssueProviderType"/>
<jsp:useBean id="oauthConnections" scope="request" type="java.util.Map"/>

<script type="text/javascript">
  (function() {
    BS.GitHubIssues = {
      selectedAuth: undefined,
      selector: undefined,

      init: function(select) {
        this.selector = $(select);
        this.selectAuthType();
      },

      selectAuthType: function() {
        this.selectedAuth = this.selector.value;
        this.onTypeChanged();
      },

      onTypeChanged: function() {
        var s = '.' + this.selectedAuth;
        $j('.js_authsetting')
                .filter(s).removeClass('hidden').end()
                .not(s).addClass('hidden');
        BS.MultilineProperties.updateVisible();
      }
    };
  })();

</script>

<div>
  <table class="editProviderTable">
    <%--@elvariable id="showType" type="java.lang.Boolean"--%>
    <c:if test="${showType}">
      <tr>
        <th><label class="shortLabel">Connection Type:</label></th>
        <td><bs:out value=" ${providerType.displayName}"/></td>
      </tr>
    </c:if>
    <tr>
      <th><label for="${name}" class="shortLabel">Display Name:<l:star/></label></th>
      <td>
        <props:textProperty name="${name}" maxlength="100"/>
        <span id="error_${name}" class="error"></span>
      </td>
    </tr>
    <tr>
      <th><label for="${repository}" class="shortLabel">Repository URL:<l:star/></label></th>
      <td>
        <props:textProperty name="${repository}" maxlength="100"/>
        <jsp:include page="/admin/repositoryControls.html?projectId=${project.externalId}&pluginName=github"/>
        <jsp:include page="/admin/repositoryControls.html?projectId=${project.externalId}&pluginName=githubApp"/>
        <span id="error_${repository}" class="error"></span>
      </td>
    </tr>
    <tr>
      <th><label for="${authType}_select">Authentication:</label></th>
      <td>
        <props:selectProperty name="${authType}"
                              id="${authType}_select"
                              onchange="BS.GitHubIssues.selectAuthType();">
          <props:option value="${authAnonymous}">Anonymous</props:option>
          <props:option value="${authAccessToken}">Access Token</props:option>
          <props:option value="${authLoginPassword}">Username / Password</props:option>
          <props:option value="${authGitHubApp}">GitHub App access token</props:option>
        </props:selectProperty>
        <span id="error_${authType}" class="error"></span>
        <div class="js_authsetting ${authLoginPassword} attentionComment">
          Username / password authentication has been deprecated. Consider using access token instead
        </div>
      </td>
    </tr>
    <tr class="js_authsetting ${authLoginPassword}">
      <th><label for="${username}" class="shortLabel">Username:<l:star/></label></th>
      <td>
        <props:textProperty name="${username}" maxlength="100"/>
        <span id="error_${username}" class="error"></span>
      </td>
    </tr>
    <tr class="js_authsetting ${authLoginPassword}">
      <th><label for="${password}" class="shortLabel">Password:<l:star/></label></th>
      <td>
        <props:passwordProperty name="${password}" maxlength="100"/>
        <span id="error_${password}" class="error"></span>
      </td>
    </tr>

    <tr class="js_authsetting ${authAccessToken}">
      <th><label for="${accessToken}" class="shortLabel">Access token:<l:star/></label></th>
      <td>
        <props:passwordProperty name="${accessToken}" maxlength="100"/>
        <span class="fieldExplanation">GitHub <a href="https://help.github.com/articles/creating-an-access-token-for-command-line-use/"> access token</a></span>
        <span id="error_${accessToken}" class="error"></span>
      </td>
    </tr>

    <tr class="js_authsetting ${authGitHubApp}">
      <th><label for="${tokenId}" class="shortLabel">GitHub App token:</label></th>
      <td>
        <%@include file="/admin/_tokenSupport.jspf"%>

        <span class="access-token-note" id="message_no_token">No access token configured.</span>
        <span class="access-token-note" id="message_we_have_token"></span>

        <c:if test="${empty oauthConnections}">
          <br/>
          <span>There are no GitHub App connections available to the project.</span>
        </c:if>

        <props:hiddenProperty name="${tokenId}" />
        <span class="error" id="error_${tokenId}"></span>

        <c:forEach items="${oauthConnections.keySet()}" var="connection">
          <c:if test="${connection.oauthProvider.isTokenRefreshSupported()}">
            <script type="application/javascript">
              BS.AuthTypeTokenSupport.connections['${connection.id}'] = '<bs:forJs>${connection.connectionDisplayName}</bs:forJs>';
            </script>
            <div class="token-connection">
                  <span class="token-connection-diplay-name" title="<c:out value='${connection.id}' />">
                    <c:out value="${connection.connectionDisplayName}" />
                  </span>
              <oauth:obtainToken connection="${connection}" className="btn btn_small token-connection-button" callback="BS.AuthTypeTokenSupport.tokenCallback">
                Acquire new
              </oauth:obtainToken>
            </div>
          </c:if>
        </c:forEach>

        <c:set var="connectorType" value="GitHubApp"/>
        <span class="smallNote connection-note" style="margin-left: 0px;">Add credentials via the
                  <a href="<c:url value='/admin/editProject.html?projectId=${project.externalId}&tab=oauthConnections#addDialog=${connectorType}'/>" target="_blank" rel="noreferrer">Project Connections</a> page</span>


      </td>
    </tr>

    <tr>
      <th><label for="${pattern}" class="shortLabel">Issue ID Pattern:<l:star/></label></th>
      <td>
        <props:textProperty name="${pattern}" maxlength="100"/>
        <span class="fieldExplanation">Use regex syntax, e.g. #(\d+)<bs:help file="Integrating+TeamCity+with+Issue+Tracker"/></span>
        <span id="error_${pattern}" class="error"></span>
      </td>
    </tr>
  </table>
</div>

<script type="text/javascript">
  BS.GitHubIssues.init('${authType}_select');
  $j(document).ready(function() {
    if (BS.Repositories != null) {
      BS.Repositories.installControls($('repository'), function(repoInfo, cre) {
        $('${name}').value = repoInfo.owner + "/" + repoInfo.name;
        $('${repository}').value = repoInfo.repositoryUrl;
        if (cre['refreshableToken']) {
          $('${authType}_select').value = "${authGitHubApp}";
          cre.connectionId = cre['oauthProviderId'];
          BS.AuthTypeTokenSupport.tokenCallback(cre);
        }
        else {
          $('${accessToken}').value = "oauth:<%=SessionUser.getUser(request).getId()%>:" + cre.oauthProviderId + ":" + cre.oauthLogin;
          $('${authType}_select').value = "${authAccessToken}";
        }
        BS.GitHubIssues.selectAuthType();
      });
    }
    // if we have received some init values
    // var params = window.location.search.toQueryParams();
    var params = BS.IssueProviderForm.initOptions;
    if (params && params['addTracker']) {
      $('${name}').value = decodeURIComponent(params['suggestedName']);
      $('${repository}').value = decodeURIComponent(params['repoUrl']);
    }
  });
</script>
