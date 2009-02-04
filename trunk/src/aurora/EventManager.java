/**
 * @(#)EventManager.java
 */

package aurora;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;


/**
 * Implementation of a managed event list.
 * @author Alex Kurzhanskiy
 * @version $Id: EventManager.java,v 1.5.2.5.2.4 2008/12/02 03:38:35 akurzhan Exp $
 */
public class EventManager implements Serializable {
	private static final long serialVersionUID = 6743420186787314016L;
	
	protected Vector<AbstractEvent> eventList = new Vector<AbstractEvent>();
	protected Vector<AbstractEvent> eventStack = new Vector<AbstractEvent>(); // used to store event history for possible rollback
	
	protected AbstractContainer container = null;
	
	
	/**
	 * Initializes the event manager from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if ((p == null) || (!p.hasChildNodes()))
			return res;
		NodeList pp = p.getChildNodes();
		try {
			for (int i = 0; i < pp.getLength(); i++) {
				if (pp.item(i).getNodeName().equals("event")) {
					Class c = Class.forName(pp.item(i).getAttributes().getNamedItem("class").getNodeValue());
					AbstractEvent evt = (AbstractEvent)c.newInstance();
					evt.setEventManager(this);
					if (evt.initFromDOM(pp.item(i)))
						addEvent(evt);
					else
						res = false;
				}
				if (pp.item(i).getNodeName().equals("include")) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(i).getAttributes().getNamedItem("uri").getNodeValue());
					initFromDOM(doc.getChildNodes().item(0));
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Generates XML description of the Event list.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		for (int i = 0; i < eventList.size(); i++) {
			eventList.get(i).xmlDump(out);
			out.print("\n");
		}
		return;
	}
	
	/**
	 * Activates events that are due to happen.<br>
	 * After an event is activated, it is removed from the list.
	 * @param top top level complex Node.
	 * @param ts timestamp.
	 * @return vector of boolean status values for each activated event:
	 * <code>true</code> if operation succeded, <code>false</code> - otherwise.
	 */
	public synchronized Vector<Boolean>	activateCurrentEvents(AbstractNodeComplex top, double ts) throws ExceptionEvent {
		Vector<Boolean> status = new Vector<Boolean>();
		while ((eventList.size() > 0) && (ts >= eventList.firstElement().getTime())) {
			AbstractEvent evt = eventList.firstElement();
			try {
				status.add(evt.activate(top));
				eventStack.add(0, evt);
				eventList.remove(0);
			}
			catch(ExceptionEvent e) {
				eventList.remove(0);
				throw new ExceptionEvent(e.getMessage());
			}
		}	
		return status;
	}
	
	/**
	 * Deactivates events that happened after given timestamp.<br>
	 * After an event is activated, it is removed from the list.
	 * @param top top level complex Node.
	 * @param ts timestamp.
	 * @return vector of boolean status values for each activated event:
	 * <code>true</code> if operation succeded, <code>false</code> - otherwise.
	 */
	public synchronized Vector<Boolean>	deactivateCurrentEvents(AbstractNodeComplex top, double ts) throws ExceptionEvent {
		Vector<Boolean> status = new Vector<Boolean>();
		while ((eventStack.size() > 0) && (ts <= eventStack.firstElement().getTime())) {
			AbstractEvent evt = eventStack.firstElement();
			try {
				status.add(evt.deactivate(top));
				eventList.add(0, evt);
				eventStack.remove(0);
			}
			catch(ExceptionEvent e) {
				eventStack.remove(0);
				throw new ExceptionEvent(e.getMessage());
			}
		}	
		return status;
	}


	/**
	 * Adds event to the event list.<br>
	 * Events in the list are soreted by the time stamp
	 * in the increasing order. 
	 * @param x event object.
	 * @return list index of the added event, <code>-1</code> - if the operation did not succeed.
	 */
	public synchronized int addEvent(AbstractEvent x) {
		int idx = -1;
		if (x == null)
			return idx;
		idx = 0;
		while ((idx < eventList.size()) && (eventList.get(idx).getTime() <= x.getTime()))
			idx++;
		eventList.add(idx, x);
		return idx;
	}
	
	/**
	 * Deletes given event from the list.
	 * @param x event object.
	 * @return list index of the cleared event, <code>-1</code> - if the event was not found.
	 */
	public synchronized int clearEvent(AbstractEvent x) {
		int idx = -1;
		if (x == null)
			return idx;
		idx = eventList.indexOf(x);
		if (idx >= 0)
			eventList.remove(idx);
		return idx;
	}
	
	/**
	 * Deletes event specified by its list index.
	 * @param idx event index in the list.
	 * @return list index of the cleared event, <code>-1</code> - if the event was not found.
	 */
	public synchronized int clearEvent(int idx) {
		if ((idx < 0) || (idx >= eventList.size()))
			return -1;
		eventList.remove(idx);
		return idx;
	}
	
	/**
	 * Empties the event list.
	 */
	public synchronized void clearEventList() {
		eventList.clear();
	}
	
	/**
	 * Returns all events in the list.
	 */
	public final Vector<AbstractEvent> getAllEvents() {
		return eventList;
	}
	
	/**
	 * Returns the event specified by its list index.
	 * @param id event index in the list.
	 * @return event object, <code>null</code> - if the event was not found.
	 */
	public final AbstractEvent getEvent(int idx) {
		AbstractEvent evt = null;
		if ((idx >= 0) && (idx < eventList.size()))
			evt = eventList.get(idx);
		return evt;
	}
	
	/**
	 * Returns vector of events that have to happen before given timestamp.
	 * @param ts timestamp.
	 * @return vector of events that are due.
	 */
	public synchronized Vector<AbstractEvent> getCurrentEvents(double ts) {
		Vector<AbstractEvent> cevts = new Vector<AbstractEvent>();
		while ((eventList.size() > 0) && (ts <= eventList.firstElement().getTime())) {
			cevts.add(eventList.firstElement());
			eventList.remove(0);
		}	
		return cevts;
	}
	
	/**
	 * Returns container to which it belongs.
	 */
	public AbstractContainer getContainer() {
		return container;
	}
	
	/**
	 * Sets container to which the event manager belongs.
	 * @param x container object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setContainer(AbstractContainer x) {
		if (x == null)
			return false;
		container = x;
		return true;
	}
	

}