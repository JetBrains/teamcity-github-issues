package jetbrains.buildServer.issueTracker.github;

import com.intellij.openapi.diagnostic.Logger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.issueTracker.AbstractIssueFetcher;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.issueTracker.github.auth.TokenCredentials;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.cache.EhCacheHelper;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueFetcher extends AbstractIssueFetcher {

  private static final Logger LOG = Loggers.ISSUE_TRACKERS;

  public GitHubIssueFetcher(@NotNull final EhCacheHelper helper) {
    super(helper);
  }

  @NotNull
  public IssueData getIssue(@NotNull String host, // repository url
                            @NotNull String id,   // issue id
                            @Nullable Credentials credentials) throws Exception {
    final String issueURL = getUrl(host, id);
    final String issueId = getIssueId(id);
    URL url;
    try {
      url = new URL(host);
      final Matcher m = GitHubConstants.OWNER_AND_REPO_PATTERN.matcher(url.getPath());
      if (!m.matches()) {
        throw new IllegalArgumentException("URL + [" + url.toString() + "] does not contain owner and repository info");
      }
      return getFromCacheOrFetch(issueURL, new MyFetchFunction(url, m.group(1), m.group(2), issueId, credentials));
    } catch (MalformedURLException e) {
      LOG.warn(e);
      throw new RuntimeException(e);
    }
  }

  private String getIssueId(@NotNull final String idString) {
    final Matcher matcher = myPattern.matcher(idString);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return idString;
    }
  }

  @NotNull
  public String getUrl(@NotNull String host, @NotNull String id) {
    return String.format("%s/issues/%s", host, getIssueId(id));
  }

  private Pattern myPattern;

  public void setPattern(final Pattern pattern) {
    myPattern = pattern;
  }

  private static class MyFetchFunction implements FetchFunction {

    @NotNull
    private final URL myURL;

    @NotNull
    private final String myOwner;

    @NotNull
    private final String myRepo;

    @NotNull
    private final String myId;

    @Nullable
    private final Credentials myCredentials;

    public MyFetchFunction(@NotNull final URL url,
                           @NotNull final String owner,
                           @NotNull final String repo,
                           @NotNull final String id,
                           @Nullable final Credentials credentials) {
      myURL = url;
      myOwner = owner;
      myRepo = repo;
      myId = id;
      myCredentials = credentials;
    }

    @NotNull
    public IssueData fetch() throws Exception {
      GitHubClient client;
      if ("github.com".equals(myURL.getHost())) {
        client = new GitHubClient();
      } else {
        client = new GitHubClient(myURL.getHost(), myURL.getPort(), myURL.getProtocol());
      }
      if (myCredentials == null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myURL.toString() + "anonymously");
        }
      } else if (myCredentials instanceof TokenCredentials) {
        final String token = ((TokenCredentials) myCredentials).getToken();
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myURL.toString() + "using token starting with [" + token.substring(0, Math.min(2, token.length())) + "]");
        }
        client.setOAuth2Token(token);
      } else {
        UsernamePasswordCredentials cr = (UsernamePasswordCredentials)myCredentials;
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myURL.toString() +  "using username + [" + cr.getUserName() + "] and password");
        }
        client.setCredentials(cr.getUserName(), cr.getPassword());
      }
      return createIssueData(new IssueService(client).getIssue(myOwner, myRepo, myId));
    }
  }

  private static IssueData createIssueData(final Issue issue) {
    return new IssueData(Integer.toString(issue.getNumber()), issue.getTitle(), issue.getState(), issue.getHtmlUrl(), IssueService.STATE_CLOSED.equals(issue.getState()));
  }
}
