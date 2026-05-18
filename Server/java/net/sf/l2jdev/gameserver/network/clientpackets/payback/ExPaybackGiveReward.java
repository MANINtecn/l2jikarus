package net.sf.l2jdev.gameserver.network.clientpackets.payback;

import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.managers.events.PaybackManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.payback.PaybackGiveReward;

public class ExPaybackGiveReward extends ClientPacket
{
	private int _eventId;
	private int _index;

	@Override
	protected void readImpl()
	{
		this._eventId = this.readByte();
		this._index = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			PaybackManager manager = PaybackManager.getInstance();
			long consumed = manager.getPlayerConsumedProgress(player.getObjectId());
			List<Integer> rewardStatus = manager.getPlayerMissionProgress(player.getObjectId());
			if (rewardStatus != null && !rewardStatus.get(this._index - 1).equals(1) && manager.getRewardsById(this._index) != null)
			{
				long count = manager.getRewardsById(this._index).getCount();
				if (count > consumed)
				{
					player.sendPacket(new PaybackGiveReward(false, this._eventId, this._index));
				}
				else
				{
					for (ItemChanceHolder item : manager.getRewardsById(this._index).getRewards())
					{
						if (item.getId() == -1)
						{
							player.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						}
						else if (Rnd.get(100) <= item.getChance())
						{
							player.addItem(ItemProcessType.REWARD, item.getId(), item.getCount(), item.getEnchantmentLevel(), player, true);
						}
					}

					manager.changeMissionProgress(player.getObjectId(), this._index - 1, 1);
					manager.storePlayerProgress(player);
					player.sendPacket(new PaybackGiveReward(true, this._eventId, this._index));
				}
			}
			else
			{
				player.sendPacket(new PaybackGiveReward(false, this._eventId, this._index));
			}
		}
	}
}
