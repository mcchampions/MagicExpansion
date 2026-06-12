package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.util.*;

import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class RightClickMan extends SlimefunItem implements EnergyNetComponent {


    public RightClickMan(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        constructMenu("交互机器人");
        addItemHandler(onBreak());
    }

    private final int[] upBorder = {0,18};
    private final int[] downBorder = {1,19};
    private final int[] eastBorder = {2,20};
    private final int[] southBorder = {3,21};
    private final int[] westBorder = {4,22};
    private final int[] northBorder = {5,23};


    private final int[] inputSlots = {};

    // Static direction mappings — avoids per-tick HashMap allocation
    private static final int[] DIR_LOGIC_SLOTS  = {9, 10, 11, 12, 13, 14};
    private static final int[] DIR_DISPLAY_SLOTS = {0, 1, 2, 3, 4, 5};
    private static final BlockFace[] DIR_FACES = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH
    };
    private static final int[] DIR_OFF_X = {0, 0, 1, 0, -1, 0};
    private static final int[] DIR_OFF_Y = {1, -1, 0, 0, 0, 0};
    private static final int[] DIR_OFF_Z = {0, 0, 0, 1, 0, -1};
    private static final String[] DIR_NAMES = {"上", "下", "东", "南", "西", "北"};

    // Player cache — avoids sorting all nearby players every tick
    private final Map<Location, Player> playerCache = new HashMap<>();
    private final Map<Location, Integer> playerCacheTick = new HashMap<>();
    private int tickCounter;
    private static final int PLAYER_CACHE_TTL = 20;

    // Display cache — avoids rebuilding ItemStack when target block + button state unchanged.
    // Key: block location -> int[6] where each int = material.ordinal() * 2 + (enabled ? 1 : 0)
    private final Map<Location, int[]> lastDisplayKey = new HashMap<>();

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                RightClickMan.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }
        });
    }

    protected void tick(Block block) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu == null) return;

        tickCounter++;

        // Phase 1: build display items & collect which directions are enabled
        boolean[] enabled = new boolean[6];
        int dirCount = 0;

        Location blockLoc = block.getLocation();
        int[] cachedKeys = lastDisplayKey.get(blockLoc);
        int[] newKeys = new int[6];

        for (int i = 0; i < 6; i++) {
            int logicSlot = DIR_LOGIC_SLOTS[i];
            int displaySlot = DIR_DISPLAY_SLOTS[i];

            Block targetBlock = block.getRelative(DIR_FACES[i]);
            boolean isEnabled = isButtonOn(menu, logicSlot);
            enabled[i] = isEnabled;
            if (isEnabled) dirCount++;

            // Compute cache key: material ordinal encodes block type, +1 if enabled
            // If key matches last tick, display item is already correct — skip rebuild
            int key = targetBlock.getType().ordinal() * 2 + (isEnabled ? 1 : 0);
            newKeys[i] = key;

            if (cachedKeys != null && cachedKeys[i] == key) {
                continue; // unchanged, skip expensive ItemStack rebuild
            }

            String statusText = isEnabled ? "§a已启用" : "§7未启用";
            ItemStack displayItem = buildDisplayItem(targetBlock, statusText);
            menu.replaceExistingItem(displaySlot, displayItem);
        }

        lastDisplayKey.put(blockLoc, newKeys);

        // Phase 2: fire PlayerInteractEvent for each enabled direction
        if (dirCount > 0) {
            Player nearest = getNearestPlayer(block);
            if (nearest != null) {
                for (int i = 0; i < 6; i++) {
                    if (!enabled[i]) continue;

                    Location loc = blockLoc.clone().add(DIR_OFF_X[i], DIR_OFF_Y[i], DIR_OFF_Z[i]);
                    PlayerInteractEvent event = new PlayerInteractEvent(
                        nearest, Action.RIGHT_CLICK_BLOCK,
                        new ItemStack(Material.AIR), loc.getBlock(), BlockFace.SELF
                    );
                    Bukkit.getPluginManager().callEvent(event);
                }
            }
        }

        // Phase 3: always update status display when someone is viewing
        if (menu.hasViewer()) {
            Player nearest = getNearestPlayer(block);
            if (nearest != null) {
                StringBuilder dirs = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    if (enabled[i]) dirs.append(DIR_NAMES[i]).append(' ');
                }
                menu.replaceExistingItem(16, new CustomItemStack(Material.PINK_CANDLE, "§b交互机器人",
                    "§b工作类型：§e右键交互方块",
                    "§b交互速度：§e1次/粘液刻",
                    "§b模拟玩家：§e" + nearest.getName(),
                    "§b模拟方向：§e" + dirs.toString().trim(),
                    "§b耗电速度：§e这个机器人不花电的",
                    "§b电量存储：§e这个机器人不储存电"));
            } else {
                menu.replaceExistingItem(16, new CustomItemStack(Material.PINK_CANDLE, "§b交互机器人",
                    "§b工作类型：§e右键交互方块",
                    "§b交互速度：§e1次/粘液刻",
                    "§c未检测到玩家在附近",
                    "§b耗电速度：§e这个机器人不花电的",
                    "§b电量存储：§e这个机器人不储存电"));
            }
        }
    }

    /**
     * Build the display ItemStack for a target block.
     * Slimefun items are cloned; plain blocks use a new ItemStack.
     * BlockState is only fetched when the item actually supports BlockStateMeta.
     */
    private ItemStack buildDisplayItem(Block targetBlock, String statusText) {
        Location targetLoc = targetBlock.getLocation();

        // Slimefun item: clone to avoid mutating the original template
        SlimefunItem sfItem = StorageCacheUtils.getSfItem(targetLoc);
        if (sfItem != null) {
            ItemStack item = sfItem.getItem().clone();
            applyDisplayInfo(item, targetBlock, statusText);
            return item;
        }

        // Air
        if (targetBlock.getType() == Material.AIR) {
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7空气");
                meta.setLore(List.of(
                    "§7坐标: " + targetBlock.getX() + "," + targetBlock.getY() + "," + targetBlock.getZ(),
                    "§7状态: " + statusText,
                    "§8（此处为空气）"
                ));
                item.setItemMeta(meta);
            }
            return item;
        }

        // Plain block
        Material blockType = targetBlock.getType();
        if (!isValidItemMaterial(blockType)) {
            return createBlockDisplayItem(blockType, targetBlock, statusText);
        }

        ItemStack item = new ItemStack(blockType);
        applyDisplayInfo(item, targetBlock, statusText);

        // Only call getState() when the item actually carries BlockStateMeta.
        // This avoids the expensive Block.getState() for simple blocks (stone, dirt, etc.)
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof BlockStateMeta bsm) {
            bsm.setBlockState(targetBlock.getState());
            item.setItemMeta(bsm);
        }

        return item;
    }

    /**
     * Cached nearest-player lookup — re-scans only every PLAYER_CACHE_TTL ticks per block.
     */
    private Player getNearestPlayer(Block block) {
        Location loc = block.getLocation();
        Integer cachedTick = playerCacheTick.get(loc);

        if (cachedTick != null && (tickCounter - cachedTick) < PLAYER_CACHE_TTL) {
            Player cached = playerCache.get(loc);
            if (cached != null && cached.isOnline()) {
                return cached;
            }
        }

        World world = loc.getWorld();
        List<Player> nearby = world.getPlayers().stream()
            .filter(p -> p.getLocation().distanceSquared(loc) <= 2500)
            .sorted(Comparator.comparingDouble(a -> a.getLocation().distance(loc)))
            .toList();

        Player nearest = nearby.isEmpty() ? null : nearby.get(0);
        playerCache.put(loc, nearest);
        playerCacheTick.put(loc, tickCounter);
        return nearest;
    }

    /**
     * 检查材料是否可以作为有效的物品
     */
    private boolean isValidItemMaterial(Material material) {
        return material.isItem() && !material.name().contains("WALL_") && !material.name().contains("DOUBLE_");
    }

    /**
     * 为不能作为物品的方块创建显示物品
     */
    private ItemStack createBlockDisplayItem(Material blockType, Block block, String statusText) {
        ItemStack displayItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = displayItem.getItemMeta();

        if (meta != null) {
            String blockName = blockType.name().toLowerCase().replace("_", " ");
            meta.setDisplayName("§7" + blockName);

            List<String> lore = new ArrayList<>();
            lore.add("§7坐标: " + block.getX() + "," + block.getY() + "," + block.getZ());
            lore.add("§7状态: " + statusText);
            lore.add("§8（此方块无法作为物品显示）");

            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }

        return displayItem;
    }

    private void constructMenu(String displayName) {
        new BlockMenuPreset(getId(), displayName) {

            @Override
            public void init() {
                constructMenu(this);
            }

            @Override
            public boolean canOpen(@Nonnull Block b, @Nonnull Player p) {
                return p.hasPermission("slimefun.inventory.bypass")
                        || Slimefun.getProtectionManager().hasPermission(p, b.getLocation(),
                        Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow itemTransportFlow) {
                return new int[]{

                };
            }
        };
    }



    protected void constructMenu(BlockMenuPreset preset) {
        // 按钮状态
        final Material BUTTON_OFF = Material.BARRIER;
        final Material BUTTON_ON = Material.LANTERN;

        int[] controlSlots = {9, 10, 11, 12, 13, 14};
        int[] directionSlots = {6, 7, 8, 24, 25, 26};

        for (int slot : controlSlots) {
            preset.addItem(slot, new CustomItemStack(BUTTON_OFF, "§7未激活", "§e点击激活"),
                    (player, slot1, item, action) -> {
                        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof BlockMenu menu)) {
                            return false;
                        }
                        ItemStack currentItem = menu.getItemInSlot(slot1);

                        if (currentItem != null && currentItem.getType() == BUTTON_ON) {
                            // ON → OFF
                            menu.replaceExistingItem(slot1, new CustomItemStack(BUTTON_OFF, "§7未激活", "§e点击激活"));
                        } else {
                            // OFF 或空 → ON
                            menu.replaceExistingItem(slot1, new CustomItemStack(BUTTON_ON, "§a已激活", "§e点击关闭"));
                        }

                        return false; // 阻止拿走
                    });
        }



        for (int i : directionSlots) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.BLUE_CANDLE)), "§9装饰蜡烛", "§7仅用于美观","§7中心为机器状态"),
                    (p, slot, item, action) -> false);
        }

        preset.addItem(15, new CustomItemStack(Material.BLUE_CANDLE, "§9装饰蜡烛", "§7仅用于美观","§7中心为机器状态"),
                (player, slot, item, action) -> false); // 点击无反应

        preset.addItem(17, new CustomItemStack(Material.BLUE_CANDLE, "§9装饰蜡烛", "§7仅用于美观","§7中心为机器状态"),
                (player, slot, item, action) -> false); // 点击无反应

        // ====== 边框说明文字（不可点击）======
        for (int i : upBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.WHITE_CONCRETE)), "§e向上", "§b此处点击，机器人与上方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : downBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.BLACK_CONCRETE)), "§e向下", "§b此处点击，机器人与下方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : eastBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.RED_CONCRETE)), "§e向东", "§b此处点击，机器人与东方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : southBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.BLUE_CONCRETE)), "§e向南", "§b此处点击，机器人与南方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : westBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.GREEN_CONCRETE)), "§e向西", "§b此处点击，机器人与西方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : northBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.YELLOW_CONCRETE)), "§e向北", "§b此处点击，机器人与北方方块交互"),
                    (p, slot, item, action) -> false);
        }

        // 状态显示槽（中间）
        preset.addItem(16, new CustomItemStack(Material.RED_CANDLE, " "),
                (p, slot, item, action) -> false);
    }

    private boolean isButtonOn(BlockMenu menu, int slot) {
        ItemStack item = menu.getItemInSlot(slot);
        return item != null && item.getType() == Material.LANTERN;
    }

    /**
     * Applies display name + coordinate/status lore to the item.
     * Preserves existing lore and appends our info below a separator.
     *
     * Performance notes:
     * - getItemMeta() is called exactly once (NBT deserialize).
     * - ItemStackHelper.getDisplayName() is only invoked for plain blocks
     *   that lack a custom name; Slimefun items reuse their existing
     *   display name via meta.getDisplayName(), avoiding a second
     *   NBT deserialize inside the helper.
     */
    private void applyDisplayInfo(ItemStack item, Block block, String statusLine) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Slimefun items already have a custom display name — reuse it directly.
        // Plain blocks (new ItemStack) have no display name — fetch the translated name.
        if (!meta.hasDisplayName()) {
            meta.setDisplayName(ItemStackHelper.getDisplayName(item));
        }

        // Build lore: preserve original, then append coordinates + status
        List<String> lore;
        if (meta.hasLore()) {
            lore = new ArrayList<>(meta.getLore());
        } else {
            lore = new ArrayList<>(4); // pre-size for: separator, coords, status, (optional)
        }
        lore.add("§8---");
        lore.add("§7坐标: " + block.getX() + "," + block.getY() + "," + block.getZ());
        if (statusLine != null) {
            lore.add("§7状态: " + statusLine);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }



    private BlockBreakHandler onBreak() {
        return new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent e, @Nonnull ItemStack i, @Nonnull List<ItemStack> list) {
                Block b = e.getBlock();
                Location loc = b.getLocation();
                BlockMenu inv = StorageCacheUtils.getMenu(loc);

                if (inv != null) {
                    inv.dropItems(loc, inputSlots);
                }

                // Clean up caches so stale entries don't leak
                playerCache.remove(loc);
                playerCacheTick.remove(loc);
                lastDisplayKey.remove(loc);
            }
        };
    }

    @Override
    public @NotNull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.NONE;
    }

    @Override
    public int getCapacity() {
        return 0;
    }
}
