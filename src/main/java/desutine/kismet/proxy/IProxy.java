package desutine.kismet.proxy;

import desutine.kismet.common.tile.TileDisplay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IProxy {
    void addInventoryModels();

    void initConfig();

    void registerTESR();

    void registerBlockItemColor();
}
