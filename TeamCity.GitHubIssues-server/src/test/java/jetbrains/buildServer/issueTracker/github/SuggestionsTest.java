/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.issueTracker.github;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.issueTracker.IssueProviderEx;
import jetbrains.buildServer.issueTracker.IssueProvidersManager;
import jetbrains.buildServer.issueTracker.github.health.HealthReportHelper;
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
  private IssueTrackerSuggestion mySuggestion;
  private SProject myProject;

  private MockHelper myHelper;

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
    myHelper = new MockHelper();
    mySuggestion = new IssueTrackerSuggestion(myPluginDescriptor, myPagePlaces, myManager, myHelper);
    myHelper.setExpectedResult(true);
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    m.assertIsSatisfied();
  }

  @Test
  public void testNoVcsRoots() {
    m.checking(new Expectations() {{
      oneOf(myManager).getProviders(myProject);
      will(returnValue(Collections.emptyList()));

      oneOf(myBuildType).getVcsRoots();
      will(returnValue(Collections.emptyList()));

    }});
    checkSuggestions(mySuggestion.getSuggestions(myProject));
  }

  @Test
  @TestFor(issues = "TW-43781")
  public void testAlreadyUsed() {
    final IssueProviderEx provider = m.mock(IssueProviderEx.class, "existing GitHub");
    m.checking(new Expectations() {{
      oneOf(myManager).getProviders(myProject);
      will(returnValue(Collections.singletonList(provider)));

      oneOf(provider).getType();
      will(returnValue(GitHubIssueProviderType.TYPE));

    }});
    checkSuggestions(mySuggestion.getSuggestions(myProject));
  }

  @Test
  public void testNoGitHub() {
    final IssueProviderEx provider = m.mock(IssueProviderEx.class, "other than GitHub");
    m.checking(new Expectations() {{
      oneOf(myManager).getProviders(myProject);
      will(returnValue(Collections.singletonList(provider)));

      oneOf(provider).getType();
      will(returnValue("some other type"));

      oneOf(myBuildType).getVcsRoots();
      will(returnValue(Collections.emptyList()));

    }});
    checkSuggestions(mySuggestion.getSuggestions(myProject));
  }

  @Test
  @TestFor(issues = "")
  public void testNoIssues() {
    myHelper.setExpectedResult(false);
    final String sourceUrl = "git@github.com:JetBrains/TeamCity.GitHubIssues.git";
    testSingleUrl(sourceUrl, null);
  }

  @Test
  public void testSSH() {
    final String sourceUrl = "git@github.com:JetBrains/TeamCity.GitHubIssues.git";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @Test
  public void testHttp() {
    final String sourceUrl = "http://github.com/JetBrains/TeamCity.GitHubIssues.git";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @Test
  public void testHttps() {
    final String sourceUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues.git";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @Test
  public void testNoDotGitHttp() {
    final String sourceUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @Test
  public void testNoDotGitHttps() {
    final String sourceUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    final String expectedUrl = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    testSingleUrl(sourceUrl, expectedUrl);
  }

  @Test
  public void testParametrizedUrl() {
    final String sourceParametrizedUrl = "https://github.com/%project.owner%/%project.repo%";
    final String sourceUrl1 = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    final String sourceUrl2 = "https://github.com/orybak/TeamCity.GitHubIssues";
    final String expectedUrl1 = "https://github.com/JetBrains/TeamCity.GitHubIssues";
    final String expectedUrl2 = "https://github.com/orybak/TeamCity.GitHubIssues";

    final VcsRoot vcsRoot = m.mock(VcsRoot.class, "parametrized-url");
    final VcsRootInstance instance1 = m.mock(VcsRootInstance.class, "real-instance-1");
    final VcsRootInstance instance2 = m.mock(VcsRootInstance.class, "real-instance-2");

    m.checking(new Expectations() {{
      oneOf(myManager).getProviders(myProject);
      will(returnValue(Collections.emptyList()));

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

  @SuppressWarnings("SameParameterValue")
  private void testSingleUrl(final String sourceUrl, String expectedUrl) {
    final VcsRoot root = m.mock(VcsRoot.class);
    m.checking(new Expectations() {{
      oneOf(myManager).getProviders(myProject);
      will(returnValue(Collections.emptyList()));

      oneOf(myBuildType).getVcsRoots();
      will(returnValue(Collections.singletonList(root)));

      oneOf(root).getVcsName();
      will(returnValue("jetbrains.git"));

      oneOf(root).getProperty("url");
      will(returnValue(sourceUrl));
    }});
    if (expectedUrl != null) {
      checkSuggestions(mySuggestion.getSuggestions(myProject), expectedUrl);
    } else {
      assertEmpty(mySuggestion.getSuggestions(myProject));
    }
  }

  @SuppressWarnings("unchecked")
  private void checkSuggestions(@NotNull final List<ProjectSuggestedItem> suggestions, String... expected) {
    if (expected.length != 0) {
      assertEquals(1, suggestions.size());
      final Map<String, Object> data = suggestions.get(0).getAdditionalData();
      assertNotNull(data);
      final Map<String, Map<String, Object>> suggestedTrackers = (Map<String, Map<String, Object>>) data.get("suggestedTrackers");
      assertNotNull(suggestedTrackers);
      assertContains(suggestedTrackers.keySet(), expected);
      for (String key: expected) {
        Map<String, Object> value = suggestedTrackers.get(key);
        assertNotNull(value);
        assertEquals(key, value.get("repoUrl"));
      }
    } else {
      assertEmpty(suggestions);
    }
  }

  private class MockHelper extends HealthReportHelper {

    private boolean myExpectedResult = true;

    public void setExpectedResult(boolean expectedResult) {
      myExpectedResult = expectedResult;
    }

    @Override
    public boolean hasIssues(@NotNull String owner, @NotNull String repo) {
      return myExpectedResult;
    }
  }
}
