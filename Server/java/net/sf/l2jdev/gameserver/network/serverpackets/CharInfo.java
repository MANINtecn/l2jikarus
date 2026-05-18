package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.appearance.PlayerAppearance;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Team;
import net.sf.l2jdev.gameserver.model.actor.instance.Decoy;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.skill.AbnormalVisualEffect;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class CharInfo extends ServerPacket
{
	private static final int[] PAPERDOLL_ORDER = new int[]
	{
		0,
		1,
		5,
		7,
		10,
		6,
		11,
		12,
		28,
		5,
		2,
		3
	};
	private final Player _player;
	private final Clan _clan;
	private int _objId;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
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
	private int _enchantLevel = 0;
	private int _armorEnchant = 0;
	private int _vehicleId = 0;
	private final PlayerAppearance _appearance;
	private final Inventory _inventory;
	private final ILocational _baitLocation;
	private final Set<AbnormalVisualEffect> _abnormalVisualEffects;
	private final Team _team;
	private final int _afkAnimation;
	private final int _rank;
	private final boolean _gmSeeInvis;

	public CharInfo(Player player, boolean gmSeeInvis)
	{
		this._player = player;
		this._objId = player.getObjectId();
		this._clan = player.getClan();
		if (player.getVehicle() != null && player.getInVehiclePosition() != null)
		{
			this._x = player.getInVehiclePosition().getX();
			this._y = player.getInVehiclePosition().getY();
			this._z = player.getInVehiclePosition().getZ();
			this._vehicleId = player.getVehicle().getObjectId();
		}
		else
		{
			this._x = player.getX();
			this._y = player.getY();
			this._z = player.getZ();
		}

		this._heading = player.getHeading();
		this._mAtkSpd = player.getMAtkSpd();
		this._pAtkSpd = player.getPAtkSpd();
		this._attackSpeedMultiplier = (float) player.getAttackSpeedMultiplier();
		this._moveMultiplier = player.getMovementSpeedMultiplier();
		this._runSpd = (int) Math.round(player.getRunSpeed() / this._moveMultiplier);
		this._walkSpd = (int) Math.round(player.getWalkSpeed() / this._moveMultiplier);
		this._swimRunSpd = (int) Math.round(player.getSwimRunSpeed() / this._moveMultiplier);
		this._swimWalkSpd = (int) Math.round(player.getSwimWalkSpeed() / this._moveMultiplier);
		this._flyRunSpd = player.isFlying() ? this._runSpd : 0;
		this._flyWalkSpd = player.isFlying() ? this._walkSpd : 0;
		this._appearance = player.getAppearance();
		this._inventory = player.getInventory();
		this._enchantLevel = this._inventory.getWeaponEnchant();
		this._armorEnchant = this._inventory.getArmorSetEnchant();
		this._baitLocation = player.getFishing().getBaitLocation();
		this._abnormalVisualEffects = player.getEffectList().getCurrentAbnormalVisualEffects();
		this._team = GeneralConfig.BLUE_TEAM_ABNORMAL_EFFECT != null && GeneralConfig.RED_TEAM_ABNORMAL_EFFECT != null ? player.getTeam() : Team.NONE;
		this._afkAnimation = player.getClan() != null && CastleManager.getInstance().getCastleByOwner(player.getClan()) != null ? (player.isClanLeader() ? 100 : 101) : 0;
		this._rank = OlympiadConfig.OLYMPIAD_HIDE_NAMES && player.isInOlympiadMode() ? 0 : (RankManager.getInstance().getPlayerGlobalRank(player) == 1 ? 1 : (RankManager.getInstance().getPlayerRaceRank(player) == 1 ? 2 : 0));
		this._gmSeeInvis = gmSeeInvis;
	}

	public CharInfo(Decoy decoy, boolean gmSeeInvis)
	{
		this(decoy.asPlayer(), gmSeeInvis);
		this._objId = decoy.getObjectId();
		this._x = decoy.getX();
		this._y = decoy.getY();
		this._z = decoy.getZ();
		this._heading = decoy.getHeading();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHAR_INFO.writeId(this, buffer);
		buffer.writeByte(0);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(this._vehicleId);
		buffer.writeInt(this._objId);
		buffer.writeString(this._player.isMercenary() ? this._player.getMercenaryName() : this._appearance.getVisibleName());
		buffer.writeShort(this._player.getRace().ordinal());
		buffer.writeByte(this._appearance.isFemale());
		buffer.writeInt(this._player.getBaseTemplate().getPlayerClass().getRootClass().getId());

		for (int slot : this.getPaperdollOrder())
		{
			buffer.writeInt(this._inventory.getPaperdollItemDisplayId(slot));
		}

		for (int slot : this.getPaperdollOrderAugument())
		{
			VariationInstance augment = this._inventory.getPaperdollAugmentation(slot);
			if (augment != null)
			{
				buffer.writeInt(augment.getOption1Id());
				buffer.writeInt(augment.getOption2Id());
				buffer.writeInt(augment.getOption3Id());
			}
			else
			{
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
			}
		}

		for (int slotx : this.getPaperdollOrderAugument())
		{
			VariationInstance augment = this._inventory.getPaperdollAugmentation(slotx);
			if (augment != null)
			{
				buffer.writeInt(augment.getOption1Id());
				buffer.writeInt(augment.getOption2Id());
				buffer.writeInt(augment.getOption3Id());
			}
			else
			{
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
			}
		}

		buffer.writeByte(this._armorEnchant);

		for (int slotxx : this.getPaperdollOrderVisualId())
		{
			buffer.writeInt(this._inventory.getPaperdollItemVisualId(slotxx));
		}

		buffer.writeByte(this._player.getPvpFlag());
		buffer.writeInt(this._player.getReputation());
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
		buffer.writeDouble(this._player.getCollisionRadius());
		buffer.writeDouble(this._player.getCollisionHeight());
		buffer.writeInt(this._player.getVisualHair());
		buffer.writeInt(this._player.getVisualHairColor());
		buffer.writeInt(this._player.getVisualFace());
		buffer.writeString(this._gmSeeInvis ? "Invisible" : (this._player.isMercenary() ? "" : this._appearance.getVisibleTitle()));
		buffer.writeInt(this._appearance.getVisibleClanId());
		buffer.writeInt(this._appearance.getVisibleClanCrestId());
		buffer.writeInt(this._appearance.getVisibleAllyId());
		buffer.writeInt(this._appearance.getVisibleAllyCrestId());
		buffer.writeByte(!this._player.isSitting());
		buffer.writeByte(this._player.isRunning());
		buffer.writeByte(this._player.isInCombat());
		buffer.writeByte(!this._player.isInOlympiadMode() && this._player.isAlikeDead());
		buffer.writeByte(this._player.isInvisible());
		buffer.writeByte(this._player.getMountType().ordinal());
		buffer.writeByte(this._player.getPrivateStoreType().getId());
		buffer.writeShort(this._player.getCubics().size());
		this._player.getCubics().keySet().forEach(buffer::writeShort);
		buffer.writeByte(this._player.isInMatchingRoom());
		buffer.writeByte(this._player.isInsideZone(ZoneId.WATER) ? 1 : (this._player.isFlyingMounted() ? 2 : 0));
		buffer.writeShort(this._player.getRecomHave());
		buffer.writeInt(this._player.getMountNpcId() == 0 ? 0 : this._player.getMountNpcId() + 1000000);
		buffer.writeInt(this._player.getPlayerClass().getId());
		buffer.writeInt(0);
		buffer.writeByte(this._player.isMounted() ? 0 : this._enchantLevel);
		buffer.writeByte(this._player.getTeam().getId());
		buffer.writeInt(this._player.getClanCrestLargeId());
		buffer.writeByte(this._player.isNoble());
		buffer.writeByte(!this._player.isHero() && (!this._player.isGM() || !GeneralConfig.GM_HERO_AURA) ? 0 : 2);
		buffer.writeByte(this._player.isFishing());
		if (this._baitLocation != null)
		{
			buffer.writeInt(this._baitLocation.getX());
			buffer.writeInt(this._baitLocation.getY());
			buffer.writeInt(this._baitLocation.getZ());
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}

		buffer.writeInt(this._appearance.getNameColor());
		buffer.writeInt(this._heading);
		buffer.writeByte(this._player.getPledgeClass());
		buffer.writeShort(this._player.getPledgeType());
		buffer.writeInt(this._appearance.getTitleColor());
		buffer.writeByte(this._player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(this._player.getCursedWeaponEquippedId()) : 0);
		buffer.writeInt(this._clan != null ? this._clan.getReputationScore() : 0);
		buffer.writeInt(this._player.getTransformationDisplayId());
		buffer.writeInt(this._player.getAgathionId());
		buffer.writeByte(0);
		buffer.writeInt((int) Math.round(this._player.getCurrentCp()));
		buffer.writeInt((int) this._player.getMaxHp());
		buffer.writeInt((int) Math.round(this._player.getCurrentHp()));
		buffer.writeInt(this._player.getMaxMp());
		buffer.writeInt((int) Math.round(this._player.getCurrentMp()));
		buffer.writeByte(0);
		buffer.writeInt(this._abnormalVisualEffects.size() + (this._gmSeeInvis ? 1 : 0) + (this._team != Team.NONE ? 1 : 0));

		for (AbnormalVisualEffect abnormalVisualEffect : this._abnormalVisualEffects)
		{
			buffer.writeShort(abnormalVisualEffect.getClientId());
		}

		if (this._gmSeeInvis)
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

		buffer.writeByte(this._player.isTrueHero() ? 100 : 0);
		buffer.writeByte(this._player.isHairAccessoryEnabled());
		buffer.writeByte(this._player.getAbilityPointsUsed());
		buffer.writeInt(0);
		buffer.writeInt(this._afkAnimation);
		buffer.writeInt(this._rank);
		buffer.writeShort(0);
		buffer.writeByte(0);
		buffer.writeInt(this._player.getPlayerClass().getId());
		buffer.writeByte(0);
		switch (this._player.getBaseClass())
		{
			case 247:
			case 248:
			case 249:
			case 250:
			case 251:
			case 252:
			case 253:
			case 254:
				buffer.writeInt(this._player.getVisualHairColor() - 1);
				break;
			default:
				buffer.writeInt(this._player.getVisualHairColor() + 1);
		}

		buffer.writeInt(0);
		buffer.writeByte(this._player.getPlayerClass().level() + 1);
		buffer.writeInt(0);
	}

	@Override
	public int[] getPaperdollOrder()
	{
		return PAPERDOLL_ORDER;
	}
}
