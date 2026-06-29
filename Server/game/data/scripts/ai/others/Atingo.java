/*
 * Copyright (c) 2013 L2jBAN-JDEV
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ai.others;

import java.time.Duration;
import java.util.function.Consumer;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.creature.OnCreatureHpChange;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.script.Script;

/**
 * @author Berezkin Nikolay
 */
public class Atingo extends Script
{
	// NPCs
	private static final int ATINGO = 25914;
	private static final int SIN_EATER = 25924;
	private static final int FOX = 25992; // raposa - guardiao raro (1%)
	// 6 guardioes comuns (10% cada). Fox tratado separado (1%). Resto = Sin Eater.
	public static final int[] PETS =
	{
		25923, // Hawk
		25922, // Dragon
		25921, // Kookaburra
		25918, // Wolf
		25920, // Tiger
		25919  // Buffalo
	};

	// TODO: adicionar mais 3 zonas futuras (coletar coordenadas in-game):
	//   - Primeval Isle
	//   - Tower of Insolence
	//   - Orc Barracks

	// Zonas de spawn (gatilho): Atingo escolhe uma ZONA aleatoria e depois um ponto REAL dela.
	// Assim o jogador sabe a zona, mas nao o ponto exato.
	private static final Location[][] SPAWN_ZONES =
	{
		{ // Aden Royal Cemetery (gatilho 175385,20318,3276)
			new Location(190607, 21482, -3704),
			new Location(177035, 17021, -3384),
			new Location(177869, 23647, -3384),
			new Location(169660, 20328, -3356)
		},
		{ // Fire Dragon's Swamp (gatilho 146060,-13961,-4469)
			new Location(140284, -20833, -3159),
			new Location(142143, -25032, -2045),
			new Location(149969, -23586, -3159),
			new Location(140333, -10592, -4574),
			new Location(152047, -10097, -4515)
		},
		{ // Shifting Mirage (gatilho -39062,184098,-4016)
			new Location(-41452, 173771, -3785),
			new Location(-38441, 183351, -4001),
			new Location(-35145, 190888, -3666)
		},
		{ // Tayga Camp (gatilho -53885,138618,-2934)
			new Location(-56062, 140716, -2666),
			new Location(-52361, 138015, -2947),
			new Location(-52747, 144325, -2901)
		}
	};

	// Misc
	private static final Duration ATINGO_RESPAWN_DURATION = Duration.ofMinutes(30);

	public Atingo()
	{
		addSpawnId(ATINGO);
		addKillId(ATINGO);
	}

	// Spawna 1 Atingo em cada zona (num ponto aleatorio dentro dela).
	private void spawnAllZones()
	{
		for (Location[] zone : SPAWN_ZONES)
		{
			addSpawn(ATINGO, getRandomEntry(zone));
		}
	}

	// Respawna 1 Atingo numa zona aleatoria (substitui o que morreu).
	private void respawnOne()
	{
		final Location[] zone = getRandomEntry(SPAWN_ZONES);
		addSpawn(ATINGO, getRandomEntry(zone));
	}

	@Override
	public void onSpawn(Npc npc)
	{
		npc.addListener(new ConsumerEventListener(npc, EventType.ON_CREATURE_HP_CHANGE, (Consumer<OnCreatureHpChange>) this::onHpChange, this));
	}

	@Override
	protected void onLoad()
	{
		ThreadPool.schedule(() -> spawnAllZones(), ATINGO_RESPAWN_DURATION.toMillis());
		super.onLoad();
	}

	private void onHpChange(OnCreatureHpChange hpChangeEvent)
	{
		final Npc creature = hpChangeEvent.getCreature().asNpc();
		if ((creature.getScriptValue() == 0) && !creature.isDead() && creature.isInCombat() && (getRandom(100) < 40))
		{
			// Rate ponderado: 6 guardioes comuns 10% cada (0-599), Raposa 1% (600-609), Sin Eater ~39% (610-999).
			final int roll = getRandom(1000);
			final int petId;
			if (roll < 600)
			{
				petId = PETS[roll / 100];
			}
			else if (roll < 610)
			{
				petId = FOX;
			}
			else
			{
				petId = SIN_EATER;
			}
			final Npc pet = addSpawn(petId, GeoEngine.getInstance().getValidLocation(creature.getX(), creature.getY(), creature.getZ(), creature.getX() + 50, creature.getY() + 50, creature.getZ(), null));
			creature.setScriptValue(pet.getObjectId());
			pet.setRunning();
			final Creature target = creature.asAttackable().getMostHated();
			if (target != null)
			{
				pet.asAttackable().addDamageHate(target, 0, 999);
			}
		}
	}

	@Override
	public void onKill(Npc npc, Player killer, boolean isSummon)
	{
		ThreadPool.schedule(() -> respawnOne(), ATINGO_RESPAWN_DURATION.toMillis());
	}

	public static void main(String[] args)
	{
		new Atingo();
	}
}
