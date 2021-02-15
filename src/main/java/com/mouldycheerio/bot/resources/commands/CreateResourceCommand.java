package com.mouldycheerio.bot.resources.commands;

import java.util.ArrayList;
import java.util.List;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.Command;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CreateResourceCommand extends ResourceCommand implements Command {
    private List<String> questions;

    public CreateResourceCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.hidden("createResource"));

        questions = new ArrayList<String>();
        questions.add("What is the name of this resource?");
        questions.add("What is the symbol for this resource?");
//        questions.add("What should the arbitrary value of this resource be?");
    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        String ownerid = op.getConfig().getString("ownerid");
        if (ownerid.equals(e.getAuthor().getId())) {
            PeelingUtils.askQuestions(e, questions, (responses) -> {
                String name = responses.get(0);
                String symbol = responses.get(1);
//                long value = Long.parseLong(PeelingUtils.mentionToId(responses.get(2)));

                Resource resource = new Resource(name, symbol, 1, getResourceManager());
                getResourceManager().addResource(resource);

                op.sendMessage(e, "Resources", ":white_check_mark: added **" + name + "** to the list of resources");
            });
        } else {
            op.sendMessage(e, "missing permissions", ":x: this command is not intended for you");
        }
    }

}
