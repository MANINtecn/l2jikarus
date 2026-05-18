package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.enums.player.Sex;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.FakePlayerHolder;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class FakePlayerInfo extends ServerPacket
{
	private final Npc _npc;
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _mAtkSpd;
	private final int _pAtkSpd;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	private final float _attackSpeedMultiplier;
	private final FakePlayerHolder _fpcHolder;
	private final Set<AbnormalVisualEffect> _abnormalVisualEffects;
	private final Clan _clan;

	public FakePlayerInfo(Npc npc)
	{
		this._npc = npc;
		this._objId = npc.getObjectId();
		this._x = npc.getX();
		this._y = npc.getY();
		this._z = npc.getZ();
		this._heading = npc.getHeading();
		this._mAtkSpd = npc.getMAtkSpd();
		this._pAtkSpd = npc.getPAtkSpd();
		this._attackSpeedMultiplier = (float) npc.getAttackSpeedMultiplier();
		this._moveMultiplier = npc.getMovementSpeedMultiplier();
		this._runSpd = (int) Math.round(npc.getRunSpeed() / this._moveMultiplier);
		this._walkSpd = (int) Math.round(npc.getWalkSpeed() / this._moveMultiplier);
		this._swimRunSpd = (int) Math.round(npc.getSwimRunSpeed() / this._moveMultiplier);
		this._swimWalkSpd = (int) Math.round(npc.getSwimWalkSpeed() / this._moveMultiplier);
		this._flyRunSpd = npc.isFlying() ? this._runSpd : 0;
		this._flyWalkSpd = npc.isFlying() ? this._walkSpd : 0;
		this._fpcHolder = npc.getTemplate().getFakePlayerInfo();
		this._abnormalVisualEffects = this._npc.getEffectList().getCurrentAbnormalVisualEffects();
		this._clan = ClanTable.getInstance().getClan(this._fpcHolder.getClanId());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHAR_INFO.writeId(this, buffer);
		buffer.writeByte(0);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(0);
		buffer.writeInt(this._objId);
		buffer.writeString(this._npc.getName());
		buffer.writeShort(this._npc.getRace().ordinal());
		buffer.writeByte(this._npc.getTemplate().getSex() == Sex.FEMALE);
		buffer.writeInt(this._fpcHolder.getPlayerClass().getRootClass().getId());
		buffer.writeInt(0);
		buffer.writeInt(this._fpcHolder.getEquipHead());
		buffer.writeInt(this._fpcHolder.getEquipRHand());
		buffer.writeInt(this._fpcHolder.getEquipLHand());
		buffer.writeInt(this._fpcHolder.getEquipGloves());
		buffer.writeInt(this._fpcHolder.getEquipChest());
		buffer.writeInt(this._fpcHolder.getEquipLegs());
		buffer.writeInt(this._fpcHolder.getEquipFeet());
		buffer.writeInt(this._fpcHolder.getEquipCloak());
		buffer.writeInt(this._fpcHolder.getEquipRHand());
		buffer.writeInt(this._fpcHolder.getEquipHair());
		buffer.writeInt(this._fpcHolder.getEquipHair2());

		for (int _ : this.getPaperdollOrderAugument())
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}

		buffer.writeByte(this._fpcHolder.getArmorEnchantLevel());

		for (int _ : this.getPaperdollOrderVisualId())
		{
			buffer.writeInt(0);
		}

		buffer.writeByte(this._npc.getScriptValue());
		buffer.writeInt(this._npc.getReputation());
		buffer.writeInt(this._mAtkSpd);
		buffer.writeInt(this._pAtkSpd);
		buffer.writeShort(this._runSpd);
		buffer.writeShort(this._walkSpd);
		buffer.writeShort(this._swimRunSpd);
		buffer.writeShort(this._swimWalkSpd);
		buffer.writeShort(this._flyRunSpd);
		buffer.writeShort(this._flyWalkSpd);
		buffer.writeShort(this._flyRunSpd);
		buffer.writeShort(this._flyWalkSpd);
		buffer.writeDouble(this._moveMultiplier);
		buffer.writeDouble(this._attackSpeedMultiplier);
		buffer.writeDouble(this._npc.getCollisionRadius());
		buffer.writeDouble(this._npc.getCollisionHeight());
		buffer.writeInt(this._fpcHolder.getHair());
		buffer.writeInt(this._fpcHolder.getHairColor());
		buffer.writeInt(this._fpcHolder.getFace());
		buffer.writeString(this._npc.getTemplate().getTitle());
		if (this._clan != null)
		{
			buffer.writeInt(this._clan.getId());
			buffer.writeInt(this._clan.getCrestId());
			buffer.writeInt(this._clan.getAllyId());
			buffer.writeInt(this._clan.getAllyCrestId());
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}

		buffer.writeByte(!this._fpcHolder.isSitting());
		buffer.writeByte(this._npc.isRunning());
		buffer.writeByte(this._npc.isInCombat());
		buffer.writeByte(this._npc.isAlikeDead());
		buffer.writeByte(this._npc.isInvisible());
		buffer.writeByte(0);
		buffer.writeByte(this._fpcHolder.getPrivateStoreType());
		buffer.writeShort(0);
		buffer.writeByte(0);
		buffer.writeByte(this._npc.isInsideZone(ZoneId.WATER));
		buffer.writeShort(this._fpcHolder.getRecommends());
		buffer.writeInt(0);
		buffer.writeInt(this._fpcHolder.getPlayerClass().getId());
		buffer.writeInt(0);
		buffer.writeByte(this._fpcHolder.getWeaponEnchantLevel());
		buffer.writeByte(this._npc.getTeam().getId());
		buffer.writeInt(this._clan != null ? this._clan.getCrestLargeId() : 0);
		buffer.writeByte(this._fpcHolder.getNobleLevel());
		buffer.writeByte(this._fpcHolder.isHero() ? 2 : 0);
		buffer.writeByte(this._fpcHolder.isFishing());
		buffer.writeInt(this._fpcHolder.getBaitLocationX());
		buffer.writeInt(this._fpcHolder.getBaitLocationY());
		buffer.writeInt(this._fpcHolder.getBaitLocationZ());
		buffer.writeInt(this._fpcHolder.getNameColor());
		buffer.writeInt(this._heading);
		buffer.writeByte(this._fpcHolder.getPledgeStatus());
		buffer.writeShort(0);
		buffer.writeInt(this._fpcHolder.getTitleColor());
		buffer.writeByte(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(this._fpcHolder.getAgathionId());
		buffer.writeByte(0);
		buffer.writeInt(0);
		buffer.writeInt((int) this._npc.getMaxHp());
		buffer.writeInt((int) Math.round(this._npc.getCurrentHp()));
		buffer.writeInt(this._npc.getMaxMp());
		buffer.writeInt((int) Math.round(this._npc.getCurrentMp()));
		buffer.writeByte(0);
		buffer.writeInt(this._abnormalVisualEffects.size() + (this._npc.isInvisible() ? 1 : 0));

		for (AbnormalVisualEffect abnormalVisualEffect : this._abnormalVisualEffects)
		{
			buffer.writeShort(abnormalVisualEffect.getClientId());
		}

		if (this._npc.isInvisible())
		{
			buffer.writeShort(AbnormalVisualEffect.STEALTH.getClientId());
		}

		buffer.writeByte(0);
		buffer.writeByte(this._fpcHolder.getHair() > 0 || this._fpcHolder.getEquipHair2() > 0);
		buffer.writeByte(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeShort(0);
		buffer.writeByte(0);
		buffer.writeInt(this._fpcHolder.getPlayerClass().getId());
		buffer.writeByte(0);
		buffer.writeInt(this._fpcHolder.getHairColor() + 1);
		buffer.writeInt(0);
		buffer.writeByte(this._fpcHolder.getPlayerClass().level() + 1);
	}
}
