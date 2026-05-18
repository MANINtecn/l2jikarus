package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.EnchantSkillGroupsData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExEnchantSkillInfo extends ServerPacket
{
	private final Set<Integer> _routes;
	private final int _skillId;
	private final int _skillLevel;
	private final int _skillSubLevel;
	private final int _currentSubLevel;

	public ExEnchantSkillInfo(int skillId, int skillLevel, int skillSubLevel, int currentSubLevel)
	{
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._skillSubLevel = skillSubLevel;
		this._currentSubLevel = currentSubLevel;
		this._routes = EnchantSkillGroupsData.getInstance().getRouteForSkill(this._skillId, this._skillLevel);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_SKILL_INFO.writeId(this, buffer);
		buffer.writeInt(client.getPlayer().getReplacementSkill(this._skillId));
		buffer.writeShort(this._skillLevel);
		buffer.writeShort(this._skillSubLevel);
		buffer.writeInt(this._skillSubLevel % 1000 != EnchantSkillGroupsData.MAX_ENCHANT_LEVEL);
		buffer.writeInt(this._skillSubLevel > 1000);
		buffer.writeInt(this._routes.size());
		this._routes.forEach(route -> {
			int routeId = route / 1000;
			int currentRouteId = this._skillSubLevel / 1000;
			int subLevel = this._currentSubLevel > 0 ? route + this._currentSubLevel % 1000 - 1 : route;
			buffer.writeShort(this._skillLevel);
			buffer.writeShort(currentRouteId != routeId ? subLevel : Math.min(subLevel + 1, route + (EnchantSkillGroupsData.MAX_ENCHANT_LEVEL - 1)));
		});
	}
}
