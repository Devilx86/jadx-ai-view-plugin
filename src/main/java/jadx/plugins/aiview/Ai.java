package jadx.plugins.aiview;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;

public class Ai {

	public enum OpenAiChatModelName {
		GPT_3_5_TURBO("gpt-3.5-turbo"),
		GPT_3_5_TURBO_1106("gpt-3.5-turbo-1106"),
		GPT_3_5_TURBO_0125("gpt-3.5-turbo-0125"),
		GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k"),
		GPT_4("gpt-4"),
		GPT_4_0613("gpt-4-0613"),
		GPT_4_TURBO_PREVIEW("gpt-4-turbo-preview"),
		GPT_4_1106_PREVIEW("gpt-4-1106-preview"),
		GPT_4_0125_PREVIEW("gpt-4-0125-preview"),
		GPT_4_32K("gpt-4-32k"),
		GPT_4_32K_0314("gpt-4-32k-0314"),
		GPT_4_32K_0613("gpt-4-32k-0613"),
		GPT_4_O("gpt-4o"),
		GPT_4_O_MINI("gpt-4o-mini");

		private final String stringValue;

		OpenAiChatModelName(String stringValue) {
			this.stringValue = stringValue;
		}

		@Override
		public String toString() {
			return stringValue;
		}
	}

	public static String getOpenAIAnalysis(String code, OpenAiChatModelName model, JadxAiViewOptions options) {
		if (options.OPENAI_PROMPT == null || options.OPENAI_PROMPT.isBlank()) {
			return "";
		}

		ChatLanguageModel aiChatModel = OpenAiChatModel.builder()
				.apiKey(options.OPENAI_API_KEY)
				.modelName(model.toString())
				.build();

		ChatMessage systemMessage = new SystemMessage(options.OPENAI_PROMPT);
		List<ChatMessage> messages = new ArrayList<>();
		messages.add(systemMessage);
		messages.add(new UserMessage(code));
		Response<AiMessage> messageResponse = aiChatModel.generate(messages);

		String response = messageResponse.toString();
		if (options.showOnlyCodeOutput() && response.contains("```java")) {
			return response.split("```java")[1].split("```")[0].trim();
		}

		return response;
	}

	public static String getGeminiAnalysis(String code, JadxAiViewOptions options) {
		if (options.GEMINI_PROMPT == null || options.GEMINI_PROMPT.isBlank()) {
			return "";
		}

		ChatLanguageModel gemini = GoogleAiGeminiChatModel.builder()
				.apiKey(options.GEMINI_API_KEY)
				.modelName("gemini-1.5-flash")
				.temperature(0.1)
				.build();

		ChatMessage systemMessage = new SystemMessage(options.OPENAI_PROMPT);
		List<ChatMessage> messages = new ArrayList<>();
		messages.add(systemMessage);
		messages.add(new UserMessage(code));
		Response<AiMessage> messageResponse = gemini.generate(messages);

		String response = messageResponse.toString();
		if (options.showOnlyCodeOutput() && response.contains("```java")) {
			return response.split("```java")[1].split("```")[0].trim();
		}
		return response;
	}

	public static String getGPT4FreeAnalysis(String code, OpenAiChatModelName model, JadxAiViewOptions options) {
		if (options.GPT4FREE_PROMPT == null || options.GPT4FREE_PROMPT.isBlank()) {
			return "";
		}

		String prompt = options.GPT4FREE_PROMPT;
		prompt += code;
		if (options.showOnlyCodeOutput()) {
			prompt += "Only share the updated/commented code";
		}

		try {
			File tempFile = File.createTempFile("jadx_ai", ".py", new File(System.getProperty("java.io.tmpdir")));
			PrintWriter writer = new PrintWriter(tempFile, StandardCharsets.UTF_8);
			writer.println("import sys");
			writer.println("from g4f.client import Client");
			writer.println("prompt = '''" + prompt + "'''");
			writer.println("client = Client()");
			writer.println("response = client.chat.completions.create(model='" + model.toString() + "', messages=[{\"role\": \"user\", \"content\": prompt}],).choices[0].message.content");
			writer.println("print(response)");
			writer.close();

			if (tempFile.exists()) {
				Process process = new ProcessBuilder("python3", tempFile.getAbsolutePath()).start();
				BufferedReader inputReader =
						new BufferedReader(new InputStreamReader(process.getInputStream()));

				BufferedReader errorReader =
						new BufferedReader(new InputStreamReader(process.getErrorStream()));

				StringBuilder builder = new StringBuilder();
				String line = null;
				while ((line = inputReader.readLine()) != null) {
					builder.append(line);
					builder.append(System.lineSeparator());
				}

				String response = builder.toString();
				if (response.isBlank()) {
					while ((line = errorReader.readLine()) != null) {
						builder.append(line);
						builder.append(System.lineSeparator());
					}
				}
				response = builder.toString();

				tempFile.delete();


				if (options.showOnlyCodeOutput() && response.contains("```java")) {
					return response.split("```java")[1].split("```")[0].trim();
				}

				return response;
			}
			return "ERROR: GPT4Free Failed, temporary file could not be found!";
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
