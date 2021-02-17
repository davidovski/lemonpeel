package com.mouldycheerio.bot.resources.market;

import java.util.function.Consumer;

import org.json.JSONObject;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class Offer extends JSONObject {
    private static final String ID = "id";
    private static final String BUYING_QUANTITY = "buying_quantity";
    private static final String SELLING_QUANTITY = "selling_quantity";
    private static final String BUYING_RESOURCE = "buying_resource";
    private static final String SELLING_RESOURCE = "selling_resource";
    private static final String USERID = "userid";

    public static int highest = 0;

    public Offer() {
        super();
    }

    public Offer(JSONObject jsonObject) {
        super();
        jsonObject.keySet().forEach(k -> put(k, jsonObject.get(k)));

        if (has(ID)) {
            if (getID() >= highest) {
                highest = getID();
            }
        }
    }

    public int getID() {
        return getInt(ID);
    }

    public void setID(int id) {
        put(ID, id);
        if (getID() >= highest) {
            highest = getID();
        }
    }

    public String getUserID() {
        return getString(USERID);
    }

    public void getUser(JDA jda, Consumer<User> c) {
        jda.retrieveUserById(getUserID()).queue(u -> c.accept(u));
    }

    public User getUser(JDA jda) {
        return jda.retrieveUserById(getUserID()).complete();
    }

    public void setUserID(String userid) {
        put(USERID, userid);
    }

    public Resource getSellingResource(ResourceManager resourceManager) {
        String string = getString(SELLING_RESOURCE);
        return resourceManager.getResource(string);
    }

    public void setSellingResource(Resource resource) {
        put(SELLING_RESOURCE, resource.getName());
    }

    public Resource getBuyingResource(ResourceManager resourceManager) {
        String string = getString(BUYING_RESOURCE);
        return resourceManager.getResource(string);
    }

    public void setBuyingResource(Resource resource) {
        put(BUYING_RESOURCE, resource.getName());
    }

    public long getSellingQuantity() {
        return getLong(SELLING_QUANTITY);
    }

    public void setSellingQuantity(long q) {
        put(SELLING_QUANTITY, q);
    }

    public long getBuyingQuantity() {
        return getLong(BUYING_QUANTITY);
    }

    public void setBuyingQuantity(long q) {
        put(BUYING_QUANTITY, q);
    }

    public String getAsString(ResourceManager resourceManager) {
        return getSellingResource(resourceManager).prettyValue(getSellingQuantity()) + " for " + getBuyingResource(resourceManager).prettyValue(getBuyingQuantity());
    }
}
