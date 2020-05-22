package com.jaoafa.jaotanChatLogger2.Command;

import java.util.Date;

import com.jaoafa.jaotanChatLogger2.Lib.Library;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class Command_jcl {
	@SubscribeEvent
	public void onNewMessage(MessageReceivedEvent event) {
		if (!event.isFromType(ChannelType.TEXT)) {
			return;
		}
		Guild guild = event.getGuild();
		MessageChannel channel = event.getChannel();
		User user = event.getAuthor();
		Message message = event.getMessage();

		if (!message.getContentRaw().startsWith("/jcl ")) {
			return;
		}
		if (message.getContentRaw().contains("\n")) {
			return;
		}

		String[] args = message.getContentRaw().split(" ");

		System.out.println(
				"[" + Library.sdfFormat(new Date()) + "] Cmd    | " + guild.getName() + "#" + channel.getName()
						+ " | " + user.getAsTag()
						+ " | " + message.getContentRaw());

		if (args[1].equalsIgnoreCase("search")) {
			// /jcl search <Text>
			channel.sendMessage(user.getAsMention() + ", 未実装").queue();
			return;
		}
		channel.sendMessage(user.getAsMention() + ", ```/jcl search <Text>```").queue();
	}
}
