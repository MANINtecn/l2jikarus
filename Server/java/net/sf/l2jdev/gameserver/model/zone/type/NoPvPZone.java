package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;

public class NoPvPZone extends ZoneType
{
	public NoPvPZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			if (player.getSiegeState() != 0 && GeneralConfig.PEACE_ZONE_MODE == 1)
			{
				return;
			}
		}

		if (GeneralConfig.PEACE_ZONE_MODE != 2)
		{
			creature.setInsideZone(ZoneId.NO_PVP, true);
		}

		if (creature.isPlayer())
		{
			creature.broadcastInfo();
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (GeneralConfig.PEACE_ZONE_MODE != 2)
		{
			creature.setInsideZone(ZoneId.NO_PVP, false);
		}

		if (creature.isPlayer() && !creature.isTeleporting())
		{
			creature.broadcastInfo();
		}
	}

	@Override
	public void setEnabled(boolean value)
	{
		super.setEnabled(value);
		if (value)
		{
			for (Player player : World.getInstance().getPlayers())
			{
				if (player != null && this.isInsideZone(player))
				{
					this.revalidateInZone(player);
					if (player.getPet() != null)
					{
						this.revalidateInZone(player.getPet());
					}

					for (Summon summon : player.getServitors().values())
					{
						this.revalidateInZone(summon);
					}
				}
			}
		}
		else
		{
			for (Creature creature : this.getCharactersInside())
			{
				if (creature != null)
				{
					this.removeCharacter(creature);
				}
			}
		}
	}
}
