package com.gfs.ihub;

import com.innoventsolutions.idapihelper.IdapiHelper;

public abstract class ActuateWorker {
	protected final IdapiHelper helper;
	
	public ActuateWorker(IdapiHelper helper){
		this.helper = helper;
	}

}
