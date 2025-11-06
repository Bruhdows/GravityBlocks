package com.bruhdows.gravityblocks.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.awt.*;

public class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component color(String content) {
        return MINI_MESSAGE.deserialize(content);
    }

    public static void sendMessage(Player player, String content) {
        player.sendMessage(color(content));
    }

    public static void sendActionBar(Player player, String content) {
        player.sendActionBar(color(content));
    }
}
