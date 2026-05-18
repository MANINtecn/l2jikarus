package net.sf.l2jdev.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import net.sf.l2jdev.commons.config.InterfaceConfig;
import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.time.TimeUtil;
import net.sf.l2jdev.commons.ui.DarkTheme;

public class AccountManager extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JTabbedPane _tabPanel;
	private JTextField _usernameField;
	private JTextField _passwordField;
	private JCheckBox _accessLevelCheckBox;
	private JComboBox<String> _addAccessLevelBox;
	private JButton _createButton;
	private JButton _testConnectionButton;
	private JLabel _statusConnection;
	private JProgressBar _progressBar;
	private JButton _searchButton;
	private JButton _updateButton;
	private JButton _deleteButton;
	private JTextField _searchAccount;
	private JPasswordField _changePassword;
	private JComboBox<String> _accuntSelcet;
	private JComboBox<String> _changeAccessLevelBox;
	private JLabel _accountCount;
	private JLabel _selectedAccount;
	private JTable _accountsTable;
	private JButton _refreshButton;
	private JButton _nextButton;
	private JButton _prevButton;
	private JLabel _totalAccounts;
	private JLabel _statusPages;
	private int currentPage = 1;

	public AccountManager()
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
		this.setTitle("BAN-JDEV - Account Manager");
		this.setMinimumSize(new Dimension(600, 400));
		this.setDefaultCloseOperation(3);
		this.setLocationRelativeTo(null);
		this._tabPanel = new JTabbedPane();
		this._tabPanel.addTab("➕ Create Account", null, this.createAccountPanel(), "Create a new game account.");
		this._tabPanel.addTab("\ud83d\udee0 Manage Accounts", null, this.manageAccountsPanel(), "Edit or manage existing accounts.");
		this._tabPanel.addTab("\ud83d\udcdc List Accounts", null, this.listAccountsPanel(), "View all registered accounts.");
		this._tabPanel.addChangeListener(_ -> {
			int selectedIndex = this._tabPanel.getSelectedIndex();
			if (selectedIndex == 2)
			{
				this.loadPage(this.currentPage);
			}
		});
		this.addWindowListener(new WindowAdapter()
		{
			{
				Objects.requireNonNull(AccountManager.this);
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
				System.out.println("[INFO] Cleaning up resources...");
				DatabaseFactory.close();
				System.exit(0);
			}
		});
		List<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "16x16.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "32x32.png").getImage());

		this.add(this._tabPanel);
		this.setIconImages(icons);
		this.setVisible(true);
	}

	private JPanel createAccountPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = 2;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Username:"), gbc);
		gbc.gridx = 1;
		this._usernameField = new JTextField(15);
		this._usernameField.setToolTipText("Enter the username for the account.");
		panel.add(this._usernameField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("Password:"), gbc);
		gbc.gridx = 1;
		this._passwordField = new JPasswordField(15);
		this._passwordField.setToolTipText("Enter the password for the account.");
		panel.add(this._passwordField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		JLabel accessLevelLabel = new JLabel("Access Level:");
		accessLevelLabel.setToolTipText("Disablad check box means default 'User' access level.");
		panel.add(accessLevelLabel, gbc);
		JPanel accessLevelPanel = new JPanel(new FlowLayout(0, 5, 0));
		this._accessLevelCheckBox = new JCheckBox();
		this._accessLevelCheckBox.setToolTipText("Enable checkbox to add access level.");
		this._addAccessLevelBox = new JComboBox<>(new String[]
		{
			"0 - User",
			"-1 - Banned",
			"10 - Chat Moderator",
			"20 - Test GM",
			"30 - General GM",
			"40 - Support GM",
			"50 - Event GM",
			"60 - Head GM",
			"70 - Admin",
			"100 - Master"
		});
		this._addAccessLevelBox.setEnabled(false);
		this._addAccessLevelBox.setToolTipText("Select the access level for the account.");
		this._accessLevelCheckBox.addActionListener(_ -> this._addAccessLevelBox.setEnabled(this._accessLevelCheckBox.isSelected()));
		accessLevelPanel.add(this._accessLevelCheckBox);
		accessLevelPanel.add(this._addAccessLevelBox);
		this._createButton = new JButton("Create Account  \ud83d\udcdd");
		this._createButton.setPreferredSize(new Dimension(150, 35));
		this._createButton.addActionListener(this::createAccount);
		this._createButton.setToolTipText("Click to create the new account.");
		gbc.gridx = 1;
		panel.add(accessLevelPanel, gbc);
		gbc.gridx = 2;
		panel.add(this._createButton, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 3;
		this._progressBar = new JProgressBar();
		this._progressBar.setPreferredSize(new Dimension(200, 25));
		this._progressBar.setVisible(true);
		panel.add(this._progressBar, gbc);
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		this._testConnectionButton = new JButton("Test Connection  \ud83d\udce1");
		this._testConnectionButton.setPreferredSize(new Dimension(150, 35));
		this._testConnectionButton.addActionListener(this::testConnection);
		this._testConnectionButton.setToolTipText("Click to test the database connection.");
		panel.add(this._testConnectionButton, gbc);
		gbc.gridx = 1;
		this._statusConnection = new JLabel("●");
		this._statusConnection.setFont(new Font("Arial", 1, 50));
		this._statusConnection.setForeground(Color.WHITE);
		panel.add(this._statusConnection, gbc);
		return panel;
	}

	private JPanel manageAccountsPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JPanel topPanel = new JPanel(new GridLayout(1, 2));
		topPanel.setPreferredSize(new Dimension(panel.getWidth(), 40));
		this._selectedAccount = new JLabel("", 2);
		this._selectedAccount.setFont(new Font("Arial", 1, 16));
		this._selectedAccount.setCursor(Cursor.getPredefinedCursor(12));
		this._selectedAccount.addMouseListener(new MouseAdapter()
		{
			{
				Objects.requireNonNull(AccountManager.this);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (!AccountManager.this._selectedAccount.getText().isEmpty() && !AccountManager.this._selectedAccount.getText().equals("No account selected"))
				{
					AccountManager.this.showAccountInfoPopup();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				if (!AccountManager.this._selectedAccount.getText().isEmpty() && !AccountManager.this._selectedAccount.getText().equals("No account selected"))
				{
					AccountManager.this._selectedAccount.setCursor(Cursor.getPredefinedCursor(12));
				}
				else
				{
					AccountManager.this._selectedAccount.setCursor(Cursor.getDefaultCursor());
				}
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				AccountManager.this._selectedAccount.setCursor(Cursor.getDefaultCursor());
			}
		});
		this._selectedAccount.setUI(new BasicLabelUI()
		{
			{
				Objects.requireNonNull(AccountManager.this);
			}

			@Override
			public void paint(Graphics g, JComponent c)
			{
				super.paint(g, c);
				String text = ((JLabel) c).getText();
				if (text != null && !text.isEmpty())
				{
					FontMetrics fm = g.getFontMetrics();
					int textWidth = fm.stringWidth(text);
					 
					int y = c.getHeight() - 8;
					g.drawLine(0, y, 0 + textWidth, y);
				}
			}
		});
		topPanel.add(this._selectedAccount);
		this._accountCount = new JLabel("", 2);
		this._accountCount.setFont(new Font("Arial", 1, 14));
		topPanel.add(this._accountCount);
		panel.add(topPanel, "North");
		JPanel centerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		centerPanel.add(new JLabel("Search Account:"), gbc);
		gbc.gridx = 1;
		this._searchAccount = new JTextField();
		this._searchAccount.setPreferredSize(new Dimension(200, 25));
		this._searchAccount.setToolTipText("Search for an account by username.");
		centerPanel.add(this._searchAccount, gbc);
		this._searchAccount.getDocument().addDocumentListener(new DocumentListener()
		{
			{
				Objects.requireNonNull(AccountManager.this);
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				this.toggleSearchButton();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				this.toggleSearchButton();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				this.toggleSearchButton();
			}

			private void toggleSearchButton()
			{
				if (AccountManager.this._searchAccount.getText().length() >= 1)
				{
					AccountManager.this._searchButton.setEnabled(true);
				}
				else
				{
					AccountManager.this._searchButton.setEnabled(false);
				}
			}
		});
		this._searchAccount.addKeyListener(new KeyAdapter()
		{
			{
				Objects.requireNonNull(AccountManager.this);
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == 10 && AccountManager.this._searchButton.isEnabled())
				{
					AccountManager.this.searchAccount(AccountManager.this._searchAccount.getText());
				}
			}
		});
		gbc.gridx = 2;
		this._searchButton = new JButton("Search \ud83d\udd0d");
		this._searchButton.setEnabled(false);
		this._searchButton.addActionListener(_ -> this.searchAccount(this._searchAccount.getText()));
		centerPanel.add(this._searchButton, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		centerPanel.add(new JLabel("Select Account:"), gbc);
		this._accuntSelcet = new JComboBox<>();
		this._accuntSelcet.setPreferredSize(new Dimension(200, 25));
		this._accuntSelcet.setToolTipText("Select an account.");
		this._accuntSelcet.setEnabled(false);
		this._accuntSelcet.addActionListener(_ -> {
			if (this._accuntSelcet.getSelectedItem() != null)
			{
				this._changePassword.setEnabled(true);
				this._updateButton.setEnabled(true);
				this._changeAccessLevelBox.setEnabled(true);
			}
			else
			{
				this._changePassword.setEnabled(false);
				this._updateButton.setEnabled(false);
				this._changeAccessLevelBox.setEnabled(false);
			}
		});
		gbc.gridx = 1;
		centerPanel.add(this._accuntSelcet, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		centerPanel.add(new JLabel("New Password:"), gbc);
		gbc.gridx = 1;
		this._changePassword = new JPasswordField();
		this._changePassword.setPreferredSize(new Dimension(200, 25));
		this._changePassword.setEnabled(false);
		centerPanel.add(this._changePassword, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		centerPanel.add(new JLabel("Change Access Level:"), gbc);
		gbc.gridx = 1;
		this._changeAccessLevelBox = new JComboBox<>(new String[]
		{
			"0 - User",
			"-1 - Banned",
			"10 - Chat Moderator",
			"20 - Test GM",
			"30 - General GM",
			"40 - Support GM",
			"50 - Event GM",
			"60 - Head GM",
			"70 - Admin",
			"100 - Master"
		});
		this._changeAccessLevelBox.setEnabled(false);
		this._changeAccessLevelBox.setPreferredSize(new Dimension(200, 25));
		centerPanel.add(this._changeAccessLevelBox, gbc);
		gbc.gridx = 0;
		gbc.gridy = 5;
		this._updateButton = new JButton("Update Account Information ✏️");
		this._updateButton.setPreferredSize(new Dimension(220, 35));
		this._updateButton.setEnabled(false);
		this._updateButton.addActionListener(this::updateAccount);
		centerPanel.add(this._updateButton, gbc);
		gbc.gridx = 1;
		gbc.gridy = 5;
		this._deleteButton = new JButton("Delete Account Permanently ❌");
		this._deleteButton.setPreferredSize(new Dimension(220, 35));
		this._deleteButton.setEnabled(false);
		this._deleteButton.addActionListener(this::deleteAccount);
		centerPanel.add(this._deleteButton, gbc);
		panel.add(centerPanel, "Center");
		return panel;
	}

	private JPanel listAccountsPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		String[] columnNames = new String[]
		{
			"User",
			"Access Level",
			"Email",
			"Last IP",
			"Last Server",
			"PC IP",
			"Hop 1",
			"Hop 2",
			"Hop 3",
			"Hop 4",
			"Creation Date",
			"Last Active"
		};
		DefaultTableModel model = new DefaultTableModel(columnNames, 0);
		this._accountsTable = new JTable(model)
		{
			{
				Objects.requireNonNull(AccountManager.this);
			}

			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		this._accountsTable.setAutoResizeMode(0);
		TableColumnModel columnModel = this._accountsTable.getColumnModel();

		for (int i = 0; i < columnModel.getColumnCount(); i++)
		{
			columnModel.getColumn(i).setPreferredWidth(150);
		}

		JScrollPane scrollPane = new JScrollPane(this._accountsTable);
		scrollPane.setHorizontalScrollBarPolicy(30);
		final JTextField searchField = new JTextField(15);
		searchField.setToolTipText("Search by username...");
		searchField.addKeyListener(new KeyAdapter()
		{
			{
				Objects.requireNonNull(AccountManager.this);
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				String query = searchField.getText().trim().toLowerCase();
				AccountManager.this.filterAccounts(query);
			}
		});
		JPanel searchPanel = new JPanel(new BorderLayout());
		JPanel leftSearchPanel = new JPanel(new FlowLayout(0));
		leftSearchPanel.add(new JLabel("Filter: "));
		leftSearchPanel.add(searchField);
		this._totalAccounts = new JLabel("");
		JPanel rightSearchPanel = new JPanel(new FlowLayout(2));
		rightSearchPanel.add(this._totalAccounts);
		searchPanel.add(leftSearchPanel, "West");
		searchPanel.add(rightSearchPanel, "East");
		this._refreshButton = new JButton("Refresh ♻️");
		this._refreshButton.setToolTipText("Reload the account list");
		this._refreshButton.addActionListener(_ -> this.refreshAccountsList());
		JPanel paginationPanel = new JPanel(new FlowLayout(0));
		this._prevButton = new JButton("⬅️ Previous");
		this._nextButton = new JButton("Next ➡️");
		this._prevButton.setToolTipText("Go to the previous page");
		this._nextButton.setToolTipText("Go to the next page");
		this._prevButton.addActionListener(_ -> this.loadPage(this.currentPage - 1));
		this._nextButton.addActionListener(_ -> this.loadPage(this.currentPage + 1));
		paginationPanel.add(this._prevButton);
		paginationPanel.add(this._nextButton);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(this._refreshButton, "West");
		bottomPanel.add(paginationPanel, "East");
		this._statusPages = new JLabel();
		JPanel statusPanel = new JPanel(new FlowLayout(1));
		statusPanel.add(this._statusPages);
		bottomPanel.add(statusPanel, "Center");
		panel.add(searchPanel, "North");
		panel.add(scrollPane, "Center");
		panel.add(bottomPanel, "South");
		this.refreshAccountsList();
		return panel;
	}

	private void refreshAccountsList()
	{
		this._refreshButton.setEnabled(false);
		(new SwingWorker<Object[][], Void>()
		{
			{
				Objects.requireNonNull(AccountManager.this);
			}

			@Override
			protected Object[][] doInBackground()
			{
				return AccountManager.this.loadAccountsData(AccountManager.this.currentPage);
			}

			@Override
			protected void done()
			{
				try
				{
					Object[][] data = this.get();
					DefaultTableModel model = (DefaultTableModel) AccountManager.this._accountsTable.getModel();
					model.setRowCount(0);

					for (Object[] row : data)
					{
						model.addRow(row);
					}

					int totalAccounts = AccountManager.this.getTotalAccountsCount();
					AccountManager.this._totalAccounts.setText("<html><b>Total Accounts: " + totalAccounts + "</b></html>");
				}
				catch (Exception var10)
				{
					var10.printStackTrace();
				}
				finally
				{
					AccountManager.this._refreshButton.setEnabled(true);
				}
			}
		}).execute();
	}

	private void filterAccounts(String query)
	{
		DefaultTableModel model = (DefaultTableModel) this._accountsTable.getModel();
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
		this._accountsTable.setRowSorter(sorter);
		if (query.isEmpty())
		{
			sorter.setRowFilter(null);
		}
		else
		{
			sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 0));
		}
	}

	private void loadPage(int newPage)
	{
		if (newPage >= 1)
		{
			int totalPages = this.getTotalPages();
			if (newPage <= totalPages)
			{
				this.currentPage = newPage;
				this._statusPages.setText("Page " + this.currentPage + " / " + totalPages);
				this._prevButton.setEnabled(this.currentPage > 1);
				this._nextButton.setEnabled(this.currentPage < totalPages);
				this.refreshAccountsList();
			}
		}
	}

	private int getTotalPages()
	{
		int totalAccounts = this.getTotalAccountsCount();
		return (int) Math.ceil(totalAccounts / 1000.0);
	}

	protected int getTotalAccountsCount()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM accounts"); ResultSet result = statement.executeQuery();)
		{
			return result.next() ? result.getInt(1) : -1;
		}
		catch (SQLException var12)
		{
			JOptionPane.showMessageDialog(null, "Error while retrieving total accounts count: " + var12.getMessage(), "Database Error", 0);
			return -1;
		}
	}

	protected Object[][] loadAccountsData(int page)
	{
		int offset = (page - 1) * 1000;
		List<Object[]> accountList = new ArrayList<>();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT login, accessLevel, email, lastIP, lastServer, pcIp, hop1, hop2, hop3, hop4, created_time, lastactive FROM accounts LIMIT ? OFFSET ?");)
		{
			statement.setInt(1, 1000);
			statement.setInt(2, offset);

			try (ResultSet result = statement.executeQuery())
			{
				while (result.next())
				{
					accountList.add(new Object[]
					{
						result.getString("login"),
						result.getInt("accessLevel"),
						result.getString("email") != null ? result.getString("email") : "N/A",
						result.getString("lastIP") != null ? result.getString("lastIP") : "Unknown",
						result.getInt("lastServer"),
						result.getString("pcIp") != null ? result.getString("pcIp") : "N/A",
						result.getString("hop1") != null ? result.getString("hop1") : "N/A",
						result.getString("hop2") != null ? result.getString("hop2") : "N/A",
						result.getString("hop3") != null ? result.getString("hop3") : "N/A",
						result.getString("hop4") != null ? result.getString("hop4") : "N/A",
						TimeUtil.getDateTimeString(new Date(result.getTimestamp("created_time").getTime())),
						TimeUtil.getDateTimeString(new Date(result.getLong("lastactive")))
					});
				}
			}
		}
		catch (SQLException var15)
		{
			var15.printStackTrace();
		}

		return accountList.isEmpty() ? new Object[0][0] : accountList.toArray(new Object[0][0]);
	}

	private void showAccountInfoPopup()
	{
		String accountName = this._selectedAccount.getText();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT login, accessLevel, email, lastIP, created_time, lastactive FROM accounts WHERE login = ?");)
		{
			statement.setString(1, accountName);

			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					String login = result.getString("login");
					int accessLevel = result.getInt("accessLevel");
					String email = result.getString("email") != null ? result.getString("email") : "N/A";
					String lastIP = result.getString("lastIP") != null ? result.getString("lastIP") : "Unknown";
					String createdTime = result.getTimestamp("created_time") != null ? TimeUtil.getDateTimeString(new Date(result.getTimestamp("created_time").getTime())) : "Unknown";
					String lastActive = result.getLong("lastactive") > 0L ? TimeUtil.getDateTimeString(new Date(result.getLong("lastactive"))) : "Never";
					String message = String.format("<html><b>\ud83d\udd39 Account Name:</b> %s<br><b>\ud83d\udd10 Access Level:</b> %d<br><b>\ud83d\udce7 Email:</b> %s<br><b>\ud83c\udf10 Last IP:</b> %s<br><b>\ud83d\udcc5 Created:</b> %s<br><b>\ud83d\udd52 Last Active:</b> %s</html>", login, accessLevel, email, lastIP, createdTime, lastActive);
					JLabel label = new JLabel(message);
					label.setFont(new Font("Arial", 0, 13));
					JOptionPane.showMessageDialog(null, label, "Account Information", 1);
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Account not found!", "Error", 0);
				}
			}
		}
		catch (SQLException var19)
		{
			var19.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error retrieving account information!", "Database Error", 0);
		}
	}

	private void searchAccount(String username)
	{
		List<String> accountList = new ArrayList<>();
		Map<String, String> accountAccessLevels = new HashMap<>();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT login, accessLevel FROM accounts WHERE login LIKE ?");)
		{
			statement.setString(1, "%" + username + "%");

			try (ResultSet result = statement.executeQuery())
			{
				while (result.next())
				{
					String login = result.getString("login");
					String accessLevel = result.getString("accessLevel");
					accountList.add(login);
					accountAccessLevels.put(login, accessLevel);
				}
			}
		}
		catch (SQLException var15)
		{
			var15.printStackTrace();
		}

		int resultsCount = accountList.size();
		if (resultsCount > 0)
		{
			this._accuntSelcet.setEnabled(true);
			this._accuntSelcet.removeAllItems();

			for (String account : accountList)
			{
				this._accuntSelcet.addItem(account);
			}

			this._accuntSelcet.setSelectedIndex(-1);
			this._accountCount.setText(resultsCount + " account(s) found.");
			this._accountCount.setForeground(Color.GREEN);
			this._selectedAccount.setText("");
			this._updateButton.setEnabled(true);
			this._deleteButton.setEnabled(true);
			this._accuntSelcet.addActionListener(_ -> {
				String selectedAccount = (String) this._accuntSelcet.getSelectedItem();
				if (selectedAccount != null)
				{
					this._selectedAccount.setText(selectedAccount);
				}
			});
		}
		else
		{
			this._accountCount.setText("No account found.");
			this._accountCount.setForeground(Color.RED);
			this._accuntSelcet.setEnabled(false);
			this._changePassword.setEnabled(false);
			this._updateButton.setEnabled(false);
			this._deleteButton.setEnabled(false);
			this._selectedAccount.setText("");
		}
	}

	private void createAccount(ActionEvent event)
	{
		String username = this._usernameField.getText().trim();
		String password = new String(((JPasswordField) this._passwordField).getPassword()).trim();
		int accessLevel = this._accessLevelCheckBox.isSelected() ? Integer.parseInt(((String) this._addAccessLevelBox.getSelectedItem()).split(" - ")[0]) : 0;
		if (!username.isEmpty() && !password.isEmpty())
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				if (con == null)
				{
					return;
				}

				try (PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM accounts WHERE login = ?"))
				{
					statement.setString(1, username);

					try (ResultSet result = statement.executeQuery())
					{
						if (result.next() && result.getInt(1) > 0)
						{
							JOptionPane.showMessageDialog(this, "Username already exists! Please choose another.", "Error", 0);
							return;
						}
					}
				}

				MessageDigest md = MessageDigest.getInstance("SHA");
				byte[] raw = password.getBytes(StandardCharsets.UTF_8);
				String hashBase64 = Base64.getEncoder().encodeToString(md.digest(raw));

				try (PreparedStatement statement = con.prepareStatement("INSERT INTO accounts (login, password, accessLevel) VALUES (?, ?, ?)"))
				{
					statement.setString(1, username);
					statement.setString(2, hashBase64);
					statement.setInt(3, accessLevel);
					if (statement.executeUpdate() > 0)
					{
						JOptionPane.showMessageDialog(this, "Account created successfully!", "Success", 1);
						this._usernameField.setText("");
						this._passwordField.setText("");
					}
					else
					{
						JOptionPane.showMessageDialog(this, "Failed to create the account. Please try again.", "Error", 0);
					}
				}
			}
			catch (Exception var20)
			{
				JOptionPane.showMessageDialog(this, "Error creating account: " + var20.getMessage(), "Error", 0);
				var20.printStackTrace();
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Username and Password cannot be empty!", "Error", 0);
		}
	}

	private void updateAccount(ActionEvent event)
	{
		String password = new String(this._changePassword.getPassword()).trim();
		String selectedUsername = (String) this._accuntSelcet.getSelectedItem();
		String accessLevelStr = (String) this._changeAccessLevelBox.getSelectedItem();
		if (accessLevelStr != null && accessLevelStr.contains(" - "))
		{
			int accessLevel = Integer.parseInt(accessLevelStr.split(" - ")[0]);
			if (selectedUsername != null && !selectedUsername.isEmpty())
			{
				int confirmUpdate = JOptionPane.showConfirmDialog(this, "Are you sure you want to update the account?", "Confirm Update", 0, 3);
				if (confirmUpdate == 0)
				{
					try (Connection con = DatabaseFactory.getConnection())
					{
						if (con == null)
						{
							return;
						}

						StringBuilder sql = new StringBuilder("UPDATE accounts SET ");
						List<Object> params = new ArrayList<>();
						if (!password.isEmpty())
						{
							MessageDigest md = MessageDigest.getInstance("SHA");
							byte[] raw = password.getBytes(StandardCharsets.UTF_8);
							String hashBase64 = Base64.getEncoder().encodeToString(md.digest(raw));
							sql.append("password = ?, ");
							params.add(hashBase64);
						}

						if (this._changeAccessLevelBox.isEnabled())
						{
							sql.append("accessLevel = ?, ");
							params.add(accessLevel);
						}

						if (params.isEmpty())
						{
							JOptionPane.showMessageDialog(this, "Nothing to update!", "Warning", 2);
							return;
						}
						sql.setLength(sql.length() - 2);
						sql.append(" WHERE login = ?");
						params.add(selectedUsername);

						try (PreparedStatement statement = con.prepareStatement(sql.toString()))
						{
							for (int i = 0; i < params.size(); i++)
							{
								statement.setObject(i + 1, params.get(i));
							}

							if (statement.executeUpdate() > 0)
							{
								JOptionPane.showMessageDialog(this, "Account updated successfully!", "Success", 1);
								this._changePassword.setText("");
							}
							else
							{
								JOptionPane.showMessageDialog(this, "Failed to update the account. Please try again.", "Error", 0);
							}
						}
					}
					catch (Exception var17)
					{
						JOptionPane.showMessageDialog(this, "Error updating account: " + var17.getMessage(), "Error", 0);
						var17.printStackTrace();
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(this, "No account selected!", "Error", 0);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Invalid access level selection!", "Error", 0);
		}
	}

	private void deleteAccount(ActionEvent event)
	{
		String username = (String) this._accuntSelcet.getSelectedItem();
		if (username != null && !username.trim().isEmpty())
		{
			int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the account '" + username + "'?", "Confirm Deletion", 0);
			if (confirm == 0)
			{
				try (Connection con = DatabaseFactory.getConnection())
				{
					if (con != null)
					{
						try (PreparedStatement statement = con.prepareStatement("DELETE FROM accounts WHERE login = ?"))
						{
							statement.setString(1, username);
							int rowsAffected = statement.executeUpdate();
							if (rowsAffected > 0)
							{
								JOptionPane.showMessageDialog(this, "Account '" + username + "' deleted successfully!", "Success", 1);
								this._accuntSelcet.removeItem(username);
							}
							else
							{
								JOptionPane.showMessageDialog(this, "No account found with that username.", "Error", 0);
							}
						}
					}
				}
				catch (SQLException var12)
				{
					JOptionPane.showMessageDialog(this, "SQL Error: " + var12.getMessage(), "Database Error", 0);
					var12.printStackTrace();
				}
				catch (Exception var13)
				{
					JOptionPane.showMessageDialog(this, "Unexpected error: " + var13.getMessage(), "Error", 0);
					var13.printStackTrace();
				}
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Please select an account to delete.", "Warning", 2);
		}
	}

	private void testConnection(ActionEvent event)
	{
		this._progressBar.setString("Testing...");
		this._progressBar.setStringPainted(true);
		this._progressBar.setIndeterminate(true);
		SwingWorker<Boolean, Void> worker = new SwingWorker<>()
		{
			{
				Objects.requireNonNull(AccountManager.this);
			}

			@Override
			protected Boolean doInBackground() throws Exception
			{
				Thread.sleep(2000L);
				return AccountManager.this.databaseConnection();
			}

			@Override
			protected void done()
			{
				try
				{
					boolean isConnected = this.get();
					if (isConnected)
					{
						AccountManager.this._statusConnection.setForeground(Color.GREEN);
						AccountManager.this._progressBar.setString("Connected");
					}
					else
					{
						AccountManager.this._statusConnection.setForeground(Color.RED);
						AccountManager.this._progressBar.setString("Failed");
					}

					AccountManager.this._progressBar.setIndeterminate(false);
					AccountManager.this._progressBar.setValue(100);
					SwingWorker<Void, Void> resetColorWorker = new SwingWorker<>()
					{
						@Override
						protected Void doInBackground() throws Exception
						{
							Thread.sleep(5000L);
							return null;
						}

						@Override
						protected void done()
						{
							AccountManager.this._statusConnection.setForeground(Color.WHITE);
						}
					};
					resetColorWorker.execute();
				}
				catch (Exception var3)
				{
					AccountManager.this._statusConnection.setForeground(Color.RED);
					AccountManager.this._progressBar.setString("Failed");
					AccountManager.this._progressBar.setIndeterminate(false);
					AccountManager.this._progressBar.setValue(100);
					var3.printStackTrace();
				}
			}
		};
		worker.execute();
	}

	protected boolean databaseConnection()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			return con != null;
		}
		catch (SQLException var6)
		{
			var6.printStackTrace();
			return false;
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
								System.out.println(System.lineSeparator() + "[INFO] You selected: Create Account");
								this.createAccountCmd(scanner);
								break;
							case 2:
								System.out.println(System.lineSeparator() + "[INFO] You selected: Delete Account");
								this.deleteAccountCmd(scanner);
								break;
							case 3:
								System.out.println(System.lineSeparator() + "[INFO] You selected: Update Accounts");
								this.updateAccountCmd();
								break;
							case 4:
								System.out.println(System.lineSeparator() + "[INFO] You selected: List All Accounts");
								this.listAccountsCmd();
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
		System.out.println("  [1] Create Account");
		System.out.println("  [2] Delete Account");
		System.out.println("  [3] Update Accounts");
		System.out.println("  [4] List All Accounts");
		System.out.println("  [5] Exit");
		System.out.println("---------------------------------------------------------");
	}

	protected void createAccountCmd(Scanner scanner)
	{
		String username = "";
		String password = "";
		int accessLevel = 0;

		while (username.isEmpty())
		{
			System.out.print("Enter username: ");
			username = scanner.nextLine().trim();
			if (username.isEmpty())
			{
				System.out.println("Username cannot be empty! Please provide a valid username.");
			}
		}

		while (password.isEmpty())
		{
			System.out.print("Enter password: ");
			password = scanner.nextLine().trim();
			if (password.isEmpty())
			{
				System.out.println("Password cannot be empty! Please provide a valid password.");
			}
		}

		Set<Integer> validAccessLevels = new HashSet<>(Arrays.asList(-1, 0, 10, 20, 30, 40, 50, 60, 70, 100));
		System.out.println("Select access level from the following options:");
		System.out.println("0 - User (default)");
		System.out.println("-1 - Banned");
		System.out.println("10 - Chat Moderator");
		System.out.println("20 - Test GM");
		System.out.println("30 - General GM");
		System.out.println("40 - Support GM");
		System.out.println("50 - Event GM");
		System.out.println("60 - Head GM");
		System.out.println("70 - Admin");
		System.out.println("100 - Master");
		boolean validAccessLevel = false;

		while (!validAccessLevel)
		{
			System.out.print("Enter access level (default is 0 - User, or choose a number corresponding to the role): ");
			String input = scanner.nextLine().trim();
			if (input.isEmpty())
			{
				validAccessLevel = true;
				accessLevel = 0;
				System.out.println("Default access level (0 - User) selected.");
			}
			else
			{
				try
				{
					accessLevel = Integer.parseInt(input);
					if (validAccessLevels.contains(accessLevel))
					{
						validAccessLevel = true;
					}
					else
					{
						System.out.println("Invalid access level! Please select a valid role.");
					}
				}
				catch (NumberFormatException var18)
				{
					System.out.println("Invalid input. Please enter a valid number for access level.");
				}
			}
		}

		try (Connection con = DatabaseFactory.getConnection())
		{
			if (con == null)
			{
				return;
			}

			try (PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM accounts WHERE login = ?"))
			{
				statement.setString(1, username);

				try (ResultSet result = statement.executeQuery())
				{
					if (result.next() && result.getInt(1) > 0)
					{
						System.out.println("Username already exists! Please choose another.");
						return;
					}
				}
			}

			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
			String hashedPassword = Base64.getEncoder().encodeToString(hashedBytes);

			try (PreparedStatement statement = con.prepareStatement("INSERT INTO accounts (login, password, accessLevel) VALUES (?, ?, ?)"))
			{
				statement.setString(1, username);
				statement.setString(2, hashedPassword);
				statement.setInt(3, accessLevel);
				if (statement.executeUpdate() > 0)
				{
					System.out.println("Account " + username + " created successfully!");
				}
				else
				{
					System.out.println("Failed to create the account.");
				}
			}
		}
		catch (Exception var23)
		{
			System.out.println("Error creating account: " + var23.getMessage());
		}
	}

	protected void deleteAccountCmd(Scanner scanner)
	{
		System.out.print("Enter the username of the account to delete: ");
		String username = "";
		if (scanner.hasNextLine())
		{
			username = scanner.nextLine().trim();
			if (username.isEmpty())
			{
				System.out.println("Username cannot be empty!");
			}
			else
			{
				System.out.print("Are you sure you want to delete the account '" + username + "'? (y/n): ");
				String confirmation = scanner.nextLine().trim().toLowerCase();
				if (!confirmation.equals("y"))
				{
					System.out.println("Account deletion cancelled.");
				}
				else
				{
					try (Connection con = DatabaseFactory.getConnection())
					{
						if (con == null)
						{
							return;
						}

						try (PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM accounts WHERE login = ?"))
						{
							statement.setString(1, username);

							try (ResultSet result = statement.executeQuery())
							{
								if (result.next() && result.getInt(1) == 0)
								{
									System.out.println("Account with the username '" + username + "' does not exist.");
									return;
								}
							}
						}

						try (PreparedStatement statement = con.prepareStatement("DELETE FROM accounts WHERE login = ?"))
						{
							statement.setString(1, username);
							if (statement.executeUpdate() > 0)
							{
								System.out.println("Account '" + username + "' deleted successfully.");
							}
							else
							{
								System.out.println("Failed to delete the account. Please try again.");
							}
						}
					}
					catch (Exception var17)
					{
						System.out.println("Error deleting account: " + var17.getMessage());
						var17.printStackTrace();
					}
				}
			}
		}
		else
		{
			System.out.println("No input provided.");
		}
	}

	protected void updateAccountCmd()
	{
		try
		{
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
			{
				System.out.print("Enter the username of the account to update: ");
				String username = reader.readLine().trim();
				if (username.isEmpty())
				{
					System.out.println("Username cannot be empty!");
					return;
				}

				try (Connection con = DatabaseFactory.getConnection())
				{
					if (con == null)
					{
						System.out.println("Database connection failed!");
						return;
					}

					try (PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM accounts WHERE login = ?"))
					{
						statement.setString(1, username);

						try (ResultSet result = statement.executeQuery())
						{
							if (result.next() && result.getInt(1) == 0)
							{
								System.out.println("Account with username '" + username + "' does not exist.");
								return;
							}
						}
					}

					System.out.print("Enter new password (or leave empty to keep current password): ");
					String newPassword = reader.readLine().trim();
					String hashBase64 = null;
					if (!newPassword.isEmpty())
					{
						MessageDigest md = MessageDigest.getInstance("SHA");
						byte[] raw = newPassword.getBytes(StandardCharsets.UTF_8);
						hashBase64 = Base64.getEncoder().encodeToString(md.digest(raw));
					}

					Integer newAccessLevel = null;

					while (true)
					{
						System.out.println("Select new access level (leave empty to keep current level):");
						System.out.println("0 - User");
						System.out.println("-1 - Banned");
						System.out.println("10 - Chat Moderator");
						System.out.println("20 - Test GM");
						System.out.println("30 - General GM");
						System.out.println("40 - Support GM");
						System.out.println("50 - Event GM");
						System.out.println("60 - Head GM");
						System.out.println("70 - Admin");
						System.out.println("100 - Master");
						System.out.print("Enter new access level: ");
						String accessLevelInput = reader.readLine().trim();
						if (accessLevelInput.isEmpty())
						{
							break;
						}

						try
						{
							newAccessLevel = Integer.parseInt(accessLevelInput);
							break;
						}
						catch (NumberFormatException var18)
						{
							System.out.println("Invalid access level. Please enter a valid number.");
						}
					}

					if (hashBase64 != null || newAccessLevel != null)
					{
						StringBuilder sqlBuilder = new StringBuilder("UPDATE accounts SET ");
						List<Object> params = new ArrayList<>();
						if (hashBase64 != null)
						{
							sqlBuilder.append("password = ?, ");
							params.add(hashBase64);
						}

						if (newAccessLevel != null)
						{
							sqlBuilder.append("accessLevel = ?, ");
							params.add(newAccessLevel);
						}

						sqlBuilder.setLength(sqlBuilder.length() - 2);
						sqlBuilder.append(" WHERE login = ?");
						params.add(username);

						try (PreparedStatement statement = con.prepareStatement(sqlBuilder.toString()))
						{
							for (int i = 0; i < params.size(); i++)
							{
								statement.setObject(i + 1, params.get(i));
							}

							int rowsUpdated = statement.executeUpdate();
							if (rowsUpdated > 0)
							{
								System.out.println("Account '" + username + "' updated successfully!");
							}
							else
							{
								System.out.println("Failed to update the account. Please try again.");
							}

							return;
						}
					}

					System.out.println("No changes made to the '" + username + "' account.");
				}
			}
		}
		catch (Exception var23)
		{
			System.out.println("Error updating account: " + var23.getMessage());
			var23.printStackTrace();
		}
	}

	private void listAccountsCmd()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			if (con != null)
			{
				try (PreparedStatement statement = con.prepareStatement("SELECT login, accessLevel FROM accounts"); ResultSet result = statement.executeQuery();)
				{
					if (!result.isBeforeFirst())
					{
						System.out.println("No accounts found.");
						return;
					}
					System.out.println("================================================");
					System.out.printf("| %-4s | %-22s | %-12s |" + System.lineSeparator(), "No.", "Username", "Access Level");
					System.out.println("================================================");
					int userNumber = 1;

					while (result.next())
					{
						String username = result.getString("login");
						int accessLevel = result.getInt("accessLevel");
						System.out.printf("| %-4d | %-22s | %-12d |" + System.lineSeparator(), userNumber++, username, accessLevel);
						System.out.println("------------------------------------------------");
					}

					int totalAccounts = this.getTotalAccountsCount();
					System.out.println(System.lineSeparator() + "Total Accounts: " + (totalAccounts >= 0 ? totalAccounts : "Error retrieving count"));
				}
				catch (Exception var12)
				{
					System.out.println("Error retrieving accounts: " + var12.getMessage());
					var12.printStackTrace();
				}
			}
		}
		catch (Exception var14)
		{
			System.out.println("Error with database connection: " + var14.getMessage());
			var14.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		InterfaceConfig.load();
		DatabaseFactory.init();
		SwingUtilities.invokeLater(AccountManager::new);
	}
}
