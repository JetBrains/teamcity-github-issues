package jetbrains.buildServer.issueTracker.github.health;

import jetbrains.buildServer.serverSide.IOGuard;
import jetbrains.buildServer.serverSide.oauth.github.GitHubClientSSL;
import jetbrains.buildServer.util.FuncThrow;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jetbrains.annotations.NotNull;

public class HealthReportHelper {

  public boolean hasIssues(@NotNull final String owner, @NotNull final String repo) {
    try {
      return IOGuard.allowNetworkCall((FuncThrow<Boolean, Exception>)() -> {
        final Repository repository = new RepositoryService(new GitHubClientSSL()).getRepository(owner, repo);
        return repository.isHasIssues();
      });
    } catch (Exception ignored) {
    }
    return false;
  }
}
