package desutine.kismet.proxy;

import desutine.kismet.common.config.ConfigKismet;
import desutine.kismet.common.init.ModItems;
import desutine.kismet.common.tile.TileDisplay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy extends CommonProxy {
    @Override
    public void addInventoryModels() {
        // NOOP
    }

    @Override
    public void initConfig() {
        ConfigKismet.preInit();
    }

    @Override
    public void registerTESR() {
        // NOOP
    }

    @Override
    public void registerBlockItemColor() {
        // NOOP
    }

    @Override
    public boolean onDisplayBlockSideActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, TileDisplay te) {
        if(te == null) return false;

        // If right-clicked with the key in any hand, regen the item
        if(heldItem != null && heldItem.isItemEqual(new ItemStack(ModItems.itemKey))){ // key = free regen
            te.getNewTarget();
            return true;
        }

        return false;
    }
}
