package net.sf.l2jdev.gameserver.network.clientpackets.equipmentupgrade;

import net.sf.l2jdev.gameserver.data.holders.EquipmentUpgradeNormalHolder;
import net.sf.l2jdev.gameserver.data.xml.EquipmentUpgradeNormalData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgrade.ExUpgradeSystemProbList;

public class RequestUpgradeSystemProbList extends ClientPacket
{
	private int _type;
	private int _upgradeId;

	@Override
	protected void readImpl()
	{
		this._type = this.readInt();
		this._upgradeId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			EquipmentUpgradeNormalHolder holder = EquipmentUpgradeNormalData.getInstance().getUpgrade(this._upgradeId);
			if (holder != null)
			{
				player.sendPacket(new ExUpgradeSystemProbList(this._type, this._upgradeId, holder.getChance(), holder.getChanceToReceiveBonusItems()));
			}
		}
	}
}
