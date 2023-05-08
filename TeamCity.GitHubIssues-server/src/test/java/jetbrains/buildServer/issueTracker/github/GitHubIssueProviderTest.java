/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionsManager;
import jetbrains.buildServer.serverSide.oauth.OAuthTokensStorage;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueProviderTest extends BaseTestCase {

  private Mockery m;

  private PluginDescriptor myDescriptor;
  private GitHubIssueProviderType myType;
  private GitHubIssueProvider myProvider;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery() {{
      setImposteriser(ClassImposteriser.INSTANCE);
    }};
    myDescriptor = m.mock(PluginDescriptor.class);
    m.checking(new Expectations() {{
      allowing(myDescriptor).getPluginResourcesPath(with(any(String.class)));
      will(returnValue("path"));
    }});
    myType = new GitHubIssueProviderType(myDescriptor);
    final GitHubIssueFetcher fetcher = m.mock(GitHubIssueFetcher.class);
    m.checking(new Expectations() {{
      allowing(fetcher);
    }});
    final OAuthTokensStorage storage = m.mock(OAuthTokensStorage.class);
    final UserModel model = m.mock(UserModel.class);
    m.checking(new Expectations() {{
      allowing(storage);
      allowing(model);
    }});
    final GitHubIssueProviderFactory factory = new GitHubIssueProviderFactory(myType, fetcher, storage, model, m.mock(OAuthConnectionsManager.class));
    myProvider = (GitHubIssueProvider) factory.createProvider(m.mock(SProject.class));
  }

  @Test
  public void testSetProperties_3rdPartyProvider() throws Exception {
    myProvider.setProperties(getProperties("repo/owner"));
    assertEquals("https://github.com/repo/owner", myProvider.getProperties().get("host"));
  }

  @Test
  public void testSetProperties_NewProvider() throws Exception {
    myProvider.setProperties(getProperties("https://github.com/repo/owner"));
    assertEquals("https://github.com/repo/owner", myProvider.getProperties().get("host"));
  }

  @Test
  public void testValidate_3rdPartyProvider_Valid() throws Exception {
    assertEmpty(myProvider.getPropertiesProcessor().process(getProperties("repo/owner")));
  }

  @Test
  @TestFor(issues = "TW-45803")
  public void testValidate_3rdPartyProvider_NoAuthType() throws Exception {
    final Map<String, String> properties = getProperties("repo/owner");
    properties.remove(GitHubConstants.PARAM_AUTH_TYPE);
    assertEmpty(myProvider.getPropertiesProcessor().process(properties));
  }

  @Test
  public void testValidate_NewProvider_Valid() throws Exception {
    assertEmpty(myProvider.getPropertiesProcessor().process(getProperties("https://github.com/repo/owner")));
  }

  private Map<String, String> getProperties(@NotNull final String repo) {
    final Map<String, String> result = new HashMap<>(myType.getDefaultProperties());
    result.put(GitHubConstants.PARAM_REPOSITORY, repo);
    return result;
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    m.assertIsSatisfied();
  }
}
