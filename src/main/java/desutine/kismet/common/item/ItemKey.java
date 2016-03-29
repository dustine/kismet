package desutine.kismet.common.item;

import desutine.kismet.common.init.ModBlocks;
import desutine.kismet.reference.Names;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemKey extends ItemKismet {
    public ItemKey() {
        super();
        this.setUnlocalizedName(Names.Items.KEY);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(worldIn.getBlockState(pos).getBlock().isAssociatedBlock(ModBlocks.DISPLAY)){
            stack.stackSize--;
        }
        return EnumActionResult.PASS;
    }
}
