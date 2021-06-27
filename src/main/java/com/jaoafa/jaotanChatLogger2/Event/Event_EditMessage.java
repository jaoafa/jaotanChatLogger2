package com.jaoafa.jaotanChatLogger2.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

import com.jaoafa.jaotanChatLogger2.Main;
import com.jaoafa.jaotanChatLogger2.Lib.Library;
import com.jaoafa.jaotanChatLogger2.Lib.MySQLDBManager;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class Event_EditMessage {
	@SubscribeEvent
	public void onEditMessage(MessageUpdateEvent event) {
		if (!event.isFromType(ChannelType.TEXT)) {
			return;
		}
		Guild guild = event.getGuild();
		MessageChannel channel = event.getChannel();
		Member member = event.getMember();
		User user = event.getAuthor();
		Message message = event.getMessage();

		if (!Main.isTargetGuild(guild.getIdLong())) {
			return;
		}

		MySQLDBManager MySQLDBManager = Main.getMySQLDBManager();

		// msgidと同じ行が存在するか。該当最終行と比べて同じだったら無視
		try {
			Connection conn = MySQLDBManager.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("SELECT * FROM jaotanChatLogger2 WHERE msgid = ? ORDER BY rowid DESC LIMIT 1");
			stmt.setLong(1, message.getIdLong());
			ResultSet res = stmt.executeQuery();
			if (res.next() && res.getString("rawtext").equals(message.getContentRaw())) {
				System.out.println(
						"[" + Library.sdfFormat(new Date()) + "] EDIT   | " + guild.getName() + "#" + channel.getName()
								+ " | " + user.getAsTag()
								+ " | Error | rawtext equaled.");
				res.close();
				stmt.close();
				return;
			}
			res.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(
					"[" + Library.sdfFormat(new Date()) + "] EDIT   | " + guild.getName() + "#" + channel.getName()
							+ " | " + user.getAsTag()
							+ " | SQLException | "
							+ e.getMessage());
		}

		String nickname = member != null ? member.getNickname() : null;
		String attachments = String.join("\n",
				message.getAttachments().stream().map(attachment -> attachment.getUrl()).collect(Collectors.toList()));
		if (attachments.isEmpty()) {
			attachments = null;
		}

		String hash = DigestUtils
				.sha1Hex(message.getIdLong() + "_" + message.getTimeEdited().toEpochSecond() + "_edit"); // メッセージID + _ + 編集時刻 + _edit

		try {
			Connection conn = MySQLDBManager.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO `jaotanChatLogger2` (`msgid`, `displaytext`, `rawtext`, `guild_name`, `guild_id`, `channel_name`, `channel_id`, `author_name`, `author_nickname`, `author_id`, `author_discriminator`, `author_bot`, `author_webhook`, `msgtype`, `type`, `attachments`, `machine`, `hash`, `timestamp`, `created_at`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, message.getIdLong()); // msgid
			stmt.setString(2, message.getContentDisplay()); // displaytext
			stmt.setString(3, message.getContentRaw()); // rawtext
			stmt.setString(4, guild.getName()); // guild_name
			stmt.setLong(5, guild.getIdLong()); // guild_id
			stmt.setString(6, channel.getName()); // channel_name
			stmt.setLong(7, channel.getIdLong()); // channel_id
			stmt.setString(8, user.getName()); // author_name
			stmt.setString(9, nickname); // author_nickname
			stmt.setLong(10, user.getIdLong()); // author_id
			stmt.setInt(11, Integer.parseInt(user.getDiscriminator())); // author_discriminator
			stmt.setBoolean(12, user.isBot()); // author_bot
			stmt.setBoolean(13, message.isWebhookMessage()); // author_webhook
			stmt.setString(14, message.getType().name()); // msgtype
			stmt.setInt(15, 1); // type | 0 = new, 1 = edit, -1 = delete
			stmt.setString(16, attachments); // attachments
			stmt.setString(17, Library.getHostName()); // machine
			stmt.setString(18, hash); // hash
			stmt.setTimestamp(19, Timestamp.from(message.getTimeCreated().toInstant())); // timestamp
			stmt.executeUpdate();
			ResultSet res = stmt.getGeneratedKeys();
			if (res != null && res.next()) {
				System.out.println(
						"[" + Library.sdfFormat(new Date()) + "] EDIT   | " + guild.getName() + "#" + channel.getName()
								+ " | " + user.getAsTag()
								+ " | Successful | "
								+ res.getInt(1));
			} else {
				System.out.println(
						"[" + Library.sdfFormat(new Date()) + "] EDIT   | " + guild.getName() + "#" + channel.getName()
								+ " | " + user.getAsTag()
								+ " | Error | null");
			}
			res.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(
					"[" + Library.sdfFormat(new Date()) + "] EDIT   | " + guild.getName() + "#" + channel.getName()
							+ " | " + user.getAsTag()
							+ " | SQLException | "
							+ e.getMessage());
		}
	}
}
