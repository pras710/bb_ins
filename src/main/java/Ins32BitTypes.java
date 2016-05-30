package main.java;
import java.io.*;
import java.util.*;

public enum Ins32BitTypes implements InsTypeInterface
{
	/**
	 * Bits to determine the type.
	 * Confirmed input
	 * Condition input
	 *
	 * Confirmed output
	 * Condition output
	 * */
	Multiply                  (new byte[][]{new byte[]{27, 0}, new byte[]{26, 0}, new byte[]{25, 0}, new byte[]{24, 0}, new byte[]{23, 0}, new byte[]{22, 0}, 
			new byte[]{4, 1}, new byte[]{5, 0}, new byte[]{6, 0}, new byte[]{7, 1}}, 
			new InOut[]{new ConditionInOut(20, 1, "cpsr"), new ConfirmedInOut(16, 4)}, 
			new InOut[]{new ConditionInOut(21, 1, 16, 4), new ConfirmedInOut(8, 4), new ConfirmedInOut(0, 4)}),// new ConfirmedInOut(12, 4),
//(0b000_0000_0_0000_0000_0000_1001_0000),
	MultiplyLong              (new byte[][]{new byte[]{27, 0}, new byte[]{26, 0}, new byte[]{25, 0}, new byte[]{24, 0}, new byte[]{23, 1},  
			new byte[]{4, 1}, new byte[]{5, 0}, new byte[]{6, 0}, new byte[]{7, 1}}, 
			new InOut[]{new ConditionInOut(20, 1, "cpsr"), new ConfirmedInOut(16, 4), new ConfirmedInOut(12, 4)}, 
			new InOut[]{new ConditionInOut(21, 1, 16, 4), new ConditionInOut(21, 1, 12, 4), new ConfirmedInOut(8, 4), new ConfirmedInOut(0, 4)}),
//(0b000_0100_0_0000_0000_0000_1001_0000),
	SingleDataSwap            (new byte[][]{new byte[]{27, 0}, new byte[]{26, 0}, new byte[]{25, 0}, new byte[]{24, 1}, new byte[]{23, 0}, new byte[]{21, 0}, new byte[]{20, 0},  
			new byte[]{4, 1}, new byte[]{5, 0}, new byte[]{6, 0}, new byte[]{7, 1}, new byte[]{8, 0},  new byte[]{9, 0}, new byte[]{10, 0}, new byte[]{11, 0}}, 
			new InOut[]{new ConfirmedInOut(12, 4), new ConfirmedInOut(16, 4)}, 
			new InOut[]{new ConfirmedInOut(16, 4), new ConfirmedInOut(0, 4), new ConfirmedInOut(16, 4, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "";//+(( (opcode & (1 << 22)) > 0? 1: 4));
				}
				})}),
//(0b000_1000_0_0000_0000_0000_1001_0000),
	BranchExchange            (new byte[][]{new byte[]{27, 0}, new byte[]{26, 0}, new byte[]{25, 0}, new byte[]{24, 1}, new byte[]{23, 0}, new byte[]{22, 0}, new byte[]{21, 1}, 
			new byte[]{20, 0},  new byte[]{4, 1}, new byte[]{5, 0}, new byte[]{6, 0}, new byte[]{7, 1}, new byte[]{8, 1},  new byte[]{9, 1}, new byte[]{10, 1}, new byte[]{11, 1},
			new byte[]{12, 1}, new byte[]{13, 1}, new byte[]{14, 1}, new byte[]{15, 1}, new byte[]{16, 1}, new byte[]{17, 1}, new byte[]{18, 1}, new byte[]{19, 1} }, 
			null,
			new InOut[]{new ConfirmedInOut(0, 4)}),//(0b000_1001_0_1111_1111_1111_0001_0000),
	HalfWordDataTransferRegOff(new byte[][]{new byte[]{27, 0}, new byte[]{26, 0}, new byte[]{25, 0},  new byte[]{22, 0},
			new byte[]{4, 1},  new byte[]{7, 1}, new byte[]{8, 0},  new byte[]{9, 0}, new byte[]{10, 0}, new byte[]{11, 0}},
			new InOut[]{new ConditionInOut(20, 1, 12, 4), new ConditionInOut(21, 1, 16, 4), 
			new ConditionInOut(20, 0, 16, 4, true, new AddressLengthReturner(){
				public String  returnLength(long opcode)
				{
					return ( ( (opcode & (1 << 23) ) > 0)?"+":"-")+"[r"+( ( (opcode & 0xf00) >> 4 ) | (opcode & 0xf))+"]";
				}
				})},
			new InOut[]{new ConditionInOut(20, 0, 12, 4), new ConfirmedInOut(16, 4),
			new ConditionInOut(20, 1, 16, 4, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					//return "[r"+(( ( (opcode & (1 << 23) ) > 0)?1:-1)*( ( (opcode & 0xf00) >> 4 ) | (opcode & 0xf)))+"]";
					return ( ( (opcode & (1 << 23) ) > 0)?"+":"-")+"[r"+( ( (opcode & 0xf00) >> 4 ) | (opcode & 0xf))+"]";
				}
				})}),
			//(0b000_0000_0_0000_0000_0000_1001_0000),
	HalfWordDataTransferImmOff(new byte[][]{new byte[]{27, 0}, new byte[]{26, 0}, new byte[]{25, 0},  new byte[]{22, 1},
			new byte[]{4, 1},  new byte[]{7, 1}},
			new InOut[]{new ConditionInOut(20, 1, 12, 4), new ConditionInOut(21, 1, 16, 4), 
			new ConditionInOut(20, 0, 16, 4, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "["+(( ( (opcode & (1 << 23) ) > 0)?1:-1)*( ( (opcode & 0xf00) >> 4 ) | (opcode & 0xf)))+"]";
				}
				})},
			new InOut[]{new ConditionInOut(20, 0, 12, 4), new ConfirmedInOut(16, 4),
			new ConditionInOut(20, 1, 16, 4, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					return "["+(( ( (opcode & (1 << 23) ) > 0)?1:-1)*( ( (opcode & 0xf00) >> 4 ) | (opcode & 0xf)))+"]";
				}
				})}),//(0b000_0010_0_0000_0000_0000_1001_0000),
//	PSRTransfer       (new byte[][]{new byte[]{27, 0}, new byte[]{26, 0},
//									new byte[]{24, 1}, new byte[]{23, 0},
//									new byte[]{20, 0}
//								}, 
//			new InOut[]{new ConditionInOut(21, 0, 12, 4), new ConditionInOut(21,1, "cpsr_i")}, 
//			new InOut[]{new ConditionInOut(21, 0, "cpsr_i"), new ConditionInOut(21, 1, 0, 4)}),
	AdvancedCoprocDataTransfer        (new byte[][]{new byte[]{31, 1}, new byte[]{30, 1}, new byte[]{29, 1}, new byte[]{28, 1},
			new byte[]{27, 0}, new byte[]{26, 1}, new byte[]{25, 0}, new byte[]{24, 0}}, //vld1 to 4, vst1 to 4
			new InOut[]{new ConditionInOut(21, 1, "disregard:curls", 12, 4, 8, 4), new ConfirmedInOut(16, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long rm = opcode&0xf;
							if(rm == 0xd)
							{
								return "";
							}
							return "disregard";
						}
					}), new ConditionInOut(21, 0, 16, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long rm = opcode&0xf;
							if(rm == 0xd || rm == 0xf)
							{
								return "";
							}
							return "+[r"+rm+"]";//+size bits at 6 and 7
						}
					})
			},
			new InOut[]{new ConditionInOut(21, 1, 16, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long rm = opcode&0xf;
							if(rm == 0xd || rm == 0xf)
							{
								return "";
							}
							return "+[r"+rm+"]";//+size bits at 6 and 7
						}
					}), new ConditionInOut(21, 0, "disregard:curls", 12, 4, 8, 4)
			}),
	AdvancedCoprocDataOperation       (new byte[][]{new byte[]{31, 1}, new byte[]{30, 1}, new byte[]{29, 1}, new byte[]{28, 1},
			new byte[]{27, 0}, new byte[]{26, 0}, new byte[]{25, 1}},
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
	AdvancedCoprocRegTransfer         (new byte[][]{new byte[]{31, 1}, new byte[]{30, 1}, new byte[]{29, 1}, new byte[]{28, 1},
			new byte[]{27, 1}, new byte[]{26, 0}, new byte[]{25, 0} }, //vld1 to 4, vst1 to 4
			new InOut[]{new ConditionInOut(21, 1, "disregard:curls", 12, 4, 8, 4), new ConfirmedInOut(16, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long rm = opcode&0xf;
							if(rm == 0xd)
							{
								return "";
							}
							return "disregard";
						}
					}), new ConditionInOut(21, 0, 16, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long rm = opcode&0xf;
							if(rm == 0xd || rm == 0xf)
							{
								return "";
							}
							return "+[r"+rm+"]";//+size bits at 6 and 7
						}
					})
			},
			new InOut[]{new ConditionInOut(21, 1, 16, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long rm = opcode&0xf;
							if(rm == 0xd || rm == 0xf)
							{
								return "";
							}
							return "+[r"+rm+"]";//+size bits at 6 and 7
						}
					}), new ConditionInOut(21, 0, "disregard:curls", 12, 4, 8, 4)
			}),/*(new byte[][]{new byte[]{31, 1}, new byte[]{30, 1}, new byte[]{29, 1}, new byte[]{28, 1},
			new byte[]{27, 1}, new byte[]{26, 1}, new byte[]{25, 1}, new byte[]{24, 0}, new byte[]{4, 1}},
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
					}), new ConditionInOut(20, 1, "cr", 0, 4, 8, 4)}),//(0b111_0000_0_0000_0000_0000_0001_0000),*/
	DataProcPSRTransfer       (new byte[][]{new byte[]{27, 0}, new byte[]{26, 0}}, 
			new InOut[]{new ConditionInOut(20, 1, "cpsr"), new ConfirmedInOut(12, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							if(((opcode>>24)&1)==0)return "";
							if(((opcode>>16)&0xf)==0 && ((opcode>>21)&0x1f)==0x1d)return "";
							if(((opcode>>20)&0xff)==0x30)return "";
							if(((opcode>>20)&0xff)==0x34)return "";
							return "disregard";
						}
					})}, 
			new InOut[]{new ConditionInOut(25, 0, 0, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							if(((opcode>>4)&0xf) == 7 && ((opcode >>20)&0xff) == 0x16)
							{
								return "disregard";
							}
							return "";
						}
					}), new ConfirmedInOut(16, 4, true, new AddressLengthReturner(){
					//}), new ConditionInOut(21, 0, 16, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							if(((opcode>>20)&0xff)==0x30)return "disregard";
							if(((opcode>>20)&0xff)==0x34)return "disregard";
							if(((opcode>>20)&0xf)==0x2)return "disregard";
							return "";
						}
						})}),
	SingleDataTransfer        (new byte[][]{new byte[]{27, 0}, new byte[]{26, 1}}, 
			new InOut[]{new ConditionInOut(20, 1, 12, 4), new ConditionInOut(20, 0, 16, 4, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					if((opcode & (1 << 25)) == 0)
					{
						return "";
					}
					else
					{
						return "+[r"+(opcode&0xf)+"]";
					}
					//if((opcode & (1 << 25)) == 0)
					//{
					//	return "["+shift(( ( ( opcode & (1 << 23) ) > 0 )?1:-1 ) , (opcode & 0xf), ((opcode&0xff0)>>4))+"]";
					//}
					//else
					//{
					//	return "["+(( ( ( opcode & (1 << 23) ) > 0 )?1:-1 ) * (opcode & 0xfff))+"]";
					//}
				}
				})},
			new InOut[]{new ConditionInOut(20, 0, 12, 4), new ConditionInOut(20, 1, 16, 4, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					if((opcode & (1 << 25)) == 0)
					{
						return "";
					}
					else
					{
						return "+[r"+(opcode&0xf)+"]";
					}
					//if((opcode & (1 << 25)) == 0)
					//{
					//	return "["+shift(( ( ( opcode & (1 << 23) ) > 0 )?1:-1 ) , (opcode & 0xf),((opcode&0xff0)>>4))+"]";
					//}
					//else
					//{
					//	return "["+(( ( ( opcode & (1 << 23) ) > 0 )?1:-1 ) * (opcode & 0xfff))+"]";
					//}
				}
				})}
				),//(0b010_0000_0_0000_0000_0000_0000_0000),
	Undefined                 (new byte[][]{new byte[]{27, 0}, new byte[]{26, 1}, new byte[]{25, 1}, new byte[]{4, 1}}, null, null),//(0b011_0000_0_0000_0000_0000_0001_0000),
	BlockDataTransfer         (new byte[][]{new byte[]{27, 1}, new byte[]{26, 0}, new byte[]{25, 0}},
	//		new InOut[]{new ConditionInOut(20, 1, 12, 4), new ConditionInOut(20, 0, 16, 4, true, new AddressLengthReturner(){
	//			public String returnLength(long opcode)
	//			{
	//				if((opcode & (1 << 25)) == 0)
	//				{
	//					return "["+(( ( ( opcode & (1 << 23) ) > 0 )?1:-1 ) * (opcode & 0xf))+"] shift = "+((opcode&0xff0)>>4);
	//				}
	//				else
	//				{
	//					return "["+(( ( ( opcode & (1 << 23) ) > 0 )?1:-1 ) * (opcode & 0xfff))+"]";
	//				}
	//			}
	//			})},
	//		new InOut[]{new ConditionInOut(20, 0, 12, 4), new ConditionInOut(20, 1, 16, 4, true, new AddressLengthReturner(){
	//			public String returnLength(long opcode)
	//			{
	//				if((opcode & (1 << 25)) == 0)
	//				{
	//					return "["+(( ( ( opcode & (1 << 23) ) > 0 )?1:-1 ) * (opcode & 0xf))+"] shift = "+((opcode&0xff0)>>4);
	//				}
	//				else
	//				{
	//					return ""+(( ( ( opcode & (1 << 23) ) > 0 )?1:-1 ) * (opcode & 0xfff));
	//				}
	//			}
	//			})}),
			new InOut[]{new ConditionInOut(21, 1, 16, 4), new ConditionInOut(20, 1, 0, 15), new ConditionInOut(20, 0, 16, 4, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{//nobody cares about this anymore!!!
					int count = 0;
					for(int i = 0; i <= 15; i++)
					{
						if((opcode & (1 << i)) > 0)count++;
					}
					return "";//+(((opcode & (1 << 23)) > 0)? 1:-1)*count;
				}
				})}, 
			new InOut[]{new ConditionInOut(20, 0, 0, 15), new ConditionInOut(20, 1, 16, 4, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{//nobody cares about this anymore!!!
					int count = 0;
					for(int i = 0; i <= 15; i++)
					{
						if((opcode & (1 << i)) > 0)count++;
					}
					return "";//+(((opcode & (1 << 23)) > 0)? 1:-1)*count;
				}
				})}),//(0b100_0000_0_0000_0000_0000_0000_0000),
	Branch                    (new byte[][]{new byte[]{27, 1}, new byte[]{26, 0}, new byte[]{25, 1}}, 
			new InOut[]{new ConditionInOut(24, 1, "r14")},
			new InOut[]{new ConditionInOut(24, 1, "pc")}),//(0b101_0000_0_0000_0000_0000_0000_0000),
	//false ec510f1e :: CoprocDataTransfer [[disregard, [r1]+[-90], cr6], [cp15:0, [r0]disregard, disregard]] to [[r1], [0]] 4c25bac8 ec510f1e      mrrc	15, 1, r0, r1, cr14 ||  101627 1  culprit is: cr6 <=> cr6, 15 || 
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
									if(((opcode >> 4) &0xf) == 1)
									{
									return "cr"+(opcode&0xf);
									}
									else if(((opcode >> 4) &0xf) > 1)
									{
									return "cr"+((opcode&0x7)|((((opcode>>5)&0x1))<<4));
									}
								//	if( ((opcode>>4)&0xf) > 0)
								//	{
								//		return "cr"+((opcode&0x7)<<(((opcode>>4)&0xf)));
								//	}
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
										if(((opcode >> 4) &0xf) == 1)
										{
											return "cr"+(opcode&0xf);
										}
										else if(((opcode >> 4) &0xf) > 1)
										{
											return "cr"+((opcode&0x7)|((((opcode>>5)&0x1))<<4));
											//return "cr"+((opcode&0x7)<<(((opcode>>4)&0xf)));
										}
										else
										{
											return "cr"+(opcode&7);
										}
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
	SWI                       (new byte[][]{new byte[]{27, 1}, new byte[]{26, 1}, new byte[]{25, 1}, new byte[]{24, 1}}, null, null);//(0b111_1000_0_0000_0000_0000_0000_0000);
	public static String shift(int sign, long immediate, long shift_code)
	{
		if((shift_code & 1) == 1)
		{
			return "shift_"+immediate+"_by_r"+(shift_code>>4)+"_code_"+((shift_code>>1)&3)+"_mult_"+sign;
		}
		else
		{
			return "shift_"+immediate+"_by_"+(shift_code>>3)+"_code_"+((shift_code>>1)&3)+"_mult_"+sign;
		}
	}
	//regs places
	//cond flags

	//thumb format
	
	byte[][] op_type_detector;
	InOut[] dests, srcs;
	Ins32BitTypes(byte[][] operation_type_detector, InOut[] dests, InOut[] srcs)
	{
		op_type_detector = operation_type_detector;
		this.dests = dests;
		this.srcs = srcs;
	}

	static Ins32BitTypes getMyType(long opcode)
	{
		if(((opcode>>28)&0xf) < 0xf || true)
		{
			for(Ins32BitTypes i: Ins32BitTypes.values())
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
		else
		{
			return null;//InsThumbEE.getMyType(opcode);
		}
		//return null;
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
