package main.java;
import java.util.*;
import java.io.*;
public class StrandMaintainer implements Serializable//<StrandMaintainer>
{
	public int THRESHOLD = 20;//THE TOP HALF COUNTER WILL TAKE CARE OF THE LIMITATIONS
	public static final int THRESHOLD_1000 = 250;
	public static final byte THRESHOLD_PERC = 5;
	//public static final boolean PRINT_ALL_STRANDS = true;
	public static final boolean PRINT_ALL_STRANDS = false;
	//ORIGINAL PRAS: public static final byte THRESHOLD = 20;
	TrackedStrands tracked = null;
	HashMap<String, Integer> prevMaintains;
	ArrayList<InsTypeChain> prevRawChains;
	HashMap<String, HashSet<InsTypeChain>> activeLoadStoreChains = new HashMap<>();
	HashMap<String, HashMap<String, Integer>> completedLoadStoreChains = new HashMap<>();
	boolean postProcessed = false;
	InsTypeChain longestStrand = null;
	int totalDependenceEdges = 0;
	long tid = -1;
	TreeMap<String, Integer> myFinalStrands;
	/**BloatedInstructions count the number of dependence edges + [single noded graph] in the dependence graphs
	 * */
	int instructions = 0, bloatedInstructions = 0;
	long maxMem = Runtime.getRuntime().maxMemory();
	private void readObject(ObjectInputStream in)throws IOException, ClassNotFoundException
	{
		prevMaintains = (HashMap<String, Integer>)in.readObject();
		prevRawChains = (ArrayList<InsTypeChain>)in.readObject();
		activeLoadStoreChains = (HashMap<String, HashSet<InsTypeChain>>)in.readObject();
		completedLoadStoreChains = (HashMap<String, HashMap<String, Integer>>)in.readObject();
		longestStrand = (InsTypeChain)in.readObject();
		postProcessed = (Boolean)in.readObject();
		totalDependenceEdges = (Integer)in.readObject();
		tid = (Long)in.readObject();
		myFinalStrands = (TreeMap<String, Integer>)in.readObject();
		tracked = (TrackedStrands)in.readObject();
	}
	private void writeObject(ObjectOutputStream out)throws IOException
	{
		out.writeObject(prevMaintains);
		out.writeObject(prevRawChains);
		out.writeObject(activeLoadStoreChains);
		out.writeObject(completedLoadStoreChains);
		out.writeObject(longestStrand);
		out.writeObject((Boolean)postProcessed);
		out.writeObject((Integer)totalDependenceEdges);
		out.writeObject((Long)tid);
		out.writeObject(myFinalStrands);
		out.writeObject(tracked);
	}
	public StrandMaintainer(long tid, TrackedStrands tracked)
	{
		this.tracked = tracked;
		this.tid = tid;
		prevMaintains = new HashMap<>();
		prevRawChains = new ArrayList<>();
		myFinalStrands = new TreeMap<String, Integer>();
	}
	public void addAllChains(StrandMaintainer sm)
	{
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
			for(String s_s:intsm.keySet())
			{
				Integer intMe = intnow.get(s_s);
				if(intMe == null)
				{
					intMe = 0;
				}
				intnow.put(s_s, intMe+intsm.get(s_s));
			}
		}
	}
	public void clearMore()
	{
		System.out.println(completedLoadStoreChains.size()+" "+findSizeOf()+" "+activeLoadStoreChains.keySet());
		if(1==1)return;	
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
		//:return "totalChainData = "+chain+", MaxChainLen = "+max+" TotalChains = "+count+" MaxChainsForKey = "+iterMax;
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

		for(InsTypeChain insNow:bb.myStrands)
		{
			strand_stat++;
			if(insNow.isCoprocBasedChain)
			{
				coprocChain++;
			}
			//Form Chains Starting From those loads and index it by their destinations
			//For Others: Just Try Appending and forming:
			for(ChainData cd:insNow.myChainDefinition)
			{

				String sTt = cd.operandName;
				if(sTt.indexOf("-68")!=-1 || sTt.indexOf("112")!=-1 || sTt.indexOf("-60")!=-1 || sTt.indexOf("40")!=-1 || sTt.indexOf("80")!=-1 || sTt.indexOf("-54")!=-1)
				{
					System.out.println(cd.comments+" +++===== "+sTt+" "+cd.operandName+" "+cd.inouts);
					System.exit(0);
				}
				switch(cd.ins_name)
				{
					case "ld":
						{
						InsTypeChain insAtLoad = new InsTypeChain(cd);
						bloatedInstructions++;
						HashSet<InsTypeChain> mySources = activeLoadStoreChains.get(cd.operandName);
						for(String myOuts:cd.inouts.get(1))
						{
							HashSet<InsTypeChain> myDest = activeLoadStoreChains.get(myOuts);
							if(myDest == null)
							{
								myDest = new HashSet<>();
								activeLoadStoreChains.put(myOuts, myDest);
								try
								{
									Integer.parseInt(myOuts);
									System.out.println(cd.comments+" ===== "+myOuts+" "+cd.operandName+" "+cd.inouts);
					System.exit(0);
								}
								catch(Exception e){}
							}
						}
						for(String myOuts:cd.inouts.get(1))
						{
							HashSet<InsTypeChain> myDest = activeLoadStoreChains.get(myOuts);
							if(myDest.equals(mySources))
							{
								for(InsTypeChain myInput:mySources)
								{
									myInput.addToChain(cd);
									bloatedInstructions++;
								}
								myDest.add(insAtLoad);
							}
							else
							{
								myDest.clear();
								myDest.add(insAtLoad);
								if(mySources != null)
								{
									for(InsTypeChain myInput:mySources)
									{
										InsTypeChain copy = myInput.getACopy();
										copy.addToChain(cd);
										bloatedInstructions++;
										myDest.add(copy);
									}
								}
							}
							//Now Do Some SPRUCING:SPRUCE//search-web-meta-tags
							//DELETE THE BIGGEST SET OF CHAINS!!!
							//TODO: Find whether this is necessary or not!
							while(myDest.size() > THRESHOLD_1000 * 0.9)
							{
								InsTypeChain deleter = myDest.iterator().next();
								for(InsTypeChain iter:myDest)
								{
									if(iter.bloatFactor > deleter.bloatFactor)
									{
										deleter = iter;
									}
								}
								myDest.remove(deleter);
							}
						}
						break;
						}
					case "st":
						{
						//idealize idealize idealize
						HashSet<InsTypeChain> mySources = activeLoadStoreChains.get(cd.operandName);
						if(mySources != null)
						{
							for(String myOuts:cd.inouts.get(1))
							{
								HashSet<InsTypeChain> myDest = activeLoadStoreChains.get(myOuts);
								if(myDest == null)
								{
									myDest = new HashSet<>();
									activeLoadStoreChains.put(myOuts, myDest);
								}
							}
							for(String myOuts:cd.inouts.get(1))
							{
								HashSet<InsTypeChain> myDest = activeLoadStoreChains.get(myOuts);
								for(InsTypeChain myInNow: mySources)
								{
									if(myDest.equals(mySources))
									{
										myInNow.addToChain(cd);
										bloatedInstructions++;
										idealize(myInNow);
									}
									else
									{
										InsTypeChain copy = myInNow.getACopy();
										copy.addToChain(cd);
										bloatedInstructions++;
										idealize(copy);
										myDest.add(copy);
									}
								}
								long freeMem = Runtime.getRuntime().freeMemory();
								double memPerc = (freeMem*1.0/maxMem);
//								System.out.println("memstats: "+memPerc+" "+freeMem+" "+maxMem);
								if(memPerc < 0.1)
								{
//									System.out.println("< 10%");
									toString();
									clearMore();
									THRESHOLD += 20;
									System.out.println("THRESHOLD too low:"+THRESHOLD);
								}
									//Now Do Some SPRUCING: ALL SPRUCINGs are taken care off?
									//DELETE THE BIGGEST SET OF CHAINS!!!
									//TODO: Find whether this is necessary or not!
									while(myDest.size() > THRESHOLD_1000 * 0.9)
									{
										InsTypeChain deleter = myDest.iterator().next();
										for(InsTypeChain iter:myDest)
										{
											if(iter.bloatFactor > deleter.bloatFactor)
											{
												deleter = iter;
											}
										}
										myDest.remove(deleter);
									}
							}
						}
						break;
						}
					default:
						{
						HashSet<InsTypeChain> mySources = activeLoadStoreChains.get(cd.operandName);
						if(mySources != null)
						{
							for(String myOuts:cd.inouts.get(1))
							{
								HashSet<InsTypeChain> myDest = activeLoadStoreChains.get(myOuts);
								if(myDest == null)
								{
									myDest = new HashSet<>();
									activeLoadStoreChains.put(myOuts, myDest);
								}
							}
							for(String myOuts:cd.inouts.get(1))
							{
								HashSet<InsTypeChain> myDest = activeLoadStoreChains.get(myOuts);
								for(InsTypeChain myInNow: mySources)
								{
									if(myDest.equals(mySources))
									{
										myInNow.addToChain(cd);
										bloatedInstructions++;
									}
									else
									{
										InsTypeChain copy = myInNow.getACopy();
										copy.addToChain(cd);
										bloatedInstructions++;
										myDest.add(copy);
									}
								}
								//Now Do Some SPRUCING:spruce
								//DELETE THE BIGGEST SET OF CHAINS!!!
								//TODO: Find whether this is necessary or not!
								//ANSWER: OBVIOUSLY IMPORTANT TO CLEAN THINGS UP :D
								while(myDest.size() > THRESHOLD_1000 * 0.9)
								{
									InsTypeChain deleter = myDest.iterator().next();
									for(InsTypeChain iter:myDest)
									{
										if(iter.bloatFactor > deleter.bloatFactor)
										{
											deleter = iter;
										}
									}
									myDest.remove(deleter);
								}
							}
						}
						break;
					}
				}
			}
		}
	//	System.out.println("bb completed!");
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
		if(key.length() > 30)
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
		if(PRINT_ALL_STRANDS)
		{
			System.out.println(ins);
		}
		if(ins.size() == 0)
		{
//			System.out.println("size = 0: "+ins);
			return;
		}
		if(!ins.isIdeal())
		{
//			System.out.println("not ideal: "+ins);
			return;
		}
		if(!tracked.canGo(ins.getSubString()))
		{
//			System.out.println("tracked says no!"+ins);
			return;
		}
		if(ins.size() > THRESHOLD_1000)
		{
			System.out.println("size =  "+ins.size());
			return;
		}
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
		{
//			System.out.println(iCount+"<<<<<<<<<< is not null!!!");
		}
		Integer intCount = iCount.get(ins.getStartingPC());
		if(intCount == null)
		{
			intCount = 0;
		}
		intCount += 1;
//		System.out.println("intCount became = "+intCount);
		if(max_count < intCount)
		{
			max_count = intCount;
		}
		if(max_count == intCount)
		{
//			System.out.println(iCount+" = "+ins);
		}
//		if(idealize_counter % 500 == 0)
		{
//			System.out.println(completedLoadStoreChains.size());
		}
		iCount.put(ins.getStartingPC(), intCount);
		int iCountSum = 0;
		for(String s:iCount.keySet())
		{
			iCountSum += iCount.get(s);
		}
		if(iCountSum == THRESHOLD+1)
		{
			//PRAS: THIS WAS A USEFUL THING EARLIER. NOW, not required??
			//TODO: CHECK THIS
//			for(String s: iCount.keySet())
//			{
//				System.out.println(ins.toString()+"\t"+iCount.get(s)+"\n"+s+" haiboinst");
//			}
			completedLoadStoreChains.remove(keyTranslated);
			completedLoadStoreChains.put(ins.toString(), iCount);
		}
	}
	public void clearAll()
	{
		//destinationStrandMap.clear();
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
		if(1==1)//SORTING OF 5000 odd elements is time consuming...
		{
			Collections.sort( list, new Comparator<Map.Entry<String, HashMap<String, Integer>>>()
			{
				@Override
				public int compare( Map.Entry<String, HashMap<String, Integer>> o1, Map.Entry<String, HashMap<String, Integer>> o2 )
				{
					HashMap<String, Integer> o1_tL = o1.getValue();
					HashMap<String, Integer> o2_tL = o2.getValue();
					int numIns1 = o1.getKey().length() - o1.getKey().replace(",","").length() - 4;
					int numIns2 = o2.getKey().length() - o2.getKey().replace(",","").length() - 4;
					int i1 = 0, i2 = 0;
					for(String s:o1_tL.keySet())
					{
						i1 += o1_tL.get(s);
					}
					for(String s:o2_tL.keySet())
					{
						i2 += o2_tL.get(s);
					}
					return (i2*numIns2-i1*numIns1);
					//return -1*((o1_tL).compareTo(o2_tL));
					//return -1*((o1.getValue()).compareTo( o2.getValue() ));
				}
			} );
		}
		int topHalfCounter = 0;
		HashMap<String, Integer> toDelete = new HashMap<>();
		for(Map.Entry<String, HashMap<String, Integer>> ent: list)
		{
			topHalfCounter ++;
		//	if(topHalfCounter > 100)
		//	{
		//		//just to break the large dumpings...
		//		break;
		//	}
			//System.out.println(ent.getKey()+" "+ent.getKey().split(",").length+" > "+GetSrcDest.totalInstructions);
			//if(ent.getValue() * (ent.getKey().split(",").length-3) > GetSrcDest.totalInstructions*0.01 && ent.getValue() > 50)
			HashMap<String, Integer> hmap = ent.getValue();
			int total = 0;
			int leng = ent.getKey().split(",").length - 2;
			for(String s:hmap.keySet())
			{
				total += hmap.get(s);
			}
			if(topHalfCounter < 100 && total > THRESHOLD || total * leng * 100.0 / instructions > THRESHOLD_PERC)// || topHalfCounter >= list.size()/2)
			{
				if(flag)
				{
					System.out.println("top strands: (total = "+completedLoadStoreChains.size()+","+bloatedInstructions+"):"+instructions);
					flag = false;
				}
				int pcCount = 0;
				if(hmap.size() <= 10)
				{
					boolean firstTime = true;
					for(String s:hmap.keySet())
					{
						if(firstTime)
						{
							System.out.println(ent.getKey()+","+s+","+tid+""+"\t"+hmap.get(s));
							firstTime = false;
						}
						else
						{
							System.out.println("same,"+s+","+tid+""+"\t"+hmap.get(s));
						}
					}
				}
				else
				{

					List<Map.Entry<String, Integer>> list_now = new LinkedList<>( hmap.entrySet() );
					if(1==1)//SORTING OF 5000 odd elements is time consuming...
					{
						Collections.sort( list_now, new Comparator<Map.Entry<String, Integer>>()
						{
							@Override
							public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
							{
								Integer o1_tL = o1.getValue();
								Integer o2_tL = o2.getValue();
								return (o2_tL-o1_tL);
								//return -1*((o1_tL).compareTo(o2_tL));
								//return -1*((o1.getValue()).compareTo( o2.getValue() ));
							}
						} );
						int printOnly = 10;
						int tempIterTotal = total;
						for(Map.Entry<String, Integer> enti:list_now)
						{
							printOnly --;
							if(printOnly == 0)
							{
								break;
							}
							if(printOnly == 9)
							{
								System.out.println(ent.getKey()+","+enti.getKey()+","+tid+""+"\t"+enti.getValue());
							}
							else
							{
								System.out.println("same,"+enti.getKey()+","+tid+""+"\t"+enti.getValue());
							}
							tempIterTotal -= enti.getValue();
						}
						if(tempIterTotal > 0)
						{
							System.out.println("same,"+"oth"+(hmap.size()-10)+","+tid+""+"\t"+tempIterTotal);
						}
					}
				}
				myFinalStrands.put(ent.getKey(), total);//ent.getValue()+","+LightStrander.tidStringTracker.get(tid+"")+","+tid+""+"\t"+ent.getValue());
				//insChainToKey.get(ent.getKey()).verbosePrint();
			}
			else
			{
				//mark for deleting?
				toDelete.put(ent.getKey(), total);
			}
		}
		if(toDelete.size() == completedLoadStoreChains.size())
		{
			THRESHOLD -= 10;
			if (THRESHOLD < 10)
			{
				THRESHOLD = 10;
			}
			System.out.println("THRESHOLD is too high!: "+THRESHOLD);
		}
		System.out.println("Going to delete");
		for(String s:toDelete.keySet())
		{
			completedLoadStoreChains.remove(s);
		}
		System.out.println("Deleting is slow!"+completedLoadStoreChains.size());
		return ""+flag;
	}
	public String toStringOld()
	{
//		if(!postProcessed)postProcess();
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
