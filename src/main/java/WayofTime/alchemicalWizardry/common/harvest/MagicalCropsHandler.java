package WayofTime.alchemicalWizardry.common.harvest;

import WayofTime.alchemicalWizardry.api.harvest.IHarvestHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import java.util.List;

public class MagicalCropsHandler implements IHarvestHandler
{

    public Block harvestBlock;
    public int harvestMeta;
    public IPlantable harvestSeed;

    public MagicalCropsHandler(String id, int meta)
    {
        harvestBlock = getBlockForString(id);
        harvestMeta = meta;
    }

    public boolean canHandleBlock(Block block)
    {
        return block == harvestBlock;
    }

    public int getHarvestMeta(Block block)
    {
        return harvestMeta;
    }

    @Override
    public boolean harvestAndPlant(World world, int xCoord, int yCoord, int zCoord, Block block, int meta)
    {
        if (!this.canHandleBlock(block) || meta != this.getHarvestMeta(block))
        {
            return false;
        }
        world.func_147480_a(xCoord, yCoord, zCoord, true);
        return true;
    }
    
    public static Block getBlockForString(String str)
    {
        String[] parts = str.split(":");
        String modId = parts[0];
        String name = parts[1];
        return GameRegistry.findBlock(modId, name);
    }
}
