package me.suff.mc.regen.client.rendering.types;

import com.mojang.blaze3d.platform.GlStateManager;
import me.suff.mc.regen.common.capability.IRegen;
import me.suff.mc.regen.common.capability.RegenCap;
import me.suff.mc.regen.common.types.FieryType;
import me.suff.mc.regen.common.types.RegenTypes;
import me.suff.mc.regen.util.client.RenderUtil;
import me.suff.mc.regen.util.common.PlayerUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.ModList;

public class FieryRenderer extends ATypeRenderer<FieryType> {


    /* Note 1#: Quark does this weird thing, where it appears to break the Regeneration Effect Staying at the arms
     * In theory, this is because the two mods animate the player model, not sure if on my side or theres
     * It is most likely there's, but until it is properly investigated, some fairly terrible fixes
     * can be found throughout this class */

    public static final FieryRenderer INSTANCE = new FieryRenderer();

    /* This renders a overlay of the LivingEntities model over the original model and colors it their primary color,
    the opacity increasing and creasing over time */
    public static void renderOverlay(LivingRenderer renderer, LivingEntity entityPlayer, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        RegenCap.get(entityPlayer).ifPresent((data) -> {
            GlStateManager.pushMatrix();
            RenderUtil.setLightmapTextureCoords(240, 240);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 1);
            Vec3d color = data.getPrimaryColor();
            float opacity = MathHelper.clamp(MathHelper.sin((entityPlayer.tickCount + partialTicks) / 10F) * 0.1F + 0.1F, 0.11F, 1F);
            GlStateManager.color4f((float) color.x, (float) color.y, (float) color.z, opacity);
            renderer.getModel().render(entityPlayer, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            RenderUtil.restoreLightMap();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        });
    }

    /* This renders the "Fiery" cone, used in the hands and the head of the player */
    public static void renderCone(LivingEntity entityPlayer, float scale, float scale2, Vec3d color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuilder();

        for (int i = 0; i < 8; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(entityPlayer.tickCount * 4 + i * 45, 0.0F, 1.0F, 0.0F);
            GlStateManager.scalef(1.0f, 1.0f, 0.65f);
            vertexBuffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
            vertexBuffer.vertex(0.0D, 0.0D, 0.0D).color((float) color.x, (float) color.y, (float) color.z, 55).endVertex();
            vertexBuffer.vertex(-0.266D * scale, scale, -0.5F * scale).color((float) color.x, (float) color.y, (float) color.z, 55).endVertex();
            vertexBuffer.vertex(0.266D * scale, scale, -0.5F * scale).color((float) color.x, (float) color.y, (float) color.z, 55).endVertex();
            vertexBuffer.vertex(0.0D, scale2, 1.0F * scale).color((float) color.x, (float) color.y, (float) color.z, 55).endVertex();
            vertexBuffer.vertex(-0.266D * scale, scale, -0.5F * scale).color((float) color.x, (float) color.y, (float) color.z, 55).endVertex();
            tessellator.end();
            GlStateManager.popMatrix();
        }
    }

    /* This renders the "Fiery" cone at the the players arms, it has it's own method for more control and to help
     * assist in fixing Note #1 */
    private static void renderConeAtArms(LivingEntity player, HandSide side) {
        GlStateManager.pushMatrix();
        RegenCap.get(player).ifPresent((data) -> {
            double x = data.getRegenType().create().getAnimationProgress(data);
            double p = 109.89010989010987; // see the wiki for the explanation of these "magic" numbers
            double r = 0.09890109890109888;
            double f = p * Math.pow(x, 2) - r;
            float cf = MathHelper.clamp((float) f, 0F, 1F);
            float primaryScale = data.isSyncingToJar() ? 100 : cf * 4F;
            float secondaryScale = data.isSyncingToJar() ? 100 : cf * 6.4F;

            CompoundNBT style = data.getStyle();
            Vec3d primaryColor = new Vec3d(style.getFloat("PrimaryRed"), style.getFloat("PrimaryGreen"), style.getFloat("PrimaryBlue"));
            Vec3d secondaryColor = new Vec3d(style.getFloat("SecondaryRed"), style.getFloat("SecondaryGreen"), style.getFloat("SecondaryBlue"));

            // State manager changes
            GlStateManager.pushTextureAttributes();
            GlStateManager.disableTexture();
            GlStateManager.enableAlphaTest();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.SourceFactor.CONSTANT_ALPHA.value);
            GlStateManager.depthMask(true);
            RenderUtil.setLightmapTextureCoords(65, 65);

            if (data.isSyncingToJar()) {
                GlStateManager.rotatef(-20, 1, 0, 0);
            }


            /* See Note #1 at top of class*/
            if (ModList.get().isLoaded("quark")) {
                double animationProgress = data.getAnimationTicks();
                float armRotY = (float) animationProgress * 1.5F;
                float armRotZ = (float) animationProgress * 1.5F;

                if (armRotY > 90) {
                    armRotY = 90;
                }

                if (armRotZ > 95) {
                    armRotZ = 95;
                }

                GlStateManager.translatef(0, 0.10F, 0);
                GlStateManager.rotated(side == HandSide.LEFT ? armRotZ : -armRotZ, 0, 0, 1);
                GlStateManager.rotated(side == HandSide.LEFT ? armRotY : -armRotY, 0, 1, 0);
            }

            renderCone(player, primaryScale, primaryScale, primaryColor);
            renderCone(player, secondaryScale, secondaryScale * 1.5f, secondaryColor);

            // Undo state manager changes
            RenderUtil.restoreLightMap();
            GlStateManager.depthMask(false);
            GlStateManager.disableBlend();
            GlStateManager.disableAlphaTest();
            GlStateManager.color4f(255, 255, 255, 255);
            GlStateManager.enableTexture();
            GlStateManager.popAttributes();
        });
        GlStateManager.popMatrix();
    }

    @Override
    public void renderHand(LivingEntity player, HandSide handSide, LivingRenderer render) {

        /* See Note #1 at top of class*/
        if (!ModList.get().isLoaded("quark")) {
            renderConeAtArms(player, handSide);
        }
    }

    @Override
    protected void onRenderPre(FieryType type, RenderPlayerEvent.Pre event, IRegen capability) {
        /* This method has no implementation for this Regeneration type */
    }

    @Override
    protected void onRenderPost(FieryType type, RenderPlayerEvent.Post event, IRegen capability) {
        /* This method has no implementation for this Regeneration type */
    }

    @Override
    public void onRenderLayer(FieryType type, LivingRenderer renderLivingBase, IRegen capability, LivingEntity entityPlayer, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        // State manager changes
        GlStateManager.pushTextureAttributes();
        GlStateManager.disableTexture();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(true);
        RenderUtil.setLightmapTextureCoords(65, 65);

        CompoundNBT style = capability.getStyle();
        Vec3d primaryColor = new Vec3d(style.getFloat("PrimaryRed"), style.getFloat("PrimaryGreen"), style.getFloat("PrimaryBlue"));
        Vec3d secondaryColor = new Vec3d(style.getFloat("SecondaryRed"), style.getFloat("SecondaryGreen"), style.getFloat("SecondaryBlue"));

        double x = type.getAnimationProgress(capability);
        double p = 109.89010989010987; // see the wiki for the explanation of these "magic" numbers
        double r = 0.09890109890109888;
        double f = p * Math.pow(x, 2) - r;

        float cf = MathHelper.clamp((float) f, 0F, 1F);
        float primaryScale = cf * 4F;
        float secondaryScale = cf * 6.4F;

        // Render head cone
        GlStateManager.pushMatrix();

        if (renderLivingBase.getModel() instanceof BipedModel) {
            BipedModel player = (BipedModel) renderLivingBase.getModel();
            player.head.translateTo(scale);
        }

        GlStateManager.translatef(0f, 0.09f, 0f);
        GlStateManager.rotatef(180, 1.0f, 0.0f, 0.0f);

        renderCone(entityPlayer, primaryScale / 1.6F, primaryScale * .75F, primaryColor);
        renderCone(entityPlayer, secondaryScale / 1.6F, secondaryScale / 1.5F, secondaryColor);
        GlStateManager.popMatrix();

        if (!capability.isSyncingToJar()) {
            // Render glowing overlay
            renderOverlay(renderLivingBase, entityPlayer, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        }
        // Undo state manager changes
        RenderUtil.restoreLightMap();
        GlStateManager.depthMask(false);
        GlStateManager.disableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.color4f(255, 255, 255, 255);
        GlStateManager.enableTexture();
        GlStateManager.popAttributes();


        /* See Note #1 at top of class*/
        if (ModList.get().isLoaded("quark")) {
            renderConeAtArms(entityPlayer, HandSide.LEFT);
            renderConeAtArms(entityPlayer, HandSide.RIGHT);
        }
    }

    @Override
    public void preRenderCallback(LivingRenderer renderer, LivingEntity entity) {
        /* This method has no implementation for this Regeneration type */
    }

    @Override
    public void animateEntity(BipedModel playerModel, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        RegenCap.get(entity).ifPresent((data) -> {

            /* We want the player to go into a "T-Pose" type animation while they are Regenerating in this Fiery Type */
            if (data.getState() == PlayerUtil.RegenState.REGENERATING && data.getRegenType() == RegenTypes.FIERY) {
                double animationProgress = data.getAnimationTicks();
                double arm_shake = entity.getRandom().nextDouble();
                float armRotY = (float) animationProgress * 1.5F;
                float armRotZ = (float) animationProgress * 1.5F;
                float headRot = (float) animationProgress * 1.5F;

                if (armRotY > 90) {
                    armRotY = 90;
                }

                if (armRotZ > 95) {
                    armRotZ = 95;
                }

                if (headRot > 45) {
                    headRot = 45;
                }

                // ARMS
                playerModel.leftArm.yRot = 0;
                playerModel.rightArm.yRot = 0;

                playerModel.leftArm.xRot = 0;
                playerModel.rightArm.xRot = 0;

                playerModel.leftArm.zRot = (float) -Math.toRadians(armRotZ + arm_shake);
                playerModel.rightArm.zRot = (float) Math.toRadians(armRotZ + arm_shake);
                playerModel.leftArm.yRot = (float) -Math.toRadians(armRotY);
                playerModel.rightArm.yRot = (float) Math.toRadians(armRotY);

                // BODY
                playerModel.body.xRot = 0;
                playerModel.body.yRot = 0;
                playerModel.body.zRot = 0;

                // LEGS
                playerModel.leftLeg.yRot = 0;
                playerModel.rightLeg.yRot = 0;

                playerModel.leftLeg.xRot = 0;
                playerModel.rightLeg.xRot = 0;

                playerModel.leftLeg.zRot = (float) -Math.toRadians(5);
                playerModel.rightLeg.zRot = (float) Math.toRadians(5);


                playerModel.head.xRot = (float) Math.toRadians(-headRot);
                playerModel.head.yRot = (float) Math.toRadians(0);
                playerModel.head.zRot = (float) Math.toRadians(0);

            }
        });
    }
}