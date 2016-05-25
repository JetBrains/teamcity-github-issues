package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.issueTracker.IssueProviderEx;
import jetbrains.buildServer.issueTracker.IssueProvidersManager;
import jetbrains.buildServer.issueTracker.github.health.IssueTrackerSuggestion;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.healthStatus.ProjectSuggestedItem;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.vcs.VcsRootInstance;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

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

      allowing(myProject).getOwnBuildTypes();
      will(returnValue(Collections.singletonList(myBuildType)));

      allowing(myProject).getProjectId();
      will(returnValue("PROJECT_ID"));

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
      oneOf(myManager).getOwnProviders(myProject);
      will(returnValue(Collections.emptyMap()));

      oneOf(myBuildType).getVcsRoots();
      will(returnValue(Collections.emptyList()));

    }});
    checkSuggestions(mySuggestion.getSuggestions(myProject));
  }

  @Test
  @TestFor(issues = "TW-43781")
  public void testAlreadyUsed() throws Exception {
    final String id = "providerId123";
    final IssueProviderEx provider = m.mock(IssueProviderEx.class, "existing GitHub");
    m.checking(new Expectations() {{
      oneOf(myManager).getOwnProviders(myProject);
      will(returnValue(Collections.singletonMap(id, provider)));

      oneOf(provider).getType();
      will(returnValue(myType.getType()));

    }});
    checkSuggestions(mySuggestion.getSuggestions(myProject));
  }

  @Test
  public void testNoGitHub() throws Exception {
    final String id = "providerId123";
    final IssueProviderEx provider = m.mock(IssueProviderEx.class, "other than GitHub");
    m.checking(new Expectations() {{
      oneOf(myManager).getOwnProviders(myProject);
      will(returnValue(Collections.singletonMap(id, provider)));

      oneOf(provider).getType();
      will(returnValue("some other type"));

      oneOf(myBuildType).getVcsRoots();
      will(returnValue(Collections.emptyList()));

    }});
    checkSuggestions(mySuggestion.getSuggestions(myProject));
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

  @Test
  public void testNoDotGitHttp() throws Exception {
    final String sourceUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @Test
  public void testNoDotGitHttps() throws Exception {
    final String sourceUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @Test
  public void testParametrizedUrl() throws Exception {
    final String sourceParametrizedUrl = "https://github.com/%project.owner%/%project.repo%";
    final String sourceUrl1 = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    final String sourceUrl2 = "https://github.com/orybak/TeamCity.GitHubIssues";
    final String expectedUrl1 = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    final String expectedUrl2 = "https://github.com/orybak/TeamCity.GitHubIssues";

    final VcsRoot vcsRoot = m.mock(VcsRoot.class, "parametrized-url");
    final VcsRootInstance instance1 = m.mock(VcsRootInstance.class, "real-instance-1");
    final VcsRootInstance instance2 = m.mock(VcsRootInstance.class, "real-instance-2");

    m.checking(new Expectations() {{
      oneOf(myManager).getOwnProviders(myProject);
      will(returnValue(Collections.emptyMap()));

      oneOf(myBuildType).getVcsRoots();
      will(returnValue(Collections.singletonList(vcsRoot)));

      oneOf(vcsRoot).getVcsName();
      will(returnValue("jetbrains.git"));

      oneOf(vcsRoot).getProperty("url");
      will(returnValue(sourceParametrizedUrl));

      oneOf(myBuildType).getVcsRootInstances();
      will(returnValue(Arrays.asList(instance1, instance2)));

      oneOf(instance1).getVcsName();
      will(returnValue("jetbrains.git"));

      oneOf(instance1).getProperty("url");
      will(returnValue(sourceUrl1));

      oneOf(instance2).getVcsName();
      will(returnValue("jetbrains.git"));

      oneOf(instance2).getProperty("url");
      will(returnValue(sourceUrl2));
    }});
    checkSuggestions(mySuggestion.getSuggestions(myProject), expectedUrl1, expectedUrl2);
  }

  @SuppressWarnings("unchecked")
  private void testSingleUrl(final String sourceUrl, String expectedUrl) {
    final VcsRoot root = m.mock(VcsRoot.class);
    m.checking(new Expectations() {{
      oneOf(myManager).getOwnProviders(myProject);
      will(returnValue(Collections.emptyMap()));

      oneOf(myBuildType).getVcsRoots();
      will(returnValue(Collections.singletonList(root)));

      oneOf(root).getVcsName();
      will(returnValue("jetbrains.git"));

      oneOf(root).getProperty("url");
      will(returnValue(sourceUrl));
    }});
    checkSuggestions(mySuggestion.getSuggestions(myProject), expectedUrl);
  }

  @SuppressWarnings("unchecked")
  private void checkSuggestions(@NotNull final List<ProjectSuggestedItem> suggestions, String... expected) {
    if (expected.length != 0) {
      assertEquals(1, suggestions.size());
      final Map<String, Object> data = suggestions.get(0).getAdditionalData();
      assertNotNull(data);
      final Map<String, Map<String, Object>> suggestedTrackers = (Map<String, Map<String, Object>>) data.get("suggestedTrackers");
      assertNotNull(suggestedTrackers);
      assertEquals(expected.length, suggestedTrackers.size());
      assertContains(suggestedTrackers.keySet(), expected);
      for (String key : expected) {
        Map<String, Object> value = suggestedTrackers.get(key);
        assertNotNull(value);
        assertEquals(key, value.get("repoUrl"));
      }
    } else {
      assertEmpty(suggestions);
    }
  }
}
