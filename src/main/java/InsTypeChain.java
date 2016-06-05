package main.java;
import java.io.*;
import java.util.*;
public class InsTypeChain implements Serializable//<InsTypeChain>
{
	ArrayList<ChainData> myChainDefinition;
	//NOT SCALING WELL TreeSet<Long> lineNumberOccurrence;
	//TreeSet<BasicBlock> lineNumberOccurrence;
	boolean isCoprocBasedChain = false, containsLoad = false;
	short bloatFactor = 0;
	public void writeObject(ObjectOutputStream out)throws IOException
	{
		out.writeObject(myChainDefinition);
		out.writeObject((Boolean)isCoprocBasedChain);
		out.writeObject((Boolean)containsLoad);
		//out.writeObject(lineNumberOccurrence);
	}
	public void readObject(ObjectInputStream in)throws IOException, ClassNotFoundException
	{
		myChainDefinition = (ArrayList<ChainData>)in.readObject();
		isCoprocBasedChain = (Boolean)in.readObject();
		containsLoad = (Boolean)in.readObject();
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
				containsLoad |= cd.ins_name.equals("ld");
				bloatFactor += cd.bloatFactor;
			}
		}
		//lineNumberOccurrence = new TreeSet<>();
	}
	public boolean matches(InsTypeChain ins)
	{
		int si = ins.size();
		if(si == this.size())
		{
			for(int i = 0; i < si; i++)
			{
				if(!ins.myChainDefinition.get(i).matches(this.myChainDefinition.get(i)))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	public InsTypeChain(ChainData interd)
	{
		myChainDefinition = new ArrayList<>();
		//lineNumberOccurrence = new TreeSet<>();
		myChainDefinition.add(interd);
		isCoprocBasedChain |= interd.isCoprocInstruction();
		containsLoad |= (interd.ins_name.indexOf("ld")!=-1);
		bloatFactor += interd.bloatFactor;
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
			bloatFactor += cd_now.bloatFactor;
			ret.myChainDefinition.add(cd_now);
			ret.isCoprocBasedChain |= cd_now.isCoprocInstruction();
			ret.containsLoad |= cd_now.ins_name.equals("ld");
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
			bloatFactor += cd_now.bloatFactor;
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
				ret.bloatFactor += interd.bloatFactor;
				ret.isCoprocBasedChain |= interd.isCoprocInstruction();
				ret.containsLoad |= (interd.ins_name.indexOf("ld")!=-1);//containsLoad;
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
		String dest = this.getDestination();
		String src = insd.getSource();
		if(!"".equals(dest) && !"".equals(src) && dest.indexOf(src)==-1)
		{
			System.out.println(this+" is appended to "+insd);
			System.out.println(this.getDestination()+" != "+insd.getSource());
			System.exit(0);
		}

		myChainDefinition.addAll(insd.myChainDefinition);
		bloatFactor += insd.bloatFactor;
		isCoprocBasedChain |= insd.isCoprocBasedChain;
		containsLoad |= insd.containsLoad;
//		this.lineNumberOccurrence.addAll(insd.lineNumberOccurrence);
	}
	public void addToChain(ChainData insd)
	{
		myChainDefinition.add(insd);
		bloatFactor += insd.bloatFactor;
		isCoprocBasedChain |= insd.isCoprocInstruction();
		containsLoad |= (insd.ins_name.indexOf("ld")!=-1);//containsLoad;
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
		numBrs = 1;
		length = myChainDefinition.size();
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
		for(ChainData cd:myChainDefinition)
		{
			System.out.println(cd.original()+" << check dependings: "+cd.operandName+" in: "+cd.comments+" "+cd.insType+" "+cd.inouts);
		}
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
		boolean ld = myChainDefinition.get(0).toString().equals("ld"), st = myChainDefinition.get(myChainDefinition.size()-1).toString().equals("st");
		return (ld && st);
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
		if(myChainDefinition.size() > 0)
		{
			return myChainDefinition.get(0).operandName;
		}
		return "";
	}
	public String getDestination()
	{
		if(myChainDefinition.size() > 0)
		{
			return myChainDefinition.get(myChainDefinition.size()-1).inouts.get(1).toString();//operandName;
		}
		return "";
	}
	public int size()
	{
		return myChainDefinition.size();
	}
}
