package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.handler.EffectHandler;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.options.OptionSkillHolder;
import net.sf.l2jdev.gameserver.model.options.OptionSkillType;
import net.sf.l2jdev.gameserver.model.options.Options;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import org.w3c.dom.Document;

public class OptionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(OptionData.class.getName());
	private static Options[] _options;
	private static Map<Integer, Options> _optionMap = new ConcurrentHashMap<>();

	protected OptionData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this.parseDatapackDirectory("data/stats/augmentation/options", false);
		_options = new Options[Collections.max(_optionMap.keySet()) + 1];

		for (Entry<Integer, Options> option : _optionMap.entrySet())
		{
			_options[option.getKey()] = option.getValue();
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _optionMap.size() + " options.");
		_optionMap.clear();
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "option", optionNode -> {
			int id = this.parseInteger(optionNode.getAttributes(), "id");
			Options option = new Options(id);
			this.forEach(optionNode, IXmlReader::isNode, innerNode -> {
				String s0$ = innerNode.getNodeName();
				switch (s0$)
				{
					case "effects":
						this.forEach(innerNode, "effect", effectNode -> {
							String name = this.parseString(effectNode.getAttributes(), "name");
							StatSet params = new StatSet();
							this.forEach(effectNode, IXmlReader::isNode, paramNode -> params.set(paramNode.getNodeName(), SkillData.getInstance().parseValue(paramNode, true, false, Collections.emptyMap())));
							option.addEffect(EffectHandler.getInstance().getHandlerFactory(name).apply(params));
						});
						break;
					case "active_skill":
						int skillIdxxxx = this.parseInteger(innerNode.getAttributes(), "id");
						int skillLevelxxxx = this.parseInteger(innerNode.getAttributes(), "level");
						Skill skillxxxx = SkillData.getInstance().getSkill(skillIdxxxx, skillLevelxxxx);
						if (skillxxxx != null)
						{
							option.addActiveSkill(skillxxxx);
						}
						else
						{
							LOGGER.info(this.getClass().getSimpleName() + ": Could not find skill " + skillIdxxxx + "(" + skillLevelxxxx + ") used by option " + id + ".");
						}
						break;
					case "passive_skill":
						int skillIdxxx = this.parseInteger(innerNode.getAttributes(), "id");
						int skillLevelxxx = this.parseInteger(innerNode.getAttributes(), "level");
						Skill skillxxx = SkillData.getInstance().getSkill(skillIdxxx, skillLevelxxx);
						if (skillxxx != null)
						{
							option.addPassiveSkill(skillxxx);
						}
						else
						{
							LOGGER.info(this.getClass().getSimpleName() + ": Could not find skill " + skillIdxxx + "(" + skillLevelxxx + ") used by option " + id + ".");
						}
						break;
					case "attack_skill":
						int skillIdxx = this.parseInteger(innerNode.getAttributes(), "id");
						int skillLevelxx = this.parseInteger(innerNode.getAttributes(), "level");
						Skill skillxx = SkillData.getInstance().getSkill(skillIdxx, skillLevelxx);
						if (skillxx != null)
						{
							option.addActivationSkill(new OptionSkillHolder(skillxx, this.parseDouble(innerNode.getAttributes(), "chance"), OptionSkillType.ATTACK));
						}
						else
						{
							LOGGER.info(this.getClass().getSimpleName() + ": Could not find skill " + skillIdxx + "(" + skillLevelxx + ") used by option " + id + ".");
						}
						break;
					case "magic_skill":
						int skillIdx = this.parseInteger(innerNode.getAttributes(), "id");
						int skillLevelx = this.parseInteger(innerNode.getAttributes(), "level");
						Skill skillx = SkillData.getInstance().getSkill(skillIdx, skillLevelx);
						if (skillx != null)
						{
							option.addActivationSkill(new OptionSkillHolder(skillx, this.parseDouble(innerNode.getAttributes(), "chance"), OptionSkillType.MAGIC));
						}
						else
						{
							LOGGER.info(this.getClass().getSimpleName() + ": Could not find skill " + skillIdx + "(" + skillLevelx + ") used by option " + id + ".");
						}
						break;
					case "critical_skill":
						int skillId = this.parseInteger(innerNode.getAttributes(), "id");
						int skillLevel = this.parseInteger(innerNode.getAttributes(), "level");
						Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
						if (skill != null)
						{
							option.addActivationSkill(new OptionSkillHolder(skill, this.parseDouble(innerNode.getAttributes(), "chance"), OptionSkillType.CRITICAL));
						}
						else
						{
							LOGGER.info(this.getClass().getSimpleName() + ": Could not find skill " + skillId + "(" + skillLevel + ") used by option " + id + ".");
						}
				}
			});
			_optionMap.put(option.getId(), option);
		}));
	}

	public Options getOptions(int id)
	{
		return id > -1 && _options.length > id ? _options[id] : null;
	}

	public static OptionData getInstance()
	{
		return OptionData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final OptionData INSTANCE = new OptionData();
	}
}
