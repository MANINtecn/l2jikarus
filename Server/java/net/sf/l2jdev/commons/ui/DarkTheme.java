package net.sf.l2jdev.commons.ui;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class DarkTheme
{
	public static void activate()
	{
		try
		{
			/*
			 * ============================= Paleta de cores =============================
			 */
			Color graphiteDark = new Color(18, 20, 23); // #121417
			Color graphiteMid = new Color(28, 30, 33); // #1C1E21
			Color graphiteLight = new Color(38, 41, 45); // #26292D
			
			Color whiteText = new Color(235, 235, 235); // Branco suave
			Color disabledText = new Color(120, 123, 128); // Desabilitado
			
			Color blueLogger = new Color(70, 150, 255); // Azul vibrante (LOG)
			Color whiteSelectBg = new Color(70, 110, 170);
			Color darkSelectText = new Color(25, 25, 25);
			
			Color focusBlue = new Color(90, 170, 255);
			
			/*
			 * ============================= Base Nimbus (Fundo) =============================
			 */
			UIManager.put("control", graphiteMid);
			UIManager.put("info", graphiteLight);
			UIManager.put("nimbusBase", graphiteDark);
			UIManager.put("nimbusBlueGrey", new Color(52, 55, 59));
			UIManager.put("nimbusLightBackground", graphiteMid);
			
			/*
			 * ============================= Texto GLOBAL (Branco) =============================
			 */
			UIManager.put("text", whiteText);
			UIManager.put("controlText", whiteText);
			UIManager.put("infoText", whiteText);
			UIManager.put("textForeground", whiteText);
			
			UIManager.put("Label.foreground", whiteText);
			UIManager.put("Button.foreground", whiteText);
			UIManager.put("ToggleButton.foreground", whiteText);
			UIManager.put("RadioButton.foreground", whiteText);
			UIManager.put("CheckBox.foreground", whiteText);
			UIManager.put("Menu.foreground", whiteText);
			UIManager.put("MenuItem.foreground", whiteText);
			UIManager.put("TabbedPane.foreground", whiteText);
			
			UIManager.put("Table.foreground", whiteText);
			UIManager.put("TableHeader.foreground", whiteText);
			UIManager.put("List.foreground", whiteText);
			UIManager.put("Tree.foreground", whiteText);
			
			UIManager.put("TextField.foreground", whiteText);
			UIManager.put("PasswordField.foreground", whiteText);
			
			/*
			 * ============================= LOGGER / CONSOLE (Azul) =============================
			 */
			UIManager.put("TextArea.foreground", blueLogger);
			UIManager.put("TextPane.foreground", blueLogger);
			UIManager.put("EditorPane.foreground", blueLogger);
			
			/*
			 * ============================= Seleção / Foco =============================
			 */
			UIManager.put("nimbusSelectionBackground", whiteSelectBg);
			UIManager.put("nimbusSelectedText", darkSelectText);
			UIManager.put("nimbusFocus", focusBlue);
			
			/*
			 * ============================= Estados =============================
			 */
			UIManager.put("nimbusDisabledText", disabledText);
			
			/*
			 * ============================= Aplicar LookAndFeel =============================
			 */
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
