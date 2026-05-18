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
package ai.bosses.Antharas;

import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.script.Script;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.serverpackets.OnEventTrigger;

/**
 * @author BAN-JDEV
 */
public class AntharasEntrance extends Script
{
	// Zone
	private static final ZoneType ANTHARAS_ENTRANCE_ZONE = ZoneManager.getInstance().getZoneByName("giran_2321_001");

	// Other
	private static final int ANTHARAS_ENTRANCE_EMITTER_ID = 23210002;

	private AntharasEntrance()
	{
		addEnterZoneId(ANTHARAS_ENTRANCE_ZONE.getId());
	}

	@Override
	public void onEnterZone(Creature creature, ZoneType zone)
	{
		if (creature.isPlayer())
		{
			creature.sendPacket(new OnEventTrigger(ANTHARAS_ENTRANCE_EMITTER_ID, true));
		}
	}

	public static void main(String[] args)
	{
		new AntharasEntrance();
	}
}
