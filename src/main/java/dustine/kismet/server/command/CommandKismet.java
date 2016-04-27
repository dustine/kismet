package dustine.kismet.server.command;

import dustine.kismet.Reference;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.*;
import java.util.stream.Collectors;

public class CommandKismet extends CommandBase {
    private final List<ICommandComponent> components;
    private final List<String> names;

    public CommandKismet() {
        this.components = new ArrayList<>();
        this.components.add(new CCReset(getCommandName()));
        this.components.add(new CCDump(getCommandName()));
        this.components.add(new CCStats(getCommandName()));
        this.components.add(new CCRefresh(getCommandName()));


        // cached
        this.names = this.components.stream().map(ICommandComponent::getComponentName).collect(Collectors.toList());
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
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(getCommandName());
        }

        final ICommandComponent component = this.components.stream()
                .filter(c -> args[0].equals(c.getComponentName()))
                .findFirst()
                .orElseThrow(() -> new WrongUsageException(getCommandName()));
        component.execute(server, sender, Arrays.copyOfRange(args, 1, args.length));
    }

    static void sendLine(ICommandSender sender, String msg) {
        sender.addChatMessage(new TextComponentString(msg));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        if (!sender.canCommandSenderUseCommand(getRequiredPermissionLevel(), getCommandName()))
            return Collections.emptyList();

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, this.names);
        } else {
            final Optional<ICommandComponent> name = this.components.stream()
                    .filter(c -> args[0].equals(c.getComponentName()))
                    .findFirst();
            if (!name.isPresent())
                return Collections.emptyList();
            final ICommandComponent component = name.get();
            return getListOfStringsMatchingLastWord(args,
                    component.getTabCompletionOptions(server, sender, Arrays.copyOfRange(args, 1, args.length), pos)
            );
        }
    }

    static void send(ICommandSender sender, String msg) {
        sender.addChatMessage(new TextComponentString(String.format("[%s] %s", Reference.Names.MOD_PRETTY, msg)));
    }


}
