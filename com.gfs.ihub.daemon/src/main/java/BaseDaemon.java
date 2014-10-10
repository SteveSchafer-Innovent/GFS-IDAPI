public class BaseDaemon {

	protected static String CONFIG_DIR;

	public BaseDaemon() {
	}
	
	public static void init(String[] args){
		if (args[0] != null && args[0].length() > 1) {
			CONFIG_DIR = args[0];
		} else {
			CONFIG_DIR = "D:/Actuate3/BIRTiHubVisualization/modules/BIRTiHub/iHub/data/server/log/mailer";
		}
		
	}

}