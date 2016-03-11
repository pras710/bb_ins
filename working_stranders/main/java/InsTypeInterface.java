package main.java;
import java.io.*;
import java.util.*;
public interface InsTypeInterface
{
	public ArrayList< ArrayList<String> > fillOperands(long opcode);
	public String getOperands(long opcode);
}
