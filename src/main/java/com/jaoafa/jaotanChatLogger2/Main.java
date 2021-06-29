package com.jaoafa.jaotanChatLogger2;

import com.jaoafa.jaotanChatLogger2.Event.Event_DeleteMessage;
import com.jaoafa.jaotanChatLogger2.Event.Event_EditMessage;
import com.jaoafa.jaotanChatLogger2.Event.Event_NewMessage;
import com.jaoafa.jaotanChatLogger2.Event.Event_Ready;
import com.jaoafa.jaotanChatLogger2.Lib.Config;
import com.jaoafa.jaotanChatLogger2.Lib.MySQLDBManager;
import net.dv8tion.jda.api.JDABuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    private static MySQLDBManager manager = null;
    private static final List<Long> targetGuilds = new ArrayList<>();

    public static void main(String[] args) {
        loadConfig();
        loadTargetGuilds();
    }

    static void loadConfig() {
        convertConfigPropToJSON();

        Config config = new Config();
        manager = config.getMySQLDBManager();

        try {
            JDABuilder jdabuilder = JDABuilder.createDefault(config.getToken())
                .setAutoReconnect(true)
                .setBulkDeleteSplittingEnabled(false)
                .setContextEnabled(false);

            jdabuilder.addEventListeners(new Event_Ready());
            jdabuilder.addEventListeners(new Event_NewMessage());
            jdabuilder.addEventListeners(new Event_EditMessage());
            jdabuilder.addEventListeners(new Event_DeleteMessage());

            jdabuilder.build().awaitReady();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void convertConfigPropToJSON() {
        File before_file = new File("conf.properties");
        File after_file = new File("config.json");
        if (after_file.exists()) {
            System.out.println("config.jsonファイルが存在するため、それを利用します。");
            return;
        }
        if (!before_file.exists()) {
            System.out.println("config.jsonファイルが存在せず、またconf.propertiesファイルもありませんでした。終了します。");
            System.exit(1);
            return;
        }
        try {
            InputStream is = new FileInputStream(before_file);

            Properties props = new Properties();
            props.load(is);

            JSONObject object = new JSONObject();
            for (String key : props.stringPropertyNames()) {
                object.put(key, props.getProperty(key));
            }

            Files.writeString(after_file.toPath(), object.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void loadTargetGuilds() {
        try {
            String json = Files.readString(Path.of("targetGuilds.json"));
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                targetGuilds.add(array.getLong(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTargetGuild(long target) {
        return targetGuilds.stream()
            .anyMatch(guild_id -> guild_id == target);
    }

    public static MySQLDBManager getMySQLDBManager() {
        return manager;
    }
}
