/*
 * Copyright (c) 2021 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baehyeonwoo.advctravel

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.*
import org.bukkit.event.player.PlayerLoginEvent.Result
import org.bukkit.plugin.Plugin


/***
 * @author BaeHyeonWoo
 */

class AdvcTravelEvent : Listener {
    private fun getInstance(): Plugin {
        return AdvcTravelMain.instance
    }

    private val server = getInstance().server

    private val config = getInstance().config

    private val administrator = config.getString("administrator").toString()

    private val runner = config.getString("runner").toString()

    private val beds = arrayOf(
        Material.WHITE_BED,
        Material.ORANGE_BED,
        Material.MAGENTA_BED,
        Material.LIGHT_BLUE_BED,
        Material.YELLOW_BED,
        Material.LIME_BED,
        Material.PINK_BED,
        Material.GRAY_BED,
        Material.LIGHT_GRAY_BED,
        Material.PURPLE_BED,
        Material.GREEN_BED,
        Material.BROWN_BED,
        Material.RED_BED,
        Material.CYAN_BED,
        Material.BLUE_BED,
        Material.BLACK_BED
    )

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val p = e.player
        p.noDamageTicks = 0
    }

    @EventHandler
    fun onPlayerAdvancementDone(e: PlayerAdvancementDoneEvent) {
        val p = e.player
        val advancement = e.advancement

        if (runner.contains(p.uniqueId.toString())) {
            if (!advancement.key.toString().startsWith("minecraft:recipes") && !advancement.key.toString().endsWith("root")) {
                server.maxPlayers = server.maxPlayers + 1
                config.set("max-players", server.maxPlayers)
                getInstance().saveConfig()
            }
        }
    }

    @EventHandler
    fun onAsyncChat(e: AsyncChatEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onPlayerCommandPreprocess(e: PlayerCommandPreprocessEvent) {
        val p = e.player
        if (!administrator.contains(p.uniqueId.toString())) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val p = e.player
        val b = e.block

        if (beds.contains(b.type)) {
            if (p.world.environment == World.Environment.NETHER || p.world.environment == World.Environment.THE_END) {
                e.isCancelled = true
                p.sendMessage(text("지옥과 엔더에서는 침대가 막혀있습니다!", NamedTextColor.RED))
            }
        }
        if (b.type == Material.RESPAWN_ANCHOR) {
            if (p.world.environment == World.Environment.NORMAL || p.world.environment == World.Environment.THE_END) {
                e.isCancelled = true
                p.sendMessage(text("오버월드와 엔더에서는 리스폰 정박기가 막혀있습니다!", NamedTextColor.RED))
            }
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(e: EntityDamageByEntityEvent) {
        val damager = e.damager
        val entity = e.entity

        if (damager is Player && entity is Player) {
            if (!runner.contains(damager.uniqueId.toString()) && !runner.contains(entity.uniqueId.toString())) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerLogin(e: PlayerLoginEvent) {
        val p = e.player

        if (administrator.contains(p.uniqueId.toString())) {
            if (e.result == Result.KICK_FULL && !p.isBanned) {
                e.allow()
            }
        }
    }

    @EventHandler
    fun onPlayerInteractEvent(e: PlayerInteractEvent) {
        val p = e.player
        val block = e.clickedBlock?.type

        if (!runner.contains(p.uniqueId.toString())) {
            if (e.action == Action.LEFT_CLICK_BLOCK || e.action == Action.RIGHT_CLICK_BLOCK) {
                if (block != null && !block.isAir && block == Material.END_PORTAL_FRAME || block == Material.DRAGON_EGG) {
                    e.isCancelled = true
                    p.sendMessage(text("밸런스를 위해 이 블록에 상호작용이 불가능합니다.", NamedTextColor.RED))
                }
            }
        }
    }

    @EventHandler
    fun onPlayerAttemptItemPickup(e: PlayerAttemptPickupItemEvent) {
        val p = e.player
        val item = e.item.itemStack

        if (item.type == Material.DRAGON_EGG) {
            if (!runner.contains(p.uniqueId.toString())) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPaperServerListPing(e: PaperServerListPingEvent) {
        e.motd(text("ADVANCEMENT TRAVELER", NamedTextColor.RED, TextDecoration.BOLD))
    }
}