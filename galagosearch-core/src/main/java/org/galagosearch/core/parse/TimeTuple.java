package org.galagosearch.core.parse;

import java.util.List;


public class TimeTuple{
	Time tb_l, tb_u, te_l, te_u;
	
	public TimeTuple(){
		tb_l = new Time();
		tb_u = new Time();
		te_l = new Time();
		te_u = new Time();
	}

	public int overlap(Time t){

		return 0;
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
            else{
            	
            }
        }
	}
	
	private void update(Time t) {
		// TODO Auto-generated method stub
		
		if(tb_l.compareTo(t) >= 0) Time.copy(tb_l, t);
		Time.copy(tb_u, t);
		Time.copy(te_l, t);
		if(te_u.compareTo(t) <= 0)Time.copy(te_u, t);
	}

	private static int getDate(List<String> terms, int i) {
		// TODO Auto-generated method stub
		return 1;
	}
	
	public String toString() {
		return  " tb_l : " + tb_l.toString() +
				" tb_u : " + tb_u.toString() +
				" te_l : " + te_l.toString() + 
				" te_u : " + te_u.toString();
	}

	/**
	 * @param month to be checked 
	 * @return true if month is a valid month literal
	 */
	private static boolean isMonth(String month) {
		return Time.monthMap.containsKey(month);
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

		return 1;
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
}