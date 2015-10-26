package jetbrains.buildServer.issueTracker.github.auth;

import jetbrains.buildServer.issueTracker.IssueFetcherAuthenticator;
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
    final String authType = properties.get("authType");
    if ("loginpassword".equals(authType)) {
      final String username = properties.get("username");
      final String password = properties.get("secure:password");
      myCredentials = new UsernamePasswordCredentials(username, password);
    } else if ("accesstoken".equals(authType)) {
      final String token = properties.get("secure:accessToken");
      myCredentials = new TokenCredentials(token);
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
