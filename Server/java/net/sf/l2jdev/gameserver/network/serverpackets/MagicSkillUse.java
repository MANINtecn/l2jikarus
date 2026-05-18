package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.skill.SkillCastingType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class MagicSkillUse extends ServerPacket
{
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseGroup;
	private final int _reuseDelay;
	private final int _actionId;
	private final SkillCastingType _castingType;
	private final Creature _creature;
	private final WorldObject _target;
	private final boolean _isGroundTargetSkill;
	private final Location _groundLocation;

	public MagicSkillUse(Creature creature, WorldObject target, int skillId, int skillLevel, int hitTime, int reuseDelay, int reuseGroup, int actionId, SkillCastingType castingType, boolean isGroundTargetSkill)
	{
		this._creature = creature;
		this._target = target;
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._hitTime = hitTime;
		this._reuseGroup = reuseGroup;
		this._reuseDelay = reuseDelay;
		this._actionId = actionId;
		this._castingType = castingType;
		this._isGroundTargetSkill = isGroundTargetSkill;
		this._groundLocation = creature.isPlayer() ? creature.asPlayer().getCurrentSkillWorldPosition() : null;
	}

	public MagicSkillUse(Creature creature, WorldObject target, int skillId, int skillLevel, int hitTime, int reuseDelay, int reuseGroup, int actionId, SkillCastingType castingType)
	{
		this(creature, target, skillId, skillLevel, hitTime, reuseDelay, reuseGroup, actionId, castingType, false);
	}

	public MagicSkillUse(Creature creature, WorldObject target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		this(creature, target, skillId, skillLevel, hitTime, reuseDelay, -1, -1, SkillCastingType.NORMAL);
	}

	public MagicSkillUse(Creature creature, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		this(creature, creature, skillId, skillLevel, hitTime, reuseDelay, -1, -1, SkillCastingType.NORMAL);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.MAGIC_SKILL_USE.writeId(this, buffer);
		buffer.writeInt(this._castingType.getClientBarId());
		buffer.writeInt(this._creature.getObjectId());
		buffer.writeInt(this._target.getObjectId());
		buffer.writeInt(this._skillId);
		buffer.writeInt(this._skillLevel);
		buffer.writeInt(this._hitTime);
		buffer.writeInt(this._reuseGroup);
		buffer.writeInt(this._reuseDelay);
		buffer.writeInt(this._creature.getX());
		buffer.writeInt(this._creature.getY());
		buffer.writeInt(this._creature.getZ());
		buffer.writeShort(this._isGroundTargetSkill ? '\uffff' : 0);
		if (this._groundLocation == null)
		{
			buffer.writeShort(0);
		}
		else
		{
			buffer.writeShort(1);
			buffer.writeInt(this._groundLocation.getX());
			buffer.writeInt(this._groundLocation.getY());
			buffer.writeInt(this._groundLocation.getZ());
		}

		buffer.writeInt(this._target.getX());
		buffer.writeInt(this._target.getY());
		buffer.writeInt(this._target.getZ());
		buffer.writeInt(this._actionId >= 0);
		buffer.writeInt(this._actionId >= 0 ? this._actionId : 0);
		if (this._groundLocation == null)
		{
			buffer.writeInt(-1);
		}
	}
}
