package net.sf.l2jdev.gameserver.network.clientpackets.stats;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExSetStatusBonus extends ClientPacket
{
	private int _str;
	private int _dex;
	private int _con;
	private int _int;
	private int _wit;
	private int _men;

	@Override
	protected void readImpl()
	{
		this.readShort();
		this.readShort();
		this._str = this.readShort();
		this._dex = this.readShort();
		this._con = this.readShort();
		this._int = this.readShort();
		this._wit = this.readShort();
		this._men = this.readShort();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._str >= 0 && this._dex >= 0 && this._con >= 0 && this._int >= 0 && this._wit >= 0 && this._men >= 0)
			{
				int usedPoints = player.getVariables().getInt("STAT_POINTS", 0);
				int effectBonus = (int) player.getStat().getValue(Stat.ELIXIR_USAGE_LIMIT, 0.0);
				int elixirsAvailable = player.getVariables().getInt("ELIXIRS_AVAILABLE", 0) + effectBonus;
				int currentPoints = this._str + this._dex + this._con + this._int + this._wit + this._men;
				int possiblePoints = player.getLevel() < 76 ? 0 : player.getLevel() - 75 + elixirsAvailable - usedPoints;
				if (possiblePoints > 0 && currentPoints <= possiblePoints)
				{
					if (this._str > 0)
					{
						player.getVariables().set("STAT_STR", player.getVariables().getInt("STAT_STR", 0) + this._str);
					}

					if (this._dex > 0)
					{
						player.getVariables().set("STAT_DEX", player.getVariables().getInt("STAT_DEX", 0) + this._dex);
					}

					if (this._con > 0)
					{
						player.getVariables().set("STAT_CON", player.getVariables().getInt("STAT_CON", 0) + this._con);
					}

					if (this._int > 0)
					{
						player.getVariables().set("STAT_INT", player.getVariables().getInt("STAT_INT", 0) + this._int);
					}

					if (this._wit > 0)
					{
						player.getVariables().set("STAT_WIT", player.getVariables().getInt("STAT_WIT", 0) + this._wit);
					}

					if (this._men > 0)
					{
						player.getVariables().set("STAT_MEN", player.getVariables().getInt("STAT_MEN", 0) + this._men);
					}

					player.getStat().recalculateStats(true);
					player.calculateStatIncreaseSkills();
					player.updateUserInfo();
				}
			}
		}
	}
}
