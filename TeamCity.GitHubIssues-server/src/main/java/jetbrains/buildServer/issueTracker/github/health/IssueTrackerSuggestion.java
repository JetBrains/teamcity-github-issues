package jetbrains.buildServer.issueTracker.github.health;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.issueTracker.IssueProviderEx;
import jetbrains.buildServer.issueTracker.IssueProvidersManager;
import jetbrains.buildServer.issueTracker.github.GitHubIssueProviderType;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.healthStatus.BuildTypeSuggestedItem;
import jetbrains.buildServer.vcs.VcsRootInstance;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.suggestions.BuildTypeSuggestion;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class IssueTrackerSuggestion extends BuildTypeSuggestion {

  private static final String GIT_VCS_NAME = "jetbrains.git";
  private static final String GIT_FETCH_URL_PROPERTY = "url";

  @NotNull
  private final String myViewUrl;

  @NotNull
  private final IssueProvidersManager myIssueProvidersManager;

  @NotNull
  private final GitHubIssueProviderType myType;

  public IssueTrackerSuggestion(@NotNull final PluginDescriptor descriptor,
                                @NotNull PagePlaces pagePlaces,
                                @NotNull final IssueProvidersManager issueProvidersManager,
                                @NotNull final GitHubIssueProviderType type) {
    super("addGitHubIssueTracker", "Suggest to add a GitHub Issue Tracker", pagePlaces);
    myType = type;
    myViewUrl = descriptor.getPluginResourcesPath("health/addGitHubIssueTracker.jsp");
    myIssueProvidersManager = issueProvidersManager;
  }

  @NotNull
  @Override
  public List<BuildTypeSuggestedItem> getSuggestions(@NotNull SBuildType buildType) {
    final SProject project = buildType.getProject();
    final String type = myType.getType();
    final Map<String, IssueProviderEx> providers = myIssueProvidersManager.getProviders(project);
    boolean alreadyUsed = false;
    for (IssueProviderEx issueProviderEx: providers.values()) {
      if (type.equals(issueProviderEx.getType())) {
        alreadyUsed = true;
        break;
      }
    }
    List<BuildTypeSuggestedItem> result = new ArrayList<BuildTypeSuggestedItem>();
    if (!alreadyUsed) {
      List<VcsRootInstance> vcsRoots = buildType.getVcsRootInstances();
      if (!vcsRoots.isEmpty()) {
        VcsRootInstance gitHubInstance = null;
        URL instanceUrl = null;
        for (VcsRootInstance instance: vcsRoots) {
          if (GIT_VCS_NAME.equals(instance.getVcsName())) {
            String fetchUrl = instance.getProperty(GIT_FETCH_URL_PROPERTY);
            if (!StringUtil.isEmptyOrSpaces(fetchUrl)) {
              try {
                URL url = new URL(fetchUrl);
                if ("github.com".equals(url.getHost())) {
                  gitHubInstance = instance;
                  instanceUrl = url;
                  break;
                }
              } catch (Exception ignored) {
              }
            }
          }
        }
        if (gitHubInstance != null) {
          try {
            final Map<String, Object> data = new HashMap<String, Object>();
            data.put("vcsRoot", gitHubInstance);
            data.put("type", myType.getType());
            data.put("repoUrl", new URL("https", instanceUrl.getHost(), instanceUrl.getPath()));
            data.put("suggestedName", instanceUrl.getPath().substring(1));
            result.add(new BuildTypeSuggestedItem(getType(), buildType, data));
          } catch (Exception ignored) {}
        }
      }
    }
    return result;
  }

  @NotNull
  @Override
  public String getViewUrl() {
    return myViewUrl;
  }


}
