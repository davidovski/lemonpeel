package com.mouldycheerio.bot.resources.commands;

import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.Command;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RemoveCityCommand extends ResourceCommand implements Command {

    public RemoveCityCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.hidden("removeFactory"));

    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        String ownerid = op.getConfig().getString("ownerid");
        if (ownerid.equals(e.getAuthor().getId())) {
            if (args.length > 0) {
                String name = args[0];
                PeelingUtils.askQuestion(e, "Are you sure you want to delete **" + name + "**?\n(warning: it will be removed from all users's city lists)\nSend `confirm` to proceed", r -> {
                    if (r.equalsIgnoreCase("confirm")) {
                        boolean removeCity = getResourceManager().removeCity(name);
                        if (removeCity) {
                            op.sendMessage(e, "removed", "removed the city **" + name + "** from the list of city");

                        } else {
                            op.sendMessage(e, "not found", ":x: the city **" + name + "** was not found");
                        }
                    } else {
                        op.sendMessage(e, "cancelled", ":x: operation cancelled");

                    }
                });
            } else {
                op.sendMessage(e, "incorrect usage", ":x: usage: `.removeCity [name]`");
            }
        } else {
            op.sendMessage(e, "missing permissions", ":x: this command is not intended for you");
        }
    }

}
