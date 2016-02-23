package main.java;
import java.io.*;
import java.sql.*;
import java.util.*;
public class TakeBBOut
{
	public static void main1(String args[])throws Exception
	{
		if(args == null)
		{
			args = new String[]{"first_app.txt", "1"};
		}
		HashSet<Long> correctedBasicBlockFlows = new HashSet<>();
		HashMap<Long, BasicBlock> basicBlocksFormed = new HashMap<>();
		TreeMap<Long, BasicBlock> programFlow = new TreeMap<>();
		TreeMap<Long, String> addressSequence = new TreeMap<Long, String>();
		int upto = Integer.parseInt(args[1]);
		String line = "";
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		long linesRead = 0;
		boolean inside_bb = false;
		BasicBlock bb_current = null;
		while((line = br.readLine())!=null)
		{
			linesRead++;
			if(line.trim().length() == 0) continue;
			if(line.startsWith("before entering:"))
			{
				inside_bb = true;
				continue;
			}
			if(inside_bb)
			{
				if(line.indexOf("Lookup:")==-1)
				{
					if(line.equals("***end***"))
					{
						inside_bb = false;
						bb_current = null;
						continue;
					}
					try
					{
						String temp_s[] = line.split(" ");
						String pc_string = temp_s[1];
						long pc = Long.parseLong(pc_string, 16);
						addressSequence.put(pc, linesRead+":"+line);
						if(bb_current == null)
						{
							bb_current = new BasicBlock(pc, Integer.parseInt(temp_s[0]), line.substring(4));
							programFlow.put(linesRead, bb_current);//new BasicBlock("gap", pc, linesRead+":"+line));
							basicBlocksFormed.put(pc, bb_current);
						}
						else
						{
							bb_current.instructions.add(line.substring(4));
							bb_current.insCount++;
							bb_current.myIdStr = "("+Long.toHexString(bb_current.startingPC)+","+bb_current.insCount+","+temp_s[0]+")";
						}
						continue;
					}
					catch(Exception e)
					{
						System.out.println(line);
						e.printStackTrace();
						System.exit(0);
					}
				}
				else if(line.indexOf("Lookup:") != -1)
				{
					inside_bb = false;
					String temp_s[] = line.split(" ");
					String pc_string = "pc";
					BasicBlock b = null;
					try
					{
						pc_string = temp_s[2];
						long pc = Long.parseLong(pc_string.trim(), 16);
						int bytes = Integer.parseInt(temp_s[3].trim());
						long insCount = 0;
						long lineKey = Long.parseLong(addressSequence.get(pc).split(":")[0]);
						if(basicBlocksFormed.get(pc) == null)
						{
							System.out.println("this case should not exist!");
							System.exit(0);
							b = new BasicBlock(addressSequence, pc, bytes);
							basicBlocksFormed.put(pc, b);
							b.correctFlow(programFlow, lineKey);
							correctedBasicBlockFlows.add(pc);
						}
						else
						{
							b = basicBlocksFormed.get(pc);//programFlow.get(lineKey);
							if(b == null)
							{
								System.out.println(linesRead+": lineKey not working: "+Long.toHexString(pc)+" linekey = "+lineKey+" "+line+" "+addressSequence.get(pc));
								System.exit(0);
							}
							b.incrementOccurence();
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
						System.out.println(line);
						System.out.println(pc_string);
						System.exit(0);
					}
					programFlow.put(linesRead, b);
				}
				else if(line.indexOf("ld")!=-1)
				{

				}
			}
		}
		br.close();
		//AppText.mergeGapsToBBs(programFlow, linesRead);
		long totalInstructions = 0;
		int min_size = Integer.MAX_VALUE, max_size = -1, avg_size = 0; 
		for(Long pc:basicBlocksFormed.keySet())
		{
			BasicBlock bbTemp = basicBlocksFormed.get(pc);
			int temp = bbTemp.insCount;
			if(temp > 300)
			{
				System.out.println(bbTemp.instructions);
			}
			//System.out.println(temp);
			if(temp > max_size)
				max_size = temp;
			if(temp < min_size)
				min_size = temp;
			avg_size += temp;
			totalInstructions += bbTemp.insCount * bbTemp.occurence;
		}
		avg_size = (int)Math.round(avg_size/(double)basicBlocksFormed.size());
		//for(long k = 1; k <= linesRead; k++)
		//{
		//	BasicBlock bb = programFlow.get(k);
		//	if(bb!=null && bb.notPrinted)
		//	{
		//		bb.notPrinted = false;
		////		System.out.println(k+" "+bb.isGap+" "+Long.toHexString(bb.startingPC)+" "+bb.insCount+" "+bb.occurence);
		//		totalInstructions += (bb.insCount*bb.occurence);
		//	//	if(bb.insCount == 13 && bb.occurence == 8314) 
		//	//		System.out.println(bb.instructions);
		//	}
		//}
		System.out.println("TotalInstructions: "+totalInstructions+" TotalBasicBlocks: "+basicBlocksFormed.size()+" [min,max,avg] = ["+min_size+","+max_size+","+avg_size+"]"); 
		
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost/test");
		ResultSet rs = null;
		Statement stmt = con.createStatement();
		for(int limit = 1; limit <= upto; limit++)
		{
			HashMap<String, Long> distincts = new HashMap<>();
			int lineCount = 0, totalCount = 0;
			ArrayDeque<BasicBlock> queue = new ArrayDeque<>();
			long runningSum = 0;
			for(Long lKey:programFlow.keySet())
			{
				BasicBlock blk = programFlow.get(lKey);
				queue.add(blk);
				if(queue.size()%10 == 0)System.out.println("queue.size = "+queue.size());
				runningSum += blk.insCount;
				if(queue.size() == limit)
				{
					String item = queue.toString();
					Long exist = distincts.get(item);
					if(exist == null)
					{
						exist = 0l;
					}
					exist += runningSum;
					distincts.put(item, exist);
					BasicBlock bRemove = queue.remove();
					runningSum -= bRemove.insCount;
				}
			}
			List<Map.Entry<String, Long>> list = new LinkedList<>( distincts.entrySet() );
			Collections.sort( list, new Comparator<Map.Entry<String, Long>>()
					{
					@Override
					public int compare( Map.Entry<String, Long> o1, Map.Entry<String, Long> o2 )
					{
					return -1*((o1.getValue()).compareTo( o2.getValue() ));
					}
					} );
			double perc = 0.01;
			//System.out.println(" instruction to "+(perc*100)+"% ("+distincts.size()+" distinct basic blocks chain length: "+limit+") : ");
			int one_percent = 20;//100;//(int)(distincts.size()*perc);
			if(limit > 1)
			{
				one_percent = 5;
				System.out.print(limit+" ");
			}
			int serialNumber = 1;
			for(Map.Entry<String, Long> ent: list)
			{
				//if(ent.getValue() < one_percent)
				//{
				//	System.out.println("other "+((distincts.size() - serialNumber)*100.0/distincts.size())+" pieces contribute < "+(perc*100)+"% each");
				//	break;
				//}
				double nowPerc = (100.0*ent.getValue()/totalInstructions);
				//if(nowPerc < 1)break;
				if(limit == 1)
				{
					try
					{
						long pc = Long.parseLong(ent.getKey().substring(ent.getKey().indexOf("(")+1,ent.getKey().indexOf(",")), 16);
						BasicBlock bb = basicBlocksFormed.get(pc);
						System.out.println(serialNumber+" "+nowPerc+" percent "+ ent.getKey()+" "+bb.checkLibBlock(stmt));
					}
					catch(Exception e)
					{
						System.out.println(ent.getKey());
						e.printStackTrace();
						System.exit(0);
					}
				}
				else
				{
					System.out.print(nowPerc+" ");
				}
				serialNumber++;
				if(serialNumber >= one_percent)break;
			}
			if(limit > 1)
			{
				System.out.println();
			}
			else
			{
				System.out.println("****************************");
				System.out.print("1 ");
				serialNumber = 1;
				for(Map.Entry<String, Long> ent:list)
				{
					double nowPerc = (100.0*ent.getValue()/totalInstructions);
					System.out.print(nowPerc+" ");
					serialNumber++;
					if(serialNumber >= 5)break;
				}
				System.out.println();
			}
		}
	}
}
