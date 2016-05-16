import java.util.*;
import java.util.Map.Entry;
public class MasterChain
{
	ArrayList<String> superString = new ArrayList<>();
	ArrayList<Integer> superInteger = new ArrayList<>();
	String stringS = "";
	static int totalCounter = 0;
	public int getArrayIndex(int stringIndex)
	{
		int counter = 0, len = 0;
		for(String s:superString)
		{
			len += s.length()+2;
			if(len >= stringIndex)
			{
				return counter;
			}
			counter++;
		}
		return counter % superString.size();
	}
	public double formWith(String one, GroupConcise gcon)
	{
		int insMatched = 0;
		String now = one.substring(1);//, one.indexOf("]"));
		String arr[] = now.split(",");
		int additionValue = gcon.numInsSaved/arr.length; 
		int nowIndex = stringS.indexOf(now);
		if(nowIndex!=-1)
		{
//			System.out.println("awesome");
			for(String s:arr)
			{
				s = s.trim();
				int index = getArrayIndex(stringS.indexOf(s, nowIndex-1));
//				System.out.println("arrayindex = "+index+", string = "+stringS+": s = "+s+": ind = "+stringS.indexOf(s));
				Integer in = superInteger.get(index);
				superInteger.set(index, in+additionValue);
			}
			return 1.0;
		}
		if(superString.size() == 0)
		{
			for(String s:arr)
			{
				superString.add(s.trim());
				superInteger.add(additionValue);
			}
			stringS = superString.toString();
			return 1.0;
		}
		StringBuilder strb = new StringBuilder("");
		StringBuilder prev = new StringBuilder("");
		int countMatched = 0;
		int indexToContinue = 0;
		int myCurrentIndex = 0;
		int prevIndex = 0;
		for(String s:arr)
		{
			s = s.trim();
			prev = strb;
			prevIndex = myCurrentIndex;
			strb.append(s.trim());
			myCurrentIndex = stringS.indexOf(strb.toString(), indexToContinue);
			if(myCurrentIndex == -1)
			{
				//1. find all index of prev
				//2. insert cost = number of chains benefitting - number of chains that has to jump this instruction inserted.
				//3. find where the insert cost is minimum.
				//4. all the remaining instructions are to be matched only in the place after this current insert.
				//System.out.println("not awesome!"+strb);
				strb = new StringBuilder(s.trim());
				indexToContinue = stringS.indexOf(prev.toString(), indexToContinue)+prev.length();
//				System.out.println("vetting to "+ indexToContinue);
				myCurrentIndex = stringS.indexOf(strb.toString(), indexToContinue);
				if(myCurrentIndex == -1)
				{
					int currIndex = getArrayIndex(prevIndex+prev.length());
					superString.add(currIndex, s.trim());
					superInteger.add(currIndex, additionValue);
					stringS = superString.toString();
					System.out.println("ins not found... adding in the middle");
				}
				/*
				 *
				 *dynamic programming as to where to insert the missing string: as of now, insert in the end.
				 *
				 * **/
				break;
			}
			strb.append(", ");
			if(myCurrentIndex == -1)
			{
				System.out.println("wrong?"+myCurrentIndex);
				System.exit(0);
			}
			int arrIndex = getArrayIndex(myCurrentIndex);
			Integer in = superInteger.get(arrIndex);
			in += additionValue;
		//	if(in > additionValue && !(s.equals("ld") || s.equals("st")))
		//	{
		//		System.out.println(s+" "+in);
		//	}
			superInteger.set(arrIndex, in);
			countMatched++;
		}
		if(superString.size() != superInteger.size())
		{
			System.out.println(superString.size()+"<><====><>"+superInteger.size());
		}
		return countMatched;
	}
	public void formAll(String f, Map<String, GroupConcise> allStrands)
	{
		System.out.println("creating master chain!");
		List<Entry<String, GroupConcise>> list = new ArrayList<Entry<String, GroupConcise>>(allStrands.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, GroupConcise>>() {
				public int compare(Map.Entry<String, GroupConcise> o1, Map.Entry<String, GroupConcise> o2) {
				return o2.getKey().split(",").length - o1.getKey().split(",").length;
				//return o2.getKey().substring(0, o2.getKey().indexOf("]")).split(",").length - o1.getKey().substring(0, o1.getKey().indexOf("]")).split(",").length;
				}
			});
		System.out.println("sorting done: "+list.iterator().next());
		int totalCoverage = 0;
		for(Entry<String, GroupConcise> entry:list)
		{
			formWith(entry.getKey(), entry.getValue());
			totalCoverage += entry.getValue().numInsSaved;
		}
		System.out.println(superString+"\t"+superString.size()+"\t"+(totalCoverage*100.0/totalCounter));
		for(int i = 0; i < superString.size(); i++)
		{
			System.out.print(superString.get(i)+"="+superInteger.get(i)+", ");
		}
		System.out.println(superString.size()+"<><====><>"+superInteger.size());
	}
	public MasterChain(String app, Map<String, GroupConcise> allStrands)
	{
		formAll(app, allStrands);
	}
}
