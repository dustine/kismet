package dustine.kismet.server.command;

import dustine.kismet.Kismet;
import dustine.kismet.target.TargetLibraryBuilder;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CCRefresh implements ICommandComponent {
    private final String parentName;

    public CCRefresh(String parentName) {
        this.parentName = parentName;
    }

    @Override
    public String getComponentName() {
        return "refresh";
    }

    @Override
    public String getParentName() {
        return parentName;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException {
        final WSDTargetDatabase targetDatabase = WSDTargetDatabase.get(sender.getEntityWorld());
        if (Kismet.databaseBuilder != null) {
            CommandKismet.send(sender, "Refreshing target database...");
            TargetLibraryBuilder.build(targetDatabase);
        } else {
            CommandKismet.send(sender, "Error, target database not found");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }
}
