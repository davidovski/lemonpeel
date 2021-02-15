package com.mouldycheerio.bot.resources.market;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;

public class MarketManager {
    private ResourceManager resourceManager;

    private List<Offer> offers = new ArrayList<Offer>();

    private File dataFile;

    private CustomBot bot;

    public MarketManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        bot = resourceManager.getBot();

        dataFile = new File(resourceManager.getBot().getDatadir(), "market.json");

        load();
    }

    public void load() {
        JSONObject loadJSON = PeelingUtils.loadJSON(dataFile);
        if (loadJSON.has("offers")) {
        loadJSON.getJSONArray("offers").forEach(o -> {
            if (o instanceof JSONObject) {
                Offer offer = new Offer((JSONObject) o);
                offers.add(offer);
            }
        });
    }
    }

    public void save() {
        JSONObject jsonObject = new JSONObject();
        for (Offer offer : offers) {
            jsonObject.append("offers", offer);
        }

        PeelingUtils.saveJSON(dataFile, jsonObject);
    }

    public Offer getOffer(String id) {
        for (Offer offer : offers) {
            if (id.equals(offer.getID() + "")) {
                return offer;
            }
        }
        return null;
    }

    public void addOffer(Offer offer) {
        offers.add(offer);
        processOffers();
    }

    public void processOffers() {
        Iterator<Offer> iterator = offers.iterator();
        while (iterator.hasNext()) {
            Offer offer = iterator.next();

            User seller = offer.getUser(bot.getClient());
            long quantity = offer.getSellingQuantity();
            Resource resource = offer.getSellingResource(resourceManager);
            long l = resource.get(seller);
            if (l < quantity) {

                sendNoLongerValidMessage(offer, seller);
                iterator.remove();
            }
        }

        save();
    }

    private void sendNoLongerValidMessage(Offer offer, User seller) {
        seller.openPrivateChannel().queue(pc -> {
            pc.sendMessage("Your market offer: **" + offer.getAsString(resourceManager) + "** [" + offer.getID() + "] is no longer valid because you no longer have enough resources to complete it.").queue();
        });
    }

    public void removeOffer(Offer o) {
        offers.remove(o);
        save();
    }

    public List<Offer> listOffers() {
        return Collections.unmodifiableList(offers);
    }

    public List<Field> getEmbedFields() {
        return listOffers().stream().map(o -> new Field("[" + o.getID() + "] " + o.getUser(bot.getClient()).getAsTag(), o.getAsString(resourceManager), false))
                .collect(Collectors.toList());
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
