

package jetbrains.buildServer.issueTracker.github;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public interface GitHubConstants {
  String PARAM_NAME = "name";
  String PARAM_REPOSITORY = "repository";
  String PARAM_AUTH_TYPE = "authType";
  String PARAM_USERNAME = "username";
  String PARAM_PASSWORD = "secure:password";
  String PARAM_ACCESS_TOKEN = "secure:accessToken";
  String PARAM_PATTERN = "pattern";


  String AUTH_ANONYMOUS = "anonymous";
  String AUTH_LOGINPASSWORD = "loginpassword";
  String AUTH_ACCESSTOKEN = "accesstoken";

  Pattern OWNER_AND_REPO_PATTERN = Pattern.compile("/?([^/]+)/([^/]+)/?$");
}