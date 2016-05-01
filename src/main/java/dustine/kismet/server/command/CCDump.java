package dustine.kismet.server.command;

import dustine.kismet.Log;
import dustine.kismet.target.*;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

class CCDump extends CommandComponent {

    CCDump(String parent) {
        super(parent);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
                                                BlockPos pos) {
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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException {
        if (args.length < 1) {
            throw new WrongUsageException(getParentName() + '.' + getCommandName());
        }

        EnumLists list;
        try {
            list = EnumLists.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new WrongUsageException(getParentName() + '.' + getCommandName());
        }

        Collection<String> dump;
        dump = getStringDump(sender, list);

        dump.stream().forEachOrdered(s -> CommandKismet.sendLine(sender, s));
    }

    private Collection<String> getStringDump(ICommandSender sender, EnumLists list) {
        Collection<String> dump;
        final WSDTargetDatabase targetDatabase = WSDTargetDatabase.get(sender.getEntityWorld());
        switch (list) {
            case SAVEDATA:
                dump = getSortedInformedStacks(targetDatabase.getDatabase());
                break;
            case DATABASE:
                dump = getSortedInformedStacks(
                        new ArrayList<>(TargetLibraryBuilder.getConfigStacks(targetDatabase.getDatabase()).values()));
                break;
            case LIBRARY:
                dump = getSortedInformedStacks(TargetLibrary.getLibrary());
                break;
            case BLACKLIST:
                dump = new ArrayList<>();
                for (EnumOrigin origin : EnumOrigin.values()) {
                    final Set<String> overrides = TargetPatcher.getBlacklist(origin);
                    dump.addAll(overrides.stream()
                            .map(s -> String.format("%s:%s", origin, s))
                            .collect(Collectors.toList()));
                }
                dump = dump.stream().sorted().collect(Collectors.toList());
                break;
            case OVERRIDE:
                dump = new ArrayList<>();
                for (EnumOrigin origin : EnumOrigin.values()) {
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

    private List<String> getSortedInformedStacks(List<InformedStack> stacks) {
        return stacks.stream()
                .sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
                .map(s -> String.format("%s %s", s.getOrigins(), s))
                .collect(Collectors.toList());
    }


}
