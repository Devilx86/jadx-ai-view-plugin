package jadx.plugins.aiview;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AiViewFrame extends JFrame {
	private HashMap<String, String> tabMap;

	private final JadxAiViewOptions options;
	private JTabbedPane tabbedPane = null;
	private boolean isWindowClosed = false;
	private volatile JPanel progressPanel;

	public boolean isWindowClosed() {
		return isWindowClosed;
	}

	public void showProgressPanel() {
		this.progressPanel.setVisible(true);
	}

	public void hideProgressPanel() {
		this.progressPanel.setVisible(false);
	}

	public AiViewFrame(JadxAiViewOptions options) {
		this.options = options;
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isWindowClosed = true;
			}
		});
		this.setLayout(new BorderLayout());
		buildMenuBar();

		tabMap = new HashMap<String, String>();

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout());
		progressPanel.add(new JLabel("Loading..."), BorderLayout.LINE_START);
		progressPanel.add(progressBar, BorderLayout.LINE_END);
		progressPanel.setVisible(false);

		this.tabbedPane = new JTabbedPane();
		this.add(tabbedPane, BorderLayout.CENTER);

		this.add(progressPanel, BorderLayout.PAGE_END);
		this.setTitle(JadxAiViewPlugin.PLUGIN_ID);
		this.setSize(800, 700);
	}

	public static String showTextAreaDialog(String text, String title) {
		if (title == null || title.isBlank()) {
			title = JadxAiViewPlugin.PLUGIN_ID;
		}
		JTextArea textArea = new JTextArea(text);
		textArea.setColumns(30);
		textArea.setRows(10);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
		int ret = JOptionPane.showConfirmDialog(null, new JScrollPane(textArea), title, JOptionPane.OK_OPTION);
		if (ret == 0) {
			return textArea.getText();
		}
		return null;
	}

	private void saveTabMap(String fileName) {
		File file = new File(fileName);
		try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream s = new ObjectOutputStream(f);
			s.writeObject(tabMap);
			s.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadTabMap(String fileName) {
		File file = new File(fileName);
		try {
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream s = new ObjectInputStream(f);
			HashMap<String, String> tabMapLoaded = (HashMap<String, String>) s.readObject();
			s.close();

			for (Map.Entry<String, String> entry : tabMapLoaded.entrySet()) {
				tabMap.put(entry.getKey(), entry.getValue());
				addTab(entry.getKey(), entry.getValue());
			}
		} catch (IOException | ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "ERROR: Failed to load tabs from the file");
		}
	}

	public void setEditorFont(int fontSize) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			Component component = tabbedPane.getComponentAt(i);
			if (component instanceof JPanel) {
				JPanel panel = (JPanel) component;
				for (Component comp : panel.getComponents()) {
					if (comp instanceof RTextScrollPane) {
						RTextScrollPane scrollPane = ((RTextScrollPane) comp);
						scrollPane.getGutter().setLineNumberFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
						if (scrollPane.getViewport().getView() instanceof RSyntaxTextArea) {
							RSyntaxTextArea textArea = (RSyntaxTextArea) scrollPane.getViewport().getView();
							textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
						}
					}

				}
			}
		}
	}

	public int getFontSizePopup() {
		int moveCount = 0;

		JSlider slider = new JSlider(10, 31);
		slider.setMajorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setValue(options.getEditorFontSize());

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(2, 1));
		sliderPanel.setPreferredSize(new Dimension(500, 115));
		sliderPanel.add(slider);

		String title = "Set Font Size";
		int dialogResponse = JOptionPane.showOptionDialog
				(this,
						sliderPanel,
						title,
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null, null, null
				);
		if (JOptionPane.OK_OPTION == dialogResponse) {
			moveCount = slider.getValue();
		} else {
			moveCount = 0;
		}

		return moveCount;
	}

	public void buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		JMenuItem saveItem = new JMenuItem("Save Tabs");
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.jadxaiview", "jadxaiview"));
				fileChooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return null;
					}

					@Override
					public boolean accept(File file) {
						return false;
					}
				});
				int returnVal = fileChooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String path = file.getPath();
					if (!path.endsWith(".jadxaiview")) {
						path = path + ".jadxaiview";
					}
					saveTabMap(path);
					repaint();

				}
			}
		});

		JMenuItem loadItem = new JMenuItem("Load Tabs");
		loadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.jadxaiview", "jadxaiview"));
				fileChooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return null;
					}

					@Override
					public boolean accept(File file) {
						return false;
					}
				});
				int returnVal = fileChooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String path = file.getPath();
					if (path.endsWith(".jadxaiview") && file.exists()) {
						loadTabMap(path);
					}
					repaint();

				}
			}
		});
		fileMenu.add(saveItem);
		fileMenu.add(loadItem);
		menuBar.add(fileMenu);


		JMenu settingsMenu = new JMenu("Settings");
		JMenuItem editPromptItem = new JMenuItem("Edit Prompt");
		editPromptItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if (options.getSelectedBackend() == JadxAiViewOptions.SupportedAIBackends.GEMINI_1_5_FLASH) {
					String updatedPrompt = showTextAreaDialog(options.GEMINI_PROMPT, JadxAiViewPlugin.PLUGIN_ID + " ~ Edit " + options.getSelectedBackend() + " Prompt");
					if (updatedPrompt != null) {
						options.GEMINI_PROMPT = updatedPrompt;
					}
				} else if (options.getSelectedBackend() == JadxAiViewOptions.SupportedAIBackends.OPENAI_GPT_4_0_MINI) {
					String updatedPrompt = showTextAreaDialog(options.OPENAI_PROMPT, JadxAiViewPlugin.PLUGIN_ID + " ~ Edit " + options.getSelectedBackend() + " Prompt");
					if (updatedPrompt != null) {
						options.OPENAI_PROMPT = updatedPrompt;
					}
				} else if (options.getSelectedBackend() == JadxAiViewOptions.SupportedAIBackends.GPT4FREE_GPT_3_5_TURBO) {
					String updatedPrompt = showTextAreaDialog(options.GPT4FREE_PROMPT, JadxAiViewPlugin.PLUGIN_ID + " ~ Edit " + options.getSelectedBackend() + " Prompt");
					if (updatedPrompt != null) {
						options.GPT4FREE_PROMPT = updatedPrompt;
					}
				}
			}
		});

		JMenuItem refreshUiItem = new JMenuItem("Refresh UI");
		refreshUiItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				setEditorFont(options.getEditorFontSize());
			}
		});

		JMenuItem fontSizeItem = new JMenuItem("Font Size");
		fontSizeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				int fontSize = getFontSizePopup();
				if (fontSize > 0) {
					options.setEditorFontSize(fontSize);
					setEditorFont(options.getEditorFontSize());
				}
			}
		});

		settingsMenu.add(editPromptItem);
		settingsMenu.add(refreshUiItem);
		settingsMenu.add(fontSizeItem);
		menuBar.add(settingsMenu);

		setJMenuBar(menuBar);
	}

	private JPanel createTabHead(String title) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setOpaque(false);
		JButton closeBtn = new JButton("x");
		JLabel titleLabel = new JLabel(title + "    ");
		closeBtn.setBorderPainted(false);
		closeBtn.setOpaque(false);

		closeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i;
				for (i = 0; i <= tabbedPane.getTabCount() - 1; i++) {
					if (title.equals(tabbedPane.getTitleAt(i)))
						break;
				}
				tabMap.remove(tabbedPane.getTitleAt(i));
				tabbedPane.removeTabAt(i);
			}
		});

		panel.add(titleLabel);
		panel.add(closeBtn);
		return panel;
	}

	/* To check tab name not same to not break createTabHead() */
	private boolean isTabNameExists(String title) {
		for (int i = 0; i <= tabbedPane.getTabCount() - 1; i++) {
			if (title.equals(tabbedPane.getTitleAt(i)))
				return true;
		}
		return false;
	}

	public void addTab(String title, String body) {
		int count = 0;
		String tmp = title;
		while (isTabNameExists(tmp)) {
			count++;
			tmp = title + count;
		}
		title = tmp;

		JPanel cp = new JPanel(new BorderLayout());

		RSyntaxTextArea textArea = new RSyntaxTextArea(20, 40);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);
		textArea.setText(body);
		textArea.setEditable(false);

		RTextScrollPane scrollPane = new RTextScrollPane(textArea);
		textArea.setCaretPosition(0);
		cp.add(scrollPane);
		this.tabbedPane.addTab(title, cp);
		tabMap.put(title, body);

		int index = tabbedPane.indexOfTab(title);
		tabbedPane.setTabComponentAt(index, createTabHead(title));
		tabbedPane.setSelectedIndex(index);
		setEditorFont(options.getEditorFontSize());
	}
}
