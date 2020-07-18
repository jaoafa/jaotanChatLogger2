package com.jaoafa.jaotanChatLogger2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;

import com.jaoafa.jaotanChatLogger2.Command.Command_jcl;
import com.jaoafa.jaotanChatLogger2.Event.Event_DeleteMessage;
import com.jaoafa.jaotanChatLogger2.Event.Event_EditMessage;
import com.jaoafa.jaotanChatLogger2.Event.Event_NewMessage;
import com.jaoafa.jaotanChatLogger2.Event.Event_Ready;
import com.jaoafa.jaotanChatLogger2.Lib.MySQLDBManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;

public class Main {
	private static String DiscordToken = null;
	private static MySQLDBManager MySQLDBManager = null;
	private static JDA JDA = null;
	private static List<Long> targetGuilds = new ArrayList<>();

	public static void main(String[] args) {
		loadConfig();
		loadTargetGuilds();

		try {
			JDABuilder jdabuilder = JDABuilder.createDefault(DiscordToken)
					.setAutoReconnect(true)
					.setBulkDeleteSplittingEnabled(false)
					.setContextEnabled(false)
					.setEventManager(new AnnotatedEventManager());

			jdabuilder.addEventListeners(new Event_Ready());
			jdabuilder.addEventListeners(new Event_NewMessage());
			jdabuilder.addEventListeners(new Event_EditMessage());
			jdabuilder.addEventListeners(new Event_DeleteMessage());
			jdabuilder.addEventListeners(new Command_jcl());

			JDA = jdabuilder.build().awaitReady();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	static void loadConfig() {
		Properties props;
		try {
			File f = new File("conf.properties");
			InputStream is = new FileInputStream(f);

			// プロパティファイルを読み込む
			props = new Properties();
			props.load(is);

			DiscordToken = props.getProperty("token");

			MySQLDBManager = new MySQLDBManager(
					props.getProperty("sqlserver"),
					props.getProperty("sqlport"),
					props.getProperty("sqldatabase"),
					props.getProperty("sqluser"),
					props.getProperty("sqlpassword"));
		} catch (FileNotFoundException e) {
			// ファイル生成
			props = new Properties();
			props.setProperty("token", "PLEASETOKEN");
			props.setProperty("sqlserver", "PLEASE");
			props.setProperty("sqlport", "PLEASE");
			props.setProperty("sqldatabase", "PLEASE");
			props.setProperty("sqluser", "PLEASE");
			props.setProperty("sqlpassword", "PLEASE");
			try {
				props.store(new FileOutputStream("conf.properties"), "Comments");
				System.out.println("Please Config Token!");

				System.exit(-1);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				System.exit(-1);
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(-1);
				return;
			}
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
			return;
		} catch (ClassNotFoundException e) {
			System.out.println("MySQLへの接続に失敗しました。(MySQL接続するためのクラスが見つかりません)");
			System.exit(-1);
			return;
		}
	}

	static void loadTargetGuilds() {
		try {
			FileReader fr = new FileReader(new File("targetGuilds.json"));
			BufferedReader br = new BufferedReader(fr);
			String str = "";
			String data;
			while ((data = br.readLine()) != null) {
				str += data;
			}
			JSONArray array = new JSONArray(str);
			for (int i = 0; i < array.length(); i++) {
				targetGuilds.add(array.getLong(i));
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static JDA getJDA() {
		return JDA;
	}

	public static void setJDA(JDA jda) {
		JDA = jda;
	}

	public static List<Long> getTargetGuilds() {
		return targetGuilds;
	}

	public static boolean isTargetGuild(long target) {
		return targetGuilds.stream().filter(guildid -> guildid == target).count() != 0;
	}

	public static MySQLDBManager getMySQLDBManager() {
		return MySQLDBManager;
	}
}
