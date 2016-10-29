package net.codepixl.EvolutionSim;

import java.nio.ByteBuffer;

public class Nucleus{
	public static String toDNA(byte[] bytes){
		StringBuilder ret = new StringBuilder();
		for(byte byt : bytes){
			boolean a = (byt & 0x1) != 0;
			boolean b = (byt & 0x2) != 0;
			boolean c = (byt & 0x4) != 0;
			boolean d = (byt & 0x8) != 0;
			boolean e = (byt & 0x10) != 0;
			boolean f = (byt & 0x20) != 0;
			boolean g = (byt & 0x40) != 0;
			boolean h = (byt & 0x80) != 0;
			ret.append(toBase(h,g));
			ret.append(toBase(f,e));
			ret.append(toBase(d,c));
			ret.append(toBase(b,a));
		}
		return ret.toString();
	}
	
	public static char booleanToBin(boolean bool){
		return bool ? '1' : '0';
	}
	
	public static String booleanToBin(boolean... bool){
		String ret = "";
		for(boolean b : bool)
			ret+=booleanToBin(b);
		return ret;
	}
	
	public static byte[] fromDNA(String DNA){
		StringBuilder ret = new StringBuilder();
		for(int i = 0; i < DNA.length(); i+=4){
			String b = "";
			for(int j = 0; j < 4; j++){
				boolean[] bits = fromBase(DNA.charAt(i+j));
				b += bits[0] ? "1" : "0";
				b += bits[1] ? "1" : "0";
			}
			ret.append((char)Integer.parseInt(b,2));
		}
		return ret.toString().getBytes();
	}
	
	public static boolean[] fromBase(char base){
		boolean[] ret = null;
		switch(base){
			case 'G':
				ret = new boolean[] {true, true};
				break;
			case 'T':
				ret = new boolean[] {false, true};
				break;
			case 'A':
				ret = new boolean[] {true, false};
				break;
			default:
				ret = new boolean[] {false,false};
		}
		return ret;
	}
	
	public static char toBase(boolean bit1, boolean bit2){
		if(bit1 && bit2)
			return 'G';
		if(!bit1 && bit2)
			return 'T';
		if(!bit1 && !bit2)
			return 'C';
		if(bit1 && !bit2)
			return 'A';
		System.out.println("CORE MELTDOWN, UNIVERSE IS BROKEN");
		return '!'; //AKA the universe is out of wack
	}
}
