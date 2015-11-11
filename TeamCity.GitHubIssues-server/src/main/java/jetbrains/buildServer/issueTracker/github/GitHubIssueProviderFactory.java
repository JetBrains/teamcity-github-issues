package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.issueTracker.AbstractIssueProviderFactory;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueProvider;
import jetbrains.buildServer.issueTracker.IssueProviderType;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueProviderFactory extends AbstractIssueProviderFactory {


  public GitHubIssueProviderFactory(@NotNull IssueProviderType type,
                                    @NotNull IssueFetcher fetcher) {
    super(type, fetcher);
  }

  @NotNull
  public IssueProvider createProvider() {
    return new GitHubIssueProvider(myType, myFetcher);
  }
}
