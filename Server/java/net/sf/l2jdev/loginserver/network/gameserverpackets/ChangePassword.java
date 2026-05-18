package net.sf.l2jdev.loginserver.network.gameserverpackets;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.logging.Logger;
import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.loginserver.GameServerTable;
import net.sf.l2jdev.loginserver.GameServerThread;

public class ChangePassword extends BaseReadablePacket {
   protected static final Logger LOGGER = Logger.getLogger(ChangePassword.class.getName());

   public ChangePassword(byte[] decrypt) {
      super(decrypt);
      this.readByte();
      String accountName = this.readString();
      String characterName = this.readString();
      String curpass = this.readString();
      String newpass = this.readString();
      GameServerThread gst = null;

      for (GameServerTable.GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values()) {
         if (gsi.getGameServerThread() != null && gsi.getGameServerThread().hasAccountOnGameServer(accountName)) {
            gst = gsi.getGameServerThread();
         }
      }

      if (gst != null) {
         if (curpass != null && newpass != null) {
            try {
               MessageDigest md = MessageDigest.getInstance("SHA");
               byte[] raw = md.digest(curpass.getBytes(StandardCharsets.UTF_8));
               String curpassEnc = Base64.getEncoder().encodeToString(raw);
               String pass = null;
               int passUpdated = 0;

               try (
                  Connection con = DatabaseFactory.getConnection();
                  PreparedStatement ps = con.prepareStatement("SELECT password FROM accounts WHERE login=?");
               ) {
                  ps.setString(1, accountName);

                  try (ResultSet rs = ps.executeQuery()) {
                     if (rs.next()) {
                        pass = rs.getString("password");
                     }
                  }
               }

               if (curpassEnc.equals(pass)) {
                  byte[] password = md.digest(newpass.getBytes(StandardCharsets.UTF_8));

                  try (
                     Connection con = DatabaseFactory.getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE accounts SET password=? WHERE login=?");
                  ) {
                     ps.setString(1, Base64.getEncoder().encodeToString(password));
                     ps.setString(2, accountName);
                     passUpdated = ps.executeUpdate();
                  }

                  LOGGER.info(
                     "The password for account " + accountName + " has been changed from " + curpassEnc + " to " + Base64.getEncoder().encodeToString(password)
                  );
                  if (passUpdated > 0) {
                     gst.changePasswordResponse(characterName, "You have successfully changed your password!");
                  } else {
                     gst.changePasswordResponse(characterName, "The password change was unsuccessful!");
                  }
               } else {
                  gst.changePasswordResponse(characterName, "The typed current password doesn't match with your current one.");
               }
            } catch (Exception var28) {
               LOGGER.warning("Error while changing password for account " + accountName + " requested by player " + characterName + "! " + var28);
            }
         } else {
            gst.changePasswordResponse(characterName, "Invalid password data! Try again.");
         }
      }
   }
}
