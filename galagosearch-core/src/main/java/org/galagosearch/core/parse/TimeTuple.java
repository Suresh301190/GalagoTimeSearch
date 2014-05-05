package org.galagosearch.core.parse;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.print.Doc;


public class TimeTuple implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 318681837648995451L;

	public static TimeTuple qTF;
	private static double q_abs = 1;
	private static final double _defABS;

	public static void Q_abs(){
		q_abs = abs(qTF);
	}

	public Time tb_l, tb_u, te_l, te_u;
	static final TimeTuple _defTimeFrame;
	static{
		_defTimeFrame = new TimeTuple("1-1-1900:31-12-1900:1-1-2015:31-12-2015".split(":"));
		System.out.println(_defABS = abs(_defTimeFrame));
	}
	boolean hasFrame = false;

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
	
	private TimeTuple(Time t, Time pub){
		if(t.year == 0){
			t.year = pub.year;
		}
		te_u = te_l = tb_u = tb_l = t;
		
		if(tb_l.month == 0){
			tb_l.month = 1;
			tb_u.month = 12;
			te_l.month = 1;
			te_u.month = 12;
		}
		if(tb_l.date == 0){
			tb_l.date = 1;
			tb_u.date = 30;
			te_l.date = 1;
			te_u.date = 30;
		}
	}

	/**
	 * To find the P(Q|T) efficiently
	 * @param key to retrieve Time info from index
	 * @param Q Query Temporal info
	 * @return the value of P(Q|T)
	 * @throws IOException
	 */
	public static double overlap(String key) throws IOException{
		//System.out.println(key + " : " + Time.tReader.getValueString(key));
		//System.out.println(Time._keys.contains(key));
		//System.out.println(Time._Map.get(key));
		TimeWrap D;
		/*
		if((D = Time._perfectMap.get(key)) == null){
			D = new TimeWrap(_defTimeFrame);
			return 100;
			//System.out.println(key);
		}
		else{
			if(check(D.timeFrame.tb_l)){
				D.timeFrame = _defTimeFrame;
			}
			//System.out.println(D.timeFrame.toString());
		}
		//*/
		Entry<String, TimeWrap> en = Time._perfectMap.floorEntry(key);
		if(en != null){
			D = en.getValue();
			if(check(D.timeFrame.tb_l)){
				D.timeFrame.update(new Time(("0-0-" + D.publication.year).split("-")));
				D.absT = abs(D.timeFrame);
			}
			double dr = 1.0 * D.absT*q_abs;
			double pQ_T;
			if((pQ_T = (intersection(D.timeFrame, qTF))) == -1){
				return 1000;
			}
			//System.out.println(D.timeFrame.toString() + "\n" + intersection(D.timeFrame, qTF) + " " + dr + " : " + pQ_T);
			return pQ_T/dr;
		}

		return 100;
		//*/
	}

	public static boolean check(Time t) {
		// TODO Auto-generated method stub
		return t.year == 0 && t.month == 0 && t.date == 0;
	}

	private static double intersection(TimeTuple tf, TimeTuple q) {
		// TODO Auto-generated method stub

		if(tf.te_u.compareTo(q.tb_l) < 0 || q.te_u.compareTo(tf.tb_l) < 0){
			return -1;
		}
		else{
			TimeTuple in = new TimeTuple();
			in.tb_l = tf.tb_l.compareTo(q.tb_l)>0?tf.tb_l:q.tb_l;
			in.tb_u = tf.tb_u.compareTo(q.tb_u)<0?tf.tb_u:q.tb_u;

			if(in.tb_l.compareTo(in.tb_u) > 0){
				in.tb_u = tf.tb_u.compareTo(q.tb_u)>0?tf.tb_u:q.tb_u;
			}
			in.te_l = tf.te_l.compareTo(q.te_l)>0?tf.te_l:q.te_l;
			in.te_u = tf.te_u.compareTo(q.te_u)<0?tf.te_u:q.te_u;

			if(in.te_l.compareTo(in.te_u) > 0){
				in.te_l = tf.te_l.compareTo(q.te_l)<0?tf.te_l:q.te_l;
			}

			System.out.println(tf.toString());
			System.out.println(q.toString());
			System.out.println(in.toString());

			System.out.println("\n");
			
			

			return (abs(in));
		}
	}

	private static boolean overlapExists(TimeTuple tf, TimeTuple q) {
		// TODO Auto-generated method stub
		if(tf.te_u.compareTo(q.tb_l) <= 0 || q.te_u.compareTo(tf.tb_l) < 0){

		}
		return false;
	}

	/**
	 * To compute the abs(T) efficiently
	 * @param timeFrame to compute from
	 * @return the abs value of Time Frame
	 */
	public static double abs(TimeTuple timeFrame) {
		// TODO Auto-generated method stub
		if(timeFrame.tb_u.compareTo(timeFrame.te_l) <= 0){
			return Math.abs((Time.abs(timeFrame.tb_u, timeFrame.tb_l) + 1)
					* (Time.abs(timeFrame.te_u, timeFrame.te_l) + 1));
		}

		double ans = (Time.abs(timeFrame.te_u, timeFrame.te_l) + 1)
				*((Time.abs(timeFrame.te_l, timeFrame.tb_l) + 1) + Time.abs(timeFrame.tb_u, timeFrame.te_l));

		ans -= 0.5*(Time.abs(timeFrame.tb_u, timeFrame.te_l)) 
				* (Time.abs(timeFrame.tb_u, timeFrame.te_l) + 1);
		return Math.abs(ans);
	}

	/**
	 * Extracts the time information from the document and 
	 * sets TimeFrame for the document
	 * @param document from which it needs to be retrieved
	 */
	public static void setTimeFrame(Document document, boolean isQuery) {
		// TODO Auto-generated method stub
		document.timeFrame = new TimeTuple();
		document.T = new ArrayList<TimeTuple>();
		Time t = new Time();
		
		int counter = 0;

		for (int i = 0; i < document.terms.size(); ++i) {
			String term = document.terms.get(i);

			if (isYear(term)) {
				t.year = Integer.parseInt(term);
				t.month = getMonth(document.terms, i);
				t.date = getDate(document.terms, i);
				document.timeFrame.hasFrame = true;
				document.timeFrame.update(t);
				addTuple(document, t);
				counter = i;
				if(isQuery){
					document.terms.remove(i);
					--i;
				}
			}
			else if(isMonth(term)){
				t.month = Time.monthMap.get(term);
				t.date = getDate(document.terms, i);
				document.timeFrame.hasFrame = true;
				if(t.year != 0) document.timeFrame.update(t);
				
				if(Math.abs(counter - i) > 1){
					addTuple(document, t);
				}
				
				if(isQuery){
					document.terms.remove(i);
					--i;
				}
			}
		}
		
		System.out.println(document.T.toString());
	}
	
	private static synchronized void addTuple(Document document, Time t){
		document.T.add(new TimeTuple(t, document.publication));
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
	
	StringBuffer sbTuple = new StringBuffer();
}