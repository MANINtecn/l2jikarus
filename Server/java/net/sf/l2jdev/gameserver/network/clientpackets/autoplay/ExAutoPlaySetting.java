package net.sf.l2jdev.gameserver.network.clientpackets.autoplay;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.autoplay.ExAutoPlaySettingSend;
import net.sf.l2jdev.gameserver.taskmanagers.AutoPlayTaskManager;

public class ExAutoPlaySetting extends ClientPacket
{
	private int _options;
	private boolean _active;
	private boolean _pickUp;
	private int _nextTargetMode;
	private boolean _shortRange;
	private int _potionPercent;
	private int _petPotionPercent;
	private boolean _respectfulHunting;
	private int _macroIndex;

	@Override
	protected void readImpl()
	{
		this._options = this.readShort();
		this._active = this.readByte() == 1;
		this._pickUp = this.readByte() == 1;
		this._nextTargetMode = this.readShort();
		this._shortRange = this.readByte() == 1;
		this._potionPercent = this.readInt();
		this._petPotionPercent = this.readInt();
		this._respectfulHunting = this.readByte() == 1;
		this._macroIndex = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.hasResumedAutoPlay())
			{
				player.setResumedAutoPlay(false);
			}
			else
			{
				player.sendPacket(new ExAutoPlaySettingSend(this._options, this._active, this._pickUp, this._nextTargetMode, this._shortRange, this._potionPercent, this._respectfulHunting, this._petPotionPercent));
				player.getAutoPlaySettings().setAutoPotionPercent(this._potionPercent);
				if (GeneralConfig.ENABLE_AUTO_PLAY)
				{
					List<Integer> settings = new ArrayList<>(8);
					settings.add(0, this._options);
					settings.add(1, this._active ? 1 : 0);
					settings.add(2, this._pickUp ? 1 : 0);
					settings.add(3, this._nextTargetMode);
					settings.add(4, this._shortRange ? 1 : 0);
					settings.add(5, this._potionPercent);
					settings.add(6, this._respectfulHunting ? 1 : 0);
					settings.add(7, this._petPotionPercent);
					settings.add(8, this._macroIndex);
					player.getVariables().setIntegerList("AUTO_USE_SETTINGS", settings);
					player.getAutoPlaySettings().setOptions(this._options);
					player.getAutoPlaySettings().setPickup(this._pickUp);
					player.getAutoPlaySettings().setNextTargetMode(this._nextTargetMode);
					player.getAutoPlaySettings().setShortRange(this._shortRange);
					player.getAutoPlaySettings().setRespectfulHunting(this._respectfulHunting);
					player.getAutoPlaySettings().setAutoPetPotionPercent(this._petPotionPercent);
					if (this._active)
					{
						AutoPlayTaskManager.getInstance().startAutoPlay(player);
					}
					else
					{
						AutoPlayTaskManager.getInstance().stopAutoPlay(player);
					}
				}
			}
		}
	}
}
