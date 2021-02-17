package com.mouldycheerio.bot.resources.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.commands.CommandFail;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ValuesCommand extends ResourceCommand {

    public ValuesCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.from("marketValue,values,prices", "Show estimated market values of all the resources"));
    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot b, String[] args) throws CommandFail {
        long total = getResourceManager().getPrimaryResource().total();

        List<Resource> resources = new ArrayList<Resource>(getResourceManager().getResources());
        Collections.sort(resources, (r1, r2) -> Long.compare(r2.getValue(),r1.getValue()));

        StringBuilder stringBuilder = new StringBuilder();
        resources.forEach((r) -> {
            long v = r.getValue();
            stringBuilder.append("**1** " + r.getName() + r.getSymbol() + " : ");
            if (v < 0) {
                stringBuilder.append("**undetermined**");
            } else {
                stringBuilder.append("**" + getResourceManager().getPrimaryResource().prettyValue(v) + "**");
            }
            stringBuilder.append("\n");
        });

        b.sendMessage(e, "Estimated Market Values", stringBuilder.toString());

    }

}
