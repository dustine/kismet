package dustine.kismet.server.command;

import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.InformedStack;
import dustine.kismet.target.library.TargetLibrary;
import dustine.kismet.target.library.TargetLibraryBuilder;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.List;

public class CCStats implements ICommandComponent {
    private final String parentName;

    public CCStats(String parentName) {
        this.parentName = parentName;
    }

    @Override
    public String getComponentName() {
        return "stats";
    }

    @Override
    public String getParentName() {
        return parentName;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException {
        final WSDTargetDatabase targetDatabase = WSDTargetDatabase.get(sender.getEntityWorld());
        CommandKismet.send(sender, "Printing library stats: Savedata, Database, Library");
        final Collection<InformedStack> configStacks = TargetLibraryBuilder.getConfigStacks(targetDatabase.getStacks()).values();

        // types
        for (EnumOrigin type : EnumOrigin.values()) {
            CommandKismet.sendLine(sender, String.format("[§b%s§r]: %d, %d, %d", type,
                    targetDatabase.getStacks().stream().filter(s -> s.hasOrigin(type)).count(),
                    configStacks.stream().filter(s -> s.hasOrigin(type)).count(),
                    TargetLibrary.getLibrary().stream().filter(s -> s.hasOrigin(type)).count()
            ));
        }

        // empty types
        CommandKismet.sendLine(sender, String.format("[#0]: %d, %d, %d",
                targetDatabase.getStacks().stream().filter(s -> s.getOrigins().size() == 0).count(),
                configStacks.stream().filter(s -> s.getOrigins().size() == 0).count(),
                TargetLibrary.getLibrary().stream().filter(s -> s.getOrigins().size() == 0).count()
        ));

        // obtainable
        CommandKismet.sendLine(sender, String.format("§d%s§r: %d, %d, %d", "Obtainable",
                targetDatabase.getStacks().stream().filter(InformedStack::isObtainable).count(),
                configStacks.stream().filter(InformedStack::isObtainable).count(),
                TargetLibrary.getLibrary().stream().filter(InformedStack::isObtainable).count()
        ));

        // total
        CommandKismet.sendLine(sender, String.format("§d%s§r: %d, %d, %d", "Total",
                targetDatabase.getStacks().size(),
                configStacks.size(),
                TargetLibrary.getLibrary().size()
        ));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }
}
