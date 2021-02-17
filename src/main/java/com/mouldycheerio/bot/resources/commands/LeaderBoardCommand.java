package com.mouldycheerio.bot.resources.commands;

import static java.util.stream.Collectors.toMap;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.commands.CommandDetails;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LeaderBoardCommand extends ResourceCommand {

    public LeaderBoardCommand(ResourceManager resourceManager) {
        super(resourceManager);
        setCommandDetails(CommandDetails.from("leaderboard,top,best", "Lists the most valuable users", "resources [@user]"));

    }

    @Override
    public void execute(MessageReceivedEvent e, CustomBot op, String[] args) {
        AtomicInteger n = new AtomicInteger(0);
op.sendMessage(
        e, "Most Valuable Players", String.join(
                "\n", getResourceManager().getPrimaryResource().getUsers().stream().collect(
                        toMap(
                                Function.identity(),
                                user -> getResourceManager()
                                        .getResources()
                                        .stream()
                                        .map(r -> r.getValue(user) * r.getValue())
                                        .reduce((long) 0, (a, b) -> a + b)
                        )
                ).entrySet().stream()
                        .sorted(
                                (o1, o2) -> Long.compare(o2.getValue(), o1.getValue())
                        )
                        .limit(10)
                        .map(
                                entry -> String.format(
                                        "[%d] <@%s> -> **%s**",
                                        n.incrementAndGet(),
                                        entry.getKey(),
                                        getResourceManager().getPrimaryResource().prettyValue(
                                                entry.getValue()
                                        )
                                )
                        ).collect(Collectors.toList())
        )
);
    }
}
