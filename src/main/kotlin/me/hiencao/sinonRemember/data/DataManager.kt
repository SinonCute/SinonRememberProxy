package me.hiencao.sinonRemember.data

import me.hiencao.sinonRemember.SinonRemember
import me.hiencao.sinonRemember.StorageType
import me.hiencao.sinonRemember.config.ConfigManager
import me.hiencao.sinonRemember.data.provider.RedisStorage
import me.hiencao.sinonRemember.data.provider.YamlProvider
import net.md_5.bungee.api.config.ServerInfo

object DataManager {
    private lateinit var plugin: SinonRemember
    private lateinit var storageProvider: StorageProvider

    fun init(plugin: SinonRemember) {
        this.plugin = plugin
        storageProvider = when (ConfigManager.storageType) {
            StorageType.YAML -> {
                YamlProvider(plugin)
            }
            StorageType.REDIS -> {
                RedisStorage(plugin)
            }
        }
        plugin.logger.info("Using ${ConfigManager.storageType} storage provider")
        storageProvider.load()
    }

    fun reload() {
        storageProvider.load()
    }

    fun insertData(uuid: String, group: String, server: String) {
        storageProvider.insertData(uuid, group, server)
    }

    fun getData(uuid: String, group: String): ServerInfo {
        return storageProvider.getData(uuid, group)
    }
}