package net.sf.l2jdev.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.sf.l2jdev.commons.config.InterfaceConfig;
import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.ui.DarkTheme;
import net.sf.l2jdev.commons.util.HexUtil;
import net.sf.l2jdev.loginserver.GameServerTable;

public class GameServerRegister extends JFrame
{
	private static final long serialVersionUID = 1L;
	private final Scanner _scanner = new Scanner(System.in);
	private JPanel _buttonPanel;
	private JButton _btnRegister;
	private JButton _btnList;
	private JButton _btnRemove;
	private JButton _btnRemoveAll;
	private JTable _serverTable;
	private DefaultTableModel _serverTableModel;

	private GameServerRegister()
	{
		if (InterfaceConfig.ENABLE_GUI)
		{
			System.setProperty("sun.java2d.opengl", "false");
			System.setProperty("sun.java2d.d3d", "false");
			System.setProperty("sun.java2d.noddraw", "true");
			if (InterfaceConfig.DARK_THEME)
			{
				DarkTheme.activate();
			}

			this.gui();
		}
		else
		{
			this.console();
		}
	}

	private void gui()
	{
		this.setTitle("Game Server Register");
		this.setMinimumSize(new Dimension(500, 300));
		this.setDefaultCloseOperation(3);
		this.setLocationRelativeTo(null);
		if (InterfaceConfig.ENABLE_GUI && InterfaceConfig.DARK_THEME)
		{
			DarkTheme.activate();
		}

		this.addWindowListener(new WindowAdapter()
		{
			{
				Objects.requireNonNull(GameServerRegister.this);
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
				DatabaseFactory.close();
				System.exit(0);
			}
		});
		List<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "16x16.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "32x32.png").getImage());

		this.setLayout(new BorderLayout());
		this._buttonPanel = new JPanel(new GridLayout(1, 5, 5, 5));
		this._btnList = new JButton("Servers List");
		this._btnRegister = new JButton("Register Server");
		this._btnRemove = new JButton("Remove Server");
		this._btnRemoveAll = new JButton("Remove All");
		this._buttonPanel.add(this._btnList);
		this._buttonPanel.add(this._btnRegister);
		this._buttonPanel.add(this._btnRemove);
		this._buttonPanel.add(this._btnRemoveAll);
		this._btnList.addActionListener(_ -> this.serversList());
		this._btnRegister.addActionListener(_ -> this.registerServer());
		this._btnRemove.addActionListener(_ -> this.unregisterServer());
		this._btnRemoveAll.addActionListener(_ -> this.unregisterAllServers());
		String[] columnNames = new String[]
		{
			"Server ID",
			"Server Name",
			"Status"
		};
		this._serverTableModel = new DefaultTableModel(columnNames, 0)
		{
			{
				Objects.requireNonNull(GameServerRegister.this);
			}

			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		this._serverTable = new JTable(this._serverTableModel);
		this._serverTable.setSelectionMode(0);
		this._serverTable.getColumnModel().getColumn(2).setCellRenderer(new GameServerRegister.StatusCellRenderer());
		JPanel serverPanel = new JPanel(new BorderLayout());
		TitledBorder titledBorder = BorderFactory.createTitledBorder("Servers");
		serverPanel.setBorder(titledBorder);
		JScrollPane tableScrollPane = new JScrollPane(this._serverTable);
		serverPanel.add(tableScrollPane, "Center");
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(this._buttonPanel, "North");
		mainPanel.add(serverPanel, "Center");
		this._serverTable.addMouseListener(new MouseAdapter()
		{
			{
				Objects.requireNonNull(GameServerRegister.this);
			}

			@Override
			public void mouseClicked(MouseEvent event)
			{
				if (event.getButton() == 3)
				{
					int row = GameServerRegister.this._serverTable.rowAtPoint(event.getPoint());
					GameServerRegister.this._serverTable.setRowSelectionInterval(row, row);
					JPopupMenu popupMenu = new JPopupMenu();
					JMenuItem registerItem = new JMenuItem("Register Server");
					registerItem.addActionListener(_ -> {
						int serverId = (Integer) GameServerRegister.this._serverTable.getValueAt(row, 0);
						String serverName = (String) GameServerRegister.this._serverTable.getValueAt(row, 1);
						if (GameServerTable.getInstance().hasRegisteredGameServerOnId(serverId))
						{
							JOptionPane.showMessageDialog(GameServerRegister.this, "Server " + serverName + " with ID " + serverId + " is already registered.", "Error", 0);
						}
						else
						{
							int confirm = JOptionPane.showConfirmDialog(GameServerRegister.this, "Do you want to register " + serverName + "?", "Confirm Registration", 0);
							if (confirm == 0)
							{
								try
								{
									GameServerRegister.createAndRegister(serverId, ".");
									JOptionPane.showMessageDialog(GameServerRegister.this, "Server " + serverName + " registered successfully!", "Success", 1);
									GameServerRegister.this.serversList();
								}
								catch (IOException var7)
								{
									JOptionPane.showMessageDialog(GameServerRegister.this, "An error occurred while registering " + serverName + ".", "Error", 0);
								}
							}
						}
					});
					JMenuItem unregisterItem = new JMenuItem("Unregister Server");
					unregisterItem.addActionListener(_ -> {
						int serverId = (Integer) GameServerRegister.this._serverTable.getValueAt(row, 0);
						String serverName = (String) GameServerRegister.this._serverTable.getValueAt(row, 1);
						if (!GameServerTable.getInstance().hasRegisteredGameServerOnId(serverId))
						{
							JOptionPane.showMessageDialog(GameServerRegister.this, "Server " + serverName + " with ID " + serverId + " is not registered.", "Error", 0);
						}
						else
						{
							int confirm = JOptionPane.showConfirmDialog(GameServerRegister.this, "Are you sure you want to remove GameServer " + serverId + " - " + serverName + "?", "Confirm Removal", 0);
							if (confirm == 0)
							{
								try
								{
									GameServerRegister.removeServer(serverId);
									JOptionPane.showMessageDialog(GameServerRegister.this, "Game Server ID: " + serverId + " has been successfully removed.", "Success", 1);
									GameServerRegister.this.serversList();
								}
								catch (SQLException var7)
								{
									JOptionPane.showMessageDialog(GameServerRegister.this, "An error occurred while trying to unregister the Game Server.", "Error", 0);
								}
							}
						}
					});
					popupMenu.add(registerItem);
					popupMenu.add(unregisterItem);
					popupMenu.show(event.getComponent(), event.getX(), event.getY());
				}
			}
		});
		this.add(mainPanel, "Center");
		this.setIconImages(icons);
		this.setVisible(true);
	}

	private void serversList()
	{
		SwingUtilities.invokeLater(() -> {
			Map<Integer, String> serverNames = GameServerTable.getInstance().getServerNames();
			if (serverNames.isEmpty())
			{
				JOptionPane.showMessageDialog(null, "No game servers found.", "Information", 1);
			}
			else
			{
				 
				this._serverTableModel.setRowCount(0);

				for (Entry<Integer, String> entry : serverNames.entrySet())
				{
					int id = entry.getKey();
					String serverName = entry.getValue();
					boolean inUse = GameServerTable.getInstance().hasRegisteredGameServerOnId(id);
					String status = inUse ? "In Use" : "Free";
					this._serverTableModel.addRow(new Object[]
					{
						id,
						serverName,
						status
					});
				}
			}
		});
	}

	private void registerServer()
	{
		JTextField idField = new JTextField(10);
		Object[] message = new Object[]
		{
			"Enter Game Server ID:",
			idField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Register Game Server", 2);
		if (option == 0)
		{
			String input = idField.getText().trim();
			if (input.isEmpty())
			{
				JOptionPane.showMessageDialog(null, "Game Server ID cannot be empty.", "Error", 0);
				return;
			}

			try
			{
				int id = Integer.parseInt(input);
				if (id <= 0)
				{
					JOptionPane.showMessageDialog(null, "Game Server ID must be a positive number.", "Error", 0);
					return;
				}

				String serverName = GameServerTable.getInstance().getServerNameById(id);
				if (serverName == null)
				{
					JOptionPane.showMessageDialog(null, "No server found for ID: " + id, "Error", 0);
				}
				else if (GameServerTable.getInstance().hasRegisteredGameServerOnId(id))
				{
					JOptionPane.showMessageDialog(null, "Server '" + serverName + "' with ID " + id + " is already registered.", "Error", 0);
				}
				else
				{
					createAndRegister(id, ".");
					JOptionPane.showMessageDialog(null, "Game server with ID: " + id + " (" + serverName + ") has been successfully registered.", "Success", 1);
					idField.setText("");
					this.serversList();
				}
			}
			catch (NumberFormatException var7)
			{
				JOptionPane.showMessageDialog(null, "Invalid Game Server ID entered. Please enter a valid number.", "Error", 0);
			}
			catch (IOException var8)
			{
				JOptionPane.showMessageDialog(null, "An error occurred while trying to register the Game Server.", "Error", 0);
			}
		}
	}

	private void unregisterServer()
	{
		JTextField idField = new JTextField(10);
		Object[] message = new Object[]
		{
			"Enter Game Server ID to unregister:",
			idField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Unregister Game Server", 2);
		if (option == 0)
		{
			String input = idField.getText().trim();
			if (input.isEmpty())
			{
				JOptionPane.showMessageDialog(null, "Game Server ID cannot be empty.", "Error", 0);
				return;
			}

			try
			{
				int id = Integer.parseInt(input);
				String serverName = GameServerTable.getInstance().getServerNameById(id);
				if (serverName == null)
				{
					JOptionPane.showMessageDialog(null, "No Game Server found for ID: " + id, "Error", 0);
				}
				else if (GameServerTable.getInstance().hasRegisteredGameServerOnId(id))
				{
					int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove GameServer " + id + "  (" + serverName + ")?", "Confirm Removal", 0);
					if (confirm == 0)
					{
						removeServer(id);
						JOptionPane.showMessageDialog(null, "Game Server ID: " + id + " (" + serverName + ") has been successfully removed.", "Success", 1);
						this.serversList();
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null, "No GameServer is registered with ID: " + id, "Error", 0);
				}
			}
			catch (NumberFormatException var8)
			{
				JOptionPane.showMessageDialog(null, "Invalid Game Server ID entered. Please enter a valid number.", "Error", 0);
			}
			catch (SQLException var9)
			{
				JOptionPane.showMessageDialog(null, "An error occurred while trying to unregister the Game Server.", "Error", 0);
			}
		}
	}

	private void unregisterAllServers()
	{
		if (GameServerTable.getInstance().getRegisteredGameServers().isEmpty())
		{
			JOptionPane.showMessageDialog(null, "No game servers are currently registered.", "Info", 1);
		}
		else
		{
			int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to unregister all game servers?", "Confirm Unregister All Game Servers", 0, 2);
			if (confirm == 0)
			{
				try
				{
					removeAllServers();
					JOptionPane.showMessageDialog(null, "All game servers have been unregistered successfully.", "Success", 1);
					this.serversList();
				}
				catch (SQLException var3)
				{
					JOptionPane.showMessageDialog(null, "Error while unregistering game servers: " + var3.getMessage(), "Error", 0);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Unregister operation canceled by the user.", "Canceled", 1);
			}
		}
	}

	private static void createAndRegister(int id, String outDir) throws IOException
	{
		byte[] hexId = HexUtil.generateHexBytes(16);
		GameServerTable.getInstance().registerServerOnDB(hexId, id, "");
		Properties hexSetting = new Properties();
		File file = new File(outDir, "hexid.txt");
		file.createNewFile();

		try (OutputStream out = new FileOutputStream(file))
		{
			hexSetting.setProperty("ServerID", String.valueOf(id));
			hexSetting.setProperty("HexID", new BigInteger(hexId).toString(16));
			hexSetting.store(out, "The HexId to Auth into LoginServer");
		}
	}

	private static void removeServer(int id) throws SQLException
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM gameservers WHERE server_id = ?");)
		{
			statement.setInt(1, id);
			statement.executeUpdate();
		}

		GameServerTable.getInstance().getRegisteredGameServers().remove(id);
	}

	private static void removeAllServers() throws SQLException
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement statement = con.createStatement();)
		{
			statement.executeUpdate("DELETE FROM gameservers");
			GameServerTable.getInstance().getRegisteredGameServers().clear();
		}
	}

	private void console()
	{
		this.showMenu();

		try
		{
			Scanner scanner = new Scanner(System.in);

			try
			{
				while (true)
				{
					System.out.print(System.lineSeparator() + "Enter your choice: ");
					if (!scanner.hasNextInt())
					{
						System.out.println(System.lineSeparator() + "[ERROR] Invalid input. Please enter a number.");
						scanner.nextLine();
					}
					else
					{
						int choice = scanner.nextInt();
						scanner.nextLine();
						switch (choice)
						{
							case 1:
								System.out.println(System.lineSeparator() + "[INFO] You selected: List Servers");
								this.listServersConsole();
								break;
							case 2:
								System.out.println(System.lineSeparator() + "[INFO] You selected: Register Server");
								this.registerServerConsole(scanner);
								break;
							case 3:
								System.out.println(System.lineSeparator() + "[INFO] You selected: Remove Server");
								this.removeServerConsole(scanner);
								break;
							case 4:
								System.out.println(System.lineSeparator() + "[INFO] You selected: Remove All Servers");
								this.removeAllServersConsole();
								break;
							case 5:
								System.out.println(System.lineSeparator() + "[EXIT] Exiting the application. Goodbye!");

								try
								{
									Thread.sleep(2000L);
								}
								catch (InterruptedException var10)
								{
								}

								System.exit(0);
								break;
							default:
								System.out.println(System.lineSeparator() + "[WARNING] Invalid choice. Please select a valid option.");
						}

						this.showMenu();
					}
				}
			}
			catch (Throwable var11)
			{
				try
				{
					scanner.close();
				}
				catch (Throwable var9)
				{
					var11.addSuppressed(var9);
				}

				throw var11;
			}
		}
		finally
		{
			System.out.println("[INFO] Cleaning up resources...");
			DatabaseFactory.close();
		}
	}

	protected void showMenu()
	{
		System.out.println(System.lineSeparator() + "=========================================================");
		System.out.println("                  AVAILABLE COMMANDS                     ");
		System.out.println("---------------------------------------------------------");
		System.out.println("  [1] List Servers");
		System.out.println("  [2] Register Server");
		System.out.println("  [3] Remove Server");
		System.out.println("  [4] Remove All Servers");
		System.out.println("  [5] Exit");
		System.out.println("---------------------------------------------------------");
	}

	protected void listServersConsole()
	{
		Map<Integer, String> serverNames = GameServerTable.getInstance().getServerNames();
		if (serverNames.isEmpty())
		{
			System.out.println("No game servers found.");
		}
		else
		{
			System.out.println(System.lineSeparator() + "List Servers:");
			System.out.println("+----------------+------------------------+-----------+");
			System.out.printf("| %-14s | %-22s | %-9s |" + System.lineSeparator(), "Server ID", "Server Name", "Status");
			System.out.println("+----------------+------------------------+-----------+");

			for (Entry<Integer, String> entry : serverNames.entrySet())
			{
				int id = entry.getKey();
				String serverName = entry.getValue();
				boolean inUse = GameServerTable.getInstance().hasRegisteredGameServerOnId(id);
				String status = inUse ? "In Use" : "Free";
				System.out.printf("| %-14d | %-22s | %-9s |" + System.lineSeparator(), id, serverName, status);
			}

			System.out.println("+----------------+------------------------+-----------+");
		}
	}

	private void registerServerConsole(Scanner scanner)
	{
		System.out.print("Enter Game Server ID to register: ");
		String input = scanner.nextLine().trim();
		if (input.isEmpty())
		{
			System.out.println("Game Server ID cannot be empty.");
		}
		else
		{
			try
			{
				int id = Integer.parseInt(input);
				if (id <= 0)
				{
					System.out.println("Game Server ID must be a positive number.");
					return;
				}

				String serverName = GameServerTable.getInstance().getServerNameById(id);
				if (serverName == null)
				{
					System.out.println("No server found for ID: " + id);
					return;
				}

				if (GameServerTable.getInstance().hasRegisteredGameServerOnId(id))
				{
					System.out.println("Server '" + serverName + "' with ID " + id + " is already registered.");
					return;
				}

				createAndRegister(id, ".");
				System.out.println("Game server with ID: " + id + " (" + serverName + ") has been successfully registered.");
				this.serversList();
			}
			catch (NumberFormatException var5)
			{
				System.out.println("Invalid Game Server ID entered. Please enter a valid number.");
			}
			catch (IOException var6)
			{
				System.out.println("An error occurred while trying to register the Game Server: " + var6.getMessage());
			}
		}
	}

	protected void removeServerConsole(Scanner scanner)
	{
		System.out.print("Enter Server ID to remove: ");
		String input = scanner.nextLine().trim();
		if (input.isEmpty())
		{
			System.out.println("Server ID cannot be empty.");
		}
		else
		{
			try
			{
				int serverId = Integer.parseInt(input);
				if (!GameServerTable.getInstance().hasRegisteredGameServerOnId(serverId))
				{
					System.out.println("No Game Server is registered with ID " + serverId + ".");
					return;
				}

				String serverName = GameServerTable.getInstance().getServerNameById(serverId);
				System.out.print("Are you sure you want to remove Game Server " + serverId + " (" + serverName + ")? (y/n): ");
				String response = scanner.nextLine().trim().toLowerCase();
				if (!response.equals("y"))
				{
					System.out.println("Operation canceled.");
					return;
				}

				removeServer(serverId);
				System.out.println("Game Server ID " + serverId + " (" + serverName + ") has been successfully removed.");
			}
			catch (NumberFormatException var6)
			{
				System.out.println("Invalid Server ID. Please enter a valid number.");
			}
			catch (SQLException var7)
			{
				System.out.println("Error removing server from the database: " + var7.getMessage());
			}
			catch (Exception var8)
			{
				System.out.println("An unexpected error occurred: " + var8.getMessage());
			}
		}
	}

	private void removeAllServersConsole()
	{
		System.out.print("Are you sure you want to remove all servers? (y/n): ");
		String response = this._scanner.nextLine().trim().toLowerCase();
		if (!response.equals("y"))
		{
			System.out.println("Operation canceled.");
		}
		else
		{
			try
			{
				removeAllServers();
				System.out.println("All servers have been successfully removed.");
			}
			catch (SQLException var3)
			{
				System.out.println("Error removing all servers: " + var3.getMessage());
			}
			catch (Exception var4)
			{
				System.out.println("An unexpected error occurred: " + var4.getMessage());
			}
		}
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			InterfaceConfig.load();
			DatabaseFactory.init();
			GameServerTable.getInstance();
			new GameServerRegister();
		});
	}

	private class StatusCellRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private StatusCellRenderer()
		{
			Objects.requireNonNull(GameServerRegister.this);
			super();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cellComponent.setForeground(Color.WHITE);
			if (value != null && value.equals("Free"))
			{
				cellComponent.setForeground(Color.GREEN);
			}
			else if (value != null && value.equals("In Use"))
			{
				cellComponent.setForeground(Color.RED);
			}

			if (isSelected)
			{
				cellComponent.setBackground(table.getSelectionBackground());
				cellComponent.setForeground(table.getSelectionForeground());
			}
			else
			{
				cellComponent.setBackground(table.getBackground());
			}

			return cellComponent;
		}
	}
}
