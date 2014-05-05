package org.galagosearch.core.parse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.galagosearch.core.index.GenericElement;
import org.galagosearch.core.index.IndexElement;
import org.galagosearch.core.index.IndexReader;
import org.galagosearch.core.index.IndexWriter;

public class Time implements Comparable<Time>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5083703260990174416L;
	public static final String index = "TimeIndex", TTIndex = "TimeTupleIndex", Tabs ="TimeABS";
	/**
	 * To write to Index File TImeIndex
	 */
	public static IndexWriter tWriter, t_Doc_Writer;

	/**
	 * To read Index file TimeIndex
	 */
	public static IndexReader tReader, t_Doc_Reader;
	
	public static int min;
	
	public static boolean isBuild = false, isSearch = false;
	public static PrintStream ps;
	
	public static String path;
	
	/**
	 * To check for existence of Temporal Index files
	 */
	public static File f_Time, f_Abs; 

	static HashMap<String, Integer> monthMap;
	public static HashSet<String> _keys;
	public static HashMap<String, TimeWrap> _Map;
	
	public static TreeMap<String, TimeWrap> _perfectMap;

	int date = 0, month = 0, year = 0;
	
	public static double[] abs_T;
	
	public static int counter = 0;

	public Time(){}
	
	public static PrintStream qFos;
	
	/**
	 * Parses the input string and extracts the time from it.
	 * @param t
	 */
	public Time(String[] t) {
		// TODO Auto-generated constructor stub
		//System.out.println(t[0] +  " " + t[1] + " " + t[2]);
		date = Integer.parseInt(t[0]);
		month = Integer.parseInt(t[1]);
		year = Integer.parseInt(t[2]);
	}
	
	public static double abs(Time t1, Time t2){
		double ans;
		
		ans = (double) 365*(t1.year - t2.year);
		ans += 30*(t1.month - t1.month) + (t1.date - t2.date);
		
		return ans;
	}


	/**
	 * To set the respective date, month, year mapping to 
	 * Numbers Jan to 1, Feb to 2 respectively
	 * @param args 
	 * @param out 
	 */
	public static void init(String args, boolean isBuild, OutputStream out){
		Time.isBuild = isBuild;
		Time.isSearch = !isBuild;
		
		try {
			qFos = new PrintStream(new File("/home/ocean/Desktop/QueryDump.txt"));
			path = args;
			f_Time = new File(path + TTIndex);
			f_Abs = new File(path + Tabs);
			
			if(isBuild) {
				tWriter = new IndexWriter(path + index);
				t_Doc_Writer = new IndexWriter(path + "/TuplesTime");
			}
			if(!isBuild){
				tReader = new IndexReader(path + index);
				t_Doc_Reader = new IndexReader(path + "/TuplesTime");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("In Time Init take a look");
			System.exit(-1);
		}
		if(isSearch){
			_Map = new HashMap<String, TimeWrap>();
			_keys = new HashSet<String>();
		}
		
		ps = new PrintStream(out);
		monthMap = new HashMap<String, Integer>();
		monthMap.put("jan", 1);	monthMap.put("january", 1); 	
		monthMap.put("feb", 2); monthMap.put("february", 2);	
		monthMap.put("mar", 3); monthMap.put("march", 3);		
		monthMap.put("apr", 4); monthMap.put("april", 4);		
		monthMap.put("may", 5);									
		monthMap.put("jun", 6); monthMap.put("june", 6);		
		monthMap.put("jul", 7); monthMap.put("july", 7);		
		monthMap.put("aug", 8); monthMap.put("august", 8);		
		monthMap.put("sep", 9); monthMap.put("september", 9); 		
		monthMap.put("oct", 10);monthMap.put("october", 10);	
		monthMap.put("nov", 11);monthMap.put("november", 11);	
		monthMap.put("dec", 12);monthMap.put("december", 12);	
		
		monthMap.put("Jan", 1);	monthMap.put("January", 1); 	
		monthMap.put("Feb", 2); monthMap.put("February", 2);	
		monthMap.put("Mar", 3); monthMap.put("March", 3);		
		monthMap.put("Apr", 4); monthMap.put("April", 4);		
		monthMap.put("May", 5);									
		monthMap.put("Jun", 6); monthMap.put("June", 6);		
		monthMap.put("Jul", 7); monthMap.put("July", 7);		
		monthMap.put("Aug", 8); monthMap.put("August", 8);		
		monthMap.put("Sep", 9); monthMap.put("September", 9); 		
		monthMap.put("Oct", 10);monthMap.put("October", 10);	
		monthMap.put("Nov", 11);monthMap.put("November", 11);	
		monthMap.put("Dec", 12);monthMap.put("December", 12);	

		//monthMap.put("fall", 9); monthMap.put("qtr", 4);
		//monthMap.put("Fall", 9); monthMap.put("Qtr", 4);
		//monthMap.put("Fall", 9); monthMap.put("Mid", 6);
	}

	private static StringBuffer sbTuple = new StringBuffer("");

	/**
	 * adds a Index Element to TimeInvertedList in Synchronized way
	 * @param generic Element to add
	 */
	public static synchronized void add(IndexElement ge, Document doc){
		try {
			tWriter.add(ge);
			sbTuple.setLength(0);
			for(TimeTuple tup:doc.T){
				sbTuple.append(tup.toStore() + "#");
			}
			t_Doc_Writer.add(new GenericElement(doc.identifier, sbTuple.toString()));
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
			t_Doc_Writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * to retrieve time from the index
	 * @param key
	 * @return string representation of TimeFrame
	 * @throws IOException
	 */
	public static synchronized String getTime(String key) throws IOException{
		return tReader.getValueString(key);
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
			
			if(t_Doc_Writer != null){
				t_Doc_Writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}