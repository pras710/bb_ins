package main.java;
import java.util.*;
import java.io.*;
public class StrandMaintainer implements Serializable//<StrandMaintainer>
{
	public static final byte THRESHOLD = 20;
	public static final byte THRESHOLD_PERC = 1;
	//ORIGINAL PRAS: public static final byte THRESHOLD = 20;
	HashMap<String, InsTypeChain > destinationStrandMap;
	TrackedStrands tracked = null;
	//HashMap<String, ArrayList<InsTypeChain> > destinationStrandMap;
	HashMap<String, Integer> prevMaintains;
	ArrayList<InsTypeChain> prevRawChains;
	//HashMap<String, Integer> subChains;
//	HashMap<String, Integer> homoChains;
	HashMap<String, HashSet<InsTypeChain>> activeLoadStoreChains = new HashMap<>();
	HashMap<String, HashMap<String, Integer>> completedLoadStoreChains = new HashMap<>();
//	HashMap<String, HashSet<String>> completedLoadStoreBBPCs = new HashMap<>();
//	HashMap<String, InsTypeChain> insChainToKey = new HashMap<>();
	boolean postProcessed = false;
	InsTypeChain longestStrand = null;
	int totalDependenceEdges = 0;
	long tid = -1;
	TreeMap<String, Integer> myFinalStrands;
	int instructions = 0;
	private void readObject(ObjectInputStream in)throws IOException, ClassNotFoundException
	{
		destinationStrandMap = (HashMap<String, InsTypeChain>)in.readObject();
		prevMaintains = (HashMap<String, Integer>)in.readObject();
		prevRawChains = (ArrayList<InsTypeChain>)in.readObject();
		//homoChains = (HashMap<String, Integer>)in.readObject();
		activeLoadStoreChains = (HashMap<String, HashSet<InsTypeChain>>)in.readObject();
//		System.out.println(activeLoadStoreChains.size()+" read actives");
		completedLoadStoreChains = (HashMap<String, HashMap<String, Integer>>)in.readObject();
//		completedLoadStoreBBPCs = (HashMap<String, HashSet<String>>)in.readObject();
		longestStrand = (InsTypeChain)in.readObject();
		postProcessed = (Boolean)in.readObject();
		totalDependenceEdges = (Integer)in.readObject();
		tid = (Long)in.readObject();
		myFinalStrands = (TreeMap<String, Integer>)in.readObject();
		tracked = (TrackedStrands)in.readObject();
//		insChainToKey = (HashMap<String, InsTypeChain>)in.readObject();
	}
	private void writeObject(ObjectOutputStream out)throws IOException
	{
		out.writeObject(destinationStrandMap);
		out.writeObject(prevMaintains);
		out.writeObject(prevRawChains);
//		out.writeObject(homoChains);
		out.writeObject(activeLoadStoreChains);
		out.writeObject(completedLoadStoreChains);
//		out.writeObject(completedLoadStoreBBPCs);
		out.writeObject(longestStrand);
		out.writeObject((Boolean)postProcessed);
		out.writeObject((Integer)totalDependenceEdges);
		out.writeObject((Long)tid);
		out.writeObject(myFinalStrands);
		out.writeObject(tracked);
//		out.writeObject(insChainToKey);
	}
	/*static*/ int objCount = 0;
	public StrandMaintainer(long tid, TrackedStrands tracked)
	{
		this.tracked = tracked;
		System.out.println("creating obj count = "+objCount);
		objCount++;
		this.tid = tid;
		prevMaintains = new HashMap<>();
		prevRawChains = new ArrayList<>();
		destinationStrandMap = new HashMap<>();
		//subChains = new HashMap<>();
		//homoChains = new HashMap<>();
		myFinalStrands = new TreeMap<String, Integer>();
	}
	public void addAllChains(StrandMaintainer sm)
	{
		//System.out.println("calling this:");
//		for(String s:sm.insChainToKey.keySet())
//		{
//			InsTypeChain ins = this.insChainToKey.get(s);
//			if(ins != null)
//			{
//				//ins.lineNumberOccurrence.addAll(sm.insChainToKey.get(s).lineNumberOccurrence);
//			}
//			else
//			{
//				this.insChainToKey.put(s, sm.insChainToKey.get(s));
//			}
//		}
		int countChains = 0;
		for(String s:sm.activeLoadStoreChains.keySet())
		{
			HashSet<InsTypeChain> insThis = this.activeLoadStoreChains.get(s),
				insSm = sm.activeLoadStoreChains.get(s);
			if(insThis != null)
			{
				insSm.addAll(insThis);
			}
			countChains += insThis.size();
			this.activeLoadStoreChains.put(s, insSm);
		}
		System.out.println(countChains+" adding all chains");
		for(String s:sm.completedLoadStoreChains.keySet())
		{
			HashMap<String, Integer> intnow = this.completedLoadStoreChains.get(s),
					intsm = sm.completedLoadStoreChains.get(s);
			if(intnow == null)
			{
				intnow = new HashMap<String, Integer>();
				this.completedLoadStoreChains.put(s, intnow);
			}
//			if(intnow + intsm > 50)
//			{
				for(String s_s:intsm.keySet())
				{
					Integer intMe = intnow.get(s_s);
					if(intMe == null)
					{
						intMe = 0;
					}
					intnow.put(s_s, intMe+intsm.get(s_s));
				}
//				HashSet<String> hsThis = completedLoadStoreBBPCs.get(s), 
//					hsNow = sm.completedLoadStoreBBPCs.get(s);
				//if(hsThis == null)
				//{
				//	hsThis = new HashSet<String>();
				//}
				//if(hsNow == null)
				//{
				//	System.out.println("hs now is null as well?");
				//}
				//hsThis.addAll(hsNow);
				//completedLoadStoreBBPCs.put(s, hsThis);
//				System.out.println(completedLoadStoreBBPCs);
				//hsNow.clear();
//			}
		}

	}
	public void postProcess()
	{
		postProcessed = true;
		if(1==1)return;
		for(String s: destinationStrandMap.keySet())
		{
			//saveNew(destinationStrandMap.get(s));
			collectStatsFrom(destinationStrandMap.get(s));
		}
		//for(int i = 2; i <= 12; i++)
		//{
		//	for(InsTypeChain inChain: prevRawChains)
		//	{
		//		if(inChain.size() <= i)continue;
		//		//find sub-chains and maintain..
		//		ArrayDeque<ChainData> queue = new ArrayDeque<>();
		//		String key = inChain.toString();
		//		int multFactor = prevMaintains.get(key);
		//		for(ChainData c:inChain.myChainDefinition)
		//		{
		//			queue.add(c);
		//			if(queue.size() == i)
		//			{
		//				String addKey = queue.toString();
		//				queue.remove();
		//				Integer iT = prevMaintains.get(addKey);
		//				if(iT == null)
		//				{
		//					iT = 0;
		//				}
		//				iT = iT+multFactor;
		//				prevMaintains.put(addKey, iT);
		//			}
		//		}
		//	}
		//}
	}
	public String findSizeOf()
	{
		int chain = 0;
		int max = 0;
		int count = 0;
		int iterMax = 0;
		for(String s:activeLoadStoreChains.keySet())
		{
			int iter = 0;
			HashSet<InsTypeChain> ins = activeLoadStoreChains.get(s);
			for(InsTypeChain inss:ins)
			{
				iter++;
				chain += inss.size();
				count++;
				if(max < inss.size())
				{
					max = inss.size();
				}
			}
			if(iterMax < iter)
			{
				iterMax = iter;
			}
		}
		return "totalChainData = "+chain+", MaxChainLen = "+max+" TotalChains = "+count+" MaxChainsForKey = "+iterMax;
	}
	/*static */int bb_count_stat = 0, strand_stat = 0, coprocChain = 0;
	public void addNext(BasicBlock bb, long tid)
	{
		instructions += bb.insCount;
		bb_count_stat++;
		if(tid != this.tid)
		{
			System.out.println("failing sanity check! tid mismatch: this = "+this.tid+" != "+tid);
		}
		if(postProcessed)
		{
			System.out.println("trying to add strand after post processing!!");
			System.exit(0);
		}
		for(InsTypeChain ins:bb.myStrands)
		{
			strand_stat++;
			//idealize(ins);
			//if(1==1)continue;
			HashSet<InsTypeChain> current = activeLoadStoreChains.get(ins.getSource());
			if(current == null)
			{
				if(ins.isIdeal())
				{
					idealize(ins);
				}
				current = new HashSet<>();
				//System.out.println("storing"+ins);
				activeLoadStoreChains.put(ins.getDestination(), current);
				current = activeLoadStoreChains.get(ins.getDestination());
				if(current == null)
				{
					current = new HashSet<>();
					activeLoadStoreChains.put(ins.getDestination(), current);
				}
				pickAllLoads(ins, current);
				//ins = ins.copyFromLoad();
				//if(ins != null)
				//{
				//	current.add(ins);
				//}
			}
			else
			{
				addTillStore(ins, current);
				current = activeLoadStoreChains.get(ins.getDestination());
				if(current == null)
				{
					current = new HashSet<>();
					activeLoadStoreChains.put(ins.getDestination(), current);
				}
				pickAllLoads(ins, current);
			}
			//InsTypeChain current = destinationStrandMap.get(ins.getSource());
			//if(current == null)
			//{
			//	saveNew(ins);
			//}
			//else
			//{
			//	current.addToChain(ins);
			//}
			//System.out.println(activeLoadStoreChains.size()+" addnext adds to "+current.size()+" "+findSizeOf());
			if(ins.isCoprocBasedChain)
			{
				coprocChain++;
			}
		}
		if(bb_count_stat % 1000 == 0)
		{
			if(bb_count_stat % 10000 == 0)
			{
				System.out.println("completed bb number: "+bb_count_stat+" "+strand_stat+" "+coprocChain);
			}
			if(bb_count_stat >= 4000)
			{
				//System.out.println(this.toString());
				toString();
				this.clearAll();
			}
		}
	}
	volatile HashSet<String> printableHasher = new HashSet<String>();
	public String translateKey(InsTypeChain ins)
	{
		String key = ins.toString();
		if(key.length() > 10)
		{
			key = ""+key.hashCode();
		}
		return key;
	}
	public boolean notAlreadyPrinted(InsTypeChain ins)
	{
		String key = ins.toString();
		if(key.length() > 10)
		{
			key = ""+key.hashCode();
		}
		//boolean ret = (printableHasher.contains(key));
		return printableHasher.add(key);
		//return ret;
	}
	/*static*/ int idealize_counter = 0;
	/*static*/ int max_count = 0;
	public void idealize(InsTypeChain ins)
	{
		if(!ins.isIdeal())return;
		if(!tracked.canGo(ins.getSubString()))
		{
			//System.out.println("tracked says no!"+ins);
			return;
		}
		if(ins.size() > 1000)return;
		//if(notAlreadyPrinted(ins))
		//{
		//	System.out.println(ins+","+ins.getStartingPC()+" 1");
		//}
		//if(1==1)return;
		idealize_counter++;
		String keyTranslated = translateKey(ins);
		HashMap<String, Integer> iCount = completedLoadStoreChains.get(ins.toString());
		if(iCount == null)
		{
			iCount = completedLoadStoreChains.get(keyTranslated);
			if(iCount == null)
			{
				iCount = new HashMap<String, Integer>();
				completedLoadStoreChains.put(keyTranslated, iCount);
			}
		}
//		else
//		{
//			InsTypeChain prev = insChainToKey.get(ins.toString());
//			if(prev != null)
//			{
//				//System.out.println(ins.lineNumberOccurrence+" "+iCount);
//				ins.lineNumberOccurrence.addAll(prev.lineNumberOccurrence);
//				System.out.println(prev.lineNumberOccurrence+" "+ins.lineNumberOccurrence+" "+(iCount+1));
//			}
//			else //if(prev == null)
//			{
//				System.out.println("inschain to key is not good!");
//				System.exit(0);
//			}
//		}
		Integer intCount = iCount.get(ins.getStartingPC());
		if(intCount == null)
		{
			intCount = 0;
		}
		intCount += 1;
		if(max_count < intCount)
		{
			max_count = intCount;
		}
		if(max_count == intCount)
		{
			//System.out.println(iCount+" = "+ins);
		}
		//if(ins.toString().startsWith(GetSrcDest.toCheckString))
		//{
		//	//System.out.println(ins.isCoprocBasedChain+" "+ins.myChainDefinition.get(0).verbose()+" "+this.tid+" "+ins.myChainDefinition.get(ins.myChainDefinition.size()-1).verbose());
		//	System.out.println("***"+tid);
		//	ins.verbosePrint();
		//	System.exit(0);
		//}
		//if(1==1)return;
		if(idealize_counter % 500 == 0)
		{
			//System.out.println(completedLoadStoreChains.size());
		}
		//int i = 1;
		//if(completedLoadStoreChains.size() > 1500)
//REMOVE THIS COMMENT TO REMOVE SOME INS CHAINS FROM POLLUTIONS
		//for(int i = 1; i <= 150 && completedLoadStoreChains.size() > 1500; i++)
		//{
		//	//int max = 1000000;
		//	//String key = "";
		//	//for(String s: completedLoadStoreChains.keySet())
		//	//{
		//	//	if(completedLoadStoreChains.get(s) < max)
		//	//	{
		//	//		max = completedLoadStoreChains.get(s);
		//	//		key = s;
		//	//	}
		//	//}
		//	//completedLoadStoreChains.remove(key);
		//	if(completedLoadStoreChains.containsValue(i))
		//	{
		////		for(Map.Entry<String, Integer> ent: completedLoadStoreChains.entrySet())
		////		{
		////			if(ent.getValue() == i)
		////			{
		////				insChainToKey.remove(ent.getKey());
		////			}
		////		}
		//		while(completedLoadStoreChains.values().remove(i)){
		//		}
		//	}
		//}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////REMOVE TILL HERE
		iCount.put(ins.getStartingPC(), intCount);
		//completedLoadStoreChains.put(ins.toString(), iCount);
		int iCountSum = 0;
//		int size = ins.size();
		for(String s:iCount.keySet())
		{
			iCountSum += iCount.get(s);
		}
		if(iCountSum == THRESHOLD+1)
		{
			//System.out.println(ins.toString()+"\t"+THRESHOLD);
			for(String s: iCount.keySet())
			{
				System.out.println(ins.toString()+"\t"+iCount.get(s)+"\n"+s+" haiboinst");
			}
			//PRAS: REMOVE THIS::::
			//Since icount already holds threshold, clear both icount and completed loadstore chains..
			completedLoadStoreChains.remove(keyTranslated);
			completedLoadStoreChains.put(ins.toString(), iCount);
			//ins.verbosePrint();
		}
//		HashSet<String> hsNow = completedLoadStoreBBPCs.get(ins.toString());
//		if(hsNow == null)
//		{
//			hsNow = new HashSet<String>();
//		}
//		hsNow.add(ins.myChainDefinition.get(0).pc);
//		completedLoadStoreBBPCs.put(ins.toString(), hsNow);
//		//if(iCount > 50)
//		{
//	//		insChainToKey.put(ins.toString(), ins);
//		}
	}
	public void pickAllLoads(InsTypeChain ins, HashSet<InsTypeChain> current)
	{
		HashSet<InsTypeChain> removes = new HashSet<>();
		while(ins != null)
		{
			ins = ins.copyFromLoad();
			if(ins == null) break;
			current.add(ins);
			idealize(ins);
			ins.myChainDefinition.remove(0);
			if(current.size() > 1000)
			{
				InsTypeChain toRemove = current.iterator().next();
				for(InsTypeChain insT:current)
				{
					if(insT.size() > toRemove.size())
					{
						toRemove = insT;
					}
				}
				current.remove(toRemove);
			}
		}
	}
	public void addTillStore(InsTypeChain ins, HashSet<InsTypeChain> current)
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
					idealize(nu);
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
	HashSet<String> homoInsChains = new HashSet<>();//J
	public void collectStatsFrom(InsTypeChain prev)
	{
		Integer i = prevMaintains.get(prev.toString());
		if(i == null)
		{
			i = 0;
		}
		i = i+1;
		prevRawChains.add(prev);
		if(longestStrand == null || longestStrand.size() < prev.size())
		{
			longestStrand = prev;
		}
//		String prev_s = "";
//		boolean homoIns = true;//r
//		for(ChainData cd:prev.myChainDefinition)
//		{
//			String now = cd.toString();
//			if(prev_s.equals(now))
//			{
//				Integer it_p = homoChains.get(prev_s);
//				if(it_p == null)
//				{
//					it_p = 0;
//				}
//				it_p += 1;
//				homoChains.put(prev_s, it_p);
//			}
//			else
//			{
//				if(!prev_s.equals(""))
//				{
//					homoIns = false;
//				}
//			}
//			prev_s = now;
//		}
//		if(homoIns)
//		{
//			homoInsChains.add(prev.toString());
//			//System.out.println(prev);
//		}
		totalDependenceEdges += (prev.size() - 1);
		prevMaintains.put(prev.toString(), i);
	}
	public void clearAll()
	{
		destinationStrandMap.clear();
		prevMaintains.clear();
		prevRawChains.clear();
		//homoChains.clear();
		activeLoadStoreChains.clear();
		completedLoadStoreChains.clear();
		//insChainToKey.clear();
	}
	public String toString()
	{
		boolean flag = true;

		List<Map.Entry<String, HashMap<String, Integer>>> list = new LinkedList<>( completedLoadStoreChains.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<String, HashMap<String, Integer>>>()
		{
			@Override
			public int compare( Map.Entry<String, HashMap<String, Integer>> o1, Map.Entry<String, HashMap<String, Integer>> o2 )
			{
				HashMap<String, Integer> o1_tL = o1.getValue();
				HashMap<String, Integer> o2_tL = o2.getValue();
				int i1 = 0, i2 = 0;
				for(String s:o1_tL.keySet())
				{
					i1 += o1_tL.get(s);
				}
				for(String s:o2_tL.keySet())
				{
					i2 += o2_tL.get(s);
				}
				return (i2-i1);
				//return -1*((o1_tL).compareTo(o2_tL));
				//return -1*((o1.getValue()).compareTo( o2.getValue() ));
			}
		} );
		for(Map.Entry<String, HashMap<String, Integer>> ent: list)
		{
			//System.out.println(ent.getKey()+" "+ent.getKey().split(",").length+" > "+GetSrcDest.totalInstructions);
			//if(ent.getValue() * (ent.getKey().split(",").length-3) > GetSrcDest.totalInstructions*0.01 && ent.getValue() > 50)
			HashMap<String, Integer> hmap = ent.getValue();
			int total = 0;
			int leng = ent.getKey().split(",").length - 2;
			for(String s:hmap.keySet())
			{
				total += hmap.get(s);
			}
			if(total > THRESHOLD || total * leng * 100.0 / instructions > THRESHOLD_PERC)
			{
				if(flag)
				{
					System.out.println("top strands: (total = "+completedLoadStoreChains.size()+"):"+instructions);
					flag = false;
				}
				for(String s:hmap.keySet())
				{
					System.out.println(ent.getKey()+","+s+","+tid+""+"\t"+hmap.get(s));
				}
				myFinalStrands.put(ent.getKey(), total);//ent.getValue()+","+LightStrander.tidStringTracker.get(tid+"")+","+tid+""+"\t"+ent.getValue());
				//insChainToKey.get(ent.getKey()).verbosePrint();
			}
			else
			{
				//mark for deleting?
			}
		}
		boolean deleter = false;
		do
		{
			deleter = false;
			for(String s:completedLoadStoreChains.keySet())
			{
				HashMap<String, Integer> hmap = completedLoadStoreChains.get(s);
				int total = 0;
				for(String ss_s:hmap.keySet())
				{
					total += hmap.get(ss_s);
				}
				if(total <= THRESHOLD)
				{
					completedLoadStoreChains.remove(s);
					//insChainToKey.remove(s);
					deleter = true;
					break;
				}
			}
		}while(deleter);
		return ""+flag;//"*****************"+tid;//strb.toString();
	}
	public String toStringOld()
	{
		if(!postProcessed)postProcess();
		List<Map.Entry<String, Integer>> list = new LinkedList<>( prevMaintains.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<String, Integer>>()
		{
			@Override
			public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
			{
				Integer o1_tL = o1.getValue()*(o1.getKey().split(", ",-1).length-1);
				Integer o2_tL = o2.getValue()*(o2.getKey().split(", ",-1).length-1);
				return -1*((o1_tL).compareTo(o2_tL));
				//return -1*((o1.getValue()).compareTo( o2.getValue() ));
			}
		} );
		System.out.println("top strands:\n");
		System.out.println(" # dependences: "+totalDependenceEdges+" Longest Chain: "+longestStrand.size());;
			//.append(longestStrand).append("\n");
		//for(String al: prevMaintains.keySet())
		for(Map.Entry<String, Integer> ent: list)
		{
			String al = ent.getKey();
			int leng = (ent.getKey().split(", ",-1).length-1);
			int trueLength = ent.getValue()*leng;
			if(trueLength*100.0/totalDependenceEdges > 1.0 )//&& !homoInsChains.contains(al)
			//if(prevMaintains.get(al) > 1)
			{
				if(leng <= 12000000000000l)
				{
					System.out.print(al);
				}
				else
				{
					System.out.print("["+leng+"]");
				}
				System.out.println(" = "+ent.getValue()+" = "+trueLength+" = "+(trueLength*100.0/totalDependenceEdges));
			}
		}
		//for(String s:homoChains.keySet())
		//{
		//	strb.append(s).append(" = ").append(homoChains.get(s)*100.0/totalDependenceEdges).append("\n");
		//}
		return tid+"***************\n";
	}
}
