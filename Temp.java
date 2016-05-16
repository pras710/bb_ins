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
		for(int i = 0; i < 10; i++)
		{
			Temp t1 = new Temp();
			t1.id = i;
			new Thread(t1).start();
		}
		Thread.sleep(50000);
	}
}
