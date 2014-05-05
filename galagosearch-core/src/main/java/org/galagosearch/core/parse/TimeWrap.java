package org.galagosearch.core.parse;

import java.io.Serializable;
import java.util.ArrayList;


public class TimeWrap implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8227525493264447910L;
	public Time publication;
	public TimeTuple timeFrame;
	public ArrayList<TimeTuple> timeTuples;
	public double absT;
	
	public TimeWrap() {
		publication = new Time();
		timeFrame = new TimeTuple();
		absT = TimeTuple.abs(timeFrame);
	}
	
	public TimeWrap(String[] val){
		//System.out.println(val[0] + " " + val[1]);
		publication = new Time(val[0].split("-"));
		timeFrame = new TimeTuple(val[1].split(":"));
		absT = TimeTuple.abs(timeFrame);
	}
	
	public TimeWrap(Time t, TimeTuple timeFrame, ArrayList<TimeTuple> timeTuples){
		this.publication = t;
		this.timeFrame = timeFrame;
		this.timeTuples = timeTuples;
	}
	
	public TimeWrap(TimeTuple deftimeframe) {
		// TODO Auto-generated constructor stub
		timeFrame = deftimeframe;
		absT = TimeTuple.abs(timeFrame);
	}

	public String toString() {
		return publication.toString() + "|" + timeFrame.toString();
	}
}
