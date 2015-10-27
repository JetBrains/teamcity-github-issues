package jetbrains.buildServer.issueTracker.github.auth;

import jetbrains.buildServer.issueTracker.IssueFetcherAuthenticator;
import jetbrains.buildServer.issueTracker.github.GitHubConstants;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubAuthenticator implements IssueFetcherAuthenticator {

  private final Credentials myCredentials;

  public GitHubAuthenticator(@NotNull final Map<String, String> properties) {
    final String authType = properties.get(GitHubConstants.PARAM_AUTH_TYPE);
    if (GitHubConstants.AUTH_LOGINPASSWORD.equals(authType)) {
      final String username = properties.get(GitHubConstants.PARAM_USERNAME);
      final String password = properties.get(GitHubConstants.PARAM_PASSWORD);
      myCredentials = new UsernamePasswordCredentials(username, password);
    } else if (GitHubConstants.AUTH_ACCESSTOKEN.equals(authType)) {
      myCredentials = new TokenCredentials(properties.get(GitHubConstants.AUTH_ACCESSTOKEN));
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
