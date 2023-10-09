package kor.toxicity.furniture.manager

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.stream.JsonWriter
import kor.toxicity.furniture.ToxicityFurnitureImpl
import kor.toxicity.furniture.api.entity.FurnitureEntity
import kor.toxicity.furniture.api.event.FurnitureInteractEvent
import kor.toxicity.furniture.blueprint.BaseFurnitureBlueprint
import kor.toxicity.furniture.blueprint.FurnitureBlueprintImpl
import kor.toxicity.furniture.blueprint.ModelEngineFurnitureBlueprint
import kor.toxicity.furniture.chunk.ChunkLoc
import kor.toxicity.furniture.entity.BaseFurnitureEntity
import kor.toxicity.furniture.entity.FurnitureEntityImpl
import kor.toxicity.furniture.entity.ModelEngineFurnitureEntity
import kor.toxicity.furniture.extension.FURNITURE_ITEM_KEY
import kor.toxicity.furniture.extension.GSON
import kor.toxicity.furniture.extension.call
import kor.toxicity.furniture.registry.EntityRegistry
import kor.toxicity.toxicitylibs.api.command.SenderType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object EntityManager: FurnitureManager {

    private val entityRegistryMap = ConcurrentHashMap<UUID, EntityRegistry>()
    private val blueprintMap = HashMap<String, FurnitureBlueprintImpl>()

    private val playerThreadMap = HashMap<UUID, BukkitTask>()

    private val remover = ItemStack(Material.BONE).apply {
        itemMeta = itemMeta?.apply {
            setDisplayName(ChatColor.GOLD.toString() + "Remover")
            lore = listOf(
                ChatColor.WHITE.toString() + "Right click - remove furniture"
            )
        }
    }

    override fun start(furniture: ToxicityFurnitureImpl) {

        furniture.command
            .create("give")
            .setAliases(arrayOf("g"))
            .setDescription("give furniture item.")
            .setUsage("give <player> <item>")
            .setPermission(arrayOf("furniture.give"))
            .setLength(2)
            .setExecutor { t, u ->
                Bukkit.getPlayer(u[0])?.let { player ->
                    blueprintMap[u[1]]?.let { blueprint ->
                        player.inventory.addItem(if (blueprint is BaseFurnitureBlueprint) blueprint.asset else ItemStack(Material.FLINT).apply {
                            itemMeta = itemMeta?.apply {
                                setDisplayName("ModelEngine furniture: " + u[1])
                                persistentDataContainer.set(FURNITURE_ITEM_KEY, PersistentDataType.STRING, u[1])
                            }
                        })
                        ToxicityFurnitureImpl.audiences.sender(t).sendMessage(Component.text("Successfully given!").color(NamedTextColor.GREEN))
                    } ?: ToxicityFurnitureImpl.audiences.sender(t).sendMessage(Component.text("This item doesn't exist!").color(NamedTextColor.RED))
                } ?: ToxicityFurnitureImpl.audiences.sender(t).sendMessage(Component.text("This player is not online!").color(NamedTextColor.RED))
            }
            .setTabCompleter { _, u ->
                if (u.size == 2) blueprintMap.keys.filter {
                    it.contains(u[1])
                } else null
            }
            .build()
            .create("save")
            .setAliases(arrayOf("s"))
            .setDescription("save current furniture.")
            .setUsage("save")
            .setPermission(arrayOf("furniture.save"))
            .setExecutor { t, _ ->
                Bukkit.getScheduler().runTaskAsynchronously(furniture) { _ ->
                    save(furniture)
                    ToxicityFurnitureImpl.audiences.sender(t).sendMessage(Component.text("Save finished!").color(NamedTextColor.GREEN))
                }
            }
            .build()
            .create("remover")
            .setAliases(arrayOf("r"))
            .setDescription("get remover.")
            .setUsage("remover")
            .setPermission(arrayOf("furniture.remover"))
            .setAllowedSender(arrayOf(SenderType.PLAYER))
            .setExecutor { t, _ ->
                (t as Player).inventory.addItem(remover)
                ToxicityFurnitureImpl.audiences.sender(t).sendMessage(Component.text("Successfully given.").color(NamedTextColor.GREEN))
            }
            .build()

        Bukkit.getWorlds().forEach {
            entityRegistryMap[it.uid] = EntityRegistry(furniture,it)
        }
        Bukkit.getPluginManager().registerEvents(object : Listener {

            private val currentRegistry = HashMap<UUID, EntityRegistry>()

            @EventHandler
            fun join(e: PlayerJoinEvent) {
                val player = e.player
                change(player, entityRegistryMap.getOrPut(player.world.uid) {
                    EntityRegistry(furniture,player.world)
                })
                Bukkit.getScheduler().runTaskAsynchronously(furniture) { _ ->
                    enable(furniture,player)
                    playerThreadMap[player.uniqueId] = Bukkit.getScheduler().runTaskTimerAsynchronously(furniture, Runnable {
                        enable(furniture,player)
                    },5,5)
                }
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                currentRegistry.remove(player.uniqueId)?.removeEntity(player)
                playerThreadMap.remove(player.uniqueId)?.cancel()
            }
            @EventHandler
            fun interact(e: PlayerInteractAtEntityEvent) {
                val player = e.player
                val register = entityRegistryMap.getOrPut(player.world.uid) {
                    EntityRegistry(furniture,player.world)
                }
                register.getByUUID(e.rightClicked.uniqueId)?.let {
                    FurnitureInteractEvent(player,it,e).call()
                    e.isCancelled = true
                    if (remover.isSimilar(player.inventory.itemInMainHand)) {
                        register.removeEntity(it)
                    }
                }
            }
            @EventHandler
            fun unload(e: ChunkUnloadEvent) {
                val chunk = e.chunk
                val world = e.world
                entityRegistryMap.getOrPut(world.uid) {
                    EntityRegistry(furniture,world)
                }.removeEntity(ChunkLoc(chunk.x,chunk.z))
            }
            @EventHandler
            fun load(e: ChunkLoadEvent) {
                val chunk = e.chunk
                val world = e.world
                entityRegistryMap.getOrPut(world.uid) {
                    EntityRegistry(furniture,world)
                }.add(ChunkLoc(chunk.x,chunk.z))
            }
            @EventHandler
            fun worldChange(e: PlayerChangedWorldEvent) {
                val player = e.player
                change(player, entityRegistryMap.getOrPut(player.world.uid) {
                    EntityRegistry(furniture,player.world)
                })
            }
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            fun interact(e: PlayerInteractEvent) {
                val item = e.item ?: return
                val block = e.clickedBlock ?: return
                val player = e.player
                if (e.action == Action.RIGHT_CLICK_BLOCK) {
                    item.itemMeta?.persistentDataContainer?.get(FURNITURE_ITEM_KEY, PersistentDataType.STRING)?.let {
                        blueprintMap[it]?.let { blueprint ->
                            val playerLoc = player.location
                            var playerYaw = playerLoc.yaw
                            if (playerYaw < 0) playerYaw += 360

                            spawn(furniture, blueprint, block.location.apply {
                                x += 0.5
                                y += 1
                                z += 0.5
                                yaw = ((playerYaw.toInt() / 90) * 90).toFloat()
                            })

                            e.isCancelled = true
                        }
                    }
                }
            }

            @EventHandler
            fun damage(e: EntityDamageEvent) {
                val world = e.entity.world
                if (entityRegistryMap.getOrPut(world.uid) {
                    EntityRegistry(furniture,world)
                }.getByUUID(e.entity.uniqueId) != null) e.isCancelled = true
            }

            private fun change(player: Player, registry: EntityRegistry) {
                currentRegistry.put(player.uniqueId,registry.apply {
                    add(player)
                })?.removeEntity(player)
            }

        },furniture)
        Bukkit.getScheduler().runTaskTimerAsynchronously(furniture, { _ ->
            save(furniture)
        },6000,6000)
    }

    private fun save(furniture: ToxicityFurnitureImpl) {
        val data = File(furniture.dataFolder.apply {
            if (!exists()) mkdir()
        }, ".data").apply {
            if (!exists()) mkdir()
        }
        Bukkit.getWorlds().forEach { world ->
            try {
                val json = JsonArray()
                entityRegistryMap[world.uid]?.forEach {
                    if (it.blueprint.isMarkedToSave) json.add(JsonArray().apply {
                        val loc = it.location
                        add(it.blueprint.key)
                        add(loc.x)
                        add(loc.y)
                        add(loc.z)
                        add(loc.yaw)
                        add(loc.pitch)
                    })
                }
                JsonWriter(File(data, "${world.name}.json").bufferedWriter()).use { writer ->
                    GSON.toJson(json, writer)
                }
            } catch (ex: Exception) {
                ToxicityFurnitureImpl.warn("Unable to save this file: ${world.name}.json")
            }
        }
    }

    override fun reload(furniture: ToxicityFurnitureImpl) {
        if (blueprintMap.isNotEmpty()) {
            save(furniture)
            blueprintMap.clear()
        }
        entityRegistryMap.forEach {
            it.value.removeEntity(false)
        }
        furniture.loadFolder("blueprints") { file, section ->
            section.getKeys(false).forEach { str ->
                section.getConfigurationSection(str)?.let { config ->
                    try {
                        when (val type = (config.getString("type") ?: "base").lowercase()) {
                            "base" -> {
                                blueprintMap[str] = BaseFurnitureBlueprint(str, config)
                            }
                            "modelengine" -> {
                                try {
                                    blueprintMap[str] = ModelEngineFurnitureBlueprint(str, config)
                                } catch (ex: Throwable) {
                                    throw RuntimeException("Unable to find ModelEngine")
                                }
                            }
                            else -> throw RuntimeException("Unable to find this type: $type")
                        }
                    } catch (ex: Exception) {
                        ToxicityFurnitureImpl.warn("Unable to read this blueprint: $str in ${file.name}")
                        ToxicityFurnitureImpl.warn("Reason: ${ex.message}")
                    }
                }
            }
        }
        val data = File(furniture.dataFolder.apply {
            if (!exists()) mkdir()
        }, ".data").apply {
            if (!exists()) mkdir()
        }
        Bukkit.getWorlds().forEach { world ->
            try {
                File(data,"${world.name}.json").apply {
                    if (!exists()) outputStream().buffered().use {
                        it.write("[]".toByteArray())
                    }
                }.bufferedReader().use { reader ->
                    JsonParser.parseReader(reader).asJsonArray.forEach { element ->
                        val array = element.asJsonArray
                        blueprintMap[array[0].asString]?.let {
                            place(furniture, it, Location(
                                world,
                                array[1].asDouble,
                                array[2].asDouble,
                                array[3].asDouble,
                                array[4].asFloat,
                                array[5].asFloat,
                            ))
                        }
                    }
                }
            } catch (ex: Exception) {
                ToxicityFurnitureImpl.warn("Unable to read this file: ${world.name}.json")
                ToxicityFurnitureImpl.warn("Reason: ${ex.message}")
            }
        }
        Bukkit.getOnlinePlayers().forEach { player ->
            enable(furniture, player)
        }
    }
    private fun enable(furniture: ToxicityFurnitureImpl, player: Player) {
        val world = player.world
        val loc = player.location.chunk
        val chunk = ChunkLoc(loc.x,loc.z)
        val set = HashSet<ChunkLoc>()
        fun enable(chunk: ChunkLoc, count: Int) {
            if (count < ToxicityFurnitureImpl.Config.renderDistance) {
                val i = count + 1

                enable(ChunkLoc(chunk.x, chunk.z + 1), i)
                enable(ChunkLoc(chunk.x + 1, chunk.z), i)
                enable(ChunkLoc(chunk.x + 1, chunk.z + 1), i)


                enable(ChunkLoc(chunk.x, chunk.z - 1), i)
                enable(ChunkLoc(chunk.x - 1, chunk.z), i)
                enable(ChunkLoc(chunk.x - 1, chunk.z - 1), i)

                enable(ChunkLoc(chunk.x + 1, chunk.z - 1), i)
                enable(ChunkLoc(chunk.x - 1, chunk.z + 1), i)
            } else {
                set.add(ChunkLoc(chunk.x,chunk.z))
            }
        }
        enable(chunk, 0)
        val registry = entityRegistryMap.getOrPut(world.uid) {
            EntityRegistry(furniture,world)
        }
        set.forEach {
            if (registry.enableChunk(it)) registry.add(player, false)
        }
    }

    override fun end(furniture: ToxicityFurnitureImpl) {
        save(furniture)
    }

    fun spawn(furniture: ToxicityFurnitureImpl, blueprint: FurnitureBlueprintImpl, location: Location): FurnitureEntity {
        val world = location.world!!
        return place(furniture, blueprint, location).apply {
            Bukkit.getOnlinePlayers().forEach { targetPlayer ->
                if (targetPlayer.world.uid == world.uid) spawn(targetPlayer)
            }
        }
    }
    private fun place(furniture: ToxicityFurnitureImpl, blueprint: FurnitureBlueprintImpl, location: Location): FurnitureEntity {
        val world = location.world!!
        var rotateYaw = location.yaw
        if (rotateYaw < 0) rotateYaw += 360
        val entity = when (blueprint) {
            is BaseFurnitureBlueprint -> BaseFurnitureEntity(furniture, blueprint, location)
            is ModelEngineFurnitureBlueprint -> ModelEngineFurnitureEntity(furniture, blueprint, location)
        }
        entityRegistryMap.getOrPut(world.uid) {
            EntityRegistry(furniture,world)
        }.addEntity(entity)
        return entity
    }

    fun deSpawn(furniture: ToxicityFurnitureImpl, entity: FurnitureEntityImpl) {
        val loc = entity.location
        val world = loc.world!!
        entityRegistryMap.getOrPut(world.uid) {
            EntityRegistry(furniture, world)
        }.removeEntity(entity)
    }

    fun getBlueprint(name: String) = blueprintMap[name]
    fun getByUUID(world: World, uuid: UUID) = entityRegistryMap[world.uid]?.getByUUID(uuid)
}