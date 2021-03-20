package com.bungoh.escape.files;

import com.bungoh.escape.Escape;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataFile {

    private Escape plugin;
    private static File file;
    private static YamlConfiguration config;

    public DataFile(Escape plugin) {
        this.plugin = plugin;

        file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        save();
    }

    /**
     * Checks if the arena with this name exists in config
     * @return true if the arena exists in the config
     */
    public static boolean arenaExists(String name) {
        return config.contains("arenas." + name);
    }

    /**
     * Initializes the arena in the configuration file with a world and a unique name if the arena doesn't exist.
     * Sets the ready state of the arena to false.
     * @param name - The unique name of the Arena
     * @param world - The world that the Arena is in
     * @return true if the arena was created, false if an arena with that name already exists
     */
    public static boolean initArena(String name, World world) {
        if (!arenaExists(name)) {
            config.set("arenas." + name + ".world", world.getName());
            config.set("arenas." + name + ".ready", false);
            save();
            return true;
        }

        return false;
    }

    /**
     * Sets the lobby location to the arena in the configuration file if the arena exists.
     * @param name - The unique name of the Arena
     * @param location - The target lobby location
     * @return 0 if the Arena does not exist, 1 if the Arena is in the ready state, and 2 if the Arena successfully
     * added a lobby spawn.
     */
    public static int setArenaLobbySpawn(String name, Location location) {
        if (arenaExists(name)) {
            if (!checkArenaReady(name)) {
                String path = "arenas." + name + ".lobbyspawn";
                config.set(path, location);
                save();
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    /**
     * Get arena lobby spawn given the name of the Arena
     * @param name The name of the arena
     * @return The location of the lobby spawn
     */
    public static Location getArenaLobbySpawn(String name) {
        if (arenaExists(name)) {
            return (Location) config.get("arenas." + name + ".lobbyspawn");
        }

        return null;
    }

    /**
     * Get arena corner one bound given the name of the Arena
     * @param name The name of the arena
     * @return The location of the lobby spawn
     */
    public static Location getArenaCornerOne(String name) {
        if (arenaExists(name)) {
            return (Location) config.get("arenas." + name + ".c1");
        }

        return null;
    }

    /**
     * Get arena corner two bound given the name of the Arena
     * @param name The name of the arena
     * @return The location of the lobby spawn
     */
    public static Location getArenaCornerTwo(String name) {
        if (arenaExists(name)) {
            return (Location) config.get("arenas." + name + ".c2");
        }

        return null;
    }

    /**
     * Get arena runner spawn given the name of the Arena
     * @param name The name of the arena
     * @return The location of the lobby spawn
     */
    public static Location getArenaRunnerSpawn(String name) {
        if (arenaExists(name)) {
            return (Location) config.get("arenas." + name + ".runnerspawn");
        }

        return null;
    }

    /**
     * Get arena killer spawn given the name of the Arena
     * @param name The name of the arena
     * @return The location of the lobby spawn
     */
    public static Location getArenaKillerSpawn(String name) {
        if (arenaExists(name)) {
            return (Location) config.get("arenas." + name + ".killerspawn");
        }

        return null;
    }

    /**
     * Get arena escape location given the name of the Arena
     * @param name The name of the arena
     * @return The location of the lobby spawn
     */
    public static Location getArenaEscapeLocation(String name) {
        if (arenaExists(name)) {
            return (Location) config.get("arenas." + name + ".escapeloc");
        }

        return null;
    }

    /**
     * Sets the corner 1 bound the arena in the configuration file if the arena exists.
     * @param name - The unique name of the Arena
     * @param location - The target lobby location
     * @return 0 if the Arena does not exist, 1 if the Arena is in the ready state, and 2 if the Arena successfully
     * added the first corner
     */
    public static int setArenaCornerOne(String name, Location location) {
        if (arenaExists(name)) {
            if (!checkArenaReady(name)) {
                String path = "arenas." + name + ".c1";
                config.set(path, location);
                save();
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    /**
     * Sets the corner 2 bound the arena in the configuration file if the arena exists.
     * @param name - The unique name of the Arena
     * @param location - The target lobby location
     * @return 0 if the Arena does not exist, 1 if the Arena is in the ready state, and 2 if the Arena successfully
     * added the second corner
     */
    public static int setArenaCornerTwo(String name, Location location) {
        if (arenaExists(name)) {
            if (!checkArenaReady(name)) {
                String path = "arenas." + name + ".c2";
                config.set(path, location);
                save();
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    /**
     * Sets the runner spawn of the arena in the configuration file if the arena exists.
     * @param name - The unique name of the Arena
     * @param location - The target lobby location
     * @return 0 if the Arena does not exist, 1 if the Arena is in the ready state, and 2 if the Arena successfully
     * added the runner spawn
     */
    public static int setArenaRunnerSpawn(String name, Location location) {
        if (arenaExists(name)) {
            if (!checkArenaReady(name)) {
                String path = "arenas." + name + ".runnerspawn";
                config.set(path, location);
                save();
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    /**
     * Sets the killer spawn of the arena in the configuration file if the arena exists.
     * @param name - The unique name of the Arena
     * @param location - The target lobby location
     * @return 0 if the Arena does not exist, 1 if the Arena is in the ready state, and 2 if the Arena successfully
     * added the killer spawn
     */
    public static int setArenaKillerSpawn(String name, Location location) {
        if (arenaExists(name)) {
            if (!checkArenaReady(name)) {
                String path = "arenas." + name + ".killerspawn";
                config.set(path, location);
                save();
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    /**
     * Sets the escape location of the arena in the configuration file if the arena exists.
     * @param name - The unique name of the Arena
     * @param location - The target lobby location
     * @return 0 if the Arena does not exist, 1 if the Arena is in the ready state, and 2 if the Arena successfully
     * added the escape location
     */
    public static int setArenaEscapeLocation(String name, Location location) {
        if (arenaExists(name)) {
            if (!checkArenaReady(name)) {
                String path = "arenas." + name + ".escapeloc";
                config.set(path, location);
                save();
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    /**
     * Remove the arena lobby location.
     * @param name The unique name of the Arena
     * @return 0 if the location was removed, -1 if there is no lobby location, -2 if the Arena does not exist, and
     * -3 if the Arena is not ready.
     */
    public static int removeArenaLobbyLocation(String name) {
        if (arenaExists(name)) {
            if (!checkArenaReady(name)) {
                String path = "arenas." + name + ".lobby";
                if (config.get(path) == null) {
                    return -1;
                }
                config.set(path, null);
                save();
                return 0;
            }
            return -3;
        }
        return -2;
    }

    /**
     * Toggles the ready state of the Arena
     * @param name The unique name of the Arena
     * @return 0 if the Arena does not exist, 1 if the Arena has completed setup, 2 if the Arena is ready, and 3 if the
     * Arena is not ready.
     */
    public static int arenaToggleReady(String name) {
        if (arenaExists(name)) {
            if (checkArenaSetupCompletion(name)) {
                String path = "arenas." + name + ".ready";

                boolean bool = config.getBoolean(path);
                config.set(path, !bool);
                save();
                if (!bool) {
                    return 2; // The Arena is ready.
                } else {
                    return 3; //The Arena is not ready.
                }
            } else {
                return 1; // The Arena has not completed setup.
            }
        } else {
            return 0; // The Arena does not exist.
        }
    }

    /**
     * Check if Arena is in the ready state.
     * @param name The unique the name of the Arena
     * @return true if the Arena is ready and false if the Arena doesn't exist or it is not ready.
     */
    public static boolean checkArenaReady(String name) {
        if (arenaExists(name)) {
            return config.getBoolean("arenas." + name + ".ready");
        }

        return false;
    }

    /**
     * Checks if the Arena has all required information in the config.
     * @param name The unique name of the Arena
     * @return true if the arena is ready to be used
     */
    public static boolean checkArenaSetupCompletion(String name) {
        if (arenaExists(name)) {
            String basePath = "arenas." + name + ".";
            if (config.get(basePath + ".lobbyspawn") == null
                    || config.get(basePath + ".c1") == null
                    || config.get(basePath + ".c2") == null
                    || config.get(basePath + ".runnerspawn") == null
                    || config.get(basePath + ".killerspawn") == null
                    || config.get(basePath + ".escapeloc") == null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Removes the Arena from the Data File
     * @param name The unique name of the Arena
     * @return 0 if the Arena does not exist, 1 if the Arena is in the ready state, and 2 if the arena was successfully removed.
     */
    public static int removeArena(String name) {
        if (arenaExists(name)) {
            if (!checkArenaReady(name)) {
                if (getArenaAmount() == 1) {
                    config.set("arenas", null);
                } else {
                    config.set("arenas." + name, null);
                }
                save();
                return 2;
            } else {
                return 1;
            }
        }

        return 0;
    }

    /**
     * Loop through arenas and get the amount of present Arenas there are.
     * @return The number of arenas in existence.
     */
    public static int getArenaAmount() {
        if (config.getConfigurationSection("arenas") != null) {
            return config.getConfigurationSection("arenas").getKeys(false).size();
        }

        return 0;
    }

    /**
     * Gives all Arena Names
     * @return Set of Arena Names but null if no Arenas exist.
     */
    public static Set<String> getArenaNames() {
        if (config.getConfigurationSection("arenas") != null) {
            return config.getConfigurationSection("arenas").getKeys(false);
        }

        return new HashSet<>();
    }

    /**
     * Attempt to save the data.yml file.
     */
    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
