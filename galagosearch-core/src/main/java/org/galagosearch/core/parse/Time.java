package org.galagosearch.core.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.galagosearch.core.index.IndexElement;
import org.galagosearch.core.index.IndexReader;
import org.galagosearch.core.index.IndexWriter;

public class Time implements Comparable<Time>{

	private static final String index = "TimeIndex";
	/**
	 * To write to Index File TImeIndex
	 */
	public static IndexWriter tWriter;

	/**
	 * To read Index file TimeIndex
	 */
	public static IndexReader tReader;
	public static PrintStream ps;

	static HashMap<String, Integer> monthMap;

	int date, month, year;

	public static void copy(Time t1, Time t2) {
		// TODO Auto-generated constructor stub
		t1.date = t2.date;
		t1.month = t2.month;
		t1.year = t2.year;
	}


	/**
	 * To set the respective date, month, year mapping to 
	 * Numbers Jan to 1, Feb to 2 respectively
	 * @param args 
	 * @param out 
	 */
	public static void init(String args, boolean isBuild, OutputStream out){

		try {
			if(isBuild) tWriter = new IndexWriter(args + index);
			if(!isBuild) tReader = new IndexReader(args + index);			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("In Time Init take a look");
			System.exit(-1);
		}
		ps = new PrintStream(out);
		monthMap = new HashMap<String, Integer>();
		monthMap.put("jan", 1);	monthMap.put("january", 1); 	monthMap.put("1", 1);
		monthMap.put("feb", 2); monthMap.put("february", 2);	monthMap.put("2", 2);
		monthMap.put("mar", 3); monthMap.put("march", 3);		monthMap.put("3", 3);
		monthMap.put("apr", 4); monthMap.put("april", 4);		monthMap.put("4", 4);
		monthMap.put("may", 5);									monthMap.put("5", 5);
		monthMap.put("jun", 6); monthMap.put("june", 6);		monthMap.put("6", 6);
		monthMap.put("jul", 7); monthMap.put("july", 7);		monthMap.put("7", 7);
		monthMap.put("aug", 8); monthMap.put("august", 8);		monthMap.put("8", 8);
		monthMap.put("sep", 9); monthMap.put("september", 9); 	monthMap.put("9", 9);	
		monthMap.put("oct", 10);monthMap.put("october", 10);	monthMap.put("10", 10);
		monthMap.put("nov", 11);monthMap.put("november", 11);	monthMap.put("11", 11);
		monthMap.put("dec", 12);monthMap.put("december", 12);	monthMap.put("12", 12);

		monthMap.put("fall", 9);
	}


	/**
	 * adds a Index Element to TimeInvertedList in Synchronized way
	 * @param generic Element to add
	 */
	public static synchronized void add(IndexElement ge){
		try {
			tWriter.add(ge);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Synchronized method to flush the partial inverted list on to file
	 * as the buffer fills up
	 */
	public static synchronized void flush(){
		try {
			tWriter.flush();
			ps.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int compareTo(Time t) {
		// TODO Auto-generated method stub
		if(year == 0){
			return 0;
		}
		if(year == t.year){
			if(month == t.month){
				return date - t.date;
			}
			else return month - t.month;
		}
		else return year - t.year;
	}

	public String toString(){
		return date + "-" + month + "-" + year;
	}

	/**
	 * retrieves the publication time from meta-data of the XML file
	 * @param document from which it  needs to be retrieved
	 * @return time if it contains else null
	 */
	public static void setPublicationTime(Document document) {
		// TODO Auto-generated method stub
		document.publication = new Time();
		try{
			for(Tag tag:document.tags){
				String name = tag.attributes.get("name");
				if(name != null){
					if(name.equals("publication_day_of_month")){
						document.publication.date = 
								Integer.parseInt(tag.attributes.get("content"));
					}
					else if(name.equals("publication_month")){
						document.publication.month = 
								Integer.parseInt((tag.attributes.get("content")));

					}
					else if(name.equals("publication_year")){
						document.publication.year = 
								Integer.parseInt((tag.attributes.get("content")));
					}
				}
			}
		}catch(Exception e){
			//e.printStackTrace();
			Time.ps.println("In Extract Time Info");
		}
	}

	/**
	 * To close all the IndexReaders, IndexWriters, and Streams
	 */
	public static void close(){
		try {
			if(ps != null) 
				ps.close();

			if(tWriter != null) 
				tWriter.close();

			if(tReader != null) 
				tReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}