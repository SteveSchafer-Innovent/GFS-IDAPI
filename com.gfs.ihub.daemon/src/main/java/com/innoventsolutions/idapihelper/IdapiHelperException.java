package com.innoventsolutions.idapihelper;

/**
 * An exception thrown by IdapiHelper
 * 
 * @author Steve Schafer / Innovent Solutions
 * @version 1.0
 */
public class IdapiHelperException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3669678356216750103L;

	public IdapiHelperException()
	{
		super();
	}

	public IdapiHelperException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public IdapiHelperException(String message)
	{
		super(message);
	}

	public IdapiHelperException(Throwable cause)
	{
		super(cause);
	}
}
