package aurora.hwc.control.signal;

public class NEMA {

	public final static int _null 	= -1;
	public final static int _1 	= 0;
	public final static int _2 	= 1;
	public final static int _3 	= 2;
	public final static int _4 	= 3;
	public final static int _5 	= 4;
	public final static int _6 	= 5;
	public final static int _7 	= 6;
	public final static int _8 	= 7;
	
	public static int index(int nema){
		if(nema>=1 && nema<=8)
			return nema-1;
		else
			return -1;
	}
}
