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

import java.util.regex.Pattern;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import jetbrains.buildServer.util.cache.ResetCacheRegisterImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
    final EhCacheUtil cacheUtil = new EhCacheUtil(new ServerPaths(createTempDir().getAbsolutePath()),
                                                    EventDispatcher.create(BuildServerListener.class),
                                                    new ResetCacheRegisterImpl());

    myFetcher = new GitHubIssueFetcher(cacheUtil, () -> null);
    myFetcher.setPattern(Pattern.compile("#(\\d+)"));
  }

  @Test(enabled = false)
  public void testGetIssueAnonymously() throws Exception {
    IssueData data = myFetcher.getIssue("http://github.com/JetBrains/TeamCity.SharedResources", "#9", null);
    assertNotNull(data);
    System.out.println(data.toString());
  }

  @Test(enabled = false)
  public void testGHEAnonymously() throws Exception {
    IssueData data = myFetcher.getIssue("http://teamcity-github-enterprise.labs.intellij.net/orybak/ent-repo-public", "#1", null);
    assertNotNull(data);
    System.out.println(data.toString());
  }
}
