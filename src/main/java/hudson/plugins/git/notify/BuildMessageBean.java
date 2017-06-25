package hudson.plugins.git.notify;

import hudson.model.Result;
import hudson.model.Run;

import java.util.Map;

public class BuildMessageBean {
	private String jobName;
	private String fullName;
	private Long buildNumber;
	private Long duration;
	private String buildId;
	// CHANGE TO MAP SVN 版本号
	private Map<String, String> causeMap;
	private long startTime;// 构建开始时间，;
	private String buildResult;//
	private EventType eventType;

	private Integer healthScore;//

	private Long lastSuccessBuildTime;
	private Integer lastSuccessBuildNumber;

	private Long lastFailureBuildTime;
	private Integer lastFailureBuildNumber;

	private Integer lastBuildNumber;
	private String lastBuildResult;
	private Long lastBuildDuration;
	
	private String nodeName; 

	private Map<String, String> params;

	public enum EventType {
		Start("START"), Delete("DELETE"), Completed("COMPLETED"), Finalized(
				"FINALIZED");

		String type;

		EventType(String type) {
			this.type = type;
		}

		public String toString() {
			return this.type;
		}
	}

	@SuppressWarnings("rawtypes")
	public void setLastSuccInfo(Run r) {
		if (r != null) {
			this.lastSuccessBuildNumber = r.number;
			this.lastSuccessBuildTime = r.getTimeInMillis();
		}
	}

	@SuppressWarnings("rawtypes")
	public void setLastFailureInfo(Run r) {
		if (r != null) {
			this.lastFailureBuildNumber = r.number;
			this.lastFailureBuildTime = r.getTimeInMillis();
		}
	}

	/**
	 * public static final Result SUCCESS = new
	 * Result("SUCCESS",BallColor.BLUE,0,true);
	 * 
	 * public static final Result UNSTABLE = new
	 * Result("UNSTABLE",BallColor.YELLOW,1,true);
	 * 
	 * public static final Result FAILURE = new
	 * Result("FAILURE",BallColor.RED,2,true);
	 * 
	 * public static final Result NOT_BUILT = new
	 * Result("NOT_BUILT",BallColor.NOTBUILT,3,false);
	 * 
	 * public static final Result ABORTED = new
	 * Result("ABORTED",BallColor.ABORTED,4,false);
	 */
	@SuppressWarnings("rawtypes")
	private String getStatus(Run r) {
		Result result = r.getResult();
		String status = null;
		if (result != null) {
			status = result.toString();
		}
		return status;
	}

	@SuppressWarnings("rawtypes")
	public void setBuildResult(Run r) {
		setBuildResult(getStatus(r));
	}

	@SuppressWarnings("rawtypes")
	public void setLastBuildInfo(Run r, Run lastRun) {
		if (lastRun != null && lastRun.isBuilding()) {
			lastRun = lastRun.getPreviousBuild();
		}
		if (lastRun != null) {
			if (lastRun.number == r.number) {
				lastRun = r.getPreviousBuild();
			}
		}
		if (lastRun != null) {
			this.lastBuildDuration = lastRun.getDuration();
			this.lastBuildNumber = lastRun.number;
			this.lastBuildResult = getStatus(lastRun);
		}
	}

	public enum CauseType {
		UserCause("userCause"), UserIdCause("userIdCause"), RemoteCause(
				"remoteCause"), UpstreamCause("upstreamCause"), LegacyCodeCause(
				"legacyCodeCause"), SCMTriggerCause("scmTriggerCause"), TimerTriggerCause(
				"timerTriggerCause"), NULL("NULL");

		String type;

		CauseType(String type) {
			this.type = type;
		}

		public String toString() {
			return this.type;
		}
	}

	public BuildMessageBean() {
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Long getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(Long buildNumber) {
		this.buildNumber = buildNumber;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getBuildResult() {
		return buildResult;
	}

	public void setBuildResult(String buildResult) {
		this.buildResult = buildResult;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public Integer getHealthScore() {
		return healthScore;
	}

	public void setHealthScore(Integer healthScore) {
		this.healthScore = healthScore;
	}

	public Long getLastSuccessBuildTime() {
		return lastSuccessBuildTime;
	}

	public void setLastSuccessBuildTime(Long lastSuccessBuildTime) {
		this.lastSuccessBuildTime = lastSuccessBuildTime;
	}

	public Integer getLastSuccessBuildNumber() {
		return lastSuccessBuildNumber;
	}

	public void setLastSuccessBuildNumber(Integer lastSuccessBuildNumber) {
		this.lastSuccessBuildNumber = lastSuccessBuildNumber;
	}

	public Long getLastFailureBuildTime() {
		return lastFailureBuildTime;
	}

	public void setLastFailureBuildTime(Long lastFailureBuildTime) {
		this.lastFailureBuildTime = lastFailureBuildTime;
	}

	public Integer getLastFailureBuildNumber() {
		return lastFailureBuildNumber;
	}

	public void setLastFailureBuildNumber(Integer lastFailureBuildNumber) {
		this.lastFailureBuildNumber = lastFailureBuildNumber;
	}

	public Long getLastBuildDuration() {
		return lastBuildDuration;
	}

	public void setLastBuildDuration(Long lastBuildDuration) {
		this.lastBuildDuration = lastBuildDuration;
	}

	public Integer getLastBuildNumber() {
		return lastBuildNumber;
	}

	public void setLastBuildNumber(Integer lastBuildNumber) {
		this.lastBuildNumber = lastBuildNumber;
	}

	public String getLastBuildResult() {
		return lastBuildResult;
	}

	public void setLastBuildResult(String lastBuildResult) {
		this.lastBuildResult = lastBuildResult;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public Map<String, String> getCauseMap() {
		return causeMap;
	}

	public void setCauseMap(Map<String, String> causeMap) {
		this.causeMap = causeMap;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
}
