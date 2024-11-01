package me.hiencao.sinonRemember.data.provider

import me.hiencao.sinonRemember.SinonRemember
import me.hiencao.sinonRemember.config.ConfigManager
import me.hiencao.sinonRemember.data.StorageProvider
import net.md_5.bungee.api.config.ServerInfo
import redis.clients.jedis.JedisPool

private const val REDIS_KEY = "sinonRemember"

class RedisStorage(override val plugin: SinonRemember) : StorageProvider {
    private lateinit var pool: JedisPool

    override fun load() {
        val config = ConfigManager.redisConfig

        pool = JedisPool(config.host, config.port)
        if (config.password.isNotEmpty()) {
            pool.resource.use { jedis ->
                jedis.auth(config.password)
            }
        }

        pool.resource.use { jedis ->
            val res = jedis.ping();
            if (res != "PONG") {
                throw IllegalStateException("Failed to connect to Redis server")
            } else {
                plugin.logger.info("Connected to Redis server")
            }
        }
    }

    override fun insertData(uuid: String, group: String, server: String) {
        pool.resource.use { jedis ->
            jedis.hset(REDIS_KEY + uuid, group, server)
        }
    }

    override fun getData(uuid: String, group: String): ServerInfo {
        return pool.resource.use { jedis ->
            val server = jedis.hget(REDIS_KEY + uuid, group);
            if (server != null) {
                plugin.findAvailableServer(server)
            } else {
                val groupServers = ConfigManager.serverGroups.firstOrNull { it.id == group }?.servers
                    ?: throw IllegalStateException("Server group not found for group ID: $group")

                if (groupServers.isNotEmpty()) {
                    plugin.findAvailableServer(groupServers[0])
                } else {
                    throw IllegalStateException("No servers configured for group ID: $group")
                }
            }
        }
    }
}