package com.mouldycheerio.bot.resources.commands;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.Command;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetPrimaryResourceCommand extends ResourceCommand implements Command {

    public SetPrimaryResourceCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.hidden("setPrimaryResource"));

    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        String ownerid = op.getConfig().getString("ownerid");
        if (ownerid.equals(e.getAuthor().getId())) {
            if (args.length > 0) {
                Resource resource = getResourceManager().getResource(args[0]);
                if (resource != null) {
                    PeelingUtils.askQuestion(e, "This will reset all users balance for this resource, type `confirm` to continue", r -> {
                        if (r.equals("confirm")) {
                            getResourceManager().setPrimaryResource(resource.getName());

                            e.getGuild().loadMembers().onSuccess(members -> {
                                members.forEach(m -> getResourceManager().getPrimaryResource().set(m.getUser(), 0));
                            });

                            op.sendMessage(e, "primary resource", "Set **" + resource.getName() + "** to the primary resource");
                        }
                    });
                } else {
                    op.sendMessage(e, "primary resource", "This resource was not found");
                }
            } else {
                op.sendMessage(e, "primary resource", "This will be the default resource that will be used for trading\nUsage: `.setPrimaryResource [resource name]`");
            }
        } else {
            op.sendMessage(e, "missing permissions", ":x: this command is not intended for you");
        }
    }

}
