package com.mouldycheerio.bot.resources;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mouldycheerio.bot.resources.commands.CreateFactoryCommand;
import com.mouldycheerio.bot.resources.commands.CreateResourceCommand;
import com.mouldycheerio.bot.resources.commands.FactoriesCommand;
import com.mouldycheerio.bot.resources.commands.FactoryCommand;
import com.mouldycheerio.bot.resources.commands.GiveResourcesCommand;
import com.mouldycheerio.bot.resources.commands.ListResourcesCommand;
import com.mouldycheerio.bot.resources.commands.MyResourcesCommand;
import com.mouldycheerio.bot.resources.commands.RemoveCityCommand;
import com.mouldycheerio.bot.resources.commands.RemoveResourceCommand;
import com.mouldycheerio.bot.resources.commands.SetPrimaryResourceCommand;
import com.mouldycheerio.bot.resources.factories.Factory;
import com.mouldycheerio.bot.resources.market.MarketCommand;
import com.mouldycheerio.bot.resources.market.MarketManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.CommandController;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.util.DatabaseUtils;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.entities.User;

public class ResourceManager {

    private static final int DEFAULT_RESOURCE_PRODUCTION = 50;
    private static final String PRIMARY_RESOURCE_KEY = "primary_resource";
    private static final String RESOURCES_KEY = "resources";
    private static final String CITIES_KEY = "cities";
    private List<Resource> resources = new ArrayList<Resource>();
    private List<Factory> cities = new ArrayList<Factory>();

    private File configFile;
    private CustomBot bot;
    private String databasepath;

    private String primaryResource = "money";

    private long lastUpdate = System.currentTimeMillis();
    private JSONObject userCities;
    private File citiesFile;
    private MarketManager marketManager;

    public ResourceManager(CustomBot customBot) {
        this.bot = customBot;
        configFile = new File(customBot.getDatadir(), "resources_config.json");
        citiesFile = new File(customBot.getDatadir(), "cities.json");

        File database = new File(customBot.getDatadir(), "resources.db");
        databasepath = database.getPath();

        File updateDatabase = new File(customBot.getDatadir(), "data.db");
        try {
            DatabaseUtils.createSimpleKVtable(updateDatabase.getPath(), "resources");
        } catch (SQLException e1) {
        }
        try {
            long u = DatabaseUtils.getInKVtable(updateDatabase.getPath(), "resources", "update");
            lastUpdate = u == 0 ? System.currentTimeMillis() : u;
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        loadResources();
        loadCities();

        marketManager = new MarketManager(this);

        initCommands();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (System.currentTimeMillis() > lastUpdate) {
                    System.out.println(lastUpdate + " and now " + System.currentTimeMillis());
                    lastUpdate += 1000 * 60 * 60;
                    try {
                        DatabaseUtils.putInKVtable(updateDatabase.getPath(), "resources", "update", lastUpdate);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    update();
                }
            }
        }, 100, 1000);
    }

    private void initCommands() {
        CommandController cc = bot.getCommandController();

        // setup commands
        cc.addCommand(new ListResourcesCommand(this));
        cc.addCommand(new CreateResourceCommand(this));
        cc.addCommand(new RemoveResourceCommand(this));
        cc.addCommand(new SetPrimaryResourceCommand(this));

        cc.addCommand(new CreateFactoryCommand(this));
        cc.addCommand(new RemoveCityCommand(this));

        MyResourcesCommand myResourcesCommand = new MyResourcesCommand(this);
        cc.addCommand(CommandDetails.hidden("tick"), (e, b, args) -> {
            String ownerid = b.getConfig().getString("ownerid");
            if (ownerid.equals(e.getAuthor().getId())) {
                update();
                myResourcesCommand.execute(e, b, args);
            }
        });

        // public commands
        cc.addCommand(myResourcesCommand);
        cc.addCommand(new FactoryCommand(this));
        cc.addCommand(new FactoriesCommand(this));

        cc.addCommand(new GiveResourcesCommand(this));
        cc.addCommand(new MarketCommand(marketManager));

    }

    private void loadResources() {
        JSONObject loadJSON = PeelingUtils.loadJSON(configFile);

        if (loadJSON.has(CITIES_KEY)) {
            JSONArray jsonArray = loadJSON.getJSONArray(CITIES_KEY);
            for (Object o : jsonArray) {
                if (o instanceof JSONObject) {
                    Factory city = new Factory((JSONObject) o);
                    cities.add(city);
                }
            }
        }
        if (loadJSON.has(RESOURCES_KEY)) {
            JSONArray jsonArray = loadJSON.getJSONArray(RESOURCES_KEY);
            for (Object o : jsonArray) {
                if (o instanceof JSONObject) {
                    Resource resource = new Resource((JSONObject) o, this);
                    resources.add(resource);
                }
            }
        }

        if (loadJSON.has(PRIMARY_RESOURCE_KEY)) {
            setPrimaryResource(loadJSON.getString(PRIMARY_RESOURCE_KEY));
        }
    }

    private void saveResources() {
        JSONObject save = new JSONObject();
        save.put(PRIMARY_RESOURCE_KEY, primaryResource);

        resources.forEach(r -> save.append(RESOURCES_KEY, r));
        cities.forEach(r -> save.append(CITIES_KEY, r));

        PeelingUtils.saveJSONPretty(configFile, save);
    }

    private void loadCities() {
        userCities = PeelingUtils.loadJSON(citiesFile);
    }

    private void saveCities() {
        PeelingUtils.saveJSON(citiesFile, userCities);
    }

    public void addCity(Factory city) {
        cities.add(city);
        saveResources();
    }

    public Factory getCity(String input) {
        for (Factory city : cities) {
            if (city.getName().equalsIgnoreCase(input)) {
                return city;
            }
        }
        return null;
    }

    public List<Factory> listCities() {
        return Collections.unmodifiableList(cities);
    }

    public boolean removeCity(String name) {
        Factory city = getCity(name);
        if (city != null) {
            cities.remove(city);
            saveResources();
            return true;
        } else {
            return false;
        }
    }

    public void addResource(Resource resource) {
        resources.add(resource);
        saveResources();
    }

    public Resource getResource(String input) {
        for (Resource resource : resources) {
            if (resource.getName().equalsIgnoreCase(input) || resource.getSymbol().equalsIgnoreCase(input)) {
                return resource;
            }
        }
        return null;
    }

    public List<Resource> listResources() {
        return Collections.unmodifiableList(resources);
    }

    public boolean removeResource(String name) {
        Resource resource = getResource(name);
        if (resource != null) {
            resources.remove(resource);
            saveResources();
            return true;
        } else {
            return false;
        }
    }

    public CustomBot getBot() {
        return bot;
    }

    public void setBot(CustomBot bot) {
        this.bot = bot;
    }

    public String getDatabasepath() {
        return databasepath;
    }

    public void setDatabasepath(String databasepath) {
        this.databasepath = databasepath;
    }

    public Resource getPrimaryResource() {
        Resource resource = getResource(primaryResource);
        return resource == null ? resources.get(0) : resource;

    }

    public String getPrimaryResourceName() {
        return primaryResource;
    }

    public void setPrimaryResource(String primaryResource) {
        this.primaryResource = primaryResource;
        saveResources();
    }

    public void addCity(User user, String city) {
        userCities.append(user.getId(), city);
        saveCities();
    }

    public List<Factory> listCities(User user) {
        List<Factory> list = new ArrayList<Factory>();
        if (userCities.has(user.getId())) {
            userCities.getJSONArray(user.getId()).forEach(o -> {
                if (o instanceof String) {
                    Factory city = getCity((String) o);
                    if (city != null) {
                        list.add(city);
                    }
                }
            });
        }
        return list;
    }

    public ResourceTable getUserProduction(User user) {
        if (getPrimaryResource().get(user) == 0) {
            getPrimaryResource().set(user, 0);
        }
        ResourceTable prouction = new ResourceTable();
        prouction.setResource(getPrimaryResource(), DEFAULT_RESOURCE_PRODUCTION);

        List<Factory> listCities = listCities(user);
        for (Factory city : listCities) {
            ResourceTable productionTable = city.getProductionTable();
            ResourceTable negatives = productionTable.negatives();

            if  (negatives.doesUserHave(user, this).size() == 0)  {
                prouction.addResourceTable(city.getProductionTable());
            }
        }

        return prouction;
    }

    private void update() {
        try {
            DatabaseUtils.listKeys(databasepath, getPrimaryResourceName()).forEach(id -> {
                bot.getClient().retrieveUserById(id).queue(
                        user -> {
                            // System.out.println("updating " + user.getName() + "'s resources");
                            if (getPrimaryResource() != null) {
                                getPrimaryResource().increment(user, DEFAULT_RESOURCE_PRODUCTION);

                                List<Factory> listCities = listCities(user);
                                for (Factory city : listCities) {
                                    ResourceTable productionTable = city.getProductionTable();
                                    ResourceTable negatives = productionTable.negatives();

                                    if  (negatives.doesUserHave(user, this).size() == 0)  {
                                        productionTable.getTable(this)
                                        .forEach((r, q) -> {
                                            r.increment(user, q);
                                        });
                                    }
                                }

                            }
                        }
                );
            });
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
