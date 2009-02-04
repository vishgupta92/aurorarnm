package aurora.hwc.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import aurora.AuroraConstants;
import aurora.hwc.common.WindowAbout;
import aurora.util.UtilGUI;


/**
 * GISImporter:
 * - Open shp file
 * - Filter by type
 * - Remove reduntant edges
 * - Export to aurora 
 * 
 * @author jkwon
 * Convert GIS shape file to Aurora configuration XML files
 *  
 */
public class GISImporter extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final String separator = 
		"=======================================\n";

	private File file = null;
	private Container content ;
	private JLabel statusBar = new JLabel(" ");
	private JTextArea logText =null;
	private GISObject gisObject;


	/**
	 * Constructor which takes care of most steps
	 */
	public GISImporter(String[] args) throws Exception{
		super("Aurora GIS Importer");
		content = getContentPane();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setJMenuBar(createMenuBar());
		createButtons();

		JPanel logPanel = new JPanel();
		logPanel.setBorder(BorderFactory.createTitledBorder("LOG"));
		logText = new JTextArea();
		JScrollPane scroller = new JScrollPane(logText);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		myLog("Log of Aurora GIS Importer\n");
		logPanel.add(scroller, BorderLayout.CENTER);
		scroller.setPreferredSize(new Dimension(780, 460));
		content.add(logPanel, BorderLayout.CENTER);
		content.add(statusBar, BorderLayout.SOUTH);
		setSize(800, 600);
        ImageIcon icon = UtilGUI.createImageIcon("/icons/gislogo2.jpg");
        if (icon != null)
        	setIconImage(icon.getImage());
        setVisible(true);

		gisObject = new GISObject(this);
		myLog("Starting...");
		timestamp();
		
		if (false){
			file = new File("C:\\WORK\\aurora\\config\\caalamst_tiny.shp");
			openFile(file.toURI().toURL());
		}
	}



	public static void main(String[] args) throws Exception {
		final GISImporter gisImporter = new GISImporter(args);
	}

	/**
	 * read in the input shape file into native GIS feature collection
	 */
	private  void openFile(URL shape) {
		myLog(separator + "Opening file...");
		try {
			if( shape == null) {
				shape = openURL();
			}
			if (shape == null) return;
			gisObject.openShapefile(shape);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		gisObject.debug();
		buttonOpen.setEnabled(true);
		buttonFilterType.setEnabled(true);
		buttonFilterSimplify.setEnabled(true);
		buttonSaveConfig.setEnabled(true);
		timestamp();
	}



	/**
	 * step 2: Type filter
	 */

	private  void typeFilter(){
		myLog(separator + "Filtering by road types...");
		if (gisObject.getFeatureCollection()  == null) {
			myLog("featureCollection is empty!");
			return;
		}

		GISImporterTypeWindow vw = 
			new GISImporterTypeWindow(this, true, gisObject.getFeatureCollection());

		if (!vw.getAnswer()) {
			myLog("Operation cancelled.");
			return;
		}

		String attributeName = vw.getAttributeName(); 

		Object[] avs = vw.getAttributeValues(); 

		if (vw.getValueText() != null && vw.getValueText().length()>0){
			avs = vw.getValueText().split(",");
		}

		if (avs.length<1) return;
		gisObject.typeFilter(attributeName, avs);
		timestamp();
	}

	/**
	 * step 3: simplifying edges
	 */
	private  void simplifyEdges(){
		myLog(separator + "Simplifying edges...");
		gisObject.simplifyEdges();
		buttonFilterSimplify.setEnabled(false);
		buttonFilterType.setEnabled(false);
		myLog("For another edge-simplification, re-open the GIS file");
		timestamp();
	}


	/**
	 * step 4: exporting to aurora XML (can borrow Alex' codes?) 
	 */
	private void exportToXML(){
		myLog(separator + "Exporting to XML...");
		if (gisObject.getFeatureCollection() == null) {
			myLog("featureCollection is empty!");
			return;
		}
		File newFile = getNewXMLFile( file );
		if (newFile == null) {
			myLog("Exporting operation cancelled!");
			return;
		}
		gisObject.exportToXML(newFile);
		
		timestamp();
	}


	/**
	 * Export to shapefile 
	 * cf. http://osdir.com/ml/gis.geotools2.user/2006-07/msg00060.html
	 */
	private void exportToGIS() {
		myLog(separator + "Exporting to GIS...");

		if (gisObject.getFeatureCollection() == null) {
			myLog("featureCollection is empty!");
			return;
		}

		File newFile = getNewShapeFile(file, 2);
		if (newFile ==null ) {
			myLog("GIS Export Canceled");
			return;
		}
		gisObject.exportToGIS(newFile);

		timestamp();
	}




	/**
	 * This method prompts the user for an XML file
	 * 
	 * @param file2
	 * @return
	 */
	private static File getNewXMLFile(File file2) {
		String path = file2.getAbsolutePath();
		String newPath = path.substring(0,path.length()-4) + ".xml";

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save XML file");
		chooser.setSelectedFile( new File( newPath ));		
		chooser.setFileFilter( new FileFilter(){
			public boolean accept( File f ) {
				return f.isDirectory() || f.getPath().endsWith("xml") || f.getPath().endsWith("XML");
			}
			public String getDescription() {
				return "XML file";
			}
		});
		int returnVal = chooser.showSaveDialog(null);
		if(returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File newFile = chooser.getSelectedFile();

		return newFile;
	}



	/**
	 * This method will prompt the user for a shapefile.
	 * 
	 * @return url to selected shapefile.
	 * @throws MalformedURLException
	 */
	private  URL openURL() throws MalformedURLException {
		URL shapeURL = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Shape file", "shp", "SHP"));
		fileChooser.setSelectedFile(new 
				File("C:\\WORK\\aurora\\demo\\demo.shp"));
				//File("."));
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			shapeURL = file.toURI().toURL();
		} else {
		}
		return shapeURL;
	}


	/**
	 * This method prompts the user for a new GIS shapefile name
	 * 
	 * @param file
	 * @param level
	 * @return
	 */
	private static File getNewShapeFile(File file, Integer level) {
		String path = null;
		String newPath = null;
		if (file != null){
			path = file.getAbsolutePath();
//			String newPath = path.substring(0,path.length()-4) + "2.shp";
			newPath = path.substring(0,path.length()-4) + level + ".shp";
		}

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save shapefile");
		chooser.setSelectedFile( new File( newPath ));		
		chooser.setFileFilter( new FileFilter(){
			public boolean accept( File f ) {
				return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP");
			}
			public String getDescription() {
				return "Shapefiles";
			}
		});
		int returnVal = chooser.showSaveDialog(null);

		if(returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File newFile = chooser.getSelectedFile();
		if( newFile.equals( file )){
			System.out.println("Cannot replace "+file);
			return null;
		}
		return newFile;
	}

	/**
	 * put the current timestamp in the log window
	 */
	private  void timestamp() {
		String datestr = (new Date()).toString();
		logText.append( "("+ datestr + ")" + "\n");
		logText.setCaretPosition(logText.getText().length());
	}



	/**
	 * send log message to the log screen
	 */
	private  void myLog(String logmessage) {
		logText.append(logmessage + "\n");
		logText.setCaretPosition(logText.getText().length());
	}

	
	private HashMap<String, JMenuItem> cmd2item = new HashMap<String, JMenuItem>();
	private final static String cmdFileNewSimulation = "FileNewSimulation";
	private final static String cmdFileOpenFile = "FileOpenFile";
	private final static String cmdFileSaveConfiguration = "FileSaveConfiguration";
	private final static String cmdFileSaveAs = "FileSaveAs";
	private final static String cmdFileExit = "FileExit";
	private final static String cmdViewZoomIn = "ViewZoomIn";
	private final static String cmdViewZoomOut = "ViewZoomOut";
	private final static String cmdFilterGeo= "FilterGeo";
	private final static String cmdFilterType= "FilterType";
	private final static String cmdFilterSimplify= "FilterSimplify";
	private final static String cmdHelpAbout= "HelpAbout";
	private final static String cmdHelpContactTOPL = "HelpContactTOPL";

	private JButton buttonOpen;
	private JButton buttonFilterType;
	private JButton buttonFilterSimplify;
	private JButton buttonSaveConfig;

	/**
	 * create buttons
	 */
	private void createButtons(){
		Font font = new Font("SansSerif", Font.BOLD, 16);
		JPanel controls = new JPanel();
		
		
		buttonOpen = new JButton("Open GIS File");
		buttonOpen.setActionCommand(cmdFileOpenFile);
		buttonOpen.addActionListener(this);
		controls.add(buttonOpen);
		buttonOpen.setFont(font);
		
		buttonFilterType = new JButton("Type Filter...");
		buttonFilterType.setActionCommand(cmdFilterType);
		buttonFilterType.addActionListener(this);
		controls.add(buttonFilterType);
		buttonFilterType.setFont(font);

		buttonFilterSimplify = new JButton("Simplify Edges...");
		buttonFilterSimplify.setActionCommand(cmdFilterSimplify);
		buttonFilterSimplify.addActionListener(this);
		controls.add(buttonFilterSimplify);
		buttonFilterSimplify.setFont(font);

		buttonSaveConfig = new JButton("Save as XML...");
		buttonSaveConfig.setActionCommand(cmdFileSaveConfiguration);
		buttonSaveConfig.addActionListener(this);
		controls.add(buttonSaveConfig);
		buttonSaveConfig.setFont(font);

		JButton buttonSaveAs = new JButton("Save as GIS...");
		buttonSaveAs.setActionCommand(cmdFileSaveAs);
		buttonSaveAs.addActionListener(this);
//		controls.add(buttonSaveAs);
		buttonSaveAs.setFont(font);
		GridLayout experimentLayout = new GridLayout(1,5);
		controls.setLayout(experimentLayout);
		controls.setPreferredSize(new Dimension(800, 40));

		buttonFilterType.setEnabled(false);
		buttonFilterSimplify.setEnabled(false);
		buttonSaveConfig.setEnabled(false);

		controls.setBackground(Color.DARK_GRAY);
		content.add(controls, BorderLayout.NORTH);

	}

	/**
	 * Creates menu bar.
	 * @return menu bar.
	 */
	private JMenuBar createMenuBar() {
		JMenuItem item;
		JMenu menu;
		JMenuBar mainMenu = new JMenuBar();
		// Filemenu
		menu = new JMenu("File");
		item = new JMenuItem("New Configuration");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		item.addActionListener(this);
		item.setEnabled(true);
		item.setActionCommand(cmdFileNewSimulation);
		cmd2item.put(cmdFileNewSimulation, item);
		item = new JMenuItem("Open GIS...");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		item.addActionListener(this);
		item.setActionCommand(cmdFileOpenFile);
		cmd2item.put(cmdFileOpenFile, item);
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Save As XML...");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		item.addActionListener(this);
		item.setEnabled(true);
		item.setActionCommand(cmdFileSaveConfiguration);
		cmd2item.put(cmdFileSaveConfiguration, item);
		menu.add(item);
		item = new JMenuItem("Save As GIS...");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		item.addActionListener(this);
		item.setEnabled(true);
		item.setActionCommand(cmdFileSaveAs);
		cmd2item.put(cmdFileSaveAs, item);
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Exit");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		item.addActionListener(this);
		item.setActionCommand(cmdFileExit);
		cmd2item.put(cmdFileExit, item);
		menu.add(item);
		mainMenu.add(menu);
		// View menu
		menu = new JMenu("View");
		item = new JMenuItem("Zoom In");
		item.setAccelerator(KeyStroke.getKeyStroke('+'));
		item.addActionListener(this);
		item.setActionCommand(cmdViewZoomIn);
		cmd2item.put(cmdViewZoomIn, item);
		menu.add(item);
		item = new JMenuItem("Zoom Out");
		item.setAccelerator(KeyStroke.getKeyStroke('-'));
		item.addActionListener(this);
		item.setActionCommand(cmdViewZoomOut);
		cmd2item.put(cmdViewZoomOut, item);
		menu.add(item);
		// Mode menu
		menu = new JMenu("Filter");
		item = new JMenuItem("Type Filter...");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		item.addActionListener(this);
		item.setActionCommand(cmdFilterType);
		cmd2item.put(cmdFilterType, item);
		menu.add(item);
		item = new JMenuItem("Simplify Edges...");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
		item.addActionListener(this);
		item.setActionCommand(cmdFilterSimplify);
		cmd2item.put(cmdFilterSimplify, item);
		menu.add(item);
		mainMenu.add(menu);
		//
		menu = new JMenu("Help");
		item = new JMenuItem("About");
		item.addActionListener(this);
		item.setActionCommand(cmdHelpAbout);
		cmd2item.put(cmdHelpAbout, item);
		menu.add(item);
		item = new JMenuItem("Contact TOPL");
		item.addActionListener(this);
		item.setActionCommand(cmdHelpContactTOPL);
		cmd2item.put(cmdHelpContactTOPL, item);
        menu.add(item);

		mainMenu.add(menu);

		return mainMenu;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdFileOpenFile.equals(cmd)) {
			openFile(null);
			return;
		}
		if (cmdFilterType.equals(cmd)) {
			typeFilter();
			return;
		}
		if (cmdFilterSimplify.equals(cmd)) {
			simplifyEdges();
			return;
		}
		if (cmdFileSaveAs.equals(cmd)) {
			exportToGIS();
			return;
		}
		if (cmdFileSaveConfiguration.equals(cmd)){
			exportToXML();
			return;
		}
		if (cmdHelpAbout.equals(cmd)) {
			new WindowAbout("GISImporter", this);
			return;
		}
		if (cmdHelpContactTOPL.equals(cmd)) {
			Desktop desktop = null;
			String email = AuroraConstants.CONTACT_EMAIL;
			if (Desktop.isDesktopSupported()) {
				desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.MAIL))
					try {
						desktop.mail(new URI("mailto", email, null));
						return;
					}
					catch(Exception exp) { }
			}
			JOptionPane.showMessageDialog(this, "Cannot launch email client...\n Please, email your questions to\n" + email , "", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (cmdFileExit.equals(cmd)){
			statusBar.setText("Exiting...");
			setVisible(false);
			dispose();
			System.exit(0);
		}

	}

	public JTextArea getLogText(){
		return logText;
	}

}

/*
 * TODO: extra data to store in XML?? (for other people) what other fields to add?
 * TODO: intelligent edge simplification; say we don't want to merge when two edges 
 * have different road types, # of lanes, etc.
 * TODO: efficiency: currently, export to GIS leads to a very large *.dbf file
 */
