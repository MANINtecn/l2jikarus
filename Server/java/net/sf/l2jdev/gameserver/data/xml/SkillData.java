package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.StringUtil;
import net.sf.l2jdev.commons.util.TraceUtil;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.handler.EffectHandler;
import net.sf.l2jdev.gameserver.handler.SkillConditionHandler;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.EffectScope;
import net.sf.l2jdev.gameserver.model.skill.ISkillCondition;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.SkillConditionScope;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SkillData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SkillData.class.getName());
	private final Map<Long, Skill> _skillsByHash = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _maxSkillLevels = new ConcurrentHashMap<>();

	protected SkillData()
	{
		this.load();
	}

	@Override
	public boolean isValidating()
	{
		return false;
	}

	@Override
	public synchronized void load()
	{
		this._skillsByHash.clear();
		this._maxSkillLevels.clear();
		this.parseDatapackDirectory("data/stats/skills/", false);
		if (GeneralConfig.CUSTOM_SKILLS_LOAD)
		{
			this.parseDatapackDirectory("data/stats/skills/custom", false);
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._skillsByHash.size() + " Skills.");
	}

	public void reload()
	{
		this.load();
		SkillTreeData.getInstance().load();
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node node = document.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("list".equalsIgnoreCase(node.getNodeName()))
			{
				for (Node listNode = node.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
				{
					if ("skill".equalsIgnoreCase(listNode.getNodeName()))
					{
						NamedNodeMap attributes = listNode.getAttributes();
						Map<Integer, Set<Integer>> levels = new HashMap<>();
						Map<Integer, Map<Integer, StatSet>> skillInfo = new HashMap<>();
						StatSet generalSkillInfo = skillInfo.computeIfAbsent(-1, _ -> new HashMap<>()).computeIfAbsent(-1, _ -> new StatSet());
						parseAttributes(attributes, "", generalSkillInfo);
						Map<String, Map<Integer, Map<Integer, Object>>> variableValues = new HashMap<>();
						Map<EffectScope, List<SkillData.NamedParamInfo>> effectParamInfo = new EnumMap<>(EffectScope.class);
						Map<SkillConditionScope, List<SkillData.NamedParamInfo>> conditionParamInfo = new EnumMap<>(SkillConditionScope.class);

						for (Node skillNode = listNode.getFirstChild(); skillNode != null; skillNode = skillNode.getNextSibling())
						{
							String skillNodeName = skillNode.getNodeName();
							String i = skillNodeName.toLowerCase();
							switch (i)
							{
								case "variable":
									attributes = skillNode.getAttributes();
									String name = "@" + this.parseString(attributes, "name");
									variableValues.put(name, this.parseValues(skillNode));
								case "#text":
									break;
								default:
									EffectScope effectScope = EffectScope.findByXmlNodeName(skillNodeName);
									if (effectScope != null)
									{
										for (Node effectsNode = skillNode.getFirstChild(); effectsNode != null; effectsNode = effectsNode.getNextSibling())
										{
											if ("effect".equalsIgnoreCase(effectsNode.getNodeName()))
											{
												effectParamInfo.computeIfAbsent(effectScope, _ -> new LinkedList<>()).add(this.parseNamedParamInfo(effectsNode, variableValues));
											}
										}
									}
									else
									{
										SkillConditionScope skillConditionScope = SkillConditionScope.findByXmlNodeName(skillNodeName);
										if (skillConditionScope != null)
										{
											for (Node conditionNode = skillNode.getFirstChild(); conditionNode != null; conditionNode = conditionNode.getNextSibling())
											{
												if ("condition".equalsIgnoreCase(conditionNode.getNodeName()))
												{
													conditionParamInfo.computeIfAbsent(skillConditionScope, _ -> new LinkedList<>()).add(this.parseNamedParamInfo(conditionNode, variableValues));
												}
											}
										}
										else
										{
											this.parseInfo(skillNode, variableValues, skillInfo);
										}
									}
							}
						}

						int fromLevel = generalSkillInfo.getInt(".fromLevel", 1);
						int toLevel = generalSkillInfo.getInt(".toLevel", 0);

						for (int i = fromLevel; i <= toLevel; i++)
						{
							levels.computeIfAbsent(i, _ -> new HashSet<>()).add(0);
						}

						skillInfo.forEach((level, subLevelMap) -> {
							if (level != -1)
							{
								subLevelMap.forEach((subLevel, _) -> {
									if (subLevel != -1)
									{
										levels.computeIfAbsent(level, _ -> new HashSet<>()).add(subLevel);
									}
								});
							}
						});
						Stream.concat(effectParamInfo.values().stream(), conditionParamInfo.values().stream()).forEach(namedParamInfos -> namedParamInfos.forEach(namedParamInfo -> {
							namedParamInfo.getInfo().forEach((level, subLevelMap) -> {
								if (level != -1)
								{
									subLevelMap.forEach((subLevel, _) -> {
										if (subLevel != -1)
										{
											levels.computeIfAbsent(level, _ -> new HashSet<>()).add(subLevel);
										}
									});
								}
							});
							if (namedParamInfo.getFromLevel() != null && namedParamInfo.getToLevel() != null)
							{
								for (int ix = namedParamInfo.getFromLevel(); ix <= namedParamInfo.getToLevel(); ix++)
								{
									if (namedParamInfo.getFromSubLevel() != null && namedParamInfo.getToSubLevel() != null)
									{
										for (int j = namedParamInfo.getFromSubLevel(); j <= namedParamInfo.getToSubLevel(); j++)
										{
											levels.computeIfAbsent(ix, _ -> new HashSet<>()).add(j);
										}
									}
									else
									{
										levels.computeIfAbsent(ix, _ -> new HashSet<>()).add(0);
									}
								}
							}
						}));
						levels.forEach((level, subLevels) -> subLevels.forEach(subLevel -> {
							StatSet statSet = Optional.ofNullable(skillInfo.getOrDefault(level, Collections.emptyMap()).get(subLevel)).orElseGet(StatSet::new);
							skillInfo.getOrDefault(level, Collections.emptyMap()).getOrDefault(-1, StatSet.EMPTY_STATSET).getSet().forEach(statSet.getSet()::putIfAbsent);
							skillInfo.getOrDefault(-1, Collections.emptyMap()).getOrDefault(-1, StatSet.EMPTY_STATSET).getSet().forEach(statSet.getSet()::putIfAbsent);
							statSet.set(".level", level);
							statSet.set(".subLevel", subLevel);
							Skill skill = new Skill(statSet);
							SkillData.forEachNamedParamInfoParam(effectParamInfo, level, subLevel, (effectScopex, params) -> {
								String effectName = params.getString(".name");
								params.remove(".name");

								try
								{
									Function<StatSet, AbstractEffect> effectFunction = EffectHandler.getInstance().getHandlerFactory(effectName);
									if (effectFunction != null)
									{
										skill.addEffect(effectScopex, effectFunction.apply(params));
									}
									else
									{
										LOGGER.warning(this.getClass().getSimpleName() + ": Missing effect for Skill Id[" + statSet.getInt(".id") + "] Level[" + level + "] SubLevel[" + subLevel + "] Effect Scope[" + effectScopex + "] Effect Name[" + effectName + "]");
									}
								}
								catch (Exception var9x)
								{
									LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed loading effect for Skill Id[" + statSet.getInt(".id") + "] Level[" + level + "] SubLevel[" + subLevel + "] Effect Scope[" + effectScopex + "] Effect Name[" + effectName + "]", var9x);
								}
							});
							SkillData.forEachNamedParamInfoParam(conditionParamInfo, level, subLevel, (skillConditionScopex, params) -> {
								String conditionName = params.getString(".name");
								params.remove(".name");

								try
								{
									Function<StatSet, ISkillCondition> conditionFunction = SkillConditionHandler.getInstance().getHandlerFactory(conditionName);
									if (conditionFunction != null)
									{
										if (skill.isPassive())
										{
											if (skillConditionScopex != SkillConditionScope.PASSIVE)
											{
												LOGGER.warning(this.getClass().getSimpleName() + ": Non passive condition for passive Skill Id[" + statSet.getInt(".id") + "] Level[" + level + "] SubLevel[" + subLevel + "]");
											}
										}
										else if (skillConditionScopex == SkillConditionScope.PASSIVE)
										{
											LOGGER.warning(this.getClass().getSimpleName() + ": Passive condition for non passive Skill Id[" + statSet.getInt(".id") + "] Level[" + level + "] SubLevel[" + subLevel + "]");
										}

										skill.addCondition(skillConditionScopex, conditionFunction.apply(params));
									}
									else
									{
										LOGGER.warning(this.getClass().getSimpleName() + ": Missing condition for Skill Id[" + statSet.getInt(".id") + "] Level[" + level + "] SubLevel[" + subLevel + "] Effect Scope[" + skillConditionScopex + "] Effect Name[" + conditionName + "]");
									}
								}
								catch (Exception var9x)
								{
									LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed loading condition for Skill Id[" + statSet.getInt(".id") + "] Level[" + level + "] SubLevel[" + subLevel + "] Condition Scope[" + skillConditionScopex + "] Condition Name[" + conditionName + "]", var9x);
								}
							});
							this._skillsByHash.put(getSkillHashCode(skill), skill);
							this._maxSkillLevels.merge(skill.getId(), skill.getLevel(), Integer::max);
							if (skill.getSubLevel() % 1000 == 1)
							{
								EnchantSkillGroupsData.getInstance().addRouteForSkill(skill.getId(), skill.getLevel(), skill.getSubLevel());
							}
						}));
					}
				}
			}
		}
	}

	private static <T> void forEachNamedParamInfoParam(Map<T, List<SkillData.NamedParamInfo>> paramInfo, int level, int subLevel, BiConsumer<T, StatSet> consumer)
	{
		paramInfo.forEach((scope, namedParamInfos) -> namedParamInfos.forEach(namedParamInfo -> {
			if ((namedParamInfo.getFromLevel() == null && namedParamInfo.getToLevel() == null || namedParamInfo.getFromLevel() <= level && namedParamInfo.getToLevel() >= level) && (namedParamInfo.getFromSubLevel() == null && namedParamInfo.getToSubLevel() == null || namedParamInfo.getFromSubLevel() <= subLevel && namedParamInfo.getToSubLevel() >= subLevel))
			{
				StatSet params = Optional.ofNullable(namedParamInfo.getInfo().getOrDefault(level, Collections.emptyMap()).get(subLevel)).orElseGet(StatSet::new);
				namedParamInfo.getInfo().getOrDefault(level, Collections.emptyMap()).getOrDefault(-1, StatSet.EMPTY_STATSET).getSet().forEach(params.getSet()::putIfAbsent);
				namedParamInfo.getInfo().getOrDefault(-1, Collections.emptyMap()).getOrDefault(-1, StatSet.EMPTY_STATSET).getSet().forEach(params.getSet()::putIfAbsent);
				params.set(".name", namedParamInfo.getName());
				consumer.accept(scope, params);
			}
		}));
	}

	private SkillData.NamedParamInfo parseNamedParamInfo(Node node, Map<String, Map<Integer, Map<Integer, Object>>> variableValues)
	{
		NamedNodeMap attributes = node.getAttributes();
		String name = this.parseString(attributes, "name");
		Integer level = this.parseInteger(attributes, "level");
		Integer fromLevel = this.parseInteger(attributes, "fromLevel", level);
		Integer toLevel = this.parseInteger(attributes, "toLevel", level);
		Integer subLevel = this.parseInteger(attributes, "subLevel");
		Integer fromSubLevel = this.parseInteger(attributes, "fromSubLevel", subLevel);
		Integer toSubLevel = this.parseInteger(attributes, "toSubLevel", subLevel);
		Map<Integer, Map<Integer, StatSet>> info = new HashMap<>();

		for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (!n.getNodeName().equals("#text"))
			{
				this.parseInfo(n, variableValues, info);
			}
		}

		return new SkillData.NamedParamInfo(name, fromLevel, toLevel, fromSubLevel, toSubLevel, info);
	}

	private void parseInfo(Node node, Map<String, Map<Integer, Map<Integer, Object>>> variableValues, Map<Integer, Map<Integer, StatSet>> info)
	{
		Map<Integer, Map<Integer, Object>> values = this.parseValues(node);
		Object generalValue = values.getOrDefault(-1, Collections.emptyMap()).get(-1);
		if (generalValue != null)
		{
			String stringGeneralValue = String.valueOf(generalValue);
			if (stringGeneralValue.startsWith("@"))
			{
				Map<Integer, Map<Integer, Object>> variableValue = variableValues.get(stringGeneralValue);
				if (variableValue == null)
				{
					throw new IllegalArgumentException("undefined variable " + stringGeneralValue);
				}

				values = variableValue;
			}
		}

		values.forEach((level, subLevelMap) -> subLevelMap.forEach((subLevel, value) -> info.computeIfAbsent(level, _ -> new HashMap<>()).computeIfAbsent(subLevel, _ -> new StatSet()).set(node.getNodeName(), value)));
	}

	private Map<Integer, Map<Integer, Object>> parseValues(Node node)
	{
		Map<Integer, Map<Integer, Object>> values = new HashMap<>();
		Object parsedValue = this.parseValue(node, true, false, Collections.emptyMap());
		if (parsedValue != null)
		{
			values.computeIfAbsent(-1, _ -> new HashMap<>()).put(-1, parsedValue);
		}
		else
		{
			for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equalsIgnoreCase("value"))
				{
					NamedNodeMap attributes = n.getAttributes();
					Integer level = this.parseInteger(attributes, "level");
					if (level != null)
					{
						parsedValue = this.parseValue(n, false, false, Collections.emptyMap());
						if (parsedValue != null)
						{
							Integer subLevel = this.parseInteger(attributes, "subLevel", -1);
							values.computeIfAbsent(level, _ -> new HashMap<>()).put(subLevel, parsedValue);
						}
					}
					else
					{
						int fromLevel = this.parseInteger(attributes, "fromLevel");
						int toLevel = this.parseInteger(attributes, "toLevel");
						int fromSubLevel = this.parseInteger(attributes, "fromSubLevel", -1);
						int toSubLevel = this.parseInteger(attributes, "toSubLevel", -1);

						for (int i = fromLevel; i <= toLevel; i++)
						{
							for (int j = fromSubLevel; j <= toSubLevel; j++)
							{
								Map<Integer, Object> subValues = values.computeIfAbsent(i, _ -> new HashMap<>());
								Map<String, Double> variables = new HashMap<>();
								variables.put("index", i - fromLevel + 1.0);
								variables.put("subIndex", j - fromSubLevel + 1.0);
								Object base = values.getOrDefault(i, Collections.emptyMap()).get(-1);
								String baseText = String.valueOf(base);
								if (base != null && !(base instanceof StatSet) && !baseText.equalsIgnoreCase("true") && !baseText.equalsIgnoreCase("false"))
								{
									variables.put("base", Double.parseDouble(baseText));
								}

								parsedValue = this.parseValue(n, false, false, variables);
								if (parsedValue != null)
								{
									subValues.put(j, parsedValue);
								}
							}
						}
					}
				}
			}
		}

		return values;
	}

	protected Object parseValue(Node node, boolean blockValue, boolean parseAttributes, Map<String, Double> variables)
	{
		StatSet statSet = null;
		List<Object> list = null;
		Object text = null;
		if (parseAttributes && (!node.getNodeName().equals("value") || !blockValue) && node.getAttributes().getLength() > 0)
		{
			statSet = new StatSet();
			parseAttributes(node.getAttributes(), "", statSet, variables);
		}

		Node n;
		for (n = node.getFirstChild(); n != null; n = n.getNextSibling())
		{
			String nodeName = n.getNodeName();
			String var10 = n.getNodeName();
			switch (var10)
			{
				case "#text":
					String valuex = n.getNodeValue().trim();
					if (!valuex.isEmpty())
					{
						text = parseNodeValue(valuex, variables);
					}
					break;
				case "item":
					if (list == null)
					{
						list = new LinkedList<>();
					}

					Object itemValue = this.parseValue(n, false, true, variables);
					if (itemValue != null)
					{
						list.add(itemValue);
					}
					break;
				case "value":
					if (blockValue)
					{
						break;
					}
				default:
					Object value = this.parseValue(n, false, true, variables);
					if (value != null)
					{
						if (statSet == null)
						{
							statSet = new StatSet();
						}

						statSet.set(nodeName, value);
					}
			}
		}

		if (list != null)
		{
			if (text != null)
			{
				throw new IllegalArgumentException("Text and list in same node are not allowed. Node[" + n + "]");
			}

			if (statSet == null)
			{
				return list;
			}

			statSet.set(".", list);
		}

		if (text != null)
		{
			if (list != null)
			{
				throw new IllegalArgumentException("Text and list in same node are not allowed. Node[" + n + "]");
			}

			if (statSet == null)
			{
				return text;
			}

			statSet.set(".", text);
		}

		return statSet;
	}

	private static void parseAttributes(NamedNodeMap attributes, String prefix, StatSet statSet)
	{
		parseAttributes(attributes, prefix, statSet, Collections.emptyMap());
	}

	private static void parseAttributes(NamedNodeMap attributes, String prefix, StatSet statSet, Map<String, Double> variables)
	{
		for (int i = 0; i < attributes.getLength(); i++)
		{
			Node attributeNode = attributes.item(i);
			statSet.set(prefix + "." + attributeNode.getNodeName(), parseNodeValue(attributeNode.getNodeValue(), variables));
		}
	}

	private static Object parseNodeValue(String value, Map<String, Double> variables)
	{
		if (value.startsWith("{") && value.endsWith("}"))
		{
			String expression = value.substring(1, value.length() - 1).trim();
			if (expression.isEmpty())
			{
				throw new IllegalArgumentException("Empty expression inside {}.");
			}
			return evaluateExpression(expression, variables);
		}
		return value;
	}

	private static double evaluateExpression(String expression, Map<String, Double> variables)
	{
		String postfix = toPostfix(expression, variables);
		return evaluatePostfix(postfix);
	}

	private static String toPostfix(String expression, Map<String, Double> variables)
	{
		Deque<String> operators = new ArrayDeque<>();
		StringBuilder postfix = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(expression, "+-*/() ", true);
		boolean expectNumber = true;

		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken().trim();
			if (!token.isEmpty())
			{
				if (variables.containsKey(token))
				{
					token = variables.get(token).toString();
				}

				if (isNumeric(token))
				{
					postfix.append(token).append(' ');
					expectNumber = false;
				}
				else if (token.equals("-") && expectNumber)
				{
					String nextToken = tokenizer.hasMoreTokens() ? tokenizer.nextToken().trim() : null;
					if (nextToken == null || !isNumeric(nextToken))
					{
						throw new IllegalArgumentException("Invalid syntax near '-' in expression.");
					}

					postfix.append("-").append(nextToken).append(" ");
					expectNumber = false;
				}
				else if (isOperator(token))
				{
					while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token))
					{
						postfix.append(operators.pop()).append(' ');
					}

					operators.push(token);
					expectNumber = true;
				}
				else if (token.equals("("))
				{
					operators.push(token);
					expectNumber = true;
				}
				else if (token.equals(")"))
				{
					while (!operators.isEmpty() && !operators.peek().equals("("))
					{
						postfix.append(operators.pop()).append(' ');
					}

					operators.pop();
					expectNumber = false;
				}
			}
		}

		while (!operators.isEmpty())
		{
			postfix.append(operators.pop()).append(' ');
		}

		return postfix.toString().trim();
	}

	private static double evaluatePostfix(String postfix)
	{
		Deque<Double> stack = new ArrayDeque<>();
		StringTokenizer tokenizer = new StringTokenizer(postfix);

		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			if (isNumeric(token))
			{
				stack.push(Double.parseDouble(token));
			}
			else if (isOperator(token))
			{
				if (stack.size() < 2)
				{
					throw new IllegalStateException("Not enough operands for the operator: " + token);
				}

				double b = stack.pop();
				double a = stack.pop();
				switch (token)
				{
					case "+":
						stack.push(a + b);
						break;
					case "-":
						stack.push(a - b);
						break;
					case "*":
						stack.push(a * b);
						break;
					case "/":
						stack.push(a / b);
				}
			}
		}

		if (stack.size() != 1)
		{
			throw new IllegalStateException("The postfix expression did not evaluate to a single result.");
		}
		return stack.pop();
	}

	private static boolean isNumeric(String token)
	{
		try
		{
			Double.parseDouble(token);
			return true;
		}
		catch (NumberFormatException var3)
		{
			return false;
		}
	}

	private static boolean isOperator(String token)
	{
		return "+-*/".contains(token);
	}

	private static int precedence(String operator)
	{
		switch (operator)
		{
			case "+":
			case "-":
				return 1;
			case "*":
			case "/":
				return 2;
			default:
				return -1;
		}
	}

	public static long getSkillHashCode(Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel(), skill.getSubLevel());
	}

	public static long getSkillHashCode(int skillId, int skillLevel)
	{
		return getSkillHashCode(skillId, skillLevel, 0);
	}

	public static long getSkillHashCode(int skillId, int skillLevel, int subSkillLevel)
	{
		return skillId * 4294967296L + subSkillLevel * 65536 + skillLevel;
	}

	public Skill getSkill(int skillId, int level)
	{
		return this.getSkill(skillId, level, 0);
	}

	public Skill getSkill(int skillId, int level, int subLevel)
	{
		Skill result = this._skillsByHash.get(getSkillHashCode(skillId, level, subLevel));
		if (result != null)
		{
			return result;
		}
		int maxLevel = this.getMaxLevel(skillId);
		if (maxLevel > 0 && level > maxLevel)
		{
			LOGGER.warning(StringUtil.concat(this.getClass().getSimpleName(), ": Call to unexisting skill level id: ", String.valueOf(skillId), " requested level: ", String.valueOf(level), " max level: ", String.valueOf(maxLevel), ".", System.lineSeparator(), TraceUtil.getStackTrace(new Exception())));
			return this._skillsByHash.get(getSkillHashCode(skillId, maxLevel));
		}
		LOGGER.warning(StringUtil.concat(this.getClass().getSimpleName(), ": No skill info found for skill id ", String.valueOf(skillId), " and skill level ", String.valueOf(level), ".", System.lineSeparator(), TraceUtil.getStackTrace(new Exception())));
		return null;
	}

	public int getMaxLevel(int skillId)
	{
		Integer maxLevel = this._maxSkillLevels.get(skillId);
		return maxLevel != null ? maxLevel : 0;
	}

	public List<Skill> getSiegeSkills(boolean addNoble, boolean hasCastle)
	{
		List<Skill> result = new LinkedList<>();
		result.add(this._skillsByHash.get(getSkillHashCode(CommonSkill.SEAL_OF_RULER.getId(), 1)));
		result.add(this._skillsByHash.get(getSkillHashCode(247, 1)));
		if (addNoble)
		{
			result.add(this._skillsByHash.get(getSkillHashCode(326, 1)));
		}

		if (hasCastle)
		{
			result.add(this._skillsByHash.get(getSkillHashCode(844, 1)));
			result.add(this._skillsByHash.get(getSkillHashCode(845, 1)));
		}

		return result;
	}

	public static SkillData getInstance()
	{
		return SkillData.SingletonHolder.INSTANCE;
	}

	private class NamedParamInfo
	{
		private final String _name;
		private final Integer _fromLevel;
		private final Integer _toLevel;
		private final Integer _fromSubLevel;
		private final Integer _toSubLevel;
		private final Map<Integer, Map<Integer, StatSet>> _info;

		public NamedParamInfo(String name, Integer fromLevel, Integer toLevel, Integer fromSubLevel, Integer toSubLevel, Map<Integer, Map<Integer, StatSet>> info)
		{
			Objects.requireNonNull(SkillData.this);
			super();
			this._name = name;
			this._fromLevel = fromLevel;
			this._toLevel = toLevel;
			this._fromSubLevel = fromSubLevel;
			this._toSubLevel = toSubLevel;
			this._info = info;
		}

		public String getName()
		{
			return this._name;
		}

		public Integer getFromLevel()
		{
			return this._fromLevel;
		}

		public Integer getToLevel()
		{
			return this._toLevel;
		}

		public Integer getFromSubLevel()
		{
			return this._fromSubLevel;
		}

		public Integer getToSubLevel()
		{
			return this._toSubLevel;
		}

		public Map<Integer, Map<Integer, StatSet>> getInfo()
		{
			return this._info;
		}
	}

	private static class SingletonHolder
	{
		protected static final SkillData INSTANCE = new SkillData();
	}
}
