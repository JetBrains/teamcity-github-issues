package jetbrains.buildServer.issueTracker.github.health;

import java.io.IOException;
import jetbrains.buildServer.serverSide.oauth.github.GitHubClientSSL;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jetbrains.annotations.NotNull;

public class HealthReportHelper {

  public boolean hasIssues(@NotNull final String owner, @NotNull final String repo) {
    try {
      final Repository repository = new RepositoryService(new GitHubClientSSL()).getRepository(owner, repo);
      return repository.isHasIssues();
    } catch (IOException ignored) {
    }
    return false;
  }
}
