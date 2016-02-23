package main.java;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.net.*;
public class ServerService
{
	public static void main1(String args[])throws Exception
	{
		ServerSocket ss = new ServerSocket(0xdb1);
		while(true)
		{
			try
			{
				Socket s = ss.accept();
				new InsertEverythingInside(s);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
class InsertEverythingInside extends Thread
{
	BufferedReader br = null;
	PrintWriter pw = null;
	Socket soc = null;
	Statement stmt = null;
	Connection con = null;
	public void run()
	{
		while(true)
		{
			try
			{
				String query = br.readLine();
				if(stmt == null)
				{
					manageMysql();
				}
				pw.println(stmt.executeUpdate(query));
			}
			catch(Exception e)
			{
				e.printStackTrace(pw);
				e.printStackTrace();
			}
		}
	}
	public void manageMysql() throws Exception
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost/prasanna_db", "pur128", "951814771Cse");
			stmt = con.createStatement();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	public InsertEverythingInside(Socket s)
	{
		try
		{
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
			this.soc = s;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		this.start();
	}
}
