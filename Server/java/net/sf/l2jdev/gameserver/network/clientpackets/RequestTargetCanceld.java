package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.TargetUnselected;

public class RequestTargetCanceld extends ClientPacket
{
	private boolean _targetLost;

	@Override
	protected void readImpl()
	{
		this._targetLost = this.readShort() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isLockedTarget())
			{
				player.sendPacket(SystemMessageId.FAILED_TO_REMOVE_ENMITY);
			}
			else
			{
				if (player.getQueuedSkill() != null)
				{
					player.setQueuedSkill(null, null, false, false);
				}

				if (player.isCastingNow())
				{
					player.abortAllSkillCasters();
				}

				if (this._targetLost)
				{
					player.setTarget(null);
				}

				if (player.isInAirShip())
				{
					player.broadcastPacket(new TargetUnselected(player));
				}
			}
		}
	}
}
