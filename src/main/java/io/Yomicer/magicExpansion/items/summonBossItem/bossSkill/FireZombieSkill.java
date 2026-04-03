package io.Yomicer.magicExpansion.items.summonBossItem.bossSkill;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FireZombieSkill {



    // 生成粒子特效
    private static void spawnParticleEffects(Location location,Player player) {
        // 火焰粒子
        location.getWorld().spawnParticle(Particle.FLAME, location, 1000, 10, 8, 10, 0.1);
//        player.sendMessage("FLAME触发");

        // 烟雾粒子
        location.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 1000, 10, 8, 10, 0.1);
//        player.sendMessage("CAMPFIRE_COSY_SMOKE触发");
        // 魔法粒子
        // 定义粒子的颜色（青色）
//        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.0f);
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, 1000, 10, 8, 10, 0.1);
//        player.sendMessage("ENCHANT触发");
    }

    // 生成粒子特效
    private static void spawnOneParticle(Location location,Player player, Particle particle, int num) {
        // 火焰粒子
        location.getWorld().spawnParticle(particle, location, num, 10, 8, 10, 0.1);
//        player.sendMessage("FLAME触发");
    }
    private static void spawnOneParticle(Location location,Player player, Particle particle, int num, double x, double y, double z) {
        // 火焰粒子
        location.getWorld().spawnParticle(particle, location, num, x, y, z, 0.1);
//        player.sendMessage("FLAME触发");
    }

    private static void spawnOneParticle(Location location, Player player, Particle particle, Particle.DustOptions extraInfo){
        location.getWorld().spawnParticle(particle, location, 1000, 10, 8, 10, 0.1,extraInfo);
    }


    /**
     * 获取附近的玩家
     */
    private static List<Player> getNearbyPlayers(LivingEntity mob, double x,double y) {
        return mob.getWorld().getNearbyEntities(mob.getLocation(), x, y, x).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
    }

    /**
     * 技能：魔法攻击
     */
    public static void magicAttackSkill(LivingEntity mob, String bossName) {
        for (Player nearbyPlayer : getNearbyPlayers(mob,10,5)) {
            nearbyPlayer.damage(10, mob); // 造成5点伤害
            nearbyPlayer.sendMessage(bossName+"对你发动了魔法攻击！");
            spawnOneParticle(nearbyPlayer.getLocation(), nearbyPlayer, Particle.WATER_BUBBLE,2500);
        }
    }

    /**
     * 技能：红石粒子攻击 僵尸大招，精神攻击
     */
    public static void redstoneParticleAttackSkill(LivingEntity mob) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.0f);
        for (Player nearbyPlayer : getNearbyPlayers(mob,10,5)) {
            nearbyPlayer.damage(8, mob); // 造成5点伤害
            nearbyPlayer.sendMessage("§b§l烈焰僵尸对你发动了精神攻击！");
            spawnOneParticle(nearbyPlayer.getLocation(), nearbyPlayer, Particle.REDSTONE, dustOptions);
            // 添加负面效果（持续2秒，等级10）
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 10)); // 攻击缓慢
            nearbyPlayer.sendMessage(ChatColor.RED + "§l你的手臂变得沉重无比，" + ChatColor.YELLOW + "§l攻击速度大幅下降！");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 10));         // 移动缓慢
            nearbyPlayer.sendMessage(ChatColor.DARK_BLUE + "§l你的双腿像灌了铅一样，" + ChatColor.GOLD + "§l移动变得异常迟缓！");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 10));    // 致盲
            nearbyPlayer.sendMessage(ChatColor.BLACK + "§l黑暗笼罩了你的视野，" + ChatColor.LIGHT_PURPLE + "§l你几乎什么都看不见！");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 180, 10));    // 反胃
            nearbyPlayer.sendMessage(ChatColor.GREEN + "§l一阵强烈的眩晕感袭来，" + ChatColor.DARK_GREEN + "§l你的世界开始天旋地转！");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 10));       // 饥饿
            nearbyPlayer.sendMessage(ChatColor.DARK_RED + "§l你的肚子发出痛苦的呻吟，" + ChatColor.GRAY + "§l饥饿感正在吞噬你的体力！");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 10));     // 失明（1.19+版本支持）
            nearbyPlayer.sendMessage(ChatColor.DARK_GRAY + "§l深邃的黑暗侵蚀了你的灵魂，" + ChatColor.WHITE + "§l你完全陷入了无尽的虚无！");
        }
    }
    /**
     * 技能：絮风
     */
    public static void redstoneParticleAttackSkillWindElf(LivingEntity mob) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 105, 180), 5.0f);
        for (Player nearbyPlayer : getNearbyPlayers(mob,15,8)) {
            nearbyPlayer.damage(21, mob); // 造成21点伤害
            nearbyPlayer.sendMessage("§3§l风灵对你发动了絮风攻击！");
            spawnOneParticle(nearbyPlayer.getLocation(), nearbyPlayer, Particle.REDSTONE, dustOptions);
            // 1. 失衡：跳跃提升（反向效果）→ 模拟“被狂风吹起，无法落地”
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 80, 5)); // 被气流托起，漂浮
            nearbyPlayer.sendMessage(ChatColor.AQUA + "§l↑↑↑ 狂暴的气流将你高高卷起！" + ChatColor.WHITE + " §l你无法控制地向上漂浮...");

            // 2. 减速：缓慢 + 缓慢挖掘（双重迟滞）
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 3));
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 3));
            nearbyPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "§l🌀 四周的风形成枷锁，" + ChatColor.GRAY + "§l你的动作变得迟缓而无力...");

            // 3. 视觉干扰：反胃（模拟眩晕 + 视野扭曲）
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 250, 3));
            nearbyPlayer.sendMessage(ChatColor.DARK_AQUA + "§l🌪️ 眼前的世界疯狂旋转，" + ChatColor.YELLOW + "§l你分不清哪边是天，哪边是地...");

            // 4. 呼吸困难：饥饿效果（象征体力流失）
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 80, 3));
            nearbyPlayer.sendMessage(ChatColor.DARK_RED + "§l💨 强风封锁了你的呼吸，" + ChatColor.GOLD + "§l体力正随着每一次喘息迅速流失...");

            // 5. 风暴之眼： blindness（短暂致盲，模拟沙尘迷眼）
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 100));
            nearbyPlayer.sendMessage(ChatColor.BLACK + "§l🌫️ 灰白色的风沙扑面而来，" + ChatColor.WHITE + "§l你的双眼几乎无法视物...");

            // 可选：1.19+ 支持 DARKNESS（更契合“风暴遮蔽光明”）
            if (Bukkit.getVersion().contains("1.19") || Bukkit.getVersion().contains("1.20") || Bukkit.getVersion().contains("1.21")) {
                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 100));
                nearbyPlayer.sendMessage(ChatColor.DARK_GRAY + "🌑 风灵之怒遮蔽了光明，" + ChatColor.GRAY + "§l连灵魂都被黑暗侵蚀...");
            }
        }
    }

    /**
     * 技能：火焰攻击
     */
    public static void fireParticleAttackSkill(LivingEntity mob) {

        // 获取附近的玩家
        List<Player> nearbyPlayers = getNearbyPlayers(mob,10,5);

        // 如果没有玩家在范围内，直接返回
        if (nearbyPlayers.isEmpty()) {
            return;
        }

        // 随机选择一个目标玩家
        Player targetPlayer = nearbyPlayers.get(new Random().nextInt(nearbyPlayers.size()));

        // 对目标玩家造成伤害
        targetPlayer.damage(88, mob); // 造成88点伤害

        // 发送提示信息给目标玩家
        targetPlayer.sendMessage("§c§l烈焰僵尸锁定了你！");
        targetPlayer.sendMessage("§e§l炙热的火焰吞噬了你的灵魂...");
        targetPlayer.sendMessage("§c§l你受到了" +"§e§l88点伤害§c§l" +"！");

        // 添加链接线效果
        spawnLinkEffect(mob.getLocation(), targetPlayer.getLocation(),Color.RED);
        // 在目标玩家位置生成火焰粒子效果
        spawnOneParticle(targetPlayer.getLocation(), targetPlayer, Particle.FLAME, 200, 1, 1, 1);

        // 广播消息给范围内的其他玩家（非目标玩家）
        for (Player nearbyPlayer : nearbyPlayers) {
            if (!nearbyPlayer.equals(targetPlayer)) {
                nearbyPlayer.sendMessage("§e§l烈焰僵尸刚刚发动了火焰攻击！");
                nearbyPlayer.sendMessage("§e§l它锁定了 §b"+ targetPlayer.getName() + "§e§l！");
            }
        }
    }
    /**
     * 技能：双子风攻击
     */
    public static void twoWindParticleAttackSkill(LivingEntity mob) {

        // 获取附近的玩家
        List<Player> nearbyPlayers = getNearbyPlayers(mob,15,8);

        // 如果没有玩家在范围内，直接返回
        if (nearbyPlayers.isEmpty()) {
            return;
        }

        // 随机选择一个目标玩家
        Player targetPlayer = nearbyPlayers.get(new Random().nextInt(nearbyPlayers.size()));
        Player targetPlayer2 = nearbyPlayers.get(new Random().nextInt(nearbyPlayers.size()));

        // 对目标玩家造成伤害
        targetPlayer.damage(68, mob); // 造成78点伤害
        targetPlayer2.damage(136, mob); // 造成78点伤害

        // 发送提示信息给目标玩家
        targetPlayer.sendMessage("§b§l乂风锁定了你！");
        targetPlayer.sendMessage("§e§l狂风席卷而来，试图撕裂你的防御...");
        targetPlayer.sendMessage("§b§l你遭受了" + "§e§l68点伤害§b§l" + "！");

        targetPlayer2.sendMessage("§b§l爻风锁定了你！");
        targetPlayer2.sendMessage("§e§l狂风席卷而来，透过你的护甲，试图撕裂你的肉身...");
        targetPlayer2.sendMessage("§b§l你遭受了" + "§e§l136点伤害§b§l" + "！");

        // 添加链接线效果
        spawnLinkEffect(mob.getLocation(), targetPlayer.getLocation(), Color.AQUA);
        spawnLinkEffect(mob.getLocation(), targetPlayer2.getLocation(), Color.AQUA);
        // 在目标玩家位置生成火焰粒子效果
        spawnOneParticle(targetPlayer.getLocation(), targetPlayer, Particle.WATER_SPLASH, 300, 1, 1, 1);
        spawnOneParticle(targetPlayer2.getLocation(), targetPlayer, Particle.WATER_SPLASH, 300, 1, 1, 1);

        // 广播消息给范围内的其他玩家（非目标玩家）
        for (Player nearbyPlayer : nearbyPlayers) {
            if (!nearbyPlayer.equals(targetPlayer)&&!nearbyPlayer.equals(targetPlayer2)) {
                nearbyPlayer.sendMessage("§e§l风灵发动了双子风攻击！");
                nearbyPlayer.sendMessage("§e§l乂风锁定了 §b"+ targetPlayer.getName() + "§e§l！");
                nearbyPlayer.sendMessage("§e§l爻风锁定了 §b"+ targetPlayer2.getName() + "§e§l！");
            }
        }
    }



    /**
     * 在两个位置之间生成链接线效果
     *
     * @param start 起始位置（例如烈火僵尸的位置）
     * @param end   结束位置（例如目标玩家的位置）
     */
    public static void spawnLinkEffect(Location start, Location end,Color color) {
        // 获取起始和结束位置的坐标
        double startX = start.getX();
        double startY = start.getY();
        double startZ = start.getZ();

        double endX = end.getX();
        double endY = end.getY();
        double endZ = end.getZ();

        // 计算两点之间的距离
        double distance = start.distance(end);

        // 设置粒子的数量和间隔
        int particleCount = (int) (distance * 5); // 每单位距离生成 5 个粒子
        double deltaX = (endX - startX) / particleCount;
        double deltaY = (endY - startY) / particleCount;
        double deltaZ = (endZ - startZ) / particleCount;

        // 在两点之间生成粒子
        for (int i = 0; i < particleCount; i++) {
            double x = startX + deltaX * i;
            double y = startY + deltaY * i;
            double z = startZ + deltaZ * i;

            // 创建粒子位置
            Location particleLocation = new Location(start.getWorld(), x, y, z);

            // 生成粒子效果（例如红线或能量线）
            start.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, new Particle.DustOptions(color, 1.0f));
        }
    }




}
