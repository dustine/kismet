package dustine.kismet.server.command;

import dustine.kismet.Kismet;
import dustine.kismet.network.message.MessageSubtypeCount;
import dustine.kismet.util.StackHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public abstract class CCSuperStackEntry extends CommandComponent {
    private final String parent;
    private final EnumCommandType type;

    CCSuperStackEntry(String parent, EnumCommandType type) {
        super(parent);
        this.parent = parent;
        this.type = type;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            // player-centric version
            if (!(sender instanceof EntityPlayerMP)) {
                throw new CommandException("Sub-command (without more arguments) can only be run in-game");
            }
            final ItemStack item = ((EntityPlayerMP) sender).inventory.getCurrentItem();
            if (item == null || item.getItem() == null) {
                throw new CommandException("Selected hotbar slot is empty or contains an invalid item");
            }
            Kismet.network.sendTo(new MessageSubtypeCount(item, this.type), (EntityPlayerMP) sender);
        } else {
            String entry = String.join(":", (CharSequence[]) args);
            ItemStack stack = StackHelper.getItemStack(entry);
            if (stack == null) {
                throw new CommandException(String.format("Invalid key %s", entry));
            }
            String fixedEntry = StackHelper.toUniqueKey(stack, entry.split(":", 4).length > 2);

            processEntry(sender, fixedEntry);
        }
    }

    protected abstract void processEntry(ICommandSender sender, String entry);

    @Override public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
                                                          BlockPos pos) {
        return (args.length == 1 ?
                getListOfStringsMatchingLastWord(args, Item.REGISTRY.getKeys()) :
                Collections.<String>emptyList());
    }

    public enum EnumCommandType {
        BLOCK, PARDON, FORCE, UNFORCE
    }
}
