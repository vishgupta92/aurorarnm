package aurora.hwc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.AbstractMonitor;
import aurora.ExceptionConfiguration;
import aurora.ExceptionDatabase;
import aurora.ExceptionSimulation;

public class MonitorSimpleOutput extends AbstractMonitor {

	private static final long serialVersionUID = -5783278301498493107L;

	private Vector<AbstractLinkHWC> links = new Vector<AbstractLinkHWC>();
	private BufferedWriter out_time;
	private BufferedWriter out_dty;
	private BufferedWriter out_flw;
	private String prefix;
	private int passnumber = 0;

	@Override
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {

		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		try {
			NodeList pp = p.getChildNodes();
			for (int i = 0; i < pp.getLength(); i++) {
				if (pp.item(i).getNodeName().equals("prefix")) {
					prefix = pp.item(i).getTextContent();
				}
				if (pp.item(i).getNodeName().equals("links")) {
					StringTokenizer st = new StringTokenizer(pp.item(i).getTextContent(), ", \t");
					while (st.hasMoreTokens()) {
						AbstractLinkHWC L = (AbstractLinkHWC) myNetwork.getLinkById(Integer.parseInt(st.nextToken()));
						if(L!=null)
							links.add(L);
						else 
							res = false;
					}					
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}

	@Override
	public boolean initialize() throws ExceptionConfiguration, ExceptionDatabase {

		try {
			File tmpDataFile = myNetwork.getContainer().getMySettings().getTmpDataFile();
			out_time = new BufferedWriter(new FileWriter(tmpDataFile.getParentFile() + File.separator + prefix + "_time.txt"));
			out_dty  = new BufferedWriter(new FileWriter(tmpDataFile.getParentFile() + File.separator + prefix + "_dty.txt"));
			out_flw  = new BufferedWriter(new FileWriter(tmpDataFile.getParentFile() + File.separator + prefix + "_flw.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return super.initialize();

	}

	@Override
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {

		try {
			passnumber++;
			if (passnumber==3){
				out_time.write((ts-1)+"\n");
				for(int i=0;i<links.size();i++){
					out_dty.write(String.format("%.3f\t",links.get(i).getAverageDensity().sum().getCenter()));
					out_flw.write(String.format("%.3f\t",links.get(i).getAverageOutFlow().sum().getCenter()));
				}
				out_dty.write("\n");
				out_flw.write("\n");
			}
			
			if(passnumber==3)
				passnumber=0;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return super.dataUpdate(ts);
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTypeLetterCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTypeString() {
		// TODO Auto-generated method stub
		return null;
	}

}
