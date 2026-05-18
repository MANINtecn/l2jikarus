package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.holders.ResurrectByPaymentHolder;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.SiegeClan;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class Die extends ServerPacket
{
	private final int _objectId;
	private final boolean _isSweepable;
	private int _flags = 1;
	private int _delayFeather = 0;
	private Player _player;

	public Die(Creature creature)
	{
		this._objectId = creature.getObjectId();
		this._isSweepable = creature.isAttackable() && creature.isSweepActive();
		if (creature.isPlayer())
		{
			this._player = creature.asPlayer();

			for (BuffInfo effect : creature.getEffectList().getEffects())
			{
				if (effect.getSkill().getId() == CommonSkill.FEATHER_OF_BLESSING.getId())
				{
					this._delayFeather = effect.getTime();
					break;
				}
			}

			if (this._player.isInTimedHuntingZone())
			{
				this._flags = -32388608;
				return;
			}

			Clan clan = this._player.getClan();
			boolean isInCastleDefense = false;
			boolean isInFortDefense = false;
			SiegeClan siegeClan = null;
			Castle castle = CastleManager.getInstance().getCastle(creature);
			Fort fort = FortManager.getInstance().getFort(creature);
			if (castle != null && castle.getSiege().isInProgress())
			{
				siegeClan = castle.getSiege().getAttackerClan(clan);
				isInCastleDefense = siegeClan == null && castle.getSiege().checkIsDefender(clan);
			}
			else if (fort != null && fort.getSiege().isInProgress())
			{
				siegeClan = fort.getSiege().getAttackerClan(clan);
				isInFortDefense = siegeClan == null && fort.getSiege().checkIsDefender(clan);
			}

			if (clan != null && clan.getHideoutId() > 0)
			{
				this._flags += 2;
			}

			if (clan != null && clan.getCastleId() > 0 || isInCastleDefense)
			{
				this._flags += 4;
			}

			if (clan != null && clan.getFortId() > 0 || isInFortDefense)
			{
				this._flags += 8;
			}

			if (siegeClan != null && !isInCastleDefense && !isInFortDefense && !siegeClan.getFlag().isEmpty())
			{
				this._flags += 16;
			}

			if (creature.getAccessLevel().allowFixedRes() || creature.getInventory().haveItemForSelfResurrection())
			{
				this._flags += 32;
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.DIE.writeId(this, buffer);
		buffer.writeInt(this._objectId);
		buffer.writeLong(this._flags);
		buffer.writeInt(this._isSweepable);
		buffer.writeInt(this._delayFeather);
		buffer.writeByte(0);
		buffer.writeInt(0);
		if (this._player != null && PlayerConfig.RESURRECT_BY_PAYMENT_ENABLED)
		{
			int resurrectTimes = this._player.getVariables().getInt("RESURRECT_BY_PAYMENT_COUNT", 0) + 1;
			int originalValue = resurrectTimes - 1;
			if (originalValue < PlayerConfig.RESURRECT_BY_PAYMENT_MAX_FREE_TIMES)
			{
				buffer.writeInt(PlayerConfig.RESURRECT_BY_PAYMENT_MAX_FREE_TIMES - originalValue);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
			}
			else
			{
				buffer.writeInt(0);
				this.getValues(buffer, this._player, originalValue);
			}
		}
		else
		{
			buffer.writeInt(1);
			buffer.writeInt(0);
			buffer.writeInt(-1);
			buffer.writeInt(0);
			buffer.writeInt(-1);
		}

		buffer.writeInt(0);
	}

	protected void getValues(WritableBuffer buffer, Player player, int originalValue)
	{
		if (PlayerConfig.RESURRECT_BY_PAYMENT_FIRST_RESURRECT_VALUES != null && PlayerConfig.RESURRECT_BY_PAYMENT_SECOND_RESURRECT_VALUES != null)
		{
			List<Integer> levelListFirst = new ArrayList<>(PlayerConfig.RESURRECT_BY_PAYMENT_FIRST_RESURRECT_VALUES.keySet());
			List<Integer> levelListSecond = new ArrayList<>(PlayerConfig.RESURRECT_BY_PAYMENT_SECOND_RESURRECT_VALUES.keySet());

			for (int level : levelListSecond)
			{
				if (PlayerConfig.RESURRECT_BY_PAYMENT_SECOND_RESURRECT_VALUES.isEmpty())
				{
					buffer.writeInt(0);
					buffer.writeInt(-1);
					break;
				}

				if (player.getLevel() < level || levelListSecond.lastIndexOf(level) == levelListSecond.size() - 1)
				{
					int maxResTime;
					try
					{
						maxResTime = PlayerConfig.RESURRECT_BY_PAYMENT_SECOND_RESURRECT_VALUES.get(level).keySet().stream().max(Integer::compareTo).get();
					}
					catch (Exception var12)
					{
						buffer.writeInt(0);
						buffer.writeInt(-1);
						return;
					}

					int getValue = maxResTime <= originalValue ? maxResTime : originalValue + 1;
					ResurrectByPaymentHolder rbph = PlayerConfig.RESURRECT_BY_PAYMENT_SECOND_RESURRECT_VALUES.get(level).get(getValue);
					if (rbph != null)
					{
						buffer.writeInt((int) (rbph.getAmount() * player.getStat().getValue(Stat.RESURRECTION_FEE_MODIFIER, 1.0)));
						buffer.writeInt(Math.toIntExact(Math.round(rbph.getResurrectPercent())));
					}
					else
					{
						buffer.writeInt(0);
						buffer.writeInt(-1);
					}
					break;
				}
			}

			for (int level : levelListFirst)
			{
				if (PlayerConfig.RESURRECT_BY_PAYMENT_FIRST_RESURRECT_VALUES.isEmpty())
				{
					buffer.writeInt(0);
					buffer.writeInt(-1);
					break;
				}

				if (player.getLevel() < level || levelListFirst.lastIndexOf(level) == levelListFirst.size() - 1)
				{
					int maxResTimex;
					try
					{
						maxResTimex = PlayerConfig.RESURRECT_BY_PAYMENT_FIRST_RESURRECT_VALUES.get(level).keySet().stream().max(Integer::compareTo).get();
					}
					catch (Exception var11)
					{
						buffer.writeInt(0);
						buffer.writeInt(-1);
						return;
					}

					int getValue = maxResTimex <= originalValue ? maxResTimex : originalValue + 1;
					ResurrectByPaymentHolder rbph = PlayerConfig.RESURRECT_BY_PAYMENT_FIRST_RESURRECT_VALUES.get(level).get(getValue);
					if (rbph != null)
					{
						buffer.writeInt((int) (rbph.getAmount() * player.getStat().getValue(Stat.RESURRECTION_FEE_MODIFIER, 1.0)));
						buffer.writeInt(Math.toIntExact(Math.round(rbph.getResurrectPercent())));
					}
					else
					{
						buffer.writeInt(0);
						buffer.writeInt(-1);
					}
					break;
				}
			}
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(-1);
			buffer.writeInt(0);
			buffer.writeInt(-1);
		}
	}
}
