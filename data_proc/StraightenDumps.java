import java.io.*;
import java.util.*;
public class StraightenDumps
{
	static boolean insideBB = false;
	public static PrintWriter classifyMe(String line, PrintWriter bb, PrintWriter mem, PrintWriter garb)
	{
		if(line.startsWith("@@"))return mem;
		if(line.indexOf("Lookup")!=-1)
		{
			insideBB = false;
			return bb;
		}
		if(line.startsWith("before entering"))
		{
			insideBB = true;
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
	static HashMap<String, StringBuilder> pctobb = new HashMap<>();
	public static void printAppropriately(String line, PrintWriter bb, PrintWriter mem, PrintWriter garb, int lineNumber)
	{
		boolean insideBBOld = insideBB;
		PrintWriter pw = classifyMe(line, bb, mem, garb);
		if(insideBBOld != insideBB && bb == pw)
		{
			//pw.print(lineNumber+"|^|");
			strb.append(lineNumber+"|^|");
		}
		if(pw != bb)
		{
			pw.print(lineNumber+":"+line);
			pw.println();
		}
		else
		{
			strb.append(line);
			if(!insideBB)
			{
				//pw.println();
				if(strb.indexOf("Lookup") != -1)
				{
					String pc = strb.substring(strb.indexOf("Lookup")+7).trim().split(" ")[0].trim();
					StringBuilder temp = pctobb.get(pc);
					if(temp == null)
					{
						System.out.println(pctobb);
						System.out.println(strb+" *"+pc+"*");
						System.exit(0);
					}
					else
					{
					//	System.out.println(temp);
					}
					pw.print(temp);
					pw.println(strb.toString().replaceAll("Lookup", "Lokup"));//just to get some tid information
				}
				else
				{
					String pc = strb.toString().split(" ")[1].trim();
					pctobb.put(pc.trim(), strb);
					pw.println(strb);
				}
				//System.out.println(strb.toString());
				strb = new StringBuilder("");
			}
			else
			{
				//pw.print("|^|");
				strb.append("|^|");
			}
		}
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
