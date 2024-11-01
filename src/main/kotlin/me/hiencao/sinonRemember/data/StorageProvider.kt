package me.hiencao.sinonRemember.data

import me.hiencao.sinonRemember.ServerGroup
import me.hiencao.sinonRemember.SinonRemember
import net.md_5.bungee.api.config.ServerInfo

interface StorageProvider {
    val plugin: SinonRemember

    fun load()
    fun insertData(uuid: String, group: String, server: String)
    fun getData(uuid: String, group: String): ServerInfo
}