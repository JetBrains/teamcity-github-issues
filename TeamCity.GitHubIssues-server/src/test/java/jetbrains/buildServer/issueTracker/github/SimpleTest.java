package jetbrains.buildServer.issueTracker.github;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class SimpleTest {

  private void queryGitHub(GitHubClient gh) throws IOException {
    RepositoryService service = new RepositoryService(gh);
    Repository repo = service.getRepository("orybak", "mstest-legacy-provider");
    System.out.println(gh.getRequestLimit());
    System.out.println(gh.getRemainingRequests());
    Assert.assertNotNull(repo);
    System.out.println(repo.getName());
    System.out.println(repo.getGitUrl());
    System.out.println(repo.getHtmlUrl());
  }

  @Test
  public void testConnect_Anonymous() throws Exception {
    final GitHubClient gh = getAnonymousGitHub();
    queryGitHub(gh);
  }

  private GitHubClient getAnonymousGitHub() throws IOException {
    return new GitHubClient();
  }

  @Test(enabled = false)
  public void testConnect_Token() throws Exception {
    final GitHubClient gh = getMyGitHub();
    queryGitHub(gh);
  }

  private GitHubClient getMyGitHub() throws IOException {
    GitHubClient client = new GitHubClient();
    client.setOAuth2Token("sample_token");
    return client;
  }

  @Test
  public void testGetAllOpenIssues() throws Exception {

    final GitHubClient gh = getAnonymousGitHub();
    final List<Issue> issues = new IssueService(gh).getIssues("JetBrains", "TeamCity.SharedResources", new HashMap<String, String>() {{
      put(IssueService.FILTER_STATE, IssueService.STATE_OPEN);
    }});

    for (Issue issue: issues) {
      System.out.println(issue.getNumber() + ": " + issue.getTitle() + " : " + issue.getBody());
    }
  }

}
