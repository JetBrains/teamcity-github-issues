package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.issueTracker.IssueProviderType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.issueTracker.github.GitHubConstants.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueProviderType extends IssueProviderType {

  @NotNull
  private final String myConfigUrl;

  @NotNull
  private final String myPopupUrl;

  @NotNull
  private static final String DEFAULT_ISSUE_PATTERN = "#(\\d+)";

  public GitHubIssueProviderType(@NotNull final PluginDescriptor pluginDescriptor) {
    myConfigUrl = pluginDescriptor.getPluginResourcesPath("admin/editIssueProvider.jsp");
    myPopupUrl = pluginDescriptor.getPluginResourcesPath("popup.jsp");
  }

  @NotNull
  @Override
  public String getType() {
    return "GithubIssues"; // must be 'GithubIssues' to handle existing github integration
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "GitHub";
  }

  @NotNull
  @Override
  public String getEditParametersUrl() {
    return myConfigUrl;
  }

  @NotNull
  @Override
  public String getIssueDetailsUrl() {
    return myPopupUrl;
  }

  @NotNull
  @Override
  public Map<String, String> getDefaultProperties() {
    return new HashMap<String, String>() {{
      put(PARAM_AUTH_TYPE, AUTH_ANONYMOUS);
      put(PARAM_PATTERN, DEFAULT_ISSUE_PATTERN);
    }};
  }
}
