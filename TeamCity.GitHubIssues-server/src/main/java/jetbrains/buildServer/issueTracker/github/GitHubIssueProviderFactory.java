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

package jetbrains.buildServer.issueTracker.github;

import jetbrains.buildServer.issueTracker.AbstractIssueProviderFactory;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueProvider;
import jetbrains.buildServer.issueTracker.IssueProviderType;
import jetbrains.buildServer.serverSide.oauth.OAuthTokensStorage;
import jetbrains.buildServer.users.UserModel;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class GitHubIssueProviderFactory extends AbstractIssueProviderFactory {

  @NotNull
  private final OAuthTokensStorage myStorage;

  @NotNull
  private final UserModel myUserModel;

  public GitHubIssueProviderFactory(@NotNull final IssueProviderType type,
                                    @NotNull final IssueFetcher fetcher,
                                    @NotNull final OAuthTokensStorage storage,
                                    @NotNull final UserModel userModel) {
    super(type, fetcher);
    myStorage = storage;
    myUserModel = userModel;
  }

  @NotNull
  public IssueProvider createProvider() {
    return new GitHubIssueProvider(myType, myFetcher, myStorage, myUserModel);
  }
}
