package jetbrains.buildServer.issueTracker.github;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.issueTracker.AbstractIssueFetcher;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueFetcher extends AbstractIssueFetcher {

  public GitHubIssueFetcher(@NotNull final EhCacheUtil cacheUtil) {
    super(cacheUtil);
  }

  @NotNull
  public IssueData getIssue(@NotNull String host,
                            @NotNull String id,
                            @Nullable Credentials credentials) throws Exception {
    // host = "owner/repo"
    String url = getUrl(host, id);
    String[] strs = host.split("/");
    String realId = getRealId(id);
    return getFromCacheOrFetch(url, new MyFetchFunction(strs[0], strs[1], realId, credentials));
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
    private final String myOwner;
    @NotNull
    private final String myRepo;
    @NotNull
    private final String myIdString;
    @Nullable
    private final Credentials myCredentials;

    public MyFetchFunction(@NotNull final String owner,
                           @NotNull final String repo,
                           @NotNull final String idString,
                           @Nullable final Credentials credentials) {


      myOwner = owner;
      myRepo = repo;
      myIdString = idString;
      myCredentials = credentials;
    }

    @NotNull
    public IssueData fetch() throws Exception {
      if (myCredentials == null) {
        final GitHub gitHub = GitHub.connectAnonymously();
        GHRepository repository = gitHub.getRepository(myOwner + "/" + myRepo); // why split then join?
        // todo: here match myIdString to regexp
        GHIssue issue = repository.getIssue(Integer.parseInt(myIdString));
        return createIssueData(issue);
      }
      throw new NotImplementedException("Hello!");
    }
  }




  static IssueData createIssueData(final GHIssue issue) {
    // issue == null?
    return new IssueData(Integer.toString(issue.getId()), issue.getTitle(), issue.getState().name(), issue.getUrl().toString(), issue.getState() == GHIssueState.CLOSED);
  }
}
