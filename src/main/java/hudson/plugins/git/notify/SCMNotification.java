package hudson.plugins.git.notify;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.notify.Enumations.EnventMonitorType;
import hudson.plugins.git.notify.Enumations.EnventStatus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class SCMNotification{

	private  static HttpURLConnection connection(URL url) throws IOException {
		HttpURLConnection con = null;
		con = (HttpURLConnection) url.openConnection();

		con.setConnectTimeout(20000);
		con.setReadTimeout(10000);
		return con;
	}
	
	@SuppressWarnings("rawtypes")
	public static void sendDataToCIPlatForm(Run build, TaskListener listener,
			String sufix) {
		try {
			EnvVars var = build.getEnvironment(listener);
			
			String monitorUrl = (String) var.get("IEG_CI_BUILD_STEP_URL");
			String jobName = (String) var.get("JOB_NAME");
			String buildNumber = (String) var.get("BUILD_NUMBER");
			String type = EnventMonitorType.SVN.getType();
			String status = EnventStatus.FAILED.getStatus();
			listener.getLogger().println("git checkout exception happended,try to notify rd platform,type is GIT ,status is "+status+".");
			long duration = (new Date()).getTime() - build.getStartTimeInMillis();
			duration = duration < 0?0:duration;
			float timeInsenconds = (float) (duration/1000.0);
			/*listener.getLogger().println(sufix+": duration time millseconds:" + duration);*/
			if (monitorUrl != null) {
				
				/*listener.getLogger().println(
						sufix + ": start notify ieg ci platform scm monitor result!");*/
				URL url = new URL(String.format(monitorUrl,
						new Object[] {jobName , buildNumber , type , URLEncoder.encode(String.valueOf(timeInsenconds), "utf-8"),status}));
				/*listener.getLogger().println(sufix+": "+url);*/
				HttpURLConnection con = null;
				int statusCode = 0;
				try {
					con = connection(url);
					statusCode = con.getResponseCode();
					/*listener.getLogger().println(
							sufix + ": SCM checkout monitor return code:" + statusCode );*/
				} catch (Exception e) {
					e.printStackTrace();

					listener.getLogger().print(
							sufix + ": SCM checkout monitor error infomation:" + e);
				}

				int count = 4;
				
				while (count-- > 0) {
					if (statusCode != 200) {
						try {
							listener.getLogger().println(
									sufix + ": SCM checkout monitor return code:" + statusCode
											+ "\n");

							listener.getLogger()
									.println(sufix + ": SCM checkout monitor error response,wait 5s to retry");

							Thread.sleep(10000L);
							con = connection(url);
							statusCode = con.getResponseCode();
						} catch (Exception e) {
							e.printStackTrace();

							listener.getLogger().println(
									sufix + ": SCM checkout monitor error infomation:" + e);
						}
					}

				}

				/*if (statusCode == 200) {
					listener.getLogger().println(
							sufix + ": SCM checkout monitor success!");
				}*/
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}	
}
