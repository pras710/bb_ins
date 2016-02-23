package main.java;
import java.util.*;
import java.sql.*;
import java.io.*;
public class AppText
{
	public static void mergeGapsToBBs(TreeMap<Long, BasicBlock> programFlow, long linesRead)
	{
		boolean changed = false;
		long broken_place = 1;
		do
		{
			changed = false;
			for(long i = broken_place; i <= linesRead; i++)
			{
				BasicBlock bb = programFlow.get(i);
				if(bb != null && bb.isGap)
				{
					broken_place = i;
					bb.makeNoGaps(programFlow, i);
					changed = true;
					break;
				}
			}
		}while(changed);
	}
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
		while((line = br.readLine())!=null)
		{
			linesRead++;
			if(line.trim().length() == 0) continue;
			if(!line.startsWith("Lookup:"))
			{
				try
				{
					String temp_s[] = line.split(" ");
					String pc_string = temp_s[0];
					long pc = Long.parseLong(pc_string, 16);
					addressSequence.put(pc, linesRead+":"+line);
					programFlow.put(linesRead, new BasicBlock("gap", pc, linesRead+":"+line));
					continue;
				}
				catch(Exception e)
				{
					System.out.println(line);
					e.printStackTrace();
					System.exit(0);
				}
			}

			String temp_s[] = line.split(" ");
			String pc_string = temp_s[1];
			long pc = Long.parseLong(pc_string.trim(), 16);
			int bytes = Integer.parseInt(temp_s[2].trim());
			long insCount = 0;
			BasicBlock b = null;
			long lineKey = Long.parseLong(addressSequence.get(pc).split(":")[0]);
			if(basicBlocksFormed.get(pc) == null)
			{
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
			programFlow.put(linesRead, b);
		}
		br.close();
		mergeGapsToBBs(programFlow, linesRead);
		long totalInstructions = 0;
		for(Long pc:basicBlocksFormed.keySet())
		{
			BasicBlock bbTemp = basicBlocksFormed.get(pc);
			totalInstructions += bbTemp.insCount * bbTemp.occurence;
		}
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
		System.out.println("TotalInstructions: "+totalInstructions+" TotalBasicBlocks: "+basicBlocksFormed.size());
		
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
			double perc = 1;//0.01;
			//System.out.println(" instruction to "+(perc*100)+"% ("+distincts.size()+" distinct basic blocks chain length: "+limit+") : ");
			int one_percent = (int)(distincts.size()*perc);
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
					long pc = Long.parseLong(ent.getKey().substring(ent.getKey().indexOf("(")+1,ent.getKey().indexOf(",")), 16);
					try
					{
						BasicBlock bb = basicBlocksFormed.get(pc);
						if(bb == null)continue;
						System.out.println(serialNumber+" "+nowPerc+" percent "+ ent.getKey()+" "+ bb.checkLibBlock(stmt));
					}
					catch(Exception e)
					{
						System.out.println(Long.toHexString(pc));
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

			/*
				//NavigableSet<Long> myhighers = addressSequence.tailSet(pc, true);
				//if(myhighers == null)
				//{
				//	System.out.println("decoding went wrong!"+pc);
				//	System.exit(0);
				//}
				//for(Long l:myhighers)
				//{
				//	if(l < bytes+pc)insCount++;
				//	else break;
				//}
				try
				{
					line = (line.substring(8, 17).trim());
					//System.out.println(line);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.out.println(line);
					System.exit(0);
				}
				lineCount++;

				queue.add(pc);
				queueInsCount.add(insCount);
				if(lineCount == i)
				{
					Long temp = distincts.get(queue.toString());
					if(temp == null)
					{
						temp = insCount;
						totalCount+=insCount;
					}
					//System.out.println(queue.toString());
					distincts.put(queue.toString(), runningSum+temp);
					totalCount += insCount;
					runningSum += insCount;
					lineCount -= 1;
					queue.remove();
					long lTemp = queueInsCount.remove();
					runningSum -= lTemp;
				}
			}
			br.close();
			*/
	}
}
