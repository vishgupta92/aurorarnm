package aurora.hwc.fdcalibration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class PeMSClearinghouseInterpreter {

	public ArrayList<File> PeMSCH_5min;

	public PeMSClearinghouseInterpreter(ArrayList<File> f){
		PeMSCH_5min=f;	
	}
	
    public void Read5minData(ArrayList<Integer> selectedvds,ArrayList<ArrayList<Integer>> selectedlanes,HashMap <Integer,FiveMinuteData> data) throws Exception {
    		
		int i,j,lane;
    	String line;
    	int indexof;
    	ArrayList<Integer> lanes;
    	ArrayList<Float> laneflw = new ArrayList<Float>();
    	ArrayList<Float> laneocc = new ArrayList<Float>();
    	ArrayList<Float> lanespd = new ArrayList<Float>();
        Calendar calendar = Calendar.getInstance();
    	float flw,dty,spd;
    	long time;
    	
    	// step through data file
    	
    	for(i=0;i<PeMSCH_5min.size();i++){
        	
    		BufferedReader fin = new BufferedReader(new FileReader(PeMSCH_5min.get(i)));
            while ((line=fin.readLine())!=null){
                String f[] = line.split(",");
                int vds = Integer.parseInt(f[1]);

                indexof = selectedvds.indexOf(vds);
                if(indexof<0)
                	continue;
                
        		calendar.setTime(ConvertTime(f[0]));
        		time = calendar.getTime().getTime()/1000;
        		
                lanes = selectedlanes.get(indexof);
            	laneflw.clear();
            	laneocc.clear();
            	lanespd.clear();
            	
            	// store in lane-wise ArrayList
                for(j=0;j<lanes.size();j++){
                	lane = lanes.get(j)-1;
                	laneflw.add(Float.parseFloat(f[5*(lane+1)+8]));               
                	laneocc.add(Float.parseFloat(f[5*(lane+1)+9]));            
                	lanespd.add(Float.parseFloat(f[5*(lane+1)+10]));
                }
                
                // compute totals
                flw=0;
                dty=0;
                spd=0;
                for(j=0;j<lanes.size();j++){
                	flw += laneflw.get(j)*12f;
                	spd += lanespd.get(j);
                }
                spd /= lanes.size();
                dty = flw/spd;
                
                // find the data structure and store. 
                FiveMinuteData D = data.get(vds);
                D.flw.add(flw);
                D.dty.add(dty);
                D.spd.add(spd);
                D.time.add(time);
                
            }
            fin.close();     
    	}
    	 
    }
	
    private Date ConvertTime(final String timestr) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        ParsePosition pp = new ParsePosition(0);
        return formatter.parse(timestr,pp);
    }
    
}
