package jadx.plugins.aiview;

import jadx.api.JavaNode;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.plugins.JadxPluginContext;

import java.util.function.Consumer;

public class JadxAiViewAction implements Consumer<ICodeNodeRef> {
	private final JadxPluginContext context;
	private final JadxAiViewOptions options;

	public JadxAiViewAction(JadxPluginContext context, JadxAiViewOptions options) {
		this.context = context;
		this.options = options;
	}

	public static void addToPopupMenu(JadxPluginContext context, JadxAiViewOptions options) {
		if (context.getGuiContext() == null) {
			return;
		}
		JadxAiViewAction actionAll = new JadxAiViewAction(context, options);
		context.getGuiContext().addPopupMenuAction("Show AI Analysis", JadxAiViewAction::canActivate, null, actionAll);

		// TODO: Add to Prompt feature i.e users can right click and add a class/method to prompt so that the AI knows what the added method does
		// context.getGuiContext().addPopupMenuAction("Add to prompt", JadxAIAction::canActivate, null, actionAll);
	}

	public static Boolean canActivate(ICodeNodeRef ref) {
		return ref.getAnnType() == ICodeAnnotation.AnnType.METHOD
				|| (ref.getAnnType() == ICodeAnnotation.AnnType.CLASS);
	}


	/* Function invoked on selecting Show AI Analysis */
	@Override
	public void accept(ICodeNodeRef iCodeNodeRef) {
		JavaNode node = context.getDecompiler().getJavaNodeByRef(iCodeNodeRef);
		AiViewMain.open(options, node);
	}
}
