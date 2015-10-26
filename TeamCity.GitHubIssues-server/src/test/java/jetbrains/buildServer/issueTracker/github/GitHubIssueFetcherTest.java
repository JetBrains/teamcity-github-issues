package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.cache.EhCacheHelper;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import jetbrains.buildServer.util.cache.ResetCacheRegisterImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueFetcherTest extends BaseTestCase {

  private GitHubIssueFetcher myFetcher;

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    final EhCacheHelper helper = new EhCacheUtil(new ServerPaths(createTempDir().getAbsolutePath()),
                                                    EventDispatcher.create(BuildServerListener.class),
                                                    new ResetCacheRegisterImpl());

    myFetcher = new GitHubIssueFetcher(helper);
    myFetcher.setPattern(Pattern.compile("#(\\d+)"));
  }

  @Test
  public void testGetIssueAnonymously() throws Exception {
    IssueData data = myFetcher.getIssue("JetBrains/TeamCity.SharedResources", "#9", null);
    assertNotNull(data);
    System.out.println(data.toString());
  }
}
