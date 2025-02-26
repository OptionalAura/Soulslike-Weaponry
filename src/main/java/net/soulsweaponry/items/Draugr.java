package net.soulsweaponry.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.util.WeaponUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Draugr extends SwordItem {

    public static final String NIGHT = "night";

    public Draugr(ToolMaterial toolMaterial, float attackSpeed, Settings settings) {
        super(toolMaterial, 1, attackSpeed, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        this.refreshDayTime(world, stack);
    }

    private void refreshDayTime(World world, ItemStack stack) {
        if (stack.hasNbt() && !world.isClient) {
            stack.getNbt().putBoolean(NIGHT, world.getDimension().hasSkyLight() && world.isNight());
        }
    }

    private boolean isNight(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains(NIGHT)) {
            return stack.getNbt().getBoolean(NIGHT);
        } else {
            return false;
        }
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
        if (slot == EquipmentSlot.MAINHAND) {
            Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
            if (ConfigConstructor.disable_use_draugr) {
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 0, EntityAttributeModifier.Operation.ADDITION));
            } else {
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", this.isNight(stack) ? ConfigConstructor.draugr_damage_at_night - 1 : 0, EntityAttributeModifier.Operation.ADDITION));
            }
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.4D, EntityAttributeModifier.Operation.ADDITION));
            attributeModifiers = builder.build();
            return attributeModifiers;
        } else {
            return super.getAttributeModifiers(slot);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (ConfigConstructor.disable_use_draugr) {
            tooltip.add(Text.translatableWithFallback("tooltip.soulsweapons.disabled","Disabled"));
        }
        if (Screen.hasShiftDown()) {
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.NIGHT_PROWLER, stack, tooltip);
        } else {
            tooltip.add(Text.translatable("tooltip.soulsweapons.shift"));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }
}