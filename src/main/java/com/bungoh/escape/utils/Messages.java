package com.bungoh.escape.utils;

import com.bungoh.escape.files.ConfigFile;

public enum Messages {

    PREFIX("prefix"),
    ARENA_DOES_NOT_EXIST("messages.arena_does_not_exist"),
    ARENA_ALREADY_EXISTS("messages.arena_already_exists"),
    ARENA_NOT_SETUP("messages.arena_not_setup"),
    ARENA_READY("messages.arena_ready"),
    ARENA_NOT_READY("messages.arena_not_ready"),
    ARENA_NOT_EDITABLE("messages.arena_not_editable"),
    ARENA_CREATED("messages.arena_created"),
    ARENA_REMOVED("messages.arena_removed"),
    ARENA_CORNER_ONE_SET("messages.arena_corner_one_set"),
    ARENA_CORNER_TWO_SET("messages.arena_corner_two_set"),
    ARENA_ESCAPE_SET("messages.arena_escape_set"),
    ARENA_KILLER_LOCATION_SET("messages.arena_killer_location_set"),
    ARENA_RUNNER_LOCATION_SET("messages.arena_runner_location_set"),
    ARENA_LOBBY_LOCATION_SET("messages.arena_lobby_location_set"),
    ARENA_NOT_RECRUITING("messages.arena_not_recruiting"),
    ARENA_GENERATOR_ADDED("messages.arena_generator_added"),
    ARENA_GENERATOR_REMOVED("messages.arena_generator_removed"),
    ARENA_NO_GENERATORS_LEFT("messages.arena_no_generators_left"),
    ARENA_INSUFFICIENT_GENS("messages.arena_insufficient_gens"),
    ARENA_ALREADY_INGAME("messages.arena_already_ingame"),
    UNEXPECTED_ERROR("messages.unexpected_error");

    private String path;

    Messages(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
