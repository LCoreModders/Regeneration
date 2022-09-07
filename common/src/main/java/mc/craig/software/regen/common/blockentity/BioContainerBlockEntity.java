package mc.craig.software.regen.common.blockentity;

import mc.craig.software.regen.common.item.HandItem;
import mc.craig.software.regen.common.objects.RItems;
import mc.craig.software.regen.common.objects.RSounds;
import mc.craig.software.regen.common.objects.RTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BioContainerBlockEntity extends BlockEntity implements BlockEntityTicker<BioContainerBlockEntity> {

    private boolean updateSkin = true;
    private boolean isOpen = false;

    private AnimationState openState = new AnimationState();
    private AnimationState closeState = new AnimationState();

    public BioContainerBlockEntity(BlockPos pos, BlockState state) {
        super(RTiles.HAND_JAR.get(), pos, state);
    }

    public static void spawnParticles(Level world, BlockPos blockPos) {
        RandomSource random = world.random;

        for (Direction direction : Direction.values()) {
            BlockPos blockpos = blockPos.relative(direction);
            if (!world.getBlockState(blockpos).isSolidRender(world, blockpos)) {
                Direction.Axis currentDirection = direction.getAxis();
                double x = currentDirection == Direction.Axis.X ? 0.5D + 0.5625D * (double) direction.getStepX() : (double) random.nextFloat();
                double y = currentDirection == Direction.Axis.Y ? 0.5D + 0.5625D * (double) direction.getStepY() : (double) random.nextFloat();
                double z = currentDirection == Direction.Axis.Z ? 0.5D + 0.5625D * (double) direction.getStepZ() : (double) random.nextFloat();
                //TODO Custom Particle
                world.addParticle(ParticleTypes.ANGRY_VILLAGER, (double) blockPos.getX() + x, (double) blockPos.getY() + y, (double) blockPos.getZ() + z, 0.0D, 0.0D, 0.0D);
            }
        }
    }


    public AnimationState getOpenState() {
        return openState;
    }

    public AnimationState getCloseState() {
        return closeState;
    }

    public float getLindos() {
        return HandItem.getEnergy(getHand());
    }

    public void setLindos(float lindos) {
        lindos = Mth.clamp(lindos, 0, 100);
        HandItem.setEnergy(lindos, getHand());
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }


    @Override
    public void tick(@NotNull Level currentLevel, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull BioContainerBlockEntity bioContainerBlockEntity) {

        isOpen = getHand().isEmpty();

        if(isOpen){
            closeState.stop();
            if(!openState.isStarted()){
                openState.start(Math.toIntExact(currentLevel.getDayTime()));
            }
        } else {
            openState.stop();
            if(!closeState.isStarted()){
                closeState.start(Math.toIntExact(currentLevel.getDayTime()));
            }
        }

        if (isValid(Action.CREATE)) {
            spawnParticles(currentLevel, worldPosition);
        }
        if (currentLevel != null && currentLevel.isClientSide) return;

        if (currentLevel.getGameTime() % 77 == 0 && !isOpen) {
            currentLevel.playSound(null, getBlockPos(), RSounds.JAR_BUBBLES.get(), SoundSource.PLAYERS, 0.2F, 0.2F);
        }

        if (level.getGameTime() % 100 == 0) {
            if (pendingSkinUpdate()) {
                setUpdateSkin(false);
            }
        }
    }

    public boolean isValid(Action action) {
        if (action == Action.ADD) {
            return getHand().getItem() == RItems.HAND.get() && getLindos() < 100;
        }
        if (action == Action.CREATE) {
            return getHand().getItem() == RItems.HAND.get() && getLindos() >= 100;
        }
        return false;
    }

    public void dropHandIfPresent(Player player) {
        if (!getHand().isEmpty()) {
            if (player != null) {
                if (!player.addItem(getHand())) {
                    Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), getHand());
                }
            } else {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), getHand());
            }
            setHand(ItemStack.EMPTY);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag compoundTag = new CompoundTag();
        saveAdditional(compoundTag);
        return compoundTag;
    }

    public void sendUpdates() {
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);
        setChanged();
    }


    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void load(CompoundTag nbt) {
        setLindos(nbt.getFloat("energy"));
        setUpdateSkin(nbt.getBoolean("update_skin"));
        setOpen(nbt.getBoolean("is_open"));
        super.load(nbt);
    }


    @Override
    public void saveAdditional(CompoundTag compound) {
        compound.putFloat("energy", getLindos());
        compound.putBoolean("update_skin", updateSkin);
        compound.putBoolean("is_open", isOpen);
        super.saveAdditional(compound);
    }

    public boolean pendingSkinUpdate() {
        return updateSkin;
    }

    public void setUpdateSkin(boolean updateSkin) {
        this.updateSkin = updateSkin;
    }

    public ItemStack getHand() {
        return ItemStack.EMPTY; //TODO
      //  return itemHandler.getStackInSlot(0);
    }

    // ==== Inventory ====

    public void setHand(ItemStack stack) {
        setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    public enum Action {
        ADD, CREATE
    }
}