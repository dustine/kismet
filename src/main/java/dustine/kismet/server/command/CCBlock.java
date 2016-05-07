package dustine.kismet.server.command;

import dustine.kismet.Kismet;
import dustine.kismet.config.ConfigKismet;
import dustine.kismet.network.message.MessageSubtypeCount;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.util.StackHelper;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public class CCBlock extends CommandComponent {
    public CCBlock(String parent) {
        super(parent);
    }

    @Override public String getCommandName() {
        return "block";
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
            Kismet.network.sendTo(new MessageSubtypeCount(item, MessageSubtypeCount.MessageCommandStackType.BLOCK),
                    (EntityPlayerMP) sender);
        } else {
            String entry = String.join(":", (CharSequence[]) args);
            ItemStack stack = StackHelper.getItemStack(entry);
            if (stack == null) {
                throw new CommandException(String.format("Invalid key %s", entry));
            }

            // standardize entry and add it
            String fixedEntry = StackHelper.toUniqueKey(stack, entry.split(":", 4).length > 2);
            ConfigKismet.addToGenFilter(fixedEntry);
            TargetLibrary.build(WSDTargetDatabase.get(sender.getEntityWorld()));
            CommandKismet.send(sender,
                    new TextComponentString(String.format("Added %s to gen target filter", fixedEntry))
            );
        }

    }

    @Override public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
                                                          BlockPos pos) {
        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
