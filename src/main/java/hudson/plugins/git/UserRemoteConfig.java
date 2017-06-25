package hudson.plugins.git;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.GitURIRequirementsBuilder;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static hudson.Util.*;

@ExportedBean
public class UserRemoteConfig extends AbstractDescribableImpl<UserRemoteConfig> implements Serializable {

    private String name;
    private String refspec;
    private String url;
    private String credentialsId;

    @DataBoundConstructor
    public UserRemoteConfig(String url, String name, String refspec, String credentialsId) {
        this.url = fixEmptyAndTrim(url);
        this.name = fixEmpty(name);
        this.refspec = fixEmpty(refspec);
        this.credentialsId = fixEmpty(credentialsId);
    }

    @Exported
    public String getName() {
        return name;
    }

    @Exported
    public String getRefspec() {
        return refspec;
    }

    @Exported
    public String getUrl() {
        return url;
    }

    @Exported
    public String getCredentialsId() {
        return credentialsId;
    }

    public String toString() {
        return getRefspec() + " => " + getUrl() + " (" + getName() + ")";
    }

    private final static Pattern SCP_LIKE = Pattern.compile("(.*):(.*)");

    @Extension
    public static class DescriptorImpl extends Descriptor<UserRemoteConfig> {

        private final static Logger logger = Logger.getLogger(DescriptorImpl.class.getName());

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item project,
                                                     @QueryParameter String url) {
            if (project == null || !project.hasPermission(Item.CONFIGURE)) {
                return new StandardListBoxModel();
            }
            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withMatching(
                            GitClient.CREDENTIALS_MATCHER,
                            CredentialsProvider.lookupCredentials(StandardCredentials.class,
                                    project,
                                    ACL.SYSTEM,
                                    GitURIRequirementsBuilder.fromUri(url).build())
                    );
        }

        public FormValidation doCheckCredentialsId(@AncestorInPath Item project,
                                                   @QueryParameter String url,
                                                   @QueryParameter String value) {
            if (project == null || !project.hasPermission(Item.CONFIGURE)) {
                return FormValidation.ok();
            }

            value = Util.fixEmptyAndTrim(value);
            if (value == null) {
                return FormValidation.ok();
            }

            url = Util.fixEmptyAndTrim(url);
            if (url == null)
            // not set, can't check
            {
                return FormValidation.ok();
            }

            if (url.indexOf('$') >= 0)
            // set by variable, can't check
            {
                return FormValidation.ok();
            }

            StandardCredentials credentials = lookupCredentials(project, value, url);

            if (credentials == null) {
                // no credentials available, can't check
                return FormValidation.warning("Cannot find any credentials with id " + value);
            }

            // TODO check if this type of credential is acceptible to the Git client or does it merit warning the user

            return FormValidation.ok();
        }

        public FormValidation doCheckUrlWithPassWord(
                @AncestorInPath Item project, @QueryParameter String userName,
                @QueryParameter String password, @QueryParameter String value)
                throws IOException, InterruptedException {

            // if (project == null || !project.hasPermission(Item.CONFIGURE)) {
            // return FormValidation.ok();
            // }

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter Git repository.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.ok();

            // get git executable on master
            final EnvVars environment = new EnvVars(System.getenv()); // GitUtils.getPollEnvironment(project,
            // null,
            // launcher,
            // TaskListener.NULL,
            // false);

            GitClient git = Git.with(TaskListener.NULL, environment)
                    .using(GitTool.getDefaultInstallation().getGitExe())
                    .getClient();

			 /*String temp;
			try {
				temp = URLDecoder.decode(password, "utf-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				temp = password;
			}
			password = temp;*/
            git.addDefaultCredentials(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, null, "ieg-git-check-remote", userName, password));

            // attempt to connect the provided URL
            try {
                git.getHeadRev(url, "HEAD");
            } catch (GitException e) {
                return FormValidation.error(Messages
                        .UserRemoteConfig_FailedToConnect(e.getMessage()));
            }

            return FormValidation.ok();
        }

        private final static String USER_NAME = "userName";
        private final static String PRIVATE_KEY = "privateKey";
        private final static String PASS_PHASE = "passPhase";
        private final static String URL = "url";
        /**
         * Do check the ssh credential
         * @return
         * @throws IOException
         * @throws InterruptedException
         */
        @RequirePOST
        public void doCheckUrlWithSshKey(StaplerRequest request, StaplerResponse response)
                throws Exception {
            JSONObject object = request.getSubmittedForm();
            logger.info("Trying to check if the ssh key is right - " + object.toString(2));

            /**
             * The object format
             * {
             *      "userName": "xxx",
             *      "privateKey": "xxx",
             *      "passPhase": "xxx",
             *      "url": "xxx"
             * }
             */
            if (!object.has(USER_NAME)) {
                throw new IllegalArgumentException("Miss userName");
            }
            String userName = object.getString(USER_NAME);

            if (!object.has(PRIVATE_KEY)){
                throw new IllegalArgumentException("Miss private key");
            }
            String privateKey = object.getString(PRIVATE_KEY);

            if (!object.has(URL)){
                throw new IllegalArgumentException("Miss url");
            }
            String url = object.getString(URL);

            String passPhase = "";
            if (object.has(PASS_PHASE)){
                passPhase = object.getString(PASS_PHASE);
            }

            // get git executable on master
            final EnvVars environment = new EnvVars(System.getenv());
            GitClient git = Git.with(TaskListener.NULL, environment)
                    .using(GitTool.getDefaultInstallation().getGitExe())
                    .getClient();

            git.addDefaultCredentials(new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, null, userName,
                    new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKey), passPhase, "ieg-git-check-remote"));

            // attempt to connect the provided URL
            int code;
            String message;
            try {
                git.getHeadRev(url, "HEAD");
                code = StaplerResponse.SC_OK;
                message = "OK";
            } catch (GitException e) {
                logger.log(Level.WARNING, "Fail to check the ssh private key credential", e);
                code = StaplerResponse.SC_FORBIDDEN;
                message = "Fail to check the ssh private key credential - " + e.getMessage();
            }

            response.setStatus(code);
            response.setContentType("text/plain");
            PrintWriter w = response.getWriter();
            w.write(message);
            w.close();
        }

        public FormValidation doCheckUrl(@AncestorInPath Item project,
                                         @QueryParameter String credentialsId,
                                         @QueryParameter String value) throws IOException, InterruptedException {

            if (project == null || !project.hasPermission(Item.CONFIGURE)) {
                return FormValidation.ok();
            }

            String url = Util.fixEmptyAndTrim(value);
            if (url == null)
                return FormValidation.error("Please enter Git repository.");

            if (url.indexOf('$') >= 0)
                // set by variable, can't validate
                return FormValidation.ok();

            // get git executable on master
            final EnvVars environment = new EnvVars(System.getenv()); // GitUtils.getPollEnvironment(project, null, launcher, TaskListener.NULL, false);

            GitClient git = Git.with(TaskListener.NULL, environment)
                    .using(GitTool.getDefaultInstallation().getGitExe())
                    .getClient();
            git.addDefaultCredentials(lookupCredentials(project, credentialsId, url));

            // attempt to connect the provided URL
            try {
                git.getHeadRev(url, "HEAD");
            } catch (GitException e) {
                return FormValidation.error(Messages.UserRemoteConfig_FailedToConnect(e.getMessage()));
            }

            return FormValidation.ok();
        }

        private static StandardCredentials lookupCredentials(Item project, String credentialId, String uri) {
            return (credentialId == null) ? null : CredentialsMatchers.firstOrNull(
                        CredentialsProvider.lookupCredentials(StandardCredentials.class, project, ACL.SYSTEM,
                                GitURIRequirementsBuilder.fromUri(uri).build()),
                        CredentialsMatchers.withId(credentialId));
        }

        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
