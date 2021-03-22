package com.bungoh.escape.game;

import com.bungoh.escape.files.ConfigFile;
import com.bungoh.escape.files.DataFile;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Manager {

    private static ArrayList<Arena> arenas;
    private static Material generatorMaterial;

    public Manager() {
        arenas = new ArrayList<>();
        generatorMaterial = Material.matchMaterial(ConfigFile.getGeneratorBlock());
        for (String name : DataFile.getArenaNames()) {
            arenas.add(new Arena(name));
        }
    }

    public static void addArena(String name) {
        arenas.add(new Arena(name));
    }

    public static void removeArena(Arena arena) {
        arenas.remove(arena);
    }

    public static boolean isPlaying(Player player) {
        for (Arena arena: arenas) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public static Arena getArena(String name) {
        for (Arena arena : arenas) {
            if (arena.getName().equals(name)) {
                return arena;
            }
        }

        return null;
    }

    public static Arena getArena(Player player) {
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return arena;
            }
        }

        return null;
    }

    public static boolean isRecruiting(String name) {
        return getArena(name).getState() == GameState.RECRUITING;
    }

    public static ArrayList<Arena> getArenas() {
        return arenas;
    }

    public static Material getGeneratorMaterial() {
        return generatorMaterial;
    }

    public static void resetAllArenas() {
        for (Arena a : arenas) {
            a.reset();
        }
    }
}
