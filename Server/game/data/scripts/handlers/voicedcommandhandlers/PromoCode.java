package handlers.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;

/**
 * Voiced command .code XXXX para resgatar codigos promocionais/referral.
 * Codigos criados no painel do site, armazenados em promo_codes.
 */
public class PromoCode implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "code" };

	@Override
	public String[] getCommandList()
	{
		return VOICED_COMMANDS;
	}

	@Override
	public boolean onCommand(String command, Player player, String params)
	{
		if (params == null || params.trim().isEmpty())
		{
			player.sendMessage("Uso: .code SEUCODIGO");
			player.sendMessage("Exemplo: .code STREAMER10");
			return true;
		}

		String code = params.trim().toUpperCase();
		redeemCode(player, code);
		return true;
	}

	private void redeemCode(Player player, String code)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("SELECT items, active, max_uses, uses FROM promo_codes WHERE code=?"))
			{
				ps.setString(1, code);
				try (ResultSet rs = ps.executeQuery())
				{
					if (!rs.next())
					{
						player.sendMessage("[Codigo] Codigo '" + code + "' invalido.");
						return;
					}
					if (rs.getInt("active") == 0)
					{
						player.sendMessage("[Codigo] Este codigo nao esta ativo.");
						return;
					}
					int maxUses = rs.getInt("max_uses");
					int uses = rs.getInt("uses");
					if (maxUses > 0 && uses >= maxUses)
					{
						player.sendMessage("[Codigo] Este codigo ja atingiu o limite de usos.");
						return;
					}
					String items = rs.getString("items");

					try (PreparedStatement ps2 = con.prepareStatement("SELECT 1 FROM promo_redeemed WHERE code=? AND account_name=?"))
					{
						ps2.setString(1, code);
						ps2.setString(2, player.getAccountName());
						try (ResultSet rs2 = ps2.executeQuery())
						{
							if (rs2.next())
							{
								player.sendMessage("[Codigo] Voce ja resgatou este codigo.");
								return;
							}
						}
					}

					// Entrega os itens
					for (String entry : items.split(";"))
					{
						String[] parts = entry.trim().split(":");
						if (parts.length == 2)
						{
							try
							{
								int itemId = Integer.parseInt(parts[0].trim());
								long count = Long.parseLong(parts[1].trim());
								player.addItem(ItemProcessType.REWARD, itemId, count, player, true);
							}
							catch (NumberFormatException ignored)
							{
							}
						}
					}

					// Registra resgate
					try (PreparedStatement ps3 = con.prepareStatement("INSERT INTO promo_redeemed (code, account_name, redeemed_at) VALUES (?,?,?)"))
					{
						ps3.setString(1, code);
						ps3.setString(2, player.getAccountName());
						ps3.setLong(3, System.currentTimeMillis());
						ps3.executeUpdate();
					}

					// Incrementa contador
					try (PreparedStatement ps4 = con.prepareStatement("UPDATE promo_codes SET uses=uses+1 WHERE code=?"))
					{
						ps4.setString(1, code);
						ps4.executeUpdate();
					}

					player.sendMessage("[Codigo] '" + code + "' resgatado com sucesso! Verifique seu inventario.");
				}
			}
		}
		catch (Exception e)
		{
			player.sendMessage("[Codigo] Erro ao processar. Tente novamente.");
		}
	}
}
