package com.mouldycheerio.bot.resources.market;

import java.util.List;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.commands.DetailedCommand;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MarketCommand extends DetailedCommand {
    private ResourceManager resourceManager;
    private MarketManager marketManager;

    public MarketCommand(MarketManager marketManager) {
        this.marketManager = marketManager;
        this.resourceManager = marketManager.getResourceManager();
        setCommandDetails(CommandDetails.from("market", "Buy and sell resources at a given quantity on the market", "market sell [resources] for [price]"));

    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {

        marketManager.processOffers();
        if (args.length == 0) {
            // TODO allow multipages with <-, ->

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Market");
            embedBuilder.setColor(op.color);
            if (marketManager.getEmbedFields().size() == 0) {
                embedBuilder.setDescription("**:x: There are no available offers!**\n" + "Create one with `market sell`");
                e.getTextChannel().sendMessage(embedBuilder.build()).queue();
            } else {
                embedBuilder.setDescription("Use `" + op.getPrefixManager().getPrefix(e.getGuild()) + "market buy [id]` to accept an offer");
                PeelingUtils.pagesEmbed(e.getAuthor(), e.getTextChannel(), embedBuilder, marketManager.getEmbedFields());
            }

        } else if (args.length > 0) {
            if (args[0].equals("buy")) {
                if (args.length > 1) {
                    buy(e, op, args);
                } else {
                    op.sendMessage(e, "not found", ":x: please specify an id!");
                }
            } else if (args[0].equals("sell")) {
                sell(e, op, args);
            }
        }
    }

    private void sell(MessageReceivedEvent e, CustomBot op, String[] args) {
        if (args.length > 5) {

            try {
                long sellQ = Math.abs(Long.parseLong(args[1]));
                long buyQ = Math.abs(Long.parseLong(args[4]));
                String sellRname = args[2];
                Resource sellR = resourceManager.getResource(sellRname);
                String buyRname = args[5];
                Resource buyR = resourceManager.getResource(buyRname);

                if (sellR.get(e.getAuthor()) >= sellQ) {
                    Offer offer = new Offer();

                    offer.setUserID(e.getAuthor().getId());
                    offer.setBuyingQuantity(buyQ);
                    offer.setBuyingResource(buyR);
                    offer.setID(Offer.highest + 1);
                    offer.setSellingQuantity(sellQ);
                    offer.setSellingResource(sellR);

                    marketManager.addOffer(offer);
                    op.sendMessage(e, "market", ":white_check_mark: successfully listed **" + offer.getAsString(resourceManager) + "** offer id: `" + offer.getID() + "`");
                } else {
                    op.sendMessage(e, "not enough", ":x: You don't have enough " + sellR + " to offer");
                }

            } catch (NumberFormatException ex) {
                sendSellUsage(e, op);
            }
        } else {
            sendSellUsage(e, op);
        }
    }

    private void sendSellUsage(MessageReceivedEvent e, CustomBot op) {
        List<Resource> listResources = resourceManager.listResources();
        Resource resource1 = listResources.get(listResources.size() - 1);
        Resource resource2 = listResources.get(0);

        int q1 = (int) (Math.random() * 10);
        int q2 = (int) (Math.random() * 10);
        op.sendMessage(
                e, "incorrect usage",
                ":x: usage: `market sell [resources] for [price]`\ni.e: `market sell " + q1 + " " + resource1.getName() + " for " + q2 + " " + resource2.getName() + "`"
        );
    }

    private void buy(MessageReceivedEvent e, CustomBot op, String[] args) {
        String id = args[1];
        Offer offer = marketManager.getOffer(id);
        if (offer != null) {
            offer.getUser(e.getJDA(), seller ->{
            if (seller.equals(e.getAuthor())) {
                op.sendMessage(e, "error", ":x: you cannot accept your own offer");
            } else {
                long buyingQuantity = offer.getBuyingQuantity();
                Resource buyingResource = offer.getBuyingResource(resourceManager);
                if (buyingResource.get(e.getAuthor()) >= buyingQuantity) {

                    long sellingQuantity = offer.getSellingQuantity();
                    Resource sellingResource = offer.getSellingResource(resourceManager);

                    buyingResource.increment(e.getAuthor(), -buyingQuantity);
                    sellingResource.increment(e.getAuthor(), sellingQuantity);

                    if (seller != null) {
                        buyingResource.increment(seller, buyingQuantity);
                        sellingResource.increment(seller, -sellingQuantity);
                    }
                    seller.openPrivateChannel().queue(pc -> {
                        pc.sendMessage(
                                "**" + e.getAuthor().getAsTag() + "** has accepted your market offer: **" + offer.getAsString(resourceManager) + "** [" + offer.getID() + "]")
                                .queue();
                    });

                    String from = "";
                    if (seller != null) {
                        from = " from " + seller.getAsMention();
                    }

                    marketManager.removeOffer(offer);
                    op.sendMessage(e, "market", ":white_check_mark: successfully bought **" + sellingResource.prettyValue(sellingQuantity) + "** for **"
                            + buyingResource.prettyValue(buyingQuantity) + "**" + from);

                } else {
                    long have = buyingResource.get(e.getAuthor());
                    long more = buyingQuantity - have;
                    op.sendMessage(e, "not enough", ":x: you need **" + buyingResource.prettyValue(more) + "** more to buy this");
                }
            }
            });
        } else {
            op.sendMessage(e, "not found", ":x: please specify a valid id!");
        }
    }
}
