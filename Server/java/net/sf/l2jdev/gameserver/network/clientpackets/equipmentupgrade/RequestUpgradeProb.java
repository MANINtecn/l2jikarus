package net.sf.l2jdev.gameserver.network.clientpackets.equipmentupgrade;

import net.sf.l2jdev.gameserver.data.holders.EquipmentUpgradeHolder;
import net.sf.l2jdev.gameserver.data.xml.EquipmentUpgradeData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgrade.ExUpgradeProb;

public class RequestUpgradeProb extends ClientPacket
{
	private int _upgradeId;

	@Override
	protected void readImpl()
	{
		this._upgradeId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			EquipmentUpgradeHolder holder = EquipmentUpgradeData.getInstance().getUpgrade(this._upgradeId);
			if (holder != null)
			{
				player.sendPacket(new ExUpgradeProb(this._upgradeId, 100.0));
			}
		}
	}
}
