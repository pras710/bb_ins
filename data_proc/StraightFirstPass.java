import java.io.*;
import java.util.*;
public class StraightFirstPass
{
	static boolean insideBB = false;
	public static PrintWriter classifyMe(String line, PrintWriter bb, PrintWriter mem, PrintWriter garb)
	{
		if(line.startsWith("@@"))
		{
//			memPrint.append(line).append("|^|");
			return mem;
		}
		if(line.indexOf("Lookup")!=-1)
		{
			insideBB = false;
//			bb.print(memPrint.toString());
//			bb.print("+++");
//			memPrint = new StringBuilder("");
			return bb;
		}
		if(line.startsWith("before entering"))
		{
			bb.println(strb.append(line));
			bb.flush();
			strb = new StringBuilder("");
			insideBB = true;
//			regPrint.append(line).append("|^|");
//			bb.print("+++");
//			bb.print(memPrint.toString());//old bb accessed these mems: both loads and stores
//			bb.print("+++");
//			bb.print(regPrint);//old bb changed these regs
//			bb.println();//new bb starts
//			memPrint = new StringBuilder("");
			//bb.print(regPrint.append("+++").toString());//
			return mem;
		}
		if(insideBB)
		{
			insideBB = (line.indexOf("*end*") == -1) && !line.startsWith("@@");
			return bb;
		}
		return garb;
	}
	static StringBuilder strb = new StringBuilder("");
	static StringBuilder bbPrint = new StringBuilder("");
	static StringBuilder regPrint = new StringBuilder("");
	static HashMap<String, StringBuilder> pctobb = new HashMap<String, StringBuilder>();
	public static void printAppropriately(String line, PrintWriter bb, PrintWriter mem, PrintWriter garb, int lineNumber)
	{
		boolean insideBBOld = insideBB;
		PrintWriter pw = classifyMe(line, bb, mem, garb);
		if(pw == garb)
		{
			pw.println(lineNumber+":"+line);
		}
		else
		{
			if(strb.indexOf("Lookup") == -1 && !line.startsWith("before entering"))
			{
				strb.append(line+"|^|");
			}
			if(pw != bb)
			{
				pw.println(lineNumber+":"+line);
			}
			else
			{

				if(strb.indexOf("Lookup") != -1)
				{
					String pc = strb.substring(line.indexOf("Lookup")+7).trim().split(" ")[0].trim();
					StringBuilder temp = pctobb.get(pc);
					if(temp == null)
					{
						//System.out.println(pctobb);
						System.out.println(strb+" *"+pc+"*");
						//System.exit(0);
					}
					else
					{
					//	System.out.println(temp);
						strb.append(temp);
						strb.append(line.replaceAll("Lookup", "Lokup")+"|^|");//just to get some tid information
					}
				}
				else
				{
					bbPrint.append(line+"|^|");
					if(insideBBOld != insideBB)
					{
						try
						{
							String pc = bbPrint.toString().split(" ")[1].trim();
							pctobb.put(pc, bbPrint);
							//System.out.println(bbPrint);
							bbPrint = new StringBuilder("");
						}
						catch(Exception e)
						{
							System.out.println(bbPrint);
							e.printStackTrace();
							bbPrint = new StringBuilder("");
						//	System.exit(0);
						}
					}
				}
			}
		}
	//	if(insideBBOld != insideBB && bb == pw)
	//	{
	//		//pw.print(lineNumber+"|^|");
	//		pw.println();
	//		strb = new StringBuilder(lineNumber+"|^|");
	//	}
	//	if(pw == mem)
	//	{
	//		strb.append(line+"|^|");
	//	}
	//	if(pw != bb)
	//	{
	//		pw.print(line);
//	//		pw.println();
	//	}
	//	else
	//	{
	//		strb.append(line);
	//		if(!insideBB)
	//		{
	//			//pw.println();
	//			if(strb.indexOf("Lookup") != -1)
	//			{
	//				String pc = strb.substring(strb.indexOf("Lookup")+7).trim().split(" ")[0].trim();
	//				StringBuilder temp = pctobb.get(pc);
	//				if(temp == null)
	//				{
	//					//System.out.println(pctobb);
	//					System.out.println(strb+" *"+pc+"*");
	//					//System.exit(0);
	//				}
	//				else
	//				{
	//				//	System.out.println(temp);
	//				}
	//				pw.print(temp);
	//				pw.print(strb.toString().replaceAll("Lookup", "Lokup")+"|^|");//just to get some tid information
	//			}
	//			else
	//			{
	//				System.out.println(strb);
	//				String pc = strb.toString().split(" ")[1].trim();
	//				pctobb.put(pc.trim(), strb);
	//				pw.print(strb);
	//			}
	//			//System.out.println(strb.toString());
	//			//strb = new StringBuilder("");
	//		}
	//		else
	//		{
	//			//pw.print("|^|");
	//			strb.append("|^|");
	//		}
	//	}
		pw.flush();
	}
	public static void main(String args[])throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		String line = "";
		PrintWriter bbs = new PrintWriter(new FileOutputStream(args[0]+"_bbs"));
		PrintWriter mem = new PrintWriter(new FileOutputStream(args[0]+"_mem"));
		PrintWriter garb = new PrintWriter(new FileOutputStream(args[0]+"_garb"));
		int lineNumber = 0;
		while((line = br.readLine())!=null)
		{
			printAppropriately(line, bbs, mem, garb, lineNumber++);
			//if(lineNumber == 10000)break;
		}
		br.close();
		bbs.flush();
		bbs.close();
		mem.flush();
		mem.close();
		garb.flush();
		garb.close();
	}
}
