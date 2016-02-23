package main.java;
import java.util.*;
public class StrandMaintainer
{
	HashMap<String, InsTypeChain > destinationStrandMap;
	//HashMap<String, ArrayList<InsTypeChain> > destinationStrandMap;
	HashMap<String, Integer> prevMaintains;
	ArrayList<InsTypeChain> prevRawChains;
	HashMap<String, Integer> subChains;
	HashMap<String, Integer> homoChains;
	HashMap<String, HashSet<InsTypeChain>> activeLoadStoreChains = new HashMap<>();
	HashMap<String, Integer> completedLoadStoreChains = new HashMap<>();
	boolean postProcessed = false;
	InsTypeChain longestStrand = null;
	int totalDependenceEdges = 0;
	long tid = -1;
	public StrandMaintainer(long tid)
	{
		this.tid = tid;
		prevMaintains = new HashMap<>();
		prevRawChains = new ArrayList<>();
		destinationStrandMap = new HashMap<>();
		subChains = new HashMap<>();
		homoChains = new HashMap<>();
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
	static int bb_count_stat = 0, strand_stat = 0, coprocChain = 0;
	public void addNext(BasicBlock bb, long tid)
	{
		bb_count_stat++;
		if(tid != this.tid)
		{
			System.out.println("failing sanity check!");
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
			if(ins.isCoprocBasedChain)
			{
				coprocChain++;
			}
		}
		if(bb_count_stat % 10000 == 0)
		{
			System.out.println("completed bb number: "+bb_count_stat+" "+strand_stat+" "+coprocChain);
		}
	}
	static int idealize_counter = 0;
	static int max_count = 0;
	public void idealize(InsTypeChain ins)
	{
		if(!ins.isIdeal())return;
		idealize_counter++;
		Integer iCount = completedLoadStoreChains.get(ins.toString());
		if(iCount == null)
		{
			iCount = 0;
		}
		iCount += 1;
		if(max_count < iCount)
		{
			max_count = iCount;
		}
		if(max_count == iCount)
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
		if(ins.size() > 1000)return;
		completedLoadStoreChains.put(ins.toString(), iCount);
		if(idealize_counter % 500 == 0)
		{
			//System.out.println(completedLoadStoreChains.size());
		}
		for(int i = 1; i <= 150 && completedLoadStoreChains.size() > 1500; i++)
		{
			//int max = 1000000;
			//String key = "";
			//for(String s: completedLoadStoreChains.keySet())
			//{
			//	if(completedLoadStoreChains.get(s) < max)
			//	{
			//		max = completedLoadStoreChains.get(s);
			//		key = s;
			//	}
			//}
			//completedLoadStoreChains.remove(key);
			completedLoadStoreChains.values().remove(i);
		}
	}
	public void pickAllLoads(InsTypeChain ins, HashSet<InsTypeChain> current)
	{
		while(ins != null)
		{
			ins = ins.copyFromLoad();
			if(ins == null) break;
			current.add(ins);
			idealize(ins);
			ins.myChainDefinition.remove(0);
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
		String prev_s = "";
		boolean homoIns = true;//r
		for(ChainData cd:prev.myChainDefinition)
		{
			String now = cd.toString();
			if(prev_s.equals(now))
			{
				Integer it_p = homoChains.get(prev_s);
				if(it_p == null)
				{
					it_p = 0;
				}
				it_p += 1;
				homoChains.put(prev_s, it_p);
			}
			else
			{
				if(!prev_s.equals(""))
				{
					homoIns = false;
				}
			}
			prev_s = now;
		}
		if(homoIns)
		{
			homoInsChains.add(prev.toString());
			//System.out.println(prev);
		}
		totalDependenceEdges += (prev.size() - 1);
		prevMaintains.put(prev.toString(), i);
	}
	public String toString()
	{
		System.out.println("top strands: (total = "+completedLoadStoreChains.size()+")");

		List<Map.Entry<String, Integer>> list = new LinkedList<>( completedLoadStoreChains.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<String, Integer>>()
		{
			@Override
			public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
			{
				Integer o1_tL = o1.getValue();
				Integer o2_tL = o2.getValue();
				return -1*((o1_tL).compareTo(o2_tL));
				//return -1*((o1.getValue()).compareTo( o2.getValue() ));
			}
		} );
		for(Map.Entry<String, Integer> ent: list)
		{
			//System.out.println(ent.getKey()+" "+ent.getKey().split(",").length+" > "+GetSrcDest.totalInstructions);
			//if(ent.getValue() * (ent.getKey().split(",").length-3) > GetSrcDest.totalInstructions*0.01 && ent.getValue() > 50)
			if(ent.getValue() > 50)
			{
				System.out.println(ent.getKey()+","+LightStrander.tidStringTracker.get(tid+"")+","+tid+""+"\t"+ent.getValue());
			}
		}
		return "*****************"+tid;//strb.toString();
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
