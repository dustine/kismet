package dustine.kismet.server.command;

import dustine.kismet.Reference;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.*;
import java.util.stream.Collectors;

public class CommandKismet extends CommandBase {
    private static final Style ERROR_STYLE = new Style().setColor(TextFormatting.RED);
    private static ITextComponent HEADER = new TextComponentString("")
            .appendSibling(new TextComponentString(String.format("[%s] ", Reference.Names.MOD))
                    .setStyle(new Style().setColor(TextFormatting.YELLOW))
            );
    private final List<CommandComponent> components;
    private final List<String> names;

    public CommandKismet() {
        this.components = new ArrayList<>();
        this.components.add(new CCBlock(getCommandName()));
        this.components.add(new CCDump(getCommandName()));
        this.components.add(new CCForce(getCommandName()));
        this.components.add(new CCPardon(getCommandName()));
        this.components.add(new CCRefresh(getCommandName()));
        this.components.add(new CCReset(getCommandName()));
        this.components.add(new CCServerOnlyReset(getCommandName()));
        this.components.add(new CCStats(getCommandName()));
        this.components.add(new CCUnforce(getCommandName()));

        // cached
        this.names = this.components.stream().map(CommandComponent::getCommandName).collect(Collectors.toList());
    }

    @Override public String getCommandName() {
        return Reference.MOD_ID;
    }

    static void sendLine(ICommandSender sender, String msg) {
        sender.addChatMessage(new TextComponentString(msg));
    }

    public static void send(ICommandSender sender, ITextComponent msg) {
        final ITextComponent component = new TextComponentString(HEADER.getFormattedText());
        component.appendSibling(msg);
        sender.addChatMessage(component);
    }

    public static void error(String error) throws CommandException {
        throw new CommandException(String.format("[%s] %s", Reference.Names.MOD, error));
    }

    public static void sendError(ICommandSender sender, TextComponentString msg) {
        sender.addChatMessage(msg.setStyle(ERROR_STYLE));
    }

    @Override public String getCommandUsage(ICommandSender sender) {
        return "commands.kismet.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        final CommandComponent component = this.components.stream()
                .filter(c -> args[0].equals(c.getCommandName()))
                .findFirst()
                .orElseThrow(() -> new WrongUsageException(getCommandName()));

        component.execute(server, sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
                                                          BlockPos pos) {
        if (!sender.canCommandSenderUseCommand(getRequiredPermissionLevel(), getCommandName()))
            return Collections.emptyList();

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, this.names);
        } else {
            final Optional<CommandComponent> name = this.components.stream()
                    .filter(c -> args[0].equals(c.getCommandName()))
                    .findFirst();
            if (!name.isPresent())
                return Collections.emptyList();
            final CommandComponent component = name.get();
            return getListOfStringsMatchingLastWord(args,
                    component.getTabCompletionOptions(server, sender, Arrays.copyOfRange(args, 1, args.length), pos)
            );
        }
    }
}
