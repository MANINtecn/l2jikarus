package net.sf.l2jdev.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.ui.DarkTheme;
import net.sf.l2jdev.commons.ui.SplashScreen;
import net.sf.l2jdev.commons.util.ConfigReader;

public class DatabaseInstaller extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static final String INTERFACE_CONFIG_FILE = "./config/Interface.ini";
	private JTextField _hostField;
	private JTextField _portField;
	private JTextField _userField;
	private JTextField _dbNameField;
	private JPasswordField _passField;
	private JTextPane _outputArea;
	private JButton _installButton;
	private JButton _testConnectionButton;
	private JCheckBox _loginDbCheckBox;
	private JCheckBox _gameDbCheckBox;
	private JProgressBar _progressBar;

	private DatabaseInstaller()
	{
		ConfigReader interfaceConfig = new ConfigReader("./config/Interface.ini");
		if (interfaceConfig.getBoolean("EnableGUI", false) && !GraphicsEnvironment.isHeadless())
		{
			System.setProperty("sun.java2d.opengl", "false");
			System.setProperty("sun.java2d.d3d", "false");
			System.setProperty("sun.java2d.noddraw", "true");
			if (interfaceConfig.getBoolean("DarkTheme", true))
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
		new SplashScreen(".." + File.separator + "images" + File.separator + "splashscreen.gif", this);
		this.setTitle("BAN-JDEV - Database Installer");
		this.setMinimumSize(new Dimension(620, 400));
		this.setDefaultCloseOperation(3);
		this.setLocationRelativeTo(null);
		ConfigReader interfaceConfig = new ConfigReader("./config/Interface.ini");
		if (interfaceConfig.getBoolean("EnableGUI", true) && !GraphicsEnvironment.isHeadless() && interfaceConfig.getBoolean("DarkTheme", true))
		{
			DarkTheme.activate();
		}

		List<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "16x16.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "32x32.png").getImage());

		this.setIconImages(icons);
		JSplitPane splitPane = new JSplitPane(1);
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, 1));
		splitPane.setLeftComponent(leftPanel);
		leftPanel.add(new JLabel("Host:"));
		leftPanel.add(Box.createVerticalStrut(5));
		this._hostField = new JTextField("localhost", 20);
		this._hostField.setPreferredSize(new Dimension(200, 20));
		this._hostField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		leftPanel.add(this._hostField);
		leftPanel.add(new JLabel("Port:"));
		leftPanel.add(Box.createVerticalStrut(5));
		this._portField = new JTextField("3306", 20);
		this._portField.setPreferredSize(new Dimension(200, 20));
		this._portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		leftPanel.add(this._portField);
		leftPanel.add(new JLabel("Username:"));
		leftPanel.add(Box.createVerticalStrut(5));
		this._userField = new JTextField("root", 20);
		this._userField.setPreferredSize(new Dimension(200, 20));
		this._userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		leftPanel.add(this._userField);
		leftPanel.add(new JLabel("Password:"));
		leftPanel.add(Box.createVerticalStrut(5));
		this._passField = new JPasswordField(20);
		this._passField.setPreferredSize(new Dimension(200, 20));
		this._passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		leftPanel.add(this._passField);
		leftPanel.add(new JLabel("Database:"));
		leftPanel.add(Box.createVerticalStrut(5));
		this._dbNameField = new JTextField("l2jBAN-JDEVessence", 20);
		this._dbNameField.setPreferredSize(new Dimension(200, 20));
		this._dbNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		leftPanel.add(this._dbNameField);
		this._loginDbCheckBox = new JCheckBox("Install Login");
		leftPanel.add(Box.createVerticalStrut(5));
		this._loginDbCheckBox.setSelected(true);
		this._loginDbCheckBox.setPreferredSize(new Dimension(200, 20));
		this._loginDbCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		leftPanel.add(this._loginDbCheckBox);
		this._gameDbCheckBox = new JCheckBox("Install Game");
		leftPanel.add(Box.createVerticalStrut(5));
		this._gameDbCheckBox.setSelected(true);
		this._gameDbCheckBox.setPreferredSize(new Dimension(200, 20));
		this._gameDbCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		leftPanel.add(this._gameDbCheckBox);
		this._loginDbCheckBox.addItemListener(_ -> this.checkCheckboxesAndUpdateButtonState());
		this._gameDbCheckBox.addItemListener(_ -> this.checkCheckboxesAndUpdateButtonState());
		this._testConnectionButton = new JButton("Test Connection");
		this._testConnectionButton.addActionListener(_ -> this.testDatabaseConnection());
		leftPanel.add(Box.createVerticalStrut(5));
		this._testConnectionButton.setPreferredSize(new Dimension(200, 30));
		this._testConnectionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		leftPanel.add(this._testConnectionButton);
		this._installButton = new JButton("Install Database");
		this._installButton.addActionListener(_ -> this.installDatabase());
		leftPanel.add(Box.createVerticalStrut(5));
		this._installButton.setPreferredSize(new Dimension(200, 30));
		this._installButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		this.checkCheckboxesAndUpdateButtonState();
		leftPanel.add(this._installButton);
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		splitPane.setRightComponent(rightPanel);
		this._outputArea = new JTextPane();
		this._outputArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(this._outputArea);
		rightPanel.add(scrollPane, "Center");
		this._progressBar = new JProgressBar(0, 100);
		this._progressBar.setStringPainted(true);
		this._progressBar.setPreferredSize(new Dimension(200, 20));
		this._progressBar.setVisible(true);
		rightPanel.add(this._progressBar, "South");
		splitPane.setDividerLocation(130);
		this.add(splitPane);
	}

	private void console()
	{
		System.out.println("=========================================================");
		System.out.println("       L2jBAN-JDEV Development - Database Installer        ");
		System.out.println("               Created by Skache                        ");
		System.out.println("=========================================================");
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
								System.out.println(System.lineSeparator() + "[INFO] You selected: Test Database Connection");
								this.testDatabaseConnectionConsole(scanner);
								break;
							case 2:
								System.out.println(System.lineSeparator() + "[INFO] You selected: Install Database");
								this.installDatabaseMenu(scanner);
								break;
							case 3:
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
		System.out.println("  [1] Test Database Connection");
		System.out.println("  [2] Install Database");
		System.out.println("  [3] Exit");
		System.out.println("---------------------------------------------------------");
	}

	private void installDatabaseMenu(Scanner scanner)
	{
		System.out.println("=========================================================");
		System.out.println("             L2jBAN-JDEV Database Installation             ");
		System.out.println("=========================================================");
		System.out.println("[1] Install Both Login and Game Databases");
		System.out.println("[2] Install Login Database");
		System.out.println("[3] Install Game Database");
		System.out.println("[4] Exit");
		System.out.print("\nEnter your choice: ");
		int choice = scanner.nextInt();
		scanner.nextLine();
		boolean installationSuccessful = false;
		System.out.print("Enter MySQL Host [localhost]: ");
		String host = scanner.nextLine().trim();
		if (host.isEmpty())
		{
			host = "localhost";
		}

		System.out.print("Enter MySQL Port [3306]: ");
		String port = scanner.nextLine().trim();
		if (port.isEmpty())
		{
			port = "3306";
		}

		System.out.print("Enter MySQL Username [root]: ");
		String username = scanner.nextLine().trim();
		if (username.isEmpty())
		{
			username = "root";
		}

		System.out.print("Enter MySQL Password: ");
		String password = scanner.nextLine().trim();
		System.out.print("Enter Database Name [l2jBAN-JDEVinterlude]: ");
		String dbName = scanner.nextLine().trim();
		if (dbName.isEmpty())
		{
			dbName = "l2jBAN-JDEVinterlude";
		}

		switch (choice)
		{
			case 1:
				System.out.println("[INFO] You selected: Install Both Login and Game Databases");
				installationSuccessful = this.installDatabaseConsole("login", host, port, username, password, dbName, scanner, true);
				if (installationSuccessful)
				{
					installationSuccessful = this.installDatabaseConsole("game", host, port, username, password, dbName, scanner, false);
				}
				break;
			case 2:
				System.out.println("[INFO] You selected: Install Login Database");
				installationSuccessful = this.installDatabaseConsole("login", host, port, username, password, dbName, scanner, true);
				break;
			case 3:
				System.out.println("[INFO] You selected: Install Game Database");
				installationSuccessful = this.installDatabaseConsole("game", host, port, username, password, dbName, scanner, false);
				break;
			case 4:
				System.out.println("[INFO] Exiting installation...");
				return;
			default:
				System.out.println("[ERROR] Invalid choice. Please select a valid option.");
		}

		if (installationSuccessful)
		{
			System.out.println("[INFO] Installation completed successfully.");
		}
		else
		{
			System.out.println("[ERROR] Installation failed. Please check the error logs.");
		}
	}

	private boolean installDatabaseConsole(String dbType, String host, String port, String username, String password, String dbName, Scanner scanner, boolean isLogin)
	{
		try
		{
			boolean isDatabaseCreated = this.createDatabaseConsole(host, port, username, password, dbName, scanner);
			if (!isDatabaseCreated)
			{
				return false;
			}
			System.out.println("[INFO] Executing SQL scripts for " + dbType + " database...");
			return this.executeDatabaseScriptsConsole(dbType, host, port, username, password, dbName);
		}
		catch (Exception var11)
		{
			System.out.println("[ERROR] An unexpected error occurred: " + var11.getMessage());
			var11.printStackTrace();
			return false;
		}
	}

	protected boolean createDatabaseConsole(String host, String port, String username, String password, String dbName, Scanner scanner)
	{
		String dbUrl = "jdbc:mysql://" + host + ":" + port;

		try
		{
			boolean var17;
			try (Connection connection = DriverManager.getConnection(dbUrl, username, password); Statement statement = connection.createStatement();)
			{
				ResultSet resultSet = statement.executeQuery("SHOW DATABASES LIKE '" + dbName + "'");
				if (resultSet.next())
				{
					System.out.println("[INFO] Database '" + dbName + "' already exists, skipping creation.");
					return true;
				}

				statement.execute("CREATE DATABASE `" + dbName + "`");
				System.out.println("[INFO] Database '" + dbName + "' created successfully.");
				var17 = true;
			}

			return var17;
		}
		catch (SQLException var16)
		{
			System.out.println("[ERROR] Error creating the database: " + var16.getMessage());
			return false;
		}
	}

	private boolean executeDatabaseScriptsConsole(String dbType, String host, String port, String username, String password, String dbName)
	{
		String sqlDirectory = "sql/" + dbType;
		File dir = new File(sqlDirectory);
		if (dir.exists() && dir.isDirectory())
		{
			File[] sqlFiles = dir.listFiles((_, name) -> name.endsWith(".sql"));
			if (sqlFiles != null && sqlFiles.length != 0)
			{
				Arrays.sort(sqlFiles, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

				try
				{
					boolean var21;
					try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName, username, password); Statement statement = connection.createStatement();)
					{
						for (File sqlFile : sqlFiles)
						{
							this.executeSQLScriptConsole(statement, sqlFile);
						}

						var21 = true;
					}

					return var21;
				}
				catch (SQLException var20)
				{
					System.out.println("[ERROR] Error executing SQL: " + var20.getMessage());
					return false;
				}
			}
			System.out.println("[ERROR] No SQL files found in directory: " + dir.getAbsolutePath());
			return false;
		}
		System.out.println("[ERROR] Directory not found: " + dir.getAbsolutePath());
		return false;
	}

	protected void executeSQLScriptConsole(Statement statement, File sqlFile)
	{
		System.out.println("[INFO] Executing SQL script: " + sqlFile.getName());

		try (Scanner fileScanner = new Scanner(sqlFile))
		{
			StringBuilder sb = new StringBuilder();

			while (fileScanner.hasNextLine())
			{
				String line = fileScanner.nextLine().trim();
				if (!line.startsWith("--") && !line.isEmpty())
				{
					if (line.contains("--"))
					{
						line = line.split("--")[0].trim();
					}

					sb.append(line).append(" ");
					if (line.endsWith(";"))
					{
						String sql = sb.toString().trim();
						if (!sql.isEmpty())
						{
							try
							{
								statement.execute(sql);
							}
							catch (SQLException var9)
							{
								System.out.println("[ERROR] Error executing SQL: " + sql + " - " + var9.getMessage());
							}
						}

						sb.setLength(0);
					}
				}
			}
		}
		catch (IOException var11)
		{
			System.out.println("[ERROR] Error reading SQL file: " + sqlFile.getName() + " - " + var11.getMessage());
		}
	}

	private void testDatabaseConnectionConsole(Scanner scanner)
	{
		try
		{
 
			System.out.print("Enter Host [localhost]: ");
			String host = scanner.nextLine().trim();
			if (host.isEmpty())
			{
				host = "localhost";
			}

			System.out.print("Enter Port [3306]: ");
			String port = scanner.nextLine().trim();
			if (port.isEmpty())
			{
				port = "3306";
			}

			System.out.print("Enter Username [root]: ");
			String username = scanner.nextLine().trim();
			if (username.isEmpty())
			{
				username = "root";
			}

			System.out.print("Enter Password: ");
			String password = scanner.nextLine().trim();
			if (this.testConnection(host, port, username, password))
			{
				System.out.println(System.lineSeparator() + "[INFO] Connection successful!");
			}
			else
			{
				System.out.println(System.lineSeparator() + "[ERROR] Unable to connect to the database. Check your credentials.");
			}
		}
		catch (Exception var9)
		{
			System.out.println(System.lineSeparator() + "[ERROR] An unexpected error occurred: " + var9.getMessage());
			var9.printStackTrace();
		}
	}

	private void testDatabaseConnection()
	{
		(new SwingWorker<Void, Void>()
		{
			{
				Objects.requireNonNull(DatabaseInstaller.this);
			}

			@Override
			protected final Void doInBackground()
			{
				String host = DatabaseInstaller.this._hostField.getText().trim();
				String port = DatabaseInstaller.this._portField.getText().trim();
				String user = DatabaseInstaller.this._userField.getText().trim();
				String password = new String(DatabaseInstaller.this._passField.getPassword()).trim();
				if (DatabaseInstaller.this.testConnection(host, port, user, password))
				{
					DatabaseInstaller.this.installationProgress("Connection successful!" + System.lineSeparator(), "Success");
				}
				else
				{
					DatabaseInstaller.this.installationProgress("Error: Unable to connect to the database. Check your credentials." + System.lineSeparator(), "Error");
				}

				return null;
			}
		}).execute();
	}

	protected boolean testConnection(String host, String port, String user, String password)
	{
		String dbUrlWithoutDb = "jdbc:mysql://" + host + ":" + port;

		try
		{
			boolean var7;
			try (Connection _ = DriverManager.getConnection(dbUrlWithoutDb, user, password))
			{
				var7 = true;
			}

			return var7;
		}
		catch (SQLException var11)
		{
			System.err.println("[ERROR] SQLException: " + var11.getMessage());
			var11.printStackTrace();
			return false;
		}
	}

	private void installDatabase()
	{
		this._installButton.setEnabled(false);
		(new SwingWorker<Void, Void>()
		{
			{
				Objects.requireNonNull(DatabaseInstaller.this);
			}

			@Override
			protected Void doInBackground()
			{
				if (DatabaseInstaller.this.createDatabase())
				{
					boolean isLoginInstalled = DatabaseInstaller.this._loginDbCheckBox.isSelected();
					boolean isGameInstalled = DatabaseInstaller.this._gameDbCheckBox.isSelected();
					boolean installationSuccessful = false;
					if (isLoginInstalled)
					{
						installationSuccessful = DatabaseInstaller.this.installDatabase("login");
					}

					if (isGameInstalled)
					{
						installationSuccessful = DatabaseInstaller.this.installDatabase("game");
					}

					if (installationSuccessful)
					{
						if (isLoginInstalled && isGameInstalled)
						{
							DatabaseInstaller.this.installationProgress("Login and Game databases are installed." + System.lineSeparator(), "Success");
						}
						else if (isLoginInstalled)
						{
							DatabaseInstaller.this.installationProgress("Login database is installed." + System.lineSeparator(), "Success");
						}
						else if (isGameInstalled)
						{
							DatabaseInstaller.this.installationProgress("Game database is installed." + System.lineSeparator(), "Success");
						}
					}
					else
					{
						DatabaseInstaller.this.installationProgress("Installation failed. Please check the error logs." + System.lineSeparator(), "Error");
					}
				}

				DatabaseInstaller.this._installButton.setEnabled(true);
				return null;
			}
		}).execute();
	}

	private boolean installDatabase(String dbType)
	{
		boolean installationSuccessful = false;

		try (Connection connection = this.getDatabaseConnection(); Statement statement = connection.createStatement();)
		{
			this.installationProgress("Installing " + dbType + " database..." + System.lineSeparator(), "Info");
			if (this.executeDatabaseScripts(dbType, statement))
			{
				installationSuccessful = true;
			}
			else
			{
				this.installationProgress("Failed to install " + dbType + " database: Directory does not exist or scripts not executed." + System.lineSeparator(), "Error");
			}
		}
		catch (SQLException var11)
		{
			this.installationProgress("Error installing " + dbType + " database: " + var11.getMessage() + System.lineSeparator(), "Error");
		}

		return installationSuccessful;
	}

	private boolean executeDatabaseScripts(String dbType, Statement statement)
	{
		String sqlDirectory = Paths.get("sql", dbType).toString();
		File dir = new File(sqlDirectory);
		if (dir.exists() && dir.isDirectory())
		{
			File[] sqlFiles = dir.listFiles((_, name) -> name.endsWith(".sql"));
			if (sqlFiles != null && sqlFiles.length != 0)
			{
				Arrays.sort(sqlFiles, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
				int totalFiles = sqlFiles.length;
				int completedFiles = 0;

				for (File sqlFile : sqlFiles)
				{
					this.executeSQLScript(statement, sqlFile, false);
					completedFiles++;
					int progress = (int) ((double) completedFiles / totalFiles * 100.0);
					this._progressBar.setValue(progress);
				}

				return true;
			}
			this.installationProgress("No SQL files found in directory: " + dir.getAbsolutePath() + System.lineSeparator(), "Error");
			return false;
		}
		this.installationProgress("Error: Directory does not exist: " + dir.getAbsolutePath() + System.lineSeparator(), "Error");
		return false;
	}

	private void executeSQLScript(Statement statement, File sqlFile, boolean skipErrors)
	{
		this.installationProgress("Installing " + sqlFile.getName() + System.lineSeparator(), "Info");
		int totalStatements = 0;

		try (Scanner countScanner = new Scanner(sqlFile))
		{
			while (countScanner.hasNextLine())
			{
				String line = countScanner.nextLine().trim();
				if (line.endsWith(";"))
				{
					totalStatements++;
				}
			}
		}
		catch (IOException var18)
		{
			this.installationProgress("Error reading SQL file: " + sqlFile.getName() + " - " + var18.getMessage() + System.lineSeparator(), "Error");
			return;
		}

		int completedStatements = 0;

		try (Scanner executeScanner = new Scanner(sqlFile))
		{
			StringBuilder sb = new StringBuilder();

			while (executeScanner.hasNextLine())
			{
				String line = executeScanner.nextLine().trim();
				if (!line.startsWith("--") && !line.isEmpty())
				{
					if (line.contains("--"))
					{
						line = line.split("--")[0].trim();
					}

					sb.append(line).append(" ");
					if (line.endsWith(";"))
					{
						String sql = sb.toString().trim();
						if (!sql.isEmpty())
						{
							try
							{
								statement.execute(sql);
								completedStatements++;
								int progress = (int) ((double) completedStatements / totalStatements * 100.0);
								this._progressBar.setValue(progress);
							}
							catch (SQLException var14)
							{
								this.installationProgress("Error executing SQL: " + sql + System.lineSeparator() + "Error: " + var14.getMessage() + System.lineSeparator(), "Error");
								if (!skipErrors)
								{
									Object[] options = new Object[]
									{
										"Continue",
										"Abort"
									};
									if (JOptionPane.showOptionDialog(null, "MySQL Error: " + var14.getMessage(), "Script Error", 0, 2, null, options, options[0]) == 1)
									{
										System.exit(0);
									}
								}
							}

							sb.setLength(0);
						}
					}
				}
			}
		}
		catch (IOException var16)
		{
			this.installationProgress("Error reading SQL file: " + sqlFile.getName() + " - " + var16.getMessage() + System.lineSeparator(), "Error");
		}
	}

	private boolean createDatabase()
	{
		String host = this._hostField.getText().trim();
		String port = this._portField.getText().trim();
		String user = this._userField.getText().trim();
		String password = new String(this._passField.getPassword()).trim();
		String dbName = this._dbNameField.getText().trim();
		String dbUrl = "jdbc:mysql://" + host + ":" + port;
		if (dbName.isEmpty())
		{
			this.installationProgress("Error: Database name cannot be empty." + System.lineSeparator(), "Error");
			return false;
		}
		try
		{
			int confirm;
			try (Connection connection = DriverManager.getConnection(dbUrl, user, password); Statement statement = connection.createStatement();)
			{
				this.installationProgress("Connected." + System.lineSeparator(), "Info");
				ResultSet result = statement.executeQuery("SHOW DATABASES LIKE '" + dbName + "'");
				if (result.next())
				{
					confirm = JOptionPane.showOptionDialog(null, "Database '" + dbName + "' already exists. Do you want to reset it?" + System.lineSeparator() + "This will delete all existing data in the database.", "Database Exists", 1, 2, null, new String[]
					{
						"Delete and Recreate",
						"Install on Existing Database",
						"Cancel"
					}, "Cancel");
					if (confirm == 0)
					{
						this.installationProgress("Backing up existing database..." + System.lineSeparator(), "Info");
						this.dumpDatabase();
						statement.execute("DROP DATABASE `" + dbName + "`");
						this.installationProgress("Database '" + dbName + "' deleted." + System.lineSeparator(), "Info");
						statement.execute("CREATE DATABASE `" + dbName + "`");
						this.installationProgress("Database '" + dbName + "' created." + System.lineSeparator(), "Info");
					}
					else if (confirm == 1)
					{
						this.installationProgress("Proceeding with installation on existing database." + System.lineSeparator(), "Info");
					}
					else if (confirm == 2)
					{
						this.installationProgress("Installation cancelled." + System.lineSeparator(), "Info");
						return false;
					}
				}
				else
				{
					statement.execute("CREATE DATABASE `" + dbName + "`");
					this.installationProgress("Database '" + dbName + "' created successfully." + System.lineSeparator(), "Info");
				}

				statement.execute("USE `" + dbName + "`");
				this.installationProgress("Database '" + dbName + "' is ready." + System.lineSeparator(), "Info");
				confirm = 1;
			}

			return confirm != 0;
		}
		catch (SQLException var16)
		{
			this.installationProgress("Error: " + var16.getMessage() + System.lineSeparator(), "Error");
			return false;
		}
	}

	private void dumpDatabase()
	{
		String host = this._hostField.getText().trim();
		String port = this._portField.getText().trim();
		String user = this._userField.getText().trim();
		String password = new String(this._passField.getPassword()).trim();
		String dbName = this._dbNameField.getText().trim();
		this.ensureDumpsDirectoryExists();
		String timestamp = new SimpleDateFormat("dd.MM.yyyy_HH-mm").format(new Date());
		String filename = "dumps/" + dbName + "_dump_" + timestamp + ".sql";
		this.installationProgress("Writing dump " + timestamp + System.lineSeparator(), "Info");

		try (Connection con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName, user, password); Statement statement = con.createStatement(); ResultSet result = statement.executeQuery("SHOW TABLES");)
		{
			File dumpFile = new File(filename);

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(dumpFile)))
			{
				writer.write("/* MySQL Dump: " + dbName + " */" + System.lineSeparator() + System.lineSeparator());

				while (result.next())
				{
					String tableName = result.getString(1);
					this.installationProgress("Dumping Table " + tableName + System.lineSeparator(), "Info");
					writer.write("CREATE TABLE `" + tableName + "` (" + System.lineSeparator());

					try (Statement descStatement = con.createStatement(); ResultSet descResult = descStatement.executeQuery("DESC " + tableName);)
					{
						for (boolean firstColumn = true; descResult.next(); firstColumn = false)
						{
							if (!firstColumn)
							{
								writer.write("," + System.lineSeparator());
							}

							writer.write("\t`" + descResult.getString(1) + "` " + descResult.getString(2));
							if (descResult.getString(3).equals("NO"))
							{
								writer.write(" NOT NULL");
							}

							if (descResult.getString(4) != null)
							{
								writer.write(" DEFAULT '" + descResult.getString(4) + "'");
							}
						}

						writer.write(System.lineSeparator() + ");" + System.lineSeparator() + System.lineSeparator());
					}

					try (Statement dataStatement = con.createStatement(); ResultSet dataResult = dataStatement.executeQuery("SELECT * FROM " + tableName);)
					{
						int rowCount = 0;

						while (dataResult.next())
						{
							if (rowCount % 100 == 0)
							{
								writer.write("INSERT INTO `" + tableName + "` VALUES ");
							}

							if (rowCount > 0)
							{
								writer.write("," + System.lineSeparator());
							}

							writer.write("(");

							for (int i = 1; i <= dataResult.getMetaData().getColumnCount(); i++)
							{
								if (i > 1)
								{
									writer.write(", ");
								}

								writer.write("'" + dataResult.getString(i).replace("'", "\\'") + "'");
							}

							writer.write(")");
							if (++rowCount % 100 == 0)
							{
								writer.write(";" + System.lineSeparator());
							}
						}

						if (rowCount % 100 != 0)
						{
							writer.write(";" + System.lineSeparator());
						}
					}
				}

				this.installationProgress("Database dump completed: " + filename + System.lineSeparator(), "Success");
			}
		}
		catch (IOException | SQLException var34)
		{
			this.installationProgress("Error: " + var34.getMessage() + System.lineSeparator(), "Error");
		}
	}

	private void ensureDumpsDirectoryExists()
	{
		File dumpsDirectory = new File("dumps");
		if (!dumpsDirectory.exists())
		{
			boolean created = dumpsDirectory.mkdirs();
			if (created)
			{
				this.installationProgress("Dumps directory created: " + dumpsDirectory.getAbsolutePath() + System.lineSeparator(), "Info");
			}
			else
			{
				this.installationProgress("Error: Unable to create dumps directory." + System.lineSeparator(), "Error");
			}
		}
	}

	private void installationProgress(String text, String style)
	{
		SwingUtilities.invokeLater(() -> {
			try
			{
				StyledDocument doc = this._outputArea.getStyledDocument();
				Style errorStyle = this._outputArea.addStyle("Error", null);
				StyleConstants.setForeground(errorStyle, Color.RED);
				Style successStyle = this._outputArea.addStyle("Success", null);
				StyleConstants.setForeground(successStyle, Color.GREEN);
				Style infoStyle = this._outputArea.addStyle("Info", null);
				StyleConstants.setForeground(infoStyle, Color.WHITE);
				if (style.equals("Error"))
				{
					doc.insertString(doc.getLength(), text + System.lineSeparator(), errorStyle);
				}
				else if (style.equals("Success"))
				{
					doc.insertString(doc.getLength(), text + System.lineSeparator(), successStyle);
				}
				else
				{
					doc.insertString(doc.getLength(), text + System.lineSeparator(), infoStyle);
				}

				this._outputArea.setCaretPosition(doc.getLength());
			}
			catch (BadLocationException var7)
			{
				var7.printStackTrace();
			}
		});
	}

	private void checkCheckboxesAndUpdateButtonState()
	{
		this._installButton.setEnabled(this._loginDbCheckBox.isSelected() || this._gameDbCheckBox.isSelected());
	}

	private Connection getDatabaseConnection() throws SQLException
	{
		String host = this._hostField.getText().trim();
		String port = this._portField.getText().trim();
		String user = this._userField.getText().trim();
		String password = new String(this._passField.getPassword()).trim();
		String dbName = this._dbNameField.getText().trim();
		String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
		return DriverManager.getConnection(dbUrl, user, password);
	}

	public static void main(String[] args)
	{
		new DatabaseInstaller();
	}
}
