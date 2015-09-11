package jetbrains.buildServer.issueTracker.github;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.issueTracker.AbstractIssueProvider;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueFetcherAuthenticator;
import jetbrains.buildServer.issueTracker.IssueProviderType;
import jetbrains.buildServer.issueTracker.github.credentials.GitHubAuthenticator;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueProvider extends AbstractIssueProvider {
  public GitHubIssueProvider(@NotNull IssueProviderType type, @NotNull IssueFetcher fetcher) {
    super(type.getType(), fetcher);
  }

  @NotNull
  @Override
  protected IssueFetcherAuthenticator getAuthenticator() {
    return new GitHubAuthenticator(myProperties);
  }

  @Override
  public void setProperties(@NotNull Map<String, String> map) {
    super.setProperties(map);
    myHost = map.get("repository");
    myFetchHost = myHost;
  }

  @NotNull
  @Override
  protected Pattern compilePattern(@NotNull Map<String, String> properties) {
    final Pattern result = super.compilePattern(properties);
    ((GitHubIssueFetcher)myFetcher).setPattern(result);
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
}
