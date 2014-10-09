/*

Actuate Client Example

This program demonstrates getting all iServer counters.

*/
package example;

import com.actuate.schemas.*;

public class Counter {

	public static ActuateControl actuateControl;

	public static String inputFileName;
	public static String outputFileName;


	public static void main(String[] args) {

		try {

			// get command line arguments
			Arguments arguments = new Arguments(args);

			actuateControl = new ActuateControl(arguments.getURL());

			// Login
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();
			actuateControl.setTargetServer(arguments.getTargetServer());
			
			// Login
			ActuateSoapPort soapPort = actuateControl.proxy;

			long [] counterIds;

			{
				GetAllCounterValuesResponse response = actuateControl.getAllCounterValues();

				ArrayOfCounterInfo infoList = response.getCounterInfoList();
				CounterInfo[] counterInfos = infoList.getCounterInfo();
				
				counterIds = new long[counterInfos.length];
				for (int i=0;i<counterInfos.length;i++) {
					CounterInfo counterInfo = counterInfos[i];
					System.out.println("CounterId="+counterInfo.getCounterId());
					System.out.println("CounterName="+counterInfo.getCounterName());
					System.out.println("CounterValue="+counterInfo.getCounterValue());
					counterIds[i] = counterInfo.getCounterId();
				}
			}

			{
				ArrayOfLong counterIDList = new ArrayOfLong();
				counterIDList.set_long(counterIds);

				GetCounterValues request = new GetCounterValues();
				request.setCounterIDList(counterIDList);
				
				GetCounterValuesResponse response = soapPort.getCounterValues(request);
				CounterInfo[] counterInfos = response.getCounterInfoList().getCounterInfo();
				for (int i=0;i<counterInfos.length;i++) {
					CounterInfo counterInfo = counterInfos[i];
					System.out.println("CounterId="+counterInfo.getCounterId());
					System.out.println("CounterName="+counterInfo.getCounterName());
					System.out.println("CounterValue="+counterInfo.getCounterValue());
					counterIds[i] = counterInfo.getCounterId();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
