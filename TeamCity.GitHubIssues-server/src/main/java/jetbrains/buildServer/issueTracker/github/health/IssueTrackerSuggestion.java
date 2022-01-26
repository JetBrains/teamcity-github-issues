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

package jetbrains.buildServer.issueTracker.github.health;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jetbrains.buildServer.issueTracker.IssueProvidersManager;
import jetbrains.buildServer.issueTracker.github.GitHubIssueProviderType;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.healthStatus.ProjectSuggestedItem;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.suggestions.ProjectSuggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  private static final Pattern httpsPattern = Pattern.compile("http[s]?://github\\.com/(.*)/(.*)$");

  @NotNull
  private final String myViewUrl;

  @NotNull
  private final IssueProvidersManager myIssueProvidersManager;

  @NotNull
  private final HealthReportHelper myHelper;

  public IssueTrackerSuggestion(@NotNull final PluginDescriptor descriptor,
                                @NotNull PagePlaces pagePlaces,
                                @NotNull final IssueProvidersManager issueProvidersManager,
                                @NotNull final HealthReportHelper helper) {
    super("addGitHubIssueTracker", "Suggest to add a GitHub Issue Tracker", pagePlaces);
    myViewUrl = descriptor.getPluginResourcesPath("health/addGitHubIssueTracker.jsp");
    myIssueProvidersManager = issueProvidersManager;
    myHelper = helper;
  }

  @NotNull
  @Override
  public List<ProjectSuggestedItem> getSuggestions(@NotNull final SProject project) {
    boolean alreadyUsed = myIssueProvidersManager.getProviders(project).stream()
                                                 .anyMatch(it -> it.getType().equals(GitHubIssueProviderType.TYPE));
    final List<ProjectSuggestedItem> result = new ArrayList<>();
    if (!alreadyUsed) {
      final List<SBuildType> buildTypes = project.getOwnBuildTypes();
      Set<String> paths = getPathsFromVcsRoots(buildTypes);
      if (paths.stream().anyMatch(ReferencesResolverUtil::mayContainReference)) {
        paths = getPathsFromInstances(buildTypes);
      }
      if (!paths.isEmpty()) {
        final Map<String, Map<String, Object>> results = new HashMap<>();
        paths.stream()
             .map(this::toSuggestion)
             .filter(Objects::nonNull)
             .forEach(sMap -> results.put((String)sMap.get("repoUrl"), sMap));
        if (!results.isEmpty()) {
          result.add(new ProjectSuggestedItem(getType(), project, Collections.singletonMap("suggestedTrackers", results)));
        }
      }
    }
    return result;
  }

  private Set<String> getPathsFromVcsRoots(@NotNull final List<SBuildType> buildTypes) {
    return extractFetchUrls(buildTypes.stream().map(BuildTypeSettings::getVcsRoots));
  }

  private Set<String> getPathsFromInstances(@NotNull final List<SBuildType> buildTypes) {
    return extractFetchUrls(buildTypes.stream().map(SBuildType::getVcsRootInstances));
  }

  private Set<String> extractFetchUrls(@NotNull final Stream<List<? extends VcsRoot>> stream) {
    return stream.flatMap(List::stream)
                 .filter(it -> GIT_VCS_NAME.equals(it.getVcsName()))
                 .map(it -> it.getProperty(GIT_FETCH_URL_PROPERTY))
                 .filter(Objects::nonNull)
                 .collect(Collectors.toSet());
  }

  @Nullable
  private Map<String, Object> toSuggestion(@NotNull final String fetchUrl) {
    Matcher m;
    boolean matched = false;
    m = sshPattern.matcher(fetchUrl);
    String owner = null;
    String repo = null;
    if (m.matches()) {
      owner = m.group(1);
      repo = m.group(2);
      matched = true;
    }
    if (!matched) {
      m = httpsPattern.matcher(fetchUrl);
      if (m.matches()) {
        owner = m.group(1);
        repo = StringUtil.removeSuffix(m.group(2), ".git", true);
        matched = true;
      }
    }
    if (matched && myHelper.hasIssues(owner, repo)) {
      return getSuggestionMap(owner, repo);
    }
    return null;
  }

  @NotNull
  private Map<String, Object> getSuggestionMap(@NotNull final String owner,
                                               @NotNull final String repo) {
    final Map<String, Object> result = new HashMap<>();
    result.put("type", GitHubIssueProviderType.TYPE);
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
