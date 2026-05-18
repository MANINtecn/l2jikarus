package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.CursedWeaponsManager;
import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.appearance.PlayerAppearance;
import net.sf.l2jdev.gameserver.model.actor.templates.PlayerTemplate;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.variables.PlayerVariables;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;

public class UserInfo extends AbstractMaskPacket<UserInfoType>
{
	private Player _player;
	private int _relation;
	private int _runSpd;
	private int _walkSpd;
	private int _swimRunSpd;
	private int _swimWalkSpd;
 
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private double _moveMultiplier;
	private int _enchantLevel;
	private int _armorEnchant;
	private String _title;
	private PlayerAppearance _appearance;
	private Inventory _inventory;
	private PlayerVariables _variables;
	private int _afkAnimation;
	private int _rank;
	private final byte[] _masks = new byte[]
	{
		0,
		0,
		0,
		0
	};
	private int _initSize = 5;

	public UserInfo(Player player)
	{
		this(player, true);
	}

	public UserInfo(Player player, boolean addAll)
	{
		if (!player.isSubclassLocked())
		{
			this._player = player;
			this._relation = this.calculateRelation(player);
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
			this._variables = player.getVariables();
			this._afkAnimation = player.getClan() != null && CastleManager.getInstance().getCastleByOwner(player.getClan()) != null ? (player.isClanLeader() ? 100 : 101) : 0;
			this._rank = RankManager.getInstance().getPlayerGlobalRank(player) == 1 ? 1 : (RankManager.getInstance().getPlayerRaceRank(player) == 1 ? 2 : 0);
			this._title = player.getTitle();
			if (player.isGM() && player.isInvisible())
			{
				this._title = "[Invisible]";
			}

			if (addAll)
			{
				this.addComponentType(UserInfoType.values());
			}
		}
	}

	@Override
	protected byte[] getMasks()
	{
		return this._masks;
	}

	@Override
	protected void onNewMaskAdded(UserInfoType component)
	{
		this.calcBlockSize(component);
	}

	private void calcBlockSize(UserInfoType type)
	{
		switch (type)
		{
			case BASIC_INFO:
				if (this._player.isMercenary())
				{
					this._initSize = this._initSize + type.getBlockLength() + this._player.getMercenaryName().length() * 2;
				}
				else
				{
					this._initSize = this._initSize + type.getBlockLength() + this._appearance.getVisibleName().length() * 2;
				}
				break;
			case CLAN:
				this._initSize = this._initSize + type.getBlockLength() + this._title.length() * 2;
				break;
			default:
				this._initSize = this._initSize + type.getBlockLength();
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (this._player != null)
		{
			ServerPackets.USER_INFO.writeId(this, buffer);
			buffer.writeInt(this._player.getObjectId());
			buffer.writeInt(this._initSize);
			buffer.writeShort(30);
			buffer.writeBytes(this._masks);
			if (this.containsMask(UserInfoType.RELATION))
			{
				buffer.writeInt(this._relation);
			}

			if (this.containsMask(UserInfoType.BASIC_INFO))
			{
				if (this._player.isMercenary())
				{
					buffer.writeShort(23 + this._player.getMercenaryName().length() * 2);
					buffer.writeSizedString(this._player.getMercenaryName());
				}
				else
				{
					buffer.writeShort(23 + this._appearance.getVisibleName().length() * 2);
					buffer.writeSizedString(this._player.getName());
				}

				buffer.writeByte(this._player.isGM());
				buffer.writeByte(this._player.getRace().ordinal());
				buffer.writeByte(this._appearance.isFemale());
				buffer.writeInt(this._player.getBaseTemplate().getPlayerClass().getRootClass().getId());
				buffer.writeInt(this._player.getPlayerClass().getId());
				buffer.writeInt(this._player.getLevel());
				buffer.writeInt(this._player.getPlayerClass().getId());
			}

			if (this.containsMask(UserInfoType.BASE_STATS))
			{
				buffer.writeShort(18);
				buffer.writeShort(this._player.getSTR());
				buffer.writeShort(this._player.getDEX());
				buffer.writeShort(this._player.getCON());
				buffer.writeShort(this._player.getINT());
				buffer.writeShort(this._player.getWIT());
				buffer.writeShort(this._player.getMEN());
				buffer.writeShort(0);
				buffer.writeShort(0);
			}

			if (this.containsMask(UserInfoType.MAX_HPCPMP))
			{
				buffer.writeShort(14);
				buffer.writeInt((int) this._player.getMaxHp());
				buffer.writeInt(this._player.getMaxMp());
				buffer.writeInt(this._player.getMaxCp());
			}

			if (this.containsMask(UserInfoType.CURRENT_HPMPCP_EXP_SP))
			{
				buffer.writeShort(39);
				buffer.writeInt((int) Math.round(this._player.getCurrentHp()));
				buffer.writeInt((int) Math.round(this._player.getCurrentMp()));
				buffer.writeInt((int) Math.round(this._player.getCurrentCp()));
				buffer.writeLong(this._player.getSp());
				buffer.writeLong(this._player.getExp());
				buffer.writeDouble((float) (this._player.getExp() - ExperienceData.getInstance().getExpForLevel(this._player.getLevel())) / (float) (ExperienceData.getInstance().getExpForLevel(this._player.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(this._player.getLevel())));
				buffer.writeByte(0);
			}

			if (this.containsMask(UserInfoType.ENCHANTLEVEL))
			{
				buffer.writeShort(7);
				buffer.writeByte(this._enchantLevel);
				buffer.writeByte(this._armorEnchant);
				buffer.writeByte(0);
				buffer.writeByte(0);
				buffer.writeByte(0);
			}

			if (this.containsMask(UserInfoType.APPAREANCE))
			{
				buffer.writeShort(19);
				buffer.writeInt(this._player.getVisualHair());
				buffer.writeInt(this._player.getVisualHairColor());
				buffer.writeInt(this._player.getVisualFace());
				buffer.writeByte(this._player.isHairAccessoryEnabled());
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
			}

			if (this.containsMask(UserInfoType.STATUS))
			{
				buffer.writeShort(6);
				buffer.writeByte(this._player.getMountType().ordinal());
				buffer.writeByte(this._player.getPrivateStoreType().getId());
				buffer.writeByte(this._player.hasDwarvenCraft() || this._player.getSkillLevel(248) > 0);
				buffer.writeByte(0);
			}

			if (this.containsMask(UserInfoType.STATS))
			{
				buffer.writeShort(76);
				buffer.writeShort(this._player.getActiveWeaponItem() != null ? 40 : 20);
				buffer.writeLong(this._player.getPAtk());
				buffer.writeInt(this._player.getPAtkSpd());
				buffer.writeInt(this._player.getPDef());
				buffer.writeInt(this._player.getEvasionRate());
				buffer.writeInt(this._player.getAccuracy());
				buffer.writeInt(this._player.getCriticalHit());
				buffer.writeLong(this._player.getMAtk());
				buffer.writeInt(this._player.getMAtkSpd());
				buffer.writeInt(this._player.getPAtkSpd());
				buffer.writeInt(this._player.getMagicEvasionRate());
				buffer.writeInt(this._player.getMDef());
				buffer.writeInt(this._player.getMagicAccuracy());
				buffer.writeInt(this._player.getMCriticalHit());
				buffer.writeInt(this._player.getWeaponBonusPAtk());
				buffer.writeInt(this._player.getWeaponBonusMAtk());
				buffer.writeInt(this._player.getPSkillCriticalRate());
			}

			if (this.containsMask(UserInfoType.ELEMENTALS))
			{
				buffer.writeShort(14);
				buffer.writeShort(0);
				buffer.writeShort(0);
				buffer.writeShort(0);
				buffer.writeShort(0);
				buffer.writeShort(0);
				buffer.writeShort(0);
			}

			if (this.containsMask(UserInfoType.POSITION))
			{
				buffer.writeShort(18);
				buffer.writeInt(this._player.getX());
				buffer.writeInt(this._player.getY());
				buffer.writeInt(this._player.getZ());
				buffer.writeInt(this._player.isInVehicle() ? this._player.getVehicle().getObjectId() : 0);
			}

			if (this.containsMask(UserInfoType.SPEED))
			{
				buffer.writeShort(18);
				buffer.writeShort(this._runSpd);
				buffer.writeShort(this._walkSpd);
				buffer.writeShort(this._swimRunSpd);
				buffer.writeShort(this._swimWalkSpd);
				buffer.writeShort(0);
				buffer.writeShort(0);
				buffer.writeShort(this._flyRunSpd);
				buffer.writeShort(this._flyWalkSpd);
			}

			if (this.containsMask(UserInfoType.MULTIPLIER))
			{
				buffer.writeShort(18);
				buffer.writeDouble(this._moveMultiplier);
				buffer.writeDouble(this._player.getAttackSpeedMultiplier());
			}

			if (this.containsMask(UserInfoType.COL_RADIUS_HEIGHT))
			{
				buffer.writeShort(18);
				buffer.writeDouble(this._player.getCollisionRadius());
				buffer.writeDouble(this._player.getCollisionHeight());
			}

			if (this.containsMask(UserInfoType.ATK_ELEMENTAL))
			{
				buffer.writeShort(5);
				buffer.writeByte(0);
				buffer.writeShort(0);
			}

			if (this.containsMask(UserInfoType.CLAN))
			{
				buffer.writeShort(32 + this._title.length() * 2);
				buffer.writeSizedString(this._title);
				buffer.writeShort(this._player.getPledgeType());
				buffer.writeInt(this._player.getClanId());
				buffer.writeInt(this._player.getClanCrestLargeId());
				buffer.writeInt(this._player.getClanCrestId());
				buffer.writeInt(this._player.getClanPrivileges().getMask());
				buffer.writeByte(this._player.isClanLeader());
				buffer.writeInt(this._player.getAllyId());
				buffer.writeInt(this._player.getAllyCrestId());
				buffer.writeByte(this._player.isInMatchingRoom());
			}

			if (this.containsMask(UserInfoType.SOCIAL))
			{
				buffer.writeShort(34);
				buffer.writeByte(this._player.getPvpFlag());
				buffer.writeInt(this._player.getReputation());
				buffer.writeByte(this._player.isNoble());
				buffer.writeByte(!this._player.isHero() && (!this._player.isGM() || !GeneralConfig.GM_HERO_AURA) ? 0 : 2);
				buffer.writeByte(this._player.getPledgeClass());
				buffer.writeInt(this._player.getPkKills());
				buffer.writeInt(this._player.getPvpKills());
				buffer.writeShort(this._player.getRecomLeft());
				buffer.writeShort(this._player.getRecomHave());
				buffer.writeInt(this._afkAnimation);
				buffer.writeInt(0);
				buffer.writeInt(0);
			}

			if (this.containsMask(UserInfoType.VITA_FAME))
			{
				buffer.writeShort(19);
				buffer.writeInt(this._player.getVitalityPoints());
				buffer.writeByte(0);
				buffer.writeInt(0);
				buffer.writeInt(0);
				buffer.writeByte(0);
				buffer.writeShort(0);
				buffer.writeByte(0);
			}

			if (this.containsMask(UserInfoType.SLOTS))
			{
				buffer.writeShort(16);
				buffer.writeByte(this._inventory.getTalismanSlots());
				buffer.writeByte(this._inventory.getBroochJewelSlots());
				buffer.writeByte(this._player.getTeam().getId());
				buffer.writeInt(0);
				if (this._inventory.getAgathionSlots() > 0)
				{
					buffer.writeByte(1);
					buffer.writeByte(this._inventory.getAgathionSlots() - 1);
				}
				else
				{
					buffer.writeByte(0);
					buffer.writeByte(0);
				}

				buffer.writeByte(this._inventory.getArtifactSlots());
				buffer.writeInt(0);
			}

			if (this.containsMask(UserInfoType.MOVEMENTS))
			{
				buffer.writeShort(4);
				buffer.writeByte(this._player.isInsideZone(ZoneId.WATER) ? 1 : (this._player.isFlyingMounted() ? 2 : 0));
				buffer.writeByte(this._player.isRunning());
			}

			if (this.containsMask(UserInfoType.COLOR))
			{
				buffer.writeShort(10);
				buffer.writeInt(this._appearance.getNameColor());
				buffer.writeInt(this._appearance.getTitleColor());
			}

			if (this.containsMask(UserInfoType.INVENTORY_LIMIT))
			{
				buffer.writeShort(17);
				buffer.writeShort(0);
				buffer.writeInt(this._player.getInventoryLimit());
				buffer.writeByte(this._player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(this._player.getCursedWeaponEquippedId()) : 0);
				buffer.writeInt(0);
				buffer.writeInt(0);
			}

			if (this.containsMask(UserInfoType.TRUE_HERO))
			{
				buffer.writeShort(9);
				buffer.writeInt(0);
				buffer.writeShort(0);
				buffer.writeByte(this._player.isTrueHero() ? 100 : 0);
			}

			if (this.containsMask(UserInfoType.ATT_SPIRITS))
			{
				buffer.writeShort(34);
				buffer.writeInt((int) this._player.getFireSpiritAttack());
				buffer.writeInt((int) this._player.getWaterSpiritAttack());
				buffer.writeInt((int) this._player.getWindSpiritAttack());
				buffer.writeInt((int) this._player.getEarthSpiritAttack());
				buffer.writeInt((int) this._player.getFireSpiritDefense());
				buffer.writeInt((int) this._player.getWaterSpiritDefense());
				buffer.writeInt((int) this._player.getWindSpiritDefense());
				buffer.writeInt((int) this._player.getEarthSpiritDefense());
			}

			if (this.containsMask(UserInfoType.RANKING))
			{
				buffer.writeShort(6);
				buffer.writeInt(this._rank);
			}

			if (this.containsMask(UserInfoType.STAT_POINTS))
			{
				buffer.writeShort(28);
				buffer.writeShort(this._player.getLevel() < 76 ? 0 : this._player.getLevel() - 75 + this._variables.getInt("ELIXIRS_AVAILABLE", 0) + (int) this._player.getStat().getValue(Stat.ELIXIR_USAGE_LIMIT, 0.0));
				buffer.writeShort(this._variables.getInt("STAT_STR", 0));
				buffer.writeShort(this._variables.getInt("STAT_DEX", 0));
				buffer.writeShort(this._variables.getInt("STAT_CON", 0));
				buffer.writeShort(this._variables.getInt("STAT_INT", 0));
				buffer.writeShort(this._variables.getInt("STAT_WIT", 0));
				buffer.writeShort(this._variables.getInt("STAT_MEN", 0));
				buffer.writeShort(this._variables.getInt("STAT_STR", 0));
				buffer.writeShort(this._variables.getInt("STAT_DEX", 0));
				buffer.writeShort(this._variables.getInt("STAT_CON", 0));
				buffer.writeShort(this._variables.getInt("STAT_INT", 0));
				buffer.writeShort(this._variables.getInt("STAT_WIT", 0));
				buffer.writeShort(this._variables.getInt("STAT_MEN", 0));
			}

			if (this.containsMask(UserInfoType.STAT_ABILITIES))
			{
				buffer.writeShort(18);
				PlayerTemplate template = this._player.getTemplate();
				buffer.writeShort(this._player.getSTR() - template.getBaseSTR() - this._variables.getInt("STAT_STR", 0));
				buffer.writeShort(this._player.getDEX() - template.getBaseDEX() - this._variables.getInt("STAT_DEX", 0));
				buffer.writeShort(this._player.getCON() - template.getBaseCON() - this._variables.getInt("STAT_CON", 0));
				buffer.writeShort(this._player.getINT() - template.getBaseINT() - this._variables.getInt("STAT_INT", 0));
				buffer.writeShort(this._player.getWIT() - template.getBaseWIT() - this._variables.getInt("STAT_WIT", 0));
				buffer.writeShort(this._player.getMEN() - template.getBaseMEN() - this._variables.getInt("STAT_MEN", 0));
				buffer.writeShort(0);
				buffer.writeShort(0);
			}

			if (this.containsMask(UserInfoType.ELIXIR_USED))
			{
				buffer.writeShort(this._variables.getInt("ELIXIRS_AVAILABLE", 0));
				buffer.writeShort(0);
			}

			if (this.containsMask(UserInfoType.VANGUARD_MOUNT))
			{
				buffer.writeByte(this._player.getPlayerClass().level() + 1);
			}
		}
	}

	@Override
	public void runImpl(Player player)
	{
		if (this._player != null)
		{
			if (this.containsMask(UserInfoType.VITA_FAME))
			{
				this._player.sendUserBoostStat();
			}
		}
	}

	private int calculateRelation(Player player)
	{
		int relation = 0;
		Party party = player.getParty();
		Clan clan = player.getClan();
		if (party != null)
		{
			relation |= 8;
			if (party.getLeader() == this._player)
			{
				relation |= 16;
			}
		}

		if (clan != null)
		{
			if (player.getSiegeState() == 1)
			{
				relation |= 256;
			}
			else if (player.getSiegeState() == 2)
			{
				relation |= 32;
			}

			if (clan.getLeaderId() == player.getObjectId())
			{
				relation |= 64;
			}
		}

		if (player.getSiegeState() != 0)
		{
			relation |= 128;
		}

		return relation;
	}
}
