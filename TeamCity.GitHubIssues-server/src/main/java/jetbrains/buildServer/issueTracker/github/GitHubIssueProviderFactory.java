

package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.issueTracker.AbstractIssueProviderFactory;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueProvider;
import jetbrains.buildServer.issueTracker.IssueProviderType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.oauth.OAuthTokensStorage;
import jetbrains.buildServer.users.UserModel;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueProviderFactory extends AbstractIssueProviderFactory {

  @NotNull
  private final OAuthTokensStorage myStorage;

  @NotNull
  private final UserModel myUserModel;

  public GitHubIssueProviderFactory(@NotNull final IssueProviderType type,
                                    @NotNull final IssueFetcher fetcher,
                                    @NotNull final OAuthTokensStorage storage,
                                    @NotNull final UserModel userModel) {
    super(type, fetcher);
    myStorage = storage;
    myUserModel = userModel;
  }

  @NotNull
  public IssueProvider createProvider(@NotNull SProject project) {
    return new GitHubIssueProvider(myType, myFetcher, myStorage, myUserModel, project);
  }
}