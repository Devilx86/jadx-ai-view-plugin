package jadx.plugins.aiview;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder;

public class JadxAiViewOptions extends BasePluginOptionsBuilder {
	public String OPENAI_API_KEY = null;
	public String GEMINI_API_KEY = null;
	public String OPENAI_PROMPT = null;
	public String GEMINI_PROMPT = null;
	public String GPT4FREE_PROMPT = null;
	private SupportedAIBackends backendChoice;

	private int editorFontSize = 13;

	private Boolean showOnlyCodeOutput = true;

	public enum SupportedAIBackends {
		OPENAI_GPT_4_0_MINI,
		GEMINI_1_5_FLASH,
		GPT4FREE_GPT_3_5_TURBO,
	}

	;

	private static void cacheFontSize(int size) {
		Path filePath = getCacheDirectory();
		if (filePath != null) {
			saveToFile(filePath.toString() + File.separator + "jadx-ai.txt", String.valueOf(size));
		}
	}

	private static int getFontSizeFromCache() {
		Path filePath = getCacheDirectory();
		if (filePath != null) {
			return readIntFromFile(filePath.toString() + File.separator + "jadx-ai.txt");
		}
		return 0;
	}

	private static void saveToFile(String fileName, String data) {
		try {
			FileWriter myWriter = new FileWriter(fileName);
			myWriter.write(data);
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int readIntFromFile(String fileName) {
		int num = 0;
		try {
			Scanner scanner = new Scanner(new File(fileName));
			while (scanner.hasNextInt()) {
				num = scanner.nextInt();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return num;
	}

	private static Path getCacheDirectory() {
		String os = System.getProperty("os.name").toLowerCase();
		Path cacheDir;

		if (os.contains("win")) {
			cacheDir = Paths.get(System.getProperty("user.home"), "AppData", "Local", "skylot", "jadx");
		} else if (os.contains("mac")) {
			cacheDir = Paths.get(System.getProperty("user.home"), "Library", "Caches", "io.github.skylot.jadx");
		} else {
			cacheDir = Paths.get(System.getProperty("user.home"), ".cache", "jadx");
		}

		try {
			if (!Files.exists(cacheDir)) {
				Files.createDirectories(cacheDir);
			}
		} catch (IOException e) {
			return null;
		}

		return cacheDir;
	}

	@Override
	public void registerOptions() {
		strOption(JadxAiViewPlugin.PLUGIN_ID + ".OpenAI API Key")
				.description("OpenAI API Key")
				.setter(s -> OPENAI_API_KEY = s);

		strOption(JadxAiViewPlugin.PLUGIN_ID + ".Gemini API Key")
				.description("Gemini API Key")
				.setter(s -> GEMINI_API_KEY = s);

		// Default prompt credits: https://github.com/skylot/jadx/issues/1884
		strOption(JadxAiViewPlugin.PLUGIN_ID + ".Edit OpenAI Prompt")
				.description("OpenAI System Prompt")
				.defaultValue("Let the variable names and method names of the following code change as the name implies, the original meaning of the code cannot be changed, the order cannot be changed, and the unprocessed ones remain as they are, the number of lines of the code cannot be optimized, the code cannot be omitted, the code cannot be deleted or added, and the naming conflict cannot be allowed . The original name should be written above them in the form of a comment, keep the comment. Line comments must be added to Each line of code to explain the meaning of the code, and comments between multiple lines of code also need to be marked.")
				.setter(s -> OPENAI_PROMPT = s);

		strOption(JadxAiViewPlugin.PLUGIN_ID + ".Edit Gemini Prompt")
				.description("Gemini System Prompt")
				.defaultValue("Let the variable names and method names of the following code change as the name implies, the original meaning of the code cannot be changed, the order cannot be changed, and the unprocessed ones remain as they are, the number of lines of the code cannot be optimized, the code cannot be omitted, the code cannot be deleted or added, and the naming conflict cannot be allowed . The original name should be written above them in the form of a comment, keep the comment. Line comments must be added to Each line of code to explain the meaning of the code, and comments between multiple lines of code also need to be marked.")
				.setter(s -> GEMINI_PROMPT = s);

		strOption(JadxAiViewPlugin.PLUGIN_ID + ".Edit GPT4free Prompt")
				.description("GPT4free System Prompt")
				.defaultValue("Let the variable names and method names of the following code change as the name implies, the original meaning of the code cannot be changed, the order cannot be changed, and the unprocessed ones remain as they are, the number of lines of the code cannot be optimized, the code cannot be omitted, the code cannot be deleted or added, and the naming conflict cannot be allowed . The original name should be written above them in the form of a comment, keep the comment. Line comments must be added to Each line of code to explain the meaning of the code, and comments between multiple lines of code also need to be marked.")
				.setter(s -> GPT4FREE_PROMPT = s);

		enumOption(JadxAiViewPlugin.PLUGIN_ID + ".Select AI Backend", SupportedAIBackends.values(), SupportedAIBackends::valueOf)
				.description("Select AI Backend")
				.defaultValue(SupportedAIBackends.GPT4FREE_GPT_3_5_TURBO)
				.setter(v -> this.backendChoice = v);

		boolOption(JadxAiViewPlugin.PLUGIN_ID + ".Display only code output")
				.description("Display only code output")
				.defaultValue(true)
				.setter(b -> showOnlyCodeOutput = b);
	}

	public SupportedAIBackends getSelectedBackend() {
		return this.backendChoice;
	}

	public boolean showOnlyCodeOutput() {
		return this.showOnlyCodeOutput;
	}

	public int getEditorFontSize() {
		this.editorFontSize = getFontSizeFromCache();
		if (this.editorFontSize == 0) {
			return 13;
		}
		return this.editorFontSize;
	}

	public void setEditorFontSize(int editorFontSize) {
		this.editorFontSize = editorFontSize;
		cacheFontSize(this.editorFontSize);
	}
}
