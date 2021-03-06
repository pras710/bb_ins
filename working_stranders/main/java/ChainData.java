package main.java;
import java.util.*;
import java.io.*;
public class ChainData implements Serializable//<ChainData>
{
	InsTypeInterface insType;
	String operandName;
	String comments;
	String ins_name;
	int insCount;
	String pc;
	ArrayList<ArrayList<String>> inouts;
	public void readObject(ObjectInputStream in)throws IOException, ClassNotFoundException
	{
		insType = (InsTypeInterface)in.readObject();
		operandName = (String)in.readObject();
		comments = (String)in.readObject();
		ins_name = (String)in.readObject();
		insCount = (Integer)in.readObject();
		pc = (String)in.readObject();
		inouts = (ArrayList<ArrayList<String>>)in.readObject();
	}
	public void writeObject(ObjectOutputStream out)throws IOException
	{
		out.writeObject(insType);
		out.writeObject(operandName);
		out.writeObject(comments);
		out.writeObject(ins_name);
		out.writeObject((Integer)insCount);
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
		this.insCount = insCount;
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
	}
	public String verbose()
	{
		return insCount+" "+insType+" "+operandName+" "+comments+" ";//+pc;
	}
	public String toString()
	{
		return ins_name;//+":"+inouts;//+"_"+comments;
		//return insCount+":"+ins_name;//+":"+inouts;//+"_"+comments;
		//return insType.toString();//+"_"+comments;
	}
}
