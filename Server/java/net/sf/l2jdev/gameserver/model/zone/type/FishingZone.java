package net.sf.l2jdev.gameserver.model.zone.type;

import java.lang.ref.WeakReference;
import java.util.Objects;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.fishing.Fishing;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.serverpackets.fishing.ExAutoFishAvailable;

public class FishingZone extends ZoneType
{
	public FishingZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer())
		{
			if ((GeneralConfig.ALLOW_FISHING || creature.isGM()) && !creature.isInsideZone(ZoneId.FISHING))
			{
				final WeakReference<Player> weakPlayer = new WeakReference<>(creature.asPlayer());
				ThreadPool.execute(new Runnable()
				{
					{
						Objects.requireNonNull(FishingZone.this);
					}

					@Override
					public void run()
					{
						Player player = weakPlayer.get();
						if (player != null)
						{
							Fishing fishing = player.getFishing();
							if (player.isInsideZone(ZoneId.FISHING))
							{
								if (fishing.canFish() && !fishing.isFishing())
								{
									if (fishing.isAtValidLocation())
									{
										player.sendPacket(ExAutoFishAvailable.YES);
									}
									else
									{
										player.sendPacket(ExAutoFishAvailable.NO);
									}
								}

								ThreadPool.schedule(this, 1500L);
							}
							else
							{
								player.sendPacket(ExAutoFishAvailable.NO);
							}
						}
					}
				});
			}

			creature.setInsideZone(ZoneId.FISHING, true);
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.FISHING, false);
			creature.sendPacket(ExAutoFishAvailable.NO);
		}
	}

	public int getWaterZ()
	{
		return this.getZone().getHighZ();
	}
}
