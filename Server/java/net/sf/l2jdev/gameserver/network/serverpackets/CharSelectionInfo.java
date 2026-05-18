package net.sf.l2jdev.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.config.custom.MultilingualSupportConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflinePlayConfig;
import net.sf.l2jdev.gameserver.config.custom.OfflineTradeConfig;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;
import net.sf.l2jdev.gameserver.network.Disconnection;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.holders.CharacterInfoHolder;

public class CharSelectionInfo extends ServerPacket
{
	private static final Logger LOGGER = Logger.getLogger(CharSelectionInfo.class.getName());
	private static final int[] PAPERDOLL_ORDER = new int[]
	{
		0,
		8,
		9,
		4,
		13,
		14,
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
		3,
		16,
		15,
		17,
		18,
		19,
		20,
		21,
		22,
		23,
		24,
		25,
		26,
		27,
		29,
		30,
		31,
		32,
		33,
		34,
		35,
		36,
		37,
		38,
		39,
		40,
		41,
		42,
		43,
		44,
		45,
		46,
		47,
		48,
		49,
		50,
		51,
		52,
		53,
		54,
		55,
		56,
		57,
		58
	};
	private static final int[] PAPERDOLL_ORDER_VISUAL_ID = new int[]
	{
		5,
		7,
		10,
		6,
		11,
		12,
		5,
		2,
		3
	};
	private final String _loginName;
	private final int _sessionId;
	private int _activeId;
	private final List<CharacterInfoHolder> _characterPackages;

	public CharSelectionInfo(String loginName, int sessionId)
	{
		this._sessionId = sessionId;
		this._loginName = loginName;
		this._characterPackages = loadCharacterSelectInfo(this._loginName);
		this._activeId = -1;
	}

	public CharSelectionInfo(String loginName, int sessionId, int activeId)
	{
		this._sessionId = sessionId;
		this._loginName = loginName;
		this._characterPackages = loadCharacterSelectInfo(this._loginName);
		this._activeId = activeId;
	}

	public List<CharacterInfoHolder> getCharInfo()
	{
		return this._characterPackages;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CHARACTER_SELECTION_INFO.writeId(this, buffer);
		int size = this._characterPackages.size();
		buffer.writeInt(size);
		buffer.writeInt(ServerConfig.MAX_CHARACTERS_NUMBER_PER_ACCOUNT);
		buffer.writeByte(size == ServerConfig.MAX_CHARACTERS_NUMBER_PER_ACCOUNT);
		buffer.writeByte(1);
		buffer.writeInt(2);
		buffer.writeByte(0);
		buffer.writeByte(0);
		long lastAccess = 0L;
		if (this._activeId == -1)
		{
			for (int i = 0; i < size; i++)
			{
				if (lastAccess < this._characterPackages.get(i).getLastAccess())
				{
					lastAccess = this._characterPackages.get(i).getLastAccess();
					this._activeId = i;
				}
			}
		}

		for (int ix = 0; ix < size; ix++)
		{
			CharacterInfoHolder charInfoPackage = this._characterPackages.get(ix);
			int baseClassId = charInfoPackage.getBaseClassId();
			buffer.writeString(charInfoPackage.getName());
			buffer.writeInt(charInfoPackage.getObjectId());
			buffer.writeString(this._loginName);
			buffer.writeInt(this._sessionId);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(charInfoPackage.getSex());
			buffer.writeInt(charInfoPackage.getRace());
			buffer.writeInt(baseClassId);
			buffer.writeInt(ServerConfig.SERVER_ID);
			buffer.writeInt(charInfoPackage.getX());
			buffer.writeInt(charInfoPackage.getY());
			buffer.writeInt(charInfoPackage.getZ());
			buffer.writeDouble(charInfoPackage.getCurrentHp());
			buffer.writeDouble(charInfoPackage.getCurrentMp());
			buffer.writeLong(charInfoPackage.getSp());
			buffer.writeLong(charInfoPackage.getExp());
			buffer.writeDouble((float) (charInfoPackage.getExp() - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel())) / (float) (ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel())));
			buffer.writeInt(charInfoPackage.getLevel());
			buffer.writeInt(charInfoPackage.getReputation());
			buffer.writeInt(charInfoPackage.getPkKills());
			buffer.writeInt(charInfoPackage.getPvPKills());
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);

			for (int slot : this.getPaperdollOrder())
			{
				buffer.writeInt(charInfoPackage.getPaperdollItemId(slot));
			}

			for (int slot : this.getPaperdollOrderVisualId())
			{
				buffer.writeInt(charInfoPackage.getPaperdollItemVisualId(slot));
			}

			buffer.writeShort(charInfoPackage.getEnchantEffect(6));
			buffer.writeShort(charInfoPackage.getEnchantEffect(11));
			buffer.writeShort(charInfoPackage.getEnchantEffect(1));
			buffer.writeShort(charInfoPackage.getEnchantEffect(10));
			buffer.writeShort(charInfoPackage.getEnchantEffect(12));
			buffer.writeInt(charInfoPackage.getHairStyle());
			buffer.writeInt(charInfoPackage.getHairColor());
			buffer.writeInt(charInfoPackage.getFace());
			buffer.writeDouble(charInfoPackage.getMaxHp());
			buffer.writeDouble(charInfoPackage.getMaxMp());
			if (charInfoPackage.getAccessLevel() > -1)
			{
				if (charInfoPackage.getDeleteTimer() > 0L)
				{
					buffer.writeInt((int) ((charInfoPackage.getDeleteTimer() - System.currentTimeMillis()) / 1000L));
				}
				else
				{
					buffer.writeInt(0);
				}
			}
			else
			{
				buffer.writeInt(-1);
			}

			buffer.writeInt(charInfoPackage.getClassId());
			buffer.writeInt(ix == this._activeId);
			buffer.writeByte(charInfoPackage.getEnchantEffect(5) > 127 ? 127 : charInfoPackage.getEnchantEffect(5));
			VariationInstance augment = charInfoPackage.getAugmentation();
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

			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeInt(0);
			buffer.writeDouble(0.0);
			buffer.writeDouble(0.0);
			buffer.writeInt(charInfoPackage.getVitalityPoints());
			buffer.writeInt((int) RatesConfig.RATE_VITALITY_EXP_MULTIPLIER * 100);
			buffer.writeInt(charInfoPackage.getVitalityItemsUsed());
			buffer.writeInt(charInfoPackage.getAccessLevel() != -100);
			buffer.writeByte(charInfoPackage.isNoble());
			buffer.writeByte(Hero.getInstance().isHero(charInfoPackage.getObjectId()) ? 2 : 0);
			buffer.writeByte(charInfoPackage.isHairAccessoryEnabled());
			buffer.writeInt(0);
			buffer.writeInt((int) (charInfoPackage.getLastAccess() / 1000L));
			buffer.writeByte(0);
			switch (baseClassId)
			{
				case 247:
				case 248:
				case 249:
				case 250:
				case 251:
				case 252:
				case 253:
				case 254:
					buffer.writeInt(charInfoPackage.getHairColor() - 1);
					break;
				default:
					buffer.writeInt(charInfoPackage.getHairColor() + 1);
			}

			buffer.writeByte(charInfoPackage.getClassId() == 217 ? 1 : (charInfoPackage.getClassId() == 218 ? 2 : (charInfoPackage.getClassId() == 219 ? 3 : (charInfoPackage.getClassId() == 220 ? 4 : 0))));
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeLong(0L);
			buffer.writeLong(0L);
			buffer.writeLong(0L);
			buffer.writeLong(0L);
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}

	private static List<CharacterInfoHolder> loadCharacterSelectInfo(String loginName)
	{
		List<CharacterInfoHolder> characterList = new LinkedList<>();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE account_name=? ORDER BY createDate");)
		{
			statement.setString(1, loginName);

			try (ResultSet charList = statement.executeQuery())
			{
				while (charList.next())
				{
					CharacterInfoHolder charInfopackage = restoreChar(charList);
					if (charInfopackage != null)
					{
						characterList.add(charInfopackage);
						if (OfflineTradeConfig.OFFLINE_DISCONNECT_SAME_ACCOUNT)
						{
							Player player = World.getInstance().getPlayer(charInfopackage.getObjectId());
							if (player != null && player.isInStoreMode())
							{
								Disconnection.of(player).storeAndDelete();
								continue;
							}
						}

						if (OfflinePlayConfig.OFFLINE_PLAY_DISCONNECT_SAME_ACCOUNT)
						{
							Player player = World.getInstance().getPlayer(charInfopackage.getObjectId());
							if (player != null && player.isOfflinePlay())
							{
								Disconnection.of(player).storeAndDelete();
							}
						}
					}
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, "Could not restore char info: " + var14.getMessage(), var14);
		}

		return characterList;
	}

	private static void loadCharacterSubclassInfo(CharacterInfoHolder charInfopackage, int objectId, int activeClassId)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT exp, sp, level, vitality_points FROM character_subclasses WHERE charId=? AND class_id=? ORDER BY charId");)
		{
			statement.setInt(1, objectId);
			statement.setInt(2, activeClassId);

			try (ResultSet charList = statement.executeQuery())
			{
				if (charList.next())
				{
					charInfopackage.setExp(charList.getLong("exp"));
					charInfopackage.setSp(charList.getLong("sp"));
					charInfopackage.setLevel(charList.getInt("level"));
					charInfopackage.setVitalityPoints(charList.getInt("vitality_points"));
				}
			}
		}
		catch (Exception var14)
		{
			LOGGER.log(Level.WARNING, "Could not restore char subclass info: " + var14.getMessage(), var14);
		}
	}

	private static CharacterInfoHolder restoreChar(ResultSet chardata) throws Exception
	{
		int objectId = chardata.getInt("charId");
		String name = chardata.getString("char_name");
		long deletetime = chardata.getLong("deletetime");
		if (deletetime > 0L && System.currentTimeMillis() > deletetime)
		{
			Clan clan = ClanTable.getInstance().getClan(chardata.getInt("clanid"));
			if (clan != null)
			{
				clan.removeClanMember(objectId, 0L);
			}

			GameClient.deleteCharByObjId(objectId);
			return null;
		}
		CharacterInfoHolder charInfopackage = new CharacterInfoHolder(objectId, name);
		charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
		charInfopackage.setLevel(chardata.getInt("level"));
		charInfopackage.setMaxHp(chardata.getInt("maxhp"));
		charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
		charInfopackage.setMaxMp(chardata.getInt("maxmp"));
		charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
		charInfopackage.setReputation(chardata.getInt("reputation"));
		charInfopackage.setPkKills(chardata.getInt("pkkills"));
		charInfopackage.setPvPKills(chardata.getInt("pvpkills"));
		charInfopackage.setFace(chardata.getInt("face"));
		charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
		charInfopackage.setHairColor(chardata.getInt("haircolor"));
		charInfopackage.setSex(chardata.getInt("sex"));
		charInfopackage.setExp(chardata.getLong("exp"));
		charInfopackage.setSp(chardata.getLong("sp"));
		charInfopackage.setVitalityPoints(chardata.getInt("vitality_points"));
		charInfopackage.setClanId(chardata.getInt("clanid"));
		charInfopackage.setRace(chardata.getInt("race"));
		int baseClassId = chardata.getInt("base_class");
		int activeClassId = chardata.getInt("classid");
		charInfopackage.setX(chardata.getInt("x"));
		charInfopackage.setY(chardata.getInt("y"));
		charInfopackage.setZ(chardata.getInt("z"));
		int faction = chardata.getInt("faction");
		if (faction == 1)
		{
			charInfopackage.setGood();
		}

		if (faction == 2)
		{
			charInfopackage.setEvil();
		}

		if (MultilingualSupportConfig.MULTILANG_ENABLE)
		{
			String lang = chardata.getString("language");
			if (!MultilingualSupportConfig.MULTILANG_ALLOWED.contains(lang))
			{
				lang = MultilingualSupportConfig.MULTILANG_DEFAULT;
			}

			charInfopackage.setHtmlPrefix("data/lang/" + lang + "/");
		}

		if (baseClassId != activeClassId)
		{
			loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
		}

		charInfopackage.setClassId(activeClassId);
		int weaponObjId = charInfopackage.getPaperdollObjectId(5);
		if (weaponObjId < 1)
		{
			weaponObjId = charInfopackage.getPaperdollObjectId(5);
		}

		if (weaponObjId > 0)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT mineralId,option1,option2,option3 FROM item_variations WHERE itemId=?");)
			{
				statement.setInt(1, weaponObjId);

				try (ResultSet result = statement.executeQuery())
				{
					if (result.next())
					{
						int mineralId = result.getInt("mineralId");
						int option1 = result.getInt("option1");
						int option2 = result.getInt("option2");
						int option3 = result.getInt("option3");
						if (option1 > 0 && option2 > 0 && option3 > 0)
						{
							charInfopackage.setAugmentation(new VariationInstance(mineralId, option1, option2, option3));
						}
					}
				}
			}
			catch (Exception var23)
			{
				LOGGER.log(Level.WARNING, "Could not restore augmentation info: " + var23.getMessage(), var23);
			}
		}

		if (baseClassId == 0 && activeClassId > 0)
		{
			charInfopackage.setBaseClassId(activeClassId);
		}
		else
		{
			charInfopackage.setBaseClassId(baseClassId);
		}

		charInfopackage.setDeleteTimer(deletetime);
		charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
		charInfopackage.setNoble(chardata.getInt("nobless") == 1);
		return charInfopackage;
	}

	@Override
	public int[] getPaperdollOrder()
	{
		return PAPERDOLL_ORDER;
	}

	@Override
	public int[] getPaperdollOrderVisualId()
	{
		return PAPERDOLL_ORDER_VISUAL_ID;
	}
}
