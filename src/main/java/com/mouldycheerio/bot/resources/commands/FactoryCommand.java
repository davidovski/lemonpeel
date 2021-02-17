package com.mouldycheerio.bot.resources.commands;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.bot.resources.ResourceTable;
import com.mouldycheerio.bot.resources.factories.Factory;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.commands.CommandFail;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class FactoryCommand extends ResourceCommand {

    public FactoryCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.from("shop,factoryshop,factory,s", "Buy and list factories that can be purchased", "shop [list/info/buy]"));

    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {

        if (args.length == 0) {
//            usage(e, op);
            list(e, op);

        } else if (args.length > 0) {
            if (args[0].equalsIgnoreCase("info")) {
                if (args.length > 1) {
                    info(e, op, args);
                } else {
                    op.sendMessage(e, "incorrect usage", ":x: please provide a factory name");
                }
            } else if (args[0].equalsIgnoreCase("purchase") || args[0].equalsIgnoreCase("buy")) {
                if (args.length > 1) {
                    purchase(e, op, args);
                } else {
                    op.sendMessage(e, "incorrect usage", ":x: please provide a factory name");
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                list(e, op);
            }
        }
    }

    private void purchase(MessageReceivedEvent e, CustomBot op, String[] args) {
        String cityname = args[1];
        Factory c = getResourceManager().getCity(cityname);
        if (c == null) {
            op.sendMessage(e, "not found", ":x: no factory by that name was found");
        } else {
            User user = e.getAuthor();
            Map<Resource, Long> required = c.getPurchaseTable().doesUserHave(user, getResourceManager());

            if (required.size() == 0) {
                ResourceTable purchaseTable = c.getPurchaseTable();
                Map<Resource, Long> table = purchaseTable.getTable(getResourceManager());
                table.forEach((r, q) -> {
                    r.increment(user, -q);
                });

                getResourceManager().addCity(user, c.getName());
                op.sendMessage(e, "Factory", "You purchased **" + c.getName() + "**");
            } else {
                List<String> collect = required.entrySet().stream().map(entry -> entry.getKey().prettyValue(entry.getValue())).collect(Collectors.toList());

                op.sendMessage(e, "not enough", ":x: You haven't got enough resources!\n" + "You're missing: " + String.join(", ", collect));
            }
        }
    }

    private void usage(MessageReceivedEvent e, CustomBot op) throws CommandFail {
        sendUsage(e, op);
    }

    private void info(MessageReceivedEvent e, CustomBot op, String[] args) {
        String cityname = args[1];
        Factory c = getResourceManager().getCity(cityname);
        if (c == null) {
            op.sendMessage(e, "not found", ":x: no factories by that name have been found");
        } else {
            String info = "Cost: **" + c.getPurchaseTable().toStringList(getResourceManager()) + "**\n" + "Resources/hour: **"
                    + c.getProductionTable().toStringList(getResourceManager()) + "**\n";
            op.sendMessage(e, c.getName(), info);
        }
    }

    private void list(MessageReceivedEvent e, CustomBot op) {
        List<Field> fields = getResourceManager().listCities().stream()
                .map(c -> new Field("**" + c.getName() + "**",
                        "Costs: " + c.getPurchaseTable().toStringList(getResourceManager()) + "\nProduces: " + c.getProductionTable().toStringList(getResourceManager()), false))
                .collect(Collectors.toList());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Available Factories");
        embedBuilder.setColor(op.color);
        embedBuilder.setDescription("Use `shop purchase [name]` to buy a factory");

        PeelingUtils.pagesEmbed(e.getAuthor(), e.getTextChannel(), embedBuilder, fields);
    }

}
