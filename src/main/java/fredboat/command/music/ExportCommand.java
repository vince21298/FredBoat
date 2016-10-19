package fredboat.command.music;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.MessagingException;
import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.IMusicCommand;
import fredboat.util.TextUtils;
import java.util.List;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class ExportCommand extends Command implements IMusicCommand {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        GuildPlayer player = PlayerRegistry.get(guild.getId());
        
        if(player.getRemainingTracks().isEmpty()){
            throw new MessagingException("Nothing to export, the queue is empty.");
        }
        
        List<AudioTrack> tracks = player.getRemainingTracks();
        String out = "";
        
        for(AudioTrack at : tracks){
            if(at instanceof YoutubeAudioTrack){
                out = out + "https://www.youtube.com/watch?v=" + at.getIdentifier() + "\n";
            } else {
                out = out + at.getIdentifier() + "\n";
            }
        }
        
        try {
            String url = TextUtils.postToHastebin(out, true) + ".fredboat";
            channel.sendMessage("Exported playlist: " + url + "\nYou can provide this URL to play the current playlist later.");
        } catch (UnirestException ex) {
            throw new MessagingException("Failed to upload playlist to hastebin.com");
        }
        
        
    }

}
