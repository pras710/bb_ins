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
		return 10000*numInsSaved/totalIns;
	}
	public String toString()
	{
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
//		if(size > 0)
//		{
//			System.out.println(" uniques not 0!!"+uniques+" "+size);
//		}
		//return basicBlocksStr+"\t"+(numInsSaved*100.0/totalIns)+"\t"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques;
		//return (numInsSaved*100.0/totalIns)+"\t"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques;
		return (numInsSaved*100.0/totalIns)+"\t"+basicBlocksStr+"_"+numInsSaved+"\t"+totalIns+"\t"+size+"\t"+uniques+"\t"+pcSplitting+"_";
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
		String toPrint = real.get(s);
		if(toPrint.indexOf("),")==-1 || toPrint.indexOf(",-1\t")==-1)
		{
//			System.out.println("this is a redundant print");
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
		//	System.out.println(s+" =  "+pc+" "+freqN+" "+insSaved);
			return pc+":"+insSaved;
		}
		catch(Exception e)
		{
			System.out.println("**********"+toPrint);
			e.printStackTrace();
			System.exit(0);
		}
		return "no:"+-1;
	}
	public static void checkMismatchInDump(String path, HashMap<String, GroupConcise> groupConcise)
	{
		int countCorrected = 0;
		String totInsNow = "";
		int countMissed = 0, countTot = 0;
		HashMap<String, Boolean> hset = new HashMap<>();
		HashMap<String, String> realLine = new HashMap<>();
		try
		{
			boolean insideTopStrands = false;
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "", prev = br.readLine();
			int lineNumber = 0;
			while((line = br.readLine())!=null)
			{
				lineNumber ++;
				if(line.startsWith("beginning"))
				{
					countTot += hset.size();
					for(String s:hset.keySet())
					{
						if(!hset.get(s) && totInsNow.equals(""))
						{
							countMissed++;
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
								System.out.println("***"+key+"***");
								GroupConcise gcon = groupConcise.get(key);
								if(gcon == null)
								{
									gcon = new GroupConcise();
									groupConcise.put(key, gcon);
								}
								String []tempS = numIns.split(":");
								int nowIng = Integer.parseInt(tempS[1]);
								System.out.println(gcon);
								gcon.numInsSaved += nowIng;
								Integer ing = gcon.pcSplitting.get(tempS[0].trim());
								if(ing == null)
								{
									ing = 0;
								}
								System.out.println(key+" "+gcon+" :::: pcnow = "+tempS[0]);
								gcon.pcSplitting.put(tempS[0], ing+nowIng);
								gcon.comments+=numIns+"_";
								if(gcon.insTot.add(Integer.parseInt(totInsNow)))
								{
									gcon.totalIns += Integer.parseInt(totInsNow);
								//	gcon.comments += numIns+"_";
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
							if(s.indexOf("ldr, ldr, add, add, str")!=-1)
							{
								System.out.println("FOUND!!!"+s);
								System.exit(0);
							}
						}
					}
					hset.clear();
					totInsNow = "";
				}
				if(line.indexOf("top strands")!=-1)
				{
					String go[] = line.split(":");
					totInsNow = go[go.length - 1];
					insideTopStrands = true;
					continue;
				}
				if(insideTopStrands)
				{
					if(line.startsWith("starting data processing"))
					{
						insideTopStrands = false;
					}
					if((line.indexOf("haiboinst") != -1)&&(line.indexOf("b, totIns, length") == -1))
					{
					//	System.out.println("keeying: "+prev+" "+prev.indexOf("[")+" "+prev.indexOf("]")+" "+line);
						String line_now = prev.substring(0, prev.indexOf("]")+1)+","+line.replaceAll("haiboinst","").trim()+"h,-1\t"+prev.substring(prev.indexOf("]")+1).trim();
						String key = prev.substring(prev.indexOf("["), prev.indexOf("]")).trim();
						Boolean res = hset.get(key);
						if(res == null)
						{
							res = false;
						}
						hset.put(key, res);
						realLine.put(key, line_now);
						continue;
					}
					if(line.indexOf("b, totIns, length") == -1)continue;
					try
					{
						line = line.replaceAll(",null,-1","");
						String key = line.substring(line.indexOf("["), 
								line.indexOf("]")).trim();
						//System.out.println(key);
						//System.out.println("**************************");
						//System.out.println(hset);
						//if(hset.size() > 10)System.exit(0);
						Boolean res = hset.get(key);
						if(res == null)
						{
							res = false;
						}
						hset.put(key, res);
						realLine.put(key, line);
					}
					catch(Exception e)
					{
						System.out.println(path+":"+lineNumber+": "+line+" <<< has no []");
						e.printStackTrace();
						System.exit(0);
					}
				}
				if(line.indexOf("haiboinst") != -1)
				{
					//System.out.println("keeying: "+prev+" "+prev.indexOf("[")+" "+prev.indexOf("]"));
				}
				if(line.indexOf("if memoized") != -1 && 1 != 1)
				{
					totInsNow = line.substring(line.indexOf("out of ")+7).trim().split(" ")[0];
					String key = line.substring(line.indexOf("["), line.indexOf("]")).trim();
					Boolean res = hset.get(key);
					if(res == null)
					{
						res = false;
					}
					Boolean bool = hset.get(key);
					if(bool == null)
					{
						System.out.println("memoized is not present in hsets!  "+key+" "+line);
					}
					else
					{
						hset.put(key, true);
					}
				}
				prev = line;
			}
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		countTot += hset.size();
		for(String s:hset.keySet())
		{
			if(!hset.get(s) && totInsNow.equals(""))
			{
				countMissed++;
				//System.out.println(s+" "+hset.get(s)+" "+countMissed+"/"+countTot+" = "+(countMissed*100.0/countTot));
			}
			else if(!hset.get(s))
			{
				formatAndPrint(s, realLine);
				countCorrected++;
			}
		}
		hset.clear();
		System.out.println("end of that checker: "+" "+countMissed+"/"+countTot+" = "+(countMissed*100.0/countTot)+" "+countCorrected);
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
			key = "facebook";
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
		for(String file:dirContents)
		{
			if(file.indexOf("music")==-1)continue;
			if(file.endsWith("final_strand_dump"))
			{
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
					System.out.println(forNow.size());
				}
			}
			//if(file.endsWith("memoize_data_from_concises"))
			if(file.endsWith("memoize_stuff") && 1!=1)
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
					int indexIfMemo = line.lastIndexOf(")\t")+1;
					//System.out.println(indexIfMemo);
					String myStrandKey = line.substring(line.indexOf("["), indexIfMemo).trim();
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
//				if(concise.size() > 0)
//				{
//					System.out.println(concise.size());
//				}
				br.close();
				System.out.println(bigGuy);
//				System.out.println(concise);
			}
		}
		for(String s:g_concise.keySet())
		{

			System.out.println("---------------------------"+s+"-------------------------------");//+g_concise.get(s));
			HashMap<String, GroupConcise> concise = g_concise.get(s);
			Set<Entry<String, GroupConcise>> set = concise.entrySet();
			List<Entry<String, GroupConcise>> list = new ArrayList<Entry<String, GroupConcise>>(
					set);
			Collections.sort(list, new Comparator<Map.Entry<String, GroupConcise>>() {
					public int compare(Map.Entry<String, GroupConcise> o1, Map.Entry<String, GroupConcise> o2) {
					return o2.getValue().getPercent() - o1.getValue().getPercent();
					}
					});

			for (Entry<String, GroupConcise> entry : list) {
				//if(entry.getValue())//.size > 0
				{
					String processedForHabo = entry.getKey();
					if(processedForHabo.indexOf("ldr, ldr, add, add, str")!=-1)
					{
						System.out.println("found!!!"+entry.getKey()+"\t"+entry.getValue());
						System.exit(0);
					}
					if(!entry.getValue().toString().endsWith("_"))
					{
						System.out.println(entry.getKey()+"\t"+entry.getValue());
						System.exit(0);
					}
					//processedForHabo = processedForHabo.substring(1, processedForHabo.indexOf("](")).replaceAll(", ", "_");
					processedForHabo = processedForHabo.replaceAll("]\\("," ").replaceAll("\\)=\\("," ").replaceAll("\\)"," x ").replaceAll(",", " ");
					processedForHabo = processedForHabo.substring(1);
					//	String temp[]= processedForHabo.split(" ");
					//	processedForHaibo[processedForHaibo.length - 1] = processedForHaibo[processedForHaibo.length - 1 -
					//processedForHabo = processedForHabo.substring(1, processedForHabo.indexOf("](")).replaceAll(", ", " ");
					System.out.println(processedForHabo+"\t"+entry.getValue());
					//if(entry.getValue().basicBlocks.size() > 0)System.out.print(entry.getValue());
				}
			}
		}
		System.out.println(totalSize+" ===================== "+totalRecs);
	}
}
