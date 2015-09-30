package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.issueTracker.AbstractIssueFetcher;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.issueTracker.github.credentials.TokenCredentials;
import jetbrains.buildServer.util.cache.EhCacheHelper;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

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
    private final String myOwnerAnRepo;

    private final int myId;

    @Nullable
    private final Credentials myCredentials;

    public MyFetchFunction(@NotNull final String ownerAnRepo,
                           final int id,
                           @Nullable final Credentials credentials) {
      myOwnerAnRepo = ownerAnRepo;
      myId = id;
      myCredentials = credentials;
    }

    @NotNull
    public IssueData fetch() throws Exception {
      final GitHub gitHub;
      if (myCredentials == null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myOwnerAnRepo + "anonymously");
        }
        gitHub = GitHub.connectAnonymously();
      } else if (myCredentials instanceof TokenCredentials) {
        final String token = ((TokenCredentials) myCredentials).getToken();
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myOwnerAnRepo + "using token starting with [" + token.substring(0, Math.min(2, token.length())) + "]");
        }
        gitHub = GitHub.connectUsingOAuth(token);
      } else {
        UsernamePasswordCredentials cr = (UsernamePasswordCredentials)myCredentials;
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myOwnerAnRepo + "using username + [" + cr.getUserName() + "] and password");
        }
        gitHub = GitHub.connectUsingPassword(cr.getUserName(), cr.getPassword());
      }
      GHRepository repository = gitHub.getRepository(myOwnerAnRepo);
      GHIssue issue = repository.getIssue(myId);
      return createIssueData(issue);
    }
  }

  private static IssueData createIssueData(final GHIssue issue) {
    return new IssueData(Integer.toString(issue.getNumber()), issue.getTitle(), issue.getState().name(), issue.getUrl().toString(), issue.getState() == GHIssueState.CLOSED);
  }
}
