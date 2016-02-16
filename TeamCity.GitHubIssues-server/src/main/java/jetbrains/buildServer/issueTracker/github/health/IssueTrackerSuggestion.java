package jetbrains.buildServer.issueTracker.github.health;

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.issueTracker.IssueProvidersManager;
import jetbrains.buildServer.issueTracker.github.GitHubIssueProviderType;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.healthStatus.ProjectSuggestedItem;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.suggestions.ProjectSuggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class IssueTrackerSuggestion extends ProjectSuggestion {

  private static final String GIT_VCS_NAME = "jetbrains.git";
  private static final String GIT_FETCH_URL_PROPERTY = "url";

  /* Matches github ssh urls of format git@github.com:owner/repo.git */
  private static final Pattern sshPattern = Pattern.compile("git@github\\.com:(.+)/(.+)(?:\\.git)");

  /* Matches github http and https urls of format https://github.com/owner/repo.git */
  private static final Pattern httpsPattern = Pattern.compile("http[s]?://github\\.com/(.*)/(.*)(?:\\.git)$");

  /* Matches github http and https urls of format https://github.com/owner/repo.git */
  private static final Pattern httpsPatternNoGit = Pattern.compile("http[s]?://github\\.com/(.*)/(.*)(?:\\.git)?$");

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
  public List<ProjectSuggestedItem> getSuggestions(@NotNull final SProject project) {
    final String type = myType.getType();
    boolean alreadyUsed = myIssueProvidersManager.getProviders(project).values().stream().anyMatch(it -> it.getType().equals(type));
    final List<ProjectSuggestedItem> result = new ArrayList<>();
    if (!alreadyUsed) {
      final List<SBuildType> buildTypes = project.getOwnBuildTypes();
      List<String> paths = getPathsFromVcsRoots(buildTypes);
      if (paths.stream().anyMatch(ReferencesResolverUtil::mayContainReference)) {
        paths = getPathsFromInstances(buildTypes);
      }
      final Map<String, Map<String, Object>> results = new HashMap<>();
      paths.stream()
              .map(this::toSuggestion)
              .filter(Objects::nonNull)
              .forEach(p -> results.put(p.first, p.second));
      if (!results.isEmpty()) {
        result.add(new ProjectSuggestedItem(getType(), project, Collections.singletonMap("suggestedTrackers", results)));
      }
    }
    return result;
  }

  private List<String> getPathsFromVcsRoots(@NotNull final List<SBuildType> buildTypes) {
    return extractFetchUrls(buildTypes.stream().map(BuildTypeSettings::getVcsRoots));
  }

  private List<String> getPathsFromInstances(@NotNull final List<SBuildType> buildTypes) {
    return extractFetchUrls(buildTypes.stream().map(SBuildType::getVcsRootInstances));
  }

  private List<String> extractFetchUrls(@NotNull final Stream<List<? extends VcsRoot>> stream) {
    return stream.flatMap(it -> StreamSupport.stream(it.spliterator(), false))
            .filter(it -> GIT_VCS_NAME.equals(it.getVcsName()))
            .map(it -> it.getProperty(GIT_FETCH_URL_PROPERTY))
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
  }

  @Nullable
  private Pair<String, Map<String, Object>> toSuggestion(@NotNull final String fetchUrl) {
    Map<String, Object> result = null;
    Matcher m;
    boolean matched = false;
    m = sshPattern.matcher(fetchUrl);
    if (m.matches()) {
      result = getSuggestionMap(m.group(1), m.group(2));
      matched = true;
    }
    if (!matched) {
      m = httpsPattern.matcher(fetchUrl);
      if (m.matches()) {
        result = getSuggestionMap(m.group(1), m.group(2));
        matched = true;
      }
    }
    if (!matched) {
      m = httpsPatternNoGit.matcher(fetchUrl);
      if (m.matches()) {
        result = getSuggestionMap(m.group(1), m.group(2));
      }
    }
    if (result != null) {
      return new Pair<>((String) result.get("repoUrl"), result);
    }
    return null;
  }

  @NotNull
  private Map<String, Object> getSuggestionMap(@NotNull final String owner,
                                               @NotNull final String repo) {
    final Map<String, Object> result = new HashMap<>();
    result.put("type", myType.getType());
    result.put("suggestedName", owner + "/" + repo);
    result.put("repoUrl", getRepoUrl(owner, repo));
    return result;
  }

  @NotNull
  private String getRepoUrl(@NotNull final String owner, @NotNull final String repo) {
    return "https://github.com/" + owner + "/" + repo;
  }

  @NotNull
  @Override
  public String getViewUrl() {
    return myViewUrl;
  }
}
