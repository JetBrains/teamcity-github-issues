package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.issueTracker.AbstractIssueFetcher;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.issueTracker.github.auth.TokenCredentials;
import jetbrains.buildServer.util.cache.EhCacheHelper;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueFetcher extends AbstractIssueFetcher {

  private static final Logger LOG = Logger.getLogger(GitHubIssueFetcher.class.getName());

  public GitHubIssueFetcher(@NotNull final EhCacheHelper helper) {
    super(helper);
  }

  @NotNull
  public IssueData getIssue(@NotNull String host,
                            @NotNull String id,
                            @Nullable Credentials credentials) throws Exception {
    String url = getUrl(host, id);
    int realId = getRealId(id);
    return getFromCacheOrFetch(url, new MyFetchFunction(host, realId, credentials));
  }

  private int getRealId(@NotNull final String idString) {
    final Matcher matcher = myPattern.matcher(idString);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    } else {
      return Integer.parseInt(idString);
    }
  }

  @NotNull
  public String getUrl(@NotNull String host, @NotNull String id) {
    return String.format("http://github.com/%s/issues/%s", host, getRealId(id));
  }

  private Pattern myPattern;

  public void setPattern(final Pattern pattern) {
    myPattern = pattern;
  }

  private static class MyFetchFunction implements FetchFunction {

    @NotNull
    private final String myOwner;

    @NotNull
    private final String myRepo;

    private final int myId;

    @Nullable
    private final Credentials myCredentials;

    public MyFetchFunction(@NotNull final String ownerAnRepo,
                           final int id,
                           @Nullable final Credentials credentials) {
      final String[] str = ownerAnRepo.split("/");
      myOwner = str[0];
      myRepo = str[1];
      myId = id;
      myCredentials = credentials;
    }

    @NotNull
    public IssueData fetch() throws Exception {
      final GitHubClient client = new GitHubClient();
      if (myCredentials == null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myOwner + "/" + myRepo + "anonymously");
        }
      } else if (myCredentials instanceof TokenCredentials) {
        final String token = ((TokenCredentials) myCredentials).getToken();
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myOwner + "/" + myRepo + "using token starting with [" + token.substring(0, Math.min(2, token.length())) + "]");
        }
        client.setOAuth2Token(token);
      } else {
        UsernamePasswordCredentials cr = (UsernamePasswordCredentials)myCredentials;
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myOwner + "/" + myRepo + "using username + [" + cr.getUserName() + "] and password");
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
