package com.jaoafa.jaotanChatLogger2.Event;

import com.jaoafa.jaotanChatLogger2.Lib.Library;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Date;

public class Event_Ready extends ListenerAdapter {
    public void onReady(ReadyEvent event) {
        System.out.printf("Ready: %s#%s%n",
            event.getJDA().getSelfUser().getName(),
            event.getJDA().getSelfUser().getDiscriminator());

        TextChannel channel = event.getJDA().getTextChannelById(713308560423125016L);
        if (channel != null) {
            channel.sendMessage(String.format("**[%s | %s]** Start Javajaotan",
                Library.sdfFormat(new Date()),
                Library.getHostName())).queue();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(
            () -> {
                TextChannel _channel = event.getJDA().getTextChannelById(713308560423125016L);
                if (_channel != null) {
                    _channel.sendMessage(
                        String.format("**[%s | %s]** End Javajaotan", Library.sdfFormat(new Date()), Library.getHostName()))
                        .queue();
                }
            }));
    }

}
