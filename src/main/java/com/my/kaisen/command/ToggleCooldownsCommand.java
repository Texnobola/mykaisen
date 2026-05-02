package com.my.kaisen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.my.kaisen.network.CombatTickHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ToggleCooldownsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("mykaisen")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("cooldowns")
                        .executes(context -> {
                            CombatTickHandler.cooldownsEnabled = !CombatTickHandler.cooldownsEnabled;
                            
                            String status = CombatTickHandler.cooldownsEnabled ? "ON" : "OFF";
                            context.getSource().sendSuccess(() -> Component.literal("Ability cooldowns are now " + status), true);
                            
                            return 1;
                        })
                );

        dispatcher.register(command);
    }
}
