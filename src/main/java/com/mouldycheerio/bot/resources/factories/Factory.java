package com.mouldycheerio.bot.resources.factories;

import org.json.JSONObject;

import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.bot.resources.ResourceTable;

public class Factory extends JSONObject {
    private static final String PRODUCTION_KEY = "production";
    private static final String PURCHASE_KEY = "purchase";
    private static final String NAME_KEY = "name";

    public Factory(JSONObject jsonObject) {
        super();
        jsonObject.keySet().forEach(k -> put(k, jsonObject.get(k)));
    }

    public Factory(String name, ResourceTable cost, ResourceTable production) {
        super();
        setName(name);
        setPurchaseTable(cost);
        setProductionTable(production);
    }

    public Factory(String name, long cost, ResourceTable production, ResourceManager resourceManager) {
        super();
        setName(name);

        ResourceTable resourceTable = new ResourceTable();
        resourceTable.setResource(resourceManager.getPrimaryResource(), cost);

        setPurchaseTable(resourceTable);
        setProductionTable(production);
    }

    public String getName() {
        return getString(NAME_KEY);
    }

    public void setName(String name) {
        put(NAME_KEY, name);
    }

    public ResourceTable getProductionTable() {
        JSONObject jsonObject = getJSONObject(PRODUCTION_KEY);
        ResourceTable resourceTable = new ResourceTable(jsonObject);
        return resourceTable;
    }

    public void setProductionTable(ResourceTable resourceTable) {
        put(PRODUCTION_KEY, resourceTable);
    }

    public ResourceTable getPurchaseTable() {
        JSONObject jsonObject = getJSONObject(PURCHASE_KEY);
        ResourceTable resourceTable = new ResourceTable(jsonObject);
        return resourceTable;
    }

    public void setPurchaseTable(ResourceTable resourceTable) {
        put(PURCHASE_KEY, resourceTable);
    }

}
