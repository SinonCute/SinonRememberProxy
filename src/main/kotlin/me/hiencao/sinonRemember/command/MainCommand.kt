package me.hiencao.sinonRemember.command

import me.hiencao.sinonRemember.SinonRemember
import me.hiencao.sinonRemember.config.ConfigManager
import me.hiencao.sinonRemember.data.DataManager
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command


class MainCommand(name: String?) : Command(name) {
    override fun execute(sender: CommandSender, args: Array<String>) {

        if (args.isEmpty()) {
            val message = TextComponent("Usage: /sinonremember reload")
            message.color = ChatColor.AQUA
            sender.sendMessage(message)
            return
        }

        when (args[0]) {
            "reload" -> {
                ConfigManager.loadConfig();
                val message = TextComponent("Reloading configuration...")
                message.color = ChatColor.GREEN
                sender.sendMessage(message)
            }
            else -> {
                val message = TextComponent("Unknown command")
                message.color = ChatColor.RED
                sender.sendMessage(message)
            }
        }
    }
}