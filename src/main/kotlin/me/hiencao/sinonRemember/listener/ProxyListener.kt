package me.hiencao.sinonRemember.listener

import me.hiencao.sinonRemember.SinonRemember
import me.hiencao.sinonRemember.data.DataManager
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.event.ServerKickEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class ProxyListener : Listener {
    @EventHandler
    fun onServerConnectEvent(event: ServerConnectEvent) {
        val group = SinonRemember.instance.getServerGroup(event.target.name) ?: return
        val lastConnectedServer = DataManager.getData(event.player.uniqueId.toString(), group.id)

        event.target = lastConnectedServer
    }

    @EventHandler
    fun onServerConnectedEvent(event: ServerConnectedEvent) {
        val group = SinonRemember.instance.getServerGroup(event.server.info.name) ?: return
        DataManager.insertData(event.player.uniqueId.toString(), group.id, event.server.info.name)
    }

    @EventHandler
    fun onPlayerKick(event: ServerKickEvent) {
        val player = event.player
        val currentServer = event.kickedFrom
        val reason = event.reason.toPlainText()

        if (reason.contains("Timed out", ignoreCase = true) || reason.contains("Connection lost", ignoreCase = true)) {
            val group = SinonRemember.instance.getServerGroup(currentServer.name)

            if (group != null) {
                val fallbackServer = SinonRemember.instance.findAvailableServer(group.fallbacks[0])

                if (fallbackServer != currentServer) {
                    event.cancelServer = fallbackServer
                    event.isCancelled = true
                    player.sendMessage(TextComponent("You were disconnected due to a timeout. Redirecting to a fallback server..."))
                }
            }
        }
    }
}