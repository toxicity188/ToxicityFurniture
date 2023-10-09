package kor.toxicity.furniture

import kor.toxicity.furniture.api.FurnitureAPI
import kor.toxicity.furniture.api.blueprint.FurnitureBlueprint
import kor.toxicity.furniture.api.entity.FurnitureEntity
import kor.toxicity.furniture.blueprint.FurnitureBlueprintImpl
import kor.toxicity.furniture.manager.EntityManager
import kor.toxicity.furniture.manager.ResourcePackManager
import kor.toxicity.furniture.nms.NMS
import kor.toxicity.toxicitylibs.api.command.CommandAPI
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ToxicityFurnitureImpl: FurnitureAPI() {
    companion object {
        private lateinit var plugin: ToxicityFurnitureImpl

        lateinit var nms: NMS
            private set
        lateinit var audiences: BukkitAudiences
            private set


        fun send(message: String) = plugin.logger.info(message)
        fun warn(message: String) = plugin.logger.warning(message)

        private val managers = arrayOf(
            ResourcePackManager,
            EntityManager
        )
    }

    val command = CommandAPI("<gradient:aqua-blue>[Furniture]")
        .setCommandPrefix("fu")

        .setUnknownCommandMessage("Unknown command. try \"/fu help\" to find a command.")
        .setNotCommandMessage("Unknown command.")
        .setOpOnlyCommandMessage("This command is op only.")
        .setNotAllowedSenderMessage("You are not allowed sender.")
        .setPermissionRequiredMessage("Sorry, you don't have a permission to do that.")

        .helpBuilder
        .setDescription("All sub command in Furniture")
        .setPermission(arrayOf("furniture.help"))
        .build()

        .create("reload")
        .setAliases(arrayOf("re","rl"))
        .setDescription("reload this plugin.")
        .setUsage("reload")
        .setPermission(arrayOf("furniture.reload"))
        .setExecutor { t, _ ->
            Bukkit.getScheduler().runTaskAsynchronously(this) { _ ->
                var time = System.currentTimeMillis()
                load()
                time = System.currentTimeMillis() - time
                Bukkit.getScheduler().runTask(this) { _ ->
                    audiences.sender(t).sendMessage(Component.text("Reload completed! ($time ms)").color(NamedTextColor.GREEN))
                }
            }
        }
        .build()

    override fun onEnable() {
        super.onEnable()
        plugin = this
        try {
            nms = Class.forName("kor.toxicity.furniture.nms.${Bukkit.getServer()::class.java.`package`.name.split('.')[3]}.NMSImpl").getConstructor().newInstance() as NMS
        } catch (ex: Exception) {
            warn("Unsupported version found.")
            warn("Plugin will be disabled.")
            return
        }
        audiences = BukkitAudiences.create(this)
        getCommand("furniture")?.setExecutor(command.createTabExecutor())
        managers.forEach {
            it.start(this)
        }
        Bukkit.getScheduler().runTask(this) { _ ->
            load()
            send("Plugin enabled.")
        }
    }

    override fun onDisable() {
        managers.forEach {
            it.end(this)
        }
        send("Plugin disabled.")
    }

    private val lazyTaskList = ArrayList<() -> Unit>()
    private val syncTaskList = ArrayList<() -> Unit>()
    fun addLazyTask(action: () -> Unit) {
        lazyTaskList.add(action)
    }
    fun addSyncTask(action: () -> Unit) {
        syncTaskList.add(action)
    }
    private fun load() {
        loadFile("config")?.let {
            Config.reload(it)
        }
        managers.forEach {
            it.reload(this)
        }
        lazyTaskList.forEach {
            it()
        }
        lazyTaskList.clear()
        Bukkit.getScheduler().runTask(this) { _ ->
            syncTaskList.forEach {
                it()
            }
            syncTaskList.clear()
        }
    }

    object Config {
        var furnitureMaterial = Material.FLINT
            private set
        var renderDistance = 2
            private set

        internal fun reload(section: ConfigurationSection) {
            section.getString("furniture-material")?.let {
                try {
                    furnitureMaterial = Material.valueOf(it.uppercase())
                } catch (ex: Exception) {
                    warn("The material \"$it\" doesn't exist.")
                }
            }
            renderDistance = (section.getInt("render-distance").coerceAtLeast(16) / 16)
        }
    }

    fun loadFolder(dir: String, action: (File,ConfigurationSection) -> Unit) {
        File(dataFolder.apply {
            if (!exists()) mkdir()
        },dir).apply {
            if (!exists()) mkdir()
        }.listFiles()?.forEach {
            if (it.extension == "yml") {
                try {
                    action(it,YamlConfiguration().apply {
                        load(it)
                    })
                } catch (ex: Exception) {
                    warn("Unable to load this file: ${it.name}")
                    warn("Reason: ${ex.message}")
                }
            }
        }
    }
    fun loadFile(name: String) = try {
        YamlConfiguration().apply {
            load(File(dataFolder.apply {
                if (!exists()) mkdir()
            },"$name.yml").apply {
                if (!exists()) saveResource("$name.yml",false)
            })
        }
    } catch (ex: Exception) {
        warn("Unable to load this file: $name.yml")
        warn("Reason: ${ex.message}")
        null
    }

    override fun getBlueprint(name: String): FurnitureBlueprint? {
        return EntityManager.getBlueprint(name)
    }

    override fun getByUUID(world: World, uuid: UUID): FurnitureEntity? {
        return EntityManager.getByUUID(world, uuid)
    }

    override fun create(blueprint: FurnitureBlueprint, location: Location): FurnitureEntity {
        return EntityManager.spawn(this, blueprint as FurnitureBlueprintImpl, location)
    }

    override fun remove(entity: FurnitureEntity) {
        EntityManager.deSpawn(this, entity)
    }
}