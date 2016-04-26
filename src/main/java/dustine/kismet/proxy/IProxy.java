package dustine.kismet.proxy;

import dustine.kismet.block.BlockKismet;
import dustine.kismet.item.ItemKismet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public interface IProxy {
    /**
     * Initializes ConfigKismet (has some client side only code)
     */
    void initConfig();

    /**
     * Registers Tile Entity Special Renderer instances in client side only
     */
    void registerTESR();

    /**
     * Registers IItemColor and IBlockColor instances on client side only
     */
    void registerColorHandlers();

    /**
     * Sends the current server config to player
     *
     * @param player The player that will receive the config message
     */
    void sendConfigToClient(EntityPlayerMP player);

    /**
     * Cleans the target library on embedded server side only
     *
     * @param player The player instance with the outdated target library
     */
    void cleanTargetLibrary(EntityPlayerMP player);

    /**
     * Returns a side-safe value for getHasSubtypes.
     * <p>
     * ItemStack.getHasSubtypes doesn't have the same value on client and server: when an Item has variants that differ
     * by NBT data alone, it'll return false on dedicated server and, in most cases(1), true on client and embedded
     * servers.
     * <p>
     * Because most behaviour depending on getHasSubtypes is involved with filtering, as in agglomerating itemStacks
     * with different metadata and/or NBT data if these don't have subtypes, on environments that the value could be
     * erroneous we return true to avoid collisions on the server. It doesn't solve the desync but prevents data from
     * being lost on server operations.
     * <p>
     * (1) minecraft:spawn_egg still retrieves false on client, and it has subtypes. This seems to be the only vanilla
     * use-case. It's not a case for desync but it's still an erroneous value.
     *
     * @param stack Tested item stack
     * @return true on dedicated server, stack.getHasSubtypes() elsewhere
     */
    boolean sideSafeHasSubtypes(ItemStack stack);

    /**
     * Registers an item inventory model for the block in the client side
     *
     * @param block The block to register the item model
     */
    void registerInventoryModel(BlockKismet block);

    /**
     * Registers an item inventory model for the item in the client side
     *
     * @param item The item to register the item model
     */
    void registerInventoryModel(ItemKismet item);

    /**
     * Tries to get an EntityPlayerSP from Minecraft.getMinecraft() and place it on player As evident, this only happens
     * on the client side
     *
     * @return true if successful
     */
    EntityPlayer tryGetEntityPlayerSP();
}
