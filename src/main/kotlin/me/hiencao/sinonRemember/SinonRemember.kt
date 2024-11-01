package me.hiencao.sinonRemember

import me.hiencao.sinonRemember.config.ConfigManager
import me.hiencao.sinonRemember.data.DataManager
import me.hiencao.sinonRemember.listener.ProxyListener
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.plugin.Plugin
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class SinonRemember : Plugin() {

    private lateinit var instance: SinonRemember

    override fun onEnable() {
        instance = this
        ConfigManager.init(this)
        DataManager.init(this)
        proxy.pluginManager.registerListener(this, ProxyListener())
    }

    override fun onDisable() {
    }

    fun getServerGroup(group: String): ServerGroup? {
        return ConfigManager.serverGroups.firstOrNull { it.id == group }
    }

    fun findAvailableServer(serverName: String, fallbackIndex: Int = 0): ServerInfo {
        if (ConfigManager.serverGroups.isEmpty() || ConfigManager.serverGroups.none { it.id == serverName }) {
            return proxy.servers[ConfigManager.fallbackServer]
                ?: throw IllegalStateException("Fallback server is not configured correctly.")
        }

        val primaryServer = proxy.servers[serverName] ?: throw IllegalStateException("Primary server is not configured correctly.")
        val fallbackServers = ConfigManager.serverGroups.firstOrNull { it.id == serverName }?.fallbacks

        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(primaryServer.address.hostString, primaryServer.address.port), 1000)
            }
            return primaryServer
        } catch (e: IOException) {
            return if (fallbackServers != null && fallbackIndex < fallbackServers.size) {
                findAvailableServer(fallbackServers[fallbackIndex], fallbackIndex + 1)
            } else {
                proxy.servers[ConfigManager.fallbackServer]
                    ?: throw IllegalStateException("Fallback server is not configured correctly.")
            }
        }
    }


    companion object {
        lateinit var instance: SinonRemember
    }
}


data class ServerGroup(val id: String, val servers: List<String>, val fallbacks: List<String>)
data class RedisConfig(val host: String, val port: Int, val password: String)
enum class StorageType {
    REDIS,
    YAML
}