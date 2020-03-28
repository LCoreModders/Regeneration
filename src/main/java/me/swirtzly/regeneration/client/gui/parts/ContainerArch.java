package me.swirtzly.regeneration.client.gui.parts;

import me.swirtzly.regeneration.common.item.ItemArchInterface;
import me.swirtzly.regeneration.common.item.arch.capability.ArchInventory;
import me.swirtzly.regeneration.common.item.arch.capability.CapabilityArch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerArch extends Container {

    public static final int ID = 99;
    private final ArchInventory archInvIn;

    public ContainerArch(EntityPlayer player, ArchInventory archInv) {
        IInventory playerInv = player.inventory;
        archInvIn = archInv;

        addSlotToContainer(new SlotItemHandler(archInvIn, 0, 44 + 2 * 18, 20));

        for (int l = 0; l < 3; ++l) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(playerInv, k + l * 9 + 9, 8 + k * 18, l * 18 + 51));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(playerInv, i1, 8 + i1 * 18, 109));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer player) {
        return player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemArchInterface;
    }

    public boolean isAllowed(ItemStack stack) {
        return stack.hasCapability(CapabilityArch.CAPABILITY, null);
    }

    public static class ArchSlot extends Slot {

        public ArchSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.hasCapability(CapabilityArch.CAPABILITY, null);
        }
    }

}