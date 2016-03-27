package desutine.kismet.item;

import desutine.kismet.reference.Blocks;
import desutine.kismet.reference.Names;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class KeyItem extends ModItem {
  public KeyItem() {
    super();
    this.setUnlocalizedName(Names.KEY);
  }

  @Override
  public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (worldIn.getBlockState(pos).getBlock().isAssociatedBlock(Blocks.kismetDisplayBlock)) {
      stack.stackSize--;
    }
    return EnumActionResult.PASS;
  }
}
