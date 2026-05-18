package net.sf.l2jdev.loginserver.network.gameserverpackets;

import java.util.logging.Logger;
import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.loginserver.LoginController;

public class PlayerTracert extends BaseReadablePacket {
   protected static final Logger LOGGER = Logger.getLogger(PlayerTracert.class.getName());

   public PlayerTracert(byte[] decrypt) {
      super(decrypt);
      this.readByte();
      String account = this.readString();
      String pcIp = this.readString();
      String hop1 = this.readString();
      String hop2 = this.readString();
      String hop3 = this.readString();
      String hop4 = this.readString();
      LoginController.getInstance().setAccountLastTracert(account, pcIp, hop1, hop2, hop3, hop4);
   }
}
