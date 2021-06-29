package com.jaoafa.jaotanChatLogger2.Event;

import com.jaoafa.jaotanChatLogger2.Lib.Library;
import com.jaoafa.jaotanChatLogger2.Lib.MySQLDBManager;
import com.jaoafa.jaotanChatLogger2.Main;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.stream.Collectors;

public class Event_EditMessage extends ListenerAdapter {
    public void onMessageUpdate(MessageUpdateEvent event) {
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
                System.out.printf("[%s] EDIT   | %s#%s | %s | Error | rawtext equaled.%n",
                    Library.sdfFormat(new Date()),
                    guild.getName(),
                    channel.getName(),
                    user.getAsTag());
                res.close();
                stmt.close();
                return;
            }
            res.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.printf("[%s] EDIT   | %s#%s | %s | SQLException | %s%n",
                Library.sdfFormat(new Date()),
                guild.getName(),
                channel.getName(),
                user.getAsTag(),
                e.getMessage());
        }

        String nickname = member != null ? member.getNickname() : null;
        String attachments = message.getAttachments().stream().map(Message.Attachment::getUrl).collect(Collectors.joining("\n"));
        if (attachments.isEmpty()) {
            attachments = null;
        }

        OffsetDateTime whenEdited = message.getTimeEdited();
        if (whenEdited == null) {
            return;
        }
        String hash = DigestUtils.sha1Hex(message.getIdLong() + "_" + whenEdited.toEpochSecond() + "_edit"); // メッセージID + _ + 編集時刻 + _edit

        try {
            Connection conn = MySQLDBManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO `jaotanChatLogger2` (`msgid`, `displaytext`, `rawtext`, `guild_name`, `guild_id`, `channel_name`, `channel_id`, `author_name`, `author_nickname`, `author_id`, `author_discriminator`, `author_bot`, `author_webhook`, `msgtype`, `type`, `attachments`, `machine`, `hash`, `timestamp`, `created_at`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);",
                Statement.RETURN_GENERATED_KEYS)) {
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
                try (ResultSet res = stmt.getGeneratedKeys()) {
                    if (res != null && res.next()) {
                        System.out.printf("[%s] EDIT   | %s#%s | %s | Successful | %d%n",
                            Library.sdfFormat(new Date()),
                            guild.getName(),
                            channel.getName(),
                            user.getAsTag(),
                            res.getInt(1));
                    } else {
                        System.out.printf("[%s] EDIT   | %s#%s | %s | Error | null%n",
                            Library.sdfFormat(new Date()),
                            guild.getName(),
                            channel.getName(),
                            user.getAsTag());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.printf("[%s] EDIT   | %s#%s | %s | SQLException | %s%n",
                Library.sdfFormat(new Date()),
                guild.getName(),
                channel.getName(),
                user.getAsTag(),
                e.getMessage());
        }
    }
}
