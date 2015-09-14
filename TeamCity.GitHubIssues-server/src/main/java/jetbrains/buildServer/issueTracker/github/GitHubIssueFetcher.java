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
    String realId = getRealId(id);
    return getFromCacheOrFetch(url, new MyFetchFunction(host, realId, credentials));
  }

  private String getRealId(final @NotNull String id) {
    String realId;
    final Matcher matcher = myPattern.matcher(id);
    if (matcher.find()) {
      realId = matcher.group(1);
    } else {
      realId = id;// throw exception
    }
    return realId;
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

    @NotNull
    private final String myIdString;
    @Nullable
    private final Credentials myCredentials;

    public MyFetchFunction(@NotNull final String ownerAnRepo,
                           @NotNull final String idString,
                           @Nullable final Credentials credentials) {
      myOwnerAnRepo = ownerAnRepo;
      myIdString = idString;
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
        UsernamePasswordCredentials creds = (UsernamePasswordCredentials)myCredentials;
        if (LOG.isDebugEnabled()) {
          LOG.debug("Connecting to " + myOwnerAnRepo + "using username + [" + creds.getUserName() + "] and password");
        }
        gitHub = GitHub.connectUsingPassword(creds.getUserName(), creds.getPassword());
      }
      GHRepository repository = gitHub.getRepository(myOwnerAnRepo); // why split then join?
      // todo: here match myIdString to regexp
      GHIssue issue = repository.getIssue(Integer.parseInt(myIdString));
      return createIssueData(issue);
    }
  }

  static IssueData createIssueData(final GHIssue issue) {
    // issue == null?
    return new IssueData(Integer.toString(issue.getId()), issue.getTitle(), issue.getState().name(), issue.getUrl().toString(), issue.getState() == GHIssueState.CLOSED);
  }
}
