<%@ page import="jetbrains.buildServer.web.util.SessionUser" %>
<%@ include file="/include.jsp"%>
<%@ include file="providerConstants.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="oauth" tagdir="/WEB-INF/tags/oauth" %>



<jsp:useBean id="providerType" scope="request" type="jetbrains.buildServer.issueTracker.github.GitHubIssueProviderType"/>
<jsp:useBean id="oauthConnections" scope="request" type="java.util.Map"/>

<style type="text/css">
  .token-connection {
    width: 85% !important;
  }
  .token-connection-diplay-name {
    float: none !important;
  }
</style>

<script type="text/javascript">
  (function() {
    BS.GitHubIssues = {
      selectedAuth: undefined,
      selector: undefined,

      init: function(select) {
        this.selector = $(select);
        this.selectAuthType();
        this.onRepositoryChanged($('${repository}').value);
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
      },

      onRepositoryChanged: function(repository) {
        if (repository && repository.length > 0) {
          $j('#message_must_select_repo').hide();
          $j('.token-connection').show();
          $j('.connection-note').show();
          $j('#tokenManagementContentContainer').show();
          if (BS.TokenControlParams) {
            BS.TokenControls.showGenerateButtons();
            BS.NewTokenForm.applyConnectionFilter();
          }
        } else {
          $j('#message_must_select_repo').show();
          $j('.token-connection').hide();
          $j('.connection-note').hide();
          $j('#tokenManagementContentContainer').hide();
          if (BS.TokenControlParams) {
            BS.NewTokenDialog.cancel();
            BS.TokenControls.hideGenerateButtons();
          }
        }
      }
    };

    window.getRepositoryUrl = function () {
      return $('${repository}').value;
    }
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
        <props:textProperty name="${repository}" maxlength="100" onchange="BS.GitHubIssues.onRepositoryChanged(value);"/>
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

    <tr id="ghaIssueTokenControls" class="js_authsetting ${authGitHubApp}">
      <th><label for="${tokenId}" class="shortLabel">GitHub App token:</label></th>
      <td>
        <div class="access-token-note" id="message_must_select_repo">A repository URL must be specified before tokens can be configured.</div>

        <c:if test="${empty oauthConnections}">
          <div>There are no GitHub App connections available to the project.</div>
        </c:if>

        <props:hiddenProperty name="${tokenId}" />
        <span class="error" id="error_${tokenId}"></span>

        <c:set var="canObtainTokens" value="${true}"/>
        <c:set var="connectorType" value="GitHubApp"/>
        <oauth:tokenControlsForFeatures
            project="${project}"
            providerTypes="'${connectorType}'"
            tokenIntent="REPO_FULL"
            canObtainTokens="${canObtainTokens}"
            callback="BS.AuthTypeTokenSupport.tokenCallback"
            oauthConnections="${oauthConnections.keySet()}"
            checkForRefreshSupport="true"
            embedAfterRowElementId="ghaIssueTokenControls"
            inputClassName="ghaTokenInput"
            filterConnectionsBy="window.getRepositoryUrl">
          <jsp:attribute name="addCredentialFragment">
            <span class="smallNote connection-note" style="margin-left: 0px;">You can add credentials via the
                  <a href="<c:url value='/admin/editProject.html?projectId=${project.externalId}&tab=oauthConnections#addDialog=${connectorType}'/>" target="_blank" rel="noreferrer">Project Connections</a> page</span>
          </jsp:attribute>
        </oauth:tokenControlsForFeatures>
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
        BS.GitHubIssues.onRepositoryChanged(repoInfo.repositoryUrl);
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