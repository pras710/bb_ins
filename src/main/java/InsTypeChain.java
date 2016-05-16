package main.java;
import java.io.*;
import java.util.*;
public class InsTypeChain implements Serializable//<InsTypeChain>
{
	ArrayList<ChainData> myChainDefinition;
	//NOT SCALING WELL TreeSet<Long> lineNumberOccurrence;
	//TreeSet<BasicBlock> lineNumberOccurrence;
	boolean isCoprocBasedChain = false;
	public void writeObject(ObjectOutputStream out)throws IOException
	{
		out.writeObject(myChainDefinition);
		out.writeObject((Boolean)isCoprocBasedChain);
		//out.writeObject(lineNumberOccurrence);
	}
	public void readObject(ObjectInputStream in)throws IOException, ClassNotFoundException
	{
		myChainDefinition = (ArrayList<ChainData>)in.readObject();
		isCoprocBasedChain = (Boolean)in.readObject();
		//lineNumberOccurrence = (TreeSet<BasicBlock>)in.readObject();
	}
	public String getSubString()
	{
		String temp = myChainDefinition.toString();
		temp = temp.substring(0, temp.length() - 1);
		return temp;
	}
	public InsTypeChain(ArrayList<ChainData> interd)
	{
		myChainDefinition = new ArrayList<>();
		//for(InsTypeInterface insD:interd)
		{
			myChainDefinition.addAll(interd);
			for(ChainData cd:interd)
			{
				isCoprocBasedChain |= cd.isCoprocInstruction();
			}
		}
		//lineNumberOccurrence = new TreeSet<>();
	}
	public InsTypeChain(ChainData interd)
	{
		myChainDefinition = new ArrayList<>();
		//lineNumberOccurrence = new TreeSet<>();
		myChainDefinition.add(interd);
		isCoprocBasedChain |= interd.isCoprocInstruction();
	}
	public InsTypeChain copyTillStore(InsTypeChain ret)
	{
		String loadString = "st";
		if(ret == null)
		{
			ret = new InsTypeChain(myChainDefinition.get(0));
		}
		int i = ret.size();
		for(; i < myChainDefinition.size(); i++)
		{
			ChainData cd_now = myChainDefinition.get(i);
			ret.myChainDefinition.add(cd_now);
			ret.isCoprocBasedChain |= cd_now.isCoprocInstruction();
			if(cd_now.ins_name.contains(loadString))
			{
				break;
			}
		}
		//ret.lineNumberOccurrence.addAll(this.lineNumberOccurrence);
		return ret;
	}
	public InsTypeChain copyFromLoad()
	{
		String loadString = "ld";
		int i = 0;
		for(i =0; i < myChainDefinition.size(); i++)
		{
			ChainData cd_now = myChainDefinition.get(i);
			if(cd_now.ins_name.contains(loadString))
			{
				break;
			}
		}
		InsTypeChain ret = null;
		if(i < myChainDefinition.size())
		{
			ret = new InsTypeChain(myChainDefinition.get(i));
			i++;
			for(;i < myChainDefinition.size(); i++)
			{
				ChainData interd = myChainDefinition.get(i);
				ret.isCoprocBasedChain |= interd.isCoprocInstruction();
				ret.addToChain(interd);
			}
//			try
//			{
//				ret.lineNumberOccurrence.addAll(this.lineNumberOccurrence);
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//				System.out.println(ret.lineNumberOccurrence);
//				System.out.println(this.lineNumberOccurrence);
//				System.exit(0);
//			}
		}
		return ret;
	}
	public void addToChain(InsTypeChain insd)
	{
		myChainDefinition.addAll(insd.myChainDefinition);
		isCoprocBasedChain |= insd.isCoprocBasedChain;
//		this.lineNumberOccurrence.addAll(insd.lineNumberOccurrence);
	}
	public void addToChain(ChainData insd)
	{
		myChainDefinition.add(insd);
		isCoprocBasedChain |= insd.isCoprocInstruction();
	}
	public String lengthCovered()
	{
		int numBrs = 0, length = 0, prevCd = 0;
		for(ChainData cd:myChainDefinition)
		{
			if(prevCd >= cd.insCount)
			{
				numBrs++;
				length+= prevCd;
			}
			prevCd = cd.insCount;
		}
		length+=prevCd;
		return "(b, totIns, length)=("+numBrs+","+length+","+myChainDefinition.size()+")";
	}
	public boolean contains(String pc)
	{
		for(ChainData cd:myChainDefinition)
		{
			if(cd.verbose().indexOf(pc)!=-1)
			{
				return true;
			}
		}
		return false;
	}
	boolean verbosePrintedOnce = false;
	public void verbosePrint()
	{
		if(verbosePrintedOnce)return;
		//else
		System.out.println(myChainDefinition.get(0).verbose()+" haiboinst");
		verbosePrintedOnce = true;
		//PRAS: Comment out this FOR LOOP
		//for(ChainData cd:myChainDefinition)
		//{
		//	System.out.println(cd.verbose()+" haiboinst");
		//}
//		System.out.println(lineNumberOccurrence);
	}
	public String toString()
	{
		String ret = myChainDefinition.toString();//"";
	//	for(ChainData cd:myChainDefinition)
	//	{
	//		ret +=cd.toString();
	//	}
		return ret+lengthCovered();
		//return myChainDefinition.toString()+"["+lengthCovered()+"]";
	}
	public ArrayList<TreeSet<String>> getInsAndOuts()
	{
		ArrayList<TreeSet<String>> ret = new ArrayList<>();
		ret.add(new TreeSet<String>());
		ret.add(new TreeSet<String>());
		for(ChainData cd:myChainDefinition)
		{
			ret.get(0).addAll(cd.inouts.get(0));
			ret.get(1).addAll(cd.inouts.get(1));
		}
		return ret;
	}
	public InsTypeChain getACopy()
	{
		InsTypeChain newMe = new InsTypeChain(myChainDefinition);
//		newMe.lineNumberOccurrence.addAll(this.lineNumberOccurrence);
		return newMe;
	}
	public boolean isIdeal()
	{
		return (myChainDefinition.get(0).toString().contains("ld") &&
				myChainDefinition.get(myChainDefinition.size()-1).toString()
				.contains("st"));
	}
	public InsTypeInterface getSourceType()
	{
		return myChainDefinition.get(0).insType;
	}
	public InsTypeInterface getDestinationType()
	{
		return myChainDefinition.get(myChainDefinition.size()-1).insType;
	}
	public ChainData getSourceNode()
	{
		return myChainDefinition.get(0);
	}
	public String getStartingPC()
	{
		return getSourceNode().pc;
	}
	public ChainData getDestinationNode()
	{
		return myChainDefinition.get(myChainDefinition.size()-1);
	}
	public String getSource()
	{
		return myChainDefinition.get(0).operandName;
	}
	public String getDestination()
	{
		return myChainDefinition.get(myChainDefinition.size()-1).operandName;
	}
	public int size()
	{
		return myChainDefinition.size();
	}
}
