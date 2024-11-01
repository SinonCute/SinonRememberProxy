package me.hiencao.sinonRemember.data.provider

import me.hiencao.sinonRemember.SinonRemember
import me.hiencao.sinonRemember.config.ConfigManager
import me.hiencao.sinonRemember.data.StorageProvider
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.config.Configuration

class YamlProvider(override val plugin: SinonRemember) : StorageProvider {
    private lateinit var configData: Configuration
    private val players: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    override fun load() {
        configData = ConfigManager.loadFile("players.yml")
        configData.keys.forEach { uuid ->
            val section = configData.getSection(uuid)
            val map = mutableMapOf<String, String>()
            section.keys.forEach { group ->
                map[group] = section.getString(group)
            }
            players[uuid] = map
        }
    }

    override fun insertData(uuid: String, group: String, server: String) {
        try {
            if (players.containsKey(uuid)) {
                players[uuid]?.set(group, server)
            } else {
                players[uuid] = mutableMapOf(group to server)
            }
        } catch (e: ClassCastException) {
            if (players.containsKey(uuid)) {
                players[uuid]?.set(group, server)
            } else {
                players[uuid] = mutableMapOf(group to server)
            }
        }
        configData.set(uuid, players[uuid])
        ConfigManager.saveFile("players.yml", configData)
    }

    override fun getData(uuid: String, group: String): ServerInfo {
        return try {
            if (players.containsKey(uuid)) {
                val playerData = players[uuid]
                if (playerData?.containsKey(group) == true) {
                    return plugin.findAvailableServer(playerData[group]!!)
                }
            }

            val groupServers = ConfigManager.serverGroups.firstOrNull { it.id == group }?.servers
                ?: throw IllegalStateException("Server group not found for group ID: $group")

            if (groupServers.isNotEmpty()) {
                plugin.findAvailableServer(groupServers[0])
            } else {
                throw IllegalStateException("No servers configured for group ID: $group")
            }
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("No servers configured for group ID: $group")
        }
    }

}