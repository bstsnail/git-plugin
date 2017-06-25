package hudson.plugins.git.notify;

public class Enumations {

	public enum EnventMonitorType{
		SVN("SVN"),BUILD("BUILD"),IOSSIGN("IOSSIGN"),ARCHIVE("ARCHIVE"),SLAVE("SLAVE");
		private String type;
		
		private EnventMonitorType(String enventType){
			this.type = enventType;
		}
		
		public String getType(){
			return this.type;
		}
	}
	
	public enum EnventStatus{
		SUCCESS("SUCCESS"),FAILED("FAILURE");
		private String status;
		
		private EnventStatus(String enventStatus){
			this.status = enventStatus;
		}
		
		public String getStatus(){
			return this.status;
		}
	}
}
