package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.appearance.PlayerAppearance;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.NpcInfoType;

public class SummonInfo extends AbstractMaskPacket<NpcInfoType>
{
	private final Summon _summon;
	private final Player _attacker;
	private final long _relation;
	private final int _value;
	private final byte[] _masks = new byte[]
	{
		0,
		12,
		12,
		0,
		0
	};
	private int _initSize = 0;
	private int _blockSize = 0;
	private int _clanCrest = 0;
	private int _clanLargeCrest = 0;
	private int _allyCrest = 0;
	private int _allyId = 0;
	private int _clanId = 0;
	private int _statusMask = 0;
	private final String _title;
	private final Set<AbnormalVisualEffect> _abnormalVisualEffects;

	public SummonInfo(Summon summon, Player attacker, int value)
	{
		this._summon = summon;
		this._attacker = attacker;
		Player owner = summon.getOwner();
		this._relation = attacker != null && owner != null ? owner.getRelation(attacker) : 0L;
		this._title = owner != null && owner.isOnline() ? owner.getName() : "";
		this._value = value;
		this._abnormalVisualEffects = summon.getEffectList().getCurrentAbnormalVisualEffects();
		if (summon.getTemplate().getDisplayId() != summon.getTemplate().getId())
		{
			this._masks[2] = (byte) (this._masks[2] | 16);
			this.addComponentType(NpcInfoType.NAME);
		}

		this.addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.RELATIONS, NpcInfoType.TITLE, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING, NpcInfoType.PVP_FLAG, NpcInfoType.SHOW_NAME);
		if (summon.getHeading() > 0)
		{
			this.addComponentType(NpcInfoType.HEADING);
		}

		if (summon.getStat().getPAtkSpd() > 0 || summon.getStat().getMAtkSpd() > 0)
		{
			this.addComponentType(NpcInfoType.ATK_CAST_SPEED);
		}

		if (summon.getRunSpeed() > 0.0)
		{
			this.addComponentType(NpcInfoType.SPEED_MULTIPLIER);
		}

		if (summon.getWeapon() > 0 || summon.getArmor() > 0)
		{
			this.addComponentType(NpcInfoType.EQUIPPED);
		}

		if (summon.getTeam() != Team.NONE)
		{
			if (GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null)
			{
				this.addComponentType(NpcInfoType.ABNORMALS);
			}
			else
			{
				this.addComponentType(NpcInfoType.TEAM);
			}
		}

		if (summon.isInsideZone(ZoneId.WATER) || summon.isFlying())
		{
			this.addComponentType(NpcInfoType.SWIM_OR_FLY);
		}

		if (summon.isFlying())
		{
			this.addComponentType(NpcInfoType.FLYING);
		}

		if (summon.getMaxHp() > 0L)
		{
			this.addComponentType(NpcInfoType.MAX_HP);
		}

		if (summon.getMaxMp() > 0)
		{
			this.addComponentType(NpcInfoType.MAX_MP);
		}

		if (summon.getCurrentHp() <= summon.getMaxHp())
		{
			this.addComponentType(NpcInfoType.CURRENT_HP);
		}

		if (summon.getCurrentMp() <= summon.getMaxMp())
		{
			this.addComponentType(NpcInfoType.CURRENT_MP);
		}

		if (!this._abnormalVisualEffects.isEmpty())
		{
			this.addComponentType(NpcInfoType.ABNORMALS);
		}

		if (summon.getTemplate().getWeaponEnchant() > 0)
		{
			this.addComponentType(NpcInfoType.ENCHANT);
		}

		if (summon.getTransformationDisplayId() > 0)
		{
			this.addComponentType(NpcInfoType.TRANSFORMATION);
		}

		if (summon.isShowSummonAnimation())
		{
			this.addComponentType(NpcInfoType.SUMMONED);
		}

		if (summon.getReputation() != 0)
		{
			this.addComponentType(NpcInfoType.REPUTATION);
		}

		if (owner != null && owner.getClan() != null)
		{
			PlayerAppearance appearance = owner.getAppearance();
			this._clanId = appearance.getVisibleClanId();
			this._clanCrest = appearance.getVisibleClanCrestId();
			this._clanLargeCrest = appearance.getVisibleClanLargeCrestId();
			this._allyCrest = appearance.getVisibleAllyId();
			this._allyId = appearance.getVisibleAllyCrestId();
			this.addComponentType(NpcInfoType.CLAN);
		}

		this.addComponentType(NpcInfoType.PET_EVOLUTION_ID);
		if (summon.isInCombat())
		{
			this._statusMask |= 1;
		}

		if (summon.isDead())
		{
			this._statusMask |= 2;
		}

		if (summon.isTargetable())
		{
			this._statusMask |= 4;
		}

		this._statusMask |= 8;
		if (this._statusMask != 0)
		{
			this.addComponentType(NpcInfoType.VISUAL_STATE);
		}
	}

	@Override
	protected byte[] getMasks()
	{
		return this._masks;
	}

	@Override
	protected void onNewMaskAdded(NpcInfoType component)
	{
		this.calcBlockSize(this._summon, component);
	}

	private void calcBlockSize(Summon summon, NpcInfoType type)
	{
		switch (type)
		{
			case ATTACKABLE:
			case RELATIONS:
				this._initSize = this._initSize + type.getBlockLength();
				break;
			case TITLE:
				this._initSize = this._initSize + type.getBlockLength() + this._title.length() * 2;
				break;
			case NAME:
				this._blockSize = this._blockSize + type.getBlockLength() + summon.getName().length() * 2;
				break;
			default:
				this._blockSize = this._blockSize + type.getBlockLength();
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SUMMON_INFO.writeId(this, buffer);
		buffer.writeInt(this._summon.getObjectId());
		buffer.writeByte(this._value);
		buffer.writeShort(40);
		buffer.writeBytes(this._masks);
		buffer.writeByte(this._initSize);
		if (this.containsMask(NpcInfoType.ATTACKABLE))
		{
			buffer.writeByte(this._summon.isAutoAttackable(this._attacker));
		}

		if (this.containsMask(NpcInfoType.RELATIONS))
		{
			buffer.writeLong(this._relation);
		}

		if (this.containsMask(NpcInfoType.TITLE))
		{
			buffer.writeString(this._title);
		}

		buffer.writeShort(this._blockSize);
		if (this.containsMask(NpcInfoType.ID))
		{
			buffer.writeInt(this._summon.getTemplate().getDisplayId() + 1000000);
		}

		if (this.containsMask(NpcInfoType.POSITION))
		{
			buffer.writeInt(this._summon.getX());
			buffer.writeInt(this._summon.getY());
			buffer.writeInt(this._summon.getZ());
		}

		if (this.containsMask(NpcInfoType.HEADING))
		{
			buffer.writeInt(this._summon.getHeading());
		}

		if (this.containsMask(NpcInfoType.VEHICLE_ID))
		{
			buffer.writeInt(0);
		}

		if (this.containsMask(NpcInfoType.ATK_CAST_SPEED))
		{
			buffer.writeInt(this._summon.getPAtkSpd());
			buffer.writeInt(this._summon.getMAtkSpd());
		}

		if (this.containsMask(NpcInfoType.SPEED_MULTIPLIER))
		{
			buffer.writeFloat((float) this._summon.getMovementSpeedMultiplier());
			buffer.writeFloat((float) this._summon.getAttackSpeedMultiplier());
		}

		if (this.containsMask(NpcInfoType.EQUIPPED))
		{
			buffer.writeInt(this._summon.getWeapon());
			buffer.writeInt(this._summon.getArmor());
			buffer.writeInt(0);
		}

		if (this.containsMask(NpcInfoType.ALIVE))
		{
			buffer.writeByte(!this._summon.isDead());
		}

		if (this.containsMask(NpcInfoType.RUNNING))
		{
			buffer.writeByte(this._summon.isRunning());
		}

		if (this.containsMask(NpcInfoType.SWIM_OR_FLY))
		{
			buffer.writeByte(this._summon.isInsideZone(ZoneId.WATER) ? 1 : (this._summon.isFlying() ? 2 : 0));
		}

		if (this.containsMask(NpcInfoType.TEAM))
		{
			buffer.writeByte(this._summon.getTeam().getId());
		}

		if (this.containsMask(NpcInfoType.ENCHANT))
		{
			buffer.writeInt(this._summon.getTemplate().getWeaponEnchant());
		}

		if (this.containsMask(NpcInfoType.FLYING))
		{
			buffer.writeInt(this._summon.isFlying());
		}

		if (this.containsMask(NpcInfoType.CLONE))
		{
			buffer.writeInt(0);
		}

		if (this.containsMask(NpcInfoType.PET_EVOLUTION_ID))
		{
			buffer.writeInt(0);
		}

		if (this.containsMask(NpcInfoType.DISPLAY_EFFECT))
		{
			buffer.writeInt(0);
		}

		if (this.containsMask(NpcInfoType.TRANSFORMATION))
		{
			buffer.writeInt(this._summon.getTransformationDisplayId());
		}

		if (this.containsMask(NpcInfoType.CURRENT_HP))
		{
			buffer.writeLong((long) this._summon.getCurrentHp());
		}

		if (this.containsMask(NpcInfoType.CURRENT_MP))
		{
			buffer.writeInt((int) this._summon.getCurrentMp());
		}

		if (this.containsMask(NpcInfoType.MAX_HP))
		{
			buffer.writeLong(this._summon.getMaxHp());
		}

		if (this.containsMask(NpcInfoType.MAX_MP))
		{
			buffer.writeInt(this._summon.getMaxMp());
		}

		if (this.containsMask(NpcInfoType.SUMMONED))
		{
			buffer.writeByte(this._summon.isShowSummonAnimation() ? 2 : 0);
		}

		if (this.containsMask(NpcInfoType.FOLLOW_INFO))
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
		}

		if (this.containsMask(NpcInfoType.NAME))
		{
			buffer.writeString(this._summon.getName());
		}

		if (this.containsMask(NpcInfoType.NAME_NPCSTRINGID))
		{
			buffer.writeInt(-1);
		}

		if (this.containsMask(NpcInfoType.TITLE_NPCSTRINGID))
		{
			buffer.writeInt(-1);
		}

		if (this.containsMask(NpcInfoType.PVP_FLAG))
		{
			buffer.writeByte(this._summon.getPvpFlag());
		}

		if (this.containsMask(NpcInfoType.REPUTATION))
		{
			buffer.writeInt(this._summon.getReputation());
		}

		if (this.containsMask(NpcInfoType.CLAN))
		{
			buffer.writeInt(this._clanId);
			buffer.writeInt(this._clanCrest);
			buffer.writeInt(this._clanLargeCrest);
			buffer.writeInt(this._allyId);
			buffer.writeInt(this._allyCrest);
		}

		if (this.containsMask(NpcInfoType.VISUAL_STATE))
		{
			buffer.writeByte(this._statusMask);
		}

		if (this.containsMask(NpcInfoType.SHOW_NAME))
		{
			buffer.writeByte(1);
		}

		if (this.containsMask(NpcInfoType.ABNORMALS))
		{
			buffer.writeInt(0);
			Team team = GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null ? this._summon.getTeam() : Team.NONE;
			buffer.writeShort(this._abnormalVisualEffects.size() + (this._summon.isInvisible() ? 1 : 0) + (team != Team.NONE ? 1 : 0));

			for (AbnormalVisualEffect abnormalVisualEffect : this._abnormalVisualEffects)
			{
				buffer.writeShort(abnormalVisualEffect.getClientId());
			}

			if (this._summon.isInvisible())
			{
				buffer.writeShort(AbnormalVisualEffect.STEALTH.getClientId());
			}

			if (team == Team.BLUE)
			{
				if (GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null)
				{
					buffer.writeShort(GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT.getClientId());
				}
			}
			else if (team == Team.RED && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null)
			{
				buffer.writeShort(GeneralConfig.RED_TEAM_ABNORMAL_EFFECT.getClientId());
			}
		}
	}
}
