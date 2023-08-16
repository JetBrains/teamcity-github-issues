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
import jetbrains.buildServer.issueTracker.IssueTrackerConstants;
import jetbrains.buildServer.issueTracker.github.auth.GitHubAuthenticator;
import jetbrains.buildServer.issueTracker.github.auth.TokenCredentials;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.oauth.OAuthToken;
import jetbrains.buildServer.serverSide.oauth.OAuthTokensStorage;
import jetbrains.buildServer.util.TestFor;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Oleg Rybak <oleg.rybak@jetbrains.com>
 */
public class GitHubAuthenticatorTest extends BaseTestCase {

  private Map<String, String> myProperties;

  private Mockery m;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myProperties = new HashMap<String, String>();
    m = new Mockery() {{
      setImposteriser(ClassImposteriser.INSTANCE);
    }};
  }

  @Test
  public void testAnonymous() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ANONYMOUS);
    final Credentials result = getCredentials();
    assertNull(result);
  }

  @Test
  @TestFor(issues = "TW-45803")
  public void testEmpty_Anonymous_OldPluginCompatible() throws Exception {
    final Credentials result = getCredentials();
    assertNull(result);
  }

  @Test
  public void testLoginPassword() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_LOGINPASSWORD);
    myProperties.put(GitHubConstants.PARAM_USERNAME, "user1");
    myProperties.put(GitHubConstants.PARAM_PASSWORD, "veryinsecurepassword");
    final Credentials result = getCredentials();
    assertNotNull(result);
    assertTrue(result instanceof UsernamePasswordCredentials);
    final UsernamePasswordCredentials creds = (UsernamePasswordCredentials) result;
    assertEquals("user1", creds.getUserName());
    assertEquals("veryinsecurepassword", creds.getPassword());
  }

  @Test
  public void testToken() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "29505c65d0c8529c9e66c419f6480c4a");
    final Credentials result = getCredentials();
    assertNotNull(result);
    assertTrue(result instanceof TokenCredentials);
    assertEquals("29505c65d0c8529c9e66c419f6480c4a", ((TokenCredentials)result).getToken());
  }

  @Test
  public void testToken_OAuth_NoTokenInIssueProvider() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    final Credentials result = getCredentials();
    assertNull(result);
  }

  @Test
  public void testToken_OAuth_GitHubApp() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, IssueTrackerConstants.AUTH_STORED_TOKEN);
    myProperties.put(IssueTrackerConstants.PARAM_TOKEN_ID, "tc_token_id:APP:-1:dasdasdasda");
    final Credentials result = getCredentials();
    assertNotNull(result);
    assertTrue(result instanceof  TokenCredentials);
    assertEquals("token_value", ((TokenCredentials)result).getToken());
  }

  private Credentials getCredentials() {
    SProject project = m.mock(SProject.class);

    OAuthTokensStorage tokenStorage = m.mock(OAuthTokensStorage.class);
    m.checking(new Expectations() {{
      allowing(tokenStorage).getRefreshableToken(with(any(SProject.class)), with(any(String.class)));
      will(returnValue(new OAuthToken("token_value", "scope", "login", 123, -1)));
    }});

    return new GitHubAuthenticator(myProperties, project, tokenStorage).getCredentials();
  }

}
