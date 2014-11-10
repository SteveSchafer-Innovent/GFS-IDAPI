package com.gfs.ihub.idapi_data;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.actuate.schemas.ArrayOfJobProperties;
import com.actuate.schemas.ArrayOfJobScheduleCondition;
import com.actuate.schemas.GetJobDetails;
import com.actuate.schemas.GetJobDetailsResponse;
import com.actuate.schemas.JobProperties;
import com.actuate.schemas.JobScheduleCondition;
import com.actuate.schemas.JobScheduleField;
import com.actuate.schemas.JobScheduleSearch;
import com.actuate.schemas.SelectJobSchedules;
import com.actuate.schemas.SelectJobSchedulesResponse;
import com.gfs.ihub.options.ActuateOptions;
import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperImpl;
import com.innoventsolutions.idapihelper.ResultDefConsts;

public class GfsScheduledJobs {
	private Iterator<GfsJob> iterator;
	private IdapiHelper helper;
	private String entryLog;
	private Boolean srvrReady = false;

	// iterate over the rows in the DataSet
	public Object next() throws Exception {
		if (srvrReady) {
			if (iterator.hasNext())
				return iterator.next();
			return null;
		}
		throw new Exception("Server is not ready, probably a login failure");
	}

	// clean up
	public void close() {
		helper = null;
	}

	// do query, get jobs
	public void open(Object obj, Map<String, Object> map) throws Exception {

		DebugInputParams(obj, map);

		String user = getInputParameter(obj, "ServerUserName").toString();
		String authId = getInputParameter(obj, "AuthID").toString();
		String serverUrl = getInputParameter(obj, "ServerURL").toString();
		System.out.println("User:" + user + " AuthId:" + authId + " URL:" + serverUrl);

		if ("".equals(authId) || "".equals(serverUrl)) {
			// not running on server try to find a config directory 
			// TODO parameterize config dir
			System.out.println("Logging in using properties file");
			final String configDir = "D:\\Actuate IDAPI\\config";
			final ActuateOptions options = new ActuateOptions(configDir, "actuate.properties");
			final URL svrURL = new URL(options.getUrlString());
			helper = IdapiHelperImpl.getInstance(new URL[] { svrURL });
			helper.login(options.getVolume(), options.getUsername(), options.getPassword(), new byte[0], false);
		} else {
			// running on the server with a valid authID
			// use that authID
			System.out.println("Logging in using existing URL and authId");
			final URL svrURL = new URL(serverUrl);
			helper = IdapiHelperImpl.getInstance(new URL[] { svrURL });
			helper.setAuthId(authId);
		}

		if (helper == null || !helper.isReady()) {
			throw new Exception("Failure to connect to server");
		}

		srvrReady = true;

		iterator = getScheduledJobs().iterator();

	}

	private List<GfsJob> getScheduledJobs() throws RemoteException {

		List<GfsJob> jobs = new ArrayList<GfsJob>();

		// Getting jobs requires two IDAPI calls, first get top level jobs
		List<JobProperties> jobProps = getJobProperties();
		for (JobProperties jobProp : jobProps) {

			if ("cancelled".equalsIgnoreCase(jobProp.getState().toString())) {
				// this filter could be done in the query
				// but had difficulty getting it to accept to query conditions
				continue;
			}

			// Then for each job get the JobDetails to pull out the parameter values
			GetJobDetailsResponse jobDetailsResponse = getJobDetails(jobProp.getJobId());
			GfsJob newJob = new GfsJob(jobDetailsResponse, entryLog);

			jobs.add(newJob);
		}

		return jobs;
	}

	// Top level IDAPI call to get all Jobs 
	private List<JobProperties> getJobProperties() throws RemoteException {

		/*
		final JobScheduleCondition jc1 = new JobScheduleCondition();
		jc1.setField(JobScheduleField.State);
		jc1.setMatch("Scheduled");

		final JobScheduleCondition[] jcArray = { jc1 };

		final ArrayOfJobScheduleCondition aoJC = new ArrayOfJobScheduleCondition();
		aoJC.setJobScheduleCondition(jcArray);
		*/

		List<JobProperties> jobProps = new ArrayList<JobProperties>();
		final JobScheduleSearch jobSearch = new JobScheduleSearch();
		jobSearch.setFetchDirection(Boolean.FALSE);
		jobSearch.setFetchSize(Integer.valueOf(2500));
		//jobSearch.setConditionArray(aoJC);

		final SelectJobSchedules selectJobs = new SelectJobSchedules();
		selectJobs.setResultDef(ResultDefConsts.getJobResultDef());
		selectJobs.setSearch(jobSearch);
		final SelectJobSchedulesResponse selectJobsResponse = helper.selectJobSchedules(selectJobs);

		final ArrayOfJobProperties aoJobProps = selectJobsResponse.getJobs();
		final JobProperties[] jobPropsArray = aoJobProps.getJobProperties();
		if (jobPropsArray != null && jobPropsArray.length > 0) {
			jobProps = new ArrayList<JobProperties>(Arrays.asList(jobPropsArray));
		}

		return jobProps;
	}

	// Second level IDAPI call to get JobDetails, 
	// this contains both the schedule and the parameter information
	public GetJobDetailsResponse getJobDetails(String jobId) throws RemoteException {

		final GetJobDetails getJobDetails = new GetJobDetails();
		getJobDetails.setResultDef(ResultDefConsts.getJobDetailsResultDef());
		getJobDetails.setJobId(jobId);
		GetJobDetailsResponse jobDetailsResponse = new GetJobDetailsResponse();
		jobDetailsResponse = helper.getJobDetails(getJobDetails);

		return jobDetailsResponse;
	}

	private void displayMap(Map<?, ?> map, StringBuffer sb) {
		if (map == null) {
			sb.append("Map is empty\n");
			return;
		}

		for (Entry<?, ?> ent : map.entrySet()) {
			sb.append(ent.getKey()).append(":").append(ent.getValue().toString()).append("\n");
		}
	}

	@SuppressWarnings("unchecked")
	private Object getInputParameter(Object inputObject, String paramName) {
		if (inputObject != null && inputObject instanceof Map<?, ?>) {
			Map<String, Object> mur = (Map<String, Object>) inputObject;
			Object obj = mur.get(paramName);
			return obj == null ? "" : obj;
		}

		return "";

	}

	/**
	 * This code was written to debug the input parameters.
	 * The thought was to parameterize the input path etc, but that is fairly tricky
	 * Going to just use hard-coded path to the jar file and hard code to the config dir.
	 * 
	 * @param obj
	 * @param map
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private void DebugInputParams(Object obj, Map<String, Object> map) {
		StringBuffer sb = new StringBuffer();
		String user = null;

		sb.append("\nFirst Object\n");
		if (obj != null && obj instanceof Map<?, ?>) {
			Map<String, Object> mur = (Map<String, Object>) obj;
			displayMap(mur, sb);

		} else {
			if (obj == null) {
				sb.append("\nFirst Object is null\n");
			} else {
				sb.append("\nFirst Object is: ").append(obj.getClass().toString()).append(" and value is: ").append(obj.toString())
						.append("\n");
			}
		}

		sb.append("Second Map\n");
		displayMap(map, sb);
		this.entryLog = sb.toString();
	}

}
