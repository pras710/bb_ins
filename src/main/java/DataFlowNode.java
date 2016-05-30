package main.java;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
public class DataFlowNode
{
	String name = "";
	InsTypeInterface insType;
	int myTrickle = 0;
	int insCount = 0;
	String pc = "";
	 ArrayList<ArrayList<String>> inouts;
	HashSet<DataFlowNode> parents = new HashSet<>();
	HashSet<DataFlowNode> children = new HashSet<>();
	boolean visited = false;
	public DataFlowNode(String name, InsTypeInterface ins, int insCount, ArrayList<ArrayList<String>> inouts, String hexpc)throws Exception
	{
		pc = hexpc;
		this.inouts = inouts;
		this.insCount = insCount;
		this.name = name;
		if(!name.startsWith("phi"))
		{
			String t = name.split(":")[0];
			if(inouts.toString().indexOf(t)==-1)
			{
				throw new Exception(name+" "+inouts+" not matching the inouts");
			}
		}
		//System.out.println("***"+name+"***");
		this.insType = ins;
	}
	public String toString()
	{
		return insType.toString()+name+insCount+pc;
	}
	public void addParent(DataFlowNode parent, boolean countme)
	{
		int add = 0;
		if(countme)
		{
			add = 1;
		}
		int trickle = parent.myTrickle+add;
		if(trickle > myTrickle)
		{
			myTrickle = trickle;
		}
		parents.add(parent);
	}
	public void addChild(DataFlowNode child)
	{
		children.add(child);
	}
	boolean printed = false;
	public void printMe(StringBuilder strb)
	{
		if(printed)return;
		strb.append("["+name+","+myTrickle+"]");
		printed = true;
		for(DataFlowNode df:children)
		{
			df.printMe(strb);
		}
	}
	public void getLeaves(HashSet<DataFlowNode> hs, HashSet<DataFlowNode> output, ArrayList<InsTypeChain> strands, InsTypeChain currentChain)
	{
		if(this.visited)return;
		//if(hs.contains(this))return;
		if(currentChain == null)
		{
			currentChain = new InsTypeChain(new ChainData(this.insType, name.split(":")[0], name, insCount, inouts));
			//THIS IS NEW
			//strands.add(currentChain);
		}
		else 
		{
			//PRAS: REMOVE THIS COMMENT LATER: 
			if(!name.split(":")[1].equals(currentChain.myChainDefinition.get(currentChain.myChainDefinition.size() - 1).comments.split(":")[1]) )//&& !StrandMaintainer.PRINT_ALL_STRANDS)
			{
				//System.out.println(name+" is added to "+currentChain.myChainDefinition.get(currentChain.myChainDefinition.size() - 1).comments);
				currentChain.addToChain(new ChainData(this.insType, name.split(":")[0], name, insCount, inouts));
			}
		}

		if(children.size() == 0)// && parents.size() > 0)//CHECK: if children = 0, it stores something, but if it just gets inherited and not used?
		{
			output.add(this);
			//THIS WAS THE ORIGINAL ADDING LINE
			boolean shouldInsert = true;
			for(InsTypeChain temp:strands)
			{
				if(temp.matches(currentChain))
				{
					shouldInsert = false;
					break;
				}
			}
			if(shouldInsert)
			{
				strands.add(currentChain);
			}
//			if(!strands.toString().contains(currentChain.toString()))
//			{
//				//System.out.println("contains returns true: "+currentChain+" "+strands);
//				strands.add(currentChain);
//			}
			//System.out.print("["+name+","+myTrickle+"]: ");
		}
		this.visited = true;
		hs.add(this);
		for(DataFlowNode df:children)
		{
			InsTypeChain copyChain = currentChain.getACopy();
			//THIS IS NEW
		//	strands.add(copyChain);
			df.getLeaves(hs, output, strands, copyChain);
		}
	}
	public void getRoots(HashSet<DataFlowNode> hs, HashSet<DataFlowNode> output)
	{
		if(this.visited)return;
		//if(hs.contains(this))return;
		if(parents.size() == 0)// && children.size() > 0 )
		{
			output.add(this);
			//System.out.print("["+name+","+myTrickle+"]: ");
		}
		this.visited = true;
		//hs.add(this);
		for(DataFlowNode df:parents)
		{
			df.getRoots(hs, output);
		}
	}
}
