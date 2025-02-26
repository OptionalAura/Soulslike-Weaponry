package net.soulsweaponry.items;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.registry.EffectRegistry;
import net.soulsweaponry.registry.ParticleRegistry;
import net.soulsweaponry.util.WeaponUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Featherlight extends UltraHeavyWeapon {

    public Featherlight(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, ConfigConstructor.featherlight_damage, - (4f - (ConfigConstructor.disable_use_featherlight ? 1f : ConfigConstructor.featherlight_attack_speed)), settings, true);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (ConfigConstructor.disable_use_featherlight) {
            tooltip.add(Text.translatableWithFallback("tooltip.soulsweapons.disabled","Disabled"));
        }
        if (Screen.hasShiftDown()) {
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.FEATHERLIGHT, stack, tooltip);
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.HEAVY, stack, tooltip);
        } else {
            tooltip.add(Text.translatable("tooltip.soulsweapons.shift"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return TypedActionResult.fail(user.getStackInHand(hand));
    }

    @Override
    public float getBaseExpansion() {
        return 2.5f;
    }

    @Override
    public float getExpansionModifier() {
        return 1.6f;
    }

    @Override
    public float getLaunchMultiplier() {
        return ConfigConstructor.featherlight_launch_multiplier;
    }

    @Override
    public boolean shouldHeal() {
        return false;
    }

    @Override
    public StatusEffectInstance[] applyEffects() {
        return new StatusEffectInstance[] {
                new StatusEffectInstance(EffectRegistry.BLIGHT, 200, 4),
                new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 2)
        };
    }

    @Override
    public Map<ParticleEffect, Vec3d> getParticles() {
        Map<ParticleEffect, Vec3d> map = new HashMap<>();
        map.put(ParticleRegistry.PURPLE_FLAME, new Vec3d(1, 6, 1));
        return map;
    }
}