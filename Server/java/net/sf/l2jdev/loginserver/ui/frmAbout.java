package net.sf.l2jdev.loginserver.ui;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Window.Type;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class frmAbout
{
	private final URI _uri;
	private JFrame _frmAbout;
	
	public frmAbout()
	{
		this.initialize();
		this._uri = createURI("www.l2jBAN-JDEV.org");
		this._frmAbout.setVisible(true);
	}
	
	private void initialize()
	{
		this._frmAbout = new JFrame();
		this._frmAbout.setResizable(false);
		this._frmAbout.setTitle("About");
		this._frmAbout.setBounds(100, 100, 297, 197);
		this._frmAbout.setDefaultCloseOperation(2);
		this._frmAbout.setType(Type.UTILITY);
		this._frmAbout.getContentPane().setLayout(null);
		JLabel lblLDev = new JLabel("L2JDev");
		lblLDev.setFont(new Font("Tahoma", 0, 32));
		lblLDev.setHorizontalAlignment(0);
		lblLDev.setBounds(10, 11, 271, 39);
		this._frmAbout.getContentPane().add(lblLDev);
		JLabel lblData = new JLabel("2026-" + Calendar.getInstance().get(1));
		lblData.setHorizontalAlignment(0);
		lblData.setBounds(10, 44, 271, 14);
		this._frmAbout.getContentPane().add(lblData);
		JLabel lblLoginServer = new JLabel("Login Server");
		lblLoginServer.setHorizontalAlignment(0);
		lblLoginServer.setFont(new Font("Tahoma", 0, 14));
		lblLoginServer.setBounds(10, 86, 271, 23);
		this._frmAbout.getContentPane().add(lblLoginServer);
		JLabel site = new JLabel("www.l2jBAN-JDEV.org");
		site.setText("<html><font color=\"#000099\"><u>www.l2jbrasil.com</u></font></html>");
		site.setHorizontalAlignment(0);
		site.setBounds(76, 128, 140, 14);
		site.addMouseListener(new MouseAdapter()
		{
			{
				Objects.requireNonNull(frmAbout.this);
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				if (Desktop.isDesktopSupported())
				{
					try
					{
						Desktop.getDesktop().browse(frmAbout.this._uri);
					}
					catch (IOException var3)
					{
					}
				}
			}
		});
		this._frmAbout.getContentPane().add(site);
		this._frmAbout.setLocationRelativeTo(null);
	}
	
	private static URI createURI(String str)
	{
		try
		{
			return new URI(str);
		}
		catch (URISyntaxException var2)
		{
			throw new IllegalArgumentException(var2.getMessage(), var2);
		}
	}
}
