

package jetbrains.buildServer.issueTracker.github.auth;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.issueTracker.IssueFetcherAuthenticator;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.oauth.OAuthToken;
import jetbrains.buildServer.serverSide.oauth.OAuthTokensStorage;
import jetbrains.buildServer.util.HTTPRequestBuilder;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static jetbrains.buildServer.issueTracker.IssueTrackerConstants.AUTH_STORED_TOKEN;
import static jetbrains.buildServer.issueTracker.IssueTrackerConstants.PARAM_TOKEN_ID;
import static jetbrains.buildServer.issueTracker.github.GitHubConstants.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubAuthenticator implements IssueFetcherAuthenticator {

  private Credentials myCredentials = null;

  public GitHubAuthenticator(@NotNull final Map<String, String> properties, SProject project, OAuthTokensStorage tokenStorage) {
    final String authType = properties.get(PARAM_AUTH_TYPE);
    if (AUTH_LOGINPASSWORD.equals(authType)) {
      final String username = properties.get(PARAM_USERNAME);
      final String password = properties.get(PARAM_PASSWORD);
      myCredentials = new UsernamePasswordCredentials(username, password);
    } else if (AUTH_ACCESSTOKEN.equals(authType)) {
      final String token = properties.get(PARAM_ACCESS_TOKEN);
      if (!StringUtil.isEmptyOrSpaces(token)) {
        myCredentials = new TokenCredentials(token);
      }
    } else if (AUTH_STORED_TOKEN.equals(authType)) {
      final String tokenId = properties.get(PARAM_TOKEN_ID);
      final OAuthToken gitHubOAuthToken = tokenStorage.getToken(project, tokenId, true, true);
      if (gitHubOAuthToken != null) {
        myCredentials = new TokenCredentials(gitHubOAuthToken.getAccessToken());
      }
    }
  }

  public boolean isBasicAuth() {
    return false;
  }

  public void applyAuthScheme(@NotNull final HttpMethod httpMethod) {
  }

  @Override
  public void applyAuthScheme(@NotNull final HTTPRequestBuilder requestBuilder) {
  }

  @Nullable
  public Credentials getCredentials() {
    return myCredentials;
  }
}