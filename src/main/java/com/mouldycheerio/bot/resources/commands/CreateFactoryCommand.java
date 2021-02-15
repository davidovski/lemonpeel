package com.mouldycheerio.bot.resources.commands;

import java.util.ArrayList;
import java.util.List;

import com.mouldycheerio.bot.resources.Resource;
import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.bot.resources.ResourceTable;
import com.mouldycheerio.bot.resources.factories.Factory;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.Command;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CreateFactoryCommand extends ResourceCommand implements Command {
    private List<String> questions;

    public CreateFactoryCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.hidden("createFactory"));

        questions = new ArrayList<String>();
        questions.add("What is the name of this city?");
        questions.add("How many of each resource should this city cost?\n(list resources separated by commas (ie: `100 money, 200 food, 50 bricks`)");
        questions.add("How many of each resource should this city produce / hour?\n(list resources separated by commas (ie: `100 money, 200 food, 50 bricks`)");
    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        String ownerid = op.getConfig().getString("ownerid");
        if (ownerid.equals(e.getAuthor().getId())) {
            PeelingUtils.askQuestions(e, questions, (responses) -> {
                String name = responses.get(0);
                String purchaseList = responses.get(1);
                ResourceTable purchase = parseResources(purchaseList);
                String productionList = responses.get(2);
                ResourceTable production = parseResources(productionList);

                Factory city = new Factory(name, purchase, production);
                getResourceManager().addCity(city);

                op.sendMessage(e, "Resources", ":white_check_mark: added **" + name + "** to the list of cities");
            });
        } else {
            op.sendMessage(e, "missing permissions", ":x: this command is not intended for you");
        }
    }

    private ResourceTable parseResources(String input) {
        ResourceTable table = new ResourceTable();
        String[] resources = input.split(",");

        for (String string : resources) {
            String[] parts = string.split(" ");
            String quantityString = "0";
            String resourceName = "";
            if (parts.length == 2) {
                quantityString = parts[0];
                resourceName = parts[1];
            } else if (parts.length > 2) {
                quantityString = parts[1];
                resourceName = parts[2];
            }
            Resource resource = getResourceManager().getResource(resourceName);
            if (resource != null) {
                table.setResource(resource, Long.parseLong(quantityString));
            }
        }

        return table;
    }

}
