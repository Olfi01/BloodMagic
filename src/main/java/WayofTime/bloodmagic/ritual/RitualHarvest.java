package WayofTime.bloodmagic.ritual;

import WayofTime.bloodmagic.api.BlockStack;
import WayofTime.bloodmagic.api.Constants;
import WayofTime.bloodmagic.api.iface.IHarvestHandler;
import WayofTime.bloodmagic.api.network.SoulNetwork;
import WayofTime.bloodmagic.api.registry.HarvestRegistry;
import WayofTime.bloodmagic.api.ritual.*;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class RitualHarvest extends Ritual
{
    public static final String HARVEST_RANGE = "harvestRange";

    public RitualHarvest()
    {
        super("ritualHarvest", 0, 20000, "ritual." + Constants.Mod.MODID + ".harvestRitual");

        HarvestRegistry.registerRangeAmplifier(new BlockStack(Blocks.diamond_block), 15);
        HarvestRegistry.registerRangeAmplifier(new BlockStack(Blocks.gold_block), 10);
        HarvestRegistry.registerRangeAmplifier(new BlockStack(Blocks.iron_block), 6);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone)
    {
        SoulNetwork network = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
        World world = masterRitualStone.getWorldObj();

        if (network.getCurrentEssence() < getRefreshCost())
        {
            network.causeNauseaToPlayer();
            return;
        }

        BlockStack amplifierStack = BlockStack.getStackFromPos(world, masterRitualStone.getBlockPos().up());

        int range = 4;
        if (amplifierStack != null)
            if (HarvestRegistry.getAmplifierMap().containsKey(amplifierStack))
                range = HarvestRegistry.getAmplifierMap().get(amplifierStack);

        addBlockRange(HARVEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-range, 0, -range), new BlockPos(range + 1, 4, range + 1)));

        int harvested = 0;

        for (BlockPos pos : getBlockRange(HARVEST_RANGE).getContainedPositions(masterRitualStone.getBlockPos().up()))
            if (harvestBlock(world, pos))
                harvested++;

        network.syphon(getRefreshCost() * Math.min(100, harvested));
    }

    @Override
    public int getRefreshCost()
    {
        return 20;
    }

    @Override
    public int getRefreshTime()
    {
        return 5;
    }

    @Override
    public ArrayList<RitualComponent> getComponents()
    {
        ArrayList<RitualComponent> components = new ArrayList<RitualComponent>();

        components.add(new RitualComponent(new BlockPos(1, 0, 1), EnumRuneType.DUSK));
        components.add(new RitualComponent(new BlockPos(1, 0, -1), EnumRuneType.DUSK));
        components.add(new RitualComponent(new BlockPos(-1, 0, -1), EnumRuneType.DUSK));
        components.add(new RitualComponent(new BlockPos(-1, 0, 1), EnumRuneType.DUSK));
        components.add(new RitualComponent(new BlockPos(2, 0, 0), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(-2, 0, 0), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(0, 0, 2), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(0, 0, -2), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(3, 0, 1), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(3, 0, -1), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(-3, 0, 1), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(-3, 0, -1), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(1, 0, 3), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(-1, 0, 3), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(1, 0, -3), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(-1, 0, -3), EnumRuneType.EARTH));
        components.add(new RitualComponent(new BlockPos(2, 0, 3), EnumRuneType.WATER));
        components.add(new RitualComponent(new BlockPos(3, 0, 2), EnumRuneType.WATER));
        components.add(new RitualComponent(new BlockPos(2, 0, -3), EnumRuneType.WATER));
        components.add(new RitualComponent(new BlockPos(-3, 0, 2), EnumRuneType.WATER));
        components.add(new RitualComponent(new BlockPos(-2, 0, 3), EnumRuneType.WATER));
        components.add(new RitualComponent(new BlockPos(3, 0, -2), EnumRuneType.WATER));
        components.add(new RitualComponent(new BlockPos(-2, 0, -3), EnumRuneType.WATER));
        components.add(new RitualComponent(new BlockPos(-3, 0, -2), EnumRuneType.WATER));

        return components;
    }

    @Override
    public Ritual getNewCopy()
    {
        return new RitualHarvest();
    }

    public static boolean harvestBlock(World world, BlockPos pos) {
        BlockStack harvestStack = BlockStack.getStackFromPos(world, pos);

        for (IHarvestHandler handler : HarvestRegistry.getHandlerList())
            if (handler.harvestAndPlant(world, pos, harvestStack))
                return true;

        return false;
    }
}
