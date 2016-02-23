package main.java;
public enum Cond
{
	Z(new int[]{0, 1, 8, 9, 12, 13}), 
	C(new int[]{2, 3, 8, 9}), 
	N(new int[]{4, 5, 10, 11, 12, 13}), 
	V(new int[]{6, 7, 10, 11, 12, 13});
	int[] conds;
	public boolean isMatching(int i)
	{
		for(int ii:conds)
		{
			if(ii == i)return true;
		}
		return false;
	}
	Cond(int[] myconds)
	{
		this.conds = myconds;
	}
}
