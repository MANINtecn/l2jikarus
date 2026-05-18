package net.sf.l2jdev.loginserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.crypt.NewCrypt;
import net.sf.l2jdev.commons.network.base.BaseWritablePacket;
import net.sf.l2jdev.commons.util.TraceUtil;
import net.sf.l2jdev.loginserver.network.GameServerPacketHandler;
import net.sf.l2jdev.loginserver.network.ScrambledKeyPair;
import net.sf.l2jdev.loginserver.network.loginserverpackets.ChangePasswordResponse;
import net.sf.l2jdev.loginserver.network.loginserverpackets.InitLS;
import net.sf.l2jdev.loginserver.network.loginserverpackets.KickPlayer;
import net.sf.l2jdev.loginserver.network.loginserverpackets.LoginServerFail;
import net.sf.l2jdev.loginserver.network.loginserverpackets.RequestCharacters;

public class GameServerThread extends Thread {
   protected static final Logger LOGGER = Logger.getLogger(GameServerThread.class.getName());
   private final Set<String> _accountsOnGameServer = ConcurrentHashMap.newKeySet();
   private final Socket _socket;
   private InputStream _inputStream;
   private OutputStream _outputStream;
   private final RSAPublicKey _publicKey;
   private final RSAPrivateKey _privateKey;
   private NewCrypt _blowfishCipher;
   private final String _connectionIp;
   private String _connectionIpAddress;
   private GameServerTable.GameServerInfo _gameServerInfo;
   private GameServerPacketHandler.GameServerState _loginConnectionState = GameServerPacketHandler.GameServerState.CONNECTED;

   public GameServerThread(Socket socket) {
      this._socket = socket;
      this._connectionIp = socket.getInetAddress().getHostAddress();

      try {
         this._inputStream = this._socket.getInputStream();
         this._outputStream = new BufferedOutputStream(this._socket.getOutputStream());
      } catch (IOException var3) {
         LOGGER.warning(this.getClass().getSimpleName() + ": Failed to initialize network streams - " + var3.getMessage());
      }

      ScrambledKeyPair keyPair = LoginController.getInstance().getScrambledRSAKeyPair();
      this._privateKey = (RSAPrivateKey)keyPair.getPrivateKey();
      this._publicKey = (RSAPublicKey)keyPair.getPublicKey();
      this._blowfishCipher = new NewCrypt("_;v.]05-31!|+-%xT!^[$\u0000");
      this.setName(this.getClass().getSimpleName() + "-" + this.threadId() + "@" + this._connectionIp);
      this.start();
   }

   @Override
   public void run() {
      this._connectionIpAddress = this._socket.getInetAddress().getHostAddress();
      if (isBannedGameserverIP(this._connectionIpAddress)) {
         LOGGER.info("GameServerRegistration: IP Address " + this._connectionIpAddress + " is on the banned IP list. Connection rejected.");
         this.forceClose(1);
      } else {
         InitLS initializationPacket = new InitLS(this._publicKey.getModulus().toByteArray());

         try {
            this.sendPacket(initializationPacket);
            int lengthHighByte = 0;
            int lengthLowByte = 0;
            int packetLength = 0;
            boolean checksumValid = false;

            while (true) {
               lengthLowByte = this._inputStream.read();
               lengthHighByte = this._inputStream.read();
               packetLength = lengthHighByte * 256 + lengthLowByte;
               if (lengthHighByte < 0 || this._socket.isClosed()) {
                  LOGGER.finer("GameServerThread: Game server terminated the connection gracefully.");
                  return;
               }

               byte[] packetData = new byte[packetLength - 2];
               int totalBytesReceived = 0;
               int bytesRead = 0;

               for (int remainingBytes = packetLength - 2; bytesRead != -1 && totalBytesReceived < packetLength - 2; remainingBytes -= bytesRead) {
                  bytesRead = this._inputStream.read(packetData, totalBytesReceived, remainingBytes);
                  totalBytesReceived += bytesRead;
               }

               if (totalBytesReceived != packetLength - 2) {
                  LOGGER.warning(
                     "Incomplete packet received from game server. Expected "
                        + (packetLength - 2)
                        + " bytes, got "
                        + totalBytesReceived
                        + ". Closing connection."
                  );
                  return;
               }

               this._blowfishCipher.decrypt(packetData, 0, packetData.length);
               checksumValid = NewCrypt.verifyChecksum(packetData);
               if (!checksumValid) {
                  LOGGER.warning("Packet checksum verification failed. Possible data corruption or tampering detected. Closing connection.");
                  return;
               }

               GameServerPacketHandler.handlePacket(packetData, this);
            }
         } catch (IOException var13) {
            String serverIdentification = this.getServerId() != -1
               ? "[" + this.getServerId() + "] " + GameServerTable.getInstance().getServerNameById(this.getServerId())
               : "(" + this._connectionIpAddress + ")";
            String connectionLostMessage = "GameServer " + serverIdentification + ": Connection lost - " + var13.getMessage();
            LOGGER.info(connectionLostMessage);
         } finally {
            if (this.isAuthed()) {
               if (this._gameServerInfo != null) {
                  this._gameServerInfo.setDown();
               }

               LOGGER.info(
                  "Server ["
                     + this.getServerId()
                     + "] "
                     + GameServerTable.getInstance().getServerNameById(this.getServerId())
                     + " is now marked as disconnected."
               );
            }

            LoginServer.getInstance().getGameServerListener().removeGameServer(this);
            LoginServer.getInstance().getGameServerListener().removeFloodProtection(this._connectionIp);
         }
      }
   }

   public boolean hasAccountOnGameServer(String accountName) {
      return this._accountsOnGameServer.contains(accountName);
   }

   public int getPlayerCount() {
      return this._accountsOnGameServer.size();
   }

   public void attachGameServerInfo(GameServerTable.GameServerInfo gameServerInfo, int port, String[] hosts, int maxPlayers) {
      this.setGameServerInfo(gameServerInfo);
      gameServerInfo.setGameServerThread(this);
      gameServerInfo.setPort(port);
      this.setGameHosts(hosts);
      gameServerInfo.setMaxPlayers(maxPlayers);
      gameServerInfo.setAuthed(true);
   }

   public void forceClose(int reasonCode) {
      this.sendPacket(new LoginServerFail(reasonCode));

      try {
         this._socket.close();
      } catch (IOException var3) {
         LOGGER.finer("GameServerThread: Failed to close socket for banned server. Socket may already be closed - " + var3.getMessage());
      }
   }

   public static boolean isBannedGameserverIP(String ipAddress) {
      return false;
   }

   public void sendPacket(BaseWritablePacket packet) {
      if (this._blowfishCipher != null && this._socket != null && !this._socket.isClosed()) {
         try {
            packet.write();
            packet.writeInt(0);
            int dataSize = packet.getLength() - 2;
            int paddingNeeded = dataSize % 8;
            if (paddingNeeded != 0) {
               for (int i = paddingNeeded; i < 8; i++) {
                  packet.writeByte(0);
               }
            }

            byte[] packetData = packet.getSendableBytes();
            dataSize = packetData.length - 2;
            synchronized (this._outputStream) {
               NewCrypt.appendChecksum(packetData, 2, dataSize);
               this._blowfishCipher.crypt(packetData, 2, dataSize);
               this._outputStream.write(packetData);

               try {
                  this._outputStream.flush();
               } catch (IOException var8) {
                  LOGGER.finer("GameServerThread: Failed to flush output stream. Game server may have disconnected - " + var8.getMessage());
               }
            }
         } catch (IOException var10) {
            LOGGER.severe("GameServerThread: IOException occurred while sending packet " + packet.getClass().getSimpleName() + " - " + var10.getMessage());
            LOGGER.severe(TraceUtil.getStackTrace(var10));
         }
      }
   }

   public void kickPlayer(String accountName) {
      this.sendPacket(new KickPlayer(accountName));
   }

   public void requestCharacters(String accountName) {
      this.sendPacket(new RequestCharacters(accountName));
   }

   public void changePasswordResponse(String characterName, String responseMessage) {
      this.sendPacket(new ChangePasswordResponse(characterName, responseMessage));
   }

   public void setGameHosts(String[] hosts) {
      LOGGER.info("Updated Gameserver [" + this.getServerId() + "] " + GameServerTable.getInstance().getServerNameById(this.getServerId()) + " IP's:");
      this._gameServerInfo.clearServerAddresses();

      for (int i = 0; i < hosts.length; i += 2) {
         try {
            this._gameServerInfo.addServerAddress(hosts[i], hosts[i + 1]);
         } catch (Exception var6) {
            LOGGER.warning("Failed to resolve hostname \"" + hosts[i] + "\" - " + var6.getMessage());
         }
      }

      for (String serverAddress : this._gameServerInfo.getServerAddresses()) {
         LOGGER.info(serverAddress);
      }
   }

   public boolean isAuthed() {
      return this._gameServerInfo == null ? false : this._gameServerInfo.isAuthed();
   }

   public void setGameServerInfo(GameServerTable.GameServerInfo gameServerInfo) {
      this._gameServerInfo = gameServerInfo;
   }

   public GameServerTable.GameServerInfo getGameServerInfo() {
      return this._gameServerInfo;
   }

   public String getConnectionIpAddress() {
      return this._connectionIpAddress;
   }

   public int getServerId() {
      return this._gameServerInfo != null ? this._gameServerInfo.getId() : -1;
   }

   public RSAPrivateKey getPrivateKey() {
      return this._privateKey;
   }

   public void setBlowFish(NewCrypt blowfishCipher) {
      this._blowfishCipher = blowfishCipher;
   }

   public void addAccountOnGameServer(String accountName) {
      this._accountsOnGameServer.add(accountName);
      LoginController.getInstance().removeAuthedLoginClient(accountName);
   }

   public void removeAccountOnGameServer(String accountName) {
      this._accountsOnGameServer.remove(accountName);
      LoginController.getInstance().removeAuthedLoginClient(accountName);
   }

   public GameServerPacketHandler.GameServerState getLoginConnectionState() {
      return this._loginConnectionState;
   }

   public void setLoginConnectionState(GameServerPacketHandler.GameServerState state) {
      this._loginConnectionState = state;
   }
}
