package org.galagosearch.core.parse;

import java.io.Serializable;


public class TimeWrap implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8227525493264447910L;
	public Time publication;
	public TimeTuple timeFrame;
	
	public TimeWrap(String[] val){
		//System.out.println(val[0] + " " + val[1]);
		publication = new Time(val[0].split("-"));
		timeFrame = new TimeTuple(val[1].split(":"));
	}
}
