package org.galagosearch.core.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.galagosearch.core.index.IndexReader;
import org.galagosearch.core.index.IndexWriter;
import org.galagosearch.tupleflow.Utility;

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

	public static int min, count = 0;

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
	public static TreeMap<String, TimeWrap> _perfectTuples;

	public static HashMap<String, Integer> keyValuePair;
	public static RandomAccessFile valueFile;
	
	private static HashMap<Character, Byte> encoding = new HashMap<Character, Byte>();
	private static HashMap<Byte, Character> decoding = new HashMap<Byte, Character>();
	
	private static byte[][] lookup = new byte[128][128];
	private static String[] toDecode = new String[128];
	private static char[] toEncode = new char[]{'0', '1', '2', '3', '4', 
							'5', '6', '7', '8', '9', '-', ':', '#', '$'};

	int date = 0, month = 0, year = 0;

	public static double[] abs_T;

	public static int counter = 0, offset;

	public Time(){}

	public static PrintStream qFos, qlog;

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
		ans += 30*(t1.month - t2.month) + (t1.date - t2.date);

		return Math.abs(ans);
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
			//qFos = new PrintStream(new File("/home/ocean/Desktop/QueryDump.txt"));
			path = args;
			f_Time = new File(path + TTIndex);
			f_Abs = new File(path + Tabs);
			File file = new File(path + "/values");
			file.mkdir();
			valueFile = new RandomAccessFile(new File(path + "/values"), "rw");
			
			encoding.put('1', (byte) 1);
			encoding.put('2', (byte) 2);
			encoding.put('3', (byte) 3);
			encoding.put('4', (byte) 4);
			encoding.put('5', (byte) 5);
			encoding.put('6', (byte) 6);
			encoding.put('7', (byte) 7);
			encoding.put('8', (byte) 8);
			encoding.put('9', (byte) 9);
			encoding.put('0', (byte) 10);
			encoding.put('-', (byte) 11);
			encoding.put('#', (byte) 12);
			encoding.put(':', (byte) 13);
			encoding.put('$', (byte) 14);
			
			byte b = 1;
			for(int j = 0; j< toEncode.length; j++){
				for(int k = j; k< toEncode.length; k++){
					lookup[toEncode[j]][toEncode[k]] = b;
					toDecode[b] = toEncode[j]+""+toEncode[k];
					b++;
				}
			}
			
			

			if(isBuild) {
				keyValuePair = new HashMap<String, Integer>();
				offset = 0;
				tWriter = new IndexWriter(path + index);
				t_Doc_Writer = new IndexWriter(path + "/TuplesTime");
			}
			if(isSearch){
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path + "/keyIndex"));
				keyValuePair = (HashMap<String, Integer>) ois.readObject();
				ois.close();
				//tReader = new IndexReader(path + index);
				//t_Doc_Reader = new IndexReader(path + "/TuplesTime");
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
	public static synchronized void add(Document doc){
		try {
			count++;
			//tWriter.add(ge);
			sbTuple.setLength(0);
			for(TimeTuple tup:doc.T){
				sbTuple.append("#" + tup.toStore());
			}
			//t_Doc_Writer.add(new GenericElement(doc.identifier, 
				//	doc.publication.toString() + "#" + doc.timeFrame.toStore() + sbTuple.toString()));

			makeYours(doc.identifier, 
					doc.publication.toString() + "#" + doc.timeFrame.toStore() 
					+ sbTuple.toString());
			doc.T = null;
			doc.timeFrame = null;
			doc.publication = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static synchronized void makeYours(String identifier, String value) {
		// TODO Auto-generated method stub
		try {
			byte[] v = value.getBytes();//encode(value);
			//System.out.println(value.getBytes().length + " : " + v.length);
			valueFile.writeInt(v.length);
			valueFile.write(v);
			keyValuePair.put(identifier, offset);
			offset += v.length + 4;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized static byte[] encode(String value) {
		// TODO Auto-generated method stub
		byte[] b = new byte[(int) Math.ceil(1.0*value.length()/2)];
		for(int i=0, j = 0;i<b.length;i++){
			try{
				b[i] = lookup[value.charAt(j++)][value.charAt(j++)];				
			}catch(StringIndexOutOfBoundsException e){
				b[i] = lookup[value.charAt(j-2)]['$'];
			}
		}
		return b;
	}

	public static synchronized TimeWrap getTimeWrap(String key){
		try{
			int offset = keyValuePair.get(key);
			valueFile.seek(offset);
			int length = valueFile.readInt();
			byte[] b = new byte[length];
			valueFile.readFully(b);
			String ss[] = new String(b).split("#");//decode(b).split("#");

			Time pub = new Time(ss[0].split("-"));
			TimeTuple timeFrame = new TimeTuple(ss[1].split(":"));

			ArrayList<TimeTuple> t = new ArrayList<TimeTuple>();
			ss = Utility.subarray(ss, 2);
			for(String s:ss){
				t.add(new TimeTuple(s.split(":")));
			}
			return new TimeWrap(pub, timeFrame, t);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	static private StringBuffer cbuf = new StringBuffer();
	private static String decode(byte[] b) {
		// TODO Auto-generated method stub
		cbuf.setLength(0);
		for(int i = 0; i<b.length; i++){
			cbuf.append(toDecode[b[i]]);
		}
		
		return cbuf.toString();
	}

	public static void dump(){
		try{
			PrintWriter pw = new PrintWriter(new File(path + "/keyValue.txt"));
			for(Entry<String, Integer> en:keyValuePair.entrySet()){
				valueFile.seek(en.getValue());
				int length = valueFile.readInt();
				byte[] b = new byte[length];
				valueFile.readFully(b);
				pw.write(en.getKey() + " --> " + new String(b));
				pw.write("\n");
			}
			pw.flush();
			pw.close();
		}
		catch(Exception e){
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
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File(path + "/keyIndex")));
			oos.writeObject(keyValuePair);
			oos.flush();
			oos.close();

			if(valueFile != null){
				valueFile.close();
			}
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