package dustine.kismet.server.command;

import dustine.kismet.Kismet;
import dustine.kismet.target.TargetLibraryBuilder;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CCRefresh extends CommandComponent {

    public CCRefresh(String parent) {
        super(parent);
    }

    @Override
    public String getCommandName() {
        return "refresh";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        final WSDTargetDatabase targetDatabase = WSDTargetDatabase.get(sender.getEntityWorld());
        if (Kismet.databaseBuilder != null) {
            TargetLibraryBuilder.build(targetDatabase);
            CommandKismet.send(sender, "Refreshing target library...");
            CommandKismet.send(sender, "Done! Target library has been refreshed.");
        } else {
            CommandKismet.error("Target database not found");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
                                                BlockPos pos) {
        return null;
    }
}
