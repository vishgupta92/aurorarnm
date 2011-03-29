package aurora.hwc.fdcalibration;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class FiveMinuteData {
	public int vds;
	public ArrayList<Long> time = new ArrayList<Long>();
	public ArrayList<Float> flw = new ArrayList<Float>();
	public ArrayList<Float> dty = new ArrayList<Float>();
	public ArrayList<Float> spd = new ArrayList<Float>();
	public FiveMinuteData(int v){vds=v;}
	
	public void writetofile(String filename) throws Exception{
		Writer out = new OutputStreamWriter(new FileOutputStream(filename+"_"+vds+".txt"));
		for(int i=0;i<time.size();i++)
			out.write(time.get(i)+"\t"+flw.get(i)+"\t"+dty.get(i)+"\t"+spd.get(i)+"\n");
		out.close();
	}
}