package bookmgmt.vpplugin;

import com.vp.plugin.VPPlugin;
import com.vp.plugin.VPPluginCommandLineSupport;
import com.vp.plugin.VPPluginInfo;

public class DiagramPlugin implements VPPlugin, VPPluginCommandLineSupport {

	@Override
	public void loaded(VPPluginInfo info) {
		// GUI load: do nothing automatically
	}

	@Override
	public void unloaded() {
	}

	@Override
	public void invoke(String[] args) {
		new DiagramGenerator().generateAll();
	}
}
