package bookmgmt.vpplugin;

import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;

public class GenerateDiagramsAction implements VPActionController {

	@Override
	public void performAction(VPAction action) {
		new DiagramGenerator().generateAll();
	}

	@Override
	public void update(VPAction action) {
	}
}
