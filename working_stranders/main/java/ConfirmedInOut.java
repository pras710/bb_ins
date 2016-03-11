package main.java;
import java.util.*;
public class ConfirmedInOut extends InOut
{
	int special = -1;
	public ConfirmedInOut(String regName)
	{
		this.regName = regName;
		bit = length = reg_num_bit = -1;
	}
	public ConfirmedInOut(String regName, boolean isAddress, AddressLengthReturner arl)
	{
		this.regName = regName;
		this.isAddress = isAddress;
		this.addressLength = arl;
	}
	public ConfirmedInOut(int bit, int length, boolean isAddress, AddressLengthReturner arl)
	{
		this.bit = bit;
		this.reg_num_bit = bit;
		this.length = length;
		this.isAddress = isAddress;
		this.addressLength = arl;
	}
	public ConfirmedInOut(String coproc, int coproc_id_bit, int coproc_id_length, boolean isAddress, AddressLengthReturner arl)
	{
		this.isAddress = isAddress;
		this.addressLength = arl;
		this.coproc_reg_bit = -1;//coproc_reg_bit;
		this.coprocPrefix = coproc;
		this.coproc_reg_length = -1;//coproc_reg_length;
		this.coproc_id_bit = coproc_id_bit;
		this.coproc_id_length = coproc_id_length;
		reg_num_bit = -1;
		length = -1;
		special = -1;
	}
	public ConfirmedInOut(String coproc, int coproc_reg_bit, int coproc_reg_length, int coproc_id_bit, int coproc_id_length, int special, boolean isAddress, AddressLengthReturner arl)
	{
		this.isAddress = isAddress;
		this.addressLength = arl;
		this.coproc_reg_bit = coproc_reg_bit;
		this.coprocPrefix = coproc;
		this.coproc_reg_length = coproc_reg_length;
		this.coproc_id_bit = coproc_id_bit;
		this.coproc_id_length = coproc_id_length;
		reg_num_bit = -1;
		length = -1;
		this.special = special;
	}
	public ConfirmedInOut(String coproc, int coproc_reg_bit, int coproc_reg_length, int coproc_id_bit, int coproc_id_length, boolean isAddress, AddressLengthReturner arl)
	{
		this.isAddress = isAddress;
		this.addressLength = arl;
		this.coproc_reg_bit = coproc_reg_bit;
		this.coproc_reg_length = coproc_reg_length;
		this.coprocPrefix = coproc;
		this.coproc_id_bit = coproc_id_bit;
		this.coproc_id_length = coproc_id_length;
		reg_num_bit = -1;
		length = -1;
		special = -1;
	}
	public ConfirmedInOut(String coproc, int coproc_reg_bit, int coproc_reg_length, int coproc_id_bit, int coproc_id_length)
	{
		this.coproc_reg_bit = coproc_reg_bit;
		this.coproc_reg_length = coproc_reg_length;
		this.coprocPrefix = coproc;
		this.coproc_id_bit = coproc_id_bit;
		this.coproc_id_length = coproc_id_length;
		reg_num_bit = -1;
		length = -1;
		special = -1;
	}
	public ConfirmedInOut(int bit, int length, int special)
	{
		this.bit = bit;
		this.length = length;
		this.special = special;
	}
	public ConfirmedInOut(int bit, int length)
	{
		this.bit = bit;
		this.length = length;
	}
	@Override
	public boolean check(long opcode)
	{
		return true;
	}
	@Override
	public void fillOperand(long opcode, ArrayList<String> list)
	{
		if(coproc_id_length > 0)
		{
			int mask_id = 0;
			for(int i = 0; i < coproc_id_length; i++)
			{
				mask_id |= (1 << i);
			}
			int mask_reg = 0;
			if(coproc_reg_length > 0)
			{
				for(int i = 0; i < coproc_reg_length; i++)
				{
					mask_reg |= (1 << i);
				}
			}
			String address = "";
			if(isAddress)
			{
				address = addressLength.returnLength(opcode);
				if(address.startsWith("["))
				{
					address = "+"+address;
				}
			}
			if(coproc_reg_length > 0)
			{
				int coproc_num = (int)((opcode >> coproc_id_bit) & mask_id), coproc_reg = (int)((opcode >> coproc_reg_bit) & mask_reg); 
				//list.add(coprocStringGet(coproc_num,(int)( coproc_reg|(((opcode>>22)&1)<<4)))+address);
				int special_bit_pos = 0;
				if(special != -1 &&  ( (opcode >> special) & 1) == 1)
				{
					special_bit_pos = (1<<(length+1));
				}
				list.add(coprocStringGet(coproc_num, coproc_reg|special_bit_pos)+address);
				//5bit coproc reg = (int)( coproc_reg|(((opcode>>22)&1)<<4))
				//list.add("cp"+((opcode >> coproc_id_bit) & mask_id)+":"+((opcode >> coproc_reg_bit) & mask_reg)+address); 
			}
			else
			{
				list.add("cp"+((opcode >> coproc_id_bit) & mask_id)+":"+address); 
				//list.add("cp"+((opcode >> coproc_id_bit) & mask_id)+":"+address); 
			}
			//list.add("cp"+((opcode >> coproc_id_bit) & mask_id)+":"+((opcode >> coproc_reg_bit) & mask_reg)); 
			return;
		}
		if(bit < 0)
		{
			list.add(regName);
			return;
		}
		int mask = 0, special_bit_pos = 0;
		//String ret = "conf:";
		if(special != -1 &&  ( (opcode >> special) & 1) == 1)
		{
			special_bit_pos = (1<<(length+1));
		}
		if(length > 4)
		{
			for(int i = 0; i < length; i++)
			{
				if( ( (opcode >> (bit+i)) & 1) == 1)
				{
					//ret += "r"+i+" ";
					list.add("r"+i);
				}
			}
		}
		for(int i = 0; i < length; i++)
		{
			mask |= (1 << i);
		}
		if(isAddress)
		{
			//String address = "[r"+(length < 3?"":(special_bit_pos|( ( opcode >> reg_num_bit) & mask))) +"]"+addressLength.returnLength(opcode);
			//return ret+address;
			String addl = addressLength.returnLength(opcode);
			if(addl.startsWith("["))
			{
				addl = "+"+addl;
			}
			if(length >= 3 && length <= 4)
			{
				if(addl.equals("liiiiiiiits"))
				{
					System.out.println("special_bit_pos = "+special_bit_pos);
					System.out.printf("opcode = %x, bit = %d, mask = %x\n", opcode, reg_num_bit, mask);
				}
				list.add("[r"+(special_bit_pos|( ( opcode >> reg_num_bit ) & mask))+"]"+addl);
			}
			else if(length < 3)
			{
				list.add(regName+addl);
			}
			return;
		}
		if(length >= 3 && length <= 4)
		{
			list.add("r"+(special_bit_pos|( ( opcode >> bit ) & mask)));
		}
		else if(length < 3)
		{
			list.add(regName);
		}
		return;// ret+"r"+(length < 3?"":(special_bit_pos|( (opcode >> bit) & mask)));
	}
	@Override
	public String getOperand(long opcode)
	{
		System.out.println("ConfirmedInOut.getOperand() call DEPRECATED!!! EXITING");
		System.exit(0);
		if(coproc_reg_length > 0)
		{
			int mask_id = 0;
			for(int i = 0; i < coproc_id_length; i++)
			{
				mask_id |= (1 << i);
			}
			int mask_reg = 0;
			for(int i = 0; i < coproc_reg_length; i++)
			{
				mask_reg |= (1 << i);
			}
				int special_bit_pos = 0;
				if(special != -1 &&  ( (opcode >> special) & 1) == 1)
				{
					special_bit_pos = (1<<(length+1));
				}
				int coproc_num = (int)((opcode >> coproc_id_bit) & mask_id), coproc_reg = (int)((opcode >> coproc_reg_bit) & mask_reg); 
			return coprocStringGet(coproc_num, (int)(coproc_reg|special_bit_pos));//"cp"+((opcode >> coproc_id_bit) & mask_id)+":"+((opcode >> coproc_reg_bit) & mask_reg); 
		}
		if(bit < 0)
			return regName;
		int mask = 0, special_bit_pos = 0;
		String ret = "conf:";
		if(special != -1 &&  ( (opcode >> special) & 1) == 1)
		{
			special_bit_pos = (1<<(length+1));
		}
		if(length > 4)
		{
			for(int i = 0; i < length; i++)
			{
				if( ( (opcode >> (bit+i)) & 1) == 1)
				{
					ret += "r"+i+" ";
				}
			}
		}
		for(int i = 0; i < length; i++)
		{
			mask |= (1 << i);
		}
		if(isAddress)
		{
			String address = "[r"+(length < 3?"":(special_bit_pos|( ( opcode >> reg_num_bit) & mask))) +"]"+addressLength.returnLength(opcode);
			return ret+address;
		}
		return ret+"r"+(length < 3?"":(special_bit_pos|( (opcode >> bit) & mask)));
	}
}
