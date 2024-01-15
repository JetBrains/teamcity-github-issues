

package jetbrains.buildServer.issueTracker.github.auth;

import org.apache.commons.httpclient.Credentials;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class TokenCredentials implements Credentials {

  @NotNull
  private final String myToken;

  public TokenCredentials(@NotNull final String token) {
    myToken = token;
  }

  @NotNull
  public String getToken() {
    return myToken;
  }
}