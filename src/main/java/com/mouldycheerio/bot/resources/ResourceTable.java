package com.mouldycheerio.bot.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.User;

public class ResourceTable extends JSONObject {
    public ResourceTable(JSONObject jsonObject) {
        super();
        jsonObject.keySet().forEach(k -> put(k, jsonObject.get(k)));
    }

    public ResourceTable() {
        super();
    }

    public void setResource(Resource resource, long value) {
        put(resource.getName(), value);
    }

    public long getResource(Resource resource) {
        String name = resource.getName();
        if (has(name)) {
            return getLong(name);
        }
        return 0l;
    }

    public Map<Resource, Long> getTable(ResourceManager resourceManager) {
        HashMap<Resource, Long> hashMap = new HashMap<Resource, Long>();
        Iterator<String> keys = keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Resource resource = resourceManager.getResource(key);
            if (resource != null) {
                long q = getLong(key);
                hashMap.put(resource, q);
            }
        }

        return Collections.unmodifiableMap(hashMap);
    }

    public void addToUser(User user, ResourceManager resourceManager) {
        Map<Resource, Long> table = getTable(resourceManager);
        table.forEach((resource, q) -> {
            resource.increment(user, q);
        });
    }

    public void removeFromUser(User user, ResourceManager resourceManager) {
        Map<Resource, Long> table = getTable(resourceManager);
        table.forEach((resource, q) -> {
            resource.increment(user, -q);
        });
    }

    /**
     * @param user
     * @param resourceManager
     * @return list of resources that are lacking
     */
    public Map<Resource, Long> doesUserHave(User user, ResourceManager resourceManager) {
        Map<Resource, Long> table = getTable(resourceManager);
        Map<Resource, Long> lacking = new HashMap<Resource, Long>();
        table.forEach((resource, q) -> {
            if (resource.get(user) < q) {
                lacking.put(resource, q - resource.get(user));
            }
        });

        return Collections.unmodifiableMap(lacking);
    }

    public String toStringList(ResourceManager resourceManager) {
        Map<Resource, Long> table = getTable(resourceManager);

        List<String> parts = new ArrayList<String>();
        table.forEach((r, q) -> parts.add(r.prettyValue(q)));

        return String.join(", ", parts);
    }

    public void addResourceTable(ResourceTable table) {
        Iterator<String> keys = table.keys();
        while (keys.hasNext()) {
            String next = keys.next();

            put(next, optLong(next) + table.getLong(next));
        }
    }

    public ResourceTable negatives() {
        ResourceTable negatives = new ResourceTable();
        Iterator<String> keys = keys();
        while (keys.hasNext()) {
            String next = keys.next();
            long long1 = getLong(next);
            if (long1 < 0) {
                negatives.put(next, Math.abs(long1));
            }
        }
        return negatives;
    }

    public ResourceTable postives() {
        ResourceTable positives = new ResourceTable();
        Iterator<String> keys = keys();
        while (keys.hasNext()) {
            String next = keys.next();
            long long1 = getLong(next);
            if (long1 > 0) {
                positives.put(next, long1);
            }
        }
        return positives;
    }

}
