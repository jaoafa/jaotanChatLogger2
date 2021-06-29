package com.jaoafa.jaotanChatLogger2.Lib;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    JSONObject config;
    String token;
    MySQLDBManager manager;

    public Config() {
        try {
            config = new JSONObject(Files.readString(Path.of("config.json")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("config.jsonファイルにアクセスできないため、設定情報を取得できません。終了します。");
            System.exit(1);
            return;
        }

        this.token = config.getString("token");
        try {
            manager = new MySQLDBManager(
                requiredConfig("sqlserver"),
                requiredConfig("sqlport"),
                requiredConfig("sqldatabase"),
                requiredConfig("sqluser"),
                requiredConfig("sqlpassword"));
        } catch (ClassNotFoundException e) {
            System.out.println("MySQLDBManagerを初期化するために必要なクラスが見つからないため、終了します。");
            System.exit(1);
        }
    }

    String requiredConfig(String key) {
        if (!config.has(key)) {
            System.out.println("設定情報に必須キー「" + key + "」がありません。終了します。");
            System.exit(1);
            return null;
        }
        if (config.getString(key).equals("PLEASETOKEN") || config.getString(key).equals("PLEASE")) {
            System.out.println("設定情報の必須キー「" + key + "」が初期状態です。終了します。");
            System.exit(1);
            return null;
        }
        return config.getString(key);
    }

    public String getToken() {
        return token;
    }

    public MySQLDBManager getMySQLDBManager() {
        return manager;
    }
}
