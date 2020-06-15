package de.ryuu.adventurecraft.client.items;

import de.ryuu.adventurecraft.client.entities.BombEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class PulverisItem extends BombItem {
    public PulverisItem(Settings settings, EntityType<BombEntity> entityType) {
        super(settings, entityType);
    }

    @Override
    public void playSoundEffects(World world, PlayerEntity playerEntity) {
        world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (RANDOM.nextFloat() * 0.4F + 0.8F));
    }
}
