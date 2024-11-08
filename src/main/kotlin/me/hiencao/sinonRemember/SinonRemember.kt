package me.hiencao.sinonRemember

import me.hiencao.sinonRemember.command.MainCommand
import me.hiencao.sinonRemember.config.ConfigManager
import me.hiencao.sinonRemember.data.DataManager
import me.hiencao.sinonRemember.listener.ProxyListener
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.plugin.Plugin
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class SinonRemember : Plugin() {
    override fun onEnable() {
        instance = this
        ConfigManager.init(this)
        DataManager.init(this)
        proxy.pluginManager.registerListener(this, ProxyListener())
        proxy.pluginManager.registerCommand(this, MainCommand("sinonremember"))
    }

    override fun onDisable() {
    }

    fun getServerGroup(serverName: String): ServerGroup? {
        return ConfigManager.serverGroups.firstOrNull { it.servers.contains(serverName) }
    }

    fun isServerOnline(serverName: String): Boolean {
        return proxy.servers[serverName]?.let {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(it.address.hostString, it.address.port), 1000)
                }
                true
            } catch (e: IOException) {
                false
            }
        } ?: false
    }

    fun findAvailableServer(servers: List<String>): ServerInfo? {
        return servers.mapNotNull { proxy.getServerInfo(it) }.firstOrNull { isServerOnline(it.name) }
    }


    companion object {
        @JvmStatic
        lateinit var instance: SinonRemember
            private set
    }
}


data class ServerGroup(val id: String, val servers: List<String>, val fallbacks: List<String>)
data class RedisConfig(val host: String, val port: Int, val password: String)
enum class StorageType {
    REDIS,
    YAML
}