package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.issueTracker.github.auth.GitHubAuthenticator;
import jetbrains.buildServer.issueTracker.github.auth.TokenCredentials;
import jetbrains.buildServer.serverSide.oauth.PersonalOAuthTokens;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Rybak <oleg.rybak@jetbrains.com>
 */
public class GitHubAuthenticatorTest extends BaseTestCase {

  private PersonalOAuthTokens myTokens;

  private Map<String, String> myProperties;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myTokens = new PersonalOAuthTokens();
    myProperties = new HashMap<String, String>();
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
  public void testToken_OAuth_WrongFormat() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:someinvalidstringwithoutuser");
    final Credentials result = getCredentials();
    assertNull(result);
  }

  @Test
  public void testToken_OAuth_NoTokensStored() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:user:provider1");
    final Credentials result = getCredentials();
    assertNull(result);
  }


  @Test
  public void testToken_OAuth_UserNotFound() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:user1:provider1");
    myTokens.rememberToken("provider1", "user2", "29505c65d0c8529c9e66c419f6480c4a", "everything");
    final Credentials result = getCredentials();
    assertNull(result);
  }

  @Test
  public void testToken_OAuth_MultipleTokens() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:user1:provider1");
    myTokens.rememberToken("provider1", "user1", "29505c65d0c8529c9e66c419f6480c4a", "everything");
    myTokens.rememberToken("provider1", "user2", "someothertoken", "everything");
    final Credentials result = getCredentials();
    assertNotNull(result);
    assertTrue(result instanceof TokenCredentials);
    assertEquals("29505c65d0c8529c9e66c419f6480c4a", ((TokenCredentials)result).getToken());
  }

  @Test
  public void testToken_OAuth_Valid() throws Exception {
    myProperties.put(GitHubConstants.PARAM_AUTH_TYPE, GitHubConstants.AUTH_ACCESSTOKEN);
    myProperties.put(GitHubConstants.PARAM_ACCESS_TOKEN, "oauth:user1:provider1");
    myTokens.rememberToken("provider1", "user1", "29505c65d0c8529c9e66c419f6480c4a", "everything");
    final Credentials result = getCredentials();
    assertNotNull(result);
    assertTrue(result instanceof TokenCredentials);
    assertEquals("29505c65d0c8529c9e66c419f6480c4a", ((TokenCredentials)result).getToken());
  }

  private Credentials getCredentials() {
    return new GitHubAuthenticator(myProperties, myTokens).getCredentials();
  }
}
