package aurora.hwc.control.signal;

import aurora.hwc.control.AbstractPanelControllerComplex;

public class PanelControllerCoordinated extends AbstractPanelControllerComplex {
	private static final long serialVersionUID = 2780430939589570271L;


	public void fillPanel() {

		PanelControllerActuated panelactuated = new PanelControllerActuated();
		panelactuated.initialize(myMonitor);
		panelactuated.fillPanel();
		
		PanelControllerPretimed panelpretimed =  new PanelControllerPretimed();
		panelpretimed.initialize(myMonitor);
		panelpretimed.fillPanel();
	
		add(panelactuated);
		add(panelpretimed);
	}

}
