package com.uwjsakdj.quartz_enhanced;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Quartz_Enhanced.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // ========== 石英玉魔杖 ==========
    public static final ForgeConfigSpec.BooleanValue WAND_ATTACK_HOSTILE = BUILDER
            .comment("Whether the wand attacks hostile mobs")
            .define("wandAttackHostile", true);

    public static final ForgeConfigSpec.BooleanValue WAND_ATTACK_NEUTRAL = BUILDER
            .comment("Whether the wand attacks neutral mobs (endermen, zombified piglins, polar bears, etc.)")
            .define("wandAttackNeutral", false);

    public static final ForgeConfigSpec.BooleanValue WAND_ATTACK_FRIENDLY = BUILDER
            .comment("Whether the wand attacks friendly mobs (animals, villagers, aquatic mobs, etc.)")
            .define("wandAttackFriendly", false);

    public static final ForgeConfigSpec.BooleanValue WAND_ATTACK_PLAYERS = BUILDER
            .comment("Whether the wand attacks players (excluding the caster)")
            .define("wandAttackPlayers", false);

    public static final ForgeConfigSpec.IntValue WAND_DAMAGE_RADIUS = BUILDER
            .comment("The cylindrical radius (in blocks) of the wand's final damage")
            .defineInRange("wandDamageRadius", 200, 1, 1000);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // 缓存值，运行时直接读这些字段
    public static boolean wandAttackHostile;
    public static boolean wandAttackNeutral;
    public static boolean wandAttackFriendly;
    public static boolean wandAttackPlayers;
    public static int wandDamageRadius;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        sync();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        sync();
    }

    private static void sync() {
        wandAttackHostile = WAND_ATTACK_HOSTILE.get();
        wandAttackNeutral = WAND_ATTACK_NEUTRAL.get();
        wandAttackFriendly = WAND_ATTACK_FRIENDLY.get();
        wandAttackPlayers = WAND_ATTACK_PLAYERS.get();
        wandDamageRadius = WAND_DAMAGE_RADIUS.get();
    }
}