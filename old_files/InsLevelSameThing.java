package main.java;
import java.util.zip.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
public class InsLevelSameThing 
{
	HashMap<String, DataFlowNode> in_nodes = new HashMap<>(),
		out_nodes = new HashMap<>();
	int my_def_uses = 0, max_length_in_out_flow = 0;
	ArrayList<InsTypeInterface> myInsType = new ArrayList<>();
	TreeSet<String> myInFields = new TreeSet<>();
	TreeSet<String> myOutFields = new TreeSet<>();
	TreeMap<InsTypeChain, Integer> myDepChains = new TreeMap<>();
		//myInFields.add("cpsr");
		//myOutFields.add("cpsr");
	public void maintainDataFlow(String ins)
	{
		///ORIGINAL VERSION IS INSIDE BasicBlock.java manageInstructionParams();

		try
		{
			//System.out.print(ins+" => ");
			String opcd = ins.substring(8, 18).replace(" ", "");
			String pc = ""+Long.parseLong(ins.substring(0,8).replace(" ",""),16);
			String hexpc = ins.substring(0,8).replace(" ","");
			String result = "";
			if(opcd.length() < 8)
			{
				//thumb ()
				if(ins.indexOf("(0x")!=-1)
				{
					result = ins.substring(ins.indexOf("(0x")+3).trim();
					result = result.substring(0, result.indexOf(")"));
					//System.out.println(ins+" ==> "+result);
					//System.exit(0);
				}
			}
			if(ins.indexOf(";")!=-1)
			{
				result = ins.substring(ins.indexOf(";")+1).trim();
				//System.out.print("  result = "+result+" => ");
			}
			//System.out.println(ins+" "+pc);
			//System.exit(0);
			long opcode = Long.parseLong(opcd, 16);
			ArrayList<ArrayList<String> > alist = null;
			InsTypeInterface insType = null;
			if(opcd.length() == 8)// && !opcd.startsWith("f"))
			{
				insType = Ins32BitTypes.getMyType(opcode);
				//ret = insType.getOperands(opcode);
				alist = insType.fillOperands(opcode);
				//myInFields.addAll(alist.get(1));
				if(result.length() == 0)
				{
					for(String s:alist.get(0))
					{
						if(s.startsWith("["))
						//if(s.indexOf("shift")!=-1)
						{
							//System.out.println(hexpc+" **********i********* "+s);
							myInFields.add("pc = "+hexpc);
						}
						else
						{
							DataFlowNode d_in = out_nodes.get(s);
							DataFlowNode d_me = new DataFlowNode(s+":"+hexpc);
							if(d_in != null)
							{
								d_me.addParent(d_in, true);
								d_in.addChild(d_me);
								my_def_uses++;
							}
							in_nodes.remove(s);
							in_nodes.put(s,d_me);
							myInFields.add(s);
						}
						//if(s.indexOf("pc")!=-1 ||s.indexOf("r15")!=-1)
						//{
						//	myInFields.add(pc+";"+result+";"+s);
						//}
						//else 
						//{
						//	myInFields.add(s);
						//}
					}
				}
				for(String s:alist.get(1))
				{
					if(s.startsWith("["))
					//if(s.indexOf("shift")!=-1)
					{
						//System.out.println(hexpc+" ***********o********** "+s);
						myOutFields.add("pc = "+hexpc);
					}
					else
					{
						DataFlowNode d_me = new DataFlowNode(s+":"+hexpc);
						for(String in_s:in_nodes.keySet())
						{
							d_me.addParent(in_nodes.get(in_s), false);
							in_nodes.get(in_s).addChild(d_me);
						}
						out_nodes.remove(s);
						out_nodes.put(s, d_me);
						myOutFields.add(s);
					}
					//if(s.indexOf("pc")!=-1 ||s.indexOf("r15")!=-1)
					//{
					//	myOutFields.add(pc+";"+result+";"+s);
					//}
					//else
					//{
					//	myOutFields.add(s);
					//}
					//myOutFields.add(pc+";"+s);
				}
				//in_nodes.clear();
				//myOutFields.addAll(alist.get(0));
				myInsType.add((InsTypeInterface)insType);
			}
			else
			{
				//System.out.println(opcde);
				insType = InsThumbTypes.getMyType(opcode);
				//if(insType != null)
				//{
					//ret = insType.fillOperands(opcode).toString();
					//ret = insType.getOperands(opcode);
				alist = insType.fillOperands(opcode);
				if(result.length() == 0)
				{
					for(String s:alist.get(0))
					{
						if(s.startsWith("["))
						//if(s.indexOf("shift")!=-1)
						{
							//System.out.println(hexpc+" ************i************* "+s);
							myInFields.add("pc = "+hexpc);
						}
						else
						{
							DataFlowNode d_in = out_nodes.get(s);
							DataFlowNode d_me = new DataFlowNode(s+":"+hexpc);
							if(d_in != null)
							{
								d_me.addParent(d_in, true);
								d_in.addChild(d_me);
								my_def_uses++;
							}
							in_nodes.remove(s);
							in_nodes.put(s,d_me);
							myInFields.add(s);
						}
						//if(s.indexOf("pc")!=-1 ||s.indexOf("r15")!=-1)
						//{
						//	myInFields.add(pc+";"+result+";"+s);
						//}
						//else
						//{
						//	myInFields.add(s);
						//}
					}
				}
				for(String s:alist.get(1))
				{
					if(s.startsWith("["))
						//if(s.indexOf("shift")!=-1)
					{
						//System.out.println(hexpc+" *************o*********** "+s);
						myOutFields.add("pc = "+hexpc);
					}
					else
					{
						DataFlowNode d_me = new DataFlowNode(s+":"+hexpc);
						for(String in_s:in_nodes.keySet())
						{
							d_me.addParent(in_nodes.get(in_s), false);
							in_nodes.get(in_s).addChild(d_me);
						}
						out_nodes.remove(s);
						out_nodes.put(s, d_me);
						myOutFields.add(s);
					}
					//if(s.indexOf("pc")!=-1 ||s.indexOf("r15")!=-1)
					//{
					//	myOutFields.add(pc+";"+result+";"+s);
					//}
					//else
					//{
					//	myOutFields.add(s);
					//}
				}
				//myInFields.addAll(alist.get(1));
				//myOutFields.addAll(alist.get(0));
				myInsType.add((InsTypeInterface)insType);
				//myInOutFields.addAll(insType.fillOperands(opcode));
				//myInsType.add(insType);
				//}
			}
			//System.out.println(insType.toString()+" "+alist+" "+opcd);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void root_in_dependence()
	{
		for(DataFlowNode dfn:out_nodes.values())
		{
			dfn.getRoots(new HashSet<DataFlowNode>());
			System.out.println();
		}
	}
	public static ArrayList<String> getSub(String csv)
	{
		ArrayList<String> ret = new ArrayList<>();
		String a = csv.substring(0, csv.indexOf("), ")+1);
		String b = csv.substring(csv.lastIndexOf(",")-9);
		String c = csv.substring(csv.indexOf("), ")+1, csv.lastIndexOf(",")-9-2);
//		System.out.println("**"+a+"**");
//		System.out.println("**"+b+"**");
//		System.out.println("**"+c+"**");
		if(c.length() < 8) c = "";
		else
		{
			c = c.substring(2);
		}
//		System.out.println("**"+c+"**");
		if(c.length() > 8)
		{
			ret.add(a+", "+c+"]");
			ret.add("["+c+", "+b);
		}
		else
		{
			ret.add(a+"]");
			ret.add("["+b);
		}
//		System.out.println(ret);
		return ret;
	}
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
	private static HashMap<String, LinkedBlockingDeque<String>> memoryMap = new HashMap<>();
	static HashMap<String, Integer> pcMap = new HashMap<>();
	public static String getMemoryValue(String key)
	{
		LinkedBlockingDeque<String> lbd = memoryMap.get(key);
		if(lbd == null || lbd.size() == 0)
		{
			Integer times = pcMap.get(key);
			if(times == null)
			{
				times = 0;
			}
			times++;
			pcMap.put(key, times);
			lbd = memoryMap.get(key.substring(0, key.length()-1));
			//if(lbd != null && lbd.size() > 0)
			//{
			//	System.out.println("happens, " +lbd+" key = "+key+" "+key.substring(0, key.length()-1)+" "+lbd.remove());
			//}
			//else
			//{
			//	System.out.println("not happens! "+key+" "+key.substring(0, key.length()-1));
			//}
			return null;
		}
		String ret = lbd.getLast();
		/**
		 *cp15:12
		 *cp15:11
		  cp15:10
		  cp11:0
		  cp11:1
		  cp11:5
		  cp11:3
		  cp11:2
		  cp11:7
		  cp11:6
		  cp11:4
		  cp11:9
		  cp11:8
		  cp15:13
		  cp15:7
		  cp15:6
		  cp15:5
		  cp15:4
		  cp15:3
		  cp15:2
		  cp15:1
		  cp15:0
		  cp15:8
		  cp11:13
		  cp11:12
		  cp11:11
		  cp11:10
		  cp11:15
		  cp11:14
		 * */
		key = key.replaceAll("cp11:", "vfp:r");
		if(!key.startsWith("r") && !key.startsWith("cp15:") && !key.startsWith("vfp:") && !key.equals("cpsr"))
		{
			ret = lbd.remove();
			//System.out.println("returning pc = "+key+" val = "+ret);
		}
		return ret;
	}
	public static void parseBeforeEntering(String line)
	{
		//before entering:r0 = 0 r1 = 974209369 r2 = 64 r3 = 64 r4 = 708384584 r5 = 708744424 r6 = 708389288 r7 = 708744424 r8 = 0 r9 = 1 r10 = 1266380459 r11 = 0 r12 = 68 r13 = 1266379176 r14 = 1246303617 r15 = 1246303616 uncached_cpsr = 16 spsr = 0 b_spsr0 = 0 b_r13_0 = 1266379032 b_r14_0 = 1245746333 b_spsr1 = 2147483696 b_r13_1 = 3547758584 b_r14_1 = 1245746348 b_spsr2 = 2147484051 b_r13_2 = 3224511628 b_r14_2 = 3221387648 b_spsr3 = 147 b_r13_3 = 3224511640 b_r14_3 = 3221387840 b_spsr4 = 536871315 b_r13_4 = 3224511616 b_r14_4 = 3221387744 b_spsr5 = 0 b_r13_5 = 0 b_r14_5 = 0 b_spsr6 = 0 b_r13_6 = 0 b_r14_6 = 0 usr_regs[0] = 0 fiq_regs[0] = 0 usr_regs[1] = 0 fiq_regs[1] = 0 usr_regs[2] = 0 fiq_regs[2] = 0 usr_regs[3] = 0 fiq_regs[3] = 0 usr_regs[4] = 0 fiq_regs[4] = 0 cp15: c2_base0 = 325681177 c2_base1 = 16409 c2_control = 0 c2_mask = 0 c2_base_mask = 4294950912 c5_insn = 23 c5_data = 2071 c13_tls1 = 0 c13_tls2 = 1266384640 c13_tls3 = 0 vfp: r0 = 0.000000 r1 = 0.000000 r2 = 0.000000 r3 = 0.000000 r4 = 0.000000 r5 = 0.000000 r6 = 0.000000 r7 = 0.000000 r8 = 0.000000 r9 = 0.000000 r10 = 0.000000 r11 = 0.000000 r12 = 0.000000 r13 = 0.000000 r14 = 0.000000 r15 = 0.000000 r16 = 0.000000 r17 = 0.000000 r18 = 0.000000 r19 = 0.000000 r20 = 0.000000 r21 = 0.000000 r22 = 0.000000 r23 = 0.000000 r24 = 0.000000 r25 = 0.000000 r26 = 0.000000 r27 = 0.000000 r28 = 0.000000 r29 = 0.000000 r30 = 0.000000 r31 = 0.000000
		line = line.substring("before entering:".length());
		String[] all_values = line.split(" ");
		String prefix = "";
		for(int i = 0; i < all_values.length - 2;)
		{
			if(all_values[i+1].equals("="))
			{
				String key = prefix+all_values[i];
				//System.out.println("putting "+key+" = "+all_values[i+2]);
				putAVersion(key, all_values[i+2]);
				//LinkedBlockingDeque<String> myVersions = memoryMap.get(key);
				//if(myVersions == null)
				//{
				//	myVersions = new LinkedBlockingDeque<String>();
				//}
				//myVersions.push(all_values[i+2]);
				//memoryMap.put(key, myVersions);//all_values[i+2]);
				i+=3;
			}
			else
			{
				prefix = all_values[i];
				i++;
			}
		}
	}
	public static void putAVersion(String key, String val)
	{
		LinkedBlockingDeque<String> myVersions = memoryMap.get(key);
		if(myVersions == null)
		{
			myVersions = new LinkedBlockingDeque<String>();
		}
		myVersions.push(val);
		int allowed = 4;
		if(key.length() == 7)
		{
			allowed = 16;
		}
		if(myVersions.size() > allowed)
		{
			myVersions.remove();
		}
		memoryMap.put(key, myVersions);//all_values[i+2]);
	}
	public static void manageMemoryMap(String line)
	{
		String key = "", value = "";
		try
		{
			if(line.indexOf("activitymanager")!=-1 || line.indexOf("tid chars")!=-1 || line.indexOf("tid_now")!=-1 || line.indexOf("goldfish vmem")!=-1 
				 || line.indexOf("tids tracked")!=-1	|| line.indexOf("tid started")!=-1)
			{
				//ignore
			}
			else if(line.startsWith("@@ "))
			{
				String dat = line.substring(line.indexOf("(")+1);
				//System.out.println(dat+" from "+line);
				dat = dat.substring(0, dat.length() - 1);
				//System.out.println(dat);
				//System.exit(0);
				String spl[] = dat.split("=");
				String spli[] = spl[0].split(",");
				//putAVersion(spli[0], spl[1]);
				for(String spling:spli)
				{
					//memoryMap.put(spling, spl[1]);
					putAVersion(spling, spl[1]);
					//putAVersion(spling.substring(0, spling.length() - 1), spl[1]);
				}
				//pcMap.put(spl[0].split(",")[0], spl[1]);
			}
			else if(line.endsWith("]"))
			{
				System.out.println(line);
				System.exit(0);
				//TODO: get the subscript length and do something
				String dat = line.substring(line.indexOf("(")+1);
				dat.substring(0, dat.indexOf(")"));
				String spl[] = dat.split("=");
				//memoryMap.put(spl[0], spl[1]);
				putAVersion(spl[0], spl[1]);
				//putAVersion(spl[0].substring(0, spl[0].length()-1), spl[1]);
			}
			else if(line.endsWith(")"))
			{
				String dat = line.substring(line.indexOf("(")+1);
				dat.substring(0, dat.length() - 1);
				String spl[] = dat.split("=");
				String spli[] = spl[0].split(",");
				//putAVersion(spli[0], spl[1]);
				for(String spling:spli)
				{
					//memoryMap.put(spling, spl[1]);
					putAVersion(spling, spl[1]);
					//putAVersion(spling.substring(0, spling.length()-1), spl[1]);
				}
				//pcMap.put(spl[0].split(",")[0], spl[1]);
				//memoryMap.put(spl[0].split(",")[1], spl[1]);
				//memoryMap.put(spl[0].split(",")[0], spl[1]);
			}
			else if(line.indexOf(" get ")!=-1 || line.indexOf(" set ")!=-1)
			{
				String[] sp = line.split(" ");
				sp = sp[sp.length - 1].split("=");
				putAVersion(sp[0], sp[1]);
				//memoryMap.put(sp[0], sp[1]);
			}
			else
			{
				System.out.println(line);
				//System.exit(0);
			}
		}
		catch(Exception e)
		{
			System.out.println(line);
			e.printStackTrace();
			System.exit(0);
		}
	}
	public static void main1(String args[])throws Exception
	{

//		getSub("[(400d160c,13), (400d160c,13)]");
//		getSub("[(400d160c,13), (400d160c,13), (400d160c,13)]");
//		getSub("[(400d160c,13), (400d160c,13), (400d160c,13), (400d10c4,21), (400d160c,13), (400d160c,13)]");
//		System.exit(0);
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
		BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));
		long linesRead = 0;
		boolean inside_bb = false;
		BasicBlock bb_current = null;
		BasicBlock bb_prev = null;
		String app_name = "ch_"+args[0];
		if(app_name.length() > 15)
		{
			app_name = app_name.substring(0,14);
		}
		//PrintWriter pw_server_mysql = null;
		//Socket ss = new Socket("130.203.59.194", 0xdb1);
		//pw_server_mysql = new PrintWriter(ss.getOutputStream());
		//pw_server_mysql = new PrintWriter(args[0]+"_sqldump.sql");//new PrintWriter(ss.getOutputStream());
		//new ListenThread(new BufferedReader(new InputStreamReader(ss.getInputStream()))).start();
		while(((line = br.readLine())!=null))
		{
			linesRead++;
			if(linesRead % 100000 == 0)
			{
				System.out.println(linesRead);
			}
			//if(linesRead < 66) continue;
			if(line.trim().length() == 0) continue;
			//System.out.println(line);
			if(line.startsWith("before entering:"))
			{
				inside_bb = true;
				parseBeforeEntering(line);
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
						manageMemoryMap(line);
						line = "***end***";
					}
					String temp_splitting[] = line.split(" ");
					if(temp_splitting.length > 2)
					{
						if(temp_splitting[1].length() != 8)
						{
							manageMemoryMap(line);
							line = "***end***";
						}
					}	
					if(line.equals("***end***"))
					{
						inside_bb = false;
						bb_current.fillInsAndOuts();
						if(bb_prev!=null)
						{
							try
							{
								//bb_prev.fillAllInputs();//memoryMap, pcMap
								//bb_prev.fillAllOutputs();//memoryMap, pcMap
								//bb_prev.formQueryAndSend(/*pw_server_mysql*/app_name);
							}
							catch(Exception e)
							{
								e.printStackTrace();
								System.out.println(linesRead);
								System.exit(0);
							}
						}
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
							bb_current.myIdStr = "("+Long.toHexString(bb_current.startingPC)+","+bb_current.insCount+")";//+","+temp_s[0]+")";
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
						String seq_str = addressSequence.get(pc);
						if(seq_str != null)
						{
							long lineKey = Long.parseLong(seq_str.split(":")[0]);
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

								//bb_prev.fillAllInputs();//memoryMap, pcMap
								//bb_prev.fillAllOutputs();//memoryMap, pcMap
								//bb_prev.formQueryAndSend(app_name);
								//b.fillAllInputs(memoryMap, pcMap);
								bb_prev = b;
								bb_current = null;
								//b.manageMemory(memoryMap);
							}
						}
						else
						{
							System.out.println(pc+" is not found as a sequence!!");
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
						System.out.println(line);
						System.out.println(pc_string);
						System.exit(0);
					}
					if(b == null)
					{
						System.out.println(linesRead+"in b\n");
						//System.exit(0);
					}
					else
					{
						programFlow.put(linesRead, b);
					}
				}
				else if(line.indexOf("ld")!=-1)
				{

				}
			}
			else
			{
				//manageMemoryMap(line);
			}
		}
		br.close();
		//pw_server_mysql.flush();
		//pw_server_mysql.close();
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
		for(Long pc: basicBlocksFormed.keySet())
		{
			BasicBlock bbTemp = basicBlocksFormed.get(pc);
			//bbTemp.fillInsAndOuts();
			//for(String ins:bbTemp.instructions)
			//{
			//	//System.out.println(ins);
			//	String correctOpcode = ins.substring(8,18).replace(" ", "");
			//	String info = detectInfo (correctOpcode);
			//	if(info.equals(""))
			//	{
			//		undecoded.add(ins.substring(9).split(" ")[0]);
			//		ndecoded.add(ins.substring(18).trim().split("\t")[0]);
			//		System.out.println(ins);
			//	}
			//	//System.out.println(correctOpcode+" "+info);
			//	System.out.println(correctOpcode+" "+ins+" "+info);
			//}
		}
	/*	
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost/test");
		ResultSet rs = null;
		Statement stmt = con.createStatement();*/
		HashMap<Integer, HashSet<String> > underWatch = new HashMap<>();
		//for(int limit = 1; limit <= upto; limit++)
		for(int limit = upto; limit > 0; limit--)
		{
			HashSet<String> subsToWatch = new HashSet<>();
			underWatch.put(limit-1, subsToWatch);
			HashMap<String, Long> distincts = new HashMap<>();
			int lineCount = 0, totalCount = 0;
			ArrayDeque<BasicBlock> queue = new ArrayDeque<>();
			long runningSum = 0;
			long thisTotal = 0;
			for(Long lKey:programFlow.keySet())
			{
				BasicBlock blk = programFlow.get(lKey);
				//if(queue.size() > 0)
				//{
				//	if(queue.getLast() == blk)continue;
				//}
				if(blk == null)
				{
					System.out.println(lKey);
				}
				queue.add(blk);
				//if(queue.size()%10 == 0)System.out.println("queue.size = "+queue.size());
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
					thisTotal += runningSum;
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
			double perc = 0.0091;
			System.out.println("under watch:");
			HashSet<String> underS = underWatch.get(limit);
			if(underS != null)
			{
				for(String s:underS)
				{
					System.out.println(s+" "+(distincts.get(s)*100.0/thisTotal));
				}
			}
			System.out.println(" instruction to "+(perc*100)+"% ("+distincts.size()+" distinct basic blocks chain length: "+limit+") : ");
			int one_percent = (int)(perc*thisTotal);//(int)(distincts.size()*perc);
			//if(limit > 1)
			//{
			//	one_percent = 5;
			//	System.out.print(limit+" ");
			//}
			int serialNumber = 1;
			for(Map.Entry<String, Long> ent: list)
			{
				if(ent.getValue() < one_percent && serialNumber > 10)
				{
					System.out.println("other "+((distincts.size() - serialNumber)*100.0/distincts.size())+" pieces contribute < "+(perc*100)+"% each");
					break;
				}
				double nowPerc = (100.0*ent.getValue()/thisTotal);//totalInstructions);
				//if(nowPerc < 1)break;
				//if(limit == 1)
				//{
				//	try
				//	{
				//		long pc = Long.parseLong(ent.getKey().substring(ent.getKey().indexOf("(")+1,ent.getKey().indexOf(",")), 16);
				//		BasicBlock bb = basicBlocksFormed.get(pc);
				//		//System.out.println(serialNumber+" "+nowPerc+" percent "+ ent.getKey()+" "+bb.checkLibBlock(stmt));
				//	}
				//	catch(Exception e)
				//	{
				//		System.out.println(ent.getKey());
				//		e.printStackTrace();
				//		System.exit(0);
				//	}
				//}
				//else
				{
					System.out.print(nowPerc+" "+ent.getKey());
					if(limit>1)
						subsToWatch.addAll(getSub(ent.getKey()));
				}
				serialNumber++;
				//if(serialNumber >= one_percent)break;
			}
			underWatch.put(limit - 1, subsToWatch);
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
					double nowPerc = (100.0*ent.getValue()/thisTotal);//totalInstructions);
					System.out.println(nowPerc+" "+ent.getKey());
					serialNumber++;
					if(serialNumber >= 5)break;
				}
				System.out.println();
			}
		}
	//	for(String s: pcMap.keySet())
	//	{
	//		System.out.println("\""+s+"\":"+pcMap.get(s)+",");
	//	}
	}
}
class ListenThread extends Thread
{
	BufferedReader br = null;
	public void run()
	{
		while(true)
		{
			try
			{
				System.out.println(br.readLine());
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
	}
	public ListenThread(BufferedReader b)
	{
		br = b;
	}
}
