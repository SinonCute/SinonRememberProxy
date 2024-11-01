package me.hiencao.sinonRemember.config

import jdk.jfr.internal.SecuritySupport.getResourceAsStream
import me.hiencao.sinonRemember.RedisConfig
import me.hiencao.sinonRemember.ServerGroup
import me.hiencao.sinonRemember.SinonRemember
import me.hiencao.sinonRemember.StorageType
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object ConfigManager {
    private lateinit var plugin: SinonRemember

    lateinit var serverGroups: List<ServerGroup>
    lateinit var storageType: StorageType
    lateinit var redisConfig: RedisConfig
    lateinit var fallbackServer: String


    fun init(plugin: SinonRemember) {
        this.plugin = plugin
        loadConfig()
    }

    private fun generateDefaultFile(fileName: String) {
       if (plugin.dataFolder.exists()) {
           plugin.dataFolder.mkdirs()
       }

        val configFile = File(plugin.dataFolder, fileName)

        if (!configFile.exists()) {
            try {
                val outputStream = FileOutputStream(configFile)
                val `in`: InputStream = getResourceAsStream(fileName)
                `in`.transferTo(outputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadFile(fileName: String) : Configuration {
        return ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(File(plugin.dataFolder, fileName))
    }

    fun saveFile(fileName: String, config: Configuration) {
        ConfigurationProvider.getProvider(YamlConfiguration::class.java).save(config, File(plugin.dataFolder, fileName))
    }

    fun loadConfig() {
        generateDefaultFile("config.yml")
        val config = loadFile("config.yml")

        serverGroups = config.getSection("server_groups").keys.map { key ->
            val section = config.getSection("server_groups.$key")
            ServerGroup(
                key,
                section.getStringList("servers"),
                section.getStringList("fallbacks")
            )
        }

        fallbackServer = config.getString("fallback_server")

        storageType = StorageType.valueOf(config.getString("storage_type"))

        redisConfig = RedisConfig(
            config.getString("redis.host"),
            config.getInt("redis.port"),
            config.getString("redis.password")
        )
    }
}