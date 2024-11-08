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
        val player = event.player
        val playerUUID = player.uniqueId.toString()
        val fromServer = player.server?.info
        val targetServer = event.target

        val targetGroup = SinonRemember.instance.getServerGroup(targetServer.name) ?: return
        val lastConnectedServer = DataManager.getData(playerUUID, targetGroup.id)

        if (lastConnectedServer != null && fromServer != null &&
            lastConnectedServer != targetServer && lastConnectedServer != fromServer) {

            if (!SinonRemember.instance.isServerOnline(lastConnectedServer.name)) {
                val fallback = SinonRemember.instance.findAvailableServer(targetGroup.fallbacks)
                if (fallback != null) {
                    event.target = fallback
                    player.sendMessage(TextComponent("Server bạn online lần cuối không hoạt động. Đang chuyển hướng đến server phụ..."))
                    return
                } else {
                    event.isCancelled = true
                    player.sendMessage(TextComponent("Server hiện đang bảo trì. Vui lòng thử lại sau. Nếu bạn nghĩ đây là một lỗi, vui lòng liên hệ với quản trị viên."))
                }
            }

            event.target = lastConnectedServer
        }
    }


    @EventHandler
    fun onServerConnectedEvent(event: ServerConnectedEvent) {
        val playerUUID = event.player.uniqueId.toString()
        val targetName = event.server.info.name
        val group = SinonRemember.instance.getServerGroup(targetName) ?: return
        DataManager.insertData(playerUUID, group.id, targetName)
    }

    @EventHandler
    fun onPlayerKick(event: ServerKickEvent) {
        val player = event.player
        val currentServer = event.kickedFrom
        val reason = event.reason.toPlainText()

        if (reason.contains("Timed out", ignoreCase = true) || reason.contains("Connection lost", ignoreCase = true)) {
            val group = SinonRemember.instance.getServerGroup(currentServer.name)

            if (group != null) {
                val fallbackServer = SinonRemember.instance.findAvailableServer(group.fallbacks)

                if (fallbackServer != currentServer) {
                    event.cancelServer = fallbackServer
                    event.isCancelled = true
                    player.sendMessage(TextComponent("You were disconnected due to a timeout. Redirecting to a fallback server..."))
                }
            }
        }
    }
}