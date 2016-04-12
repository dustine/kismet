package desutine.kismet.server;

import desutine.kismet.ModLogger;
import desutine.kismet.Reference;
import desutine.kismet.util.StackHelper;
import desutine.kismet.util.TargetLibraryFactory;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CommandKismet extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return super.checkPermission(server, sender);
    }

    @Override
    public String getCommandName() {
        return Reference.MOD_ID;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return getListOfStringsMatchingLastWord(args, "rebuild", "dump");
    }


    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.kismet.usage";
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || "help".equals(args[0])) {
            throw new WrongUsageException(getCommandName());
        } else if ("rebuild".equals(args[0])) {
            // regen command
            TargetLibraryFactory.generateStacks((EntityPlayerMP) sender);
        } else if ("dump".equals(args[0])) {
            for (StackWrapper wrapper : WorldSavedDataTargets.get(sender.getEntityWorld()).getStacks()) {
                ModLogger.info(StackHelper.toUniqueKey(wrapper.getStack()) + " = " + wrapper.isObtainable());
            }
        } else {
            throw new WrongUsageException(getCommandName());
        }
    }


}
