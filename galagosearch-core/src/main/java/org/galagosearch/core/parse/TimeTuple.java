package org.galagosearch.core.parse;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;


public class TimeTuple implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 318681837648995451L;

	Time tb_l, tb_u, te_l, te_u;
	
	static StringBuffer sb = new StringBuffer("0000000");
	public TimeTuple() {
		tb_l = new Time();
		tb_u = new Time();
		te_l = new Time();
		te_u = new Time();
	}

	public TimeTuple(String[] t) {
		// TODO Auto-generated constructor stub
		tb_l = new Time(t[0].split("-"));
		tb_u = new Time(t[1].split("-"));
		te_l = new Time(t[2].split("-"));
		te_u = new Time(t[3].split("-"));
	}

	/**
	 * To find the P(Q|T) efficiently
	 * @param key to retrieve Time info from index
	 * @param Q Query Temporal info
	 * @return the value of P(Q|T)
	 * @throws IOException
	 */
	public static double overlap(String key, TimeTuple Q) throws IOException{
		//System.out.println(key + " : " + Time.tReader.getValueString(key));
		//System.out.println(Time._keys.contains(key));
		//System.out.println(Time._Map.get(key));
		
		/*
		TimeWrap D = Time._Map.get(key);
		System.out.println(D.timeFrame.toString());
		double dr = abs(D.timeFrame)*abs(Q);
		
		double pQ_T = 0;// = intersection(D.timeFrame, Q);
		pQ_T = pQ_T/dr;
			
		//*/
		return 1.0;
	}

	/**
	 * To compute the abs(T) efficiently
	 * @param timeFrame to compute from
	 * @return the abs value of Time Frame
	 */
	public static double abs(TimeTuple timeFrame) {
		// TODO Auto-generated method stub
		if(timeFrame.tb_u.compareTo(timeFrame.te_l) <= 0){
			return (Time.abs(timeFrame.tb_u, timeFrame.tb_l) + 1)
					* (Time.abs(timeFrame.te_u, timeFrame.te_l) + 1);
		}
		
		double ans = (Time.abs(timeFrame.te_u, timeFrame.te_l) + 1);
		
		ans *= (Time.abs(timeFrame.te_l, timeFrame.tb_l) + 1)
				+ Time.abs(timeFrame.tb_u, timeFrame.te_l)
				- 0.5*(Time.abs(timeFrame.tb_u, timeFrame.tb_l));
		return ans;
	}

	/**
	 * Extracts the time information from the document and 
	 * sets TimeFrame for the document
	 * @param document from which it needs to be retrieved
	 */
	public static void setTimeFrame(Document document) {
		// TODO Auto-generated method stub
		document.timeFrame = new TimeTuple();
		Time t = new Time();
		
		for (int i = 0; i < document.terms.size(); ++i) {
            String term = document.terms.get(i);
            
            if (isYear(term)) {
                t.year = Integer.parseInt(term);
                t.month = getMonth(document.terms, i);
                t.date = getDate(document.terms, i);
                
                document.timeFrame.update(t);
            }
            else if(isMonth(term)){
            	t.month = Time.monthMap.get(term);
            	t.date = getDate(document.terms, i);
            	
            	if(t.year != 0) document.timeFrame.update(t);
            }
        }
	}
	
	private void update(Time t) {
		// TODO Auto-generated method stub
		
		if(tb_l.compareTo(t) >= 0){
			copy(tb_l, t, 1, 1);
			copy(tb_u, t, 30, 12);
		}		
		
		if(te_u.compareTo(t) <= 0) {
			copy(te_l, t, 1, 1);
			copy(te_u, t, 30, 12);
		}
	}
	
	/**
	 * Copies time t1 from t2 
	 * @param t1 time to copy
	 * @param t2 time from copy
	 * @param defDate default date
	 * @param defMonth default value
	 */
	public static void copy(Time t1, Time t2, int defDate, int defMonth) {
		// TODO Auto-generated constructor stub
		
		if(t2.date != 0) t1.date = t2.date;
		else t1.date = defDate;
		
		if(t2.month != 0) t1.month = t2.month;
		else t1.month = defMonth;
		
		if(t2.year != 0){
			t1.year = t2.year;
		}
	}

	private static int getDate(List<String> terms, int i) {
		// TODO Auto-generated method stub
		
		if (i > 0 && isDate(terms.get(i-1))) {
			return Integer.parseInt(terms.get(i-1));
		}
		
		if (i < terms.size()-1 && isDate(terms.get(i+1))) {
			return Integer.parseInt(terms.get(i+1));
		}
		
		return 0;
	}
	
	private static boolean isDate(String term) {
		// TODO Auto-generated method stub
		
		if(term.length() > 2 || !term.matches("[0-9]+")) return false;
		else return true;
	}

	public String toString() {
		return  " tb_l : " + tb_l.toString() +
				" tb_u : " + tb_u.toString() +
				" te_l : " + te_l.toString() + 
				" te_u : " + te_u.toString();
	}
	
	public String toStore(){
		return  tb_l.toString() +
				":"+ tb_u.toString() +
				":"+ te_l.toString() + 
				":" + te_u.toString();
	}

	/**
	 * @param month to be checked 
	 * @return true if month is a valid month literal
	 */
	private static boolean isMonth(String month) {
		return Time.monthMap.containsKey(month.toLowerCase());
	}

	/**
	 * Retrieves the month associated with the year
	 * @param terms tokenized list of documents
	 * @param i index @which year was extracted
	 * @return month if exists
	 */
	private static int getMonth(List<String> terms, int i) {
		if (i > 0 && isMonth(terms.get(i-1))) {
			return Time.monthMap.get(terms.get(i-1));
		}

		if (i > 1 && isMonth(terms.get(i-2))) {
			return Time.monthMap.get(terms.get(i-2));
		}

		if (i < terms.size()-1 && isMonth(terms.get(i+1))) {
			return Time.monthMap.get(terms.get(i+1));
		}

		return 0;
	}

	/**
	 * returns true if the term is a year
	 * @param year to be checked for
	 * @return true if it can be an year
	 */
	private static boolean isYear(String year) {
		if (year.length() != 4)
			return false;

		char first = year.charAt(0);
		if (first != '1' && first != '2')
			return false;

		return Character.isDigit(year.charAt(1)) &&
				Character.isDigit(year.charAt(2)) &&
				Character.isDigit(year.charAt(3));
	}
	
	/**
	 * Might not use it
	 * @param year to be checked
	 * @return true if it might be an year token
	 */
	public static boolean isYear2(String year){
		
		if (year.length() != 2)
			return false;

		char first = year.charAt(0);
		if (first != '9' && first != '0')
			return false;

		return Character.isDigit(year.charAt(1));
	}
}