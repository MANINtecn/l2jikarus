package net.sf.l2jdev.gameserver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.sf.l2jdev.commons.ui.LineLimitListener;
import net.sf.l2jdev.commons.ui.SplashScreen;
import net.sf.l2jdev.commons.ui.Thema;

public class GameServerLaucher
{
	JTextArea txtrConsole;
	
	JCheckBoxMenuItem chckbxmntmEnabled;
	JCheckBoxMenuItem chckbxmntmDisabled;
	JCheckBoxMenuItem chckbxmntmGmOnly;
	
	static final String[] shutdownOptions =
	{
		"Shutdown",
		"Restart",
		"Cancel"
	};
	static final String[] restartOptions =
	{
		"Restart",
		"Cancel"
	};
	
	public GameServerLaucher()
	{
		Thema.getInstance().aplly();
		
		
		// Initialize console.
		txtrConsole = new JTextArea();
		txtrConsole.setEditable(false);
		txtrConsole.setLineWrap(true);
		txtrConsole.setWrapStyleWord(true);
		txtrConsole.setDropMode(DropMode.INSERT);
		Font defaultFont = findBestConsoleFont();
		currentFontFamily = defaultFont.getFamily();
		currentFontSize = defaultFont.getSize();
		applyConsoleFont();
		
		txtrConsole.getDocument().addDocumentListener(new LineLimitListener(800));
		
		// Initialize menu items.
		final JMenuBar menuBar = new JMenuBar();
		menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		

		
		final JMenu mnFont = new JMenu("Font");
		mnFont.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		menuBar.add(mnFont);
		
	
		
		/*
		 * ========================= FONT FAMILY =========================
		 */
		final JMenu mnFontFamily = new JMenu("Family");
		mnFontFamily.setFont(new Font("Monospaced", Font.PLAIN, 12));
		mnFont.add(mnFontFamily);
		
		String[] fontFamilies =
		{
			"JetBrains Mono",
			"Cascadia Code",
			"Source Code Pro",
			"Consolas",
			"Roboto Mono",
			"IBM Plex Mono",
			"Ubuntu Mono",
			"Monospaced",
			"Segoe UI"
		};
		
		ButtonGroup familyGroup = new ButtonGroup();
		
		for (String fontName : fontFamilies)
		{
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(fontName);
			item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			item.setSelected(fontName.equals(currentFontFamily));
			
			item.addActionListener(_ -> {
				currentFontFamily = fontName;
				applyConsoleFont();
			});
			
			familyGroup.add(item);
			mnFontFamily.add(item);
		}
		
		/*
		 * ========================= FONT SIZE =========================
		 */
		final JMenu mnFontSize = new JMenu("Size");
		mnFontSize.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		mnFont.add(mnFontSize);
		
		int[] sizes =
		{
			11,
			12,
			13,
			14,
			15,
			16,
			18
		};
		
		ButtonGroup sizeGroup = new ButtonGroup();
		
		for (int size : sizes)
		{
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.valueOf(size));
			item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			item.setSelected(size == currentFontSize);
			
			item.addActionListener(_ -> {
				currentFontSize = size;
				applyConsoleFont();
			});
			
			sizeGroup.add(item);
			mnFontSize.add(item);
		}

		
		// Set icons.
		final List<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "16x16.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "32x32.png").getImage());
		
		final JScrollPane scrollPanel = new JScrollPane(txtrConsole);
		scrollPanel.setBounds(0, 0, 800, 550);
		
		// Set frame.
		final JFrame frame = new JFrame("Game");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent ev)
			{
				int choice = JOptionPane.showOptionDialog(frame, "What do you want to do with the server?", "Server Control", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, shutdownOptions, shutdownOptions[2] // default = Cancel
				);
				
				switch (choice)
				{
					case 0: // Shutdown
					{
						Shutdown.getInstance().startShutdown(null, 1, false);
						break;
					}
					
					case 1: // Restart
					{
						Shutdown.getInstance().startShutdown(null, 1, true);
						break;
					}
					
					default: // Cancel or closed dialog
					{
						// Do nothing
						break;
					}
				}
			}
		});
		
		frame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent ev)
			{
				scrollPanel.setSize(frame.getContentPane().getSize());
			}
		});
		
		frame.setJMenuBar(menuBar);
		frame.setIconImages(icons);
		frame.add(scrollPanel, BorderLayout.CENTER);
		frame.getContentPane().setPreferredSize(new Dimension(680, 360));
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		// Redirect output to text area.
		redirectSystemStreams();
		
		// Show splash screen.
		new SplashScreen(".." + File.separator + "images" + File.separator + "splashscreen.gif", frame);
		
	}
	
	// Set where the text is redirected. In this case, txtrConsole.
	void updateTextArea(String text)
	{
		SwingUtilities.invokeLater(() -> {
			txtrConsole.append(text);
			txtrConsole.setCaretPosition(txtrConsole.getText().length());
		});
	}
	
	// Method that manages the redirect.
	private void redirectSystemStreams()
	{
		final OutputStream out = new OutputStream()
		{
			@Override
			public void write(int b)
			{
				updateTextArea(String.valueOf((char) b));
			}
			
			@Override
			public void write(byte[] b, int off, int len)
			{
				updateTextArea(new String(b, off, len));
			}
			
			@Override
			public void write(byte[] b)
			{
				write(b, 0, b.length);
			}
		};
		
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
	
	public static boolean isDigit(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isDigit(c))
			{
				return false;
			}
		}
		return true;
	}
	
	private String currentFontFamily = "Monospaced";
	private int currentFontSize = 12;
	
	private void applyConsoleFont()
	{
		txtrConsole.setFont(new Font(currentFontFamily, Font.PLAIN, currentFontSize));
	}
	
	private static Font findBestConsoleFont()
	{
		String[] preferredFonts =
		{
			"JetBrains Mono",
			"Cascadia Code",
			"Source Code Pro",
			"Consolas",
			"Roboto Mono",
			"IBM Plex Mono",
			"Ubuntu Mono",
			"Monospaced",
			"Segoe UI"
		};
		for (String fontName : preferredFonts)
		{
			Font font = new Font(fontName, Font.PLAIN, 12);
			if (font.getFamily().equals(fontName))
				return font;
		}
		return new Font("Segoe UI", Font.PLAIN, 12);
	}
	
	
}
