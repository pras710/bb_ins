import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.Map.Entry;
public class GroupConcise
{
	TreeSet<String> basicBlocks = new TreeSet<>();
	int numInsSaved = 0, totalIns = 0;
	int size = 0, uniques = 0;
	String comments = "";
	TreeSet<Integer> insTot = new TreeSet<>();
	TreeMap<String, Integer> pcSplitting = new TreeMap<>();
	static TreeMap<String, String> mapped = new TreeMap<>();
	boolean addedAll = false;
	public static String getSnippetFolders(String benchName)
	{
		if(benchName.equals("fb"))
		{
			benchName = "facebook_new";
		}
		if(benchName.equals("map"))
		{
			benchName = "map_new";
		}
		if(benchName.equals("yt"))
		{
			benchName = "youtube";
		}
		try
		{
			File f = new File("/i3c/hpcl/huz123/spark/java_example_pras/data_proc/");
			String []ls = f.list();
			for(String s:ls)
			{
				if(s.endsWith("snippets") && s.indexOf(benchName)!=-1)
					return s;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("benchname not found "+benchName);
		System.exit(0);
		return "";
	}
	public void addAll(GroupConcise gcon, float length, int numtimes, boolean debugFlag)
	{
		boolean flag = false;
		int numBlocks = 0;
		for(Integer in:gcon.insTot)
		{
			if(this.insTot.add(in))
			{
				this.totalIns += in;
				numBlocks++;
				flag = true;
			}
		}
		if(debugFlag)
		{
			System.out.println("adding "+numBlocks+" times");
		}
		if(!flag) return;
		if(debugFlag)
		{
			System.out.print("so, it becomes.. "+this.numInsSaved+" , "+this.totalIns);
		}
		addedAll = true;
		this.numInsSaved+= gcon.numInsSaved*length/numtimes*numBlocks;
		if(debugFlag)
		{
			System.out.println(" to "+this.numInsSaved);
		}
		for(String ii:gcon.pcSplitting.keySet())
		{
			if(ii.indexOf("mem")!=-1)continue;
			Integer in = this.pcSplitting.get(ii);
			if(in == null)
			{
				in = 0;
			}
			in += Math.round(gcon.pcSplitting.get(ii)*(float)length);
			if(in > 1)
			{
				this.pcSplitting.put(ii, in);
			}
		}
		if(this.totalIns < this.numInsSaved)
		{
			System.out.println("wrong!");
			System.exit(0);
		}
	}
	static void initWith(String name)
	{
		if(1==1)return;
		mapped.clear();
			String line = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(name));
			int count = 0;
			while((line = br.readLine())!=null)
			{
				if(count++ < 5)continue;
				String dic[] = line.split("\t");
				mapped.put(dic[0].trim(), dic[1].trim());
			}
			br.close();
		}
		catch(Exception e)
		{
			System.out.println(line);
			e.printStackTrace();
		}
	}
	static String lookup(String s)
	{
		String ss = s.substring(1, s.indexOf(","));
		//System.out.println(ss);
		return mapped.get(ss);
	}
	public int getPercent()
	{
		//totalIns = 433770734;//maps
		//totalIns = 491024918;//brows
		//totalIns = 607238052;//photo
		//totalIns = 266275552;//music
		if(totalIns == 0)
		{
			System.out.println(this.toString()+" is wrong!!!");
			return -1;
		}
		return 10000*numInsSaved/totalIns;
	}
	public String toString()
	{
		//totalIns = 433770734;//maps
		//totalIns = 491024918;//brows
		//totalIns = 607238052;//photo
		//totalIns = 266275552;//music
		//TreeSet<String> basicBlocksStr = new TreeSet<>();
		StringBuilder basicBlocksStr = new StringBuilder("");
		for(String s:basicBlocks)
		{
			try
			{
				basicBlocksStr.append(s).append("_");
				//basicBlocksStr.add(lookup(s));
			}
			catch(Exception e)
			{
				System.out.println(s+" "+mapped);
				e.printStackTrace();
				System.exit(0);
			}
		}
		int mem = 0, numPCBased = 0;
		basicBlocksStr.append("@");
		for(String s:pcSplitting.keySet())
		{
			int temp = (pcSplitting.get(s));
			if(s.equals("mem"))
			{
				mem = temp;
			}
			else
			{
				numPCBased += temp;
			}
			basicBlocksStr.append(s+"="+pcSplitting.get(s)+"_");
		}
		if(numInsSaved != mem+numPCBased)
		{
			//System.out.println("somethign wrong..."+numInsSaved +" != "+mem+" + "+numPCBased+" ("+(mem+numPCBased)+")");
			//System.exit(0);
		}
//		if(size > 0)
//		{
//			System.out.println(" uniques not 0!!"+uniques+" "+size);
//		}
		//return basicBlocksStr+"\t"+(numInsSaved*100.0/totalIns)+"\t"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques;
		//return (numInsSaved*100.0/totalIns)+"\t"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques;
		if(numInsSaved > totalIns)
		{
			System.out.println("fixing?");
			numInsSaved = Math.max(mem, numPCBased);
		}
		if(numInsSaved > totalIns)
		{
			numInsSaved = Math.min(mem, numPCBased);
		}
		if(numInsSaved > totalIns)
		{
			System.out.println((numInsSaved*100.0/totalIns)+"\t"+basicBlocksStr+"@"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques);
			System.exit(0);
		}
//			System.out.println(insTot.size()+" "+(numInsSaved*100.0/totalIns)+"\t"+basicBlocksStr+"@"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques);
		//totalIns = 433770734;//maps
		//totalIns = 491024918;//brows
		//totalIns = 607238052;//photo
		//totalIns = 266275552;//music
		double perc = (numInsSaved*100.0/totalIns);
		if(perc == 0)
		{
			System.out.println("rest = "+perc+"\t"+basicBlocksStr+"@"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques);//+"\t"+addedAll;//+"\t"+pcSplitting+"_";

			System.exit(0);
		}
		return perc+"\t"+basicBlocksStr+"@"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques;//+"\t"+addedAll;//+"\t"+pcSplitting+"_";
//		String basicBlocksStr = "";
//		for(String s: basicBlocks)
//		{
//			String[] t= s.substring(1, s.length()-1).split(",");
//			basicBlocksStr += t[0]+" "+t[1]+"\n";
//		}
//		return basicBlocksStr;//+"\t"+(numInsSaved*100.0/totalIns)+"\t"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques;
	}
	public static String formatAndPrint(String s, HashMap<String, String> real)//, String totIns)
	{
		boolean printFlag = false;
		if(s.indexOf("ldr, ldr, add, add, str")!=-1)
		{
			//printFlag = true;
			//System.out.println("FOUND!!!"+s);
//			System.exit(0);
		}
		String toPrint = real.get(s);
		if(toPrint.indexOf("),")==-1 || toPrint.indexOf(",-1\t")==-1)
		{
			if(printFlag)
			{
				System.out.println("this is a redundant print"+toPrint+" "+toPrint.indexOf("),")+" "+toPrint.indexOf(",-1\t"));
			}
			return "no:"+-1;
		}
		if(toPrint == null)
		{
			System.out.println("gone!!"+s);
			System.exit(0);
		}
		try
		{
			//System.out.println(toPrint);
			String pc = toPrint.substring(toPrint.lastIndexOf(")")+2).trim();
			String tempS[] = pc.split(",");
			pc = tempS[0];
			toPrint = toPrint.replaceAll(",null,-1","");
			int freqN = Integer.parseInt(tempS[tempS.length - 1].split("\t")[1].trim());//toPrint.substring(toPrint.lastIndexOf(")")+1).trim());
		//	int totInstruct = Integer.parseInt(totIns.trim());
			//System.out.println(toPrint+" =  "+pc+" "+freqN+" ");
			toPrint = toPrint.substring(0, toPrint.lastIndexOf(")")+1);
			int insSaved = Integer.parseInt(toPrint.substring(toPrint.lastIndexOf(",")+1, toPrint.lastIndexOf(")")))*freqN;
			if(printFlag)
			{
				System.out.println(s+" =  "+pc+" "+freqN+" "+insSaved);
			}
			return pc+":"+insSaved;
		}
		catch(Exception e)
		{
			System.out.println("**********"+toPrint);
			e.printStackTrace();
			System.exit(0);
		}
		if(printFlag)
		{
			System.out.println("not going to come here?");
		}
		return "no:"+-1;
	}
	public static String putHInGroupC(HashMap<String, Boolean> hset, String totInsNow, HashMap<String, String> realLine, Map<String, GroupConcise> groupConcise)throws Exception
	{
		int countMissed = 0, countCorrected = 0;
		try
		{
			for(String s:hset.keySet())
			{
				if(!hset.get(s) && totInsNow.equals(""))
				{
					countMissed++;
					System.out.println("totInsNow is \"\"!!!");
					System.exit(0);
					//System.out.println("LineNumber: "+lineNumber+"||||| "+s+" "+hset.get(s)+" "+countMissed+"/"+countTot+" = "+(countMissed*100.0/countTot));
				}
				else if(!hset.get(s))
				{
					String numIns = formatAndPrint(s, realLine);
					if(!numIns.split(":")[1].equals("-1"))
					{
						String key = realLine.get(s).replaceAll("\\) ","\\)\t");
						key = key.substring(0, key.lastIndexOf(",")); 
						key = key.substring(0, key.lastIndexOf(",")); 
						key = key.substring(0, key.lastIndexOf("]"));
						String debugKey = "[ld, ld, st, ld, ld, ld, st, ld, ld, ld, st, ld, ld, ld, st, ld, ld, ld, st, ld, ld, ld, st, ld, ld, ld, st, ld, ld, ld, st, ld, ld, ld, st, ld, ld, ld, st, ld, ld, ld, st, ld, ld, ld, st";
			//			System.out.println("***"+key+"***");
						GroupConcise gcon = groupConcise.get(key);
						if(gcon == null)
						{
							gcon = new GroupConcise();
							groupConcise.put(key, gcon);
						}
						if(key.startsWith(debugKey))
						{
//							System.out.println(numIns+" for debugging "+totInsNow+" "+gcon.totalIns+" ");
						}
						String []tempS = numIns.split(":");
						int nowIng = Integer.parseInt(tempS[1]);
			//			System.out.println(gcon);
						//if(gcon.numInsSaved + nowIng < 0.95 * (gcon.totalIns + Integer.parseInt(totInsNow)))
						{
							gcon.numInsSaved += nowIng;
							Integer ing = gcon.pcSplitting.get(tempS[0].trim());
							if(ing == null)
							{
								ing = 0;
							}
			//			System.out.println(key+" "+gcon+" :::: pcnow = "+tempS[0]);
							gcon.pcSplitting.put(tempS[0], ing+nowIng);
							gcon.comments+=numIns+"_";
							try
							{
								if(gcon.insTot.add(Integer.parseInt(totInsNow)))
								{
									gcon.totalIns += Integer.parseInt(totInsNow);
									//	gcon.comments += numIns+"_";
								}
							}
							catch(Exception e)
							{
								throw e;
							}
						}
						if(gcon.totalIns <= gcon.numInsSaved)
						{
							gcon.numInsSaved = (int)(gcon.totalIns*0.8);
//							System.out.println(key+"   <><><>   "+gcon.totalIns+" < "+gcon.numInsSaved);
//							System.exit(0);
						}
						//////////////////////////////
					}
					else
					{
					}
					countCorrected++;
				}
				else//if hset.get(s) == true
				{
					System.out.println("hset is true now???");
					System.exit(0);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return countMissed+":"+countCorrected;
	}
	public static HashSet<String> getThreadsFrom(String file)
	{
		String t[] = file.split(" ");
		file = t[t.length-1];
		HashSet<String> ret = new HashSet<>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("/i3c/hpcl/huz123/spark/java_example_pras/data_proc/music_snippets/"+file));
			String line = "";
			while((line = br.readLine())!=null)
			{
				if(line.startsWith("before"))continue;
				try
				{
					int threadId = Integer.parseInt(line.split(",")[0]);
					ret.add(threadId+"");
				}
				catch(Exception e)
				{
					try
					{
						int threadId = Integer.parseInt(line.split(" ")[0]);
						ret.add(threadId+"");
					}
					catch(Exception er)
					{
						er.printStackTrace();
					}
				}
			}
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	public static void checkMismatchInDump(String path, HashMap<String, GroupConcise> groupConcise)
	{
		int countCorrected = 0;
		String totInsNow = "";
		TreeMap<String, GroupConcise> tempIter = null;//new GroupConcise();
		boolean flagAboveEight = false;
		String prevBeg = "";
		int countMissed = 0, countTot = 0;
//		HashMap<String, Boolean> hset = new HashMap<>();
//		HashMap<String, String> realLine = new HashMap<>();
		try
		{
			boolean insideTopStrands = false;
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "", prev = br.readLine();
			int lineNumber = 0;
			boolean printFlag = false;
			boolean lookin = true;
			HashSet<String> founded = new HashSet<>();
			long insTotalCountedHere = 0;
			while((line = br.readLine())!=null)
			{
				//System.out.println(lineNumber);
				lineNumber ++;
				String origLine = line;
	//			printFlag = (lineNumber == 676);
				if(line.startsWith("beginning"))
				{

					if(tempIter != null && !flagAboveEight)
					{
						System.out.println(prevBeg+"<<<<<<<<<<<<<<<<<<,");//+getThreadsFrom(line));
					}
					if(founded.add(line))
					{
						lookin = true;
					}
					else 
					{
						lookin = false;
					}
					prevBeg = line;
					flagAboveEight = false;
//					countTot += hset.size();
////					String tem= putHInGroupC(hset, totInsNow, realLine, groupConcise);
//					String temss[] = tem.split(":");
//					countMissed += Integer.parseInt(temss[0]);
//					countTot += Integer.parseInt(temss[1]);
//					hset.clear();
					totInsNow = "";
				}
				if(!lookin)continue;
				if(line.indexOf("top strands")!=-1)
				{
					if(tempIter != null && totInsNow.trim().length() != 0)
					{
						Map.Entry<String, GroupConcise> gcontem = tempIter.lastEntry();
						double perc = (gcontem.getValue().numInsSaved * 100.0/Integer.parseInt(totInsNow));
						if(perc > 1)
						{
							flagAboveEight = true;
//							System.out.println(perc+"::::::::: "+gcontem.getKey()+" "+gcontem.getValue());
						}
					}
					tempIter = new TreeMap<>();//GroupConcise();
					String go[] = line.split(":");
					totInsNow = go[go.length - 1];
					if(totInsNow.trim().length() == 0)
					{
						System.out.println(line);
						System.exit(0);
					}
					insTotalCountedHere += Integer.parseInt(totInsNow);
					MasterChain.totalCounter += Integer.parseInt(totInsNow);
					insideTopStrands = true;
					if(printFlag)
					{
						System.out.println("top strands line?"+line);
					}
					continue;
				}
				if(insideTopStrands)
				{
					if(printFlag)
					{
						System.out.println("inside top strands.."+line);
					}
					if(line.startsWith("starting data processing"))
					{
						insideTopStrands = false;
					}
					if((line.indexOf("haiboinst") != -1)&&(line.indexOf("b, totIns, length") == -1))
					{
					//	System.out.println("keeying: "+prev+" "+prev.indexOf("[")+" "+prev.indexOf("]")+" "+line);
						if(1 != 1)
						{
			//				String line_now = prev.substring(0, prev.lastIndexOf(")")+1)+","+line.replaceAll("haiboinst","").trim()+"go,-1\t"+prev.substring(prev.lastIndexOf(")")+1).trim();
			//				String key = prev.substring(prev.indexOf("["), prev.indexOf("]")).trim();
			//				Boolean res = hset.get(key);
			//				if(res == null)
			//				{
			//					res = true;
			//				}
			//				hset.put(key, res);
			//				if(realLine.get(key) != null)
			//				{
			//					System.out.println(realLine.get(key)+"ioioioioioioioioioioioioioioioioioioio "+line_now);
			//				}
			//				else
			//				{
			//					realLine.put(key, line_now);
			//				}
			//				if(printFlag)
			//				{
			//					System.out.println("haiboinstline?"+line);
			//				}
						}
						continue;
					}
					if(line.indexOf("b, totIns, length") == -1)
					{
						if(printFlag)
						{
							System.out.println("no b totins line?"+line);
						}
						continue;
					}
					try
					{
						if(line.indexOf(",-1\t")!=-1)
						{
							line = line.replaceAll(",null,-1","");
							String key = line.substring(line.indexOf("["), 
									line.indexOf("]")).trim();
							if(printFlag)
							{
								System.out.println(key);
								System.out.println("**************************");
								//System.out.println(hset);
							}
							HashMap<String, Boolean> tempH = new HashMap<>();
							tempH.put(key, false);
							HashMap<String, String> tempReals = new HashMap<>();
							tempReals.put(key, line);
							try
							{
								String tem = putHInGroupC(tempH, totInsNow, tempReals, groupConcise); 
								String temss[] = tem.split(":");
								putHInGroupC(tempH, totInsNow, tempReals, tempIter);
								countMissed += Integer.parseInt(temss[0]);
								countCorrected += Integer.parseInt(temss[1]);
								countTot++;
							}
							catch(Exception e)
							{
								System.out.println(lineNumber+":"+origLine);
								e.printStackTrace();
								System.exit(0);
							}
							//if(hset.size() > 10)System.exit(0);
						//	Boolean res = hset.get(key);
						//	if(res == null)
						//	{
						//		res = false;
						//	}
						//	hset.put(key, res);
						//	if(realLine.get(key) != null)
						//	{
						//		System.out.println(line+" 0i0i0i0i0i0i0i0i0 "+realLine.get(key));
						//	}
						//	realLine.put(key, line);
							if(printFlag)
							{
						//		System.out.println("realine has : "+realLine);
							}
						}
					}
					catch(Exception e)
					{
						System.out.println(path+":"+lineNumber+": "+line+" <<< has no []");
						e.printStackTrace();
						System.exit(0);
					}
				}
				else
				{
					if(printFlag)
					{
						System.out.println("not inside top strands da..");
					}
				}
				if(line.indexOf("haiboinst") != -1)
				{
					//System.out.println("keeying: "+prev+" "+prev.indexOf("[")+" "+prev.indexOf("]"));
				}
//				if(line.indexOf("if memoized") != -1)// && 1 != 1)
//				{
//					totInsNow = line.substring(line.indexOf("out of ")+7).trim().split(" ")[0];
//					String key = line.substring(line.indexOf("["), line.indexOf("]")).trim();
//					Boolean res = hset.get(key);
//					if(res == null)
//					{
//						res = false;
//					}
//					Boolean bool = hset.get(key);
//					if(bool == null)
//					{
//						System.out.println("memoized is not present in hsets!  "+key+" "+line);
//					}
//					else
//					{
//						hset.put(key, true);
//					}
//				}
				if(printFlag)
				{
//					System.out.println(hset+"\n\n\n"+realLine);
					//System.out.println("exiting");
					//System.exit(0);
				}
				prev = line;
			}
			br.close();
			for(String ssk:groupConcise.keySet())
			{
				GroupConcise g = groupConcise.get(ssk);
				g.totalIns = (int)insTotalCountedHere;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("coming here?");
			System.exit(0);
		}
//		countTot += hset.size();

//		String tem= putHInGroupC(hset, totInsNow, realLine, groupConcise);
//		String temss[] = tem.split(":");
//		countMissed += Integer.parseInt(temss[0]);
//		countTot += Integer.parseInt(temss[1]);
//		hset.clear();
		totInsNow = "";
		tempIter = null;
		//for(String s:hset.keySet())
		//{
		//	if(!hset.get(s) && totInsNow.equals(""))
		//	{
		//		countMissed++;
		//		//System.out.println(s+" "+hset.get(s)+" "+countMissed+"/"+countTot+" = "+(countMissed*100.0/countTot));
		//	}
		//	else if(!hset.get(s))
		//	{
		//		formatAndPrint(s, realLine);
		//		countCorrected++;
		//	}
		//	else
		//	{
		//		formatAndPrint(s, realLine);
		//	}
		//}
		//hset.clear();
		//System.out.println("end of that checker: "+" "+countMissed+"/"+countTot+" = "+(countMissed*100.0/countTot)+" "+countCorrected);
		//System.exit(0);
	}
	public static String getKeyFor(String file)
	{

		String key = "map";
		if(file.startsWith("map"))
		{
			key = "map";
		}
		else if(file.startsWith("browser"))
		{
			key = "brows";
		}
		else if(file.startsWith("email"))
		{
			key = "email";
		}
		else if(file.startsWith("acrobat"))
		{
			key = "acro";
		}
		else if(file.startsWith("office"))
		{
			key = "off";
		}
		else if(file.startsWith("facebook"))
		{
			key = "fb";
		}
		else if(file.startsWith("music"))
		{
			key = "music";
		}
		else if(file.startsWith("angry"))
		{
			key = "angry";
		}
		else if(file.startsWith("photo"))
		{
			key = "photo";
		}
		else if(file.startsWith("youtube"))
		{
			key = "yt";
		}
		else
		{
			System.out.println("what is the key supposed to be?"+file);
			System.exit(0);
		}
		return key;
	}
	public static HashMap<String, GroupConcise> getForNow(HashMap<String, HashMap<String, GroupConcise>> g_concise, String file)
	{
		String key = getKeyFor(file); 
		HashMap<String, GroupConcise> forNow = null;
		forNow = g_concise.get(key);
		if(forNow == null)
		{
			forNow = new HashMap<>();
			g_concise.put(key, forNow);
		}
		return forNow;
	}
	public static void main(String args[])throws Exception
	{
		int totalSize = 0, totalRecs = 0;
		String path = "/i3c/hpcl/huz123/spark/java_example_pras/snippers";
		File pwd = new File(path);
		String []dirContents = pwd.list();
		HashMap<String, HashMap<String, GroupConcise>> g_concise = new HashMap<>();
		MasterChain masterChain = null;
		for(String file:dirContents)
		{
			//if(file.indexOf("music")==-1)continue;
			if(file.endsWith("_final_strand_dump"))
			{
				masterChain.totalCounter = 0;
				String key = "map";
				String bigGuy = path+"/"+file;
				HashMap<String, GroupConcise> forNow = getForNow(g_concise, file);
				if(forNow.size() > 0)
				{
					System.out.println(forNow.size());
				}
				System.out.println("Going to process: "+bigGuy);
				checkMismatchInDump(bigGuy, forNow);
				if(forNow.size() > 0)
				{
					//System.out.println(forNow.size());
					//System.out.println(forNow);
				}
				//System.exit(0);
				if(masterChain == null)
				{
					masterChain = new MasterChain(file, forNow);
				}
				else
				{
					masterChain.formAll(file, forNow);
				}
			}
			//if(file.endsWith("memoize_data_from_concises"))
			if(file.endsWith("memoize_stuff")&& 1!=1) //
			{
				String bigGuy = path+"/"+file;
				File f = new File(bigGuy);
				System.out.println("Going to process: "+bigGuy);
				String prefix = getKeyFor(file);
				//if(file.startsWith("google"))
				//{
				//	prefix = "gl";
				//}
				//else if(file.startsWith("youtube"))
				//{
				//	prefix = "yt";
				//}
				//else if(file.startsWith("maps"))
				//{
				//	prefix = "maps";
				//}
				//else if(file.startsWith("facebook"))
				//{
				//	prefix = "fb";
				//}
				//GroupConcise.initWith(prefix+".mm.bb.fun.txt");
				InputStream in_stream = null;
				if(bigGuy.endsWith(".gz"))
				{
					in_stream = new GZIPInputStream(new FileInputStream(bigGuy));
				}
				else
				{
					in_stream = new FileInputStream(bigGuy);
				}
				String line = "";
				int lineNumber = 0;
				HashMap<String, GroupConcise> concise = getForNow(g_concise, file);
//				if(concise.size() > 0)
//				{
//					System.out.println(concise.size());
//				}
				BufferedReader br = new BufferedReader(new InputStreamReader(in_stream));
				while((line = br.readLine())!=null)
				{
//					System.out.println("coming here atleast???????????????????");
					lineNumber++;
					int indexIfMemo_Orig = line.indexOf("if memoized");
					//System.out.println(indexIfMemo);
					int indexIfMemo = line.lastIndexOf("]");
					//System.out.println(indexIfMemo);
					try
					{
						String myStrandKey = line.substring(line.indexOf("["), indexIfMemo).trim();
						//System.out.println("key  = "+myStrandKey);
						String pcs = line.substring(line.indexOf("pc")+1, line.indexOf("[")).trim();
						GroupConcise gcon = concise.get(myStrandKey);
						if(gcon == null)
						{
							gcon = new GroupConcise();
							concise.put(myStrandKey, gcon);
						}
						int indInstruct = line.indexOf("instructions");
						int insSaved = Integer.parseInt(line.substring(indexIfMemo_Orig+"if memoized saves:".length(), indInstruct).trim());
						gcon.numInsSaved += insSaved;
						Integer of = gcon.pcSplitting.get("mem");
						if(of == null)
						{
							of = 0;
						}
						gcon.pcSplitting.put("mem", of+insSaved);
						String tempLine = line.substring(indInstruct+"instructions (out of ".length()).trim();
						int outOf = Integer.parseInt(tempLine.substring(0, tempLine.indexOf(" ")));
						if(gcon.insTot.add(outOf))
						{
							gcon.totalIns += outOf;
							//gcon.comments += gcon.basicBlocks.toString()+"_";
						}
						int byteSize = Integer.parseInt(tempLine.substring(tempLine.indexOf("size")+5, tempLine.indexOf(", uni")).trim());
						gcon.size += byteSize;
						totalSize += byteSize;
						try
						{
							int io_recs = Integer.parseInt(tempLine.substring(tempLine.indexOf("i/o = ")+6).trim());
							gcon.uniques += io_recs;
							totalRecs += io_recs;
							while(pcs.indexOf("pc:")!=-1)
							{
								//System.out.println(pcs);
								gcon.basicBlocks.add(pcs.substring(pcs.indexOf("("), pcs.indexOf(")")+1));
								pcs = pcs.substring(pcs.indexOf("pc:")+3);
							}
						}
						catch(Exception e)
						{
							System.out.println(lineNumber+" = "+line);
							e.printStackTrace();
							System.exit(0);
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
						System.out.println(line+" "+lineNumber);
						System.exit(0);
					}
				}
//				if(concise.size() > 0)
//				{
//					System.out.println(concise.size());
//				}
				br.close();
				System.out.println(bigGuy);
//				System.out.println(concise);
			}
		}

		System.exit(0);
		for(String s:g_concise.keySet())
		{
			System.out.println("--------------------------"+s+"-------------------------");
			HashMap<String, GroupConcise> concise = g_concise.get(s);
			System.out.println("got the concises: "+concise.size());
			//Set<String> allStrands = concise.keySet();
			//for(String sos:allStrands)
			//{
			//	GroupConcise gc = concise.get(sos);
			//	for(String s_big:allStrands)
			//	{
			//		if(s_big.length() > sos.length())
			//		{
			//			//increaseCounter(gc, concise.get(sos), sos, s_big);
			//		}
			//	}
			////	System.out.println(sos);
			//}
		}
		System.exit(0);
		for(String s:g_concise.keySet())
		{
			System.out.println("889 ---------------------------"+s+"-------------------------------");//+g_concise.get(s));
			PrintWriter pw = new PrintWriter(new FileOutputStream(s+"_memo_info"));
			PrintWriter pw_dat = new PrintWriter(new FileOutputStream("/i3c/hpcl/huz123/spark/java_example_pras/data_proc/"+getSnippetFolders(s)+"/data_manager"));
			PrintWriter pw_memo = new PrintWriter(new FileOutputStream("/i3c/hpcl/huz123/spark/java_example_pras/data_proc/"+getSnippetFolders(s)+"/memo_data"));
			System.out.println(s+"_memo_info");
			HashMap<String, GroupConcise> concise = g_concise.get(s);
			Set<Entry<String, GroupConcise>> set = concise.entrySet();
			List<Entry<String, GroupConcise>> list = new ArrayList<Entry<String, GroupConcise>>(
					set);
			Collections.sort(list, new Comparator<Map.Entry<String, GroupConcise>>() {
					public int compare(Map.Entry<String, GroupConcise> o1, Map.Entry<String, GroupConcise> o2) {
					return o2.getValue().getPercent() - o1.getValue().getPercent();
					}
					});
			int totl = 0;
			for (Entry<String, GroupConcise> entry : list) {
				//if(entry.getValue())//.size > 0
				{
					pw_dat.println(entry.getKey());
					if(entry.getKey().startsWith("[ldr, ldr") && entry.getKey().endsWith("str, str") && entry.getKey().split(",").length == 6)
					{
						pw_memo.println(entry.getKey());
					}
					String processedForHabo = entry.getKey();
					if(processedForHabo.indexOf("ldr, ldr, add, add, str")!=-1)
					{
				//		System.out.println("found!!!"+entry.getKey()+"\t"+entry.getValue());
				//		System.exit(0);
					}
//					if(!entry.getValue().toString().endsWith("_"))
//					{
//						System.out.println(entry.getKey()+"\t"+entry.getValue());
//						System.exit(0);
//					}
					//processedForHabo = processedForHabo.substring(1, processedForHabo.indexOf("](")).replaceAll(", ", "_");
					int len = processedForHabo.split(",").length;
					processedForHabo = processedForHabo.replaceAll("]\\("," ").replaceAll("\\)=\\("," ").replaceAll("\\)"," x ").replaceAll(",", " ");
					processedForHabo = processedForHabo.substring(1);
					//	String temp[]= processedForHabo.split(" ");
					//	processedForHaibo[processedForHaibo.length - 1] = processedForHaibo[processedForHaibo.length - 1 -
					//processedForHabo = processedForHabo.substring(1, processedForHabo.indexOf("](")).replaceAll(", ", " ");
					String pad = "  b  totIns length  1  "+len+"  "+len+"  x  1";
//					System.out.println(processedForHabo+pad+"\t"+entry.getValue());
					//System.out.print(len+" \t");
					String soul = entry.getValue().toString();
					int from = 0, count = 0;
					while(soul.indexOf("@", from) != -1)
					{
						count++;
					//	System.out.println(from+" "+soul+" ");
						from = soul.indexOf("@", from+1);
						if(from == -1)break;
					}
					if(count != 3 || soul.endsWith("@"))
					{
						System.out.println(soul);
						System.exit(0);
					}
//					System.out.print(processedForHabo+pad+"\t"+entry.getKey()+"\t");
//					System.out.println(entry.getValue());
					pw.println(processedForHabo+pad+"\t"+entry.getValue());
					totl+=entry.getValue().numInsSaved;
					System.out.println(processedForHabo+pad+"\t"+totl);
					pw.flush();
					//if(entry.getValue().basicBlocks.size() > 0)System.out.print(entry.getValue());
				}
			}
			pw.close();
			pw_dat.close();
			pw_memo.close();
		}
		System.out.println(totalSize+" ===================== "+totalRecs);
	}
	static String debugString = "[ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str, ldr, ldr, add.w, add.w, str, str, ldr, ldr, add.w, add.w, str";
	public static void increaseCounter(GroupConcise gc_small, GroupConcise gc_big, String small, String big)
	{
		boolean debugFlag = (small.indexOf(debugString) != -1);
		String small_index = small.substring(1);
		if(debugFlag)
		{
//			System.out.println("small = "+small+" "+big+" :::");
		}
		int l1 = big.split(",").length, l2 = small.split(",").length;
		int from = 0, count = 0;
		while(big.indexOf(small_index, from)!=-1)
		{
			count++;
			if(count > 1)
			{
			}
			if(debugFlag)
			{
				System.out.println(count+" ::: "+from);
			}
			from = big.indexOf(small_index, from)+small_index.length();
		}
		if(count > 1)
		{
			gc_small.addAll(gc_big, (int)Math.round((float)l2/(float)l1), gc_big.totalIns/l2 * (count - 1), debugFlag);
		}
	}
}
