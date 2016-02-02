package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.issueTracker.IssueProviderEx;
import jetbrains.buildServer.issueTracker.IssueProvidersManager;
import jetbrains.buildServer.issueTracker.github.health.IssueTrackerSuggestion;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.healthStatus.BuildTypeSuggestedItem;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.vcs.VcsRootInstance;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class SuggestionsTest extends BaseTestCase {

  private Mockery m;
  private SBuildType myBuildType;
  private PluginDescriptor myPluginDescriptor;
  private PagePlaces myPagePlaces;
  private IssueProvidersManager myManager;
  private GitHubIssueProviderType myType;
  private IssueTrackerSuggestion mySuggestion;
  private SProject myProject;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myBuildType = m.mock(SBuildType.class, "BuildType");
    myProject = m.mock(SProject.class, "Project");
    myPluginDescriptor = m.mock(PluginDescriptor.class);
    myPagePlaces = m.mock(PagePlaces.class);
    myManager = m.mock(IssueProvidersManager.class);
    m.checking(new Expectations() {{
      allowing(myPluginDescriptor);
      allowing(myPagePlaces);

      allowing(myBuildType).getProject();
      will(returnValue(myProject));
    }});
    myType = new GitHubIssueProviderType(myPluginDescriptor);
    mySuggestion = new IssueTrackerSuggestion(myPluginDescriptor, myPagePlaces, myManager, myType);
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    m.assertIsSatisfied();
  }

  @Test
  public void testNoVcsRoots() throws Exception {
    m.checking(new Expectations() {{
      oneOf(myManager).getProviders(myProject);
      will(returnValue(Collections.emptyMap()));

      oneOf(myBuildType).getVcsRootInstances();
      will(returnValue(Collections.emptyList()));

    }});
    final List<BuildTypeSuggestedItem> result = mySuggestion.getSuggestions(myBuildType);
    assertEmpty(result);
  }

  @Test
  @TestFor(issues = "TW-43781")
  public void testAlreadyUsed() throws Exception {
    final String id = "providerId123";
    final IssueProviderEx provider = m.mock(IssueProviderEx.class, "existing GitHub");
    m.checking(new Expectations() {{
      oneOf(myManager).getProviders(myProject);
      will(returnValue(Collections.singletonMap(id, provider)));

      oneOf(provider).getType();
      will(returnValue(myType.getType()));

    }});
    final List<BuildTypeSuggestedItem> result = mySuggestion.getSuggestions(myBuildType);
    assertEmpty(result);
  }

  @Test
  public void testNoGitHub() throws Exception {
    final String id = "providerId123";
    final IssueProviderEx provider = m.mock(IssueProviderEx.class, "other than GitHub");
    m.checking(new Expectations() {{
      oneOf(myManager).getProviders(myProject);
      will(returnValue(Collections.singletonMap(id, provider)));

      oneOf(provider).getType();
      will(returnValue("some other type"));

      oneOf(myBuildType).getVcsRootInstances();
      will(returnValue(Collections.emptyList()));

    }});
    final List<BuildTypeSuggestedItem> result = mySuggestion.getSuggestions(myBuildType);
    assertEmpty(result);
  }

  @Test
  public void testSSH() throws Exception {
    final String sourceUrl = "git@github.com:JetBrains/TeamCity.GitHubIssues.git";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @Test
  public void testHttp() throws Exception {
    final String sourceUrl = "http://github.com/JetBrains/TeamCity.GitHubIssues.git";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @Test
  public void testHttps() throws Exception {
    final String sourceUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues.git";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @SuppressWarnings("unchecked")
  private void testSingleUrl(final String sourceUrl, String expectedUrl) {
    final VcsRootInstance instance = m.mock(VcsRootInstance.class);
    m.checking(new Expectations() {{
      oneOf(myManager).getProviders(myProject);
      will(returnValue(Collections.emptyMap()));

      oneOf(myBuildType).getVcsRootInstances();
      will(returnValue(Collections.singletonList(instance)));

      oneOf(instance).getVcsName();
      will(returnValue("jetbrains.git"));

      oneOf(instance).getProperty("url");
      will(returnValue(sourceUrl));

      oneOf(myBuildType).getBuildTypeId();
      will(returnValue("buildTypeId"));
    }});

    final List<BuildTypeSuggestedItem> result = mySuggestion.getSuggestions(myBuildType);
    assertEquals(1, result.size());
    final Map<String, Object> data = result.get(0).getAdditionalData();
    assertNotNull(data);
    final List<Map<String, Object>> suggestedTrackers = (List<Map<String, Object>>)data.get("suggestedTrackers");
    assertNotNull(suggestedTrackers);
    assertEquals(1, suggestedTrackers.size());
    assertEquals(expectedUrl, suggestedTrackers.get(0).get("repoUrl"));
  }

}
