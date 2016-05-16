package main.java;
import java.util.*;
import java.io.*;
public class StrandDataBased implements Serializable//<StrandDataBased>
{
	TrackedStrands tracked = null;
	HashMap<String, InsTypeChain > destinationStrandMap;
	HashMap<String, Integer> prevMaintains;
	HashMap<String, HashSet<InsTypeChain>> activeLoadStoreChains = new HashMap<>();
	HashMap<String, String> dataSoFar = new HashMap<>();
	TreeMap<String, HashMap<String, Integer>> topStrands;
	private void readObject(ObjectInputStream in)throws IOException, ClassNotFoundException
	{
		destinationStrandMap = (HashMap<String, InsTypeChain>)in.readObject();
		prevMaintains = (HashMap<String, Integer>)in.readObject();
		activeLoadStoreChains = (HashMap<String, HashSet<InsTypeChain>>)in.readObject();
		dataSoFar = (HashMap<String, String>)in.readObject();
		topStrands = (TreeMap<String, HashMap<String, Integer>>)in.readObject();
		tracked = (TrackedStrands)in.readObject();
	}
	private void writeObject(ObjectOutputStream out)throws IOException
	{
		out.writeObject(destinationStrandMap);
		out.writeObject(prevMaintains);
		out.writeObject(activeLoadStoreChains);
		out.writeObject(dataSoFar);
		out.writeObject(topStrands);
		out.writeObject(tracked);
	}
	public StrandDataBased(TreeMap<String, HashMap<String, Integer>> topStrands, TrackedStrands tracked)
	{
		this.tracked = tracked;
		prevMaintains = new HashMap<>();
		destinationStrandMap = new HashMap<>();
		this.topStrands = topStrands;
	}
	public void addAllChains(StrandDataBased sm)
	{
		dataSoFar.putAll(sm.dataSoFar);
		sm.dataSoFar.clear();

		//for(String s:sm.activeLoadStoreChains.keySet())
		//{
		//	HashSet<InsTypeChain> insThis = this.activeLoadStoreChains.get(s),
		//		insSm = sm.activeLoadStoreChains.get(s);
		//	if(insThis != null)
		//	{
		//		insSm.addAll(insThis);
		//	}
		//	this.activeLoadStoreChains.put(s, insSm);
		//}
	}
	/*static*/ int bb_count_stat = 0, strand_stat = 0, coprocChain = 0;
	int instructions = 0;
	public void addNext(BasicBlock bb)
	{
		instructions += bb.insCount;
		bb_count_stat++;
		for(InsTypeChain ins:bb.myStrands)
		{
			strand_stat++;
			HashSet<InsTypeChain> current = activeLoadStoreChains.get(ins.getSource());
			if(current == null)
			{
				if(ins.isIdeal())
				{
					idealize(ins, bb.lineNumber, bb);
				}
				current = new HashSet<>();
				activeLoadStoreChains.put(ins.getDestination(), current);
				pickAllLoads(ins, current, bb.lineNumber, bb);
			}
			else
			{
				addTillStore(ins, current, bb.lineNumber, bb);
				pickAllLoads(ins, current, bb.lineNumber, bb);
			}
			if(ins.isCoprocBasedChain)
			{
				coprocChain++;
			}
		}
			for(String line:bb.memoryLines)
			{
				if(!line.startsWith("before entering:"))continue;
				//System.out.println(line);
				line = line.substring("before entering:".length());
				String[] all_values = line.split(" ");
				String prefix = "";
				for(int i = 0; i < all_values.length - 2;)
				{
					if(all_values[i+1].equals("="))
					{
						String key = prefix+all_values[i];
						//System.out.println("putting "+key+" = "+all_values[i+2]);
						dataSoFar.put(key, all_values[i+2]);
						//LinkedBlockingDeque<String> myVersions = memoryMap.get(key);
						//if(myVersions == null)
						//{
						//	myVersions = new LinkedBlockingDeque<String>();
						//}
						//myVersions.push(all_values[i+2]);
						//memoryMap.put(key, myVersions);//all_values[i+2]);
					}
					else
					{
						System.out.println(line);
						System.out.println("equals not matching at "+i);
						System.exit(0);
					}
					i+=3;
				}
			}
		if(bb_count_stat % 1000 == 0)
		{
			if(bb_count_stat % 10000 == 0)
			{
				System.out.println("completed bb number: "+bb_count_stat+" "+strand_stat+" "+coprocChain);
			}
			if(bb_count_stat >= 20000)
			{
				//System.out.println(this.toString());
				toString();
				this.clearAll();
			}
		}
	}
	public void idealize(InsTypeChain ins, long lineNumber, BasicBlock blockNow)
	{
		if(!ins.isIdeal())return;
		if(!tracked.canGo(ins.getSubString()))
		{
		//	System.out.println("tracked says no!"+ins);
			return;
		}
		if(ins.size() > 1000)return;
		HashMap<String, Integer> hm = topStrands.get(ins.toString());
		if(hm != null)//PRAS: put this if condition back
		{
			String temp = getData(lineNumber, ins, blockNow);
			System.out.println("data line: "+ins.myChainDefinition.get(0).pc+" "+ins.toString()+" : "+temp);
			if(1!=1)
			{
				return;//PRAS:remove the sys out and this return statement
			}
			Integer i = hm.get(temp);
			if(i == null)
			{
				i = 0;
			}
			i++;
			hm.put(temp, i);
			temp = temp.substring(temp.indexOf("|^|"));
			i = hm.get(temp);
			if(i == null)
			{
				i = 0;
			}
			i++;
			hm.put("pc:"+blockNow.myIdStr, -1);
			if(i == 1 && hm.size() >= 100)
			{
				Integer dumm = hm.get("lengthExplosion");
				if(dumm == null)
				{
					dumm = 0;
				}
				dumm++;
				hm.put("lengthExplosion", dumm);
				Integer length = hm.get("sizeExplosion");
				if(length == null)
				{
					length = 0;
				}
				length += temp.length();
				hm.put("sizeExplosion", length);
				return;
			}
			hm.put(temp, i);
		}
	}
	public String getData(long line_bb, InsTypeChain ins, BasicBlock blockNow)
	{

		StringBuilder ret_in = new StringBuilder("");
		StringBuilder ret_out = new StringBuilder("");
//		while(currentLine <= memFrames.lastKey())
//		{
//			HashMap<String, String> hm = memFrames.get(currentLine);
//			if(hm != null)
//			{
//				String isLoad = hm.get("load");
//				if(isLoad != null)
//				{
//					if(isLoad.equals("true"))
//					{
//						ret_in.append(hm.values());
//					}
//					else
//					{
//						ret_out.append(hm.values());
//					}
//				}
//				if(isLoad == null)
//				{
//					ret_out.append(hm.values());
//					//before entering line: break in the end;
//					break;
//				}
//				dataSoFar.putAll(hm);
//			}
//			currentLine++;
//		}
		ArrayList<TreeSet<String>> inouts = ins.getInsAndOuts();
		for(String in:inouts.get(0))
		{
			String result = dataSoFar.get(in);
			if(result == null)
			{
				in = in.replaceAll("ip", "r12")
					.replaceAll("sp", "r13")
					.replaceAll("sl", "r10")
					.replaceAll("fp", "r11")
					.replaceAll("pc", "r15")
					.replaceAll("\\[", "")
					.replaceAll("\\]", "")
					.replaceAll("lr", "r14");
				result = dataSoFar.get(in); 
			}
			if(result == null)
			{
				result = "";
				//System.out.println(in+" is not found in: "+dataSoFar);
			}
			//System.out.println(in+" = "+dataSoFar.get(in));
			ret_in.append(dataSoFar.get(in)).append("_");
		}
		for(String line:blockNow.memoryLines)
		{
			if(line.startsWith("@@"))
			{
				//before allthese, check whether the pc of any of the inschain matches with the pc of this load/store line
				boolean load = (line.indexOf("ld")>=0);
				line = line.substring(line.indexOf("(")+1, line.length()-1);
				String[] tempSpl = line.split("=");
				String adds[] = tempSpl[0].split(",");
				String value = tempSpl[1].trim();
				String pc = adds[0];
				String physicalAddress = adds[1];
				//if(ins.contains(pc))  //fix contains properly, fix ins input registers..
				{
					if(load)
					{
						ret_in.append(value).append("_");
						ret_in.append(physicalAddress).append("_");
						//System.out.println(ret_in);
					}
					else
					{
						ret_out.append(value).append("_");
						ret_out.append(physicalAddress).append("_");
						//System.out.println(ret_out);
					}
				}
			}
			else if(line.startsWith("before entering"))
			{
				//System.out.println("before enetering line = "+line);
				//must be the last line of the mem dump.. because, after this, the next bb starts..
				line = line.substring("before entering:".length());
				String[] all_values = line.split(" ");
				String prefix = "";
				for(int i = 0; i < all_values.length - 2;)
				{
					if(all_values[i+1].equals("="))
					{
						String key = prefix+all_values[i];
						//System.out.println("putting "+key+" = "+all_values[i+2]);
						dataSoFar.put(key, all_values[i+2]);
						ret_out.append(all_values[i+2]).append("_");
						//LinkedBlockingDeque<String> myVersions = memoryMap.get(key);
						//if(myVersions == null)
						//{
						//	myVersions = new LinkedBlockingDeque<String>();
						//}
						//myVersions.push(all_values[i+2]);
						//memoryMap.put(key, myVersions);//all_values[i+2]);
					}
					else
					{
						System.out.println(line);
						System.out.println("equals not matching at "+i);
						System.exit(0);
					}
					i+=3;
				}
			}
			else
			{
				System.out.println("something else?"+line);
			}
		}
		ret_in.append("|^|").append(ret_out);
		//System.out.println(blockNow.myIdStr+" "+ret_out+" "+ret_in);
		return ret_in.toString();
	}
	public void fixUpTopStrandsForGood()
	{

		for(String s:topStrands.keySet())
		{
			HashMap<String, Integer> hm = topStrands.get(s);
			//ArrayList<String> removes = new ArrayList<>();
			int count = 0, total = 0, size = 0, originalFreq = 0;
			for(String ss:hm.keySet())
			{
				if(ss.equals("oFreq"))
				{
					originalFreq = hm.get(ss);
				}
				if(ss.startsWith("pc"))
				{
					s=ss+":"+s;
					continue;
				}
				if(ss.equals("sizeExplosion"))continue;
				Integer freq = hm.get(ss);
				if(freq == 1)
				{
					//removes.add(ss);
				}
				else
				{
					if(!ss.equals("lengthExplosion"))
					{
						size += ss.length()/2;
					}
				}
				count++;
				total += freq;
			}
			//if(total/(float)count > 1.5)
			{
				//50% of the data gets accessed more than once print this as half guy..
				int insSaved = (total*(s.split(",").length-6));
				Integer sizeExplosion = hm.get("sizeExplosion");
				if(sizeExplosion == null)sizeExplosion = 0;
				System.out.println(s+"\t data used for memoization = "+hm);
				System.out.println(s+"\t"+originalFreq+" if memoized saves: "+insSaved+" instructions (out of "+instructions+" = "+(insSaved*100.0/instructions)+"% with a table of size "+(sizeExplosion+size)+", uniques i/o = "+count);
				if(count == 0 || size == 0)
				{
					System.out.println("PRASDEBUG: "+hm);
				}
				//System.out.println(s+" if memoized saves: "+(total*(s.split(",").length-3))+" instructions with a table of size "+size+", uniques i/o = "+count);
			}
			hm.clear();
		}
	}
	public void pickAllLoads(InsTypeChain ins, HashSet<InsTypeChain> current, long lineNumber, BasicBlock blockNow)
	{
		while(ins != null)
		{
			ins = ins.copyFromLoad();
			if(ins == null) break;
			current.add(ins);
			idealize(ins, lineNumber, blockNow);
			ins.myChainDefinition.remove(0);
		}
	}
	public void addTillStore(InsTypeChain ins, HashSet<InsTypeChain> current, long lineNumber, BasicBlock blockNow)
	{
		InsTypeChain tillStore = null;
		HashSet<InsTypeChain> removes = new HashSet<>();
		do
		{
			tillStore = ins.copyTillStore(tillStore);
			if(tillStore.getDestinationNode().ins_name.toLowerCase().contains("st"))
			{
				for(InsTypeChain ins1:current)
				{
					InsTypeChain nu = ins1.getACopy();
					nu.addToChain(tillStore);
					idealize(nu, lineNumber, blockNow);
					if(nu.size() > 1000)
					{
						removes.add(ins1);
					}
				}
			}
			for(InsTypeChain ins1:current)
			{
				ins1.addToChain(tillStore);
			}
			if(tillStore.size() == ins.size())
			{
				tillStore = null;
			}
		}while(tillStore != null);
		for(InsTypeChain insk:removes)
		{
			current.remove(insk);
		}
		while(current.size() > 900)
		{
			InsTypeChain toRemove = null;//current.iterator().next();
			int size = 0;//toRemove.size(); 
			for(InsTypeChain inso :current)
			{
				int siz = inso.size();
				if(siz > size)
				{
					toRemove = inso;
				}
			}
			//System.out.print("removing?"+size+" "+current.size()+" "+toRemove+" ");
			current.remove(toRemove);
			//System.out.println(" "+current.size());
		}
	}
	public void saveNew(InsTypeChain ins)
	{
		if(!ins.getSourceNode().ins_name.toLowerCase().contains("ld")||!ins.getSourceNode().ins_name.toLowerCase().contains("pop"))
		{
			return;//System.out.println(ins);
		}
		InsTypeChain current = destinationStrandMap.get(ins.getDestination());
		if(current != null)
		{
			collectStatsFrom(current);
		}
		current = ins.getACopy();
		//current.add(ins);
		destinationStrandMap.put(ins.getDestination(), current);
	}
	public void collectStatsFrom(InsTypeChain prev)
	{
		Integer i = prevMaintains.get(prev.toString());
		if(i == null)
		{
			i = 0;
		}
		i = i+1;
		String prev_s = "";
		prevMaintains.put(prev.toString(), i);
	}
	public void clearAll()
	{
		destinationStrandMap.clear();
		prevMaintains.clear();
		activeLoadStoreChains.clear();
	}
}
