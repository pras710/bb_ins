package main.java;
import java.util.*;
interface AddressLengthReturner {
	String returnLength(long opcode);
}
public abstract class InOut
{
	abstract boolean check(long opcode);
	abstract String getOperand(long opcode);
	abstract void fillOperand(long opcode, ArrayList<String> list);
	public int bit, length;
	public int coproc_reg_bit = -1, coproc_reg_length = -1, coproc_id_bit = -1, coproc_id_length = -1;
	public boolean isAddress = false;
	public AddressLengthReturner addressLength;
	public int reg_num_bit;
	public String regName = "<default>";
	public String coprocPrefix = "cp";
	public String coprocStringGet(int coproc_num, int coproc_reg)
	{
		//coproc_reg &= 0xf;
		if(coprocPrefix.equals("coproc")||coprocPrefix.equals("cp"))
		{
			switch(coproc_num)
			{
				case 0:
					return "cr"+coproc_reg;
				case 3:
					return "cr"+coproc_reg;
				case 8:
					return "cr"+coproc_reg;
				case 9:
					return "cr"+coproc_reg;
				case 7:
					return "cr"+coproc_reg;
				case 6:
					return "cr"+coproc_reg;
				case 1:
					return "f"+(coproc_reg&0x7);
				case 2:
					return "f"+(coproc_reg&0x7);
					//return "f"+coproc_reg;
				case 4:
				case 5:
					return "mvf"+coproc_reg;//can be mvfx, mvd, mvdx, mvf: but handled in credibilitycheck in BasicBlock
				case 10:
				case 11:
					return "s"+coproc_reg;
				case 12:
					return "cr"+coproc_reg;
				default:
					return "cp"+coproc_num+":"+coproc_reg;
			}
		}
		return coprocPrefix+coproc_reg;
	}
}
