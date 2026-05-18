package net.sf.l2jdev.gameserver.network.serverpackets.olympiad;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.data.xml.ClassListData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.olympiad.Participant;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadUserInfo extends ServerPacket
{
	private final Player _player;
	private Participant _par = null;
	private final int _curHp;
	private final int _maxHp;
	private final int _curCp;
	private final int _maxCp;

	public ExOlympiadUserInfo(Player player)
	{
		this._player = player;
		if (this._player != null)
		{
			this._curHp = (int) this._player.getCurrentHp();
			this._maxHp = (int) this._player.getMaxHp();
			this._curCp = (int) this._player.getCurrentCp();
			this._maxCp = this._player.getMaxCp();
		}
		else
		{
			this._curHp = 0;
			this._maxHp = 100;
			this._curCp = 0;
			this._maxCp = 100;
		}
	}

	public ExOlympiadUserInfo(Participant par)
	{
		this._par = par;
		this._player = par.getPlayer();
		if (this._player != null)
		{
			this._curHp = (int) this._player.getCurrentHp();
			this._maxHp = (int) this._player.getMaxHp();
			this._curCp = (int) this._player.getCurrentCp();
			this._maxCp = this._player.getMaxCp();
		}
		else
		{
			this._curHp = 0;
			this._maxHp = 100;
			this._curCp = 0;
			this._maxCp = 100;
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_USER_INFO.writeId(this, buffer);
		if (this._player != null)
		{
			buffer.writeByte(this._player.getOlympiadSide());
			buffer.writeInt(this._player.getObjectId());
			if (OlympiadConfig.OLYMPIAD_HIDE_NAMES)
			{
				buffer.writeString(ClassListData.getInstance().getClass(this._player.getPlayerClass()).getClassName());
			}
			else
			{
				buffer.writeString(this._player.getName());
			}

			buffer.writeInt(this._player.getPlayerClass().getId());
		}
		else
		{
			buffer.writeByte(this._par.getSide());
			buffer.writeInt(this._par.getObjectId());
			buffer.writeString(this._par.getName());
			buffer.writeInt(this._par.getBaseClass());
		}

		buffer.writeInt(this._curHp);
		buffer.writeInt(this._maxHp);
		buffer.writeInt(this._curCp);
		buffer.writeInt(this._maxCp);
	}
}
