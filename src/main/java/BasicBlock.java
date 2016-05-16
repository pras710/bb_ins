package main.java;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.sql.*;
public class BasicBlock implements Serializable, Comparable<BasicBlock> 
{
	public int compareTo(BasicBlock b)
	{
		return (int)(b.lineNumber - this.lineNumber);
	}
	//static final long serialVersionUID = 1L;
	public void readObject(ObjectInputStream in) throws Exception
	{
		readFromNet = true;
		myIdStr = (String)in.readObject();
		lineNumber = (Long)in.readObject();
	}
	public void writeObject(ObjectOutputStream out) throws Exception
	{
		out.writeObject(myIdStr);
		out.writeObject((Long)lineNumber);
	}

	public String toString()
	{
		return lineNumber+"";
		//return myStrands.toString();
	}
	boolean readFromNet = false;
	ArrayList<String> instructions = new ArrayList<String>();
	ArrayList<String> memoryLines = new ArrayList<String>();
	ArrayList<String> opcode = new ArrayList<String>();
	int my_tid = -1;
	int my_def_uses = 0, max_length_in_out_flow = 0;
	long startingPC;
	int numBytes;
	int occurence = 0;
	int insCount = 0;
	boolean isGap = false;
	boolean notPrinted = true;
	boolean containsCoProcIns = false;
	String myIdStr = "not_yet", my_assoc_lib = "nothing";
	long lineNumber = 0;
	public void updateLineNumber(long line)
	{
		this.lineNumber = line;
	}
	ArrayList<InsTypeInterface> myInsType;
	TreeSet<String> myInFields;
	TreeSet<String> myOutFields;
	ArrayList<String> in_data = new ArrayList<>();
	ArrayList<String> out_data = new ArrayList<>();
	static Statement stmt = null;
	static Connection con = null;
	static int queryCounter = 0;

	HashMap<String, DataFlowNode> in_nodes = new HashMap<>(),
		out_nodes = new HashMap<>();
	HashSet<DataFlowNode> realRoots, realLeaves;
	ArrayList<InsTypeChain> myStrands;
	public  void printStrands()
	{
		for(InsTypeChain ins:myStrands)
		{
			System.out.println(ins);
		}
	}
	public void emptyIntermediates(final TrackedStrands tracked)
	{
		if(opcode != null)opcode.clear();
		if(in_data != null) in_data.clear();
		if(out_data != null)out_data.clear();
		if(in_nodes != null)in_nodes.clear();
		if(out_nodes != null)in_nodes.clear();
		if(realRoots!=null)realRoots.clear();
		if(realLeaves!=null)realLeaves.clear();
		//if(myStrands!=null)myStrands.clear();
		if(myInsType != null)myInsType.clear();
		if(myInFields != null)myInFields.clear();
		if(myOutFields != null)myOutFields.clear();
		ArrayList<InsTypeChain> tempStrands = new ArrayList<>();
		for(InsTypeChain ins:myStrands)
		{
			String temp = ins.myChainDefinition.toString();
			temp = temp.substring(0, temp.length() - 1);
			if(tracked.canGo(temp))
			{
				tempStrands.add(ins);
			}
		}
		myStrands.clear();
		myStrands = tempStrands;
		//if(memoryLines != null)memoryLines.clear();
	}
	public void emptyAllButInstructions()
	{
		if(opcode != null)opcode.clear();
		if(in_data != null) in_data.clear();
		if(out_data != null)out_data.clear();
		if(in_nodes != null)in_nodes.clear();
		if(out_nodes != null)in_nodes.clear();
		if(realRoots!=null)realRoots.clear();
		if(realLeaves!=null)realLeaves.clear();
		if(myStrands!=null)myStrands.clear();
		if(myInsType != null)myInsType.clear();
		if(myInFields != null)myInFields.clear();
		if(myOutFields != null)myOutFields.clear();
		if(memoryLines != null)memoryLines.clear();
	}
	public static void printAllParseStats()
	{

		for(String insT:ht_insFails.keySet())
		{
			if(insT.endsWith("pass"))
			{
				System.out.println(insT +" : pass = "+ht_insFails.get(insT)+" fail = "+ht_insFails.get(insT.substring(0, insT.length()-4)));
			}
		}
//		System.exit(0);
	}
	public void allCategories()
	{
		for(InsTypeInterface ins1:Ins32BitTypes.values())
		{
			System.out.println(ins1);
		}
		for(InsTypeInterface ins1:InsThumbTypes.values())
		{
			System.out.println(ins1);
		}
		for(InsTypeInterface ins1:InsThumb2Type.values())
		{
			System.out.println(ins1);
		}
	}
	public void maintainMyDependenceEnds()
	{
		HashSet<DataFlowNode> tempRoots = new HashSet<DataFlowNode>(),
			tempLeaves = new HashSet<DataFlowNode>();
		realRoots = new HashSet<DataFlowNode>();
		realLeaves = new HashSet<DataFlowNode>();
		myStrands = new ArrayList<>();
		for(DataFlowNode dfn:out_nodes.values())
		{
			dfn.getRoots(tempRoots, realRoots);
		}
		for(DataFlowNode dfn:in_nodes.values())
		{
			dfn.getLeaves(tempLeaves, realLeaves, myStrands, null);
		}
		//if(startingPC == 0x40043dc0)
		//{
	//		System.out.println(myStrands);
	//		System.exit(0);
		//}
	}
	public static void manageMysql(String host) throws Exception
	{
		try
		{
			System.out.println(host);
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://"+host+"/prasanna_db", "pur128", "951814771Cse");
			stmt = con.createStatement();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			try
			{
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection("jdbc:mysql://"+host+"/prasanna_db", "pur128", "");
				stmt = con.createStatement();
			}
			catch(Exception e1)
			{
				System.out.println("jdbc:mysql://"+host+"/prasanna_db");
				e1.printStackTrace();
			}
			throw e;
		}
	}
	public BasicBlock(long pc, int tid, String first_instrn)
	{
		this.startingPC = pc;
		this.my_tid = tid;
		instructions.add(first_instrn);
		insCount = 1;
		occurence = 1;
		myIdStr = "("+Long.toHexString(startingPC)+","+insCount+")";
		myInsType = new ArrayList<>();
		myInFields = new TreeSet<>();
		myOutFields = new TreeSet<>();
		myInFields.add("cpsr");
		myOutFields.add("cpsr");
	}
	public BasicBlock(String gap, long pc, String myLine)
	{
		isGap = true;
		startingPC = pc;
		instructions.add(myLine);
		insCount = 1;
		myIdStr = "("+Long.toHexString(startingPC)+","+insCount+")";
		myInsType = new ArrayList<>();
		myInFields = new TreeSet<>();
		myOutFields = new TreeSet<>();
		myInFields.add("cpsr");
		myOutFields.add("cpsr");
	}
	public BasicBlock(TreeMap<Long, String> lines, long pc, int numbytes)
	{
		startingPC = pc;
		numBytes = numbytes;
		NavigableMap<Long, String> myhighers = lines.tailMap(pc, true);
		if(myhighers == null)
		{
			System.out.println("decoding went wrong!"+pc);
			System.exit(0);
		}
		for(Long l:myhighers.keySet())
		{
			if(l < numbytes+pc)
			{
				instructions.add(myhighers.get(l));
				insCount++;
			}
			else break;
		}
		occurence = 1;
		myIdStr = "("+Long.toHexString(startingPC)+","+insCount+")";
		//myIdStr = "("+startingPC+","+insCount+")";
		myInsType = new ArrayList<>();
		myInFields = new TreeSet<>();
		myOutFields = new TreeSet<>();
		myInFields.add("cpsr");
		myOutFields.add("cpsr");
	}
	public int getSize()
	{
		return instructions.size();
	}
	public boolean equals(BasicBlock other)
	{
		return other.startingPC == this.startingPC && other.numBytes == this.numBytes;
	}
	public void incrementOccurence()
	{
		occurence++;
	}
	public void correctFlow(TreeMap<Long, Map.Entry<BasicBlock, Long>> flow, long key, long tid)
	{
		flow.put(key, new AbstractMap.SimpleEntry<BasicBlock, Long>(this, tid));
		for(int i = 1; i < insCount; i++)
		{
			flow.remove(key+i);
		}
	}
	public void makeNoGaps(TreeMap<Long, BasicBlock> flow, long key)
	{
		this.isGap = false;
		long i = key+1;
		for(; ; i++)
		{
			BasicBlock bb = flow.get(i);
			if(bb != null && bb.isGap)
			{
				instructions.add(bb.instructions.get(0));
				numBytes = (int)(bb.startingPC - this.startingPC);
				insCount++;
			}
			else
			{
				break;
			}
		}
		while(i > key)
		{
			flow.remove(i);
			i--;
		}
		occurence = 1;
		myIdStr = "("+Long.toHexString(startingPC)+","+insCount+")";
		//myIdStr = "("+startingPC+","+insCount+")";
	}
	//public String toString(){
	//	return myIdStr;//+"["+my_assoc_lib+"]";
	//}
	public String getOpcode(String s)
	{
		String  ret = s.substring(8, 13).replaceAll(" ","")+"____";
		//String  ret = s.substring(8, 17).replaceAll(" ","");
		return ret;
	}
/*	public String getOperand(String s)
	{
		//System.out.print(" operand = "+s+" value = ");
		String s_orig = s;
		if(s.indexOf("disregard")!=-1)
		{
			return "";
		}
		if(s.startsWith("pc = "))
		{
			//System.out.println(s+"::"+s.substring(5)+" "+pcMap.get(s.substring(5))+" "+" "+memoryMap.get(s.substring(5))+pcMap);
			String key = s.substring(5);
			return LightStrander.getMemoryValue(key);
			//return memoryMap.get(s.substring(5));
		}
		if(s.indexOf(";")!=-1)
		{
			String pc_split[] = s.split(";");
			s=pc_split[2];
			if(pc_split[1].length() > 0)
			{
				return pc_split[1];
			}
//			LightStrander.putAVersion("r15", pc_split[0]);
		}
		s = s.replaceAll("pc", "r15").replaceAll("sp", "r13").replaceAll("lr","r14").replaceAll("cp10:", "vfp:r");
		//if(s.indexOf("r15")!=-1)
		//{
		//	return "";
		//}
		String dat = "";//LightStrander.getMemoryValue(s);//memoryMap.get(s);
		if(dat == null)
		{
			if(s.startsWith("["))
			{
				String register = s.substring(1, s.indexOf("]"));
				String address = "";
				try
				{
					//LinkedBlockingDeque<String> lbd = memoryMap.get(register);
					//String str = lbd.getLast();
					String str = LightStrander.getMemoryValue(register);
					address = ""+Long.parseLong(str);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.out.println(s+" "+register+" "+LightStrander.getMemoryValue(register));
					System.exit(0);
				}
				while(s.length() > s.indexOf("]")+1)
				{
					s = s.substring(s.indexOf("]")+1);
					if(s.startsWith("+"))
					{
						if(s.indexOf("[r")!=-1)
						{
							String register_2 = s.substring(2, s.indexOf("]"));
							String  address_2 = LightStrander.getMemoryValue(register_2);
							if(address_2 != null)
							{
								System.out.println(s_orig+"reg2:"+register_2+" = "+address_2);
								try
								{
									address = ""+(Long.parseLong(address)+Long.parseLong(address_2));
								}
								catch(Exception e)
								{
									e.printStackTrace();
									doTheKillRoutine(s_orig, register, register_2);
									System.exit(0);
								}
							}
							else
							{
								System.out.println(register_2+" is already consumed!!");
								System.exit(0);
							}
						}
						else if(s.indexOf("[shift")!=-1)
						{
							String temp = s.substring(s.indexOf("[")+1,s.indexOf("]"));
							String codes[] = temp.split("_");
							if(codes.length != 8)
							{
								System.out.println("something with shift code?"+s);
								System.exit(0);
							}
							long data = Long.parseLong(codes[1]);
							if(data != 0)
							{
								int shiftTimes = 0;
								if(codes[3].startsWith("r"))
								{
									System.out.println("deprecated place is exceuting!!\n");
									System.exit(0);
									//shiftTimes = Integer.parseInt(memoryMap.get(codes[3]));
								}
								else
								{
									shiftTimes = Integer.parseInt(codes[3]);
								}
								int code = Integer.parseInt(codes[5]);
								switch(code)
								{
									case 0:
										data <<= shiftTimes;
										data &= 0xff;
										break;
									case 1:
										int sign = (int)(data & 0x80);
										for(int i = 0; i < shiftTimes; i++)
										{
											data >>>= 1;
											data |= sign;
											data &= 0xff;
										}
										data &= 0xff;
										break;
									case 2:
										data >>= shiftTimes;
										data &= 0xff;
										break;
									case 3:
										byte temp_1 = (byte)data;
										for(int i = 0; i < shiftTimes; i++)
										{
											int rightmost = (int)(data & 1);
											data >>= 1;
											data |= (rightmost<<7);
										}
										//temp_1 = Byte.rotateRight(temp_1, shiftTimes);
										//data = temp_1;
								}
								data = data * Integer.parseInt(codes[7]);
								address = ""+(Long.parseLong(address)+data);
							}
						}
						else if(s.indexOf("[")!=-1)
						{
							String immediate = s.substring(2, s.indexOf("]"));
							address = ""+(Long.parseLong(address)+Long.parseLong(immediate));
							//System.out.println("immediate = "+immediate);
						}
						else
						{
							//doTheKillRoutine(register, address, s);
							break;
						}

					}
					else if(s.indexOf(" shift = ")!=-1||s.equals("+4")||s.equals("+1"))
					{
						s = "";
						break;
						//ignore
					}
					else
					{
						doTheKillRoutine(register, address, s);
					}
				}
				try
				{
					//System.out.println("address = "+address);
					address = Long.toHexString(Long.parseLong(address));
					//System.out.println("address = "+address);
					dat = LightStrander.getMemoryValue(address);//memoryMap.get(address);
					if(dat == null)
					{
						//System.out.println(memoryMap);
						System.out.println(s_orig);
						System.exit(0);
						//doTheKillRoutine("","", s);
					}
				}
				catch(Exception e)
				{
					doTheKillRoutine(register, address, s);
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		if(dat == null)// || dat.size() == 0)
		{
			return null;
			//doTheKillRoutine("", "", "");
		}
		//if(s_orig.startsWith("r"))
		//{
		//	String val = dat;//dat.getLast();
		//	return val;
		//}
		return dat;
	}
*/
	int debugCount = 0;
	public void doTheKillRoutine(String register, String address, String s)
	{
		System.out.println("do the kill routine in basic block!");
		System.out.println(register+" "+address+" "+s);
		System.out.println(instructions);
		System.out.println(myInsType+":");
		for(int i = 0; i < instructions.size(); i++)
		{
			String opcd = instructions.get(i).substring(8, 18).replace(" ", "");
			long opcode = Long.parseLong(opcd, 16);
			System.out.println(instructions.get(i)+" "+
					myInsType.get(i).fillOperands(opcode));
		}

		//if(debugCount++ == 100)System.exit(0);
		System.exit(0);
	}
/*	public void fillAllOutputs()//HashMap<String, LinkedBlockingDeque<String>> memoryMap, HashMap<String, String> pcMap
	{
		if(instructions.size()!=myInsType.size())
		{
			fillInsAndOuts();
		}
		fillAllData(myOutFields, out_data);//memoryMap, pcMap, 
	}
	public void fillAllInputs()//HashMap<String, LinkedBlockingDeque<String>> memoryMap, HashMap<String, String> pcMap)
	{
		if(instructions.size()!=myInsType.size())
		{
			fillInsAndOuts();
		}
		//System.out.println(myInFields);
		fillAllData(myInFields, in_data);//memoryMap, pcMap, 
	}
	public void formQueryAndSend(/*PrintWriter pw* /String app_name) throws Exception
	{
		for(int i = 0; i < in_data.size();i++)
		{
			String query = "insert into bb_datakeeper values('"+app_name+"', '"+Long.toHexString(startingPC)+"','"+my_tid+"','"+in_data.get(i)+"','"+out_data.get(i)+"',"+instructions.size()+","+occurence+")";
			queryCounter++;
			//if(queryCounter > 748269)
			{
				if(stmt == null)
				{
					manageMysql("localhost");
				}
				try
				{
					//if(queryCounter > 563661)
					{
						stmt.executeUpdate(query);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.out.println(query);
					System.exit(0);
				}
			}
			if(queryCounter % 1000 == 0)System.out.println("query = "+queryCounter);
			//pw.println(query);
			//pw.flush();
		}
		//int countRows = 0;
		//ResultSet rs = stmt.executeQuery("select count(*) from basicblock_store where app='"+app_name+"' && tid_using = '"+my_tid+"' && pc_key = '"+Long.toHexString(startingPC)+"'");
		//if(rs.next())
		//{
		//	countRows = rs.getInt(1);
		//}
		//if(countRows == 0)
		if(occurence == 1)
		{
			for(int i = 0; i < instructions.size(); i++)
			{
				//String query = "insert into bb_datakeeper values('"+app_name+"', '"+Long.toHexString(startingPC)+"','"+my_tid+"','"+in_data.get(i)+"','"+out_data.get(i)+"',"+instructions.size()+")";
				String query = "insert into basicblock_store values('"+app_name+"', '"+my_tid+"','"+Long.toHexString(startingPC)+"','"+i+":"+instructions.get(i)+"')";
				queryCounter++;
				//if(queryCounter > 748269)
				{
					try
					{
						//if(queryCounter > 563661)
						{
							stmt.executeUpdate(query);
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
						System.out.println(query);
						System.exit(0);
					}
				}
				if(queryCounter % 1000 == 0)System.out.println("query = "+queryCounter);
				//pw.println(query);
				//pw.flush();
			}
		}
		in_data.clear();
		out_data.clear();
	}

	public void fillAllData(TreeSet<String> myFields, ArrayList<String> inKeeper)//HashMap<String, LinkedBlockingDeque<String>> memoryMap, HashMap<String, String> pcMap, 
	{
		int mySuccess = 0;
		StringBuilder str_in = new StringBuilder("");//, str_out = new StringBuilder("");
		for(String s:myFields)
		{
			//int killCount = debugCount;
			String temp = getOperand(s)+",";
			if(temp.equals("null,"))
			{
				//System.out.println(s+" not decoded yet!!!");
				if(s.startsWith("r"))
				{
					doTheKillRoutine(s, s, s);
					System.exit(0);
				}
				temp = ",";
			}
			else
			{
				if(s.startsWith("r"))
				{
					//System.out.println("it works most times?");
				}
				mySuccess++;
			}
			str_in.append(temp);
		}
		//if(str_in.indexOf("null")!=-1)
		//{
		//	System.out.println(str_in+" "+mySuccess+" "+myFields.size()+" "+(mySuccess*100.0/myFields.size()));
		//	////int a = 1/0;
		//}
		//System.out.println(str_in+" "+mySuccess+" "+myFields.size()+" "+(mySuccess*100.0/myFields.size()));
		str_in.append(","+(mySuccess*100.0/myFields.size()));
		inKeeper.add(str_in.toString());
	}

	*/
/*	public void manageMemory(HashMap<String, LinkedBlockingDeque<String>> memoryMap, HashMap<String, String> pcMap)
	{
		if(instructions.size()!=myInsType.size())
		{
			fillInsAndOuts();
		}
		int mySuccess = 0;
		StringBuilder str_in = new StringBuilder(""), str_out = new StringBuilder("");
		for(String s:myInFields)
		{
			int killCount = debugCount;
			System.out.println(getOperand(s));
			if(debugCount > killCount)
			{
				System.out.println("not decoded!!!");
			}
			else
			{
				mySuccess++;
			}
		}
		for(String s:myOutFields)
		{
			int killCount = debugCount;
			System.out.println(getOperand(s));
			if(debugCount > killCount)
			{
				System.out.println("not decoded!!!");
			}
			else
			{
				mySuccess++;
			}
		}
		System.out.println("done all! "+(mySuccess*100.0/(myInFields.size()+myOutFields.size())));
	}
	*/
	public void defUserFormation(ArrayList< ArrayList<String>> alist, String result, String hexpc, String ins, InsTypeInterface insType, int insCount)
	{
		HashMap<String, DataFlowNode> in_temp = new HashMap<>();
		if(result.length() == 0)
		{
			for(String s:alist.get(0))
			{
				if(s.startsWith("["))
				{
					myInFields.add("pc = "+hexpc);
				}
				else
				{
					DataFlowNode d_in = out_nodes.get(s);
					DataFlowNode d_me = new DataFlowNode(s+":"+ins, insType, insCount, alist, hexpc);//replace ins with hexpc
					if(d_in != null)
					{
						d_me.addParent(d_in, true);
						d_in.addChild(d_me);
						if(culpritDetector)
						{
							//System.out.println("adding edge from "+d_in.name+" to "+d_me.name);
						}
						my_def_uses++;
					}
					else
					{
						//in_nodes.remove(s);
						in_nodes.put(s,d_me);
					}
					in_temp.put(s,d_me);
					myInFields.add(s);
				}
			}
		}
		for(String s:alist.get(1))
		{
			if(s.startsWith("["))
			{
				myOutFields.add("pc = "+hexpc);
			}
			else
			{
				DataFlowNode d_me = new DataFlowNode(s+":"+ins, insType, insCount, alist, hexpc);//ins == hexpc
				for(String in_s:in_temp.keySet())
				{
					d_me.addParent(in_temp.get(in_s), false);
					in_temp.get(in_s).addChild(d_me);
					if(culpritDetector)
					{
						//System.out.println("adding edge from "+in_temp.get(in_s).name+" to "+d_me.name);
					}
				}
				out_nodes.remove(s);
				out_nodes.put(s, d_me);
				myOutFields.add(s);
			}
		}
	}
	public void fillInsAndOuts()
	{
		if(myInsType == null)
		{
			myInsType = new ArrayList<>();
		}
		if(myInsType.size() == instructions.size())
		{
			System.out.println("no instructions");
			return;
		}
		//PRAS REMOVE THIS:
		//if(GetSrcDest.linesRead < 18400000)return;
		myInsType = new ArrayList<>();
		int insCount = 0;
		for(String ins:instructions)
		{
			manageInstructionParams(ins, insCount++);
		}
		//PRAS REMOVE THIS COMMENT the maintain my dependence ends is important!!!
		if(!LightStrander.insDecodeDebug)
		{
			maintainMyDependenceEnds();
		}
	//NotSCALABLE	if(myStrands != null)
	//NotSCALABLE	{
	//NotSCALABLE		for(InsTypeChain ins:myStrands)
	//NotSCALABLE		{
	//NotSCALABLE			//ins.lineNumberOccurrence.add(this);
	//NotSCALABLE			//NOT SCALABLE ins.lineNumberOccurrence.add(lineNumber);
	//NotSCALABLE		}
	//NotSCALABLE		//System.out.println(lineNumber);
	//NotSCALABLE	}
		//System.exit(0);
	}
	static int passedIns = 0, failedIns = 0;
	boolean culpritDetector = true;
	String culpritString = "";
	static Hashtable<String, Integer> ht_insFails = new Hashtable<>();
	public void manageInstructionParams(String ins, int insCount)
	{
		ins = ins.trim();
		//culpritDetector = (startingPC == 0x40043dc0);
		//System.out.print(ins+" => ");
		if(LightStrander.insDecodeDebug)
		{
			ins = "ba"+ins; 
			//System.out.println(ins);
		}
		//ins  = "4c25af14 f92c 2a0f  vld1.8	{d2-d3}, [ip] ";
		//System.out.println(ins);
		int startingPos = ins.indexOf(" ");
		String opcd = ins.substring(startingPos, 10+startingPos).trim();
		if(opcd.equals("c854 1c43"))
		{
			System.out.println(ins);
		}
		boolean exitCondition = false;//(opcd.indexOf(" ")!=-1);
		boolean opcdFBeginner = false;//(opcd.indexOf(" ")!=-1);
		opcd = opcd.trim();
		boolean myFlag = true;
		opcdFBeginner = opcd.trim().startsWith("f");
		if(opcd.indexOf(" ")!=-1)
		{
			//return;
			//System.out.println(ins+ "**"+opcd+"**");
			if(myFlag)
			{	
			}
			else
			{
				if(opcd.startsWith("f8"))
				{
					opcd = opcd.substring(2, 7);//opcd.indexOf(" "));
				}
				else //(opcd.startsWith("f3"))
				{
					opcd = opcd.substring(opcd.indexOf(" "));
				}
			}
			//System.out.println(opcd);
		}
		else
		{
			myFlag = false;
			myFlag = opcd.startsWith("f3")||opcd.startsWith("f4");
		}
		opcd = opcd.replace(" ", "");
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
		if(opcd.length() != 4)// && !opcd.startsWith("f"))
		{
			if(myFlag)
			{
				insType = InsThumb2Type.getMyType(opcode);
				//if(insType == InsThumb2Type.CoProcIns2)
				//{
				//	insType = Ins32BitTypes.getMyType(opcode);
				//}
				if(insType == null)
				{
					System.out.println(ins+" has no thumb2: "+opcode);
					System.exit(0);
				}
				alist = insType.fillOperands(opcode);
				boolean test = checkInOutForCredibility(alist, ins, opcd);
				if(test)
				{
					insType = Ins32BitTypes.getMyType(opcode);
					//boolean test = checkInOutForCredibility(alist, ins, opcd);
					//if(test)
					//{
					//	insType = Ins32BitTypes.CoprocDataTransfer;
					//}
				}
				//if(insType == null)
				//{
				//	insType = Ins32BitTypes.getMyType(opcode);
				//}
				//System.out.println(insType);
			}
			else
			{
				insType = Ins32BitTypes.getMyType(opcode);
				//alist = insType.fillOperands(opcode);
				//if(insType == Ins32BitTypes.CoprocDataTransfer)
				//{
				//	boolean test = checkInOutForCredibility(alist, ins, opcd);
				//	int totalOpsFound = alist.get(0).size()+alist.get(1).size();
				//	InsTypeInterface insType2 = InsThumb2Type.CoProcIns2;//getMyType(opcode|0xe0000000);
				//	if(insType2 != null)
				//	{
				//		ArrayList<ArrayList<String> > alist2 = insType2.fillOperands(opcode);
				//		boolean test2 = checkInOutForCredibility(alist2, ins, opcd);
				//		int nowTotalOps = alist2.get(0).size()+alist.get(1).size();
				//		if(nowTotalOps > totalOpsFound || test)
				//			insType = insType2;
				//	}
				//	else
				//	{
				//		System.out.println("ins type is null for thumb2");
				//	}
				//}
			//	if(test)
			//	{
			//		InsTypeInterface insType2 = InsThumb2Type.getMyType(opcode);
			//		if(insType2 != null)
			//		{
			//			insType = insType2;
			//		}
			//	}
			}
			//ret = insType.getOperands(opcode);
			//alist = insType.fillOperands(opcode);
			//myInFields.addAll(alist.get(1));
			if(insType.toString().toLowerCase().indexOf("coproc")!=-1)
			{
				containsCoProcIns = true;
			}
			myInsType.add((InsTypeInterface)insType);
		}
		else
		{
			//System.out.println(opcode);
			insType = InsThumbTypes.getMyType(opcode);
			myInsType.add((InsTypeInterface)insType);
			if(insType.toString().toLowerCase().indexOf("coproc")!=-1)
			{
				containsCoProcIns = true;
			}
		}
		if(insType == null)
		{
			System.out.println(ins+" is null typed");
			System.exit(0);
		}
		//if(insType.toString().toLowerCase().indexOf("misc")!=-1)
		//{
		//	System.out.println(ins);
		//}
		alist = insType.fillOperands(opcode);
		if(culpritDetector)
		{
			//System.out.println("cred before:"+alist);
			culpritString = "";
		}
		exitCondition = checkInOutForCredibility(alist, ins, opcd);
		if(culpritDetector)
		{
			//System.out.println("cred after:"+alist);
		}
		if(exitCondition)
		{
			failedIns++;
			Integer count_temp = ht_insFails.get(insType.toString());
			if(count_temp == null)
			{
				count_temp = 0;
			}
			count_temp = count_temp+1;
			ht_insFails.put(insType.toString(), count_temp);
			if(culpritDetector)
			{
				System.out.println((opcdFBeginner)+" "+opcd+" :: "+insType.toString()+" "+insType.fillOperands(opcode)+" to "+alist+" "+ins+" ||  "+passedIns+" "+failedIns+" "+culpritString);
				//System.out.println(passedIns+" "+failedIns);
				if(failedIns == 20)
				{
					for(String insT:ht_insFails.keySet())
					{
						if(!insT.endsWith("pass"))
						{
							System.out.println(insT +" : pass = "+ht_insFails.get(insT+"pass")+" fail = "+ht_insFails.get(insT));
						}
					}
					System.exit(0);
				}
			}
		}
		else
		{
			defUserFormation(alist, result, hexpc, ins, insType, insCount );
			passedIns++;
			Integer cou = ht_insFails.get(insType.toString()+"pass");
			if(cou == null)
			{
				cou = 0;
			}
			cou = cou+1;
			ht_insFails.put(insType.toString()+"pass", cou);
			//if(insType.toString().equals("DataProcMod2"))
			//if(ins.contains(" v"))
			//{
		//		System.out.println("passing?"+alist+" "+ins);
			//}
			if(opcdFBeginner)
			{
				//	System.out.println("***********************************");
			}
		}
	}
	public boolean checkInOutForCredibility(ArrayList<ArrayList<String> > alist, String ins, String opcd)
	{
		ArrayList< ArrayList<String> > retList = new ArrayList<>();
		ins = ins.replaceAll("cr", "cKt");//some unused character..
		ins = ins.replaceAll("r12","ip")
						.replaceAll("r13","sp")
						.replaceAll("r14","lr")
						.replaceAll("r10","sl")
						.replaceAll("r11","fp")
						.replaceAll("r15","pc");
		ins = ins.replaceAll("cKt", "cr");
		boolean exitCondition = false;
		for(ArrayList<String> al:alist)
		{
			ArrayList<String> retListIter = new ArrayList<>();
			for(String sise:al)
			{
				//String sis[] = sise.split("\\Q+\\E,");
				StringTokenizer strTok = new StringTokenizer(sise, "+,");
				boolean exitK = false;
				if(sise.startsWith("cpsr"))continue;
				if(sise.endsWith(":"))continue;
				if(sise.trim().length() == 0)continue;
				String debug_s = "";
				//for(String s:sis)
				while(strTok.hasMoreTokens())
				{
					String s = strTok.nextToken();
					s = s.replaceAll("cr", "cKt");
					s = s.replaceAll("\\[","")
						.replaceAll("\\]","")
						.replaceAll("r12","ip")
						.replaceAll("r13","sp")
						.replaceAll("r14","lr")
						.replaceAll("r10","sl")
						.replaceAll("r11","fp")
						.replaceAll("r15","pc")
						.replaceAll("cp..:","");
					s = s.replaceAll("cKt", "cr");
					boolean result_now = (ins.indexOf(s) != -1);
					if(s.startsWith("mvf"))
					{
						result_now |= (ins.indexOf(s.replaceAll("mvf", "mvfx")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("mvf", "mvd")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("mvf", "mvdx")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("mvf", "mvax")) != -1);
					}
					if(result_now)
					{
						retListIter.add(s);
					}
					if(!result_now)
					{
						result_now |= (ins.indexOf(s.replaceAll("f", "cr")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("mvf", "cr")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("cr", "mvax")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("cr", "mvfx")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("cr", "f")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("cr", "d")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("cr", "mvd")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("cr", "mvdx")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("cr", "mvax")) != -1);
						s = s.replaceAll("s","d");
						result_now |= (ins.indexOf(s) != -1);
						//result_now |= (ins.indexOf(s.replaceAll("d","r")) != -1);
						result_now |= (ins.indexOf(s.replaceAll("d","r").replaceAll("r12","ip")
						.replaceAll("r13","sp")
						.replaceAll("r14","lr")
						.replaceAll("r10","sl")
						.replaceAll("r15","pc")
						.replaceAll("r11","fp"))!=-1);
						if(result_now)
						{
							retListIter.add(s);
						}
					}
					exitK |= result_now;
					debug_s += s+",";
				}
				if(!exitK)
				{
					//for debug purposes, commented 
					if(sise.indexOf("disregard")!=-1)continue;
					if(sise.indexOf("r0")!=-1){continue;}
					if(sise.indexOf("r15")!=-1){retListIter.add(sise);continue;}
					if(sise.indexOf("r14")!=-1){retListIter.add(sise);continue;}
					if(sise.indexOf("r13")!=-1){continue;}
					if(sise.indexOf("lr")!=-1){retListIter.add(sise);continue;}
					if(sise.indexOf("sp")!=-1){retListIter.add(sise);continue;}
					if(sise.indexOf("pc")!=-1){retListIter.add(sise);continue;}
					//if(sise.startsWith("cp"))continue;
					if(ins.indexOf("cpsid") != -1 || ins.indexOf("cpsie") != -1)
					{
						continue;
					}
					if(opcd.length() < 8)
					{
						if(sise.indexOf("r7")!=-1){retListIter.add(sise);continue;}//, "pc");//!=-1)
					}
					if(culpritDetector)
					{
						int coproc_num = (int)(Long.parseLong(opcd, 16) >> 8)&0xf; 
						culpritString +=" culprit is: "+sise+" <=> "+debug_s+" "+coproc_num+" || ";
					}
					exitCondition = true;
					break;
				}
			}
			retList.add(retListIter);
		}
		if(ins.indexOf("{")!=-1 && ins.indexOf("}")!=-1)
		{
			addToList(retList, ins);
		}
		//return retList;
		alist.clear();
		alist.addAll(retList);
		return exitCondition;
	}
	public void addToList(ArrayList< ArrayList<String> > retlist, String ins)
	{
		int st = ins.indexOf("{"), end = ins.indexOf("}");
		String str = ins.substring(st+1, end);
		String prefix = "";
		for(char c:str.toCharArray())
		{
			if(Character.isAlphabetic(c))
			{
				prefix += c;
			}
			else
			{
				break;
			}
		}
		str = str.replaceAll(prefix, "");
		str = str.replaceAll("\\[", "");
		str = str.replaceAll("\\]", "");
		String []temp = str.split("-");
		if(temp.length > 1)
		{
			try
			{
				int regS = Integer.parseInt(temp[0]);
				int regE = Integer.parseInt(temp[1]);
				int incR = (regE-regS)/Math.abs(regE-regS);
				for(int iter = regS; iter != regE+incR; iter += incR)
				{
					//if(ins.trim().endsWith("}"))
					if(ins.indexOf("ld")!=-1)
					{
						retlist.get(1).add(prefix+iter);
					}
					else
					{
						retlist.get(0).add(prefix+iter);
					}
				}
			}
			catch(Exception e)
			{
				System.out.println(retlist+ins);
				e.printStackTrace();
				System.exit(0);
			}
		//	System.out.println(retlist+ins);
		}
	}
	public String checkLibBlock(Statement stmt)
	{
		try
		{
			if(!my_assoc_lib.equals("nothing"))return "";
			//	System.out.println(myIdStr);
			StringBuilder my_opcode = new StringBuilder("");
			int i = 0;
			String lib_name = "";
			for(int j = 0; j < instructions.size(); j++)
			{
				for(i = j; i < instructions.size(); i++)
				{
					my_opcode.append(getOpcode(instructions.get(i)));
					ResultSet rs = stmt.executeQuery("select lib_name from lib_hash_store where opcodes like '%"+my_opcode.toString()+"%'");
					//ResultSet rs = stmt.executeQuery("select lib_name from lib_hash_store where opcodes regexp '.*"+my_opcode.toString()+".*'");
					if(!rs.next())
					{
						break;
					}
					else
					{
						lib_name = rs.getString(1);
						while(rs.next())
						{
							lib_name+=","+rs.getString(1);
						}
					}
				}
				if(i != j)
				{
					double perc_match = ((i-j)*100.0/instructions.size());
					//System.out.println("found i = "+i+" "+perc_match+" "+lib_name+" "+my_opcode);
					my_assoc_lib = "found "+i+" "+(i*100.0/instructions.size())+" "+lib_name+" "+my_opcode;
					if(perc_match == 100.0)break;
				}
			}
			if(my_assoc_lib.equals("nothing"))
				my_assoc_lib = "not founding";
			//System.out.println(my_opcode);
			//ResultSet rs = stmt.executeQuery("select count(*) from lib_hash_store where opcodes like '%"+my_opcode.toString()+"%'");
			//if(!rs.next())
			//{
			////	break;
			//	my_assoc_lib = "found"+rs.getInt(1);
			//	System.out.println(my_assoc_lib);
			//}
			//else
			//{
			//	my_assoc_lib = "not found";//("found "+(i*100.0/instructions.size())+" match in lib");
			//}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return my_assoc_lib;
	}
}
