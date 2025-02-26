package net.soulsweaponry.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.projectile.DragonslayerSwordspearEntity;
import net.soulsweaponry.particles.ParticleEvents;
import net.soulsweaponry.particles.ParticleHandler;
import net.soulsweaponry.util.WeaponUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonslayerSwordspear extends ChargeToUseItem {

    private static final String RAINING = "raining_id";

    public DragonslayerSwordspear(ToolMaterial toolMaterial, float attackSpeed, Settings settings) {
        super(toolMaterial, ConfigConstructor.dragonslayer_swordspear_damage, attackSpeed, settings);
    }

    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (ConfigConstructor.disable_use_dragonslayer_swordspear) {
            if (ConfigConstructor.inform_player_about_disabled_use){
                user.sendMessage(Text.translatableWithFallback("soulsweapons.weapon.useDisabled","This item is disabled"));
            }
            return;
        }
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                if (stack != user.getOffHandStack()) {
                    stack.damage(1, (LivingEntity)playerEntity, (p_220045_0_) -> p_220045_0_.sendToolBreakStatus(user.getActiveHand()));
                    DragonslayerSwordspearEntity entity = new DragonslayerSwordspearEntity(world, playerEntity, stack);
                    entity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 5.0F, 1.0F);
                    entity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                    world.spawnEntity(entity);
                    world.playSoundFromEntity(null, entity, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    playerEntity.getItemCooldownManager().set(this, (ConfigConstructor.dragonslayer_swordspear_throw_cooldown - (EnchantmentHelper.getLevel(Enchantments.UNBREAKING, stack)*20)) / (world.isRaining() ? 2 : 1));
                } else {
                    stack.damage(3, (LivingEntity)playerEntity, (p_220045_0_) -> p_220045_0_.sendToolBreakStatus(user.getActiveHand()));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20, 5));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 100, 0));
                    Box chunkBox = new Box(user.getX() - 10, user.getY() - 5, user.getZ() - 10, user.getX() + 10, user.getY() + 5, user.getZ() + 10);
                    List<Entity> nearbyEntities = world.getOtherEntities(user, chunkBox);
                    //Entity["EntityKey"/number?, l = "ClientLevel", x, y, z] and so on... Includes items aswell!
                    for (Entity nearbyEntity : nearbyEntities) {
                        if (nearbyEntity instanceof LivingEntity target && !(nearbyEntity instanceof TameableEntity)) {
                            if (world.isSkyVisible(target.getBlockPos())) {
                                for (i = 0; i < ConfigConstructor.dragonslayer_swordspear_lightning_amount; i++) {
                                    LightningEntity entity = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                                    entity.setPos(target.getX(), target.getY(), target.getZ());
                                    world.spawnEntity(entity);
                                }
                            } else {
                                double x = target.getX() - user.getX();
                                double z = target.getX() - user.getX();
                                target.takeKnockback(5F, -x, -z);
                                target.damage(world.getDamageSources().mobAttack(user), ConfigConstructor.dragonslayer_swordspear_ability_damage);
                                if (!world.isClient) {
                                    ParticleHandler.particleSphereList(world, 20, target.getX(), target.getY(), target.getZ(), ParticleEvents.DARK_EXPLOSION_LIST, 0.3f);
                                }
                            }
                            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1f, 1f);
                        }
                    }
                    int sharpness = WeaponUtil.getEnchantDamageBonus(stack);
                    playerEntity.getItemCooldownManager().set(this, (ConfigConstructor.dragonslayer_swordspear_ability_cooldown - (sharpness*20)) / (world.isRaining() ? 2 : 1));
                }
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        this.updateRaining(world, stack);
    }

    private void updateRaining(World world, ItemStack stack) {
        if (stack.hasNbt()) {
            stack.getNbt().putBoolean(RAINING, world.isRaining());
        }
    }

    private boolean getRaining(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains(RAINING)) {
            return stack.getNbt().getBoolean(RAINING);
        } else {
            return false;
        }
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
        if (slot == EquipmentSlot.MAINHAND && this.getRaining(stack)) {
            Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", ConfigConstructor.dragonslayer_swordspear_damage, EntityAttributeModifier.Operation.ADDITION));
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.2D, EntityAttributeModifier.Operation.ADDITION));
            attributeModifiers = builder.build();
            return attributeModifiers;
        } else {
            return super.getAttributeModifiers(slot);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (ConfigConstructor.disable_use_dragonslayer_swordspear) {
            tooltip.add(Text.translatableWithFallback("tooltip.soulsweapons.disabled","Disabled"));
        }
        if (Screen.hasShiftDown()) {
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.LIGHTNING_CALL, stack, tooltip);
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.INFINITY, stack, tooltip);
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.THROW_LIGHTNING, stack, tooltip);
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.STORM_STOMP, stack, tooltip);
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.WEATHERBORN, stack, tooltip);
        } else {
            tooltip.add(Text.translatable("tooltip.soulsweapons.shift"));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }
}