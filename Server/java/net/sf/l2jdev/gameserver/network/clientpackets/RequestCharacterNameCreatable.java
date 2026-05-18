package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.network.serverpackets.ExIsCharNameCreatable;

public class RequestCharacterNameCreatable extends ClientPacket
{
	private String _name;
	public static final int CHARACTER_CREATE_FAILED = 1;
	public static final int NAME_ALREADY_EXISTS = 2;
	public static final int INVALID_LENGTH = 3;
	public static final int INVALID_NAME = 4;
	public static final int CANNOT_CREATE_SERVER = 5;

	@Override
	protected void readImpl()
	{
		this._name = this.readString();
	}

	@Override
	protected void runImpl()
	{
		int charId = CharInfoTable.getInstance().getIdByName(this._name);
		int result;
		if (!StringUtil.isAlphaNumeric(this._name) || !this.isValidName(this._name))
		{
			result = 4;
		}
		else if (charId > 0)
		{
			result = 2;
		}
		else if (FakePlayerData.getInstance().getProperName(this._name) != null)
		{
			result = 2;
		}
		else if (this._name.length() > 16)
		{
			result = 3;
		}
		else
		{
			result = -1;
		}

		this.getClient().sendPacket(new ExIsCharNameCreatable(result));
	}

	protected boolean isValidName(String text)
	{
		return ServerConfig.CHARNAME_TEMPLATE_PATTERN.matcher(text).matches();
	}
}
