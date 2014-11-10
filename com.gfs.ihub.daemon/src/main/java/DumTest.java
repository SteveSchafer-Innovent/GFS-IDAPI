import java.util.HashMap;
import java.util.Map;

import com.gfs.ihub.idapi_data.GfsJob;
import com.gfs.ihub.idapi_data.GfsScheduledJobs;


public class DumTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		GfsScheduledJobs gsj = new GfsScheduledJobs();
		Map<String, Object> fud = new HashMap<String, Object>();
		gsj.open(fud, null);

		boolean doMore = true;
		do {
			Object obj = gsj.next();
			if(obj != null && obj instanceof GfsJob){
				GfsJob j = (GfsJob)obj;
				
				System.out.println(j.getOwner() + ": " + j.getJobType() + " " +  j.getState() + " " + j.getEmailFrom() + " " + j.getScheduleInfo() + " " + j.getNextStartDate() );
				
				System.out.println(j.getActualOutputFileName());
				System.out.println(j.getInputFileName());
				System.out.println("=================\n");
				doMore = true;
			} else {
				doMore = false;
			}
			
		} while(doMore);

	}

}
