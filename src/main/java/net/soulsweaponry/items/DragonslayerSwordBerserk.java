package net.soulsweaponry.items;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.registry.EffectRegistry;
import net.soulsweaponry.registry.ParticleRegistry;
import net.soulsweaponry.util.IKeybindAbility;
import net.soulsweaponry.util.WeaponUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragonslayerSwordBerserk extends UltraHeavyWeapon implements IKeybindAbility {

    public DragonslayerSwordBerserk(ToolMaterial toolMaterial, float attackSpeed, Settings settings) {
        super(toolMaterial, ConfigConstructor.heap_of_raw_iron_damage, attackSpeed, settings, true);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (ConfigConstructor.disable_use_heap_of_raw_iron) {
            tooltip.add(Text.translatableWithFallback("tooltip.soulsweapons.disabled","Disabled"));
        }
        if (Screen.hasShiftDown()) {
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.RAGE, stack, tooltip);
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.HEAVY, stack, tooltip);
        } else {
            tooltip.add(Text.translatable("tooltip.soulsweapons.shift"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (ConfigConstructor.disable_use_heap_of_raw_iron) {
            stack.damage(1, attacker, (e) -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            return true;
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void useKeybindAbilityServer(ServerWorld world, ItemStack stack, PlayerEntity user) {
        if (ConfigConstructor.disable_use_heap_of_raw_iron) {
            if (ConfigConstructor.inform_player_about_disabled_use){
                user.sendMessage(Text.translatableWithFallback("soulsweapons.weapon.useDisabled","This weapon is disabled"));
            }
            return;
        }
        if (!user.getItemCooldownManager().isCoolingDown(this)) {
            stack.damage(1, user, (p_220045_0_) -> p_220045_0_.sendToolBreakStatus(user.getActiveHand()));
            user.getItemCooldownManager().set(this, ConfigConstructor.heap_of_raw_iron_cooldown);
            int power = MathHelper.floor(WeaponUtil.getEnchantDamageBonus(stack));
            user.addStatusEffect(new StatusEffectInstance(EffectRegistry.BLOODTHIRSTY, 200, power));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 0));
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, .75f, 1f);
        }
    }

    @Override
    public void useKeybindAbilityClient(ClientWorld world, ItemStack stack, ClientPlayerEntity player) {
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
        return ConfigConstructor.heap_of_raw_iron_launch_multiplier;
    }

    @Override
    public boolean shouldHeal() {
        return false;
    }

    @Override
    public StatusEffectInstance[] applyEffects() {
        return new StatusEffectInstance[] {new StatusEffectInstance(StatusEffects.WITHER, 140, 1)};
    }

    @Override
    public Map<ParticleEffect, Vec3d> getParticles() {
        Map<ParticleEffect, Vec3d> map = new HashMap<>();
        map.put(ParticleRegistry.DARK_STAR, new Vec3d(1, 6, 1));
        map.put(ParticleTypes.FLAME, new Vec3d(1, 6, 1));
        return map;
    }
}