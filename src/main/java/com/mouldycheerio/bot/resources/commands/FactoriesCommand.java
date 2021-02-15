package com.mouldycheerio.bot.resources.commands;

import java.util.List;
import java.util.stream.Collectors;

import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.CommandDetails;
import com.mouldycheerio.dbot.util.PeelingUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class FactoriesCommand extends ResourceCommand {

    public FactoriesCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.from("factories", "List the factories that you (or mentioned user) owns.", "factories [@user]"));

    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        User user;
        boolean your = true;
        if (args.length > 0 && PeelingUtils.mentionToMember(args[0], e.getGuild()).isPresent()) {
            user = PeelingUtils.mentionToMember(args[0], e.getGuild()).get().getUser();
            your = false;
        } else {
            user = e.getAuthor();
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle((your ? "Your" : user.getName() + "'s") + " Factories");
        List<Field> fields = getResourceManager().listCities(user).stream()
                .map(city -> new Field(city.getName(), "**" + city.getProductionTable().toStringList(getResourceManager()) + "** / hour", false)).collect(Collectors.toList());
        // TODO "you have no factories
        // TODO factory x2/10/542/1241515

        embedBuilder.setColor(op.color);

        PeelingUtils.pagesEmbed(e.getAuthor(), e.getTextChannel(), embedBuilder, fields);
    }

}
