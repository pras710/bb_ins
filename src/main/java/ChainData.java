package main.java;
import java.util.*;
import java.io.*;
public class ChainData implements Serializable//<ChainData>
{
	InsTypeInterface insType;
	String operandName;
	String comments;
	String ins_name, ins_name_orig;
	short insCount;
	String pc;
	ArrayList<ArrayList<String>> inouts;
//	boolean verbosePrintedOnce = false;
	public void readObject(ObjectInputStream in)throws IOException, ClassNotFoundException
	{
		insType = (InsTypeInterface)in.readObject();
		operandName = (String)in.readObject();
		comments = (String)in.readObject();
		ins_name = (String)in.readObject();
		ins_name_orig = (String)in.readObject();
		insCount = (Short)in.readObject();
		//insCount = (Integer)in.readObject();
		pc = (String)in.readObject();
		inouts = (ArrayList<ArrayList<String>>)in.readObject();
	}
	public void writeObject(ObjectOutputStream out)throws IOException
	{
		out.writeObject(insType);
		out.writeObject(operandName);
		out.writeObject(comments);
		out.writeObject(ins_name);
		out.writeObject(ins_name_orig);
		out.writeObject((Short)insCount);
		//out.writeObject((Integer)insCount);
		out.writeObject(pc);
		out.writeObject(inouts);
	}
	public boolean isCoprocInstruction()
	{
		return (insType.toString().toLowerCase().indexOf("coproc")!=-1);
	}
	public ChainData(InsTypeInterface insType, String op, String comments, int insCount,  ArrayList<ArrayList<String>> inouts)
	{
		this.inouts = inouts;
		try
		{
			comments = comments.trim();
			this.pc = comments.split(" ")[0].substring(comments.indexOf(":")+1);
			//System.out.println(comments);
		}
		catch(Exception e)
		{
			System.out.println(comments);
			e.printStackTrace();
			System.out.println("chain data constructor is messed\n");
			System.exit(0);
		}
		this.insCount = (short)insCount;
		this.insType = insType;
		operandName = op;
		this.comments = comments;
		String nexPrev = "";
		StringTokenizer strTok = new StringTokenizer(comments.substring(comments.lastIndexOf(":")+1), " \t");
		while(strTok.hasMoreTokens())
		{
			String nex = strTok.nextToken();
			if(nex.indexOf(",")!=-1)
			{
				ins_name = nexPrev;
				break;
			}
			try
			{
				Long.parseLong(nex, 16);
			}
			catch(Exception e)
			{
				ins_name = nex;
				break;
			}
			nexPrev = nex;
		}
		if(ins_name == null || ins_name.indexOf(",")!=-1)
		{
			System.out.println(comments+" >>>>>>>>>>>"+ins_name+"<<<<<<<<<<");
		}
		String [][]nom = new String[][]{new String[]{"add", "sub", "adc", "neg", "abs"}, 
			new String[]{"mul", "div", "ml", "mac", "msc", "sqrt"}, 
			new String[]{"and", "or", "not", "bi", "sb", "bf", "sxt", "uxt", "ub", "rev"},
			new String[]{"rsb", "lsb", "lsr", "rsr", "asr", "clz","rsl", "lsl"},
			new String[]{"cmp", "cmn", "tst", "teq"},
			new String[]{"ld", "pop"},
			new String[]{"st", "push"},
			new String[]{"mov", "mv", "mcr", "mrc", "msr", "mrs", "mrx","mxr", "cvt", "tos", "tou", "tod", "cpy", "nop"},
			new String[]{"j", "tbh", "bl", "bx", "tb"},
		};
		//String [][]nom = new String[][]{new String[]{"add", "sub"}, 
		//								new String[]{"mul", "div"}, 
		//								new String[]{"and", "or", "not"},
		//								new String[]{"rsb", "lsb"},
		//								new String[]{"rsl", "lsl"},
		//								new String[]{"cmp", "tst"},
		//								new String[]{"ld", "pop"},
		//								new String[]{"st", "push"},
		//								new String[]{"mov"},
		//								new String[]{"j", "tbh", "b"},
		//								//new String[]{},
		//
		//};
		ins_name_orig = ins_name;
		if(ins_name.startsWith("ld"))
		{
			ins_name = "ld";
		}
		else
		{
			for(String s[]:nom)
			{
				for(String ss:s)
				{
					if(ins_name.indexOf(ss)!=-1)
					{
				//	if(ins_name.equals("rsb") && ss.equals("b"))
				//	{
				//		System.out.println("rsb <==> b");
				//		//System.exit(0);
				//	}
					//System.out.println(ins_name+" <==> "+s[0]);
						ins_name = s[0];
						break;
					}
				}
			}
		}
	}
	public String original()
	{
		return ins_name_orig;
	}
	public String verbose()
	{
		//if(!verbosePrintedOnce)
//		{
		//	verbosePrintedOnce = true;
			return pc;
	//	}
//		else
//		{
//			return "";
//		}
		//return insCount+" "+insType+" "+operandName+" "+comments+" ";//+pc;
	}
	public String toString()
	{
		return ins_name;//+":"+inouts;//+"_"+comments;
		//return insCount+":"+ins_name;//+":"+inouts;//+"_"+comments;
		//return insType.toString();//+"_"+comments;
	}
}
