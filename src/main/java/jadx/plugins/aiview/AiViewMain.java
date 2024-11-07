package jadx.plugins.aiview;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import jadx.api.JavaNode;

public class AiViewMain {
	public static volatile AiViewFrame aiDialog;
	private static volatile AtomicInteger taskCount = new AtomicInteger();

	public static void open(JadxAiViewOptions options) {
		if (AiViewMain.aiDialog == null || AiViewMain.aiDialog.isWindowClosed()) {
			AiViewMain.aiDialog = new AiViewFrame(options);
			AiViewMain.aiDialog.setVisible(true);
			AiViewMain.aiDialog.toFront();
		}
	}

	public static void open(JadxAiViewOptions options, JavaNode node) {
		open(options);
		Runnable run = new Runnable() {
			@Override
			public void run() {
				taskCount.incrementAndGet();
				AiViewMain.aiDialog.showProgressPanel();
				String code = CodeExtractor.getCode(node);
				if (code != null && !code.isBlank()) {
					String response = null;

					if (options.getSelectedBackend() == JadxAiViewOptions.SupportedAIBackends.OPENAI_GPT_4_0_MINI) {

						if (options.OPENAI_API_KEY == null || options.OPENAI_API_KEY.isBlank()) {
							JOptionPane.showMessageDialog(null, "ERROR: Invalid API KEY, Please browse to File > Preferences > Plugins > Jadx-AI and enter your OpenAI API KEY");
						} else if (options.OPENAI_PROMPT == null || options.OPENAI_PROMPT.isBlank()) {
							JOptionPane.showMessageDialog(null, "ERROR: OpenAI Prompt is empty, Please browse to File > Preferences > Plugins > Jadx-AI and enter an OpenAI Prompt");
						} else {
							try {
								response = Ai.getOpenAIAnalysis(code, Ai.OpenAiChatModelName.GPT_4_O_MINI, options);
							} catch (Exception e) {
								response = e.getMessage();
							}
						}

					} else if (options.getSelectedBackend() == JadxAiViewOptions.SupportedAIBackends.GPT4FREE_GPT_3_5_TURBO) {

						if (options.GPT4FREE_PROMPT == null || options.GPT4FREE_PROMPT.isBlank()) {
							JOptionPane.showMessageDialog(null, "ERROR: GPT4Free Prompt is empty, Please browse to File > Preferences > Plugins > Jadx-AI and enter a GPT4Free Prompt");
						} else {
							response = Ai.getGPT4FreeAnalysis(code, Ai.OpenAiChatModelName.GPT_3_5_TURBO, options);
						}

					} else if (options.getSelectedBackend() == JadxAiViewOptions.SupportedAIBackends.GEMINI_1_5_FLASH) {

						if (options.GEMINI_API_KEY == null || options.GEMINI_API_KEY.isBlank()) {
							JOptionPane.showMessageDialog(null, "ERROR: Invalid API KEY, Please browse to File > Preferences > Plugins > Jadx-AI and enter your Gemini API KEY");
						} else if (options.GEMINI_PROMPT == null || options.GEMINI_PROMPT.isBlank()) {
							JOptionPane.showMessageDialog(null, "ERROR: Gemini Prompt is empty, Please browse to File > Preferences > Plugins > Jadx-AI and enter a Gemini Prompt");
						} else {
							try {
								response = Ai.getGeminiAnalysis(code, options);
							} catch (Exception e) {
								response = e.getMessage();
							}
						}

					}

					if (response != null && !response.isBlank()) {
						AiViewMain.aiDialog.addTab(node.getFullName(), response);
						AiViewMain.aiDialog.toFront();
					}
				}
				if (taskCount.decrementAndGet() == 0)
					AiViewMain.aiDialog.hideProgressPanel();
			}
		};
		new Thread(run).start();
	}
}
