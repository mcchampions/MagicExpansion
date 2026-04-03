package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.MagicExpansion;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

public class PluginInitializer {

    private final MagicExpansion plugin;
    @Getter
    private MagicAltarManager altarManager;
    @Getter
    private RecipeBookManager recipeBookManager;
    @Getter
    private NamespacedKey altarWandKey;
    @Getter
    private NamespacedKey recipeBookKey;

    public PluginInitializer(MagicExpansion plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        // 初始化Keys
        altarWandKey = new NamespacedKey(plugin, "altar_wand");
        recipeBookKey = new NamespacedKey(plugin, "recipe_book");

        // 初始化管理器
        altarManager = new MagicAltarManager(plugin);
        recipeBookManager = new RecipeBookManager(plugin);

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new MagicAltarListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new RecipeBookListener(plugin), plugin);

        // 注册命令
        plugin.getCommand("mxwand").setExecutor(new MagicAltarCommand(plugin));
        plugin.getCommand("mxalter").setExecutor(new MxEnchantCommand(plugin));

        // 加载配方
        altarManager.loadRecipes();
    }

}