package com.mouldycheerio.bot.resources.commands;

import java.util.List;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.CommandDetails;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ListResourcesCommand extends ResourceCommand {

    public ListResourcesCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.hidden("listResources"));

    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        List<Resource> resources = getResourceManager().listResources();
        StringBuilder stringBuilder = new StringBuilder();
        resources.forEach(r -> {
            stringBuilder.append(r.getSymbol() + " " + r.getName() + "\n");
        });

        op.sendMessage(e, "Resources", stringBuilder.toString());
    }

}
