package com.innoventsolutions.idapihelper;

/**
 * Thrown when the IdapiHelper is not ready
 * 
 * @author Steve Schafer / Innovent Solutions
 * @version 1.0
 */
public class NotReadyException extends IdapiHelperException
{
	private static final long serialVersionUID = 3032603386431974331L;

	public NotReadyException()
	{
		super("Not ready");
	}
}
