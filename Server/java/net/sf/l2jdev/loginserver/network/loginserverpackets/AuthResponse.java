package net.sf.l2jdev.loginserver.network.loginserverpackets;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;
import net.sf.l2jdev.loginserver.GameServerTable;

public class AuthResponse extends BaseWritablePacket {
   public AuthResponse(int serverId) {
      this.writeByte(2);
      this.writeByte(serverId);
      this.writeString(GameServerTable.getInstance().getServerNameById(serverId));
   }
}
