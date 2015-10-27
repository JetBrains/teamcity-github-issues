<%@ include file="/include.jsp"%>
<%@include file="providerConstants.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
  (function() {
    BS.GitHubIssues = {
      selectedAuth: undefined,
      init: function(select) {
        this.selectAuthType($(select));
      },

      selectAuthType: function(select) {
        this.selectedAuth = select.value;
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
        <td>GitHub</td>
      </tr>
    </c:if>
    <tr>
      <th><label for="${name}" class="shortLabel">Display Name: <l:star/></label></th>
      <td>
        <props:textProperty name="${name}" maxlength="100"/>
        <span id="error_${name}" class="error"></span>
      </td>
    </tr>
    <tr>
      <th><label for="${repository}" class="shortLabel">Repository: <l:star/></label></th>
      <td>
        <props:textProperty name="${repository}" maxlength="100"/>
        <span id="error_${repository}" class="error"></span>
      </td>
    </tr>
    <tr>
      <th><label for="${authType}_select">Authentication:</label></th>
      <td>
        <props:selectProperty name="${authType}"
                              id="${authType}_select"
                              onchange="BS.GitHubIssues.selectAuthType(this);">
          <props:option value="${authAnonymous}">Anonymous</props:option>
          <props:option value="${authLoginPassword}">Username / Password</props:option>
          <props:option value="${authAccessToken}">Access Token</props:option>
        </props:selectProperty>
        <span id="error_${authType}" class="error"></span>
      </td>
    </tr>
    <tr class="js_authsetting ${authLoginPassword}">
      <th><label for="${username}" class="shortLabel">Username: <l:star/></label></th>
      <td>
        <props:textProperty name="${username}" maxlength="100"/>
        <span id="error_${username}" class="error"></span>
      </td>
    </tr>
    <tr class="js_authsetting ${authLoginPassword}">
      <th><label for="${password}" class="shortLabel">Password: <l:star/></label></th>
      <td>
        <props:passwordProperty name="${password}" maxlength="100"/>
        <span id="error_${password}" class="error"></span>
      </td>
    </tr>

    <tr class="js_authsetting ${authAccessToken}">
      <th><label for="${accessToken}" class="shortLabel">Access token: <l:star/></label></th>
      <td>
        <props:passwordProperty name="${accessToken}" maxlength="100"/>
        <span id="error_${accessToken}" class="error"></span>
      </td>
    </tr>
    <tr>
      <th><label for="${pattern}" class="shortLabel">Issue ID Pattern: <l:star/></label></th>
      <td>
        <props:textProperty name="${pattern}" maxlength="100"/>
        <span id="error_${pattern}" class="error"></span>
        <span class="fieldExplanation">Use the regex syntax, e.g. #(\d+)<bs:help file="Integrating+TeamCity+with+Issue+Tracker"/></span>
      </td>
    </tr>
  </table>
</div>

<script type="text/javascript">
  BS.GitHubIssues.init('${authType}_select');
</script>
