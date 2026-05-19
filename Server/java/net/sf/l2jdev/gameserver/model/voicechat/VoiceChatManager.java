package net.sf.l2jdev.gameserver.model.voicechat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class VoiceChatManager
{
	private static final Logger LOGGER = Logger.getLogger(VoiceChatManager.class.getName());

	private static final int PORT                  = 7778;
	private static final int BROADCAST_INTERVAL_MS = 500;
	private static final int VOICE_RANGE           = 200;

	private final Set<String> _mutedPlayers = ConcurrentHashMap.newKeySet();
	private final List<ClientSession> _sessions = new CopyOnWriteArrayList<>();
	// Mapeia cada sessão TCP ao nome do personagem resolvido — evita que 2 contas
	// do mesmo IP recebam sempre o mesmo personagem (findFirst seria ambíguo)
	private final Map<ClientSession, String> _sessionPlayer = new ConcurrentHashMap<>();
	private ServerSocket _serverSocket;

	public static VoiceChatManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public void start()
	{
		try
		{
			_serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"));
			ThreadPool.execute(this::acceptLoop);
			ThreadPool.scheduleAtFixedRate(this::broadcast, 0, BROADCAST_INTERVAL_MS);
			LOGGER.info("VoiceChatManager: Listening on port " + PORT);
		}
		catch (IOException e)
		{
			LOGGER.warning("VoiceChatManager: Failed to start — " + e.getMessage());
		}
	}

	private void acceptLoop()
	{
		while (!_serverSocket.isClosed())
		{
			try
			{
				Socket socket = _serverSocket.accept();
				ClientSession session = new ClientSession(socket);
				_sessions.add(session);
			}
			catch (IOException e)
			{
				if (!_serverSocket.isClosed())
					LOGGER.warning("VoiceChatManager: Accept error — " + e.getMessage());
			}
		}
	}

	private void broadcast()
	{
		// Remove sessões mortas e limpa seus caches
		_sessions.removeIf(s ->
		{
			if (!s.isAlive())
			{
				_sessionPlayer.remove(s);
				return true;
			}
			return false;
		});

		for (ClientSession session : _sessions)
		{
			Player player = resolvePlayer(session);
			if (player == null || _mutedPlayers.contains(player.getName()))
				continue;

			// Jogadores próximos dentro do range
			List<Player> nearby = new ArrayList<>(
				World.getInstance().getVisibleObjectsInRange(player, Player.class, VOICE_RANGE));

			// Membros de party sempre incluídos, independente da distância
			var party = player.getParty();
			if (party != null)
			{
				for (Player member : party.getMembers())
				{
					if (member != player && !nearby.contains(member))
						nearby.add(member);
				}
			}

			StringBuilder sb = new StringBuilder();

			// SELF:nome:x,y,z
			sb.append("SELF:").append(player.getName()).append(':')
			  .append(player.getX()).append(',')
			  .append(player.getY()).append(',')
			  .append(player.getZ()).append('\n');

			// NEARBY:nome:x,y,z;nome:x,y,z
			sb.append("NEARBY:");
			boolean first = true;
			for (Player p : nearby)
			{
				if (p == player || _mutedPlayers.contains(p.getName()))
					continue;
				if (!first)
					sb.append(';');
				sb.append(p.getName()).append(':')
				  .append(p.getX()).append(',')
				  .append(p.getY()).append(',')
				  .append(p.getZ());
				first = false;
			}
			sb.append('\n');

			// PARTY:partyId:nome1;nome2  ou  PARTY: (sem party)
			if (party != null)
			{
				int partyId = party.getLeader().getObjectId();
				sb.append("PARTY:").append(partyId).append(':');
				boolean firstMember = true;
				for (Player member : party.getMembers())
				{
					if (member == player || _mutedPlayers.contains(member.getName()))
						continue;
					if (!firstMember)
						sb.append(';');
					sb.append(member.getName());
					firstMember = false;
				}
			}
			else
			{
				sb.append("PARTY:");
			}
			sb.append('\n');

			session.send(sb.toString());
		}
	}

	// Resolve o personagem para uma sessão TCP.
	// Usa cache para garantir que 2 sessões do mesmo IP recebam personagens distintos.
	private Player resolvePlayer(ClientSession session)
	{
		String ip = session.getIp();

		// Verifica se já temos um personagem cacheado para esta sessão
		String cached = _sessionPlayer.get(session);
		if (cached != null)
		{
			Player p = World.getInstance().getPlayers().stream()
				.filter(pl -> pl.getName().equals(cached) && pl.getClient() != null)
				.findFirst().orElse(null);
			if (p != null)
				return p;
			// Personagem saiu — limpa o cache desta sessão
			_sessionPlayer.remove(session);
		}

		// Personagens deste IP já atribuídos a outras sessões
		Set<String> taken = new HashSet<>(_sessionPlayer.values());

		Player p = World.getInstance().getPlayers().stream()
			.filter(pl -> pl.getClient() != null && ip.equals(pl.getClient().getIp()))
			.filter(pl -> !taken.contains(pl.getName()))
			.findFirst().orElse(null);

		if (p != null)
			_sessionPlayer.put(session, p.getName());

		return p;
	}

	public void toggleMute(String playerName)
	{
		if (_mutedPlayers.contains(playerName))
			_mutedPlayers.remove(playerName);
		else
			_mutedPlayers.add(playerName);
	}

	public boolean isMuted(String playerName)
	{
		return _mutedPlayers.contains(playerName);
	}

	private static class ClientSession
	{
		private final Socket _socket;
		private final PrintWriter _writer;
		private final String _ip;

		ClientSession(Socket socket) throws IOException
		{
			_socket = socket;
			_ip = socket.getInetAddress().getHostAddress();
			OutputStream os = socket.getOutputStream();
			_writer = new PrintWriter(os, true);
		}

		void send(String data)
		{
			try
			{
				_writer.print(data);
				_writer.flush();
			}
			catch (Exception e)
			{
				close();
			}
		}

		boolean isAlive()
		{
			return _socket.isConnected() && !_socket.isClosed();
		}

		void close()
		{
			try { _socket.close(); } catch (IOException ignored) {}
		}

		String getIp()
		{
			return _ip;
		}
	}

	private static class SingletonHolder
	{
		static final VoiceChatManager INSTANCE = new VoiceChatManager();
	}
}