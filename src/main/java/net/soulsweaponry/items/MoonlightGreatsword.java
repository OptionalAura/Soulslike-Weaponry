package net.soulsweaponry.items;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.projectile.MoonlightProjectile;
import net.soulsweaponry.registry.EntityRegistry;
import net.soulsweaponry.registry.SoundRegistry;
import net.soulsweaponry.registry.WeaponRegistry;
import net.soulsweaponry.util.WeaponUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoonlightGreatsword extends ChargeToUseItem {

    public MoonlightGreatsword(ToolMaterial toolMaterial, float attackSpeed, Settings settings) {
        super(toolMaterial, ConfigConstructor.moonlight_greatsword_damage, attackSpeed, settings);
    }

    public MoonlightGreatsword(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (ConfigConstructor.disable_use_moonlight_greatsword) {
            if (ConfigConstructor.inform_player_about_disabled_use) {
                user.sendMessage(Text.translatableWithFallback("soulsweapons.weapon.useDisabled","This item is disabled"));
            }
            return;
        }
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                stack.damage(3, (LivingEntity)playerEntity, (p_220045_0_) -> p_220045_0_.sendToolBreakStatus(user.getActiveHand()));

                MoonlightProjectile entity = new MoonlightProjectile(EntityRegistry.MOONLIGHT_BIG_ENTITY_TYPE, world, user);
                entity.setAgeAndPoints(30, 150, 4);
                //float damage = (float) user.getAttributes().getValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                entity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 1.5F, 1.0F);
                entity.setDamage(ConfigConstructor.moonlight_greatsword_projectile_damage);
                world.spawnEntity(entity);
                world.playSound(null, user.getBlockPos(), SoundRegistry.MOONLIGHT_BIG_EVENT, SoundCategory.PLAYERS, 1f, 1f);
                if (this instanceof BluemoonGreatsword) {
                    if (stack.hasNbt() && !playerEntity.isCreative()) {
                        stack.getNbt().putInt(IChargeNeeded.CHARGE, 0);
                    }
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (ConfigConstructor.disable_use_moonlight_greatsword && stack.isOf(WeaponRegistry.MOONLIGHT_GREATSWORD)) {
            tooltip.add(Text.translatable("tooltip.soulsweapons.disabled"));
        }
        if (Screen.hasShiftDown()) {
            if (this instanceof BluemoonGreatsword) {
                WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.NEED_CHARGE, stack, tooltip);
                WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.CHARGE, stack, tooltip);
            }
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.MOONLIGHT, stack, tooltip);
            if (!(this instanceof BluemoonGreatsword)) {
                for (int i = 1; i <= 3; i++) {
                    tooltip.add(Text.translatable("tooltip.soulsweapons.moonlight_greatsword.part_" + i).formatted(Formatting.DARK_GRAY));
                }
            }
        } else {
            tooltip.add(Text.translatable("tooltip.soulsweapons.shift"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}