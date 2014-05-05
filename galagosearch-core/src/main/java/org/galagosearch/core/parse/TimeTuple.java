package org.galagosearch.core.parse;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class TimeTuple implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 318681837648995451L;

	public static TimeTuple qTF;
	private static double q_abs = 1;
	public static void Q_abs(){
		q_abs = abs(qTF);
		System.out.println(q_abs);
	}

	public Time tb_l, tb_u, te_l, te_u;
	static final TimeTuple _defTimeFrame;
	static{
		_defTimeFrame = new TimeTuple("1-1-1900:31-12-1900:1-1-2015:31-12-2015".split(":"));
		//System.out.println(abs(_defTimeFrame));
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
		te_u = new Time();
		te_l = new Time();
		tb_u = new Time();
		tb_l = new Time();
		if(t.year == 0){
			tb_u.year = tb_l.year = te_u.year = te_l.year = pub.year;
		}
		else tb_u.year = tb_l.year = te_u.year = te_l.year = t.year;

		if(t.month == 0){
			tb_l.month = 1;
			tb_u.month = 12;
			te_l.month = 1;
			te_u.month = 12;
		}
		else{
			tb_l.month = tb_u.month = te_l.month = te_u.month = t.month;
		}
		if(t.date == 0){
			tb_l.date = 1;
			tb_u.date = 30;
			te_l.date = 1;
			te_u.date = 30;
		}
		else tb_l.date =tb_u.date = te_l.date =	te_u.date = t.date;
	}

	/**
	 * To find the P(Q|T) efficiently
	 * @param key to retrieve Time info from index
	 * @param Q Query Temporal info
	 * @return the value of P(Q|T)
	 * @throws IOException
	 */
	public static double overlap(String key) throws IOException{
	
		TimeWrap D = Time.getTimeWrap(key);
		if(D == null){
			//System.out.println(key + " -- > null");
			//return 1000;
		}
		else{
			Time.qlog.println(key);
			return -sumP_Q_T(D);
			//return 1.0;
		}
		return 1;
	}

	private static double sumP_Q_T(TimeWrap d) {
		// TODO Auto-generated method stub
		double sum = 1.0;
		//System.out.println(d.timeTuples.toString());
		for(TimeTuple tup:d.timeTuples){
			sum = sum + intersection(tup, qTF)/(abs(tup) * q_abs);
		}	
		sum /= abs(d.timeFrame);
		Time.qlog.println((1 + sum) + "\n");
		return (1 + sum);
	}

	public static boolean check(Time t) {
		// TODO Auto-generated method stub
		return t.year == 0 && t.month == 0 && t.date == 0;
	}

	private static double intersection(TimeTuple tf, TimeTuple q) {
		// TODO Auto-generated method stub
		
		/*
		System.out.println("T : " + tf.toString());
		System.out.println("Q : " + q.toString() + "\n");
		//*/
		/*
		if(tf.tb_u.compareTo(q.tb_l) < 0 
				|| q.tb_u.compareTo(tf.tb_l) < 0
				|| tf.te_u.compareTo(q.te_l) < 0
				|| tf.te_l.compareTo(q.te_u) < 0){
			return 0;
		}
		else/**/{
			TimeTuple in = new TimeTuple();
			in.tb_l = tf.tb_l.compareTo(q.tb_l)>0?tf.tb_l:q.tb_l;
			in.tb_u = tf.tb_u.compareTo(q.tb_u)<0?tf.tb_u:q.tb_u;
			in.te_l = tf.te_l.compareTo(q.te_l)>0?tf.te_l:q.te_l;
			in.te_u = tf.te_u.compareTo(q.te_u)<0?tf.te_u:q.te_u;

			if(in.tb_l.compareTo(in.tb_u) > 0 || in.te_l.compareTo(in.te_u) > 0){
				return 0;
			}
			//*
			Time.qlog.println("T : " + tf.toString());
			Time.qlog.println("Q : " + q.toString());
			Time.qlog.println("F : " + in.toString());
			Time.qlog.println(abs(in));
			//*/

			return (abs(in));
		}
	}

	/**
	 * To compute the abs(T) efficiently
	 * @param timeFrame to compute from
	 * @return the abs value of Time Frame
	 */
	public static double abs(TimeTuple timeFrame) {
		// TODO Auto-generated method stub
		if(timeFrame.tb_u.compareTo(timeFrame.te_l) <= 0){
			return ((Time.abs(timeFrame.tb_u, timeFrame.tb_l) + 1)
					* (Time.abs(timeFrame.te_u, timeFrame.te_l) + 1));
		}

		double ans = (Time.abs(timeFrame.te_u, timeFrame.te_l) + 1)
				*((Time.abs(timeFrame.te_l, timeFrame.tb_l) + 1) 
						+ Time.abs(timeFrame.tb_u, timeFrame.te_l));

		ans -= 0.5*(Time.abs(timeFrame.tb_u, timeFrame.te_l)) 
				* (Time.abs(timeFrame.tb_u, timeFrame.te_l) + 1);
		return Math.abs(ans);
	}

	private static HashSet<Integer> ctmp;

	private static	HashMap<Integer, HashSet<Integer>> check = new HashMap<Integer, HashSet<Integer>>();

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
		check.clear();
		int counter = 0;

		for (int i = 0; i < document.terms.size(); ++i) {
			String term = document.terms.get(i);

			if(isQuery && stopWords.contains(term)){
				document.terms.remove(i--);
			}
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
					document.terms.remove(i--);
				}
			}
		}

		/*
		System.out.println(document.T.toString() + " \n " + check.toString());
		for(Entry<Integer, HashSet<Integer>> c:check.entrySet()){
			System.out.println(c.getKey() + " : " + c.getValue().toString());
		}
		//*/
	}

	
	
	private static synchronized void addTuple(Document document, Time t){
		//System.out.println(t.toString() +" : "+ document.publication.toString());
		if((ctmp = check.get(t.year)) != null){
			
			if(!ctmp.contains(t.month) || t.year == 0){
				//System.out.println(t.toString() +" : "+ document.publication.toString());
				document.T.add(new TimeTuple(t, document.publication));
				ctmp.add(t.month);
			}
		}
		else{
			//System.out.println(t.toString() +" : "+ document.publication.toString());
			document.T.add(new TimeTuple(t, document.publication));
			HashSet<Integer> set = new HashSet<Integer>();
			if(t.month != 0) set.add(t.month);
			check.put(t.year, set);
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

		if(term.length() > 2 || term.charAt(0) > '3' || !term.matches("[0-9]+")) return false;
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
	
	static final HashSet<String> stopWords = 
			new HashSet<String>(Arrays.asList("i'm","you're","he's","she's","it's","we're",
					"they're","i've","you've","we've","they've","i'd","you'd","he'd","she'd",
					"we'd","they'd","i'll","you'll","he'll","she'll","we'll","they'll","isn't",
					"aren't","wasn't","weren't","hasn't","haven't","hadn't","doesn't","don't",
					"didn't","won't","wouldn't","shan't","shouldn't","can't","cannot","couldn't",
					"mustn't","let's","that's","who's","what's","here's","there's","when's",
					"where's","why's","how's","a","an","the","i","me","my","myself","we","us",
					"our","ours","ourselves","you","your","yours","yourself","yourselves","he","him","his",
					"himself","she","her","hers","herself","it","its","itself","they","them",
					"their","theirs","themselves","what","which","who","whom","this","that",
					"these","those","am","is","are","was","were","be","been","being","have",
					"has","had","having","do","does","did","doing","will","would","shall",
					"should","can2","could","may","might","must","ought","and","but","if","or","because","as","until",
					"while","of","at","by","for","with","about","against","between","into",
					"through","during","before","after","above","below","to","from","up",
					"down","in","out","on","off","over","under","again","further","then",
					"once","here","there","when","where","why","how","all","any","both",
					"each","few","more","most","other","some","such","no","nor","not",
					"only","own","same","so","than","too","very","one","every","least","less","many","now",
					"ever","never","say","says","said","also","get","go","goes","just",
					"made","make","put","see","seen","whether","like","well","back",
					"even","still","way","take","since","another","however","two",
					"three","four","five","first","second","new","old","high","long"));
}