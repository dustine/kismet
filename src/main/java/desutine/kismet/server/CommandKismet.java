package desutine.kismet.server;

import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.Reference;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

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
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return getListOfStringsMatchingLastWord(args, "reset", "dump");
    }

    @Override
    public String getCommandName() {
        return Reference.MOD_ID;
    }




    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.kismet.usage";
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || "help".equals(args[0])) {
            throw new WrongUsageException(getCommandName());
        } else if ("reset".equals(args[0])) {
            // regen command
            if (Kismet.libraryFactory != null)
                Kismet.libraryFactory.generateStacks((EntityPlayerMP) sender);
            else {
                sender.addChatMessage(new TextComponentString("[Kismet] Error, target library factory not found"));
            }
        } else if ("dump".equals(args[0])) {
            for (StackWrapper wrapper : WorldSavedDataTargets.get(sender.getEntityWorld()).getStacks()) {
                ModLogger.info(wrapper.toCompleteString());
            }
        } else {
            throw new WrongUsageException(getCommandName());
        }
    }


}
