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

import com.google.common.collect.Sets;
import java.util.*;
import java.util.function.Predicate;
import jetbrains.buildServer.issueTracker.IssueProviderType;
import jetbrains.buildServer.serverSide.ParametersDescriptor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.issueTracker.github.GitHubConstants.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueProviderType extends IssueProviderType {

  public static final String TYPE = "GithubIssues";


  public static final String CONNECTION_SUBTYPE = "connectionSubtype";

  public static final String ALL_IN_ONE_SUBTYPE = "gitHubApp";
  public static final String INSTALLATION_SUBTYPE = "gitHubAppInstallation";

  public static final Set<String> ALLOWED_SUBTYPES = Sets.newHashSet(ALL_IN_ONE_SUBTYPE, INSTALLATION_SUBTYPE);

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
    return TYPE; // must be 'GithubIssues' to handle existing github integration
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

  @NotNull
  @Override
  public Map<String, Predicate<ParametersDescriptor>> getConnectionTypes() {
    return new HashMap<String, Predicate<ParametersDescriptor>>(){{
      put("GitHubApp", connection -> ALLOWED_SUBTYPES.contains(connection.getParameters().get(CONNECTION_SUBTYPE)));
    }};
  }
}
