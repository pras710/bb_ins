package main.java;
import org.apache.spark.*;
import org.apache.spark.storage.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import scala.reflect.ClassTag; 
import scala.reflect.ClassTag$;
//class Addresses implements Comparable<String>
//{
//	String addresses[] = null;
//	public Addresses(String add)
//	{
//		this.addresses = add.split(",");
//	}
//	public int compareTo(String s)
//	{
//	}
//}
public class LightStrander 
{
	static boolean insDecodeDebug = false;
	static long linesRead = 0;

	static long totalInstructions = 0;
	static String toCheckString = ""; 
	static Hashtable<String, String> tidStringTracker = new Hashtable<>();
	public static void main1(String args[])throws Exception
	{

//		getSub("[(400d160c,13), (400d160c,13)]");
//		getSub("[(400d160c,13), (400d160c,13), (400d160c,13)]");
//		getSub("[(400d160c,13), (400d160c,13), (400d160c,13), (400d10c4,21), (400d160c,13), (400d160c,13)]");
		if(args == null)
		{
			args = new String[]{"google_ins_dump_bbs.gz", "1", ""};
		}
		String path = "/i3c/hpcl/huz123/spark/java_example_pras/data_proc/";
		System.out.println(args[2]);
		HashSet<Long> correctedBasicBlockFlows = new HashSet<>();
		HashMap<Long, BasicBlock> basicBlocksFormed = new HashMap<>();
		TreeMap<Long, Map.Entry<BasicBlock, Long>> programFlow = new TreeMap<>();
		TreeMap<Long, String> addressSequence = new TreeMap<Long, String>();
		int upto = Integer.parseInt(args[1]);
		toCheckString = args[2];
		String line = "";
		//OLD: BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));
		//NEW:
		SparkConf conf = new SparkConf().setAppName("LightStrander Hero DA!");
		JavaSparkContext sc = new JavaSparkContext(conf);
		System.out.println("starting the file read.. with "+sc.defaultMinPartitions()+" partitions");
		JavaRDD<String> lines_bbs = sc.textFile(path+args[0], 100).cache();//+"_bbs"
		
		//System.out.println("bbFlows is geting :"+lines.toArray().size());//size());
		JavaRDD<BasicBlock> bbFlows = lines_bbs.map(new Function<String, BasicBlock>(){
					@Override
					public BasicBlock call(String bbString)
					{
						BasicBlock bb = null;
					//	if(bbString.indexOf("Lookup") == -1)
					//	{
					//		System.out.println(bbString);
					//	}
						if(bbString.indexOf("null")!=-1 && (bbString.indexOf("Lookup") != -1 || bbString.indexOf("Lokup") != -1))
						{
							return new BasicBlock("gap", -1, bbString);
						}
						try
						{
							/*if(bbString.indexOf("Lookup") != -1)
							{
								System.out.println("lookup");
								String[] temp_s = bbString.split(" "); 
								temp_s[0] = temp_s[0].replaceAll(",","").trim();
								String pc_string = "pc";
								pc_string = temp_s[2];
								long pc = Long.parseLong(pc_string.trim(), 16);
								int bytes = Integer.parseInt(temp_s[3].trim());
								bb = new BasicBlock("gap", pc, bbString);
								return bb;
							}
							else*/ if(bbString.indexOf("*end*") == -1)
							{
								//first line..
								return new BasicBlock("!gap", -1, bbString);
							}
							else
							{
								//System.out.println("good?");
								StringTokenizer strTok = new StringTokenizer(bbString, "|^|");
								String lineNumber = "";//strTok.nextToken();
								String line = strTok.nextToken();
								if(line.indexOf("Lookup")!=-1 || line.indexOf("Lokup")!=-1)
								{
									line = strTok.nextToken();
								}
								String prev = "";
								String temp_s[] = line.split(" ");
???LINES MISSING
???LINES MISSING
???LINES MISSING
		//	bb.printStrands();
		//}
		//bbFlows.partition
		//bbFlows.saveAsTextFile("file:/i3c/hpcl/huz123/spark/java_example_pras/output_file");
		//bbFlows.collect().foreach(println);
		//StrandMaintainer strand = new StrandMaintainer();

	//	System.out.println(memoryFrames);
	}
}
