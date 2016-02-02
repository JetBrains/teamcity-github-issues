package jetbrains.buildServer.issueTracker.github.health;

import com.intellij.openapi.util.text.StringUtil;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class IssueTrackerSuggestion extends BuildTypeSuggestion {

  private static final String GIT_VCS_NAME = "jetbrains.git";
  private static final String GIT_FETCH_URL_PROPERTY = "url";

  private static final Pattern sshPattern = Pattern.compile("git@github\\.com:(.+)/(.+)(?:\\.git)");
  private static final Pattern httpsPattern = Pattern.compile("http[s]?://github\\.com/(.*)/(.*)(?:\\.git)");

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
    boolean alreadyUsed = myIssueProvidersManager.getProviders(project).values().stream()
            .filter(it -> it.getType().equals(type))
            .findFirst().isPresent();
    final List<BuildTypeSuggestedItem> result = new ArrayList<>();
    final List<Map<String, Object>> results = new ArrayList<>();
    if (!alreadyUsed) {
      List<VcsRootInstance> vcsRoots = buildType.getVcsRootInstances();
      if (!vcsRoots.isEmpty()) {
        for (VcsRootInstance instance: vcsRoots) {
          if (GIT_VCS_NAME.equals(instance.getVcsName())) {
            String fetchUrl = instance.getProperty(GIT_FETCH_URL_PROPERTY);
            if (!StringUtil.isEmptyOrSpaces(fetchUrl)) {
              Matcher m;
              boolean matched = false;
              m = sshPattern.matcher(fetchUrl);
              if (m.matches()) {
                // we have github ssh url
                final String owner = m.group(1);
                final String repo = m.group(2);
                results.add(getSuggestionMap(instance, owner, repo));
                matched = true;
              }
              if (!matched) {
                m = httpsPattern.matcher(fetchUrl);
                if (m.matches()) {
                  final String owner = m.group(1);
                  final String repo = m.group(2);
                  results.add(getSuggestionMap(instance, owner, repo));
                }
              }
            }
          }
        }
        if (!results.isEmpty()) {
          result.add(new BuildTypeSuggestedItem(getType(), buildType, Collections.singletonMap("suggestedTrackers", results)));
        }
      }
    }
    return result;
  }

  @NotNull
  private Map<String, Object> getSuggestionMap(@NotNull final VcsRootInstance instance,
                                               @NotNull final String owner,
                                               @NotNull final String repo) {
    final Map<String, Object> result = new HashMap<>();
    result.put("vcsRoot", instance);
    result.put("type", myType.getType());
    result.put("suggestedName", owner + "/" + repo);
    result.put("repoUrl", getIssueUrl(owner, repo));
    return result;
  }

  private String getIssueUrl(String owner, String repo) {
    try {
      return new URL("https", "github.com", "/" + owner + "/" + repo).toString();
    } catch (MalformedURLException e) {
      return "";
    }
  }

  @NotNull
  @Override
  public String getViewUrl() {
    return myViewUrl;
  }
}
