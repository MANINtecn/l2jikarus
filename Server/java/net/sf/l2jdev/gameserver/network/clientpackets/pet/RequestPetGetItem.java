package net.sf.l2jdev.gameserver.network.clientpackets.pet;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.managers.SiegeGuardManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestPetGetItem extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		World world = World.getInstance();
		Item item = (Item) world.findObject(this._objectId);
		Player player = this.getPlayer();
		if (item != null && player != null && player.hasPet())
		{
			Castle castle = CastleManager.getInstance().getCastle(item);
			if (castle != null && SiegeGuardManager.getInstance().getSiegeGuardByItem(castle.getResidenceId(), item.getId()) != null)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (FortSiegeManager.getInstance().isCombat(item.getId()))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				Pet pet = player.getPet();
				if (pet.isDead() || pet.isControlBlocked())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (pet.isUncontrollable())
				{
					player.sendPacket(SystemMessageId.WHEN_YOUR_GUARDIAN_S_SATIETY_REACHES_0_YOU_CANNOT_CONTROL_IT);
				}
				else
				{
					pet.getAI().setIntention(Intention.PICK_UP, item);
				}
			}
		}
		else
		{
			this.getClient().sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
