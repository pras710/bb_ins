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
	public DataFlowNode(String name, InsTypeInterface ins, int insCount, ArrayList<ArrayList<String>> inouts, String hexpc)
	{
		pc = hexpc;
		this.inouts = inouts;
		this.insCount = insCount;
		this.name = name;
		//System.out.println("***"+name+"***");
		this.insType = ins;
	}
	public String toString()
	{
		return insType.toString()+name+insCount;
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
		if(hs.contains(this))return;
		if(currentChain == null)
		{
			currentChain = new InsTypeChain(new ChainData(this.insType, name.split(":")[0], name, insCount, inouts));
		}
		else 
		{
			//PRAS: REMOVE THIS COMMENT LATER: 
			if(!name.split(":")[1].equals(currentChain.myChainDefinition.get(currentChain.myChainDefinition.size() - 1).comments.split(":")[1]))
			{
				//System.out.println(name+" is added to "+currentChain.myChainDefinition.get(currentChain.myChainDefinition.size() - 1).comments);
				currentChain.addToChain(new ChainData(this.insType, name.split(":")[0], name, insCount, inouts));
			}
		}

		if(children.size() == 0 && parents.size() > 0)//CHECK: if children = 0, it stores something, but if it just gets inherited and not used?
		{
			output.add(this);
			strands.add(currentChain);
			//System.out.print("["+name+","+myTrickle+"]: ");
		}
		hs.add(this);
		for(DataFlowNode df:children)
		{
			InsTypeChain copyChain = currentChain.getACopy();
			df.getLeaves(hs, output, strands, copyChain);
		}
	}
	public void getRoots(HashSet<DataFlowNode> hs, HashSet<DataFlowNode> output)
	{
		if(hs.contains(this))return;
		if(parents.size() == 0 && children.size() > 0 )
		{
			output.add(this);
			//System.out.print("["+name+","+myTrickle+"]: ");
		}
		hs.add(this);
		for(DataFlowNode df:parents)
		{
			df.getRoots(hs, output);
		}
	}
}
