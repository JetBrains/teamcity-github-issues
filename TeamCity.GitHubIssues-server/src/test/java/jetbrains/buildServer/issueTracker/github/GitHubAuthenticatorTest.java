package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.issueTracker.github.auth.GitHubAuthenticator;
import jetbrains.buildServer.issueTracker.github.auth.TokenCredentials;
import jetbrains.buildServer.serverSide.oauth.OAuthTokensStorage;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Rybak <oleg.rybak@jetbrains.com>
 */
public class GitHubAuthenticatorTest extends BaseTestCase {

  private Map<String, String> myProperties;

  private OAuthTokensStorage myStorage;

  private UserModel myUserModel;

  private Mockery m;

  private SUser myUser;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myProperties = new HashMap<String, String>();
    myStorage = new OAuthTokensStorage();
    myUserModel = m.mock(UserModel.class);
    myUser = m.mock(SUser.class);
  }

  @Test
  public void testAnonymous() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ANONYMOUS);
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
  public void testToken_Personal() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "29505c65d0c8529c9e66c419f6480c4a");
    final Credentials result = getCredentials();
    assertNotNull(result);
    assertTrue(result instanceof TokenCredentials);
    assertEquals("29505c65d0c8529c9e66c419f6480c4a", ((TokenCredentials)result).getToken());
  }

  @Test
  public void testToken_OAuth_NotokenInIssueProvider() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    final Credentials result = getCredentials();
    assertNull(result);
  }

  @Test
  public void testToken_OAuth_WrongFormat() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:someinvalidstringwithoutuser");
    final Credentials result = getCredentials();
    assertNull(result);
  }

  @Test
  public void testToken_OAuth_UserNotFound() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:1:provider1:user1");
    m.checking(new Expectations() {{
      oneOf(myUserModel).findUserById(1L);
      will(returnValue(null));
    }});
    final Credentials result = getCredentials();
    assertNull(result);
  }

  @Test
  public void testToken_OAuth_MultipleTokens_OneUser() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:1:provider1:githubUserId1");
    addExpectationsForUser();
    myStorage.rememberToken("provider1", myUser, "githubUserId1", "29505c65d0c8529c9e66c419f6480c4a", "everything");
    myStorage.rememberToken("provider1", myUser, "githubUserId2", "someothertoken", "everything");
    final Credentials result = getCredentials();
    assertNotNull(result);
    assertTrue(result instanceof TokenCredentials);
    assertEquals("29505c65d0c8529c9e66c419f6480c4a", ((TokenCredentials)result).getToken());
  }

  @Test
  public void testToken_OAuth_MultipleTokens_MultipleUsers() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:1:provider1:githubUserId1");
    final SUser myUser2 = m.mock(SUser.class, "user 2");
    m.checking(new Expectations() {{
      allowing(myUser).getId();
      will(returnValue(1L));

      allowing(myUser2).getId();
      will(returnValue(2L));

      allowing(myUserModel).findUserById(1L);
      will(returnValue(myUser));
    }});
    myStorage.rememberToken("provider1", myUser, "githubUserId1", "29505c65d0c8529c9e66c419f6480c4a", "everything");
    myStorage.rememberToken("provider1", myUser2,"githubUserId1", "someothertoken", "everything");
    final Credentials result = getCredentials();
    assertNotNull(result);
    assertTrue(result instanceof TokenCredentials);
    assertEquals("29505c65d0c8529c9e66c419f6480c4a", ((TokenCredentials)result).getToken());
  }

  @Test
  public void testToken_OAuth_Valid() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:1:provider1:githubUserId1");
    addExpectationsForUser();
    myStorage.rememberToken("provider1", myUser, "githubUserId1", "29505c65d0c8529c9e66c419f6480c4a", "everything");
    final Credentials result = getCredentials();
    assertNotNull(result);
    assertTrue(result instanceof TokenCredentials);
    assertEquals("29505c65d0c8529c9e66c419f6480c4a", ((TokenCredentials)result).getToken());
  }

  private void addExpectationsForUser() {
    m.checking(new Expectations() {{
      allowing(myUser).getId();
      will(returnValue(1L));

      allowing(myUserModel).findUserById(1L);
      will(returnValue(myUser));
    }});
  }

  private Credentials getCredentials() {
    return new GitHubAuthenticator(myProperties, myStorage, myUserModel).getCredentials();
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    m.assertIsSatisfied();
  }
}
