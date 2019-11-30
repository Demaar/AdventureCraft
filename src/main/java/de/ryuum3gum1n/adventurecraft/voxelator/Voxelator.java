package de.ryuum3gum1n.adventurecraft.voxelator;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import de.ryuum3gum1n.adventurecraft.util.BlockRegion;
import de.ryuum3gum1n.adventurecraft.util.MissingNBTTagException;
import de.ryuum3gum1n.adventurecraft.util.MutableBlockPos;
import de.ryuum3gum1n.adventurecraft.util.UndoRegion;
import de.ryuum3gum1n.adventurecraft.util.UndoTask;
import de.ryuum3gum1n.adventurecraft.voxelator.actions.VXActionGrassify;
import de.ryuum3gum1n.adventurecraft.voxelator.actions.VXActionReplace;
import de.ryuum3gum1n.adventurecraft.voxelator.actions.VXActionVariationsReplace;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateAND;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateAlways;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateAverageSmooth;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateBoxSmooth;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateHasAirAbove;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateHeightLimit;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateIsAir;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateIsSolid;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateIsState;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateIsType;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateNOT;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateOR;
import de.ryuum3gum1n.adventurecraft.voxelator.predicates.VXPredicateRandom;
import de.ryuum3gum1n.adventurecraft.voxelator.shapes.VXShapeBox;
import de.ryuum3gum1n.adventurecraft.voxelator.shapes.VXShapeCylinder;
import de.ryuum3gum1n.adventurecraft.voxelator.shapes.VXShapeSphere;

// TODO: Create (de)serialization and a GUI for the VOXELATOR system.
// TODO: Create a VXAction that repeats another VXAction multiple times.
// TODO: Create VXShape Cone
// TODO: Create VXShape Pyramid
// TODO: Create VXShape Capsule
// TODO: Create VXShape HollowCapsule
// TODO: Create VXShape Expression

/**
 * A total rewrite of the VoxelBrush: Voxelator.
 **/
public class Voxelator {

	public static interface ShapeFactory {
		public abstract String getName();

		public abstract VXShape newShape(NBTTagCompound shapeData, BlockPos origin);

		public abstract NBTTagCompound newShape(String[] parameters);

		public abstract BrushParameter[] getParameters();
	}

	public static interface ActionFactory {
		public abstract String getName();

		public abstract VXAction newAction(NBTTagCompound actionData);

		public abstract NBTTagCompound newAction(String[] parameters);

		public abstract BrushParameter[] getParameters();
	}

	public static interface FilterFactory {
		public abstract String getName();

		public abstract VXPredicate newFilter(NBTTagCompound filterData);

		public abstract NBTTagCompound newFilter(String[] parameters);

		public abstract BrushParameter[] getParameters();
	}

	public static final Map<String, ShapeFactory> shapes = Maps.newHashMap();
	public static final Map<String, ActionFactory> actions = Maps.newHashMap();
	public static final Map<String, FilterFactory> filters = Maps.newHashMap();

	static {
		registerShape(VXShapeBox.FACTORY);
		registerShape(VXShapeSphere.FACTORY);
		registerShape(VXShapeCylinder.FACTORY);

		registerAction(VXActionGrassify.FACTORY);
		registerAction(VXActionReplace.FACTORY);
		registerAction(VXActionVariationsReplace.FACTORY);

		registerFilter(VXPredicateAND.FACTORY);
		registerFilter(VXPredicateNOT.FACTORY);
		registerFilter(VXPredicateOR.FACTORY);

		registerFilter(VXPredicateAlways.FACTORY);
		registerFilter(VXPredicateAverageSmooth.FACTORY);
		registerFilter(VXPredicateBoxSmooth.FACTORY);
		registerFilter(VXPredicateHasAirAbove.FACTORY);
		registerFilter(VXPredicateHeightLimit.FACTORY);
		registerFilter(VXPredicateIsSolid.FACTORY);
		registerFilter(VXPredicateIsAir.FACTORY);
		registerFilter(VXPredicateRandom.FACTORY);
		registerFilter(VXPredicateIsState.FACTORY);
		registerFilter(VXPredicateIsType.FACTORY);
	}

	private static void registerShape(ShapeFactory factory) {
		shapes.put(factory.getName(), factory);
	}

	private static void registerAction(ActionFactory factory) {
		actions.put(factory.getName(), factory);
	}

	private static void registerFilter(FilterFactory factory) {
		filters.put(factory.getName(), factory);
	}

	public static List<String> getShapeNameList() {
		List<String> l = Lists.newArrayList();
		for (String s : shapes.keySet()) {
			l.add(s);
		}
		return l;
	}

	public static List<String> getActionNameList() {
		List<String> l = Lists.newArrayList();
		for (String s : actions.keySet()) {
			l.add(s);
		}
		return l;
	}

	public static List<String> getFilterNameList() {
		List<String> l = Lists.newArrayList();
		for (String s : filters.keySet()) {
			l.add(s);
		}
		return l;
	}

	public static VXShape newShape(NBTTagCompound shapeData, BlockPos origin) {
		if (shapeData == null) {
			throw new IllegalArgumentException("shapeData must not be null!");
		}

		String type = shapeData.getString("type");

		if (type == null) {
			throw new MissingNBTTagException("(type:TagCompound) is missing!");
		}

		ShapeFactory factory = shapes.get(type);

		if (factory != null) {
			return factory.newShape(shapeData, origin);
		}

		return null;
	}

	public static VXAction newAction(NBTTagCompound actionData) {
		if (actionData == null) {
			throw new IllegalArgumentException("actionData must not be null!");
		}

		String type = actionData.getString("type");

		if (type == null) {
			throw new MissingNBTTagException("(type:TagCompound) is missing!");
		}

		ActionFactory factory = actions.get(type);

		if (factory != null) {
			return factory.newAction(actionData);
		}

		return null;
	}

	public static VXPredicate newFilter(NBTTagCompound filterData) {
		if (filterData == null) {
			throw new IllegalArgumentException("filterData must not be null!");
		}

		String type = filterData.getString("type");

		if (type == null) {
			throw new MissingNBTTagException("(type:TagCompound) is missing!");
		}

		FilterFactory factory = filters.get(type);

		if (factory != null) {
			return factory.newFilter(filterData);
		}

		return null;
	}

	public static void apply(VXShape shape, VXPredicate predicate, VXAction action, World world, EntityPlayer player) {

		final BlockPos center = shape.getCenter();
		final BlockRegion region = shape.getRegion();
		final MutableBlockPos offset = new MutableBlockPos(center);

		final List<BlockSnapshot> previous = Lists.newArrayList();
		final List<BlockSnapshot> changes = Lists.newArrayList();

		final CachedWorldDiff fworld = new CachedWorldDiff(world, previous, changes);

		UndoRegion before = new UndoRegion(region, world);

		for (final BlockPos pos : BlockPos.getAllInBox(region.getMin(), region.getMax())) {
			offset.set(pos);
			offset.__sub(center);
			if (shape.test(pos, center, offset, fworld)) {
				if (predicate.test(pos, center, offset, fworld)) {
					action.apply(pos, center, offset, fworld);
				}
			}
		}

		fworld.applyChanges(true);

		UndoRegion after = new UndoRegion(region, world);
		UndoTask.TASKS.add(new UndoTask(before, after, "Voxelator", player.getName()));
	}

}