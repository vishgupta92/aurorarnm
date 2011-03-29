package aurora.hwc;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import com.sun.xml.internal.ws.util.StringUtils;

import aurora.AuroraConfigurable;
import aurora.ExceptionConfiguration;

public class GoogleDirectionsCache  implements AuroraConfigurable {

	private String all;
	
	@Override
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(p),new StreamResult(buffer));
			all = buffer.toString();
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}

	@Override
	public void xmlDump(PrintStream out) throws IOException {
		out.print(all+"\n");
	}

	@Override
	public boolean validate() throws ExceptionConfiguration {
		// TODO Auto-generated method stub
		return false;
	}

}
