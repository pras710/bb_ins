package main.java;
import java.util.*;
public class ConditionInOut extends InOut
{
	int value;
	int special = -1;
	public ConditionInOut(int bit, int value, String coproc, int coproc_id_bit, int coproc_id_length, boolean isAddress, AddressLengthReturner arl)
	{
		this.coprocPrefix = coproc;
		this.isAddress = isAddress;
		this.addressLength = arl;
		this.bit = bit;
		this.value = value;
		this.coproc_reg_bit = -1;//coproc_reg_bit;
		this.coproc_reg_length = -1;//coproc_reg_length;
		this.coproc_id_bit = coproc_id_bit;
		this.coproc_id_length = coproc_id_length;
		reg_num_bit = -1;
		length = -1;
	}
	public ConditionInOut(int bit, int value, String coproc, int coproc_reg_bit, int coproc_reg_length, int coproc_id_bit, int coproc_id_length, int special, boolean isAddress, AddressLengthReturner arl)
	{
		this.isAddress = isAddress;
		this.addressLength = arl;
		this.coprocPrefix = coproc;
		this.bit = bit;
		this.value = value;
		this.coproc_reg_bit = coproc_reg_bit;
		this.coproc_reg_length = coproc_reg_length;
		this.coproc_id_bit = coproc_id_bit;
		this.coproc_id_length = coproc_id_length;
		this.special = special;
		reg_num_bit = -1;
		length = -1;
	}
	public ConditionInOut(int bit, int value, String coproc, int coproc_reg_bit, int coproc_reg_length, int coproc_id_bit, int coproc_id_length, boolean isAddress, AddressLengthReturner arl)
	{
		this.isAddress = isAddress;
		this.addressLength = arl;
		this.bit = bit;
		this.coprocPrefix = coproc;
		this.value = value;
		this.coproc_reg_bit = coproc_reg_bit;
		this.coproc_reg_length = coproc_reg_length;
		this.coproc_id_bit = coproc_id_bit;
		this.coproc_id_length = coproc_id_length;
		reg_num_bit = -1;
		length = -1;
	}
	public ConditionInOut(int bit, int value, String coproc, int coproc_reg_bit, int coproc_reg_length, int coproc_id_bit, int coproc_id_length)
	{
		this.bit = bit;
		this.value = value;
		this.coproc_reg_bit = coproc_reg_bit;
		this.coproc_reg_length = coproc_reg_length;
		this.coproc_id_bit = coproc_id_bit;
		this.coprocPrefix = coproc;
		this.coproc_id_length = coproc_id_length;
		reg_num_bit = -1;
		length = -1;
	}
	public ConditionInOut(int bit, int value, String regName, boolean isAddress, AddressLengthReturner arl)
	{
		this.bit = bit;
		this.value = value;
		this.reg_num_bit = -1;
		this.length = -1;
		this.regName = regName;
		this.isAddress = isAddress;
		this.addressLength = arl;
	}
	public ConditionInOut(int bit, int value, int reg_num_bit, int length, boolean isAddress, AddressLengthReturner arl)
	{
		this.bit = bit;
		this.value = value;
		this.reg_num_bit = reg_num_bit;
		this.length = length;
		this.isAddress = isAddress;
		this.addressLength = arl;
	}
	public ConditionInOut(int bit, int value, int reg_num_bit, int length)
	{
		this.bit = bit;
		this.value = value;
		this.regName = "";
		this.reg_num_bit = reg_num_bit;
		this.length = length;
	}
	public ConditionInOut(int bit, int value, String regName)
	{
		this.bit = bit;
		this.value = value;
		this.regName = regName;
		reg_num_bit = - 1;
		length = -1;
	}
	@Override
	public boolean check(long opcode)
	{
		return ( ( (opcode & (1 << bit) ) >> bit ) == value);
	}
	@Override
	public void fillOperand(long opcode, ArrayList<String> list)
	{
		if(!check(opcode))return;
		if(reg_num_bit < 0 || length < 0)
		{
			if(coproc_id_length < 0)
			{
				if(isAddress)
				{
					String address = addressLength.returnLength(opcode);
					if(address.startsWith("["))
					{
						address = "+"+address;
					}
					list.add(regName+address);
					return;
				}
				list.add(regName);
				return;
			}
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
				int special_bit_pos = 0;
				if(special != -1 &&  ( (opcode >> special) & 1) == 1)
				{
					special_bit_pos = (1<<(length+1));
				}
				list.add(coprocStringGet(coproc_num, (int)(coproc_reg|special_bit_pos))+address);//"cp"+((opcode >> coproc_id_bit) & mask_id)+":"+((opcode >> coproc_reg_bit) & mask_reg) 
			}
			else
			{
				list.add("cp"+((opcode >> coproc_id_bit) & mask_id)+":"+address); 
			}
			return;
		}
		//String ret = "cond:";
		int mask = 0;
		if(length > 4)
		{
			for(int i = 0; i < length; i++)
			{
				if( ( (opcode >> (reg_num_bit+i)) & 1) == 1)
				{
		//			ret += "r"+i+" ";
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
			//String address = "[r"+(length < 3?"":( ( opcode >> reg_num_bit) & mask)) +"]) to "+addressLength.returnLength(opcode);
			String addl = addressLength.returnLength(opcode);
			if(addl.startsWith("["))
			{
				addl = "+"+addl;
			}
			if(length >= 3 && length <= 4)
			{
				list.add("[r"+( ( opcode >> reg_num_bit ) & mask)+"]"+addl);
			}
			else if(length < 3)
			{
				list.add(regName+addl);
			}
			return;// ret+address;
		}
		if(length >= 3 && length <= 4)
		{
			list.add("r"+(( opcode >> reg_num_bit ) & mask));
		}
		else if(length < 3)
		{
			list.add(regName);
		}
		return;
		//return ret+"r"+(length<3?"":( (opcode >> reg_num_bit) & mask));
	}
	@Override
	public String getOperand(long opcode)
	{
		if(!check(opcode))return "no_condition";
		if(reg_num_bit < 0 || length < 0)
		{
			if(coproc_id_length < 0)
			{
				if(isAddress)
				{
					String address = addressLength.returnLength(opcode);
					return address;
				}
				return regName;
			}
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

			int coproc_num = (int)((opcode >> coproc_id_bit) & mask_id), coproc_reg = (int)((opcode >> coproc_reg_bit) & mask_reg); 
				int special_bit_pos = 0;
				if(special != -1 &&  ( (opcode >> special) & 1) == 1)
				{
					special_bit_pos = (1<<(length+1));
				}
			return coprocStringGet(coproc_num, (int)(coproc_reg|special_bit_pos));
			//return "cp"+((opcode >> coproc_id_bit) & mask_id)+":"+((opcode >> coproc_reg_bit) & mask_reg); 
		}
		String ret = "cond:";
		int mask = 0;
		if(length > 4)
		{
			for(int i = 0; i < length; i++)
			{
				if( ( (opcode >> (reg_num_bit+i)) & 1) == 1)
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
			String address = "[r"+(length < 3?"":( ( opcode >> reg_num_bit) & mask)) +"]) to "+addressLength.returnLength(opcode);
			return ret+address;
		}
		return ret+"r"+(length<3?"":( (opcode >> reg_num_bit) & mask));
	}
}
