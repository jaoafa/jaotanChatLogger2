package com.jaoafa.jaotanChatLogger2.Event;

import java.util.Date;

import com.jaoafa.jaotanChatLogger2.Main;
import com.jaoafa.jaotanChatLogger2.Lib.Library;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class Event_Ready {
	@SubscribeEvent
	public void onReadyEvent(ReadyEvent event) {
		System.out.println("Ready: " + event.getJDA().getSelfUser().getName() + "#"
				+ event.getJDA().getSelfUser().getDiscriminator());

		Main.setJDA(event.getJDA());

		TextChannel channel = event.getJDA().getTextChannelById(713308560423125016L);
		if (channel != null) {
			channel.sendMessage("**[" + Library.sdfFormat(new Date()) + " | " + Library.getHostName() + "]** "
					+ "Start Javajaotan").queue();
		}
		Runtime.getRuntime().addShutdownHook(new Thread(
				() -> {
					TextChannel _channel = event.getJDA().getTextChannelById(713308560423125016L);
					if (_channel != null) {
						_channel.sendMessage(
								"**[" + Library.sdfFormat(new Date()) + " | " + Library.getHostName() + "]** "
										+ "End Javajaotan")
								.queue();
					}
				}));
	}

}
