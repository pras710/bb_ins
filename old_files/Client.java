package main.java;
import java.io.*;
import java.util.*;
import java.net.*;
class Rad extends Thread
{
	BufferedReader br;
	public Rad(BufferedReader br)
	{
		this.br = br;
	}
	public void run()
	{
		try
		{
			while(true)
			{
				String str = br.readLine();
				System.out.println(str);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
public class Client
{
	public static void main(String args[])throws Exception
	{
		System.out.println("trying connection");
		//Socket s = new Socket("p349csl11.cse.psu.edu", 3333);
		Socket s = new Socket("130.203.36.183", 3333);
		System.out.println("done?");
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		PrintWriter pw = new PrintWriter(s.getOutputStream());
		new Rad(br).start();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		{
			String i = in.readLine();
			pw.println(i);
			pw.flush();
		}
	}
}
