package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.issueTracker.AbstractIssueProvider;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueFetcherAuthenticator;
import jetbrains.buildServer.issueTracker.IssueProviderType;
import jetbrains.buildServer.issueTracker.github.auth.GitHubAuthenticator;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.oauth.OAuthToken;
import jetbrains.buildServer.serverSide.oauth.OAuthTokensStorage;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces;
import static jetbrains.buildServer.issueTracker.github.GitHubConstants.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueProvider extends AbstractIssueProvider {

  private static final String TOKEN_PREFIX_OAUTH = "oauth:";

  private static final Pattern OAUTH_PATTERN = Pattern.compile("^" + TOKEN_PREFIX_OAUTH + "(.+):(.+):(.+)$");

  @NotNull
  private final OAuthTokensStorage myStorage;

  @NotNull
  private final UserModel myUserModel;

  public GitHubIssueProvider(@NotNull final IssueProviderType type,
                             @NotNull final IssueFetcher fetcher,
                             @NotNull final OAuthTokensStorage storage,
                             @NotNull final UserModel userModel) {
    super(type.getType(), fetcher);
    myStorage = storage;
    myUserModel = userModel;
  }

  @NotNull
  @Override
  protected IssueFetcherAuthenticator getAuthenticator() {
    return new GitHubAuthenticator(myProperties);
  }

  @Override
  public void setProperties(@NotNull Map<String, String> properties) {
    super.setProperties(properties);
    myHost = getHostProperty(properties);
    myFetchHost = myHost;
    myProperties.put("host", myHost);
    patchPropertiesWithToken();
  }

  @NotNull
  @Override
  protected Pattern compilePattern(@NotNull Map<String, String> properties) {
    final Pattern result = super.compilePattern(properties);
    ((GitHubIssueFetcher) myFetcher).setPattern(result);
    return result;
  }

  @NotNull
  @Override
  protected String extractId(@NotNull final String match) {
    Matcher m = myPattern.matcher(match);
    if (m.find() && m.groupCount() >= 1) {
      return m.group(1);
    } else {
      return super.extractId(match);
    }
  }

  @NotNull
  @Override
  public PropertiesProcessor getPropertiesProcessor() {
    return MY_PROCESSOR;
  }

  private static final PropertiesProcessor MY_PROCESSOR = new PropertiesProcessor() {
    public Collection<InvalidProperty> process(Map<String, String> map) {
      final List<InvalidProperty> result = new ArrayList<>();

      // authTokenType -
      String authTypeParam = map.get(PARAM_AUTH_TYPE);
      if (isEmptyOrSpaces(authTypeParam)) {
        authTypeParam = AUTH_ANONYMOUS;
      }
      // we have auth type. check against it
      if (authTypeParam.equals(AUTH_LOGINPASSWORD)) {
        checkNotEmptyParam(result, map, PARAM_USERNAME, "Username must be specified");
        checkNotEmptyParam(result, map, PARAM_PASSWORD, "Password must be specified");
      } else if (authTypeParam.equals(AUTH_ACCESSTOKEN)) {
        checkNotEmptyParam(result, map, PARAM_ACCESS_TOKEN, "Access token must be specified");
      }

      if (checkNotEmptyParam(result, map, PARAM_PATTERN, "Issue pattern must not be empty")) {
        try {
          String patternString = map.get(PARAM_PATTERN);
          //noinspection ResultOfMethodCallIgnored
          Pattern.compile(patternString);
        } catch (PatternSyntaxException e) {
          result.add(new InvalidProperty(PARAM_PATTERN, "Syntax of issue pattern is not correct"));
        }
      }

      if (checkNotEmptyParam(result, map, PARAM_REPOSITORY, "Repository must be specified")) {
        String repo = getHostProperty(map);
        try {
          new URL(repo);
        } catch (MalformedURLException e) {
          result.add(new InvalidProperty(PARAM_REPOSITORY, "Repository URL is not correct"));
        }
      }
      return result;
    }


    private boolean checkNotEmptyParam(@NotNull final Collection<InvalidProperty> invalid,
                                       @NotNull final Map<String, String> map,
                                       @NotNull final String propertyName,
                                       @NotNull final String errorMessage) {
      if (isEmptyOrSpaces(map.get(propertyName))) {
        invalid.add(new InvalidProperty(propertyName, errorMessage));
        return false;
      }
      return true;
    }
  };

  private static String getHostProperty(@NotNull final Map<String, String> properties) {
    String result = properties.get(PARAM_REPOSITORY);
    final Matcher matcher = OWNER_AND_REPO_PATTERN.matcher(result);
    if (matcher.matches()) {
      result = "https://github.com/" + matcher.group(1) + "/" + matcher.group(2);
    }
    return result;
  }

  /**
   * Replaces token 'coordinates with actual token'
   */
  private void patchPropertiesWithToken() {
    final String token = myProperties.get(PARAM_ACCESS_TOKEN);
    if (!StringUtil.isEmptyOrSpaces(token)) {
      if (token.startsWith(TOKEN_PREFIX_OAUTH)) {
        // oauth token
        final Matcher m = OAUTH_PATTERN.matcher(token);
        if (m.matches() && m.groupCount() == 3) {
          final SUser tokenUser = myUserModel.findUserById(Long.parseLong(m.group(1)));
          if (tokenUser != null) {
            final String providerId = m.group(2);
            final String oauthUserId = m.group(3);
            final Set<OAuthToken> tokens = myStorage.getUserTokens(providerId, tokenUser);
            OAuthToken result = null;
            for (OAuthToken t: tokens) {
              if (t.getOauthLogin().equals(oauthUserId)) {
                result = t;
              }
            }
            if (result != null) {
              myProperties.put(PARAM_ACCESS_TOKEN, result.getAccessToken());
            }
          }
        }
      }
    }
  }
}
