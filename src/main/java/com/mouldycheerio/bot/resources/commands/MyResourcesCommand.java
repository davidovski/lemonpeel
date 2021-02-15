package com.mouldycheerio.bot.resources.commands;

import java.util.List;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.bot.resources.ResourceTable;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MyResourcesCommand extends ResourceCommand {

    public MyResourcesCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.from("resources,inventory,inv,balance", "Lists your current inventory of resources and the current production from all of your factories", "resources [@user]"));

    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        List<Resource> resources = getResourceManager().listResources();

        boolean your = true;
        User user;
        if (args.length > 0 && PeelingUtils.mentionToUser(args[0], e.getJDA()).isPresent()) {
            user = PeelingUtils.mentionToUser(args[0], e.getJDA()).get();
            your = false;
        } else {
            user = e.getAuthor();
        }
        ResourceTable production = getResourceManager().getUserProduction(user);
        StringBuilder stringBuilder = new StringBuilder();
        resources.forEach(r -> {
            long l = r.get(user);
            stringBuilder.append("" + r.getName() + ": **" + l + r.getSymbol() + "** ");
            long p = production.getResource(r);
            if (p != 0) {
                stringBuilder.append("[+" + r.prettyValue(p) + "/h]");
            }
            stringBuilder.append("\n");
        });

        op.sendMessage(e, (your ? "Your" : user.getName() + "'s") + "Resources", stringBuilder.toString());
    }

}
