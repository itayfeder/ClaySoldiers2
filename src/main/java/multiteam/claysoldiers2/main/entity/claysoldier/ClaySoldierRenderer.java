package multiteam.claysoldiers2.main.entity.claysoldier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import multiteam.claysoldiers2.main.modifiers.modifier.CSModifier;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class ClaySoldierRenderer extends GeoEntityRenderer<ClaySoldierEntity> {

    public ClaySoldierRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ClaySoldierModel());
    }

    @Override
    public RenderType getRenderType(ClaySoldierEntity animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void render(ClaySoldierEntity thisSoldier, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(thisSoldier, entityYaw, partialTicks, stack, bufferIn, packedLightIn);

        for (CSModifier.Instance instance : thisSoldier.getModifiers()){
            if(instance != null){
                instance.getModifier().additionalModifierRenderComponent();
            }
        }

    }
}
