package main.java;
import java.util.zip.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;
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
public class TopImportanceGetter 
{
	public static String detectInfo(String opcde)
	{
		String ret = "";
		long opcode = Long.parseLong(opcde, 16);
		if(opcde.length() == 8)
		{
			Ins32BitTypes insType = Ins32BitTypes.getMyType(opcode);
			//ret = insType.getOperands(opcode);
			ret = insType.fillOperands(opcode).toString();
			System.out.print(insType.toString()+" ");
		}
		else
		{
			//System.out.println(opcde);
			InsThumbTypes insType = InsThumbTypes.getMyType(opcode);
			if(insType != null)
			{
				ret = insType.fillOperands(opcode).toString();
				//ret = insType.getOperands(opcode);
			}
			System.out.print(insType.toString()+" ");
		}
		return ret;
	}
	public static void main(String args[])throws Exception
	{
		if(args == null)
		{
			args = new String[]{"first_app.txt", "1"};
		}
		HashSet<Long> correctedBasicBlockFlows = new HashSet<>();
		HashMap<Long, BasicBlock> basicBlocksFormed = new HashMap<>();
		TreeMap<Long, BasicBlock> programFlow = new TreeMap<>();
		TreeMap<Long, String> addressSequence = new TreeMap<Long, String>();
		String line = "";
		int found_bb = 0;
		TreeMap<String, ArrayList<Integer> > aggregateIns = new TreeMap<>();
		BasicBlock.manageMysql(args[1]);
		Connection con = BasicBlock.con;
		if(con == null)
		{
			System.out.println("cant connect to mysql");
			System.exit(0);
		}
		String app_name = "occ"+args[0];
		if(app_name.length() > 15)
		{
			app_name = app_name.substring(0,14);
		}
		int total_instructions = 1;
		Statement stmt = con.createStatement();
		Statement stmt1 = con.createStatement();
		ResultSet rs = stmt.executeQuery("select sum(num_ins) from bb_datakeeper where app='"+app_name+"'"); 
		if(rs.next())
		{
			total_instructions = rs.getInt(1);
		}
		rs = stmt.executeQuery("select * from (select distinct(pc_key),(count(*)*avg(num_ins)/"+total_instructions+") as perc, avg(num_ins) from bb_datakeeper where app='"+app_name+"' group by pc_key) as first where first.perc >= 0.01  order by first.perc desc");
		System.out.println("select * from (select distinct(pc_key),(count(*)*avg(num_ins)/"+total_instructions+") as perc, avg(num_ins) from bb_datakeeper where app='"+app_name+"' group by pc_key) as first where first.perc >= 0.01  order by first.perc desc");
		//rs = stmt.executeQuery("select * from (select distinct(pc_key),(count(*)*avg(num_ins)/"+total_instructions+") as perc, avg(num_ins) from bb_datakeeper where app='"+app_name+"' group by pc_key) as first where first.perc >= 0.01  order by first.perc desc");
		double cdf = 0.0;
		TreeMap<String, String> pcSearchKeys = new TreeMap<>();
		while(rs.next())
		{
			cdf += rs.getDouble(2);
			System.out.println(rs.getString(1)+" "+cdf+" "+rs.getString(3));
			pcSearchKeys.put(rs.getString(1), rs.getString(2));
			BasicBlock bb_iter = null;
			String ins[] = new String[rs.getInt(3)];
			ResultSet rs1 = stmt1.executeQuery("select instruction from basicblock_store where pc_key = '"+rs.getString(1)+"'");
			while(rs1.next())
			{
				String line_o = rs1.getString(1);
				System.out.println(line_o);
				String []sp = line_o.split(":");
				ins[Integer.parseInt(sp[0])] = sp[1];
			}
			bb_iter = new BasicBlock(rs.getString(1), 0, ins[0]);
			for(int i = 1; i < ins.length; i++)
			{
				System.out.println(ins[i]);
				bb_iter.instructions.add(ins[i]);
				bb_iter.insCount++;
				bb_iter.myIdStr = "("+Long.toHexString(bb_iter.startingPC)+","+bb_iter.insCount+","+0+")";
			}
			if(ins[0] == null)
			{
				continue;
			}
			bb_iter.fillInsAndOuts();
			System.out.println(bb_iter.myIdStr+" \nin:");
			System.out.println(bb_iter.myInFields);
			System.out.println("out:");
			System.out.println(bb_iter.myOutFields);
			System.out.println("instructions:"+bb_iter.myInsType);
			for(String s:bb_iter.instructions)
			{
				System.out.println(s);
			}
			System.out.println(bb_iter.myStrands);
			//System.out.println(bb_iter.realRoots);
			//System.out.println(bb_iter.realLeaves);
		}
		
		System.exit(0);
		BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));
		long linesRead = 0;
		boolean inside_bb = false;
		BasicBlock bb_current = null;
		BasicBlock bb_prev = null;
		//PrintWriter pw_server_mysql = null;
		//Socket ss = new Socket("130.203.59.194", 0xdb1);
		//pw_server_mysql = new PrintWriter(ss.getOutputStream());
		//pw_server_mysql = new PrintWriter(args[0]+"_sqldump.sql");//new PrintWriter(ss.getOutputStream());
		//new ListenThread(new BufferedReader(new InputStreamReader(ss.getInputStream()))).start();
		boolean ignoreMe = true;
		int printedCount = 0;
		while(((line = br.readLine())!=null))
		{
			linesRead++;
			if(linesRead % 1000 == 0)
			{
				//System.out.println(linesRead);
			}
			//if(linesRead < 66) continue;
			if(line.trim().length() == 0) continue;
			//System.out.println(line);
			if(line.startsWith("before entering:"))
			{
				inside_bb = true;
				//int cut = line.lastIndexOf("fp_status:")+11;
				//line = line.substring(cut);
				//beforeEnteringParting = true;
				continue;
			}
			if(!inside_bb)
			{
				String split[] = line.split(" ");
				if(split.length > 3)
				{
					boolean isPid = false;
					try
					{
						Integer.parseInt(split[0]);
						isPid = true;
					}
					catch(Exception e)
					{
						isPid = false;
					}
					if(isPid)
					{
						if(split[1].length() == 8 && (split[2].length() == 8 || split[2].length() == 4))
						{
							inside_bb = true;
						}
					}
					if(!inside_bb)
					{
						inside_bb = (line.indexOf(", Lookup: ")!=-1);
					}
				}
			}
			if(inside_bb)
			{
				//System.out.println(line);
				if(line.indexOf("Lookup:")==-1)
				{
					///////////////////////SANITY CHECK
					if(line.startsWith("@@"))
					{
						line = "***end***";
					}
					String temp_splitting[] = line.split(" ");
					if(temp_splitting.length > 2)
					{
						if(temp_splitting[1].length() != 8)
						{
							line = "***end***";
						}
					}	
					if(line.equals("***end***"))
					{
						inside_bb = false;
						if(!ignoreMe)
						{
//							System.out.println("inside this:");
//							System.exit(0);
							bb_current.fillInsAndOuts();
							System.out.println(bb_current.myIdStr+" in out size = "+bb_current.myInFields.size()+" "+bb_current.myOutFields.size()+" \nin:");
							System.out.println(bb_current.myInFields);
							System.out.println("out:");
							System.out.println(bb_current.myOutFields);
							System.out.println("out to in map:"+bb_current.my_def_uses+" max: "+bb_current.max_length_in_out_flow);
							System.out.println(bb_current.realRoots);
							System.out.println(bb_current.realLeaves);
							System.out.println("instructions:");
							for(String s:bb_current.instructions)
							{
								System.out.println(s);
							}
							for(InsTypeInterface ss:bb_current.myInsType)
							{
								String sss = ss.toString();
								ArrayList<Integer> ins = aggregateIns.get(sss);
								if(ins == null)
								{
									ins = new ArrayList<>();
								}
								while(ins.size() <= found_bb)
								{
									ins.add(0);
								}
								Integer now = ins.get(found_bb);
								now = now + 1;
								ins.set(found_bb, now);
								aggregateIns.put(sss, ins);
							}
							//System.out.println(bb_current.myInsType);
							if(0 == pcSearchKeys.size())
							{
								for(String s:aggregateIns.keySet())
								{
									System.out.print(s+" ");//+aggregateIns.get(s));
									for(Integer in_t:aggregateIns.get(s))
									{
										System.out.print(in_t+" ");
									}
									System.out.println();
								}
								System.out.println("found all!");
								System.exit(0);
							}
						}
						ignoreMe = true;
						//bb_current.fillAllInputs(memoryMap, pcMap);
						//bb_current.manageMemory(memoryMap);
						bb_prev = bb_current;
						bb_current = null;
						continue;
					}
					try
					{
						String temp_s[] = line.split(" ");
						String pc_string = temp_s[1];
						if(pcSearchKeys.get(pc_string)!=null)
						{
							ignoreMe = false;
							pcSearchKeys.remove(pc_string);
							found_bb++;
						}
						long pc = Long.parseLong(pc_string, 16);
						addressSequence.put(pc, linesRead+":"+line);
						if(bb_current == null)
						{
							bb_current = new BasicBlock(pc, Integer.parseInt(temp_s[0]), line.substring(4));
							if(!ignoreMe)
							{
								programFlow.put(linesRead, bb_current);//new BasicBlock("gap", pc, linesRead+":"+line));
								basicBlocksFormed.put(pc, bb_current);
							}
						}
						else
						{
							if(!ignoreMe)
							{
								bb_current.instructions.add(line.substring(4));
								bb_current.insCount++;
								bb_current.myIdStr = "("+Long.toHexString(bb_current.startingPC)+","+bb_current.insCount+","+temp_s[0]+")";
							}
						}
						continue;
					}
					catch(Exception e)
					{
						System.out.println(linesRead+"th line: "+line);
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
			else
			{
				if(!line.startsWith("@@") && line.indexOf("inserting")!=-1)
				{
					System.out.println(line);
				}
			}
		}
		br.close();
	}
}
