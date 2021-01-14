/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubClientTest {

  private String accessToken = "";

  @BeforeClass
  public void setUp() throws Exception {
    accessToken = System.getProperty("github.accessToken", "");
  }

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

  @Test
  public void testConnect_Token() throws Exception {
    final GitHubClient gh = getGitHubWithToken();
    queryGitHub(gh);
  }

  private GitHubClient getGitHubWithToken() throws IOException, SkipException {
    if ("".equals(accessToken)) {
      throw new SkipException("Please specify access token for tests using -DaccessToken=your_token");
    }

    GitHubClient client = new GitHubClient();
    client.setOAuth2Token(accessToken);
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
