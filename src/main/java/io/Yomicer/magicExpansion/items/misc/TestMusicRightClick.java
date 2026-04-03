package io.Yomicer.magicExpansion.items.misc;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.misc.moreLure.ZKBTMPlayer;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class TestMusicRightClick extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public TestMusicRightClick(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_MS = 1000*15;


    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {

        return e -> {
            // 阻止默认行为（放置方块或使用物品）
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);
            // 获取玩家
            Player player = e.getPlayer();
            UUID playerId = player.getUniqueId();
            long now = System.currentTimeMillis();
            // 每次使用时清理过期的冷却记录
            cooldowns.entrySet().removeIf(entry -> now - entry.getValue() >= COOLDOWN_MS);
            // 检查冷却
            if (cooldowns.containsKey(playerId)) {
                long lastUsed = cooldowns.get(playerId);
                if (now - lastUsed < COOLDOWN_MS) {
                    long remaining = (COOLDOWN_MS - (now - lastUsed)) / 1000 + 1;
                    player.sendMessage("§c该道具冷却中，请等待 " + remaining + " 秒后再使用。");
                    return;
                }
            }
            //player.sendTitle("✨🌹", "聆听星空的低语...", 10, 60, 10);
            // 播放旋律（调用我们封装的函数）
            new ZKBTMPlayer(MagicExpansion.getInstance()).playCuteMusic(player);
            // ✅ 使用成功，更新冷却时间
            cooldowns.put(playerId, now);
        };
    }



}
