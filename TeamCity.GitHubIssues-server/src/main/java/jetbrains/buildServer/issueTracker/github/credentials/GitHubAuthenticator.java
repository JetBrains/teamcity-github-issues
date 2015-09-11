package jetbrains.buildServer.issueTracker.github.credentials;

import java.util.Map;
import jetbrains.buildServer.issueTracker.IssueFetcherAuthenticator;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubAuthenticator implements IssueFetcherAuthenticator {

  public GitHubAuthenticator(@NotNull final Map<String, String> properties) {
    // no username/password - anonymous
    // only password - token
    // username and password - proper auth
  }

  public boolean isBasicAuth() {

    //
    return false;
  }

  public void applyAuthScheme(@NotNull final HttpMethod httpMethod) {
  }

  @Nullable
  public Credentials getCredentials() {
    return null;
  }
}
