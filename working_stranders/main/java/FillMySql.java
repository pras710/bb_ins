package main.java;
import java.util.*;
import java.io.*;
import java.sql.*;
class LibFile
{
	String name, part;
	long op_start, op_length;
	public LibFile(String name, String part, String op_start, String op_length)
	{
		try
		{
			this.part = part;
			this.name = name;
			this.op_start = Long.parseLong(op_start, 16);
			this.op_length = Long.parseLong(op_length, 16);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void insertIntoDB(ArrayList<String> q, Connection con, long present)
	{
		try
		{
			boolean insert_mode = true;
			StringBuilder strb = new StringBuilder("");
			for(int i = 0; i < q.size(); i++)
			{
				String opcode_built = "";
				strb.append(q.get(i));
				if(i % 448570 == 0)
				{
					if(insert_mode)
					{
						PreparedStatement pstmt = con.prepareStatement("insert into lib_hash_store values('"+name+part+"',?,"+(i*2)+")");
						pstmt.setNCharacterStream(1, new InputStreamReader(new ByteArrayInputStream(strb.toString().getBytes())));
						int rows = pstmt.executeUpdate();
						System.out.println("inserted "+rows+"?");
//						insert_mode = false;
						strb = new StringBuilder("");
					}
					else
					{
						try
						{
							PreparedStatement pstmt = con.prepareStatement("update lib_hash_store set opcodes = concat(opcodes,?) where lib_name = '"+name+part+"' ");
							pstmt.setNCharacterStream(1, new InputStreamReader(new ByteArrayInputStream(strb.toString().getBytes())));
							int rows = pstmt.executeUpdate();
							System.out.println("updated "+rows+"?");
							insert_mode = false;
							strb = new StringBuilder("");
						}
						catch(Exception e)
						{
							System.out.println("length = "+strb.length()+" size = "+i);
							e.printStackTrace();
							System.exit(0);
						}
					}
				}
			}
			PreparedStatement pstmt = con.prepareStatement("insert into lib_hash_store values('"+name+"',?,"+present+")");
			pstmt.setNCharacterStream(1, new InputStreamReader(new ByteArrayInputStream(strb.toString().getBytes())));
			int rows = pstmt.executeUpdate();
			System.out.println("inserted "+rows+"?");
			//insert_mode = false;
			strb = new StringBuilder("");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void putInDB(Connection con, String pwd)
	{
		try
		{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select count(*) from lib_hash_store where lib_name = '"+name+part+"'");
			if(rs.next() && rs.getInt(1)>0)return;
			ArrayList<String> queue = new ArrayList<>();
			FileInputStream fin = new FileInputStream(pwd+name);
			long present = 0;
			while(fin.read() !=-1)
			{
				present++;
				if(present == op_start)break;
			}
			if(present < op_start + op_length)
			{
				int by_r = -1;
				while((by_r = fin.read())!=-1 && present < op_start+op_length)
				{
					String now = Integer.toHexString((int)(by_r&0xff));
					if(now.length() > 2)
					{
						System.out.println(now);
						System.exit(0);
					}
					if(now.length() < 2)now = "0"+now;
					queue.add(now);
					if(queue.size() == op_length)
					{
						matchWithMemCpy(queue);
						//insertIntoDB(queue, con, present);
						break;
						//queue.remove(0);
					}
					present++;
				}
				fin.close();
				//insertIntoDB(queue, stmt, present);
				matchWithMemCpy(queue);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	static String opcode8 = "", opcode4 = "";
	public static void matchWithMemCpy(ArrayList<String> queue)
	{
		String myMemCpy = "DataProcFSRTransfer, DataProcFSRTransfer, DataProcFSRTransfer, BlockDataTransfer, BlockDataTransfer, BlockDataTransfer, BlockDataTransfer, DataProcFSRTransfer, SingleDataTransfer, SingleDataTransfer, DataProcFSRTransfer, DataProcFSRTransfer, Branch, ";
		String remMemCpy = myMemCpy;
		int count = 0;
		for(String s:queue)
		{
			opcode8 +=s;
			//opcode4 +=s;
			//if(opcode4.length() == 4)
			//{
			//	InsThumbTypes ins = InsThumbTypes.getMyType(Long.parseLong(opcode4, 16));
			//	if(ins != null)
			//	{
			//		System.out.println("got thumb:"+ins);
			//	}
			//	opcode4 = opcode4.substring(2);
			//}
			if(opcode8.length() == 8)
			{
				Ins32BitTypes ins = Ins32BitTypes.getMyType(Long.parseLong(opcode8, 16));
				if(ins != null)
				{
					System.out.println("got 32 bit:"+ins);
					if(remMemCpy.startsWith(ins.toString()))
					{
						count++;
						remMemCpy = remMemCpy.substring(ins.toString().length()+2);
						if(remMemCpy.equals(""))
						{
							System.out.println("foundit!");
							System.exit(0);
						}
					}
					else
					{
						if(count > 3)
						{
							System.out.println("stretch = "+count+" rem = "+remMemCpy);
						}
						count = 0;

						remMemCpy = myMemCpy;
					}
					opcode8 = "";
				}
				else
				{
					opcode8 = opcode8.substring(2);
				}
			}
		}

	}
}
public class FillMySql
{
	public static void main1(String args[])throws Exception
	{
		String pwd = "/export/home/pur128/downloads/android_dumps/lib_files_from_android/";
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost/test");
		ResultSet rs = null;
		Statement stmt = con.createStatement();
		File f = new File(pwd);
		String[] sofiles = f.list();
		ArrayList<LibFile> al = new ArrayList<>();
		for(String lib: sofiles)
		{
			if(lib.endsWith(".so"))
			{
//				Process p = Runtime.getRuntime().exec("readelf", new String[]{"--wide", "-S", pwd+lib});// libc_malloc_debug_leak.so");
				Process p = Runtime.getRuntime().exec("readelf --wide -S "+ pwd+lib);// libc_malloc_debug_leak.so");
				//System.out.println(p);
				//BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = "";
				while((line = br.readLine())!=null)
				{
					if(line.indexOf("PROGBITS")!=-1)
					{
						int len = line.length();
						do{
							len = line.length();
							line = line.trim().replaceAll("\t"," ");
							line = line.replaceAll("  "," ");
						}while(line.length() < len);
						String[] split = line.split(" ");
						int prog_ind = 0;
						for(prog_ind = 0; prog_ind < split.length; prog_ind++)
						{
							if(split[prog_ind].equals("PROGBITS"))break;
						}
						System.out.println(lib+".txt: "+line+" =>  "+split[prog_ind+1]+" "+split[prog_ind+2]+" "+split[prog_ind+3]);
						LibFile lib_file = new LibFile(lib, split[prog_ind-1],split[prog_ind+1], split[prog_ind+3]);
						al.add(lib_file);
					}
					//System.out.println(line);
				}
			}
		}
		for(LibFile l:al)
		{
			System.out.println(l.name+l.part);
			l.putInDB(con, pwd);
		}
		//rs = stmt.executeQuery("show tables");
		//while(rs.next())
		//{
		//	System.out.println(rs.getString(1));
		//}
	}
}
