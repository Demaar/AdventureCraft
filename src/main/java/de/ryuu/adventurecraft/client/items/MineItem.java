package de.ryuu.adventurecraft.client.items;

import de.ryuu.adventurecraft.client.entities.BombEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class MineItem extends BombItem {
    public MineItem(Settings settings, EntityType<BombEntity> entityType) {
        super(settings, entityType);
    }

    @Override
    public void playSoundEffects(World world, PlayerEntity playerEntity) {
        world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF, SoundCategory.NEUTRAL, 0.8F, 0.5F);
    }
}
