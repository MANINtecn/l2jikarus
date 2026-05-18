package net.sf.l2jdev.gameserver.model.vip;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import net.sf.l2jdev.gameserver.config.VipSystemConfig;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.VipData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLoad;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.ExBRNewIconCashBtnWnd;
import net.sf.l2jdev.gameserver.network.serverpackets.vip.ReceiveVipInfo;

public class VipManager
{
	private static final byte VIP_MAX_TIER = (byte) VipSystemConfig.VIP_SYSTEM_MAX_TIER;
	private final ConsumerEventListener _vipLoginListener = new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_LOGIN, event -> this.onVipLogin((OnPlayerLogin) event), this);

	protected VipManager()
	{
		if (VipSystemConfig.VIP_SYSTEM_ENABLED)
		{
			Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_LOAD, event -> this.onPlayerLoaded((OnPlayerLoad) event), this));
		}
	}

	private void onPlayerLoaded(OnPlayerLoad event)
	{
		Player player = event.getPlayer();
		player.setVipTier(this.getVipTier(player));
		if (player.getVipTier() > 0)
		{
			this.manageTier(player);
			player.addListener(this._vipLoginListener);
		}
		else
		{
			player.sendPacket(new ReceiveVipInfo(player));
			player.sendPacket(new ExBRNewIconCashBtnWnd((short) 0));
		}
	}

	protected boolean canReceiveGift(Player player)
	{
		if (!VipSystemConfig.VIP_SYSTEM_ENABLED)
		{
			return false;
		}
		return player.getVipTier() <= 0 ? false : player.getAccountVariables().getLong("Vip_Item_Bought", 0L) <= 0L;
	}

	private void onVipLogin(OnPlayerLogin event)
	{
		Player player = event.getPlayer();
		if (this.canReceiveGift(player))
		{
			player.sendPacket(new ExBRNewIconCashBtnWnd((short) 1));
		}
		else
		{
			player.sendPacket(new ExBRNewIconCashBtnWnd((short) 0));
		}

		player.removeListener(this._vipLoginListener);
		player.sendPacket(new ReceiveVipInfo(player));
	}

	public void manageTier(Player player)
	{
		if (!this.checkVipTierExpiration(player))
		{
			player.sendPacket(new ReceiveVipInfo(player));
		}

		if (player.getVipTier() > 1)
		{
			int oldSkillId = VipData.getInstance().getSkillId((byte) (player.getVipTier() - 1));
			if (oldSkillId > 0)
			{
				Skill oldSkill = SkillData.getInstance().getSkill(oldSkillId, 1);
				if (oldSkill != null)
				{
					player.removeSkill(oldSkill);
				}
			}
		}

		int skillId = VipData.getInstance().getSkillId(player.getVipTier());
		if (skillId > 0)
		{
			Skill skill = SkillData.getInstance().getSkill(skillId, 1);
			if (skill != null)
			{
				player.addSkill(skill);
			}
		}
	}

	public byte getVipTier(Player player)
	{
		return this.getVipInfo(player).getTier();
	}

	public byte getVipTier(long points)
	{
		byte temp = this.getVipInfo(points).getTier();
		if (temp > VIP_MAX_TIER)
		{
			temp = VIP_MAX_TIER;
		}

		return temp;
	}

	private VipInfo getVipInfo(Player player)
	{
		return this.getVipInfo(player.getVipPoints());
	}

	protected VipInfo getVipInfo(long points)
	{
		for (byte i = 0; i < VipData.getInstance().getVipTiers().size(); i++)
		{
			if (points < VipData.getInstance().getVipTiers().get(i).getPointsRequired())
			{
				byte temp = (byte) (i - 1);
				if (temp > VIP_MAX_TIER)
				{
					temp = VIP_MAX_TIER;
				}

				return VipData.getInstance().getVipTiers().get(temp);
			}
		}

		return VipData.getInstance().getVipTiers().get(VIP_MAX_TIER);
	}

	public long getPointsDepreciatedOnLevel(byte vipTier)
	{
		VipInfo tier = VipData.getInstance().getVipTiers().get(vipTier);
		return tier == null ? 0L : tier.getPointsDepreciated();
	}

	public long getPointsToLevel(byte vipTier)
	{
		VipInfo tier = VipData.getInstance().getVipTiers().get(vipTier);
		return tier == null ? 0L : tier.getPointsRequired();
	}

	public boolean checkVipTierExpiration(Player player)
	{
		Instant now = Instant.now();
		if (now.isAfter(Instant.ofEpochMilli(player.getVipTierExpiration())))
		{
			player.updateVipPoints(-this.getPointsDepreciatedOnLevel(player.getVipTier()));
			player.setVipTierExpiration(Instant.now().plus(30L, ChronoUnit.DAYS).toEpochMilli());
			return true;
		}
		return false;
	}

	public static VipManager getInstance()
	{
		return VipManager.Singleton.INSTANCE;
	}

	private static class Singleton
	{
		protected static final VipManager INSTANCE = new VipManager();
	}
}
