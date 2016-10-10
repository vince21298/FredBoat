package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.IMusicCommand;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class RestartCommand extends Command implements IMusicCommand {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        GuildPlayer player = PlayerRegistry.getExisting(guild);
        
        if(player != null && !player.isQueueEmpty()){
            player.getPlayingTrack().setPosition(0L);
            channel.sendMessage("**" + player.getPlayingTrack().getInfo().title + "** has been restarted.");
        } else {
            channel.sendMessage("The queue is empty.");
        }
    }
    
}
