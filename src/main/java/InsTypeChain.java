package main.java;
import java.io.*;
import java.util.*;
public class InsTypeChain
{
	ArrayList<ChainData> myChainDefinition;
	boolean isCoprocBasedChain = false;
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
	}
	public InsTypeChain(ChainData interd)
	{
		myChainDefinition = new ArrayList<>();
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
		}
		return ret;
	}
	public void addToChain(InsTypeChain insd)
	{
		myChainDefinition.addAll(insd.myChainDefinition);
		isCoprocBasedChain |= insd.isCoprocBasedChain;
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
	public void verbosePrint()
	{
		for(ChainData cd:myChainDefinition)
		{
			System.out.println(cd.verbose());
		}
	}
	public String toString()
	{
		return myChainDefinition.toString()+"["+lengthCovered()+"]";
	}
	public InsTypeChain getACopy()
	{
		InsTypeChain newMe = new InsTypeChain(myChainDefinition);
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
