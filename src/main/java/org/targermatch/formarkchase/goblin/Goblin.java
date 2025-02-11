package org.targermatch.formarkchase.goblin;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Goblin extends JavaPlugin implements Listener, CommandExecutor {
    private final Set<UUID> rolePlayers = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("giverole")).setExecutor(this);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) { //사망시 역할배정 + 드롭추가
        Player player = event.getEntity();
        if (rolePlayers.contains(player.getUniqueId())) {
            event.getDrops().clear();
            rolePlayers.remove(player.getUniqueId());
            goblin(player);
            addRandomItemsToDrops(player, event);
            if (new Random().nextBoolean()){
                Location playerLocation = player.getLocation(); // 사망한 플레이어의 위치 가져오기
                Location location = playerLocation.clone(); // 위치를 복사
                location.setY(location.getY() + 1);
                Entity creeper = location.getWorld().spawnEntity(location, EntityType.CREEPER);
                Creeper c = (Creeper) creeper;
                c.setPowered(true);
                player.sendMessage(ChatColor.GREEN + "깜짝 선물!");
                if (new Random().nextBoolean()) {
                    location.getWorld().strikeLightning(location);
                    player.sendMessage(ChatColor.AQUA + "깜짝 선물 2!");
                }
            }
        }
    }

    private void goblin(Player deadPlayer) { //새 고블린 배정
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(deadPlayer);

        if (onlinePlayers.isEmpty()) return;

        List<Player> survivalPlayers = new ArrayList<>();
        for (Player player : onlinePlayers) {
            if (player.getGameMode() == GameMode.SURVIVAL) {survivalPlayers.add(player);}
        }
        if (survivalPlayers.isEmpty()) return; // 앞에꺼랑 순서?

        Player newRolePlayer = survivalPlayers.get(new Random().nextInt(survivalPlayers.size()));
        rolePlayers.add(newRolePlayer.getUniqueId());
        newRolePlayer.sendMessage(ChatColor.LIGHT_PURPLE + "와! 고블린에 당첨되셨어요!");
        newRolePlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        Bukkit.broadcastMessage(ChatColor.YELLOW + newRolePlayer.getName() + "이(가) 황금고블린에 걸렸습니다!");
    }


    private void addRandomItemsToDrops(Player player, PlayerDeathEvent event) {
        Random random = new Random();

        event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE, random.nextInt(16) + 1));
        event.getDrops().add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, random.nextInt(8) + 1));
        if (random.nextBoolean()) {
            event.getDrops().add(new ItemStack(Material.ELYTRA));
        }
        event.getDrops().add(new ItemStack(Material.ENDER_PEARL, random.nextInt(16) + 1));
        event.getDrops().add(new ItemStack(Material.FIREWORK_ROCKET, random.nextInt(32) + 1));
        if (random.nextBoolean()) {
            event.getDrops().add(new ItemStack(Material.TOTEM_OF_UNDYING, random.nextInt(5) + 1));
        }

        event.getDrops().add(new ItemStack(Material.BEACON, random.nextInt(3) + 1));
        event.getDrops().add(new ItemStack(Material.OBSIDIAN, random.nextInt(16) + 1));

        addRandomArmorWithEnchantments(random, event);
        addRandomWeaponsWithEnchantments(random, event);
    }

    private void addRandomArmorWithEnchantments(Random random, PlayerDeathEvent event) {
        Material[] armorTypes = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS};
        //for 문에다가 반환되는 random 값에 곱하기 10해서 반올림한거  집어넣으면 여러개 뿌리기도 가능할듯
        for (Material armor : armorTypes) {
            ItemStack armorItem = new ItemStack(armor);
            armorItem.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextInt(4) + 2);
            armorItem.addEnchantment(Enchantment.DURABILITY, random.nextInt(2) + 1);
            event.getDrops().add(armorItem);
        }
    }

    private void addRandomWeaponsWithEnchantments(Random random, PlayerDeathEvent event) {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, random.nextInt(5) + 1);
        sword.addEnchantment(Enchantment.FIRE_ASPECT, random.nextInt(2) + 1);
        sword.addEnchantment(Enchantment.FIRE_ASPECT, random.nextInt(2) + 1);
        event.getDrops().add(sword);
    }





    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("giverole")) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "사용법: /giverole <플레이어>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
                return true;
            }
            rolePlayers.add(target.getUniqueId());
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
            sender.sendMessage(ChatColor.GREEN + target.getName() + "님을 황금고블린으로 등록하였습니다.");
            target.sendMessage(ChatColor.GOLD + "와! 황금고블린에 당첨되셨어요!");
            Bukkit.broadcastMessage(ChatColor.GOLD + target.getName() + "님이 황금고블린에 당첨되셨습니다!");
            return true;
        }
        return false;
    }
}