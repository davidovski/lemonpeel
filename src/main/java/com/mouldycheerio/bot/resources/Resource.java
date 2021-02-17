package com.mouldycheerio.bot.resources;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import com.mouldycheerio.dbot.util.DatabaseUtils;

import net.dv8tion.jda.api.entities.User;

public class Resource extends JSONObject {

    private static final String SYMBOL = "symbol";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private ResourceManager resourceManager;
    private String tableName;

    public Resource(String name, String symbol, long value, ResourceManager resourceManager) {
        super();
        setName(name);
        setSymbol(symbol);
        setValue(value);
        this.resourceManager = resourceManager;
        initDB();

    }

    public Resource(JSONObject jsonObject, ResourceManager resourceManager) {
        super();
        this.resourceManager = resourceManager;
        jsonObject.keySet().forEach(k -> put(k, jsonObject.get(k)));
        initDB();
    }

    public void initDB() {
        tableName = getName().split(" ")[0];
        try {
            DatabaseUtils.createSimpleKVtable(resourceManager.getDatabasepath(), tableName);
        } catch (SQLException e) {
            // e.printStackTrace();
        }
    }

    public void set(User user, long value) {
        try {
            DatabaseUtils.putInKVtable(resourceManager.getDatabasepath(), tableName, user.getId(), value);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long get(User user) {
        try {
            return DatabaseUtils.getInKVtable(resourceManager.getDatabasepath(), tableName, user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0l;
    }

    public long getValue(String userid) {
        try {
            return DatabaseUtils.getInKVtable(resourceManager.getDatabasepath(), tableName, userid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0l;
    }

    public long total() {
        try {
            return DatabaseUtils.getTotal(resourceManager.getDatabasepath(), tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getUsers() {
        try {
            return DatabaseUtils.listKeys(resourceManager.getDatabasepath(), tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void increment(User user, long amount) {
        set(user, get(user) + amount);
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        put(NAME, name);
    }

    public String getSymbol() {
        return getString(SYMBOL);
    }

    public void setSymbol(String symbol) {
        put(SYMBOL, symbol);
    }

    public long getValue() {
        if (total() <= 0) {
            return -1;
        } else {
            return resourceManager.getPrimaryResource().total() / total();
        }
    }

    public void setValue(long value) {
        put(VALUE, value);
    }

    public String prettyValue(long value) {
        return value + getSymbol();
    }

    @Override
    public String toString() {
        return getName();
    }
}
