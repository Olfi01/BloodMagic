package WayofTime.alchemicalWizardry.common.harvest;

import WayofTime.alchemicalWizardry.api.harvest.IHarvestHandler;
import WayofTime.bloodmagic.ritual.harvest.HarvestRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import java.util.List;

public class MagicalCropsHandler implements IHarvestHandler
{
    private static String[] cropNames = 
    {
        "Air", "Coal", "Dye", "Earth", "Fire", "Minicio", "Nature", "Water",
        "Redstone", "Glowstone", "Obsidian", "Nether", "Iron", 
        "Gold", "Lapis", "Experience", "Quartz", 
        "Diamond", "Emerald", 
        "Blaze", "Creeper", "Enderman", "Ghast", "Skeleton", "Slime", "Spider", "Wither",
        "Chicken", "Cow", "Pig", "Sheep",
        "Aluminum", "Ardite", "Cobalt", "Copper", "Peridot", "Ruby", "Sapphire", 
        "Rubber", "Tin", "Sulfur", "Alumite", "Bronze", "Manasteel", "Manyullyn", "Saltpeter", "Steel", "Terrasteel"
    };
    
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
    
    public static void registerCropsHandlers()
    {
        for (String name : cropNames)
        {
            String id = "magicalcrops:magicalcrops_" + name + "Crop";
            HarvestRegistry.registerHarvestHandler(new MagicalCropsHandler(id, 7));
        }
    }
}
