package me.suff.mc.regen.api;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

public class RegenerationEvent extends EntityEvent {

    public RegenerationEvent(Entity entity) {
        super(entity);
    }
}