/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.issueTracker.IssueProviderEx;
import jetbrains.buildServer.issueTracker.IssueProvidersManager;
import jetbrains.buildServer.issueTracker.github.GitHubIssueProviderType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.healthStatus.*;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.issueTracker.github.GitHubConstants.AUTH_LOGINPASSWORD;
import static jetbrains.buildServer.issueTracker.github.GitHubConstants.PARAM_AUTH_TYPE;

public class PasswordAuthReport extends HealthStatusReport {

    private static final ItemCategory GITHUB_PASS_AUTH_CATEGORY =
            new ItemCategory("githubPassAuth",
                    "GitHub issue tracker connection uses login/password authentication",
                    ItemSeverity.WARN);

    public static final String REPORT_TYPE = "GitHubPasswordAuthentication";

    @NotNull
    private final IssueProvidersManager myIssueProvidersManager;

    public PasswordAuthReport(@NotNull final IssueProvidersManager issueProvidersManager,
                              @NotNull final PluginDescriptor pluginDescriptor,
                              @NotNull final PagePlaces pagePlaces) {
        myIssueProvidersManager = issueProvidersManager;
        final HealthStatusItemPageExtension myPEx = new HealthStatusItemPageExtension(REPORT_TYPE, pagePlaces);
        myPEx.setIncludeUrl(pluginDescriptor.getPluginResourcesPath("health/passwordAuthReport.jsp"));
        myPEx.setVisibleOutsideAdminArea(true);
        myPEx.register();
    }

    @NotNull
    @Override
    public String getType() {
        return REPORT_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "GitHub issue tracker connection uses login/password authentication";
    }

    @NotNull
    @Override
    public Collection<ItemCategory> getCategories() {
        return Collections.singletonList(GITHUB_PASS_AUTH_CATEGORY);
    }

    @Override
    public boolean canReportItemsFor(@NotNull HealthStatusScope healthStatusScope) {
        return healthStatusScope.isItemWithSeverityAccepted(ItemSeverity.WARN);
    }

    @Override
    public void report(@NotNull HealthStatusScope healthStatusScope,
                       @NotNull HealthStatusItemConsumer healthStatusItemConsumer) {
        healthStatusScope.getProjects().stream()
                .flatMap(p -> myIssueProvidersManager.getOwnProviders(p).values().stream())
                .filter(it -> GitHubIssueProviderType.TYPE.equals(it.getType()))
                .filter(it -> AUTH_LOGINPASSWORD.equals(it.getProperties().get(PARAM_AUTH_TYPE)))
                .map(this::toHealthStatusItem)
                .forEach(it -> healthStatusItemConsumer.consumeForProject(it.first, it.second));
    }

    @NotNull
    private Pair<SProject, HealthStatusItem> toHealthStatusItem(@NotNull final IssueProviderEx issueProvider) {
        final Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("name", issueProvider.getName());
        additionalData.put("id", issueProvider.getId());
        additionalData.put("project", issueProvider.getProject());
        additionalData.put("type", issueProvider.getType());
        final String identity = identity(issueProvider.getProject().getProjectId(), issueProvider.getId());
        return Pair.create(issueProvider.getProject(), new HealthStatusItem(identity, GITHUB_PASS_AUTH_CATEGORY, ItemSeverity.WARN, additionalData));
    }

    private static String identity(String... parts) {
        return PasswordAuthReport.GITHUB_PASS_AUTH_CATEGORY.getId() + "_" + StringUtil.join(parts, "").hashCode();
    }
}
