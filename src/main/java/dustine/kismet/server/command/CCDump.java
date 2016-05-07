package dustine.kismet.server.command;

import dustine.kismet.Log;
import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.Target;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.target.TargetPatcher;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class CCDump extends CommandComponent {

    CCDump(final String parent) {
        super(parent);
    }

    @Override
    public List<String> getTabCompletionOptions(final MinecraftServer server, final ICommandSender sender,
                                                final String[] args,
                                                final BlockPos pos) {
        return Arrays.asList(EnumLists.values()).stream().map(EnumLists::getName).collect(Collectors.toList());
    }

    private enum EnumLists implements IStringSerializable {
        SAVEDATA, DATABASE, LIBRARY, FORCED, BLACKLIST, OVERRIDE;

        public String getName() {
            return this.name().toLowerCase();
        }
    }

    @Override public String getCommandName() {
        return "dump";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender,
                        final String[] args) throws WrongUsageException {
        if (args.length < 1) {
            throw new WrongUsageException(getParentName() + '.' + getCommandName());
        }

        final EnumLists list;
        try {
            list = EnumLists.valueOf(args[0].toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new WrongUsageException(getParentName() + '.' + getCommandName());
        }

        final Collection<String> dump;
        dump = getStringDump(sender, list);

        dump.stream().forEachOrdered(s -> CommandKismet.sendLine(sender, s));
    }

    private Collection<String> getStringDump(final ICommandSender sender, final EnumLists list) {
        Collection<String> dump;
        final WSDTargetDatabase targetDatabase = WSDTargetDatabase.get(sender.getEntityWorld());
        switch (list) {
            case SAVEDATA:
                dump = getSortedInformedStacks(new ArrayList<>(targetDatabase.getSavedata().values()));
                break;
            case DATABASE:
                dump = getSortedInformedStacks(new ArrayList<>(targetDatabase.getDatabase().values()));
                break;
            case LIBRARY:
                dump = getSortedInformedStacks(TargetLibrary.getLibrary());
                break;
            case BLACKLIST:
                dump = new ArrayList<>();
                for (final EnumOrigin origin : EnumOrigin.values()) {
                    final Set<String> overrides = TargetPatcher.getBlacklist(origin);
                    dump.addAll(overrides.stream()
                            .map(s -> String.format("%s:%s", origin, s))
                            .collect(Collectors.toList()));
                }
                dump = dump.stream().sorted().collect(Collectors.toList());
                break;
            case OVERRIDE:
                dump = new ArrayList<>();
                for (final EnumOrigin origin : EnumOrigin.values()) {
                    final Set<String> overrides = TargetPatcher.getOverrides(origin);
                    dump.addAll(overrides.stream()
                            .map(s -> String.format("%s:%s", origin, s))
                            .collect(Collectors.toList()));
                }
                dump = dump.stream().sorted().collect(Collectors.toList());
                break;
            default:
                Log.warning("Dump COMMAND using default list");
                dump = new ArrayList<String>() {};
                break;
        }
        return dump;
    }

    private List<String> getSortedInformedStacks(final List<Target> targets) {
        return targets.stream()
                .sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
                .map(s -> String.format("%s %s", s.getOrigins(), s))
                .collect(Collectors.toList());
    }


}
