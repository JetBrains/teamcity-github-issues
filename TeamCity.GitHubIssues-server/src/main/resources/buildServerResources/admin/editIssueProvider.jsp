<%@ include file="/include.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
  (function() {
    BS.GitHubIssues = {
      selectedAuth: undefined,

      init: function() {
        this.selectAuthType($('authType_select'))
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
      <th><label for="name" class="shortLabel">Display Name: <l:star/></label></th>
      <td>
        <props:textProperty name="name" maxlength="100"/>
        <span id="error_name" class="error"></span>
      </td>
    </tr>
    <tr>
      <th><label for="repository" class="shortLabel">Repository: <l:star/></label></th>
      <td>
        <props:textProperty name="repository" maxlength="100"/>
        <span id="error_repository" class="error"></span>
      </td>
    </tr>
    <tr>
      <th>Authentication:</th>
      <td>
        <props:selectProperty name="authType"
                              id="authType_select"
                              onchange="BS.GitHubIssues.selectAuthType(this);">
          <props:option value="anonymous">Anonymous</props:option>
          <props:option value="loginpassword">Username / Password</props:option>
          <props:option value="accesstoken">Access Token</props:option>
        </props:selectProperty>
      </td>
    </tr>
    <tr class="js_authsetting loginpassword">
      <th><label for="username" class="shortLabel">Username: <l:star/></label></th>
      <td>
        <props:textProperty name="username" maxlength="100"/>
        <span id="error_username" class="error"></span>
      </td>
    </tr>
    <tr class="js_authsetting loginpassword">
      <th><label for="secure:password" class="shortLabel">Password: <l:star/></label></th>
      <td>
        <props:passwordProperty name="secure:password" maxlength="100"/>
        <span id="error_secure:password" class="error"></span>
      </td>
    </tr>

    <tr class="js_authsetting accesstoken">
      <th><label for="secure:accessToken" class="shortLabel">Access token: <l:star/></label></th>
      <td>
        <props:passwordProperty name="secure:accessToken" maxlength="100"/>
        <span id="error_secure:accessToken" class="error"></span>
      </td>
    </tr>
    <tr>
      <th><label for="pattern" class="shortLabel">Issue ID Pattern: <l:star/></label></th>
      <td>
        <props:textProperty name="pattern" maxlength="100"/>
        <span id="error_pattern" class="error"></span>
        <span class="fieldExplanation">Use the regex syntax, e.g. #(\d+)<bs:help file="Integrating+TeamCity+with+Issue+Tracker"/></span>
      </td>
    </tr>
  </table>
</div>

<script type="text/javascript">
  BS.GitHubIssues.init();
</script>
