package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class PacketListener {

    public PacketListener() {
        initListeners();
    }

    private void initListeners() {
        //Spectator Cancel Use Entity Packet Send to Server
        Escape.getProtocolManager().addPacketListener(
                new PacketAdapter(Escape.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                            if (Manager.isPlaying(event.getPlayer())) {
                                if (Manager.getArena(event.getPlayer()).getState() == GameState.LIVE) {
                                    if (Manager.getArena(event.getPlayer()).getGame().getSpectator(event.getPlayer()) != null) {
                                        event.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

}
