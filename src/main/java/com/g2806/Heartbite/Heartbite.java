package com.g2806.Heartbite;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.Random;

public class Heartbite extends JavaPlugin implements Listener {

    // Constante para la vida máxima predeterminada de Minecraft (10 corazones = 20 puntos)
    private static final double DEFAULT_MAX_HEALTH = 20.0;
    // Objeto Random para generar números aleatorios (usado en cofres)
    private final Random random = new Random();

    // Método que se ejecuta al habilitar el plugin
    @Override
    public void onEnable() {
        // Registra esta clase como oyente de eventos
        getServer().getPluginManager().registerEvents(this, this);
        // Mensaje en la consola para confirmar que el plugin se inició
        getLogger().info("Heartbite enabled!");
    }

    // Método que se ejecuta al deshabilitar el plugin
    @Override
    public void onDisable() {
        // Mensaje en la consola para confirmar que el plugin se detuvo
        getLogger().info("Heartbite disabled!");
    }

    // Maneja los comandos del plugin (/vitalapple, /givecursedapple, /resethearts)
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verifica si el emisor tiene permiso de OP
        if (!sender.hasPermission("heartbite.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true; // Termina la ejecución
        }

        // Obtiene el nombre del comando en minúsculas
        String commandName = command.getName().toLowerCase();

        // Comando para restablecer la vida máxima a 10 corazones
        if (commandName.equals("resethearts")) {
            // Verifica que se haya proporcionado un argumento (el nombre del jugador)
            if (args.length != 1) {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /resethearts <player>");
                return true;
            }

            // Busca al jugador objetivo
            Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            // Restablece la vida máxima al valor predeterminado
            target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(DEFAULT_MAX_HEALTH);
            // Ajusta la salud actual al nuevo máximo
            target.setHealth(DEFAULT_MAX_HEALTH);
            // Notifica al emisor y al jugador afectado
            sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s max health to 10 hearts.");
            target.sendMessage(ChatColor.GREEN + "Your max health has been reset to 10 hearts.");
            return true;
        }

        // Verifica que los comandos de manzanas tengan 2 argumentos (jugador y corazones)
        if (args.length != 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + commandName + " <player> <hearts>");
            return true;
        }

        // Busca al jugador objetivo
        Player target = getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        // Convierte el segundo argumento a un número entero (corazones)
        int hearts;
        try {
            hearts = Integer.parseInt(args[1]);
            if (hearts <= 0) throw new NumberFormatException(); // No permite valores <= 0
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Hearts must be a positive integer.");
            return true;
        }

        // Da una Vital Apple si el comando es /vitalapple
        if (commandName.equals("givevitalapple")) {
            ItemStack vitalApple = createVitalApple(hearts);
            target.getInventory().addItem(vitalApple);
            sender.sendMessage(ChatColor.GREEN + "Gave a Vital Apple (+" + hearts + " hearts) to " + target.getName());
        }
        // Da una Cursed Apple si el comando es /givecursedapple
        else if (commandName.equals("givecursedapple")) {
            ItemStack cursedApple = createCursedApple(hearts);
            target.getInventory().addItem(cursedApple);
            sender.sendMessage(ChatColor.GREEN + "Gave a Cursed Apple (-" + hearts + " hearts) to " + target.getName());
        } else {
            return false; // Comando no reconocido
        }

        return true; // Comando ejecutado con éxito
    }

    // Crea una Vital Apple personalizada con el número de corazones especificado
    private ItemStack createVitalApple(int hearts) {
        ItemStack apple = new ItemStack(Material.GOLDEN_APPLE, 1); // Base: manzana dorada
        ItemMeta meta = apple.getItemMeta();
        if (meta != null) { // Verifica que los metadatos sean accesibles
            meta.setDisplayName(ChatColor.GOLD + "Vital Apple"); // Nombre en dorado
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Increases max health by " + hearts + " hearts.")); // Descripción
            meta.setCustomModelData(1); // Identificador único para diferenciarla
            apple.setItemMeta(meta); // Aplica los cambios
        }
        return apple;
    }

    // Crea una Cursed Apple personalizada con el número de corazones especificado
    private ItemStack createCursedApple(int hearts) {
        ItemStack apple = new ItemStack(Material.GOLDEN_APPLE, 1); // Base: manzana dorada
        ItemMeta meta = apple.getItemMeta();
        if (meta != null) { // Verifica que los metadatos sean accesibles
            meta.setDisplayName(ChatColor.DARK_RED + "Cursed Death Apple"); // Nombre en rojo oscuro
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Reduces max health by " + hearts + " hearts.")); // Descripción
            meta.setCustomModelData(2); // Identificador único diferente
            apple.setItemMeta(meta); // Aplica los cambios
        }
        return apple;
    }

    // Evento que se dispara cuando un jugador consume un ítem
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        // Solo procesa si es una manzana dorada con metadatos
        if (item.getType() != Material.GOLDEN_APPLE || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        // Verifica que los metadatos existan y tengan nombre y lore
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore()) return;

        Player player = event.getPlayer();
        // Obtiene la vida máxima actual del jugador
        double currentMaxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getBaseValue();

        // Extrae el número de corazones del lore (posición 4 después de dividir por espacios)
        String loreLine = meta.getLore().get(0);
        int hearts = Integer.parseInt(loreLine.split(" ")[4]);

        // Cancela los efectos predeterminados de la manzana dorada (regeneración y absorción)
        event.setCancelled(true);
        // Reduce manualmente la cantidad del ítem en el inventario
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }

        // Si es una Vital Apple, aumenta la vida máxima
        if (meta.getDisplayName().equals(ChatColor.GOLD + "Vital Apple")) {
            double newMaxHealth = currentMaxHealth + (hearts * 2); // 1 corazón = 2 puntos
            player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
            player.setHealth(newMaxHealth); // Restaura la salud al nuevo máximo
            // Aplica suerte
            player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 1024, 0));
            // Aplica glowing por 10 segundos
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 512, 0));
            // Aplica Health Boost
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1024, 0));
            // proteccion al fuego
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1024, 0));
            player.sendMessage(ChatColor.GREEN + "Consumed a Vital Apple! Max health increased to " + (newMaxHealth / 2) + " hearts.");
            // Aplica invulnerabilidad
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 256, 255));
        }
        // Si es una Cursed Apple, reduce la vida máxima y aplica efectos negativos
        else if (meta.getDisplayName().equals(ChatColor.DARK_RED + "Cursed Death Apple")) {
            double newMaxHealth = Math.max(2, currentMaxHealth - (hearts * 2)); // No baja de 1 corazón
            player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
            player.setHealth(Math.min(player.getHealth(), newMaxHealth)); // Ajusta la salud actual
            // Aplica veneno
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 128, 0));
            // Aplica ceguera
            player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 1024, 0));
            // Aplica nausea
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 128, 0));
            // Aplica mala suerte
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1024, 0));
            // Aplica invulnerabilidad al daño por rayo
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 128, 255));
            player.getWorld().strikeLightning(player.getLocation());
            player.sendMessage(ChatColor.RED + "Consumed a Cursed Apple! Max health reduced to " + (newMaxHealth / 2) + " hearts.");
        }
    }

    // Evento que se dispara cuando se genera loot en cofres
    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        InventoryHolder holder = event.getInventoryHolder();
        // Solo añade una Vital Apple si hay un inventario y con 10% de probabilidad
        if (holder != null && random.nextDouble() < 0.10) {
            int hearts = random.nextInt(3) + 1; // Elige entre 1, 2 o 3 corazones
            ItemStack vitalApple = createVitalApple(hearts);
            holder.getInventory().addItem(vitalApple); // Añade la manzana al cofre
        }
    }
}