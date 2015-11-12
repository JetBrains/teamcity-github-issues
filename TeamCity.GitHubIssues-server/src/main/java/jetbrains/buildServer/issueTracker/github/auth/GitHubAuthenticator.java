package jetbrains.buildServer.issueTracker.github.auth;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.issueTracker.IssueFetcherAuthenticator;
import jetbrains.buildServer.serverSide.oauth.OAuthToken;
import jetbrains.buildServer.serverSide.oauth.OAuthTokensStorage;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jetbrains.buildServer.issueTracker.github.GitHubConstants.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubAuthenticator implements IssueFetcherAuthenticator {

  private static final String TOKEN_PREFIX_OAUTH = "oauth:";

  private static final Pattern OAUTH_PATTERN = Pattern.compile("^" + TOKEN_PREFIX_OAUTH + "(.+):(.+):(.+)$");

  private Credentials myCredentials = null;

  public GitHubAuthenticator(@NotNull final Map<String, String> properties,
                             @NotNull final OAuthTokensStorage storage,
                             @NotNull final UserModel model) {
    final String authType = properties.get(PARAM_AUTH_TYPE);
    if (AUTH_LOGINPASSWORD.equals(authType)) {
      final String username = properties.get(PARAM_USERNAME);
      final String password = properties.get(PARAM_PASSWORD);
      myCredentials = new UsernamePasswordCredentials(username, password);
    } else if (AUTH_ACCESSTOKEN.equals(authType)) {
      final String token = properties.get(PARAM_ACCESS_TOKEN);
      if (!StringUtil.isEmptyOrSpaces(token)) {
        if (token.startsWith(TOKEN_PREFIX_OAUTH)) {
          // oauth token
          final Matcher m = OAUTH_PATTERN.matcher(token);
          if (m.matches() && m.groupCount() == 3) {
            final SUser tokenUser = model.findUserById(Long.parseLong(m.group(1)));
            if (tokenUser != null) {
              final String providerId = m.group(2);
              final String oauthUserId = m.group(3);
              final Set<OAuthToken> tokens = storage.getUserTokens(providerId, tokenUser);
              OAuthToken result = null;
              for (OAuthToken t: tokens) {
                if (t.getOauthLogin().equals(oauthUserId)) {
                  result = t;
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
