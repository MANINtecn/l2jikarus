package net.sf.l2jdev.gameserver.network.clientpackets.crossevent;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.managers.events.CrossEventManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventAdvancedRewardHolder;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventRareReward;

public class RequestCrossEventRareReward extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			CrossEventAdvancedRewardHolder item = this.getRareReward();
			if (item != null)
			{
				ItemHolder reward = new ItemHolder(item.getItemId(), item.getCount());
				player.sendPacket(new ExCrossEventRareReward(true, reward.getId()));
				player.setCrossAdvancedRewardCount(-1);
				player.addItem(ItemProcessType.REWARD, reward, player, true);
				player.sendPacket(new ExCrossEventInfo(player));
			}
		}
	}

	private CrossEventAdvancedRewardHolder getRareReward()
	{
		List<CrossEventAdvancedRewardHolder> tempList = new ArrayList<>();

		for (CrossEventAdvancedRewardHolder reward : CrossEventManager.getInstance().getAdvancedRewardList())
		{
			if (Rnd.get(100000) <= reward.getChance())
			{
				tempList.add(reward);
			}
		}

		return tempList.isEmpty() ? this.getRareReward() : tempList.get(Rnd.get(0, tempList.size() - 1));
	}
}
