import java.io.*;
import java.util.*;
import java.util.zip.*;
public class SnippetMaker
{
	public static void main(String args[])throws Exception
	{
		String path = "/i3c/hpcl/huz123/spark/java_example_pras/data_proc";
		File pwd = new File(path);
		String []dirContents = pwd.list();
		for(String file:dirContents)
		{
			if(file.endsWith("_snippets"))
			{
				String bigGuy = path+"/"+file;
				File f = new File(bigGuy);
				String[] contents = f.list();
				if(contents.length == 1)
				{
					System.out.println("Going to split: "+bigGuy+"/"+contents[0]);
					InputStream in_stream = null;
					if(contents[0].endsWith(".gz"))
					{
						in_stream = new GZIPInputStream(new FileInputStream(bigGuy+"/"+contents[0]));
					}
					else
					{
						in_stream = new FileInputStream(bigGuy+"/"+contents[0]);
					}
					String line = "";
					int lineNumber = 0;
					int snippetCounter = 0;
					String outpath = bigGuy+"/"+contents[0]+snippetCounter;
					PrintWriter pw = new PrintWriter(outpath+""+snippetCounter);
					snippetCounter++;
					ArrayList<String> key = new ArrayList<>();
					HashMap<String, String> values = new HashMap<>();
					BufferedReader br = new BufferedReader(new InputStreamReader(in_stream));
					while((line = br.readLine())!=null)
					{
						lineNumber++;
						if(lineNumber % 40000 == 0)
						{
							pw.flush();
							pw.close();
							pw = new PrintWriter(outpath+snippetCounter);
							snippetCounter++;
							printBeforeEnteringIn(pw, key, values);
						}
						pw.println(line);
						trackMemValues(line, key, values);
					}
					pw.flush();
					pw.close();
					br.close();
				}
			}
		}
	}
	static void printBeforeEnteringIn(PrintWriter pw, ArrayList<String> key, HashMap<String, String> values)
	{
		pw.print("before entering: ");
		for(String k:key)
		{
			pw.print(k+" = "+values.get(k)+" ");
		}
		pw.println();
	}
	static void trackMemValues(String line, ArrayList<String> keys, HashMap<String, String> values)
	{
		int ind = line.indexOf("before entering");
		if(ind != -1)
		{
			line = line.substring(ind+"before entering:".length());
			String[] all_values = line.split(" ");
			String prefix = "";
			for(int i = 0; i < all_values.length - 2;)
			{
				if(all_values[i+1].equals("="))
				{
					String key = prefix+all_values[i];
					//System.out.println("putting "+key+" = "+all_values[i+2]);
					values.put(key, all_values[i+2]);
					if(!keys.contains(key))
					{
						keys.add(key);
					}
					//LinkedBlockingDeque<String> myVersions = memoryMap.get(key);
					//if(myVersions == null)
					//{
					//	myVersions = new LinkedBlockingDeque<String>();
					//}
					//myVersions.push(all_values[i+2]);
					//memoryMap.put(key, myVersions);//all_values[i+2]);
				}
				else if(all_values[i].indexOf("=")!=-1)
				{
					String[] temp = all_values[i].split("=");
					String key = temp[0];
					String value = temp[1];
					if(temp.length == 2)
					{
						values.put(key, value);
						if(!keys.contains(key))
						{
							keys.add(key);
						}
					}
					else
					{
						System.out.println(all_values[i]+" is not a before entering tuple!!!");
						System.exit(0);
					}
				}
				else
				{
					System.out.println(line);
					System.out.println("equals not matching at "+i);
					System.exit(0);
				}
				i+=3;
			}
		}
	}
}
