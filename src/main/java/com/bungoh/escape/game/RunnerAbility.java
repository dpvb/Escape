package com.bungoh.escape.game;

import org.bukkit.inventory.ItemStack;

public abstract class RunnerAbility {

    protected ItemStack item;
    protected String name;
    protected int cooldown;
    protected Runner runner;

    public RunnerAbility(String name, int cooldown, Runner runner) {
        this.item = generateItem();
        this.name = name;
        this.cooldown = cooldown;
        this.runner = runner;
    }

    public abstract ItemStack generateItem();

    public abstract void use();

    public abstract void cleanup();

}
