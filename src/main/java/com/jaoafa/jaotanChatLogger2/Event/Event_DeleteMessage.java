package com.jaoafa.jaotanChatLogger2.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;

import com.jaoafa.jaotanChatLogger2.Main;
import com.jaoafa.jaotanChatLogger2.Lib.Library;
import com.jaoafa.jaotanChatLogger2.Lib.MySQLDBManager;

import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class Event_DeleteMessage {
	@SubscribeEvent
	public void onDeleteMessage(MessageDeleteEvent event) {
		long msgid = event.getMessageIdLong();

		String hash = DigestUtils.sha1Hex(msgid + "_delete"); // メッセージID + _delete

		MySQLDBManager MySQLDBManager = Main.getMySQLDBManager();

		try {
			Connection conn = MySQLDBManager.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO `jaotanChatLogger2` (`msgid`, `type`, `machine`, `hash`, `created_at`) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP);",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, msgid); // msgid
			stmt.setInt(2, -1); // type
			stmt.setString(3, Library.getHostName()); // machine
			stmt.setString(4, hash); // hash
			stmt.executeUpdate();
			ResultSet res = stmt.getGeneratedKeys();
			if (res != null && res.next()) {
				System.out.println(
						"[" + Library.sdfFormat(new Date()) + "] DELETE | " + msgid + " | Successful | "
								+ res.getInt(1));
			} else {
				System.out.println(
						"[" + Library.sdfFormat(new Date()) + "] DELETE | " + msgid + " | Successful | Error | null");
			}
			res.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(
					"[" + Library.sdfFormat(new Date()) + "] DELETE | " + msgid + " | SQLException | "
							+ e.getMessage());
		}

		try {
			Connection conn = MySQLDBManager.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"UPDATE jaotanChatLogger2 SET deleted = ? WHERE msgid = ?");
			stmt.setBoolean(1, true); // deleted
			stmt.setLong(2, msgid); // msgid
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(
					"[" + Library.sdfFormat(new Date()) + "] DELETE | " + msgid + " | SQLException | "
							+ e.getMessage());
		}
	}
}
