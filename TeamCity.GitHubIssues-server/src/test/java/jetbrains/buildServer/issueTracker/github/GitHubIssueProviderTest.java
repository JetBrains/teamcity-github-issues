

package jetbrains.buildServer.issueTracker.github;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.oauth.OAuthTokensStorage;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
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

  private final Synchroniser synchroniser = new Synchroniser();

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery() {{
      setImposteriser(ClassImposteriser.INSTANCE);
      setThreadingPolicy(synchroniser);
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
    final GitHubIssueProviderFactory factory = new GitHubIssueProviderFactory(myType, fetcher, storage, model);
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

  @Test(timeOut = 10000)
  public void test_ReDoS_parsing_number() throws Exception {
    ExecutorService regexExecutor = Executors.newSingleThreadExecutor();
    Future<String> issueId = regexExecutor.submit(new Callable<String>() {
      @Override
      public String call() throws Exception {
        Map<String, String> properties = getProperties("https://github.com/repo/owner");
        properties.put("pattern", "(?<![a-zA-Z-])(((((a+)+)+)+)-\\d+)");
        myProvider.setProperties(properties);
        return myProvider.extractId("issue #aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab");
      }
    });

    try {
      issueId.get(3, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      //calculating was iterrupted by MatcherUtil due to ReDos vulnerability
      assertTrue(e.getMessage().contains("regular expression is considered too time-consuming"));
    } catch (TimeoutException te) {
      //calculating should be interrupted by extractId method
      fail("issue id calculating was not interrupted by extractId method");
    }
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