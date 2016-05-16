import java.io.*;
import java.util.*;
public class ProcessMemoInfo
{
	ArrayList<ArrayList<ArrayList<Long>>> lines = new ArrayList<>();
	int max_length_in = 0, max_length_out = 0;
	ArrayList<Long> minima = new ArrayList<>();
	ArrayList<Long> maxima = new ArrayList<>();
	ArrayList<Integer> uniq = new ArrayList<>();
	HashSet<String> uniqueInputs = new HashSet<>();
	HashSet<String> uniqueCacheBlocks = new HashSet<>();
	HashSet<Long> strideMagnitudes = new HashSet<>();
	ArrayList<String> toPrint = new ArrayList<String>();
	static TreeMap<Long, Integer> histo = new TreeMap<>();
	public static void main(String args[])throws Exception
	{
		boolean printIters = true;//false;
		String line = "";
		int linesRead = 0;
		Hashtable<String, Hashtable<String, ProcessMemoInfo>> database = new Hashtable<>();
		//BufferedReader br = new BufferedReader(new FileReader("/i3c/hpcl/huz123/spark/java_example_pras/snippers/memo_dumps"));
		BufferedReader br = new BufferedReader(new FileReader("/i3c/hpcl/huz123/spark/java_example_pras/snippers/"+args[0]+"_memo_detailed_dump"));
		while((line = br.readLine())!=null)
		{
			linesRead++;
			if(!line.startsWith("data line:"))continue;
			try
			{
				String strand = line.substring(0, line.indexOf("["));
				String pcval = line.substring(line.indexOf("["), line.lastIndexOf("]"));
				Hashtable<String, ProcessMemoInfo> pc_dat = database.get(pcval);
				if(pc_dat == null)
				{
					pc_dat = new Hashtable<>();
					database.put(pcval, pc_dat);
				}
				ProcessMemoInfo pmi = pc_dat.get(strand);
				if(pmi == null)
				{
					pmi = new ProcessMemoInfo();
					pc_dat.put(strand, pmi);
				}
				//pmi.use(line.substring(line.indexOf("{")+1, line.indexOf("}")));
				pmi.use(line.substring(line.indexOf(") : ")+4).trim());
			}
			catch(Exception e)
			{
				System.out.println(line+" "+linesRead);
				e.printStackTrace();
				System.exit(0);
			}
		}
		br.close();
		for(String s1:database.keySet())
		{
			if(printIters)System.out.println(s1);
			List<Map.Entry<String, ProcessMemoInfo>> list = new LinkedList<>( database.get(s1).entrySet() );
			Collections.sort( list, new Comparator<Map.Entry<String, ProcessMemoInfo>>()
			{
				@Override
				public int compare( Map.Entry<String, ProcessMemoInfo> o1, Map.Entry<String, ProcessMemoInfo> o2 )
				{
					ProcessMemoInfo o1_tL = o1.getValue();
					ProcessMemoInfo o2_tL = o2.getValue();
					int i1 = 0, i2 = 0;
					o1_tL.putAllNumbers();
					o2_tL.putAllNumbers();
					return (o1_tL.uniqueCacheBlocks.size() - o2_tL.uniqueCacheBlocks.size());
					//return -1*((o1_tL).compareTo(o2_tL));
					//return -1*((o1.getValue()).compareTo( o2.getValue() ));
				}
			} );
			int totalRepeats = 0, totalStrides = 0;
			for(Map.Entry<String, ProcessMemoInfo> entry:list)//database.get(s1).keySet())
			{
				String s2 = entry.getKey();
				if(database.get(s1).get(s2).lines.size() > 0)
				{
					if(printIters)
					{
					//	System.out.print("\t"+s2+"\t");
					}
					ProcessMemoInfo pim = database.get(s1).get(s2);
					if(printIters)
					{
						totalStrides += pim.strideMagnitudes.size();
						totalRepeats += pim.lines.size()*pim.max_length_in;
//						System.out.println(pim.uniqueInputs.size()+"\t"+pim.uniqueCacheBlocks.size()+"\t"+pim.lines.size()+"\t"+(pim.strideMagnitudes.size()+"\t"+pim.max_length_in));//(pim.uniqueCacheBlocks.size()>1?pim.toPrint:""));
					}
				}
			}
			if(printIters)
			{
				System.out.println(totalStrides*100.0/totalRepeats);
			}
		}
		if(printIters)System.exit(0);
		System.out.println("**** HISTO ****");
		int temp = 0;
		for(Long s:histo.keySet())
		{
			temp+=histo.get(s);
		}
		int run = 0;
		TreeMap<Float, Long> percOrder = new TreeMap<>();
		for(Long s:histo.keySet())
		{
			float f = histo.get(s)*100.0f/temp;
			percOrder.put(-f, s);
			run += histo.get(s);
			//System.out.println(s+"\t"+run*100.0/temp);
		}
		float runf = 0;
		int count = 0;
		for(Float f:percOrder.keySet())
		{
			runf += (-f);
			System.out.println(percOrder.get(f)+"\t"+runf);
			if(count++ == 25)break;
		}
	}
	public void use(String io)
	{
		String spl[] = io.split(",");
		for(String tup:spl)
		{
			ArrayList<ArrayList<Long>> line = new ArrayList<>();
			line.add(new ArrayList<Long>());
			line.add(new ArrayList<Long>());
			tup = tup.trim();
			if(tup.startsWith("|^|"))continue;
			if(tup.indexOf("|^|")==-1)continue;
//			if(tup.startsWith("size")||tup.startsWith("oFreq")||tup.startsWith("length"))continue;
//			System.out.println(tup.indexOf("|^|")+"   "+tup);
			uniqueInputs.add(tup.substring(0, tup.indexOf("|^|")));
			String [][]iobreak = new String[][]{
							tup.substring(0, tup.indexOf("|^|")).split("_"),
							//tup.substring(tup.indexOf("|^|")+3, tup.indexOf("=")).split("_")
							tup.substring(tup.indexOf("|^|")+3).split("_")
							};
			//for(String sar[]:iobreak)
			for(int i = 0; i < 2; i++)
			{
				String sar[] = iobreak[i];
				ArrayList<Long> iter = line.get(i);
				for(String sa:sar)
				{
					sa = sa.replaceAll("=","");
					if(sa.trim().length() == 0)continue;
					if(sa.equals("null"))
					{
						iter.add(0L);
					}
					else
					{
						try
						{
							iter.add(Long.parseLong(sa, 16));//+"\t";
						}
						catch(Exception e)
						{
							if(sa.indexOf(".")!=-1)
							{
								iter.add((long)Double.parseDouble(sa));//+"\t";
							}
							else
							{
								e.printStackTrace();
								System.exit(0);
							}
						}
					}
				}
				//line+="1o_o1\t";
			}
			if(line.get(0).size() > max_length_in)
			{
				max_length_in = line.get(0).size();
			}
			if(line.get(1).size() > max_length_out)
			{
				max_length_out = line.get(1).size();
			}
			lines.add(line);
		}
	}
	boolean putAllCalled = false;
	public void putAllNumbers()
	{
		if(putAllCalled)return;
		putAllCalled = true;
		for(int i = 0; i < max_length_in; i++)
		{
			HashSet<Long> uniques = new HashSet<>();
			long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
			for(ArrayList<ArrayList<Long>> al:lines)
			{
				if(al.get(0).size() > i)
				{
					long l = al.get(0).get(i);
					uniques.add(l);
					if(l < min)
					{
						min = l;
					}
					if(l > max)
					{
						max = l;
					}
				}
			}
			minima.add(min);
			maxima.add(max);
			uniq.add(uniques.size());
		}
		minima.add(-1L);
		maxima.add(-1L);
		uniq.add(-1);
		for(int i = 0; i < max_length_out; i++)
		{
			HashSet<Long> uniques = new HashSet<>();
			long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
			for(ArrayList<ArrayList<Long>> al:lines)
			{
				if(al.get(1).size() > i)
				{
					long l = al.get(1).get(i);
					uniques.add(l);
					if(l < min)
					{
						min = l;
					}
					if(l > max)
					{
						max = l;
					}
				}
			}
			uniq.add(uniques.size());
			minima.add(min);
			maxima.add(max);
		}
//		for(Long l:minima)
//		{
//			System.out.print(l+"\t");
//		}
//		System.out.println();
//		for(Long l:maxima)
//		{
//			System.out.print(l+"\t");
//		}
//		System.out.println();
//		for(Integer l:uniq)
//		{
//			System.out.print(l+"\t");
//		}
//		System.out.println();
		getMoreFrom(uniqueInputs);
		//PRAS Uncomment this:
		//System.out.println("\t"+uniqueInputs.size()+"\t"+getMoreFrom(uniqueInputs)+"\t"+lines.size()+"\t"+toPrint+"\t");//+uniqueCacheBlocks);
		if(1==1)
			return;
		for(int i = 1; i < lines.size(); i++)
		{
			int j = 0;
			for(j = 0; j < lines.get(i).get(0).size() && j < lines.get(i-1).get(0).size(); j++)
			{
				long curr = lines.get(i).get(0).get(j);
				long prev = lines.get(i-1).get(0).get(j);
				System.out.print((curr - prev)+"\t");
				//float divider = (maxima.get(j) - minima.get(j))/10.0f;
				//long curr = lines.get(i).get(0).get(j);
				//System.out.print(((curr - minima.get(j))/divider)+"\t");
			}
			System.out.print("out\t");
			for(j = 0; j < lines.get(i).get(1).size() && j < lines.get(i-1).get(1).size(); j++)
			{
				try
				{
					long curr = lines.get(i).get(1).get(j);
					long prev = lines.get(i-1).get(1).get(j);
					System.out.print((curr - prev)+"\t");
				}
				catch(Exception e)
				{
					System.out.println(i+" "+j);
					e.printStackTrace();
					System.exit(0);
				}
				//float divider = (maxima.get(j+max_length_in+1) - minima.get(j+max_length_in+1))/10.0f;
				//long curr = lines.get(i).get(1).get(j);
				//System.out.print(((curr - minima.get(j+max_length_in+1))/divider)+"\t");
			}
			System.out.println();
		}
	}
	public int getMoreFrom(HashSet<String> uniq)
	{
		ArrayList<Long> prev = new ArrayList<Long>();
		for(String s:uniq)
		{
			String []ar = s.split("_");
			String it = "", stride = "";
			ArrayList<Long> now = new ArrayList<Long>();
			for(String ss:ar)
			{
				try
				{
				//	if(ss.equals("null")||ss.trim().length() == 0)
				//	{
				//		it += "0_";
				//	}
					if(ss.length() == 8 && ss.indexOf(".")==-1)
					{
						//System.out.println(ss+" "+Long.parseLong(ss, 16)+" "+(Long.parseLong(ss, 16)>>6));
						long hex = (0xff_ff_ff_ffl & (Long.parseLong(ss, 16)>>6));
						now.add(hex);
						long smallestDelta = hex;
						long smallGuy = 1000_000_00l;
						for(Long l:prev)
						{
							if(smallestDelta > Math.abs(l-hex))
							{
								smallestDelta = Math.abs(l-hex);
								smallGuy = l;
							}
						}
						if(1000_000_00l == smallGuy)
						{
							it+= hex+"_";
							stride += hex+"_";
						}
						else
						{
							it += Long.toHexString(hex)+"+";
							//it+="["+"+"+(smallGuy - hex)+"]_";//Long.toHexString(hex)+"+";
							stride += "[+"+(smallGuy-hex)+"]";
							Integer i = histo.get(smallGuy - hex);
							if(i == null)
							{
								i = 0;
							}
							histo.put(smallGuy - hex, i+1);
							strideMagnitudes.add(Math.abs(smallGuy-hex));
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			if(!it.equals(""))
			{
				uniqueCacheBlocks.add(it);
			}
			prev = now;
			if(!(stride.equals("")||(toPrint.size()>0&&toPrint.get(toPrint.size()-1).equals(stride))))
			{
				toPrint.add(stride);
			}
		}
		return uniqueCacheBlocks.size();
	}
}
