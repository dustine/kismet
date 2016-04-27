package dustine.kismet.server.command;

import dustine.kismet.ConfigKismet;
import dustine.kismet.Log;
import dustine.kismet.target.InformedStack;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.target.TargetLibraryBuilder;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class CCDump implements ICommandComponent {
    private final String commandName;

    CCDump(String commandName) {
        this.commandName = commandName;
    }

    private enum EnumLists implements IStringSerializable {
        SAVEDATA, DATABASE, LIBRARY, FORCED, BLACKLIST, HIDDEN;

        public String getName() {
            return this.name().toLowerCase();
        }
    }

    @Override
    public String getComponentName() {
        return "dump";
    }

    @Override
    public String getParentName() {
        return this.commandName;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException {
        if (args.length < 1) {
            throw new WrongUsageException(getParentName() + '.' + getComponentName());
        }

        EnumLists list;
        try {
            list = EnumLists.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new WrongUsageException(getParentName() + '.' + getComponentName());
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
                dump = getSortedInformedStacks(targetDatabase.getStacks());
                break;
            case DATABASE:
                dump = getSortedInformedStacks(
                        new ArrayList<>(TargetLibraryBuilder.getConfigStacks(targetDatabase.getStacks()).values()));
                break;
            case LIBRARY:
                dump = getSortedInformedStacks(TargetLibrary.getLibrary());
                break;
            case FORCED:
                dump = new ArrayList<>(ConfigKismet.getForceAdd()).stream()
                        .sorted().collect(Collectors.toList());
                break;
            case BLACKLIST:
                dump = new ArrayList<>(ConfigKismet.getHiddenBlacklist()).stream()
                        .map(s -> "hidden:" + s)
                        .collect(Collectors.toList());
                dump.addAll(ConfigKismet.getGenBlacklist());
                dump = dump.stream()
                        .sorted().collect(Collectors.toList());
                break;
            case HIDDEN:
                dump = new ArrayList<>(ConfigKismet.getHiddenGen()).stream().sorted().collect(Collectors.toList());
                break;
            default:
                Log.warning("Dump command using default list");
                dump = new ArrayList<String>() {
                };
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

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return Arrays.asList(EnumLists.values()).stream().map(EnumLists::getName).collect(Collectors.toList());
    }


}
