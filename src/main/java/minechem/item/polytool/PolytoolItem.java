package minechem.item.polytool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import minechem.Minechem;
import minechem.gui.CreativeTabMinechem;
import minechem.gui.GuiHandler;
import minechem.item.element.ElementEnum;
import minechem.item.element.ElementItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PolytoolItem extends ItemPickaxe
{

    public static PolytoolItem instance;

    public PolytoolItem()
    {
        super(ToolMaterial.IRON);
        instance = this;
        setCreativeTab(CreativeTabMinechem.CREATIVE_TAB_ITEMS);
        setUnlocalizedName("Polytool");
    }

    public static boolean validAlloyInfusion(ItemStack polytool, ItemStack element)
    {
        if (element.getItem() instanceof ElementItem)
        {
            PolytoolUpgradeType upgrade = PolytoolHelper.getTypeFromElement((ElementItem.getElement(element)), 1);
            if (upgrade instanceof PolytoolTypeAlloy)
            {
                ItemStack toApply = polytool.copy();
                addTypeToNBT(toApply, upgrade);
                if (!(instance.getSwordStr(toApply) > 0 && instance.getPickaxeStr(toApply) > 0 && instance.getStoneStr(toApply) > 0 && instance.getAxeStr(toApply) > 0 && instance.getShovelStr(toApply) > 0))
                {
                    return false;

                }
            }
        }
        return true;

    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack par1ItemStack, World world, EntityPlayer entityPlayer, EnumHand hand)
    {
        // Copied from journal code
        // I don't know why chunkCoordX is used
        // But LJDP probably knows, and he is smarter than me
        entityPlayer.openGui(Minechem.INSTANCE, GuiHandler.GUI_ID_POLYTOOL, world, entityPlayer.chunkCoordX, entityPlayer.chunkCoordY, entityPlayer.chunkCoordY);
        return new ActionResult<ItemStack>(EnumActionResult.PASS, par1ItemStack);
    }

    public float getSwordStr(ItemStack stack)
    {
        float result = 8;
        for (Iterator<PolytoolTypeAlloy> itr = getAlloyUpgrades(stack).iterator(); itr.hasNext(); result += itr.next().getStrSword());
        return result;
    }

    public float getPickaxeStr(ItemStack stack)
    {
        float result = 8;
        for (Iterator<PolytoolTypeAlloy> itr = getAlloyUpgrades(stack).iterator(); itr.hasNext(); result += itr.next().getStrOre());
        return result;
    }

    public float getStoneStr(ItemStack stack)
    {
        float result = 8;
        for (Iterator<PolytoolTypeAlloy> itr = getAlloyUpgrades(stack).iterator(); itr.hasNext(); result += itr.next().getStrStone());
        return result;
    }

    public float getAxeStr(ItemStack stack)
    {
        float result = 8;
        for (Iterator<PolytoolTypeAlloy> itr = getAlloyUpgrades(stack).iterator(); itr.hasNext(); result += itr.next().getStrAxe());
        return result;
    }

    public float getShovelStr(ItemStack stack)
    {
        float result = 8;
        for (Iterator<PolytoolTypeAlloy> itr = getAlloyUpgrades(stack).iterator(); itr.hasNext(); result += itr.next().getStrShovel());
        return result;
    }

    public static ArrayList<PolytoolUpgradeType> getUpgrades(ItemStack stack)
    {
        ensureNBT(stack);
        ArrayList<PolytoolUpgradeType> toReturn = new ArrayList<PolytoolUpgradeType>();
        NBTTagList list = stack.getTagCompound().getTagList("Upgrades", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            toReturn.add(PolytoolHelper.getTypeFromElement(ElementEnum.getByID(nbt.getInteger("Element")), nbt.getFloat("Power")));
        }
        return toReturn;
    }

    public static ArrayList<PolytoolTypeAlloy> getAlloyUpgrades(ItemStack stack)
    {
        ArrayList<PolytoolTypeAlloy> result = new ArrayList<PolytoolTypeAlloy>();
        for (PolytoolUpgradeType type : getUpgrades(stack))
        {
            if (type instanceof PolytoolTypeAlloy) result.add((PolytoolTypeAlloy)type);
        }
        return result;
    }

    @Override
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLivingBase, EntityLivingBase par3EntityLivingBase)
    {
        par2EntityLivingBase.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)par3EntityLivingBase), getSwordStr(par1ItemStack));

        ArrayList upgrades = getUpgrades(par1ItemStack);
        Iterator iter = upgrades.iterator();
        while (iter.hasNext())
        {
            ((PolytoolUpgradeType)iter.next()).hitEntity(par1ItemStack, par2EntityLivingBase, par3EntityLivingBase);
        }
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack p_150894_1_, World p_150894_2_, IBlockState state, BlockPos pos, EntityLivingBase p_150894_7_)
    {
        ArrayList upgrades = getUpgrades(p_150894_1_);
        Iterator iter = upgrades.iterator();
        while (iter.hasNext())
        {
            ((PolytoolUpgradeType)iter.next()).onBlockDestroyed(p_150894_1_, p_150894_2_, state.getBlock(), pos.getX(), pos.getY(), pos.getZ(), p_150894_7_);
        }
        return true;
    }

    public static void addTypeToNBT(ItemStack stack, PolytoolUpgradeType type)
    {
        ensureNBT(stack);
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList list = stack.getTagCompound().getTagList("Upgrades", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++)
        {
            if (list.getCompoundTagAt(i).getInteger("Element") == type.getElement().atomicNumber())
            {
                list.getCompoundTagAt(i).setFloat("Power", list.getCompoundTagAt(i).getFloat("Power") + type.power);

                return;
            }
        }
        compound.setFloat("Power", type.power);
        compound.setInteger("Element", type.getElement().atomicNumber());
        list.appendTag(compound);

    }

    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
    {
        ArrayList upgrades = getUpgrades(par1ItemStack);
        Iterator iter = upgrades.iterator();
        while (iter.hasNext())
        {
            ((PolytoolUpgradeType)iter.next()).onTickFull(par1ItemStack, par2World, par3Entity, par4, par5);
        }
    }

    public static void ensureNBT(ItemStack item)
    {
        if (item.getTagCompound() == null)
        {
            item.setTagCompound(new NBTTagCompound());
        }
        if (!item.getTagCompound().hasKey("Upgrades"))
        {
            item.getTagCompound().setTag("Upgrades", new NBTTagList());
        }
        if (!item.getTagCompound().hasKey("Energy"))
        {
            item.getTagCompound().setInteger("Energy", 0);
        }
    }

    public static float getPowerOfType(ItemStack item, ElementEnum element)
    {
        ArrayList upgrades = getUpgrades(item);
        Iterator iter = upgrades.iterator();
        while (iter.hasNext())
        {
            PolytoolUpgradeType next = (PolytoolUpgradeType)iter.next();
            if (next.getElement().atomicNumber() == element.atomicNumber())
            {
                return next.power;
            }
        }
        return 0;
    }

    @Override
    public float getStrVsBlock(ItemStack stack, IBlockState state)
    {
        float result = 8F;
        for (Iterator<PolytoolUpgradeType> itr = getUpgrades(stack).iterator(); itr.hasNext(); result += itr.next().getStrVsBlock(stack, state.getBlock(), state.getBlock().getMetaFromState(state)));
        return result;
    }

    @Override
    public Multimap getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
    {
        Multimap multimap = HashMultimap.create();
        multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getSwordStr(stack), 0));
        return multimap;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
    {
        super.addInformation(par1ItemStack, par2EntityPlayer, list, par4);
    }

}
