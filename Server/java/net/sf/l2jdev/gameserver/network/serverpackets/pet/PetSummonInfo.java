package net.sf.l2jdev.gameserver.network.serverpackets.pet;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.enums.EvolveLevel;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.actor.instance.Servitor;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;

public class PetSummonInfo extends ServerPacket
{
	private final Summon _summon;
	private final int _value;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
 
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	private int _maxFed;
	private int _curFed;
	private final Set<AbnormalVisualEffect> _abnormalVisualEffects;
	private final Team _team;
	private int _statusMask = 0;

	public PetSummonInfo(Summon summon, int value)
	{
		this._summon = summon;
		this._moveMultiplier = summon.getMovementSpeedMultiplier();
		this._runSpd = (int) Math.round(summon.getRunSpeed() / this._moveMultiplier);
		this._walkSpd = (int) Math.round(summon.getWalkSpeed() / this._moveMultiplier);
		this._swimRunSpd = (int) Math.round(summon.getSwimRunSpeed() / this._moveMultiplier);
		this._swimWalkSpd = (int) Math.round(summon.getSwimWalkSpeed() / this._moveMultiplier);
		this._flyRunSpd = summon.isFlying() ? this._runSpd : 0;
		this._flyWalkSpd = summon.isFlying() ? this._walkSpd : 0;
		this._value = value;
		if (summon.isPet())
		{
			Pet pet = this._summon.asPet();
			this._curFed = pet.getCurrentFed();
			this._maxFed = pet.getMaxFed();
		}
		else if (summon.isServitor())
		{
			Servitor sum = this._summon.asServitor();
			this._curFed = sum.getLifeTimeRemaining();
			this._maxFed = sum.getLifeTime();
		}

		this._abnormalVisualEffects = summon.getEffectList().getCurrentAbnormalVisualEffects();
		this._team = GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null ? this._summon.getTeam() : Team.NONE;
		if (summon.isBetrayed())
		{
			this._statusMask |= 1;
		}

		this._statusMask |= 2;
		if (summon.isRunning())
		{
			this._statusMask |= 4;
		}

		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(summon))
		{
			this._statusMask |= 8;
		}

		if (summon.isDead())
		{
			this._statusMask |= 16;
		}

		if (summon.isMountable())
		{
			this._statusMask |= 32;
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PET_INFO.writeId(this, buffer);
		buffer.writeByte(this._summon.getSummonType());
		buffer.writeInt(this._summon.getObjectId());
		buffer.writeInt(this._summon.getTemplate().getDisplayId() + 1000000);
		buffer.writeInt(this._summon.getX());
		buffer.writeInt(this._summon.getY());
		buffer.writeInt(this._summon.getZ());
		buffer.writeInt(this._summon.getHeading());
		buffer.writeInt(this._summon.getMAtkSpd());
		buffer.writeInt(this._summon.getPAtkSpd());
		buffer.writeShort(this._runSpd);
		buffer.writeShort(this._walkSpd);
		buffer.writeShort(this._swimRunSpd);
		buffer.writeShort(this._swimWalkSpd);
		buffer.writeShort(0);
		buffer.writeShort(0);
		buffer.writeShort(this._flyRunSpd);
		buffer.writeShort(this._flyWalkSpd);
		buffer.writeDouble(this._moveMultiplier);
		buffer.writeDouble(this._summon.getAttackSpeedMultiplier());
		buffer.writeDouble(this._summon.getTemplate().getFCollisionRadius());
		buffer.writeDouble(this._summon.getTemplate().getFCollisionHeight());
		buffer.writeInt(this._summon.getWeapon());
		buffer.writeInt(this._summon.getArmor());
		buffer.writeInt(0);
		buffer.writeByte(this._summon.isDead() ? 0 : (this._summon.isShowSummonAnimation() ? 2 : this._value));
		buffer.writeInt(-1);
		if (this._summon.isPet())
		{
			buffer.writeString(this._summon.getName());
		}
		else
		{
			buffer.writeString(this._summon.getTemplate().isUsingServerSideName() ? this._summon.getName() : "");
		}

		buffer.writeInt(-1);
		buffer.writeString(this._summon.getTitle());
		buffer.writeByte(this._summon.getPvpFlag());
		buffer.writeInt(this._summon.getReputation());
		buffer.writeInt(this._curFed);
		buffer.writeInt(this._maxFed);
		buffer.writeInt((int) this._summon.getCurrentHp());
		buffer.writeInt((int) this._summon.getMaxHp());
		buffer.writeInt((int) this._summon.getCurrentMp());
		buffer.writeInt(this._summon.getMaxMp());
		buffer.writeLong(this._summon.getStat().getSp());
		buffer.writeShort(this._summon.getLevel());
		buffer.writeLong(this._summon.getStat().getExp());
		buffer.writeLong(Math.min(this._summon.getExpForThisLevel(), this._summon.getStat().getExp()));
		buffer.writeLong(this._summon.getExpForNextLevel());
		buffer.writeInt(this._summon.isPet() ? this._summon.getInventory().getTotalWeight() : 0);
		buffer.writeInt(this._summon.getMaxLoad());
		buffer.writeInt(this._summon.getPAtk());
		buffer.writeInt(this._summon.getPDef());
		buffer.writeInt(this._summon.getAccuracy());
		buffer.writeInt(this._summon.getEvasionRate());
		buffer.writeInt(this._summon.getCriticalHit());
		buffer.writeInt(this._summon.getMAtk());
		buffer.writeInt(this._summon.getMDef());
		buffer.writeInt(this._summon.getMagicAccuracy());
		buffer.writeInt(this._summon.getMagicEvasionRate());
		buffer.writeInt(this._summon.getMCriticalHit());
		buffer.writeInt((int) this._summon.getMoveSpeed());
		buffer.writeInt(this._summon.getPAtkSpd());
		buffer.writeInt(this._summon.getMAtkSpd());
		buffer.writeByte(0);
		buffer.writeByte(this._summon.getTeam().getId());
		buffer.writeByte(this._summon.getSoulShotsPerHit());
		buffer.writeByte(this._summon.getSpiritShotsPerHit());
		buffer.writeInt(-1);
		buffer.writeInt(0);
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeShort(this._abnormalVisualEffects.size() + (this._summon.isInvisible() ? 1 : 0) + (this._team != Team.NONE ? 1 : 0));

		for (AbnormalVisualEffect abnormalVisualEffect : this._abnormalVisualEffects)
		{
			buffer.writeShort(abnormalVisualEffect.getClientId());
		}

		if (this._summon.isInvisible())
		{
			buffer.writeShort(AbnormalVisualEffect.STEALTH.getClientId());
		}

		if (this._team == Team.BLUE)
		{
			if (GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null)
			{
				buffer.writeShort(GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT.getClientId());
			}
		}
		else if (this._team == Team.RED && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null)
		{
			buffer.writeShort(GeneralConfig.RED_TEAM_ABNORMAL_EFFECT.getClientId());
		}

		buffer.writeByte(this._statusMask);
		if (this._summon.isPet())
		{
			Pet pet = this._summon.asPet();
			buffer.writeInt(pet.getPetData().getType());
			buffer.writeInt(pet.getEvolveLevel());
			buffer.writeInt(pet.getEvolveLevel() == 0 ? -1 : pet.getId());
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(EvolveLevel.None.ordinal());
			buffer.writeInt(0);
		}
	}
}
