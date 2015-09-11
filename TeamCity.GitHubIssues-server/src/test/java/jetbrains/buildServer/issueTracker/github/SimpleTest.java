package jetbrains.buildServer.issueTracker.github;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class SimpleTest {

  private void queryGitHub(GitHub gh) throws IOException {
    final GHRepository repo = gh.getRepository("orybak/mstest-legacy-provider");
    System.out.println(gh.getRateLimit());
    Assert.assertNotNull(repo);
    System.out.println(repo.getName());
    System.out.println(repo.getGitTransportUrl());
    System.out.println(repo.gitHttpTransportUrl());
  }

  @Test
  public void testConnect_Anonymous() throws Exception {
    final GitHub gh = getAnonymousGitHub();
    queryGitHub(gh);
  }

  private GitHub getAnonymousGitHub() throws IOException {
    return GitHub.connectAnonymously();
  }

  @Test
  public void testConnect_Token() throws Exception {
    final GitHub gh = getMyGitHub();
    queryGitHub(gh);
  }

  private GitHub getMyGitHub() throws IOException {
    return GitHub.connect("orybak", "4958bfb8793d375d247565ebbc1d67f34d067e7c");
  }

  @Test
  public void testGetAllOpenIssues() throws Exception {
    final GitHub gh = getMyGitHub();
    GHRepository repo = gh.getRepository("JetBrains/TeamCity.SharedResources");
    List<GHIssue> issues = repo.getIssues(GHIssueState.OPEN);
    for (GHIssue issue: issues) {
      System.out.println(issue.getNumber() + ": " + issue.getTitle() + " : " + issue.getBody());
    }
  }

}
