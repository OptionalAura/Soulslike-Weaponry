package net.soulsweaponry.items;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.soulsweaponry.client.renderer.item.EmpoweredDawnbreakerRenderer;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.projectile.invisible.FlamePillar;
import net.soulsweaponry.registry.EffectRegistry;
import net.soulsweaponry.registry.EntityRegistry;
import net.soulsweaponry.util.IKeybindAbility;
import net.soulsweaponry.util.WeaponUtil;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Also known as "Genesis Fracture"
 */
public class EmpoweredDawnbreaker extends AbstractDawnbreaker implements IKeybindAbility {

    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public EmpoweredDawnbreaker(ToolMaterial toolMaterial, float attackSpeed, Settings settings) {
        super(toolMaterial, ConfigConstructor.empowered_dawnbreaker_damage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (ConfigConstructor.disable_use_empowered_dawnbreaker) {
            if (ConfigConstructor.inform_player_about_disabled_use){
                attacker.sendMessage(Text.translatableWithFallback("soulsweapons.weapon.useDisabled","This item is disabled"));
            }
            stack.damage(1, attacker, (e) -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            return true;
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (ConfigConstructor.disable_use_empowered_dawnbreaker) {
            if (ConfigConstructor.inform_player_about_disabled_use) {
                user.sendMessage(Text.translatableWithFallback("soulsweapons.weapon.useDisabled","This weapon is disabled"));
            }
            return TypedActionResult.fail(itemStack);
        }
        if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
            return TypedActionResult.fail(itemStack);
        }
        else {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity player && !player.getItemCooldownManager().isCoolingDown(this)) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                stack.damage(1, player, (p_220045_0_) -> p_220045_0_.sendToolBreakStatus(user.getActiveHand()));
                this.summonFlamePillars(world, stack, user);
                if (!player.isCreative()) {
                    player.getItemCooldownManager().set(this, ConfigConstructor.empowered_dawnbreaker_ability_cooldown);
                }
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 300, 0));
            }
        }
    }

    private void summonFlamePillars(World world, ItemStack stack, LivingEntity user) {
        if (!world.isClient) {
            int i = 0;
            List<BlockPos> list = new ArrayList<>();
            list.add(new BlockPos(0, 0, 0));
            while (i < 20 + 3 * WeaponUtil.getEnchantDamageBonus(stack)) {
                int x = user.getBlockX() + user.getRandom().nextInt(12) - 6;
                int y = user.getBlockY();
                int z = user.getBlockZ() + user.getRandom().nextInt(12) - 6;
                BlockPos pos = new BlockPos(x, y, z);
                for (BlockPos listPos : list) {
                    if (listPos != pos) {
                        FlamePillar pillar = new FlamePillar(EntityRegistry.FLAME_PILLAR, world);
                        pillar.setDamage(ConfigConstructor.empowered_dawnbreaker_ability_damage + WeaponUtil.getEnchantDamageBonus(stack) * 2);
                        pillar.setPos(x, y, z);
                        pillar.setOwner(user);
                        pillar.setWarmup(i * 2);
                        world.spawnEntity(pillar);
                        i++;
                    }
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (ConfigConstructor.disable_use_empowered_dawnbreaker) {
            tooltip.add(Text.translatableWithFallback("tooltip.soulsweapons.disabled","Disabled"));
        }
        if (Screen.hasShiftDown()) {
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.DAWNBREAKER, stack, tooltip);
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.BLAZING_BLADE, stack, tooltip);
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.CHAOS_STORM, stack, tooltip);
            WeaponUtil.addAbilityTooltip(WeaponUtil.TooltipAbilities.VEIL_OF_FIRE, stack, tooltip);
        } else {
            tooltip.add(Text.translatable("tooltip.soulsweapons.shift"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private final EmpoweredDawnbreakerRenderer renderer = new EmpoweredDawnbreakerRenderer();

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public void useKeybindAbilityServer(ServerWorld world, ItemStack stack, PlayerEntity player) {
        if (ConfigConstructor.disable_use_empowered_dawnbreaker) {
            if (ConfigConstructor.inform_player_about_disabled_use) {
                player.sendMessage(Text.translatableWithFallback("soulsweapons.weapon.useDisabled","This weapon is disabled"));
            }
        } else if (!player.getItemCooldownManager().isCoolingDown(this)) {
            AbstractDawnbreaker.dawnbreakerEvent(player, player, stack);
            player.addStatusEffect(new StatusEffectInstance(EffectRegistry.VEIL_OF_FIRE, 200, MathHelper.floor(WeaponUtil.getEnchantDamageBonus(stack)/2f)));
            player.getItemCooldownManager().set(this, ConfigConstructor.empowered_dawnbreaker_ability_cooldown);
        }
        /*
        NOTE: Used to summon an orb of fireballs that shoots outwards from the player, but was a little
        too laggy with the particles from the explosion.
        double phi = Math.PI * (3. - Math.sqrt(5.));
        float points = 90;
        for (int i = 0; i < points; i++) {
            double y = 1 - (i/(points - 1)) * 2;
            double radius = Math.sqrt(1 - y*y);
            double theta = phi * i;
            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;
            AgingSmallFireball entity = new AgingSmallFireball(world, player, x, y, z);
            entity.setPos(player.getX(), player.getEyeY(), player.getZ());
            world.spawnEntity(entity);
        }
         */
    }

    @Override
    public void useKeybindAbilityClient(ClientWorld world, ItemStack stack, ClientPlayerEntity player) {
    }
}