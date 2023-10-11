package kor.toxicity.furniture.manager

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonWriter
import kor.toxicity.furniture.ToxicityFurnitureImpl
import kor.toxicity.furniture.extension.GSON
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.max

object ResourcePackManager: FurnitureManager {
    private val jsonFaces = arrayOf(
        "north",
        "east",
        "south",
        "west",
        "up",
        "down"
    )

    private val customModelDataMap = HashMap<String, Int>()

    fun getCustomModelData(name: String) = customModelDataMap[name]

    override fun start(furniture: ToxicityFurnitureImpl) {
    }

    override fun reload(furniture: ToxicityFurnitureImpl) {
        customModelDataMap.clear()
        val dataFolder = furniture.dataFolder.apply {
            if (!exists()) mkdir()
        }
        val generated = File(dataFolder, ".generated").apply {
            deleteRecursively()
            mkdir()
        }
        val assets = File(generated, "assets").apply {
            mkdir()
        }
        val minecraftFolder = File(File(File(assets, "minecraft").apply {
            mkdir()
        },"models").apply {
            mkdir()
        },"item").apply {
            mkdir()
        }
        val furnitureFolder = File(assets, "furniture").apply {
            mkdir()
        }
        val furnitureModels = File(furnitureFolder, "models").apply {
            mkdir()
        }
        val furnitureTextures = File(File(furnitureFolder, "textures").apply {
            mkdir()
        },"item").apply {
            mkdir()
        }
        try {
            val itemName = ToxicityFurnitureImpl.Config.furnitureMaterial.name.lowercase()
            val json = JsonObject().apply {
                addProperty("parent", "minecraft:item/generated")
                add("textures", JsonObject().apply {
                    addProperty("layer0", "minecraft:item/$itemName")
                })
            }
            val overrides = JsonArray()
            var i = 0
            File(dataFolder, "assets").apply {
                if (!exists()) {
                    mkdir()
                    furniture.resourcesForEach("assets") { name, stream ->
                        File(this, name).outputStream().buffered().use {
                            stream.copyTo(it)
                        }
                    }
                }
            }.listFiles()?.forEach {
                if (it.extension == "bbmodel") {
                    readBlockBenchModel(it, furnitureModels, furnitureTextures)
                    overrides.add(JsonObject().apply {
                        i++
                        add("predicate", JsonObject().apply {
                            addProperty("custom_model_data", i)
                        })
                        val nameWithoutExtension = it.nameWithoutExtension
                        addProperty("model","furniture:${nameWithoutExtension}")
                        customModelDataMap[nameWithoutExtension] = i
                    })
                }
            }
            json.add("overrides", overrides)
            JsonWriter(File(minecraftFolder,"$itemName.json").bufferedWriter()).use {
                GSON.toJson(json, it)
            }
        } catch (ex: Exception) {
            ToxicityFurnitureImpl.warn("Unable to make a resource pack")
        }
    }

    override fun end(furniture: ToxicityFurnitureImpl) {
    }

    private fun readBlockBenchModel(file: File, models: File, textures: File) {
        val n = file.nameWithoutExtension
        file.reader().buffered().use { fileReader ->
            val element = JsonParser.parseReader(fileReader).asJsonObject
            JsonWriter(FileWriter(
                File(
                    models,
                    "$n.json"
                )
            ).buffered()).use { jsonWriter ->
                val textureObject = JsonObject()
                val textureSource = element.getAsJsonArray("textures")

                for ((index, jsonElement) in textureSource.withIndex()) {
                    val source =
                        jsonElement.asJsonObject.getAsJsonPrimitive("source").asString.split(
                            ','
                        )[1]
                    val bytes = Base64.getDecoder().decode(source)
                    ByteArrayInputStream(bytes).buffered().use { by ->
                        val imageName = "${n}_$index.png"
                        val imageResult = ImageIO.read(by)
                        ImageIO.write(
                            imageResult, "png",
                            File(
                                textures,
                                imageName
                            )
                        )
                        val imageRange = imageResult.height / imageResult.width
                        if (imageRange > 1) {
                            JsonWriter(File(textures,"$imageName.mcmeta").bufferedWriter()).use {
                                GSON.toJson(JsonObject().apply {
                                    add("animation", JsonObject().apply {
                                        add("frames", JsonArray().apply {
                                            for (i in 0..<imageRange) {
                                                add(i)
                                            }
                                        })
                                    })
                                },it)
                            }
                        }
                    }
                    textureObject.addProperty(
                        index.toString(),
                        "furniture:item/${n}_$index"
                    )
                }
                val resolution = element.getAsJsonObject("resolution")
                val t = max(
                    resolution.getAsJsonPrimitive("width").asInt,
                    resolution.getAsJsonPrimitive("height").asInt
                ) / 16

                GSON.toJson(JsonObject().apply {
                    add("textures", textureObject)
                    add("elements", JsonArray().apply {
                        for (item in element.getAsJsonArray("elements")) {
                            val get = item.asJsonObject
                            add(JsonObject().apply {
                                add("from", get.getAsJsonArray("from"))
                                add("to", get.getAsJsonArray("to"))
                                add(
                                    "rotation",
                                    get.getAsJsonArray("rotation")?.run {
                                        JsonObject().apply {
                                            val one = get(0).asFloat
                                            val two = get(1).asFloat
                                            val three = get(2).asFloat
                                            if (one != 0F) {
                                                addProperty("angle", one)
                                                addProperty("axis", "x")
                                            } else if (two != 0F) {
                                                addProperty("angle", two)
                                                addProperty("axis", "y")
                                            } else {
                                                addProperty("angle", three)
                                                addProperty("axis", "z")
                                            }
                                            add(
                                                "origin",
                                                get.getAsJsonArray("origin")
                                            )
                                        }
                                    })
                                add("faces", JsonObject().apply {
                                    val original = get.getAsJsonObject("faces")
                                    for (jsonFace in jsonFaces) {
                                        original.getAsJsonObject(jsonFace)?.let { arrayGet ->
                                            add(
                                                jsonFace,
                                                JsonObject().apply {
                                                    val textureGet = try {
                                                        arrayGet.getAsJsonPrimitive(
                                                            "texture"
                                                        ).asString
                                                    } catch (ex: Exception) {
                                                        "0"
                                                    }
                                                    add(
                                                        "uv",
                                                        arrayGet.getAsJsonArray(
                                                            "uv"
                                                        )
                                                            .let { originalArray ->
                                                                JsonArray().apply {

                                                                    fun getFloat(index: Int): Float {
                                                                        return originalArray[index].asFloat
                                                                    }

                                                                    add(getFloat(0) / t)
                                                                    add(getFloat(1) / t)
                                                                    add(getFloat(2) / t)
                                                                    add(getFloat(3) / t)
                                                                }
                                                            })
                                                    addProperty(
                                                        "texture",
                                                        "#$textureGet"
                                                    )
                                                }
                                            )
                                        }
                                    }
                                })
                            })
                        }
                    })
                    add("display", element.getAsJsonObject("display"))
                }, jsonWriter)
            }
        }
    }
}