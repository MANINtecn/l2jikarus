package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.data.xml.FakePlayerData;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestBlock extends ClientPacket
{
 
	private String _name;
	private Integer _type;

	@Override
	protected void readImpl()
	{
		this._type = this.readInt();
		if (this._type == 0 || this._type == 1)
		{
			this._name = this.readString();
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		int targetId = CharInfoTable.getInstance().getIdByName(this._name);
		int targetAL = CharInfoTable.getInstance().getAccessLevelById(targetId);
		if (player != null)
		{
			switch (this._type)
			{
				case 0:
				case 1:
					if (FakePlayerData.getInstance().isTalkable(this._name))
					{
						if (this._type == 0)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST);
							sm.addString(FakePlayerData.getInstance().getProperName(this._name));
							player.sendPacket(sm);
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST);
							sm.addString(FakePlayerData.getInstance().getProperName(this._name));
							player.sendPacket(sm);
						}

						return;
					}

					if (targetId <= 0)
					{
						player.sendPacket(SystemMessageId.ERROR_WHEN_ADDING_A_USER_TO_YOUR_IGNORE_LIST);
						return;
					}

					if (targetAL > 0)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_BAN_A_GM);
						return;
					}

					if (player.getObjectId() == targetId)
					{
						return;
					}

					if (this._type == 0)
					{
						BlockList.addToBlockList(player, targetId);
					}
					else
					{
						BlockList.removeFromBlockList(player, targetId);
					}
					break;
				case 2:
					BlockList.sendListToOwner(player);
					break;
				case 3:
					player.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
					BlockList.setBlockAll(player, true);
					break;
				case 4:
					player.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
					BlockList.setBlockAll(player, false);
					break;
				default:
					PacketLogger.info("Unknown 0xA9 block type: " + this._type);
			}
		}
	}
}
