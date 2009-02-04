/**
 * @(#)DBDerby.java
 */

package aurora.hwc;

import java.io.*;
import java.sql.*;
import java.util.*;
import aurora.*;


/**
 * Derby specific implementation of database interface. 
 * @author Alex Kurzhanskiy
 * @version $Id: DBDerby.java,v 1.4.2.1.4.5 2008/12/11 20:42:37 akurzhan Exp $
 */
public final class DBDerby implements DataStorage, Serializable {
	private static final long serialVersionUID = -714746697588917594L;
	
	String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	String protocol = "jdbc:derby:";
	String dbname = "aurora_hwc_data";
	String user = "aurora";
	String password = "aurora";
	String home = null;
	
	String ldbuf = null;
	String snidbuf = null;
	String snodbuf = null;
	String snsrmbuf = null;
    
	Connection conn;
	Statement sqlcmd;
	
	
	public DBDerby() { }
	public DBDerby(String dir) { home = dir; }
	
    
	/**
	 * Initializes database interface.<br>
	 * Starts Derby JDBC driver, creates <code>aurora_data</code> database
	 * and connects to it.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase
	 */
	public boolean initialize() throws ExceptionDatabase {
		boolean rs = true;
		if (home != null)
			System.getProperties().put("derby.system.home", home);
		try {
			Class.forName(driver).newInstance();
			Properties props = new Properties();
			props.put("user", user);
			props.put("password", password);
			conn = DriverManager.getConnection(protocol + dbname + ";create=true", props);
			conn.setAutoCommit(false);
			sqlcmd = conn.createStatement();
			ResultSet res = sqlcmd.executeQuery("select count(*) from sys.systables where lower(tablename)='simulations'");
			if (!res.next())
				throw new ExceptionDatabase("Request to SYSTABLES failed.");
			if (res.getInt(1) == 0) {
				// list of simulations
				sqlcmd.addBatch("create table simulations(simno int not null unique, description varchar(1024), ctime timestamp)");
				// link data
				sqlcmd.addBatch("create table ldata(id int not null, simno int not null, ts int not null, density double not null, speed double not null, vht double not null, vmt double not null, delay double not null, ploss double not null, demand double not null, dcrit double not null, djam double not null, fmax double not null, qsize double not null, qmax double not null)");
				// simple node data
				sqlcmd.addBatch("create table snidata(id int not null, simno int not null, ts int not null, ilid int not null, ilflow double not null)");
				sqlcmd.addBatch("create table snodata(id int not null, simno int not null, ts int not null, olid int not null, olflow double not null)");
				sqlcmd.addBatch("create table snsrm(id int not null, simno int not null, ts int not null, ilid int not null, olid int not null, sratio double not null)");
				// complex node data
				sqlcmd.addBatch("create table cndata(id int not null, simno int not null, ts int not null, tp double not null)");
				sqlcmd.addBatch("create table cnidata(id int not null, simno int not null, ts int not null, ilid int not null, ilflow double not null)");
				sqlcmd.addBatch("create table cnodata(id int not null, simno int not null, ts int not null, olid int not null, olflow double not null)");
				int[] status = sqlcmd.executeBatch();
				sqlcmd.clearBatch();
				for (int i = 0; i < status.length; i++)
					rs &= (status[i] >= 0);
				/*
				 * NOTE: as of yet we do not index the tables;
				 *       however, for the future, possible column order is
				 *       simno, id, [ilid | olid]
				 */ 
			}
			conn.commit();
		}
		catch(Exception e) {
			throw new ExceptionDatabase(sqlErrorMessage(e));
		}
		return rs;
	}

	/**
	 * Saves current Link state into the Derby database.
	 * @param x Link object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase
	 */
	public boolean saveLinkData(AbstractLink x) throws ExceptionDatabase {
		AbstractLinkHWC lnk = (AbstractLinkHWC)x;
		String stmt;
		if (ldbuf == null) {
			ldbuf = "";
			stmt = "insert into ldata values (";
		}
		else
			stmt = ", (";			
		stmt += Integer.toString(lnk.getId()) + ", ";
		stmt += Integer.toString(lnk.getMyNetwork().getSimNo()) + ", ";
		stmt += Integer.toString(lnk.getTS()) + ", ";
		stmt += lnk.getDensity().toString() + ", ";
		stmt += lnk.getSpeed().toString() + ", ";
		stmt += Double.toString(lnk.getVHT()) + ", ";
		stmt += Double.toString(lnk.getVMT()) + ", ";
		stmt += Double.toString(lnk.getDelay()) + ", ";
		stmt += Double.toString(lnk.getPLoss()) + ", ";
		stmt += Double.toString(lnk.getCriticalDensity()) + ", ";
		stmt += Double.toString(lnk.getJamDensity()) + ", ";
		stmt += Double.toString(lnk.getMaxFlow()) + ", ";
		stmt += lnk.getDemand().toString() + ", ";
		stmt += lnk.getQueue().toString() + ", ";
		stmt += Double.toString(lnk.getQueueMax()) + ")";
		ldbuf += stmt;
		return true;
	}

	/**
	 * Saves current Node state into the Derby database.
	 * @param x Node object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase
	 */
	public boolean saveNodeData(AbstractNode x) throws ExceptionDatabase {
		int i;
		double val;
		boolean res = true;
		Vector<Object> inputs = x.getInputs();
		Vector<Object> outputs = x.getOutputs();
		Vector<AbstractNetworkElement> preds = x.getPredecessors();
		Vector<AbstractNetworkElement> succs = x.getSuccessors();
		String stmt;
		try {
			if (x.isSimple()) {
				AbstractNodeHWC nd = (AbstractNodeHWC)x;
				AuroraIntervalVector[][] srm = nd.getSplitRatioMatrix();
				if (snidbuf == null) {
					snidbuf = "";
					stmt = "insert into snidata values (";
				}
				else
					stmt = ", (";
				stmt += Integer.toString(nd.getId()) + ", ";
				stmt += Integer.toString(nd.getMyNetwork().getSimNo()) + ", ";
				stmt += Integer.toString(nd.getTS()) + ", ";
				for (i = 0; i < inputs.size(); i++) {
					String substmt = stmt;
					substmt += Integer.toString(preds.get(i).getId()) + ", ";
					if (inputs.get(i) != null)
						val = (Double)inputs.get(i);
					else
						val = 0.0;
					substmt += Double.toString(val) + ")";
					snidbuf += substmt;
				}
				if (snodbuf == null) {
					snodbuf = "";
					stmt = "insert into snodata values(";
				}
				else
					stmt = ", (";
				stmt += Integer.toString(nd.getId()) + ", ";
				stmt += Integer.toString(nd.getMyNetwork().getSimNo()) + ", ";
				stmt += Integer.toString(nd.getTS()) + ", ";
				for (i = 0; i < outputs.size(); i++) {
					String substmt = stmt;
					substmt += Integer.toString(succs.get(i).getId()) + ", ";
					if (outputs.get(i) != null)
						val = (Double)outputs.get(i);
					else
						val = 0.0;
					substmt += Double.toString(val) + ")";
					snodbuf += substmt;
				}
				if (snsrmbuf == null) {
					snsrmbuf = "";
					stmt = "insert into snsrm values(";
				}
				else
					stmt = ", (";
				stmt += Integer.toString(nd.getId()) + ", ";
				stmt += Integer.toString(nd.getMyNetwork().getSimNo()) + ", ";
				stmt += Integer.toString(nd.getTS()) + ", ";
				for (i = 0; i < inputs.size(); i++) {
					for (int j = 0; j < outputs.size(); j++) {
						String substmt = stmt;
						substmt += Integer.toString(preds.get(i).getId()) + ", ";
						substmt += Integer.toString(succs.get(j).getId()) + ", ";
						substmt += srm[i][j].toString() + ")";
						snsrmbuf += substmt;
					}
				}
			}
			else {
				AbstractNodeComplex nd = (AbstractNodeComplex)x;
				stmt = "insert into cndata values(";
				stmt += Integer.toString(nd.getId()) + ", ";
				stmt += Integer.toString(nd.getSimNo()) + ", ";
				stmt += Integer.toString(nd.getTS()) + ", ";
				stmt += Double.toString(nd.getTP()) + ")";
				sqlcmd.addBatch(stmt);
				stmt = "insert into cnidata values(";
				stmt += Integer.toString(nd.getId()) + ", ";
				stmt += Integer.toString(nd.getSimNo()) + ", ";
				stmt += Integer.toString(nd.getTS()) + ", ";
				for (i = 0; i < inputs.size(); i++) {
					String substmt = stmt;
					substmt += Integer.toString(preds.get(i).getId()) + ", ";
					if (inputs.get(i) != null)
						val = (Double)inputs.get(i);
					else
						val = 0.0;
					substmt += Double.toString(val) + ")";
					sqlcmd.addBatch(substmt);
				}
				stmt = "insert into cnodata values(";
				stmt += Integer.toString(nd.getId()) + ", ";
				stmt += Integer.toString(nd.getSimNo()) + ", ";
				stmt += Integer.toString(nd.getTS()) + ", ";
				for (i = 0; i < outputs.size(); i++) {
					String substmt = stmt;
					substmt += Integer.toString(succs.get(i).getId()) + ", ";
					if (outputs.get(i) != null)
						val = (Double)outputs.get(i);
					else
						val = 0.0;
					substmt += Double.toString(val) + ")";
					sqlcmd.addBatch(substmt);
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionDatabase(sqlErrorMessage(e));
		}
		return res;
	}

	/**
	 * Saves current Monitor state into the Derby database.
	 * @param x Monitor object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase
	 */
	public boolean saveMonitorData(AbstractMonitor x)	throws ExceptionDatabase {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * Executes the batch of SQL commands.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase
	 */
	public boolean execute() throws ExceptionDatabase {
		boolean res = true;
		try {
			if (ldbuf != null)
				sqlcmd.addBatch(ldbuf);
			if (snidbuf != null)
				sqlcmd.addBatch(snidbuf);
			if (snodbuf != null)
				sqlcmd.addBatch(snodbuf);
			if (snsrmbuf != null)
				sqlcmd.addBatch(snsrmbuf);
			int[] status = sqlcmd.executeBatch();
			ldbuf = null;
			snidbuf = null;
			snodbuf = null;
			snsrmbuf = null;
			sqlcmd.clearBatch();
			for (int i = 0; i < status.length; i++)
				res &= (status[i] >= 0);
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionDatabase(sqlErrorMessage(e));
		}
		return res;
	}
	
	/**
	 * Commites the changes to the database.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase
	 */
	public boolean commit() throws ExceptionDatabase {
		boolean res = true;
		try {
			conn.commit();
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionDatabase(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Closes connection to the database.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase
	 */
	public boolean close() throws ExceptionDatabase {
		boolean res = true;
		try {
			conn.close();
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionDatabase(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Generates new simulation number and sets it.<br>
	 * New entry is inserted into the <code>simulations</code> table
	 * and assigned to the specified Network Node.
	 * @param top top level Network Node.
	 * @param descr simulation description string.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase
	 */
	public boolean newSimulationNumber(AbstractNodeComplex top, String descr) throws ExceptionDatabase {
		int simno = -1;
		boolean res = false;
		try {
			ResultSet sr = sqlcmd.executeQuery("select max(simno) from simulations");
			if (!sr.next())
				simno = 1;
			else
				simno = sr.getInt(1) + 1;
			String stmt = "insert into simulations values (" + Integer.toString(simno) + ", '" + descr + "', current_timestamp)";
			res = !sqlcmd.execute(stmt);
			res &= top.setSimNo(simno);
			if (res) {
				conn.commit();
			}
		}
		catch(Exception e) {
			throw new ExceptionDatabase(sqlErrorMessage(e));
		}
		return res;
	}
	
	/**
	 * Saves simulation in its current state.
	 * @param top container object.
	 * @param fname name of the file where the simulation is to be saved,
	 * if <code>null</code>, default name is generated.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase
	 */
	public boolean saveSimulation(AbstractContainer top, String fname) throws ExceptionDatabase {
		if ((top == null) || (top.getMyNetwork() == null))
			return false;
		boolean res = true;
		DataStorage db = top.getMyNetwork().getDatabase();
		res &= top.getMyNetwork().setDatabase(null);
		String file;
		if (fname == null)
			file = home + "\\" + dbname + "\\sim" + Integer.toString(top.getMyNetwork().getSimNo()) + ".dat";
		else
			file = fname;
		try {
			ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			os.writeObject(top);
			os.close();
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionDatabase(sqlErrorMessage(e));
		}
		res &= top.getMyNetwork().setDatabase(db);
		return res;
	}
	
	
	
	
	/**
	 * Forms exception message.<br>
	 * This function is only for internal use within this class.
	 * @param e exception handle.
	 * @return exception message.
	 */
	private String sqlErrorMessage(Exception e) {
		if (e.getClass().getSimpleName() != "SQLException")
			return e.getMessage();
		SQLException sqlerr = (SQLException)e;
		String msg = "";
		while (sqlerr != null) {
			msg += sqlerr.getMessage();
			sqlerr = sqlerr.getNextException();
			if (sqlerr != null)
				msg += ";\n";
		}
		return msg;
	}

}
