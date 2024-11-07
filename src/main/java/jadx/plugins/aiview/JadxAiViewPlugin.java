package jadx.plugins.aiview;

import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.JadxPluginInfo;
import jadx.api.plugins.gui.JadxGuiContext;

public class JadxAiViewPlugin implements JadxPlugin {
	public static final String PLUGIN_ID = "jadx-ai-view";

	private final JadxAiViewOptions options = new JadxAiViewOptions();

	@Override
	public JadxPluginInfo getPluginInfo() {
		return new JadxPluginInfo(PLUGIN_ID, "AI view", "Shows AI analysis of classes/methods in a seperate view");
	}

	@Override
	public void init(JadxPluginContext context) {
		context.registerOptions(options);
		final JadxGuiContext guiContext = context.getGuiContext();
		if (guiContext != null) {
			guiContext.addMenuAction("Jadx AI View", () -> {
				AiViewMain.open(options);
			});
		}
		JadxAiViewAction.addToPopupMenu(context, options);
	}
}
