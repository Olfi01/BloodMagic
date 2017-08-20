package WayofTime.bloodmagic.item.soul;

import WayofTime.bloodmagic.BloodMagic;
import WayofTime.bloodmagic.api.Constants;
import WayofTime.bloodmagic.api.iface.IMultiWillTool;
import WayofTime.bloodmagic.api.iface.ISentientSwordEffectProvider;
import WayofTime.bloodmagic.api.iface.ISentientTool;
import WayofTime.bloodmagic.api.soul.EnumDemonWillType;
import WayofTime.bloodmagic.api.soul.IDemonWill;
import WayofTime.bloodmagic.api.soul.IDemonWillWeapon;
import WayofTime.bloodmagic.api.soul.PlayerDemonWillHandler;
import WayofTime.bloodmagic.api.util.helper.NBTHelper;
import WayofTime.bloodmagic.client.IMeshProvider;
import WayofTime.bloodmagic.client.mesh.CustomMeshDefinitionMultiWill;
import WayofTime.bloodmagic.core.RegistrarBloodMagicItems;
import WayofTime.bloodmagic.entity.mob.EntitySentientSpecter;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

public class ItemSentientAxe extends ItemAxe implements IDemonWillWeapon, IMeshProvider, IMultiWillTool, ISentientTool {
    public static int[] soulBracket = new int[]{16, 60, 200, 400, 1000};
    public static double[] defaultDamageAdded = new double[]{1, 2, 3, 3.5, 4};
    public static double[] destructiveDamageAdded = new double[]{2, 3, 4, 5, 6};
    public static double[] vengefulDamageAdded = new double[]{0, 0.5, 1, 1.5, 2};
    public static double[] steadfastDamageAdded = new double[]{0, 0.5, 1, 1.5, 2};
    public static double[] defaultDigSpeedAdded = new double[]{1, 1.5, 2, 3, 4};
    public static double[] soulDrainPerSwing = new double[]{0.05, 0.1, 0.2, 0.4, 0.75};
    public static double[] soulDrop = new double[]{2, 4, 7, 10, 13};
    public static double[] staticDrop = new double[]{1, 1, 2, 3, 3};

    public static double[] healthBonus = new double[]{0, 0, 0, 0, 0}; //TODO: Think of implementing this later
    public static double[] vengefulAttackSpeed = new double[]{-3, -2.8, -2.7, -2.6, -2.5};
    public static double[] destructiveAttackSpeed = new double[]{-3.1, -3.1, -3.2, -3.3, -3.3};

    public static int[] absorptionTime = new int[]{200, 300, 400, 500, 600};

    public static double maxAbsorptionHearts = 10;

    public static int[] poisonTime = new int[]{25, 50, 60, 80, 100};
    public static int[] poisonLevel = new int[]{0, 0, 0, 1, 1};

    public static double[] movementSpeed = new double[]{0.05, 0.1, 0.15, 0.2, 0.25};

    public final double baseAttackDamage = 8;
    public final double baseAttackSpeed = -3;

    public ItemSentientAxe() {
        super(Item.ToolMaterial.IRON);
        setMaxDamage(getMaxDamage() * 2);
//        super(ModItems.soulToolMaterial);

        setUnlocalizedName(BloodMagic.MODID + ".sentientAxe");
        setCreativeTab(BloodMagic.TAB_BM);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        float value = super.getDestroySpeed(stack, state);
        if (value > 1) {
            return (float) (value + getDigSpeedOfSword(stack));
        } else {
            return value;
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return RegistrarBloodMagicItems.ITEM_DEMON_CRYSTAL == repair.getItem() || super.getIsRepairable(toRepair, repair);
    }

    public void recalculatePowers(ItemStack stack, World world, EntityPlayer player) {
        EnumDemonWillType type = PlayerDemonWillHandler.getLargestWillType(player);
        double soulsRemaining = PlayerDemonWillHandler.getTotalDemonWill(type, player);
        this.setCurrentType(stack, soulsRemaining > 0 ? type : EnumDemonWillType.DEFAULT);
        int level = getLevel(stack, soulsRemaining);

        double drain = level >= 0 ? soulDrainPerSwing[level] : 0;
        double extraDamage = getExtraDamage(type, level);

        setDrainOfActivatedSword(stack, drain);
        setDamageOfActivatedSword(stack, baseAttackDamage + extraDamage);
        setStaticDropOfActivatedSword(stack, level >= 0 ? staticDrop[level] : 1);
        setDropOfActivatedSword(stack, level >= 0 ? soulDrop[level] : 0);
        setAttackSpeedOfSword(stack, level >= 0 ? getAttackSpeed(type, level) : baseAttackSpeed);
        setHealthBonusOfSword(stack, level >= 0 ? getHealthBonus(type, level) : 0);
        setSpeedOfSword(stack, level >= 0 ? getMovementSpeed(type, level) : 0);
        setDigSpeedOfSword(stack, level >= 0 ? getDigSpeed(type, level) : 0);
    }

    public double getExtraDamage(EnumDemonWillType type, int willBracket) {
        if (willBracket < 0) {
            return 0;
        }

        switch (type) {
            case CORROSIVE:
            case DEFAULT:
                return defaultDamageAdded[willBracket];
            case DESTRUCTIVE:
                return destructiveDamageAdded[willBracket];
            case VENGEFUL:
                return vengefulDamageAdded[willBracket];
            case STEADFAST:
                return steadfastDamageAdded[willBracket];
        }

        return 0;
    }

    public double getAttackSpeed(EnumDemonWillType type, int willBracket) {
        switch (type) {
            case VENGEFUL:
                return vengefulAttackSpeed[willBracket];
            case DESTRUCTIVE:
                return destructiveAttackSpeed[willBracket];
            default:
                return -2.9;
        }
    }

    public double getHealthBonus(EnumDemonWillType type, int willBracket) {
        switch (type) {
            case STEADFAST:
                return healthBonus[willBracket];
            default:
                return 0;
        }
    }

    public double getMovementSpeed(EnumDemonWillType type, int willBracket) {
        switch (type) {
            case VENGEFUL:
                return movementSpeed[willBracket];
            default:
                return 0;
        }
    }

    public double getDigSpeed(EnumDemonWillType type, int willBracket) {
        switch (type) {
            case VENGEFUL:
//            return movementSpeed[willBracket];
            default:
                return defaultDigSpeedAdded[willBracket];
        }
    }

    public void applyEffectToEntity(EnumDemonWillType type, int willBracket, EntityLivingBase target, EntityPlayer attacker) {
        switch (type) {
            case CORROSIVE:
                target.addPotionEffect(new PotionEffect(MobEffects.WITHER, poisonTime[willBracket], poisonLevel[willBracket]));
                break;
            case DEFAULT:
                break;
            case DESTRUCTIVE:
                break;
            case STEADFAST:
                if (!target.isEntityAlive()) {
                    float absorption = attacker.getAbsorptionAmount();
                    attacker.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, absorptionTime[willBracket], 127));
                    attacker.setAbsorptionAmount((float) Math.min(absorption + target.getMaxHealth() * 0.05f, maxAbsorptionHearts));
                }
                break;
            case VENGEFUL:
                break;
        }
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (super.hitEntity(stack, target, attacker)) {
            if (attacker instanceof EntityPlayer) {
                EntityPlayer attackerPlayer = (EntityPlayer) attacker;
                this.recalculatePowers(stack, attackerPlayer.getEntityWorld(), attackerPlayer);
                EnumDemonWillType type = this.getCurrentType(stack);
                double will = PlayerDemonWillHandler.getTotalDemonWill(type, attackerPlayer);
                int willBracket = this.getLevel(stack, will);

                applyEffectToEntity(type, willBracket, target, attackerPlayer);

                ItemStack offStack = attackerPlayer.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
                if (offStack.getItem() instanceof ISentientSwordEffectProvider) {
                    ISentientSwordEffectProvider provider = (ISentientSwordEffectProvider) offStack.getItem();
                    if (provider.providesEffectForWill(type)) {
                        provider.applyOnHitEffect(type, stack, offStack, attacker, target);
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public EnumDemonWillType getCurrentType(ItemStack stack) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        if (!tag.hasKey(Constants.NBT.WILL_TYPE)) {
            return EnumDemonWillType.DEFAULT;
        }

        return EnumDemonWillType.valueOf(tag.getString(Constants.NBT.WILL_TYPE).toUpperCase(Locale.ENGLISH));
    }

    public void setCurrentType(ItemStack stack, EnumDemonWillType type) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        tag.setString(Constants.NBT.WILL_TYPE, type.toString());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        recalculatePowers(player.getHeldItem(hand), world, player);

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    private int getLevel(ItemStack stack, double soulsRemaining) {
        int lvl = -1;
        for (int i = 0; i < soulBracket.length; i++) {
            if (soulsRemaining >= soulBracket[i]) {
                lvl = i;
            }
        }

        return lvl;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        if (!stack.hasTagCompound())
            return;

        tooltip.addAll(Arrays.asList(TextHelper.cutLongString(TextHelper.localizeEffect("tooltip.bloodmagic.sentientAxe.desc"))));
        tooltip.add(TextHelper.localizeEffect("tooltip.bloodmagic.currentType." + getCurrentType(stack).getName().toLowerCase()));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        recalculatePowers(stack, player.getEntityWorld(), player);

        double drain = this.getDrainOfActivatedSword(stack);
        if (drain > 0) {
            EnumDemonWillType type = getCurrentType(stack);
            double soulsRemaining = PlayerDemonWillHandler.getTotalDemonWill(type, player);

            if (drain > soulsRemaining) {
                return false;
            } else {
                PlayerDemonWillHandler.consumeDemonWill(type, player, drain);
            }
        }

        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemMeshDefinition getMeshDefinition() {
        return new CustomMeshDefinitionMultiWill("sentient_axe");
    }

    @Nullable
    @Override
    public ResourceLocation getCustomLocation() {
        return null;
    }

    @Override
    public List<String> getVariants() {
        List<String> ret = new ArrayList<String>();
        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            ret.add("type=" + type.getName().toLowerCase());
        }

        return ret;
    }

    @Override
    public List<ItemStack> getRandomDemonWillDrop(EntityLivingBase killedEntity, EntityLivingBase attackingEntity, ItemStack stack, int looting) {
        List<ItemStack> soulList = new ArrayList<ItemStack>();

        if (killedEntity.getEntityWorld().getDifficulty() != EnumDifficulty.PEACEFUL && !(killedEntity instanceof IMob)) {
            return soulList;
        }

        double willModifier = killedEntity instanceof EntitySlime ? 0.67 : 1;

        IDemonWill soul = ((IDemonWill) RegistrarBloodMagicItems.MONSTER_SOUL);

        EnumDemonWillType type = this.getCurrentType(stack);

        for (int i = 0; i <= looting; i++) {
            if (i == 0 || attackingEntity.getEntityWorld().rand.nextDouble() < 0.4) {
                ItemStack soulStack = soul.createWill(type.ordinal(), willModifier * (this.getDropOfActivatedSword(stack) * attackingEntity.getEntityWorld().rand.nextDouble() + this.getStaticDropOfActivatedSword(stack)) * killedEntity.getMaxHealth() / 20d);
                soulList.add(soulStack);
            }
        }

        return soulList;
    }

    //TODO: Change attack speed.
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();
        if (slot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getDamageOfActivatedSword(stack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.getAttackSpeedOfSword(stack), 0));
            multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier(new UUID(0, 31818145), "Weapon modifier", this.getHealthBonusOfSword(stack), 0));
            multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(new UUID(0, 4218052), "Weapon modifier", this.getSpeedOfSword(stack), 2));
        }

        return multimap;
    }

    public double getDamageOfActivatedSword(ItemStack stack) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();
        return tag.getDouble(Constants.NBT.SOUL_SWORD_DAMAGE);
    }

    public void setDamageOfActivatedSword(ItemStack stack, double damage) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        tag.setDouble(Constants.NBT.SOUL_SWORD_DAMAGE, damage);
    }

    public double getDrainOfActivatedSword(ItemStack stack) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();
        return tag.getDouble(Constants.NBT.SOUL_SWORD_ACTIVE_DRAIN);
    }

    public void setDrainOfActivatedSword(ItemStack stack, double drain) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        tag.setDouble(Constants.NBT.SOUL_SWORD_ACTIVE_DRAIN, drain);
    }

    public double getStaticDropOfActivatedSword(ItemStack stack) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();
        return tag.getDouble(Constants.NBT.SOUL_SWORD_STATIC_DROP);
    }

    public void setStaticDropOfActivatedSword(ItemStack stack, double drop) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        tag.setDouble(Constants.NBT.SOUL_SWORD_STATIC_DROP, drop);
    }

    public double getDropOfActivatedSword(ItemStack stack) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();
        return tag.getDouble(Constants.NBT.SOUL_SWORD_DROP);
    }

    public void setDropOfActivatedSword(ItemStack stack, double drop) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        tag.setDouble(Constants.NBT.SOUL_SWORD_DROP, drop);
    }

    public double getHealthBonusOfSword(ItemStack stack) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();
        return tag.getDouble(Constants.NBT.SOUL_SWORD_HEALTH);
    }

    public void setHealthBonusOfSword(ItemStack stack, double hp) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        tag.setDouble(Constants.NBT.SOUL_SWORD_HEALTH, hp);
    }

    public double getAttackSpeedOfSword(ItemStack stack) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();
        return tag.getDouble(Constants.NBT.SOUL_SWORD_ATTACK_SPEED);
    }

    public void setAttackSpeedOfSword(ItemStack stack, double speed) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        tag.setDouble(Constants.NBT.SOUL_SWORD_ATTACK_SPEED, speed);
    }

    public double getSpeedOfSword(ItemStack stack) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();
        return tag.getDouble(Constants.NBT.SOUL_SWORD_SPEED);
    }

    public void setSpeedOfSword(ItemStack stack, double speed) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        tag.setDouble(Constants.NBT.SOUL_SWORD_SPEED, speed);
    }

    public double getDigSpeedOfSword(ItemStack stack) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();
        return tag.getDouble(Constants.NBT.SOUL_SWORD_DIG_SPEED);
    }

    public void setDigSpeedOfSword(ItemStack stack, double speed) {
        NBTHelper.checkNBT(stack);

        NBTTagCompound tag = stack.getTagCompound();

        tag.setDouble(Constants.NBT.SOUL_SWORD_DIG_SPEED, speed);
    }

    @Override
    public boolean spawnSentientEntityOnDrop(ItemStack droppedStack, EntityPlayer player) {
        World world = player.getEntityWorld();
        if (!world.isRemote) {
            this.recalculatePowers(droppedStack, world, player);

            EnumDemonWillType type = this.getCurrentType(droppedStack);
            double soulsRemaining = PlayerDemonWillHandler.getTotalDemonWill(type, player);
            if (soulsRemaining < 1024) {
                return false;
            }

            PlayerDemonWillHandler.consumeDemonWill(type, player, 100);

            EntitySentientSpecter specterEntity = new EntitySentientSpecter(world);
            specterEntity.setPosition(player.posX, player.posY, player.posZ);
            world.spawnEntity(specterEntity);

            specterEntity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, droppedStack.copy());

            specterEntity.setType(this.getCurrentType(droppedStack));
            specterEntity.setOwner(player);
            specterEntity.setTamed(true);

            return true;
        }

        return false;
    }
}
