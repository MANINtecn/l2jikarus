package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.serverpackets.GMHennaInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.GMViewCharacterInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.GMViewItemList;
import net.sf.l2jdev.gameserver.network.serverpackets.GMViewPledgeInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.GMViewSkillInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;
import net.sf.l2jdev.gameserver.network.serverpackets.GmViewQuestInfo;

public class RequestGMCommand extends ClientPacket
{
	private String _targetName;
	private int _command;

	@Override
	protected void readImpl()
	{
		this._targetName = this.readString();
		this._command = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = this.getClient();
		if (client.getPlayer().isGM() && client.getPlayer().getAccessLevel().allowAltG())
		{
			Player player = World.getInstance().getPlayer(this._targetName);
			Clan clan = ClanTable.getInstance().getClanByName(this._targetName);
			if (player != null || clan != null && this._command == 6)
			{
				switch (this._command)
				{
					case 1:
						client.sendPacket(new GMViewCharacterInfo(player));
						client.sendPacket(new GMHennaInfo(player));
						break;
					case 2:
						if (player != null && player.getClan() != null)
						{
							client.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
						}
						break;
					case 3:
						client.sendPacket(new GMViewSkillInfo(player));
						break;
					case 4:
						client.sendPacket(new GmViewQuestInfo(player));
						break;
					case 5:
						client.sendPacket(new GMViewItemList(1, player));
						client.sendPacket(new GMViewItemList(2, player));
						client.sendPacket(new GMHennaInfo(player));
						break;
					case 6:
						if (player != null)
						{
							client.sendPacket(new GMViewWarehouseWithdrawList(1, player));
							client.sendPacket(new GMViewWarehouseWithdrawList(2, player));
						}
						else
						{
							client.sendPacket(new GMViewWarehouseWithdrawList(1, clan));
							client.sendPacket(new GMViewWarehouseWithdrawList(2, clan));
						}
				}
			}
		}
	}
}
