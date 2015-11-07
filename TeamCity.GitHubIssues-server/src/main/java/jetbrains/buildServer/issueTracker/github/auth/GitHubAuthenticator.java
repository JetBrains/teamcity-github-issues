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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubAuthenticator implements IssueFetcherAuthenticator {

  private static final Pattern OAUTH_PATTERN = Pattern.compile("^oauth:(.+):(.+)$");

  private Credentials myCredentials = null;

  public GitHubAuthenticator(@NotNull final Map<String, String> properties,
                             @NotNull final PersonalOAuthTokens personalTokens) {
    final String authType = properties.get(GitHubConstants.PARAM_AUTH_TYPE);
    if (GitHubConstants.AUTH_LOGINPASSWORD.equals(authType)) {
      final String username = properties.get(GitHubConstants.PARAM_USERNAME);
      final String password = properties.get(GitHubConstants.PARAM_PASSWORD);
      myCredentials = new UsernamePasswordCredentials(username, password);
    } else if (GitHubConstants.AUTH_ACCESSTOKEN.equals(authType)) {
      final String token = properties.get(GitHubConstants.PARAM_ACCESS_TOKEN);
      if (!StringUtil.isEmptyOrSpaces(token)) {
        if (token.startsWith("oauth:")) {
          // oauth token
          final Matcher m = OAUTH_PATTERN.matcher(token);
          if (m.matches() && m.groupCount() == 2) {
            final String username = m.group(1);
            final String providerId = m.group(2);
            final Set<PersonalOAuthTokens.Token> tokens = personalTokens.getTokens(providerId);
            if (!tokens.isEmpty()) {
              PersonalOAuthTokens.Token result = null;
              for (PersonalOAuthTokens.Token t: tokens) {
                if (t.getOwner().equals(username)) {
                  result = t;
                  break;
                }
              }
              if (result != null) {
                myCredentials = new TokenCredentials(result.getToken());
              }
            }
          }
        } else {
          // personal token
          myCredentials = new TokenCredentials(token);
        }
      }
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
