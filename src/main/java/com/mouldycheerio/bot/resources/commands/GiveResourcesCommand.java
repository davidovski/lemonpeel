package com.mouldycheerio.bot.resources.commands;

import static java.lang.Math.abs;

import java.util.Optional;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GiveResourcesCommand extends ResourceCommand {

    private String usage;

    public GiveResourcesCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.from("give,trade", "Give a user a resource", "give [@user] [quantity] [resource]"));
        usage = "\n`" + getCommandDetails().getUsage() + "`";

    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        if (args.length > 1) {
            String usermention = args[0];
            String quantitystring = args[1];
            String resourcename = getResourceManager().getPrimaryResourceName();
            if (args.length > 2) {
                resourcename = args[2];
            }

            boolean generate = false;
            String ownerid = op.getConfig().getString("ownerid");
            if (ownerid.equals(e.getAuthor().getId())) {
                if (args.length > 3) {
                    if (args[3].equals("generate")) {
                        generate = true;
                    }
                }
            }
            Optional<User> mentionToUser = PeelingUtils.mentionToUser(usermention, e.getJDA());
            if (mentionToUser.isPresent()) {
                User user = mentionToUser.get();
                try {
                    long quantity = Long.parseLong(quantitystring);
                    quantity = abs(quantity);

                    Resource resource = getResourceManager().getResource(resourcename);
                    if (resource != null) {

                        if (!generate && quantity > resource.get(e.getAuthor())) {
                            op.sendMessage(e, "give", ":x: You do not have enough " + resource.getName() + "!");
                        } else {
                            if (!generate) {
                                resource.increment(e.getAuthor(), -quantity);
                            }
                            resource.increment(user, quantity);
                            op.sendMessage(e, "give", ":white_check_mark: You gave " + user.getAsMention() + " " + resource.prettyValue(quantity));
                        }
                    } else {
                        op.sendMessage(e, "give", ":x: Please provide a valid resource!" + usage);

                    }

                } catch (NumberFormatException ex) {
                    op.sendMessage(e, "give", ":x: Please provide a valid quantity!" + usage);

                }

            } else {
                op.sendMessage(e, "give", ":x: That user was not found!" + usage);
            }
        } else {
            op.sendMessage(e, "give", ":x: Invalid usage!" + usage);

        }
    }

}
