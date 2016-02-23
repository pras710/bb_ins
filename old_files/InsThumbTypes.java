package main.java;
import java.io.*;
import java.util.*;

public enum InsThumbTypes implements InsTypeInterface
{
	/**
	 * Bits to determine the type.
	 * Confirmed input
	 * Condition input
	 *
	 * Confirmed output
	 * Condition output
	 * */
	MoveShiftedRegister     (new byte[][]{new byte[]{15, 0}, new byte[]{14, 0}, new byte[]{13, 0}},
			new InOut[]{new ConfirmedInOut(0, 3), new ConfirmedInOut("cpsr")},
			new InOut[]{new ConfirmedInOut(3, 3)}),
	AddAndSubtract          (new byte[][]{new byte[]{15, 0}, new byte[]{14, 0}, new byte[]{13, 0},
			new byte[]{12, 1}, new byte[]{11, 1}},
			new InOut[]{new ConfirmedInOut(0, 3), new ConfirmedInOut("cpsr")},
			new InOut[]{new ConfirmedInOut(3, 3), new ConditionInOut(10, 0, 6, 3)}),
	MovCmpAddSubImm         (new byte[][]{new byte[]{15, 0}, new byte[]{14, 0}, new byte[]{13, 1}},
			new InOut[]{new ConfirmedInOut(8, 3), new ConfirmedInOut("cpsr")},
			new InOut[]{new ConfirmedInOut(8, 3)}
			),
	HighRegOpBranchXchg     (new byte[][]{new byte[]{15, 0}, new byte[]{14, 1}, new byte[]{13, 0},
			new byte[]{12, 0}, new byte[]{11, 0}, new byte[]{10, 1}},
			//original: new InOut[]{new ConfirmedInOut(0, 3), new ConfirmedInOut("cpsr")},
			new InOut[]{new ConditionInOut(8, 0, "", true, 
					new AddressLengthReturner(){
						public String returnLength(long opcode){
							if(((opcode>>9)&1)==0)return "r"+((((opcode>>7)&1)<<3)|(opcode&7))+"";
							else return "";
						}
					}), new ConfirmedInOut("cpsr")},
			new InOut[]{new ConfirmedInOut(3, 4), 
						new ConditionInOut(8, 0, "", true, 
							new AddressLengthReturner(){
							public String returnLength(long opcode){
								if(((opcode>>9)&1)==0)return "r"+((((opcode>>7)&1)<<3)|(opcode&7))+"";
								else return "";
							}
						})
			}),
	SpecialALUOperation     (new byte[][]{new byte[]{15, 0}, new byte[]{14, 1}, new byte[]{13, 0},
			new byte[]{12, 0}, new byte[]{11, 0}, new byte[]{10, 1}},
			new InOut[]{new ConfirmedInOut(0, 3, 7)},
			new InOut[]{new ConfirmedInOut(3, 3, 7), new ConditionInOut(9, 0, 0, 3), new ConditionInOut(8, 1, "cpsr")}),//TODO: cpsr is an input for opcodes where bit 7 != bit 6
	ALUOperation            (new byte[][]{new byte[]{15, 0}, new byte[]{14, 1}, new byte[]{13, 0},
			new byte[]{12, 0}, new byte[]{11, 0}, new byte[]{10, 0}},
			new InOut[]{new ConfirmedInOut(0, 3), new ConfirmedInOut("cpsr")},
			new InOut[]{new ConfirmedInOut(3, 3), new ConditionInOut(9, 0, 0, 3), new ConditionInOut(8, 1, "cpsr")}),//TODO: cpsr is an input for opcodes where bit 7 != bit 6
	PCRelLoad               (new byte[][]{new byte[]{15, 0}, new byte[]{14, 1}, new byte[]{13, 0},
			new byte[]{12, 0}, new byte[]{11, 1}},
			new InOut[]{new ConfirmedInOut(8, 3)},
			new InOut[]{ 
				new ConfirmedInOut("[pc]", true, new AddressLengthReturner(){
					public String returnLength(long opcode)
					{
						return "+["+((opcode&0xff)<<2)+"]";
					}
				})}),
	LoadStoreRegOffset      (new byte[][]{new byte[]{15, 0}, new byte[]{14, 1}, new byte[]{13, 0}, new byte[]{12, 1}, new byte[]{9, 0}},
			new InOut[]{new ConditionInOut(11, 1, 0, 3), new ConditionInOut(11, 0, 3, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "[r"+( (opcode >> 6) & 7 )+"]";//+( (opcode & (1 << 10)) > 0? 1: 4);
				}
				})},
			new InOut[]{new ConditionInOut(11, 0, 0, 3), new ConditionInOut(11, 1, 3, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "[r"+( (opcode >> 6) & 7 )+"]";//+( (opcode & (1 << 10)) > 0? 1: 4);
				}
				})}
			),
	LoadStoreSignExtHalfWord(new byte[][]{new byte[]{15, 0}, new byte[]{14, 1}, new byte[]{13, 0}, new byte[]{12, 1}, new byte[]{9, 1}},
			new InOut[]{new ConfirmedInOut(0, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					if((opcode >> 10 & 1) == 0 && (opcode >> 11 & 1) == 0)
					{
						return "disregard";
					}
					return "";
				}
				}), new ConfirmedInOut(3, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					if((opcode >> 10 & 1) == 0 && (opcode >> 11 & 1) == 0)
					{
						return "[r"+( (opcode >> 6) & 7 )+"]";//+(4);
					}
					return "disregard";
				}
				})},
			new InOut[]{new ConfirmedInOut(0, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					if((opcode >> 10 & 1) == 0 && (opcode >> 11 & 1) == 0)
					{
						return "";
					}
					return "disregard";
				}
				}), new ConfirmedInOut(3, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					if((opcode >> 10 & 1) == 0 && (opcode >> 11 & 1) == 0)
					{
						return "disregard";
					}
					return "[r"+( (opcode >> 6) & 7 )+"]";//+(4);
				}
				})}),
	LoadStoreImmOffset      (new byte[][]{new byte[]{15, 0}, new byte[]{14, 1}, new byte[]{13, 1}},
			new InOut[]{new ConditionInOut(11, 1, 0, 3), new ConditionInOut(11, 0, 3, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "["+( ( (opcode >> 6) & 0x1f )<<2)+"]";//+( (opcode & (1 << 12)) > 0? 1: 4);
				}
				})},
			new InOut[]{new ConditionInOut(11, 0, 0, 3), new ConditionInOut(11, 1, 3, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "["+( ( (opcode >> 6) & 0x1f )<<2)+"]";//+( (opcode & (1 << 12)) > 0? 1: 4);
				}
				})}),
	LoadStoreHalfWord       (new byte[][]{new byte[]{15, 1}, new byte[]{14, 0}, new byte[]{13, 0}, new byte[]{12, 0}},
			new InOut[]{new ConditionInOut(11, 1, 0, 3), new ConditionInOut(11, 0, 3, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "["+( (opcode >> 6) & 0x1f )+"]";//+2
				}
				})},
			new InOut[]{new ConditionInOut(11, 0, 0, 3), new ConditionInOut(11, 1, 3, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "["+( (opcode >> 6) & 0x1f )+"]";//+2
				}
				})}),
	SPRelLoadStore          (new byte[][]{new byte[]{15, 1}, new byte[]{14, 0}, new byte[]{13, 0}, new byte[]{12, 1}},
			new InOut[]{new ConditionInOut(11, 1, 8, 3), new ConditionInOut(11, 0, "[sp]", true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "["+( (opcode ) & 0xff )+"]";
				}
				})},
			new InOut[]{new ConditionInOut(11, 0, 8, 3), new ConditionInOut(11, 1, "[sp]", true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "["+( (opcode ) & 0xff )+"]";
				}
				})}),
	LoadAddress             (new byte[][]{new byte[]{15, 1}, new byte[]{14, 0}, new byte[]{13, 1}, new byte[]{12, 0}},
			new InOut[]{new ConfirmedInOut(8, 3)},
			new InOut[]{new ConditionInOut(11, 1, "sp", true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "";//+( (opcode ) & 0xff )+"";
				}
				}),
			new ConditionInOut(11, 0, "pc", true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "";//+( (opcode ) & 0xff )+"";
				}
				})
			}),
	AddOffsetToStackPointer (new byte[][]{new byte[]{15, 1}, new byte[]{14, 0}, new byte[]{13, 1}, new byte[]{12, 1}, new byte[]{11, 0}, new byte[]{10, 0}, new byte[]{9, 0}, new byte[]{8, 0}},
			new InOut[]{new ConfirmedInOut("sp")},
			new InOut[]{new ConfirmedInOut("sp")}),
	PushAndPopRegisters     (new byte[][]{new byte[]{15, 1}, new byte[]{14, 0}, new byte[]{13, 1}, new byte[]{12, 1}, new byte[]{10, 1}, new byte[]{9, 0}},
			new InOut[]{new ConfirmedInOut("sp"), new ConditionInOut(11, 1, 0, 8), new ConditionInOut(11, 1, "pc", true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					int count = 0;
					for(int i = 0; i <= 8; i++)
					{
						if((opcode & ( 1 << i)) > 0)
							count++;
					}
					return "";//"+"+(count*4);
				}
				})},
			new InOut[]{new ConfirmedInOut("sp"), new ConditionInOut(11, 0, 0, 8), new ConditionInOut(11, 0, "lr", true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					int count = 0;
					for(int i = 0; i <= 8; i++)
					{
						if((opcode & ( 1 << i)) > 0)
							count++;
					}
					return "";//"-"+(count*4);
				}
				})}),
	Miscellaneous           (new byte[][]{new byte[]{15, 1},new byte[]{14, 0},new byte[]{13, 1},new byte[]{12, 1}},
			null, null),
	MultipleLoadStore       (new byte[][]{new byte[]{15, 1}, new byte[]{14, 1}, new byte[]{13, 0}, new byte[]{12, 0}},
			new InOut[]{new ConfirmedInOut(8, 3), new ConditionInOut(11, 0, 0, 8), new ConditionInOut(11, 1, 8, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					int count = 0;
					for(int i = 0; i < 8; i++)
					{
						if((opcode & ( 1 << i)) > 0)
							count++;
					}
					return "";//"+"+(count*4);
				}
				})},
			new InOut[]{new ConfirmedInOut(8, 3), new ConditionInOut(11, 1, 0, 8), new ConditionInOut(11, 0, 8, 3, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					int count = 0;
					for(int i = 0; i < 8; i++)
					{
						if((opcode & ( 1 << i)) > 0)
							count++;
					}
					return "";//"+"+(count*4);
				}
				})}),
	ConditionalBranch       (new byte[][]{new byte[]{15, 1}, new byte[]{14, 1}, new byte[]{13, 0}, new byte[]{12, 1}},
			null,
			new InOut[]{new ConfirmedInOut("cpsr")}
			),
	SoftwareInterrupt       (new byte[][]{new byte[]{15, 1}, new byte[]{14, 1}, new byte[]{13, 0}, new byte[]{12, 1}, new byte[]{11, 1}, new byte[]{10, 1}, new byte[]{9, 1}, new byte[]{8, 1}},
			new InOut[]{new ConfirmedInOut("spsr")},
			new InOut[]{new ConfirmedInOut("cpsr")}
			),
	UnconditionalBranch     (new byte[][]{new byte[]{15, 1}, new byte[]{14, 1}, new byte[]{13, 1}, new byte[]{12, 0}},
			new InOut[]{new ConditionInOut(11, 1, "lr")},
			new InOut[]{new ConditionInOut(11, 1, "lr")}
			//null,null
			),
	LongBranchWithLink      (new byte[][]{new byte[]{15, 1}, new byte[]{14, 1}, new byte[]{13, 1}, new byte[]{12, 1}},
			new InOut[]{new ConfirmedInOut("lr")},null
			),
	ThumbEEChkArray         (new byte[][]{new byte[]{15, 1}, new byte[]{14, 1}, new byte[]{13, 0}, new byte[]{12, 0},  new byte[]{11, 1},  new byte[]{10, 0},  new byte[]{9, 1},  new byte[]{8, 0}},
			new InOut[]{new ConfirmedInOut("lr"), new ConfirmedInOut("pc")},
			new InOut[]{new ConfirmedInOut("", true, new AddressLengthReturner(){
				public String returnLength(long opcode){
					return ""+((opcode &7)|( ( (opcode>>7)&1) << 3));
				}
				}), new ConfirmedInOut(3, 4)}
			)
	;
	byte[][] op_type_detector;
	InOut[] dests, srcs;
	InsThumbTypes(byte[][] operation_type_detector, InOut[] dests, InOut[] srcs)
	{
		op_type_detector = operation_type_detector;
		this.dests = dests;
		this.srcs = srcs;
	}
	static InsThumbTypes getMyType(long opcode)
	{
		for(InsThumbTypes i: InsThumbTypes.values())
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
		ret.add(in_operands);
		in_operands.remove("cpsr");
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
		ret.add(out_operands);
		out_operands.remove("cpsr");
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
