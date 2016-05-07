package dustine.kismet.server.command;

import dustine.kismet.Kismet;
import dustine.kismet.world.savedata.TargetDatabaseBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.Collections;
import java.util.List;

public class CCReset extends CommandComponent {
    public CCReset(String parent) {
        super(parent);
    }

    @Override
    public String getCommandName() {
        return "reset";
    }
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            CommandKismet.error("Command can only be run in-game");
            return;
        }
        if (Kismet.databaseBuilder != null) {
            CommandKismet.send(sender, new TextComponentString("Starting database reset..."));
            Kismet.databaseBuilder.build((EntityPlayerMP) sender, true);
            CommandKismet.send(sender,
                    new TextComponentString("Server side processing finished, sending to client for enriching..."));
            TargetDatabaseBuilder.setCommand(true);
        } else {
            CommandKismet.error("Target database factory not found");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
                                                BlockPos pos) {
        return Collections.emptyList();
    }
}
