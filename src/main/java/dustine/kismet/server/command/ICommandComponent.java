package dustine.kismet.server.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface ICommandComponent {
    String getComponentName();

    String getParentName();

    void execute(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException;

    List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos);
}
