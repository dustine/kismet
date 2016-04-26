package dustine.kismet.server;

import dustine.kismet.Kismet;
import dustine.kismet.ModLogger;
import dustine.kismet.Reference;
import dustine.kismet.target.InformedStack;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.target.TargetLibraryBuilder;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.*;

public class CommandKismet extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        ModLogger.info(Arrays.toString(args));
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reset", "library", "blacklist", "whitelist", "dump");
        } else if ("library".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, "stats", "refresh");
        }
        return Collections.emptyList();
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
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(getCommandName());
        } else {
            WSDTargetDatabase targets;
            Collection<InformedStack> configStacks;

            switch (args[0]) {
                case "dump":
                    targets = WSDTargetDatabase.get(sender.getEntityWorld());
                    configStacks = TargetLibraryBuilder.getConfigStacks(targets
                            .getStacks()).values();

                    StringBuilder output = new StringBuilder();

                    configStacks.stream().sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
                            .forEachOrdered(s -> {
                                List<String> loc = new ArrayList<>();
                                if (TargetLibrary.getLibrary().contains(s)) loc.add("L");
                                if (targets.getStacks().contains(s)) loc.add("SD");
                                output.append(String.format("\n%s %s", loc, s.toCompleteString()));
                            });

                    ModLogger.info(output.toString());
                    return;
                case "reset":
                    if (!(sender instanceof EntityPlayerMP)) {
                        sender.addChatMessage(new TextComponentString(String.format("[Kismet] Error, command \"%s\" " +
                                "can only be run " +
                                "in-game", args[0])));
                        return;
                    }
                    if (Kismet.libraryFactory != null) {
                        Kismet.libraryFactory.generateStacks((EntityPlayerMP) sender);
                    } else sender.addChatMessage(
                            new TextComponentString("[Kismet] Error, target database factory not found"));
                    return;
                case "library":
                    if (args.length == 1) {
                        throw new WrongUsageException(getCommandName());
                    }
                    if ("stats".equalsIgnoreCase(args[1])) {
                        targets = WSDTargetDatabase.get(sender.getEntityWorld());

                        sender.addChatMessage(new TextComponentString("[Kismet] Printing library stats: Database, " +
                                "Runtime, Library"));

                        configStacks = TargetLibraryBuilder.getConfigStacks(targets
                                .getStacks()).values();

                        // types
                        for (InformedStack.ObtainableTypes type : InformedStack.ObtainableTypes.values()) {
                            sender.addChatMessage(new TextComponentString(String.format("[§b%s§r]: %d, %d, %d", type,
                                    targets.getStacks().stream().filter(s -> s.isObtainable(type)).count(),
                                    configStacks.stream().filter(s -> s.isObtainable(type)).count(),
                                    TargetLibrary.getLibrary().stream().filter(s -> s.isObtainable(type)).count()
                            )));
                        }

                        // empty types
                        sender.addChatMessage(new TextComponentString(String.format("[#0]: %d, %d, %d",
                                targets.getStacks().stream().filter(s -> s.getObtainable().size() == 0).count(),
                                configStacks.stream().filter(s -> s.getObtainable().size() == 0).count(),
                                TargetLibrary.getLibrary().stream().filter(s -> s.getObtainable().size() == 0).count()
                        )));

                        // obtainable
                        sender.addChatMessage(new TextComponentString(String.format("§d%s§r: %d, %d, %d", "Obtainable",
                                targets.getStacks().stream().filter(InformedStack::isObtainable).count(),
                                configStacks.stream().filter(InformedStack::isObtainable).count(),
                                TargetLibrary.getLibrary().stream().filter(InformedStack::isObtainable).count()
                        )));

                        // total
                        sender.addChatMessage(new TextComponentString(String.format("§d%s§r: %d, %d, %d", "Total",
                                targets.getStacks().size(),
                                configStacks.size(),
                                TargetLibrary.getLibrary().size()
                        )));
                    } else if ("refresh".equalsIgnoreCase(args[1])) {
                        if (Kismet.libraryFactory != null) {
                            sender.addChatMessage(new TextComponentString("[Kismet] Refreshing target database..."));
                            Kismet.libraryFactory.recreateLibrary();
                        } else {
                            sender.addChatMessage(new TextComponentString("[Kismet] Error, target database factory not found"));
                        }
                    } else
                        throw new WrongUsageException(getCommandName());
                    return;
                case "blacklist":
                    return;
                case "whitelist":
                    return;
                default:
                    throw new WrongUsageException(getCommandName());
            }
        }
//        throw new WrongUsageException(getCommandName());
    }
}
