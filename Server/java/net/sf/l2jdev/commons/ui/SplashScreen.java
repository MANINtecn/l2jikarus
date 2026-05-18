package net.sf.l2jdev.commons.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;

public class SplashScreen extends JWindow
{
	private static final long serialVersionUID = 3135071544352172471L;
	
	public SplashScreen(String imagePath, JFrame frame)
	{
		JLabel splashLabel = new JLabel(new ImageIcon(imagePath));
		getContentPane().add(splashLabel, BorderLayout.CENTER);
		pack();
		
		// Centralizar a janela na tela
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = getSize();
		setLocation((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) / 2);
		
		setAlwaysOnTop(true);
		setVisible(true);
		
		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				setVisible(false);
				if (frame != null)
				{
					frame.setVisible(true);
					frame.toFront();
					frame.setState(Frame.ICONIFIED);
					frame.setState(Frame.NORMAL);
				}
				dispose();
			}
		}, 1500);
	}
}
