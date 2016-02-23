package main.java;
import java.io.*;
import java.util.*;

public enum InsThumb2Type implements InsTypeInterface
{
	/**
	 * Bits to determine the type.
	 * Confirmed input
	 * Condition input
	 *
	 * Confirmed output
	 * Condition output
	 * */
	//TODO: POP, PUSH (f8) has a thumb3 type.. got to check that:
	LoadStoreMultiples2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 0},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 0},new byte[]{16+6, 0}
			//new byte[]{15, 0}
			},
			new InOut[]{new ConditionInOut(16+4, 1, 0, 12), new ConditionInOut(16+5, 1, 16+0, 4), 
				new ConditionInOut(16+4, 0, 16+0, 4, true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					int count = 0;
					for(int i = 0; i <= 12; i++)
					{
						if((opcode & ( 1 << i)) > 0)
							count++;
					}
					return "";//"+"( (16+8 == 1?-1:1) *+(count*4));
				}
			})}
			,
			new InOut[]{new ConfirmedInOut(16+0, 4),
				new ConditionInOut(16+4, 1, 16+0, 4,true, new AddressLengthReturner(){
				public String returnLength(long opcode)
				{
					int count = 0;
					for(int i = 0; i <= 12; i++)
					{
						if((opcode & ( 1 << i)) > 0)
							count++;
					}
					return "";//"+"( (16+8 == 1?-1:1) *+(count*4));
				}
			}), new ConditionInOut(16+4, 0, 0, 12)}
			),
	LoadDualXTabBr2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 0},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 0},new byte[]{16+6, 1}, 
			new byte[]{16+4, 1}
			//new byte[]{15, 0}
			},
			new InOut[]{new ConfirmedInOut("", true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long op1 = (opcode >> (16+7)) & 3;
							long op2 = (opcode >> (16+4)) & 3;
							long op3 = (opcode >> (4)) & 7;
							if(op1 == 1 && op2 == 1 && op3 <= 1)
							{
								return "pc";//tbl, tbh ins
							}
							return "r"+((opcode>>12)&0xf);
						}
					}), new ConfirmedInOut("", true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long op1 = (opcode >> (16+7)) & 3;
							long op2 = (opcode >> (16+4)) & 3;
							if((op1 < 2 && op2 == 3) || (op1 >= 2))
							{
								return "r"+((opcode>>8)&0xf);//tbl, tbh ins
							}
							return "";
						}})},
			new InOut[]{new ConfirmedInOut(16+0, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long op1 = (opcode >> (16+7)) & 3;
							long op2 = (opcode >> (16+4)) & 3;
							long op3 = (opcode >> (4)) & 7;
							if(op1 == 1 && op2 == 1 && op3 <= 1)
							{
								return "+[r"+(opcode&0xf)+"]";//tbl, tbh ins
							}
							return "";
						}
					})}
			),
	StoreDualXBr2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 0},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 0},new byte[]{16+6, 1},
			new byte[]{16+4, 0}
			//new byte[]{15, 0}
			},
			new InOut[]{new ConfirmedInOut(12, 4),
						new ConfirmedInOut("", true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long op1 = (opcode >> (16+7)) & 3;
							long op2 = (opcode >> (16+4)) & 3;
							long op3 = (opcode >> (4)) & 7;
							if(op1 == 1 && op2 == 0 && (op3 == 4 || op3 == 5))
							{
								return "r"+(opcode&0xf)+"";//strexb, strexh
							}
							return "r"+((opcode>>8)&0xf);
						}
					})
				},
			new InOut[]{new ConfirmedInOut(16+0, 4, true, new AddressLengthReturner(){public String returnLength(long opcode){return "";}})}
			),
	DataProc2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 0},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 1},//new byte[]{16+6, 0},
			//new byte[]{15, 0}
			},
			new InOut[]{new ConfirmedInOut(8, 4), new ConditionInOut(16+4, 1, "cpsr")},
			new InOut[]{new ConfirmedInOut(16+0, 4), new ConfirmedInOut(0, 4)}
			),
//	CoProc2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
//			new byte[]{16+12, 0},new byte[]{16+11, 1},
//			new byte[]{16+10, 1},//new byte[]{16+9, 0},new byte[]{16+6, 0},
//			//new byte[]{15, 0}
//			},
//			new InOut[]{new ConditionInOut(20, 1, "coproc", 12, 4, 8, 4), new ConditionInOut(20, 0, "coproc", 16, 4, 8, 4), new ConditionInOut(20, 0, "coproc", 0, 4, 8, 4)},
//			new InOut[]{new ConditionInOut(20, 0, "coproc", 12, 4, 8, 4), new ConditionInOut(20, 1, "coproc", 16, 4, 8, 4), new ConditionInOut(20, 1, "coproc", 0, 4, 8, 4)}
//			//Copied from Ins32BitTypes.java CoprocRegTransfer
//			),//(0b111_0000_0_0000_0000_0000_0001_0000),
	DataProcMod2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 0},
			new byte[]{16+9, 0},//new byte[]{16+9, 0},new byte[]{16+6, 0},
			new byte[]{15, 0}
			},
			new InOut[]{new ConfirmedInOut(8, 4), new ConditionInOut(16+4, 1, "cpsr")},
			new InOut[]{new ConfirmedInOut(16+0, 4)}
			),
	DataProcPlainBin2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 0},
			new byte[]{16+9, 1},//new byte[]{16+9, 0},new byte[]{16+6, 0},
			new byte[]{15, 0}
			},
			new InOut[]{new ConfirmedInOut(8, 4)},
			new InOut[]{new ConditionInOut(16+8, 0, "", true, new AddressLengthReturner()
					{
						public String returnLength(long opcode)
						{
							long desc = opcode >> (16+4);
							//System.out.printf("opcode = %x, desc = %x\n", opcode, desc);
							if((desc&0x17) != 4)
							{
								return "r"+((opcode >> 16)&0xf);
							}
							else
							{
								return "";
							}
						}
					})}
			),
	BranchesMisc2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 0},
			//new byte[]{16+9, 0},//new byte[]{16+9, 0},new byte[]{16+6, 0},
			new byte[]{15, 1}
			},
			new InOut[]{new ConfirmedInOut("pc"),
				new ConditionInOut(16+5, 1, 8, 4, true, 
					new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							if(((opcode >> (16+4)) & 0x1f) == 0x1f && ((opcode >> 12)&1) == 0 && ((opcode >> 14)&1) == 0)
							{
								return "";
							}
							return "disregard";
						}
					})},
			new InOut[]{new ConfirmedInOut("cpsr"), 
				new ConditionInOut(16+5, 0, 16+0, 4, true, 
					new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							if(((opcode >> (16+6)) & 7) == 7 && ((opcode >> 12)&1) == 0 && ((opcode >> 14)&1) == 0)
							{
								return "";
							}
							return "disregard";
						}
					})}
			),
	StoreSingle2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 0},new byte[]{16+8, 0},new byte[]{16+4, 0}//,new byte[]{16+8, 0},
			},
			new InOut[]{new ConditionInOut(8,1, 16+0, 4),//writeback
				new ConfirmedInOut(16+0, 4, true, new AddressLengthReturner()
						{
							public String returnLength(long opcode)
							{
								//System.out.printf(" %x, %x, %x, %d\n", opcode, opcode>>(16+7), opcode>>(11), (opcode >> 16)&0xf);
								if(((opcode >> (16+7))&1)==1 || ((opcode >> 11)&1)==1)
								{
									//return "liiiiiiiits";
									return "";
								}
								else
								{
									//System.exit(0);
									return "+[r"+((opcode)&0xf)+"]";
								}
							}
						})
			},
			new InOut[]{new ConfirmedInOut(12, 4), new ConfirmedInOut(16+0, 4)}
			),
	LoadByteMemHint2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 0},new byte[]{16+6, 0},new byte[]{16+5, 0},new byte[]{16+4, 1},
			},
			new InOut[]{new ConditionInOut(8, 1, 16+0, 4), new ConfirmedInOut(12, 4)},
			new InOut[]{new ConfirmedInOut(16+0, 4, true, new AddressLengthReturner(){
					public String returnLength(long opcode)
					{
						return "";
					}
				}),
				new ConditionInOut(16+7, 0, 0, 4, true, new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
								if(((opcode>>6)&0x3f)==0)
								{
									return "";//+[r"+(opcode&0xf)+"]";
								}
								else
								{
									return "disregard";
								}
							}
						})
				}
			),
	LoadHalfWordMemHint2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 0},new byte[]{16+6, 0},new byte[]{16+5, 1},new byte[]{16+4, 1},
			},
			new InOut[]{new ConditionInOut(8, 1, 16+0, 4), new ConfirmedInOut(12, 4)},
			new InOut[]{new ConfirmedInOut(16+0, 4, true, new AddressLengthReturner(){
					public String returnLength(long opcode)
					{
						return "";
					}
				}),
				new ConditionInOut(16+7, 0, 0, 4, true, new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
								if(((opcode>>6)&0x3f)==0)
								{
									return "";//+[r"+(opcode&0xf)+"]";
								}
								else
								{
									return "disregard";
								}
							}
						})
				}
			//new InOut[]{},
			//new InOut[]{}
			),
	LoadWord2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 0},new byte[]{16+6, 1},new byte[]{16+5, 0},new byte[]{16+4, 1},
			},
			new InOut[]{new ConfirmedInOut(16+0, 4, true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							if(((opcode>>(16+7))&3)==0 && ((opcode>>6)&0x3f)==0)
							{
								return "+[r"+(opcode&0xf)+"]";
							}
							else
							{
								return "";
							}
						}
					})},
			new InOut[]{new ConfirmedInOut(12, 4)}
			),
	Undefined2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 0},new byte[]{16+6, 1},new byte[]{16+5, 1},new byte[]{16+4, 1},
			},
			new InOut[]{},
			new InOut[]{}
			),
	DataProcReg2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 1},new byte[]{16+8, 0}//,new byte[]{16+4, 0}//,new byte[]{16+8, 0},
			},
			new InOut[]{new ConfirmedInOut(8, 4), new ConfirmedInOut("", true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long rn = (opcode >> 16)&0xf;
							if(((opcode >> 7)&1) == 1 && rn == 15 && ((opcode>>(16+7))&1)==0)
							{
								return "";
							}
							else
							{
								return "r"+rn;
							}
						}
					})},
			new InOut[]{new ConfirmedInOut(0, 4)}
			),
	MacAbsDiff2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 1},new byte[]{16+8, 1},new byte[]{16+7, 0}//,new byte[]{16+8, 0},
			},
			new InOut[]{new ConfirmedInOut(8, 4)},
			new InOut[]{new ConfirmedInOut(16+0, 4), new ConfirmedInOut(0, 4), new ConfirmedInOut("", true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long ra = (opcode >> 12)&0xf;
							if(ra != 15)
							{
								return "r"+ra;
							}
							else
							{
								return "";
							}
						}
					})}
			),
	LongMac2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			new byte[]{16+12, 1},new byte[]{16+11, 1},
			new byte[]{16+10, 0},new byte[]{16+9, 1},new byte[]{16+8, 1},new byte[]{16+7, 1}//,new byte[]{16+8, 0},
			},
			new InOut[]{new ConfirmedInOut(16+0, 4), new ConfirmedInOut(0, 4) },
			new InOut[]{new ConfirmedInOut(12, 4), new ConfirmedInOut("", true, new AddressLengthReturner(){
					public String returnLength(long opcode)
					{
						long rdhi = (opcode >> 8)&0xf;
						if(rdhi != 15)
						{
							return "r"+rdhi;
						}
						else
						{
							return "";
						}
					}
					})}
			),
	CoProcIns2(new byte[][]{new byte[]{16+15, 1}, new byte[]{16+14, 1},new byte[]{16+13, 1},
			/*new byte[]{16+12, 1},new byte[]{16+11, 1}//pras commented 11 out?,
			 */
			//new byte[]{16+10, 1}//,new byte[]{16+9, 0},new byte[]{16+8, 0},new byte[]{16+4, 0}//,new byte[]{16+8, 0},
			},
			new InOut[]{new ConfirmedInOut("", true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long op1 = (opcode >> (16+4))&0x3f;
							long crd = (opcode >> 12)&0xf;
							long rt = crd;
							boolean ins4 =  ( (op1 & 0x3f) == 5);
							if(ins4)
							{
								return "r"+rt;
							}
							return "";
						}
					}),
					new ConfirmedInOut("", true, new AddressLengthReturner(){
					public String returnLength(long opcode)
					{
						long op1 = (opcode >> (16+4))&0x3f;
						long rn = (opcode >> 16)&0xf;
						boolean ins1 =  ((op1 & 0x3a) != 0 && (op1 & 0x21) == 0);
						boolean ins4 =  ( (op1 & 0x3f) == 5);
						//if(!(ins2 || ins3 || ins5 || ins7))
						if(ins4)
						{
							return "[r"+(rn)+"]";
						}
						if(ins1)
						{
							return "[r"+(rn)+"]";
						}
						return "";
					}
					}), new ConfirmedInOut("coproc", 8, 4, true, new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
								long op1 = (opcode >> (16+4))&0x3f;
								long op2 = (opcode >> (16+4))&1;
								long crd = (opcode >> 12)&0xf;
								long crm = (opcode&0xf);
								long imm8 = (opcode & 0xff);
								boolean ins2 =  ((op1 & 0x3a) != 0 && (op1 & 0x21) == 1);
								boolean ins3 =  ( (op1 & 0x3f) == 4);
								boolean ins5 =  ( (op1 >> 4) == 2 && op2 == 0);
								boolean ins6 =  ( (op1 >> 4) == 2 && op2 == 1 && (op1&1)==0);
								boolean ins7 =  ( (op1 >> 4) == 2 && op2 == 1 && (op1&1)==1);
								if(ins5)
								{
									crd <<= 1;
									crd |= ((opcode >>(16+6))&1);
									return "s"+crd;
								}
								if(ins7)
								{
									return "r"+crd;
								}
								if(ins2)
								{
									String ret = "s"+crd;
									boolean vldr = (( ((opcode>>8)&0xa)==0xa) && ( ((opcode >> (16+4))&3) == 1) && ( ((opcode >> (16+8))&1) == 1));
									if(!vldr)
									{
										for(int i = 0; i < 8; i++)
										{
											if(((imm8>>i)&1) == 1)
											{
												ret = ret+", s"+i;
											}
										}
										return ret;
									}
									long dmask = 0;
									int shift = 0;
									if(((opcode>>8)&1)==0)
									{
										shift = 1;
										dmask = ((opcode>>(16+6))&1);
									}
									else
									{
										shift = 0;
										dmask = (((opcode>>(16+6))&1)<<4);
									}
									//System.out.printf("%x << %d = %x | %x = %x\n", crd, shift, crd<<shift, dmask, (crd<<shift)|dmask);
									crd = (crd<<shift)|(dmask);
									return "s"+crd;
									//return "s"+crd;
								} 
								if(ins6)
								{
									//return "s"+crm;
								}
								if(ins3)
								{
									return "s"+crm+", s"+(crm+1);
								}
								return "";
							}
						}), new ConfirmedInOut("coproc", 8, 4, true, new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
								long op1 = (opcode >> (16+4))&0x3f;
								long op2 = (opcode >> (16+4))&1;
								long rn = (opcode >> 16)&0xf;
								long crn = rn;
								long crd = ( (opcode >> 12)&0xf );
								crd <<= 1;
								crd |= ((op1>>1)&1);
								crd |= (((opcode>>7)&1)<<5);
								boolean ins6 =  ( (op1 >> 4) == 2 && op2 == 1 && (op1&1)==0);
								if(ins6)
								{
									return "s"+crn;
									//return "s"+crd;
								}
								return "";
							}
						})},
			new InOut[]{new ConfirmedInOut("", true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long op1 = (opcode >> (16+4))&0x3f;
							long op2 = (opcode >> (16+4))&1;
							long rn = (opcode >> 16)&0xf;
							boolean ins2 =  ((op1 & 0x3a) != 0 && (op1 & 0x21) == 1);
							boolean ins3 =  ( (op1 & 0x3f) == 4);
							if(ins2 || ins3)
							{
								return "r"+rn;
							}
							return "";
						}
					}), new ConfirmedInOut("", true, new AddressLengthReturner(){
						public String returnLength(long opcode)
						{
							long op1 = (opcode >> (16+4))&0x3f;
							long op2 = (opcode >> (16+4))&1;
							long crd = (opcode >> 12)&0xf;
							long rt = crd;
							boolean ins3 =  ( (op1 & 0x3f) == 4);
							boolean ins6 =  ( (op1 >> 4) == 2 && op2 == 1 && (op1&1)==0);
							if(ins6)
							{
								crd <<= 1;
								crd |= ((op1>>1)&1);
								crd |= (((opcode>>7)&1)<<5);
								return "r"+crd;
							}
							if(ins3)
							{
								return "r"+crd;
							}
							return "";
						}
					}), new ConfirmedInOut("coproc", 8, 4, true, new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
								long op1 = (opcode >> (16+4))&0x3f;
								long op2 = (opcode >> (16+4))&1;
								long crd = (opcode >> 12)&0xf;
								long crm = (opcode&0xf);
								long imm8 = (opcode & 0xff);
								boolean ins1 =  ((op1 & 0x3a) != 0 && (op1 & 0x21) == 0);
								boolean ins4 =  ( (op1 & 0x3f) == 5);
								boolean ins5 =  ( (op1 >> 4) == 2 && op2 == 0);
								boolean ins7 =  ( (op1 >> 4) == 2 && op2 == 1 && (op1&1)==1);
								if(ins7)
								{
									//return "s"+crm;
								}
								if(ins4)
								{
									return "s"+crm+", s"+(crm+1);
								}
								if(ins5)
								{
									crm <<= 1;
									crm |= ((opcode >> 5)&1);
									return "s"+crm;
								}
								if(ins1)
								{
									String ret = "s"+crd;
									boolean vstr = (( ((opcode>>8)&0xa)==0xa) && ( ((opcode >> (16+4))&3) == 0) && ( ((opcode >> (16+8))&1) == 1));
									if(!vstr)
									{
										for(int i = 0; i < 8; i++)
										{
											if(((imm8>>i)&1) == 1)
											{
												ret = ret+", s"+i;
											}
										}
										return ret;
									}
									long dmask = 0;
									int shift = 0;
									if(((opcode>>8)&1)==0)
									{
										shift = 1;
										dmask = ((opcode>>(16+6))&1);
									}
									else
									{
										shift = 0;
										dmask = (((opcode>>(16+6))&1)<<4);
									}
									crd = (crd<<shift)|(dmask);
									return "s"+crd;
								}
								return "";
							}
						}), new ConfirmedInOut("coproc", 8, 4, true, new AddressLengthReturner(){
							public String returnLength(long opcode)
							{
								long op1 = (opcode >> (16+4))&0x3f;
								long op2 = (opcode >> (16+4))&1;
								long rn = (opcode >> 16)&0xf;
								long crn = rn;
								boolean ins5 =  ( (op1 >> 4) == 2 && op2 == 0);
								boolean ins7 =  ( (op1 >> 4) == 2 && op2 == 1 && (op1&1)==1);
								if(ins7)
								{
									crn <<= 1;
									crn |= ((op1>>1)&1);
									crn |= (((opcode>>7)&1)<<5);
									return "s"+crn;
								}
								if(ins5)
								{
									long cflags = (opcode>>(16+4)&3);
									long seven =  (opcode>>(16+7)&1);
									long six =  (opcode>>(6)&1);
									if(cflags == 0 ||six == 0 || seven == 0)
									{
										crn <<= 1;
										crn |= (opcode >> 7)&1;
										return "s"+crn;
									}
								}
								return "";
							}
						})}
			//new InOut[]{new ConditionInOut(20, 1, "coproc", 12, 4, 8, 4), new ConditionInOut(20, 0, "coproc", 16, 4, 8, 4), new ConditionInOut(20, 0, "coproc", 0, 4, 8, 4)},
			//new InOut[]{new ConditionInOut(20, 0, "coproc", 12, 4, 8, 4), new ConditionInOut(20, 1, "coproc", 16, 4, 8, 4), new ConditionInOut(20, 1, "coproc", 0, 4, 8, 4)}
			//Copied from Ins32BitTypes.java CoprocRegTransfer
			);
	byte[][] op_type_detector;
	InOut[] dests, srcs;
	InsThumb2Type(byte[][] operation_type_detector, InOut[] dests, InOut[] srcs)
	{
		op_type_detector = operation_type_detector;
		this.dests = dests;
		this.srcs = srcs;
	}
	static InsThumb2Type getMyType(long opcode)
	{
		for(InsThumb2Type i: InsThumb2Type.values())
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
