package main.java;
import java.util.*;
import java.io.*;
public class TrackedStrands implements Serializable
{
	HashSet<String> toMatchStrands = new HashSet<>();
	public void writeObject(ObjectOutputStream out) throws Exception
	{
		out.writeObject(toMatchStrands);
	}
	public void readObject(ObjectInputStream in) throws Exception
	{
		toMatchStrands = (HashSet<String>)in.readObject();
	}
	public TrackedStrands(String what, String path)
	{
		switch(what)
		{
			case "data_manager":
				try
				{
					BufferedReader br = new BufferedReader(new FileReader(path+"/data_manager"));
					String line = "";
					while((line = br.readLine())!=null)
					{
						toMatchStrands.add(line.trim());
					}
					br.close();	
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			//	System.out.println(toMatchStrands);
				break;
			case "memo_data":
				try
				{
					BufferedReader br = new BufferedReader(new FileReader(path+"/memo_data"));
					String line = "";
					while((line = br.readLine())!=null)
					{
						toMatchStrands.add(line.trim());
					}
					br.close();	
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
	}
	public boolean canGo(String n)
	{
		if(toMatchStrands.size() == 0)
		{
			return true;
		}
		for(String s:toMatchStrands)
		{
			if(s.indexOf(n)!=-1)
			{
				return true;
			}
		}
		return false;
	}
}
