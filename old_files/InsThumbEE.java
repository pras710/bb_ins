package main.java;
import java.io.*;
import java.util.*;

public enum InsThumbEE implements InsTypeInterface
{
	/**
	 * Bits to determine the type.
	 * Confirmed input
	 * Condition input
	 *
	 * Confirmed output
	 * Condition output
	CHECKArray3()1123
	HandleBranch()1125
	HandleBranchLP()1126
	HandleBranchWP()1127
	LDR()1128
	STR()1130
	VLD1 to VLD4
	VST1 to VST4

	 * */

	
	CoprocDataTransfer        (new byte[][]{new byte[]{27, 1}, new byte[]{26, 1}, new byte[]{25, 0}}, 
			new InOut[]{new ConditionInOut(21, 1, 16, 4), 
				new ConditionInOut(20, 1, "coproc", 12, 4, 8, 4, true,  
					new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							int tempFlag =  (int)( (opcode >> 20) & 0xf);
							int []flagDisregards = new int[]{4, 5, 8, 6, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf};
							boolean disreg = false;
							for(int i:flagDisregards)
							{
								disreg |= (i == tempFlag);
							}
							if((disreg) && ( (opcode & 0x0f0) == 0))//acc takes last 3 bitstempFlag == 4 || tempFlag == 5 || tempFlag == 8 || tempFlag == 6 || tempFlag == 9 || tempFlag == 0xa
							{
								return "disregard";
							}
							else if(tempFlag >= 0xc &&  (opcode & 0xf00)==0xb00)
							{
								return "disregard";
							}
							else if( ((opcode >>8)&0xf) == 0xa && ((opcode >> 24)&0xf) == 0xd)
							{
								return "disregard";
							}
							else if( (tempFlag) == 0x7 && ((opcode >> 24)&0xf) == 0xd)
							{
								return "disregard";
							}
							return "";
						}
					}), 
					new ConditionInOut(20, 0, 16, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							if(( opcode & (1 << 22) ) > 0)
							{
								return "["+((((opcode & (1<<23)) > 0)?1:-1)*((15*4)+(opcode & 0xff)))+"]";
							}
							return "["+((((opcode & (1<<23)) > 0)?1:-1)*(opcode & 0xff))+"]";
						}
					}), 
					//check for mra, mar io
					new ConditionInOut(24, 0, 12, 4, true, new AddressLengthReturner()
						{
							public String returnLength(long opcode)
							{
								if( ( (opcode >> 20) & 0xf) == 5 && ( (opcode & 0xfff) == 0))
								{
									return "";
								}
							return "disregard";
							}
						}), 
					new ConditionInOut(24, 0, "", true, new AddressLengthReturner()
						{
							public String returnLength(long opcode)
							{
								if( ( (opcode >> 20) & 0xf) == 4 )//acc takes last 3 bits
								{
									if( (opcode & 0xfff) == 0)
									{
										return "acc"+(opcode&7);
									}
									else //if((opcode & 0xfff) <= 7)
									{
										return "cr"+(opcode&7);
									}
								}
								return "disregard";
							}
						}),
			},
			new InOut[]{new ConditionInOut(20, 0, "coproc", 12, 4, 8, 4, true,
					new AddressLengthReturner(){
					public String returnLength(long opcode)
					{
					int tempFlag =  (int)( (opcode >> 20) & 0xf);
					int []flagDisregards = new int[]{4, 5, 8, 6, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf};
					boolean disreg = false;
					for(int i:flagDisregards)
					{
					disreg |= (i == tempFlag);
					}
					if((disreg) && ( (opcode & 0x0f0) == 0))//acc takes last 3 bitstempFlag == 4 || tempFlag == 5 || tempFlag == 8 || tempFlag == 6 || tempFlag == 9 || tempFlag == 0xa
					{
					return "disregard";
					}
					else if(tempFlag >= 0xc &&  (opcode & 0xf00)==0xb00)
					{
					return "disregard";
					}
					else if( ((opcode >>8)&0xf) == 0xa && ((opcode >> 24)&0xf) == 0xd)
					{
						return "disregard";
					}
					else if( (tempFlag&6) == 0x6 && ((opcode >> 24)&0xf) == 0xd)
					{
						return "disregard";
					}
					return "";
					}
					}),
					new ConfirmedInOut("", true,
							new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
							if( ((opcode >>8)&0xf) == 0xa && ((opcode >> 24)&0xf) == 0xd)
							{
							int dec = (int)( (opcode >> 20)&0xf);
							if(dec == 0)
							{
							return "s"+(((opcode>>12)&0xf)<<1);
							}
							else if(dec == 4)
							{
							return "s"+((((opcode>>12)&0xf)<<1)+1);
							}
							}
							return "disregard";
							}
							}),
					new ConditionInOut(20, 1, 16, 4, true, new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
							if(( opcode & (1 << 22) ) > 0)
							{
							return "["+((((opcode & (1<<23)) > 0)?1:-1)*((15*4)+(opcode & 0xff)))+"]";
							}
							return "["+((((opcode & (1<<23)) > 0)?1:-1)*(opcode & 0xff))+"]";
							}
							}),
					//check for mra, mar io
					new ConditionInOut(24, 1, 12, 4, true, new AddressLengthReturner()
							{
							public String returnLength(long opcode)
							{
							if( ( (opcode >> 20) & 0xf) == 4 && ( (opcode & 0xfff) == 0))
							{
							return "";
							}
							return "disregard";
							}
							}), new ConditionInOut(24, 0, "", true, new AddressLengthReturner()
								{
								public String returnLength(long opcode)
								{
								if( ( (opcode >> 20) & 0xf) == 5 )//acc takes last 3 bits
								{
								if( (opcode & 0xfff) == 0)
								{
								return "acc"+(opcode&7);
								}
								else //if( (opcode & 0xfff) <= 7)
								{
								return "cr"+(opcode&7);
								}
								}
								return "disregard";
								}
								})
			}),//(0b110_0000_0_0000_0000_0000_0000_0000),
			CoprocDataOperation       (new byte[][]{new byte[]{27, 1}, new byte[]{26, 1}, new byte[]{25, 1}, new byte[]{24, 0}, new byte[]{4, 0}},
					new InOut[]{new ConfirmedInOut("coproc", 12, 4, 8, 4, true, new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
							if(((opcode >> 8) & 1) == 1)
							{
							return "disregard, s"+( ((opcode>>12)&0xf) | (((opcode>>22)&1)<<4));
							}
							else
							{
							return "disregard, s"+( (((opcode>>12)&0xf)<<1) | (((opcode>>22)&1)));
							}
							//	return "";
							}
							})},
					new InOut[]{new ConfirmedInOut("coproc", 0, 4, 8, 4, true, new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
							if(((opcode >> 8) & 1) == 1)
							{
							return "disregard, s"+( ((opcode)&0xf) | (((opcode>>5)&1)<<4));
							}
							else
							{
							return "disregard, s"+( (((opcode)&0xf)<<1) | (((opcode>>5)&1)));
							}
							//return "";
							}
							}), new ConfirmedInOut("coproc", 16, 4, 8, 4, true, new AddressLengthReturner(){
								public String returnLength(long opcode)
								{
								if(((opcode >> 8) & 1) == 1)
								{
								return "disregard, s"+( ((opcode>>16)&0xf) | (((opcode>>7)&1)<<4));
								}
								else
								{
								return "disregard, s"+( (((opcode>>16)&0xf)<<1) | (((opcode>>7)&1)));
								}
								//	return "";
								}
								})}),//(0b111_0000_0_0000_0000_0000_0000_0000),
					CoprocRegTransfer         (new byte[][]{new byte[]{27, 1}, new byte[]{26, 1}, new byte[]{25, 1}, new byte[]{24, 0}, new byte[]{4, 1}},
							new InOut[]{new ConditionInOut(20, 1, 12, 4), new ConditionInOut(20, 0, "cr", 16, 4, 8, 4, true, new AddressLengthReturner(){
									public String returnLength(long opcode)
									{
										return "disregard, cr"+((((opcode>>16)&0xf)<<1) | ((opcode>>7)&1));
									}
								}), new ConditionInOut(20, 0, "cr", 0, 4, 8, 4)},
							new InOut[]{new ConditionInOut(20, 0, 12, 4), new ConditionInOut(20, 1, "cr", 16, 4, 8, 4, true, new AddressLengthReturner(){
									public String returnLength(long opcode)
									{
										return "disregard, cr"+((((opcode>>16)&0xf)<<1) | ((opcode>>7)&1));
									}
								}), new ConditionInOut(20, 1, "cr", 0, 4, 8, 4)}),//(0b111_0000_0_0000_0000_0000_0001_0000),

	byte[][] op_type_detector;
	InOut[] dests, srcs;
	InsThumbEE(byte[][] operation_type_detector, InOut[] dests, InOut[] srcs)
	{
		op_type_detector = operation_type_detector;
		this.dests = dests;
		this.srcs = srcs;
	}
	static InsThumbEE getMyType(long opcode)
	{
		for(InsThumbEE i: InsThumbEE.values())
		{
			int count = 0;
			for(byte[] bit:i.op_type_detector)
			{
				//System.out.println(Long.toBinaryString(opcode)+" ["+bit[0]+"] = "+((opcode & (1 << bit[0])) >> bit[0])+" == "+bit[1]);
				if( ( (opcode & (1 << bit[0])) >> bit[0]) == bit[1])
				{
					count++;
				}
				else
				{
					break;
				}
			}
			if(count == i.op_type_detector.length)
			{
				return i;
			}
		}
		return null;
	}
	public ArrayList< ArrayList<String> > fillOperands(long opcode)
	{
		ArrayList< ArrayList<String> > ret = new ArrayList<>();
		ArrayList<String> in_operands = new ArrayList<>();
		//StringBuilder strb = new StringBuilder("in = ");
		if(srcs != null)
		{
			for(InOut in:srcs)
			{
				//strb.append(in.getOperand(opcode)+",");
				in.fillOperand(opcode, in_operands);
			}
		}
		in_operands.remove("");
		ret.add(in_operands);
		ArrayList<String> out_operands = new ArrayList<>();
		//strb.append("; out = ");
		if(dests != null)
		{
			for(InOut out:dests)
			{
				out.fillOperand(opcode, out_operands);
				//strb.append(out.getOperand(opcode)+",");
			}
		}
		out_operands.remove("");
		ret.add(out_operands);
		return ret;
	}
	public String getOperands(long opcode)
	{
		StringBuilder strb = new StringBuilder("in = ");
		if(srcs != null)
		{
			for(InOut in:srcs)
			{
				strb.append(in.getOperand(opcode)+",");
			}
		}
		strb.append("; out = ");
		if(dests != null)
		{
			for(InOut out:dests)
			{
				strb.append(out.getOperand(opcode)+",");
			}
		}
		return strb.toString();
	}
}
