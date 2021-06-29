package com.jaoafa.jaotanChatLogger2.Event;

import com.jaoafa.jaotanChatLogger2.Lib.Library;
import com.jaoafa.jaotanChatLogger2.Lib.MySQLDBManager;
import com.jaoafa.jaotanChatLogger2.Main;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.Date;

public class Event_DeleteMessage extends ListenerAdapter {
    public void onMessageDelete(MessageDeleteEvent event) {
        long msgid = event.getMessageIdLong();

        String hash = DigestUtils.sha1Hex(msgid + "_delete"); // メッセージID + _delete

        MySQLDBManager MySQLDBManager = Main.getMySQLDBManager();

        try {
            Connection conn = MySQLDBManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO jaotanChatLogger2 (`msgid`, `type`, `machine`, `hash`, `created_at`) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP);",
                Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, msgid); // msgid
                stmt.setInt(2, -1); // type
                stmt.setString(3, Library.getHostName()); // machine
                stmt.setString(4, hash); // hash
                stmt.executeUpdate();
                try (ResultSet res = stmt.getGeneratedKeys()) {
                    if (res != null && res.next()) {
                        System.out.printf("[%s] DELETE | %d | Successful | %d%n",
                            Library.sdfFormat(new Date()),
                            msgid,
                            res.getInt(1));
                    } else {
                        System.out.printf("[%s] DELETE | %d | Successful | Error | null%n",
                            Library.sdfFormat(new Date()),
                            msgid);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.printf("[%s] DELETE | %d | SQLException | %s%n",
                Library.sdfFormat(new Date()),
                msgid,
                e.getMessage());
        }

        try {
            Connection conn = MySQLDBManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE jaotanChatLogger2 SET deleted = ? WHERE msgid = ?")) {
                stmt.setBoolean(1, true); // deleted
                stmt.setLong(2, msgid); // msgid
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.printf("[%s] DELETE | %d | SQLException | %s%n",
                Library.sdfFormat(new Date()),
                msgid,
                e.getMessage());
        }
    }
}
