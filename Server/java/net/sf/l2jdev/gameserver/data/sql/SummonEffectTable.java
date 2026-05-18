package net.sf.l2jdev.gameserver.data.sql;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class SummonEffectTable
{
	private final Map<Integer, Map<Integer, Map<Integer, Collection<SummonEffectTable.SummonEffect>>>> _servitorEffects = new ConcurrentHashMap<>();
	private final Map<Integer, Collection<SummonEffectTable.SummonEffect>> _petEffects = new ConcurrentHashMap<>();

	public Map<Integer, Map<Integer, Map<Integer, Collection<SummonEffectTable.SummonEffect>>>> getServitorEffectsOwner()
	{
		return this._servitorEffects;
	}

	public Map<Integer, Collection<SummonEffectTable.SummonEffect>> getServitorEffects(Player owner)
	{
		Map<Integer, Map<Integer, Collection<SummonEffectTable.SummonEffect>>> servitorMap = this._servitorEffects.get(owner.getObjectId());
		return servitorMap == null ? null : servitorMap.get(owner.getClassIndex());
	}

	public Map<Integer, Collection<SummonEffectTable.SummonEffect>> getPetEffects()
	{
		return this._petEffects;
	}

	public static SummonEffectTable getInstance()
	{
		return SummonEffectTable.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SummonEffectTable INSTANCE = new SummonEffectTable();
	}

	public static class SummonEffect
	{
		private final Skill _skill;
		private final int _effectCurTime;

		public SummonEffect(Skill skill, int effectCurTime)
		{
			this._skill = skill;
			this._effectCurTime = effectCurTime;
		}

		public Skill getSkill()
		{
			return this._skill;
		}

		public int getEffectCurTime()
		{
			return this._effectCurTime;
		}
	}
}
