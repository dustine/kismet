package dustine.kismet.server.command;

import dustine.kismet.Kismet;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CCRefresh extends CommandComponent {

    public CCRefresh(final String parent) {
        super(parent);
    }

    @Override
    public String getCommandName() {
        return "refresh";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender,
                        final String[] args) throws CommandException {
        final WSDTargetDatabase targetDatabase = WSDTargetDatabase.get(sender.getEntityWorld());
        if (Kismet.databaseBuilder != null) {
            TargetLibrary.build(targetDatabase);
            CommandKismet.send(sender, "Refreshing target library...");
            CommandKismet.send(sender, "Done! Target library has been refreshed.");
        } else {
            CommandKismet.error("Target database not found");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(final MinecraftServer server, final ICommandSender sender,
                                                final String[] args,
                                                final BlockPos pos) {
        return null;
    }
}
