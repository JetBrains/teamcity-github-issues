package jetbrains.buildServer.issueTracker.github.auth;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.issueTracker.IssueFetcherAuthenticator;
import jetbrains.buildServer.issueTracker.github.GitHubConstants;
import jetbrains.buildServer.serverSide.oauth.PersonalOAuthTokens;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubAuthenticator implements IssueFetcherAuthenticator {

  private final Credentials myCredentials;

  public GitHubAuthenticator(@NotNull final Map<String, String> properties,
                             @NotNull final PersonalOAuthTokens personalTokens) {
    final String authType = properties.get(GitHubConstants.PARAM_AUTH_TYPE);
    if (GitHubConstants.AUTH_LOGINPASSWORD.equals(authType)) {
      final String username = properties.get(GitHubConstants.PARAM_USERNAME);
      final String password = properties.get(GitHubConstants.PARAM_PASSWORD);
      myCredentials = new UsernamePasswordCredentials(username, password);
    } else if (GitHubConstants.AUTH_ACCESSTOKEN.equals(authType)) {
      final String token = properties.get(GitHubConstants.AUTH_ACCESSTOKEN);
      if (!StringUtil.isEmptyOrSpaces(token)) {
        if (token.startsWith("oauth:")) {
          final String providerId = token.substring("oauth:".length());
          if (!StringUtil.isEmptyOrSpaces(providerId)) {
            final Set<PersonalOAuthTokens.Token> tokens = personalTokens.getTokens(providerId);
            if (!tokens.isEmpty()) {
              // todo: get user
              myCredentials = new TokenCredentials(tokens.iterator().next().getToken());
            } else {
              myCredentials = null;
            }
          } else {
            myCredentials = null;
          }
        } else {
          myCredentials = new TokenCredentials(token);
        }
      } else {
        myCredentials = null;
      }
    } else {
      myCredentials = null;
    }
  }

  public boolean isBasicAuth() {
    return false;
  }

  public void applyAuthScheme(@NotNull final HttpMethod httpMethod) {
  }

  @Nullable
  public Credentials getCredentials() {
    return myCredentials;
  }
}
