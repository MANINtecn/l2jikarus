package net.sf.l2jdev.gameserver.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.LoginServerThread;
import net.sf.l2jdev.gameserver.data.xml.SecondaryAuthData;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.serverpackets.Ex2ndPasswordAck;
import net.sf.l2jdev.gameserver.network.serverpackets.Ex2ndPasswordCheck;
import net.sf.l2jdev.gameserver.network.serverpackets.Ex2ndPasswordVerify;
import net.sf.l2jdev.gameserver.network.serverpackets.LeaveWorld;

public class SecondaryPasswordAuth
{
	private static final Logger LOGGER = Logger.getLogger(SecondaryPasswordAuth.class.getName());
	private final GameClient _activeClient;
	private String _password;
	private int _wrongAttempts;
	private boolean _authed;
 
	public SecondaryPasswordAuth(GameClient activeClient)
	{
		this._activeClient = activeClient;
		this._password = null;
		this._wrongAttempts = 0;
		this._authed = false;
		this.loadPassword();
	}

	private void loadPassword()
	{
		String var = null;
		String value = null;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT var, value FROM account_gsdata WHERE account_name=? AND var LIKE 'secauth_%'");)
		{
			statement.setString(1, this._activeClient.getAccountName());

			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					var = rs.getString("var");
					value = rs.getString("value");
					if (var.equals("secauth_pwd"))
					{
						this._password = value;
					}
					else if (var.equals("secauth_wte"))
					{
						this._wrongAttempts = Integer.parseInt(value);
					}
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.SEVERE, "Error while reading password.", var14);
		}
	}

	public boolean savePassword(String value)
	{
		if (this.passwordExist())
		{
			LOGGER.warning("[SecondaryPasswordAuth]" + this._activeClient.getAccountName() + " forced savePassword");
			Disconnection.of(this._activeClient).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
			return false;
		}
		else if (!this.validatePassword(value))
		{
			this._activeClient.sendPacket(new Ex2ndPasswordAck(0, 1));
			return false;
		}
		else
		{
			String password = this.cryptPassword(value);

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO account_gsdata VALUES (?, ?, ?)");)
			{
				statement.setString(1, this._activeClient.getAccountName());
				statement.setString(2, "secauth_pwd");
				statement.setString(3, password);
				statement.execute();
			}
			catch (Exception var11)
			{
				LOGGER.log(Level.SEVERE, "Error while writing password.", var11);
				return false;
			}

			this._password = password;
			return true;
		}
	}

	public boolean insertWrongAttempt(int attempts)
	{
		try
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO account_gsdata VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?");)
			{
				statement.setString(1, this._activeClient.getAccountName());
				statement.setString(2, "secauth_wte");
				statement.setString(3, Integer.toString(attempts));
				statement.setString(4, Integer.toString(attempts));
				statement.execute();
			}

			return true;
		}
		catch (Exception var10)
		{
			LOGGER.log(Level.SEVERE, "Error while writing wrong attempts.", var10);
			return false;
		}
	}

	public boolean changePassword(String oldPassword, String newPassword)
	{
		if (!this.passwordExist())
		{
			LOGGER.warning("[SecondaryPasswordAuth]" + this._activeClient.getAccountName() + " forced changePassword");
			Disconnection.of(this._activeClient).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
			return false;
		}
		else if (!this.checkPassword(oldPassword, true))
		{
			return false;
		}
		else if (!this.validatePassword(newPassword))
		{
			this._activeClient.sendPacket(new Ex2ndPasswordAck(2, 1));
			return false;
		}
		else
		{
			String password = this.cryptPassword(newPassword);

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE account_gsdata SET value=? WHERE account_name=? AND var=?");)
			{
				statement.setString(1, password);
				statement.setString(2, this._activeClient.getAccountName());
				statement.setString(3, "secauth_pwd");
				statement.execute();
			}
			catch (Exception var12)
			{
				LOGGER.log(Level.SEVERE, "Error while reading password.", var12);
				return false;
			}

			this._password = password;
			this._authed = false;
			return true;
		}
	}

	public boolean checkPassword(String value, boolean skipAuth)
	{
		String password = this.cryptPassword(value);
		if (!password.equals(this._password))
		{
			this._wrongAttempts++;
			if (this._wrongAttempts < SecondaryAuthData.getInstance().getMaxAttempts())
			{
				this._activeClient.sendPacket(new Ex2ndPasswordVerify(1, this._wrongAttempts));
				this.insertWrongAttempt(this._wrongAttempts);
			}
			else
			{
				LoginServerThread.getInstance().sendTempBan(this._activeClient.getAccountName(), this._activeClient.getIp(), SecondaryAuthData.getInstance().getBanTime());
				LoginServerThread.getInstance().sendMail(this._activeClient.getAccountName(), "SATempBan", this._activeClient.getIp(), Integer.toString(SecondaryAuthData.getInstance().getMaxAttempts()), Long.toString(SecondaryAuthData.getInstance().getBanTime()), SecondaryAuthData.getInstance().getRecoveryLink());
				LOGGER.warning(this._activeClient.getAccountName() + " - (" + this._activeClient.getIp() + ") has inputted the wrong password " + this._wrongAttempts + " times in row.");
				this.insertWrongAttempt(0);
				this._activeClient.close(new Ex2ndPasswordVerify(2, SecondaryAuthData.getInstance().getMaxAttempts()));
			}

			return false;
		}
		if (!skipAuth)
		{
			this._authed = true;
			this._activeClient.sendPacket(new Ex2ndPasswordVerify(0, this._wrongAttempts));
		}

		this.insertWrongAttempt(0);
		return true;
	}

	public boolean passwordExist()
	{
		return this._password != null;
	}

	public void openDialog()
	{
		if (this.passwordExist())
		{
			this._activeClient.sendPacket(new Ex2ndPasswordCheck(1));
		}
		else
		{
			this._activeClient.sendPacket(new Ex2ndPasswordCheck(0));
		}
	}

	public boolean isAuthed()
	{
		return this._authed;
	}

	protected String cryptPassword(String password)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes(StandardCharsets.UTF_8);
			byte[] hash = md.digest(raw);
			return Base64.getEncoder().encodeToString(hash);
		}
		catch (NoSuchAlgorithmException var5)
		{
			LOGGER.severe("[SecondaryPasswordAuth]Unsupported Algorythm");
			return null;
		}
	}

	protected boolean validatePassword(String password)
	{
		if (!StringUtil.isNumeric(password))
		{
			return false;
		}
		return password.length() >= 6 && password.length() <= 8 ? !SecondaryAuthData.getInstance().isForbiddenPassword(password) : false;
	}
}
