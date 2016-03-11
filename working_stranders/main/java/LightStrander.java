package main.java;
import org.apache.spark.*;
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
			args = new String[]{"google_random", "1", ""};
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
		JavaRDD<String> lines = sc.textFile(path+args[0]).cache();//+"_bbs"
		//System.out.println("bbFlows is geting :"+lines.toArray().size());//size());
		JavaRDD<BasicBlock> bbFlows = lines.map(new Function<String, BasicBlock>(){
					@Override
					public BasicBlock call(String bbString)
					{
						BasicBlock bb = null;
					//	if(bbString.indexOf("Lookup") == -1)
					//	{
					//		System.out.println(bbString);
					//	}
						if(bbString.indexOf("null")!=-1 && bbString.indexOf("Lokup") != -1)
						{
							return new BasicBlock("gap", -1, bbString);
						}
						try
						{
							if(bbString.indexOf("Lookup") != -1)
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
							else
							{
								//System.out.println("good?");
								StringTokenizer strTok = new StringTokenizer(bbString, "|^|");
								String lineNumber = "";//strTok.nextToken();
								String line = strTok.nextToken();
								String temp_s[] = line.split(" ");
								temp_s[0] = temp_s[0].replaceAll(",","").trim();
								try
								{
									String pc_string = temp_s[1];
									long pc = Long.parseLong(pc_string, 16);
									bb = new BasicBlock(pc, Integer.parseInt(temp_s[0]), line.substring(4));
									while(strTok.hasMoreTokens())
									{
										line = strTok.nextToken();
										if(line.indexOf(" ")==-1)
										{
											lineNumber = line;
											break;
										}
										if(line.indexOf("*end*")!=-1)break;
										try
										{
											bb.instructions.add(line.substring(4));
										}
										catch(Exception e)
										{
											e.printStackTrace();
											System.out.println(line);
											bb.fillInsAndOuts();
											bb.printStrands();
											throw e;
										}
										bb.insCount++;
										bb.myIdStr = "("+Long.toHexString(bb.startingPC)+","+bb.insCount+")";
									}
								}
								catch(Exception e)
								{
									System.out.println(line);
									e.printStackTrace();
									throw e;
								}
								try
								{
									bb.fillInsAndOuts();
									//bb.printStrands();
								}
								catch(Exception e)
								{
									e.printStackTrace();
									throw e;
								}
								return bb;
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
							throw e;
						}
						//return bb;
					}
				});
//		final JavaPairRDD<Long, BasicBlock> individualBBs = bbFlows.mapToPair(new PairFunction<BasicBlock, Long, BasicBlock>(){
//					@Override
//					public scala.Tuple2<Long, BasicBlock> call(BasicBlock b)
//					{
//						if(b.isGap)
//						{
//							return new scala.Tuple2<Long, BasicBlock>(0xbadbadl, b);
//						}
//						return new scala.Tuple2<Long, BasicBlock>(b.startingPC, b);
//					}
//				});
//		final Map<Long, BasicBlock> mapPCWithBBs = individualBBs.reduceByKeyLocally(new Function2<BasicBlock, BasicBlock, BasicBlock>(){
//					@Override
//					public BasicBlock call(BasicBlock one, BasicBlock two)
//					{
//						return one;
//					}
//				});

		Long numIns = (Long)bbFlows.aggregate(0L, new Function2<Long, BasicBlock, Long>(){
					@Override
					public Long call(Long l, BasicBlock b)
					{
						if(b.isGap)
						{
							System.out.println("gap found?: "+b.startingPC);
							//return (Long)(individualBBs.lookup(b.startingPC).get(0).insCount+(Long)l);
							return 0L;//(Long)(mapPCWithBBs.get(b.startingPC).insCount+(Long)l);
						}
						return l+1;
						//return (Long)(b.insCount+(Long)l);
					}
				}, new Function2<Long, Long, Long>(){
					@Override
					public Long call(Long a, Long b)
					{
						return ((Long)a+(Long)b);
					}
				}/*, ClassTag$.MODULE$.apply(Long.class)*/);

//		JavaRDD<BasicBlock> coalesced = bbFlows.coalesce(numIns/10000);
//		JavaRDD<StrandMaintainer> strands = coalesced.mapPartitionsWithIndex(new Function2<Integer, Iterator<BasicBlock>, Iterator<StrandMaintainer>>(){
//				public Iterator<StrandMaintainer> call(Integer index, Iterator<BasicBlock> bl)
//				{
//					ArrayList<StrandMaintainer> ret = new ArrayList<>();
//					ret.add(new StrandMaintainer(-1));
//					return ret.iterator();
//				}
//				});
		StrandMaintainer strm = new StrandMaintainer(-1);
		StrandMaintainer chains = (StrandMaintainer)bbFlows.treeAggregate(strm, new Function2<StrandMaintainer, BasicBlock, StrandMaintainer>(){
					@Override
					public StrandMaintainer call(StrandMaintainer l, BasicBlock b)
					{
						if(b.isGap)
						{
							System.out.println("gap found?: "+b.startingPC);
							//return (Long)(individualBBs.lookup(b.startingPC).get(0).insCount+(Long)l);
							return l;//(Long)(mapPCWithBBs.get(b.startingPC).insCount+(Long)l);
						}
						//System.out.println("add next");
						l.addNext(b, -1);
						return l;
					}
				}, new Function2<StrandMaintainer, StrandMaintainer, StrandMaintainer>(){
					@Override
					public StrandMaintainer call(StrandMaintainer a, StrandMaintainer b)
					{
						System.out.println("in fnt2");
						String flag = b.toString();
						if(flag.equals("false"))
						{
							System.out.println(b);
						}
						a.addAllChains(b);
						return a;
					}
				}/*, ClassTag$.MODULE$.apply(Long.class)*/);
		System.out.println(numIns+" "+chains.toString()+" "+strm.toString());
				/*.reduce(
					new Function2<BasicBlock, BasicBlock, BasicBlock>()
					{
						public BasicBlock call(BasicBlock one, BasicBlock two)
						{
							
						}
					}
					);*/
		
//		JavaRDD<StrandMaintainer> strands = bbFlows.mapPartitions(new FlatMapFunction<java.util.Iterator<BasicBlock>, StrandMaintainer>(){
//				@Override
//				public StrandMaintainer call(java.util.Iterator<BasicBlock> iter)
//				{
//					int numBBs = 0;
//					StrandMaintainer strand = new StrandMaintainer(1);
//					do
//					{
//						BasicBlock b = iter.next();
//						strand.addNext(b);
//						numBBs++;
//					}while(iter.hasNext());
//					System.out.println(numBBs);
//					return strand;
//				}
//				});
		System.out.println("***********************");
		//System.out.println(bbFlows.toArray().size());
		System.out.println("***********************");
//		JavaRDD<BasicBlock> bbFlowsNoLookup = bbFlows.map(new Function<BasicBlock, BasicBlock>()
//				{
//					public BasicBlock call(BasicBlock b)
//					{
//						return b;
//					}
//				});
		//for(BasicBlock bb:bbFlows)
		//{
		//	bb.printStrands();
		//}
		//bbFlows.partition
		//bbFlows.saveAsTextFile("file:/i3c/hpcl/huz123/spark/java_example_pras/output_file");
		//bbFlows.collect().foreach(println);
		//StrandMaintainer strand = new StrandMaintainer();
	}
}
