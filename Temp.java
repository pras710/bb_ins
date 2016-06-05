import java.io.*;
public class Temp extends Thread
{
	int id = 0;
	public void run()
	{
		while(true)
		{
			try
			{
				System.out.println("Thread "+id+" running");
				Thread.sleep(2000);
			}
			catch(Exception e)
			{
			}
		}
	}
	public static void main(String args[])throws Exception
	{
		String ab =  "a, b, c, d, e, f, g";
		System.out.println(ab.replace(",",""));
		System.out.println(ab.replaceAll(",",""));






	}
}
