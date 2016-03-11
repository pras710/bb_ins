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
		HashSet<Long> correctedBasicBlockFlows = new HashSet();
		HashMap<Long, BasicBlock> basicBlocksFormed = new HashMap();
		TreeMap<Long, Map.Entry<BasicBlock, Long>> programFlow = new TreeMap();
		TreeMap<Long, String> addressSequence = new TreeMap();
		int upto = Integer.parseInt(args[1]);
		toCheckString = args[2];
		String line = "";

		SparkConf conf = new SparkConf().setAppName("LightStrander Hero DA!");
		JavaSparkContext sc = new JavaSparkContext(conf);
		System.out.println("starting the file read.. with " + sc.defaultMinPartitions() + " partitions");
		JavaRDD<String> lines_bbs = sc.textFile(path + args[0], 100).cache();

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
						bb.emptyIntermediates();
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

		JavaRDD<String> strMaints = bbFlows.mapPartitions(new FlatMapFunction<Iterator<BasicBlock>, String>()
				{
					@Override
					public Iterable<String> call(Iterator<BasicBlock> biter)
					{
						StrandMaintainer strm = new StrandMaintainer(-1L);
						while (biter.hasNext())
						{
							BasicBlock b = (BasicBlock)biter.next();
							if (!b.isGap) {
								strm.addNext(b, -1L);
							}
						}
						return strm.myFinalStrands;
					}
				});
		TreeMap<String, HashMap<String, Integer>> myPopularStrands = new TreeMap();
		Iterator<String> iterS = strMaints.toLocalIterator();
		while (iterS.hasNext())
		{
			String s = (String)iterS.next();
			myPopularStrands.put(s, new HashMap());
		}
		strMaints = null;

		StrandDataBased dataKeeper = new StrandDataBased(myPopularStrands);
		StrandDataBased dataSpew = (StrandDataBased)bbFlows.treeAggregate(dataKeeper, new Function2<StrandDataBased, BasicBlock, StrandDataBased>()
			{
				@Override
				public StrandDataBased call(StrandDataBased l, BasicBlock b)
				{
					if (b.isGap) {
						return l;
					}
					l.addNext(b);
					b.emptyIntermediates();
					return l;
				}}, 
				new Function2<StrandDataBased, StrandDataBased, StrandDataBased>(){
					@Override
					public StrandDataBased call(StrandDataBased a, StrandDataBased b)
					{
						for (String s : b.topStrands.keySet())
						{
							System.out.println(s + ":");
							System.out.println(b.topStrands.get(s));
						}
						b.topStrands.clear();
						a.addAllChains(b);
						b.clearAll();
						return a;
					}
				});
		System.out.println("***********************");
		System.out.println("***********************");
	}
}
