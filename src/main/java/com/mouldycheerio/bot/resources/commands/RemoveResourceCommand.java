package com.mouldycheerio.bot.resources.commands;

import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.Command;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RemoveResourceCommand extends ResourceCommand implements Command {

    public RemoveResourceCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.hidden("removeResource"));
    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        String ownerid = op.getConfig().getString("ownerid");
        if (ownerid.equals(e.getAuthor().getId())) {
            if (args.length > 0) {
                String name = args[0];
                PeelingUtils.askQuestion(e, "Are you sure you want to delete **" + name + "**?\n(warning: all userdata will be lost)\nSend `confirm` to proceed", r -> {
                    if (r.equalsIgnoreCase("confirm")) {
                        boolean removeResource = getResourceManager().removeResource(name);
                        if (removeResource) {
                            op.sendMessage(e, "removed", "removed the resource **" + name + "** from the list of resources");

                        } else {
                            op.sendMessage(e, "not found", ":x: the resource **" + name + "** was not found");
                        }
                    } else {
                        op.sendMessage(e, "cancelled", ":x: operation cancelled");

                    }
                });
            } else {
                op.sendMessage(e, "incorrect usage", ":x: usage: `.removeResource [name]`");
            }
        } else {
            op.sendMessage(e, "missing permissions", ":x: this command is not intended for you");
        }
    }

}
