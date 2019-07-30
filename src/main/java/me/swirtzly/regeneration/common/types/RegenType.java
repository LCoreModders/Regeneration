package me.swirtzly.regeneration.common.types;

import me.swirtzly.regeneration.client.rendering.types.ATypeRenderer;
import me.swirtzly.regeneration.common.capability.IRegeneration;
import net.minecraft.entity.player.PlayerEntity;

/**
 * SUBCLASSES MUST HAVE A DEFAULT CONSTRUCTOR
 * <p>
 * Created by Sub
 * on 16/09/2018.
 */
public interface RegenType<R extends ATypeRenderer<?>> {

	static RegenType<?> getType(TypeManager.Type type) {

		return TypeManager.getTypeInstance(TypeManager.Type.FIERY);
	}
	
	/**
	 * @return in ticks
	 */
	int getAnimationLength();
	
	R getRenderer();
	
	default void onStartRegeneration(PlayerEntity player, IRegeneration capability) {
	}
	
	default void onUpdateMidRegen(PlayerEntity player, IRegeneration capability) {
	}
	
	default void onFinishRegeneration(PlayerEntity player, IRegeneration capability) {
	}

    double getAnimationProgress(IRegeneration cap);

	TypeManager.Type getTypeID();

	
}