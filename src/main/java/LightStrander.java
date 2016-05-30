package main.java;

import java.io.*;
import java.util.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;

public class LightStrander
{
	static boolean insDecodeDebug = false;
	static long linesRead = 0L;
	static long totalInstructions = 0L;
	static String toCheckString = "";
	static Hashtable<String, String> tidStringTracker = new Hashtable();

	public static void main1(String[] args)	throws Exception
	{
		if (args == null) {
			args = new String[] { "google_ins_dump_bbs.gz", "1", "" };
		}
		String path = "/i3c/hpcl/huz123/spark/java_example_pras/data_proc/";
		System.out.println(args[2]);
		String line = "";
		final TrackedStrands trackedStrands = new TrackedStrands(args[2], path+args[0]);
		final TrackedStrands trackedMemoStrands = new TrackedStrands("memo_data", path+args[0]);
		SparkConf conf = new SparkConf().setAppName("LightStrander Hero DA!");
		JavaSparkContext sc = new JavaSparkContext(conf);
		System.out.println("starting the file read.. with " + sc.defaultMinPartitions() + " partitions");
		File f = new File(path+args[0]);
		path = path+args[0]+"/";
		ArrayList<String> fileSnippets = new ArrayList<String>();
		fileSnippets =  new ArrayList<>((List<String>)Arrays.asList(f.list()));
		//fileSnippets.add("maps_2_bbs0266");
		TreeMap<String, HashMap<String, Integer>> myPopularStrands = new TreeMap();
		String fileName = "";
		int processedFileCount = 0;
		while(fileSnippets.size() > 0)// && processedFileCount <= 50)
		{
			processedFileCount++;
			int i = Math.abs((int)(Math.random() * fileSnippets.size()))%fileSnippets.size();
			fileName = fileSnippets.get(i);
			fileSnippets.remove(fileName);
			if(fileName.equals("data_manager") || fileName.equals("memo_data"))continue;
			System.out.println("beginning.. "+fileName);
			JavaRDD<String> lines_bbs = sc.textFile(path + fileName).cache();//, 100
	
			JavaRDD<BasicBlock> bbFlows = lines_bbs.map(new Function<String, BasicBlock>(){
					@Override
					public BasicBlock call(String bbString)
					{
						BasicBlock bb = null;
						if ((bbString.indexOf("null") != -1) && ((bbString.indexOf("Lookup") != -1) || (bbString.indexOf("Lokup") != -1))) {
							return new BasicBlock("gap", -1L, bbString);
						}
						try
						{
							if (bbString.indexOf("*end*") == -1) {
								return new BasicBlock("!gap", -1L, bbString);
							}
							StringTokenizer strTok = new StringTokenizer(bbString, "|^|");
							String lineNumber = "";
							String line = strTok.nextToken();
							if ((line.indexOf("Lookup") != -1) || (line.indexOf("Lokup") != -1)) {
								line = strTok.nextToken();
							}
							String prev = "";
							String[] temp_s = line.split(" ");
							if(temp_s.length == 1)
							{
								return new BasicBlock("!gap", -1L, bbString);
							}
							temp_s[0] = temp_s[0].replaceAll(",", "").trim();
							try
							{
								String pc_string = temp_s[1];
								long pc = Long.parseLong(pc_string, 16);
								bb = new BasicBlock(pc, Integer.parseInt(temp_s[0]), line.substring(4));
								boolean endReached = false;
								while (strTok.hasMoreTokens())
								{
									prev = line;
									line = strTok.nextToken();
									if ((line.indexOf("Lookup") == -1) && (line.indexOf("Lokup") == -1)) {
										if (endReached)
										{
											bb.memoryLines.add(line);
										}
										else if (line.indexOf(" ") == -1)
										{
											if (line.trim().length() != 0)
											{
												lineNumber = line;
												endReached = true;
											}
										}
										else if (line.indexOf("*end*") != -1)
										{
											lineNumber = prev;
											endReached = true;
										}
										else
										{
											try
											{
												bb.instructions.add(line.substring(4));
											}
											catch (Exception e)
											{
												e.printStackTrace();
												System.out.println(line);
												bb.fillInsAndOuts();
												bb.printStrands();
												throw e;
											}
											bb.insCount += 1;
											bb.myIdStr = ("(" + Long.toHexString(bb.startingPC) + "," + bb.insCount + ")");
										}
									}
								}
							}
							catch (Exception e)
							{
								System.out.println(line);
								e.printStackTrace();
								throw e;
							}
							try
							{
								bb.fillInsAndOuts();
							}
							catch (Exception e)
							{
								e.printStackTrace();
								throw e;
							}
							bb.emptyIntermediates(trackedStrands);
							return bb;
						}
						catch (Exception e)
						{
							e.printStackTrace();
							throw e;
						}
					}
				});
			lines_bbs = null;
			System.out.println("done with the bb reads!!");
	
			if(!args[2].equals("data_manager"))//PRAS REMOVE THE IF CONDITION
			{
				JavaPairRDD<String, Integer> strMaints = bbFlows.mapPartitionsToPair(new PairFlatMapFunction<Iterator<BasicBlock>, String, Integer>()
						{
							@Override
							public Iterable<scala.Tuple2<String, Integer>> call(Iterator<BasicBlock> biter)
							{
								StrandMaintainer strm = new StrandMaintainer(-1L, trackedStrands);
								while (biter.hasNext())
								{
									//if(strm.bb_count_stat > 4000000)
									//{
									//	break;
									//}
									BasicBlock b = (BasicBlock)biter.next();
									if (!b.isGap) {
										strm.addNext(b, -1L);
									}
								}
								strm.toString();
								ArrayList<scala.Tuple2<String, Integer>> ret = new ArrayList<>();
								for(String s:strm.myFinalStrands.keySet())
								{
									ret.add(new scala.Tuple2<String, Integer>(s, strm.myFinalStrands.get(s)));
								}
								return ret;//strm.myFinalStrands;
							}
						}).reduceByKey(new Function2<Integer, Integer, Integer>(){
							@Override
							public Integer call(Integer a, Integer b)
							{
								return a+b;
							}
							});
				int top_10 = 100;
				Iterator<scala.Tuple2<String, Integer>> iterS = strMaints.toLocalIterator();
				while (iterS.hasNext())
				{
					scala.Tuple2<String, Integer> s = iterS.next();
					HashMap<String, Integer> hmap = new HashMap<>();
					hmap.put("oFreq", s._2);
					myPopularStrands.put(s._1, hmap);
					System.out.println(s._1+" "+s._2);//+" here???");
				//	top_10 --;
				//	if(top_10 < 0)break;
				}
				//strMaints.clearAll();
				strMaints = null;
			}//PRAS: remove these
			if(args[2].equals("data_manager"))
			{
				System.out.println("starting data processing......................................");
				StrandDataBased dataKeeper = new StrandDataBased(myPopularStrands, trackedMemoStrands);
				StrandDataBased dataSpew = (StrandDataBased)bbFlows.treeAggregate(dataKeeper, new Function2<StrandDataBased, BasicBlock, StrandDataBased>()
					{
						@Override
						public StrandDataBased call(StrandDataBased l, BasicBlock b)
						{
							if (b.isGap) {
								return l;
							}
							l.addNext(b);
							b.emptyIntermediates(trackedMemoStrands);
							return l;
						}}, 
						new Function2<StrandDataBased, StrandDataBased, StrandDataBased>(){
							@Override
							public StrandDataBased call(StrandDataBased a, StrandDataBased b)
							{
							//	b.toString();
								b.fixUpTopStrandsForGood();
								//for (String s : b.topStrands.keySet())
								//{
								//	System.out.println(s + ":");
								//	System.out.println(b.topStrands.get(s));
								//}
								b.topStrands.clear();
								a.addAllChains(b);
								b.clearAll();
								return a;
							}
						});
				System.out.println("***********************");
				//System.out.println(dataSpew.dataSoFar);//myPopularStrands);
				//System.out.println(dataSpew.topStrands);
				//TRIM mypopularstrands...
				dataSpew.fixUpTopStrandsForGood();
				myPopularStrands.clear();
				System.out.println("***********************");
				dataSpew.clearAll();
				dataKeeper.clearAll();
				dataSpew = null;
				dataKeeper = null;
				//myPopularStrands = null;
			}
		}
	}
}
