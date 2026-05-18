package net.sf.l2jdev.gameserver.util;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerState;
import net.sf.l2jdev.gameserver.model.conditions.Condition;
import net.sf.l2jdev.gameserver.model.conditions.ConditionCategoryType;
import net.sf.l2jdev.gameserver.model.conditions.ConditionChangeWeapon;
import net.sf.l2jdev.gameserver.model.conditions.ConditionGameChance;
import net.sf.l2jdev.gameserver.model.conditions.ConditionGameTime;
import net.sf.l2jdev.gameserver.model.conditions.ConditionLogicAnd;
import net.sf.l2jdev.gameserver.model.conditions.ConditionLogicNot;
import net.sf.l2jdev.gameserver.model.conditions.ConditionLogicOr;
import net.sf.l2jdev.gameserver.model.conditions.ConditionMinDistance;
import net.sf.l2jdev.gameserver.model.conditions.ConditionMinimumVitalityPoints;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerActiveEffectId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerActiveSkillId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerAgathionId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCallPc;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanCreateBase;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanEscape;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanRefuelAirship;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanResurrect;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanSummonPet;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanSummonServitor;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanSummonSiegeGolem;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanSweep;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanSwitchSubclass;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanTakeCastle;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanTakeFort;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanTransform;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCanUntransform;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCharges;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCheckAbnormal;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerClassIdRestriction;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCloakStatus;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerCp;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerDualclass;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerFlyMounted;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerHasCastle;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerHasClanHall;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerHasFort;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerHasFreeSummonPoints;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerHasFreeTeleportBookmarkSlots;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerHasPet;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerHasSummon;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerHp;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerImmobile;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerInInstance;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerInsideZoneId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerInstanceId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerInvSize;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerIsClanLeader;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerIsHero;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerIsInCombat;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerIsOnSide;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerIsPvpFlagged;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerLandingZone;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerLevel;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerLevelRange;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerMp;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerPkCount;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerPledgeClass;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerRace;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerRangeFromNpc;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerRangeFromSummonedNpc;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerServitorNpcId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerSex;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerSiegeSide;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerSouls;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerState;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerSubclass;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerTransformationId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerVehicleMounted;
import net.sf.l2jdev.gameserver.model.conditions.ConditionPlayerWeight;
import net.sf.l2jdev.gameserver.model.conditions.ConditionSiegeZone;
import net.sf.l2jdev.gameserver.model.conditions.ConditionSlotItemId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetAbnormalType;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetActiveEffectId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetActiveSkillId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetAggro;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetCheckCrtEffect;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetClassIdRestriction;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetInvSize;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetLevel;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetLevelRange;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetMyPartyExceptMe;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetNpcId;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetNpcType;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetPlayable;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetPlayer;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetRace;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetUsesWeaponKind;
import net.sf.l2jdev.gameserver.model.conditions.ConditionTargetWeight;
import net.sf.l2jdev.gameserver.model.conditions.ConditionUsingItemType;
import net.sf.l2jdev.gameserver.model.conditions.ConditionUsingSkill;
import net.sf.l2jdev.gameserver.model.conditions.ConditionUsingSlotType;
import net.sf.l2jdev.gameserver.model.conditions.ConditionWithSkill;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.type.ArmorType;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.siege.CastleSide;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SoulType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.stats.functions.FuncTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class DocumentBase
{
	protected final Logger LOGGER = Logger.getLogger(this.getClass().getName());
	private final File _file;
	protected final Map<String, String[]> _tables = new HashMap<>();

	protected DocumentBase(File pFile)
	{
		this._file = pFile;
	}

	public Document parse()
	{
		Document document = null;

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			document = factory.newDocumentBuilder().parse(this._file);
			this.parseDocument(document);
		}
		catch (Exception var3)
		{
			this.LOGGER.log(Level.SEVERE, "Error loading file " + this._file, var3);
		}

		return document;
	}

	protected abstract void parseDocument(Document var1);

	protected abstract StatSet getStatSet();

	protected abstract String getTableValue(String var1);

	protected abstract String getTableValue(String var1, int var2);

	protected void resetTable()
	{
		this._tables.clear();
	}

	protected void setTable(String name, String[] table)
	{
		this._tables.put(name, table);
	}

	protected void parseTemplate(Node node, Object template)
	{
		Condition condition = null;
		Node n = node.getFirstChild();
		if (n != null)
		{
			if ("conditions".equalsIgnoreCase(n.getNodeName()))
			{
				condition = this.parseCondition(n.getFirstChild(), template);
				Node msg = n.getAttributes().getNamedItem("msg");
				Node msgId = n.getAttributes().getNamedItem("msgId");
				if (condition != null && msg != null)
				{
					condition.setMessage(msg.getNodeValue());
				}
				else if (condition != null && msgId != null)
				{
					condition.setMessageId(Integer.decode(this.getValue(msgId.getNodeValue(), null)));
					Node addName = n.getAttributes().getNamedItem("addName");
					if (addName != null && Integer.decode(this.getValue(msgId.getNodeValue(), null)) > 0)
					{
						condition.addName();
					}
				}

				n = n.getNextSibling();
			}

			for (; n != null; n = n.getNextSibling())
			{
				String name = n.getNodeName().toLowerCase();
				switch (name)
				{
					case "add":
					case "sub":
					case "mul":
					case "div":
					case "set":
					case "enchant":
					case "enchanthp":
						this.attachFunc(n, template, name, condition);
						break;
				}
			}
		}
	}

	protected void attachFunc(Node n, Object template, String functionName, Condition attachCond)
	{
		Stat stat = Stat.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
		int order = -1;
		Node orderNode = n.getAttributes().getNamedItem("order");
		if (orderNode != null)
		{
			order = Integer.parseInt(orderNode.getNodeValue());
		}

		String valueString = n.getAttributes().getNamedItem("val").getNodeValue();
		double value;
		if (valueString.charAt(0) == '#')
		{
			value = Double.parseDouble(this.getTableValue(valueString));
		}
		else
		{
			value = Double.parseDouble(valueString);
		}

		Condition applyCond = this.parseCondition(n.getFirstChild(), template);
		FuncTemplate ft = new FuncTemplate(attachCond, applyCond, functionName, order, stat, value);
		if (template instanceof ItemTemplate)
		{
			((ItemTemplate) template).addFunctionTemplate(ft);
		}
		else
		{
			throw new RuntimeException("Attaching stat to a non-effect template [" + template + "]!!!");
		}
	}

	protected Condition parseCondition(Node node, Object template)
	{
		Node n = node;

		while (n != null && n.getNodeType() != 1)
		{
			n = n.getNextSibling();
		}

		Condition condition = null;
		if (n != null)
		{
			String var5 = n.getNodeName().toLowerCase();
			switch (var5)
			{
				case "and":
					condition = this.parseLogicAnd(n, template);
					break;
				case "or":
					condition = this.parseLogicOr(n, template);
					break;
				case "not":
					condition = this.parseLogicNot(n, template);
					break;
				case "player":
					condition = this.parsePlayerCondition(n, template);
					break;
				case "target":
					condition = this.parseTargetCondition(n, template);
					break;
				case "using":
					condition = this.parseUsingCondition(n);
					break;
				case "game":
					condition = this.parseGameCondition(n);
			}
		}

		return condition;
	}

	protected Condition parseLogicAnd(Node node, Object template)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();

		for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == 1)
			{
				cond.add(this.parseCondition(n, template));
			}
		}

		if (cond.conditions == null || cond.conditions.length == 0)
		{
			this.LOGGER.severe("Empty <and> condition in " + this._file);
		}

		return cond;
	}

	protected Condition parseLogicOr(Node node, Object template)
	{
		ConditionLogicOr cond = new ConditionLogicOr();

		for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == 1)
			{
				cond.add(this.parseCondition(n, template));
			}
		}

		if (cond.conditions == null || cond.conditions.length == 0)
		{
			this.LOGGER.severe("Empty <or> condition in " + this._file);
		}

		return cond;
	}

	protected Condition parseLogicNot(Node node, Object template)
	{
		for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == 1)
			{
				return new ConditionLogicNot(this.parseCondition(n, template));
			}
		}

		this.LOGGER.severe("Empty <not> condition in " + this._file);
		return null;
	}

	protected Condition parsePlayerCondition(Node n, Object template)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();

		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			String var7 = a.getNodeName().toLowerCase();
			switch (var7)
			{
				case "races":
					String[] racesVal = a.getNodeValue().split(",");
					Set<Race> races = EnumSet.noneOf(Race.class);
					int r = 0;

					for (; r < racesVal.length; r++)
					{
						if (racesVal[r] != null)
						{
							races.add(Race.valueOf(racesVal[r]));
						}
					}

					cond = this.joinAnd(cond, new ConditionPlayerRace(races));
					break;
				case "level":
					int lvl = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionPlayerLevel(lvl));
					break;
				case "levelrange":
					String[] range = this.getValue(a.getNodeValue(), template).split(";");
					if (range.length == 2)
					{
						int[] lvlRange = new int[]
						{
							Integer.decode(this.getValue(a.getNodeValue(), template).split(";")[0]),
							Integer.decode(this.getValue(a.getNodeValue(), template).split(";")[1])
						};
						cond = this.joinAnd(cond, new ConditionPlayerLevelRange(lvlRange));
					}
					break;
				case "resting":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.RESTING, val));
					break;
				}
				case "flying":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.FLYING, val));
					break;
				}
				case "moving":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.MOVING, val));
					break;
				}
				case "running":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.RUNNING, val));
					break;
				}
				case "standing":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.STANDING, val));
					break;
				}
				case "behind":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.BEHIND, val));
					break;
				}
				case "front":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.FRONT, val));
					break;
				}
				case "chaotic":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.CHAOTIC, val));
					break;
				}
				case "olympiad":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.OLYMPIAD, val));
					break;
				}
				case "ishero":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerIsHero(val));
					break;
				}
				case "ispvpflagged":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerIsPvpFlagged(val));
					break;
				}
				case "transformationid":
					int id = Integer.parseInt(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerTransformationId(id));
					break;
				case "hp":
					int hp = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionPlayerHp(hp));
					break;
				case "mp":
					int mp = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionPlayerMp(mp));
					break;
				case "cp":
					int cp = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionPlayerCp(cp));
					break;
				case "pkcount":
					int expIndex = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionPlayerPkCount(expIndex));
					break;
				case "siegezone":
				{
					int value = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionSiegeZone(value, true));
					break;
				}
				case "siegeside":
				{
					int value = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionPlayerSiegeSide(value));
					break;
				}
				case "charges":
				{
					int value = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionPlayerCharges(value));
					break;
				}
				case "souls":
				{
					int value = Integer.decode(this.getValue(a.getNodeValue(), template));
					SoulType type = Enum.valueOf(SoulType.class, a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerSouls(value, type));
					break;
				}
				case "weight":
					int weight = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionPlayerWeight(weight));
					break;
				case "invsize":
					int size = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionPlayerInvSize(size));
					break;
				case "isclanleader":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerIsClanLeader(val));
					break;
				}
				case "pledgeclass":
					int pledgeClass = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionPlayerPledgeClass(pledgeClass));
					break;
				case "clanhall":
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					ArrayList<Integer> array = new ArrayList<>(st.countTokens());

					while (st.hasMoreTokens())
					{
						String item = st.nextToken().trim();
						array.add(Integer.decode(this.getValue(item, template)));
					}

					cond = this.joinAnd(cond, new ConditionPlayerHasClanHall(array));
					break;
				case "fort":
					int fort = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionPlayerHasFort(fort));
					break;
				case "castle":
					int castle = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionPlayerHasCastle(castle));
					break;
				case "sex":
					int sex = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionPlayerSex(sex));
					break;
				case "flymounted":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerFlyMounted(val));
					break;
				}
				case "vehiclemounted":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerVehicleMounted(val));
					break;
				}
				case "landingzone":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerLandingZone(val));
					break;
				}
				case "active_effect_id":
				{
					int effect_id = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id));
					break;
				}
				case "active_effect_id_lvl":
				{
					String val = this.getValue(a.getNodeValue(), template);
					int effect_id = Integer.decode(this.getValue(val.split(",")[0], template));
					int effect_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
					cond = this.joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id, effect_lvl));
					break;
				}
				case "active_skill_id":
				{
					int skill_id = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id));
					break;
				}
				case "active_skill_id_lvl":
				{
					String val = this.getValue(a.getNodeValue(), template);
					int skill_id = Integer.decode(this.getValue(val.split(",")[0], template));
					int skill_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
					cond = this.joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id, skill_lvl));
					break;
				}
				case "class_id_restriction":
				{
					StringTokenizer stClass = new StringTokenizer(a.getNodeValue(), ",");
					Set<Integer> classArray = new HashSet<>(stClass.countTokens());

					while (stClass.hasMoreTokens())
					{
						String item = stClass.nextToken().trim();
						classArray.add(Integer.decode(this.getValue(item, template)));
					}
					cond = this.joinAnd(cond, new ConditionPlayerClassIdRestriction(classArray));
					break;
				}
				case "subclass":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerSubclass(val));
					break;
				}
				case "dualclass":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerDualclass(val));
					break;
				}
				case "canswitchsubclass":
					cond = this.joinAnd(cond, new ConditionPlayerCanSwitchSubclass(Integer.decode(a.getNodeValue())));
					break;
				case "instanceid":
				{
					StringTokenizer stInst = new StringTokenizer(a.getNodeValue(), ",");
					Set<Integer> instSet = new HashSet<>(stInst.countTokens());
					while (stInst.hasMoreTokens())
					{
						String item = stInst.nextToken().trim();
						instSet.add(Integer.decode(this.getValue(item, template)));
					}
					cond = this.joinAnd(cond, new ConditionPlayerInstanceId(instSet));
					break;
				}
				case "agathionid":
					int agathionId = Integer.decode(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerAgathionId(agathionId));
					break;
				case "cloakstatus":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerCloakStatus(val));
					break;
				}
				case "hassummon":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionPlayerHasSummon(val));
					break;
				}
				case "haspet":
				{
					StringTokenizer stPet = new StringTokenizer(a.getNodeValue(), ",");
					ArrayList<Integer> petArray = new ArrayList<>(stPet.countTokens());
					while (stPet.hasMoreTokens())
					{
						String item = stPet.nextToken().trim();
						petArray.add(Integer.decode(this.getValue(item, template)));
					}
					cond = this.joinAnd(cond, new ConditionPlayerHasPet(petArray));
					break;
				}
				case "servitornpcid":
				{
					StringTokenizer stServ = new StringTokenizer(a.getNodeValue(), ",");
					ArrayList<Integer> servArray = new ArrayList<>(stServ.countTokens());
					while (stServ.hasMoreTokens())
					{
						String item = stServ.nextToken().trim();
						servArray.add(Integer.decode(this.getValue(item, null)));
					}
					cond = this.joinAnd(cond, new ConditionPlayerServitorNpcId(servArray));
					break;
				}
				case "npcidradius":
				{
					StringTokenizer stNpc = new StringTokenizer(a.getNodeValue(), ",");
					if (stNpc.countTokens() != 3)
					{
						break;
					}
					String[] ids = stNpc.nextToken().split(";");
					Set<Integer> npcIds = new HashSet<>(ids.length);
					for (String id2 : ids)
					{
						npcIds.add(Integer.parseInt(this.getValue(id2, template)));
					}
					int radius = Integer.parseInt(stNpc.nextToken());
					boolean val = Boolean.parseBoolean(stNpc.nextToken());
					cond = this.joinAnd(cond, new ConditionPlayerRangeFromNpc(npcIds, radius, val));
					break;
				}
				case "summonednpcidradius":
				{
					StringTokenizer stSummoned = new StringTokenizer(a.getNodeValue(), ",");
					if (stSummoned.countTokens() != 3)
					{
						break;
					}

					String[] ids = stSummoned.nextToken().split(";");
					Set<Integer> npcIds = new HashSet<>(ids.length);

					for (String id2 : ids)
					{
						npcIds.add(Integer.parseInt(this.getValue(id2, template)));
					}

					int radius = Integer.parseInt(stSummoned.nextToken());
					boolean val = Boolean.parseBoolean(stSummoned.nextToken());
					cond = this.joinAnd(cond, new ConditionPlayerRangeFromSummonedNpc(npcIds, radius, val));
					break;
				}
				case "callpc":
					cond = this.joinAnd(cond, new ConditionPlayerCallPc(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "cancreatebase":
					cond = this.joinAnd(cond, new ConditionPlayerCanCreateBase(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "canescape":
					cond = this.joinAnd(cond, new ConditionPlayerCanEscape(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "canrefuelairship":
					cond = this.joinAnd(cond, new ConditionPlayerCanRefuelAirship(Integer.parseInt(a.getNodeValue())));
					break;
				case "canresurrect":
					cond = this.joinAnd(cond, new ConditionPlayerCanResurrect(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "cansummonpet":
					cond = this.joinAnd(cond, new ConditionPlayerCanSummonPet(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "cansummonservitor":
					cond = this.joinAnd(cond, new ConditionPlayerCanSummonServitor(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "hasfreesummonpoints":
					cond = this.joinAnd(cond, new ConditionPlayerHasFreeSummonPoints(Integer.parseInt(a.getNodeValue())));
					break;
				case "hasfreeteleportbookmarkslots":
					cond = this.joinAnd(cond, new ConditionPlayerHasFreeTeleportBookmarkSlots(Integer.parseInt(a.getNodeValue())));
					break;
				case "cansummonsiegegolem":
					cond = this.joinAnd(cond, new ConditionPlayerCanSummonSiegeGolem(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "cansweep":
					cond = this.joinAnd(cond, new ConditionPlayerCanSweep(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "cantakecastle":
					cond = this.joinAnd(cond, new ConditionPlayerCanTakeCastle(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "cantakefort":
					cond = this.joinAnd(cond, new ConditionPlayerCanTakeFort(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "cantransform":
					cond = this.joinAnd(cond, new ConditionPlayerCanTransform(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "canuntransform":
					cond = this.joinAnd(cond, new ConditionPlayerCanUntransform(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "insidezoneid":
					StringTokenizer stZone = new StringTokenizer(a.getNodeValue(), ",");
					Set<Integer> zoneIdSet = new HashSet<>(stZone.countTokens());

					while (stZone.hasMoreTokens())
					{
						String item = stZone.nextToken().trim();
						zoneIdSet.add(Integer.decode(this.getValue(item, template)));
					}

					cond = this.joinAnd(cond, new ConditionPlayerInsideZoneId(zoneIdSet));
					break;
				case "checkabnormal":
				{
					String value = a.getNodeValue();
					if (value.contains(","))
					{
						String[] values = value.split(",");
						cond = this.joinAnd(cond, new ConditionPlayerCheckAbnormal(AbnormalType.valueOf(values[0]), Integer.decode(this.getValue(values[1], template))));
					}
					else
					{
						cond = this.joinAnd(cond, new ConditionPlayerCheckAbnormal(AbnormalType.valueOf(value)));
					}
					break;
				}
				case "categorytype":
				{
					String[] values = a.getNodeValue().split(",");
					Set<CategoryType> categoryTypes = new HashSet<>(values.length);

					for (String value : values)
					{
						categoryTypes.add(CategoryType.valueOf(this.getValue(value, template)));
					}

					cond = this.joinAnd(cond, new ConditionCategoryType(categoryTypes));
					break;
				}
				case "immobile":
					cond = this.joinAnd(cond, new ConditionPlayerImmobile(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "incombat":
					cond = this.joinAnd(cond, new ConditionPlayerIsInCombat(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "isonside":
					cond = this.joinAnd(cond, new ConditionPlayerIsOnSide(Enum.valueOf(CastleSide.class, a.getNodeValue())));
					break;
				case "ininstance":
					cond = this.joinAnd(cond, new ConditionPlayerInInstance(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "minimumvitalitypoints":
					int count = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionMinimumVitalityPoints(count));
			}
		}

		if (cond == null)
		{
			this.LOGGER.severe("Unrecognized <player> condition in " + this._file);
		}

		return cond;
	}

	protected Condition parseTargetCondition(Node n, Object template)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();

		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			String var7 = a.getNodeName().toLowerCase();
			switch (var7)
			{
				case "aggro":
				{
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionTargetAggro(val));
					break;
				}
				case "siegezone":
					int value = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionSiegeZone(value, false));
					break;
				case "level":
					int lvl = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionTargetLevel(lvl));
					break;
				case "levelrange":
					String[] range = this.getValue(a.getNodeValue(), template).split(";");
					if (range.length == 2)
					{
						int[] lvlRange = new int[]
						{
							Integer.decode(this.getValue(a.getNodeValue(), template).split(";")[0]),
							Integer.decode(this.getValue(a.getNodeValue(), template).split(";")[1])
						};
						cond = this.joinAnd(cond, new ConditionTargetLevelRange(lvlRange));
					}
					break;
				case "mypartyexceptme":
					cond = this.joinAnd(cond, new ConditionTargetMyPartyExceptMe(Boolean.parseBoolean(a.getNodeValue())));
					break;
				case "playable":
					cond = this.joinAnd(cond, new ConditionTargetPlayable());
					break;
				case "player":
					cond = this.joinAnd(cond, new ConditionTargetPlayer());
					break;
				case "class_id_restriction":
					StringTokenizer stClassTarget = new StringTokenizer(a.getNodeValue(), ",");
					Set<Integer> classIdSet = new HashSet<>(stClassTarget.countTokens());

					while (stClassTarget.hasMoreTokens())
					{
						String item = stClassTarget.nextToken().trim();
						classIdSet.add(Integer.decode(this.getValue(item, null)));
					}

					cond = this.joinAnd(cond, new ConditionTargetClassIdRestriction(classIdSet));
					break;
				case "active_effect_id":
				{
					int effect_id = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionTargetActiveEffectId(effect_id));
					break;
				}
				case "active_effect_id_lvl":
				{
					String val = this.getValue(a.getNodeValue(), template);
					int effect_id = Integer.decode(this.getValue(val.split(",")[0], template));
					int effect_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
					cond = this.joinAnd(cond, new ConditionTargetActiveEffectId(effect_id, effect_lvl));
					break;
				}
				case "active_skill_id":
				{
					int skill_id = Integer.decode(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
					break;
				}
				case "active_skill_id_lvl":
				{
					String val = this.getValue(a.getNodeValue(), template);
					int skill_id = Integer.decode(this.getValue(val.split(",")[0], template));
					int skill_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
					cond = this.joinAnd(cond, new ConditionTargetActiveSkillId(skill_id, skill_lvl));
					break;
				}
				case "abnormaltype":
					AbnormalType abnormalType = AbnormalType.getAbnormalType(this.getValue(a.getNodeValue(), template));
					cond = this.joinAnd(cond, new ConditionTargetAbnormalType(abnormalType));
					break;
				case "mindistance":
					int distance = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionMinDistance(distance));
					break;
				case "race":
					cond = this.joinAnd(cond, new ConditionTargetRace(Race.valueOf(a.getNodeValue())));
					break;
				case "using":
					int usingMask = 0;
					StringTokenizer stUsing = new StringTokenizer(a.getNodeValue(), ",");

					while (stUsing.hasMoreTokens())
					{
						String item = stUsing.nextToken().trim();

						for (WeaponType wt : WeaponType.values())
						{
							if (wt.name().equals(item))
							{
								usingMask |= wt.mask();
								break;
							}
						}

						for (ArmorType at : ArmorType.values())
						{
							if (at.name().equals(item))
							{
								usingMask |= at.mask();
								break;
							}
						}
					}

					cond = this.joinAnd(cond, new ConditionTargetUsesWeaponKind(usingMask));
					break;
				case "npcid":
					StringTokenizer stNpcId = new StringTokenizer(a.getNodeValue(), ",");
					Set<Integer> npcIdSet = new HashSet<>(stNpcId.countTokens());

					while (stNpcId.hasMoreTokens())
					{
						String item = stNpcId.nextToken().trim();
						npcIdSet.add(Integer.decode(this.getValue(item, null)));
					}

					cond = this.joinAnd(cond, new ConditionTargetNpcId(npcIdSet));
					break;
				case "npctype":
					String values = this.getValue(a.getNodeValue(), template).trim();
					String[] valuesSplit = values.split(",");
					InstanceType[] types = new InstanceType[valuesSplit.length];

					for (int j = 0; j < valuesSplit.length; j++)
					{
						InstanceType type = Enum.valueOf(InstanceType.class, valuesSplit[j]);
						if (type == null)
						{
							throw new IllegalArgumentException("Instance type not recognized: " + valuesSplit[j]);
						}

						types[j] = type;
					}

					cond = this.joinAnd(cond, new ConditionTargetNpcType(types));
					break;
				case "weight":
					int weight = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionTargetWeight(weight));
					break;
				case "invsize":
					int size = Integer.decode(this.getValue(a.getNodeValue(), null));
					cond = this.joinAnd(cond, new ConditionTargetInvSize(size));
					break;
				case "checkcrteffect":
					cond = this.joinAnd(cond, new ConditionTargetCheckCrtEffect(Boolean.parseBoolean(a.getNodeValue())));
			}
		}

		if (cond == null)
		{
			this.LOGGER.severe("Unrecognized <target> condition in " + this._file);
		}

		return cond;
	}

	protected Condition parseUsingCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();

		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			String var6 = a.getNodeName().toLowerCase();
			switch (var6)
			{
				case "kind":
					int kindMask = 0;
					StringTokenizer stKind = new StringTokenizer(a.getNodeValue(), ",");

					while (stKind.hasMoreTokens())
					{
						int oldKindMask = kindMask;
						String item = stKind.nextToken().trim();

						for (WeaponType wt : WeaponType.values())
						{
							if (wt.name().equals(item))
							{
								kindMask |= wt.mask();
							}
						}

						for (ArmorType at : ArmorType.values())
						{
							if (at.name().equals(item))
							{
								kindMask |= at.mask();
							}
						}

						if (oldKindMask == kindMask)
						{
							this.LOGGER.info("[parseUsingCondition=\"kind\"] Unknown item type name: " + item);
						}
					}

					cond = this.joinAnd(cond, new ConditionUsingItemType(kindMask));
					break;
				case "slot":
					int slotMask = 0;
					StringTokenizer stSlot = new StringTokenizer(a.getNodeValue(), ",");

					while (stSlot.hasMoreTokens())
					{
						int oldSlotMask = slotMask;
						String item = stSlot.nextToken().trim();
						BodyPart bodyPart = BodyPart.fromName(item);
						if (bodyPart != null)
						{
							slotMask = (int) (slotMask | bodyPart.getMask());
						}

						if (oldSlotMask == slotMask)
						{
							this.LOGGER.info("[parseUsingCondition=\"slot\"] Unknown item slot name: " + item);
						}
					}

					cond = this.joinAnd(cond, new ConditionUsingSlotType(slotMask));
					break;
				case "skill":
				{
					int id = Integer.parseInt(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionUsingSkill(id));
					break;
				}
				case "slotitem":
				{
					StringTokenizer stSlotItem = new StringTokenizer(a.getNodeValue(), ";");
					int id = Integer.parseInt(stSlotItem.nextToken().trim());
					int slot = Integer.parseInt(stSlotItem.nextToken().trim());
					int enchant = 0;
					if (stSlotItem.hasMoreTokens())
					{
						enchant = Integer.parseInt(stSlotItem.nextToken().trim());
					}

					cond = this.joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
					break;
				}
				case "weaponchange":
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = this.joinAnd(cond, new ConditionChangeWeapon(val));
			}
		}

		if (cond == null)
		{
			this.LOGGER.severe("Unrecognized <using> condition in " + this._file);
		}

		return cond;
	}

	protected Condition parseGameCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();

		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("skill".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = this.joinAnd(cond, new ConditionWithSkill(val));
			}

			if ("night".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = this.joinAnd(cond, new ConditionGameTime(val));
			}

			if ("chance".equalsIgnoreCase(a.getNodeName()))
			{
				int val = Integer.decode(this.getValue(a.getNodeValue(), null));
				cond = this.joinAnd(cond, new ConditionGameChance(val));
			}
		}

		if (cond == null)
		{
			this.LOGGER.severe("Unrecognized <game> condition in " + this._file);
		}

		return cond;
	}

	protected void parseTable(Node n)
	{
		NamedNodeMap attrs = n.getAttributes();
		String name = attrs.getNamedItem("name").getNodeValue();
		if (name.charAt(0) != '#')
		{
			throw new IllegalArgumentException("Table name must start with #");
		}
		StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
		List<String> array = new ArrayList<>(data.countTokens());

		while (data.hasMoreTokens())
		{
			array.add(data.nextToken());
		}

		this.setTable(name, array.toArray(new String[array.size()]));
	}

	protected void parseBeanSet(Node n, StatSet set, Integer level)
	{
		String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
		String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
		char ch = value.isEmpty() ? 32 : value.charAt(0);
		if (ch != '#' && ch != '-' && !Character.isDigit(ch))
		{
			set.set(name, value);
		}
		else
		{
			set.set(name, this.getValue(value, level));
		}
	}

	protected void setExtractableSkillData(StatSet set, String value)
	{
		set.set("capsuled_items_skill", value);
	}

	protected String getValue(String value, Object template)
	{
		if (value.charAt(0) == '#')
		{
			if (template instanceof Skill)
			{
				return this.getTableValue(value);
			}
			else if (template instanceof Integer)
			{
				return this.getTableValue(value, (Integer) template);
			}
			else
			{
				throw new IllegalStateException();
			}
		}
		return value;
	}

	protected Condition joinAnd(Condition cond, Condition c)
	{
		if (cond == null)
		{
			return c;
		}
		else if (cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		else
		{
			ConditionLogicAnd and = new ConditionLogicAnd();
			and.add(cond);
			and.add(c);
			return and;
		}
	}
}
