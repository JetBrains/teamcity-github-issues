package jetbrains.buildServer.issueTracker.github;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.issueTracker.IssueProviderType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

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
       put("pattern", "\\(d+)");
    }};
  }
}
