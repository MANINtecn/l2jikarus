package net.sf.l2jdev.gameserver.model;

import net.sf.l2jdev.gameserver.data.xml.AdminData;

public class AdminCommandAccessRight
{
	private final String _command;
	private final String _description;
	private final int _level;
	private final boolean _confirmDlg;

	public AdminCommandAccessRight(StatSet set)
	{
		this._command = "admin_" + set.getString("command");
		this._description = set.getString("description", "");
		this._confirmDlg = set.getBoolean("confirmDlg", false);
		this._level = set.getInt("accessLevel", 100);
	}

	public AdminCommandAccessRight(String command, String description, boolean confirm, int level)
	{
		this._command = command;
		this._description = description;
		this._confirmDlg = confirm;
		this._level = level;
	}

	public String getCommand()
	{
		return this._command;
	}

	public String getDescription()
	{
		return this._description;
	}

	public boolean requireConfirm()
	{
		return this._confirmDlg;
	}

	public boolean hasAccess(AccessLevel playerAccessLevel)
	{
		AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(this._level);
		if (accessLevel == null)
		{
			return false;
		}
		return accessLevel.getLevel() == playerAccessLevel.getLevel() ? true : playerAccessLevel.hasChildAccess(accessLevel);
	}
}
