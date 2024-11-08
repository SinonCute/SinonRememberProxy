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
            throw IllegalStateException("Failed to insert data for player with UUID: $uuid")
        }
        configData.set(uuid, players[uuid])
        ConfigManager.saveFile("players.yml", configData)
    }

    override fun getData(uuid: String, group: String): ServerInfo? {
        if (players.containsKey(uuid)) {
            val playerData = players[uuid]
            if (playerData?.containsKey(group) == true) {
                plugin.proxy.getServerInfo(playerData[group])?.let {
                    return it
                }
            }
        }
        return null
    }
}