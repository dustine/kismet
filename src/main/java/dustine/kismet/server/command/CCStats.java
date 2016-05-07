package dustine.kismet.server.command;

import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.Target;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.Collection;
import java.util.List;

public class CCStats extends CommandComponent {
    public CCStats(final String parent) {
        super(parent);
    }

    @Override
    public String getCommandName() {
        return "stats";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender,
                        final String[] args) throws WrongUsageException {
        final WSDTargetDatabase targetDatabase = WSDTargetDatabase.get(sender.getEntityWorld());
        CommandKismet.send(sender, new TextComponentString("Printing library stats: Savedata, Database, Library"));
        final Collection<Target> configStacks = targetDatabase.getDatabase().values();

        // types
        for (final EnumOrigin type : EnumOrigin.getSorted(true)) {
            CommandKismet.sendLine(sender, String.format("[§b%s§r]: %d, %d, %d", type,
                    targetDatabase.getSavedata().values().stream().filter(s -> s.hasOrigin(type)).count(),
                    configStacks.stream().filter(s -> s.hasOrigin(type)).count(),
                    TargetLibrary.getLibrary().stream().filter(s -> s.hasOrigin(type)).count()
            ));
        }

        // empty types
        CommandKismet.sendLine(sender, String.format("[#0]: %d, %d, %d",
                targetDatabase.getSavedata().values().stream().filter(s -> s.getOrigins().size() == 0).count(),
                configStacks.stream().filter(s -> s.getOrigins().size() == 0).count(),
                TargetLibrary.getLibrary().stream().filter(s -> s.getOrigins().size() == 0).count()
        ));

        // obtainable
        CommandKismet.sendLine(sender, String.format("§d%s§r: %d, %d, %d", "Obtainable",
                targetDatabase.getSavedata().values().stream().filter(Target::isObtainable).count(),
                configStacks.stream().filter(Target::isObtainable).count(),
                TargetLibrary.getLibrary().stream().filter(Target::isObtainable).count()
        ));

        // total
        CommandKismet.sendLine(sender, String.format("§d%s§r: %d, %d, %d", "Total",
                targetDatabase.getSavedata().size(),
                configStacks.size(),
                TargetLibrary.getLibrary().size()
        ));
    }

    @Override
    public List<String> getTabCompletionOptions(final MinecraftServer server, final ICommandSender sender,
                                                final String[] args,
                                                final BlockPos pos) {
        return null;
    }
}
