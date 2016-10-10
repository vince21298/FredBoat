package fredboat.audio.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fredboat.audio.GuildPlayer;
import fredboat.util.TextUtils;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.LoggerFactory;

public class AudioLoader implements AudioLoadResultHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AudioLoader.class);

    private final ITrackProvider trackProvider;
    private final AudioPlayerManager playerManager;
    private final GuildPlayer gplayer;
    private final ConcurrentLinkedQueue<IdentifierContext> identifierQueue = new ConcurrentLinkedQueue();
    private IdentifierContext context = null;
    private volatile boolean isLoading = false;

    public AudioLoader(ITrackProvider trackProvider, AudioPlayerManager playerManager, GuildPlayer gplayer) {
        this.trackProvider = trackProvider;
        this.playerManager = playerManager;
        this.gplayer = gplayer;
    }

    public void loadAsync(IdentifierContext ic) {
        identifierQueue.add(ic);
        if (!isLoading) {
            loadNextAsync();
        }
    }

    private void loadNextAsync() {
        try {
            IdentifierContext ic = identifierQueue.poll();
            if (ic != null) {
                isLoading = true;
                context = ic;
                playerManager.loadItem(ic.identifier, this);
            } else {
                isLoading = false;
            }
        } catch (Throwable th) {
            handleThrowable(context, th);
            isLoading = false;
        }
    }

    @Override
    public void trackLoaded(AudioTrack at) {
        try {
            if (!context.isQuiet()) {
                context.textChannel.sendMessage(
                        gplayer.isPlaying() ? "**" + at.getInfo().title + "** has been added to the queue." : "**" + at.getInfo().title + "** will now play."
                );
            } else {
                log.info("Quietly loaded " + at.getIdentifier());
            }

            at.setPosition(context.getPosition());

            trackProvider.add(at);
            if (!gplayer.isPaused()) {
                gplayer.play();
            }
        } catch (Throwable th) {
            handleThrowable(context, th);
        }
        loadNextAsync();
    }

    @Override
    public void playlistLoaded(AudioPlaylist ap) {
        try {
            context.textChannel.sendMessage(
                    "Found and added `" + ap.getTracks().size() + "` songs from playlist **" + ap.getName() + "**."
            );
            
            for (AudioTrack at : ap.getTracks()) {
                trackProvider.add(at);
            }
            if (!gplayer.isPaused()) {
                gplayer.play();
            }
        } catch (Throwable th) {
            handleThrowable(context, th);
        }
        loadNextAsync();
    }

    @Override
    public void noMatches() {
        try {
            context.textChannel.sendMessage("No audio could be found for `" + context.identifier + "`."
            );
        } catch (Throwable th) {
            handleThrowable(context, th);
        }
        loadNextAsync();
    }

    @Override
    public void loadFailed(FriendlyException fe) {
        handleThrowable(context, fe);

        loadNextAsync();
    }

    private void handleThrowable(IdentifierContext ic, Throwable th) {
        try {
            if (th instanceof FriendlyException) {
                FriendlyException fe = (FriendlyException) th;
                if (fe.severity == FriendlyException.Severity.COMMON) {
                    if (ic.textChannel != null) {
                        context.textChannel.sendMessage("Error ocurred when loading info for `" + context.identifier + "`.");
                        TextUtils.handleException(fe.getCause(), context.textChannel);
                    } else {
                        log.error("Error while loading track ", th);
                    }
                } else if (ic.textChannel != null) {
                    context.textChannel.sendMessage("Suspicious error when loading info for `" + context.identifier + "`.");
                    TextUtils.handleException(fe.getCause(), context.textChannel);
                } else {
                    log.error("Error while loading track ", th);
                }
            } else if (ic.textChannel != null) {
                context.textChannel.sendMessage("Suspicious error when loading info for `" + context.identifier + "`.");
                TextUtils.handleException(th, context.textChannel);
            } else {
                log.error("Error while loading track ", th);
            }
        } catch (Exception e) {
            log.error("Error when trying to handle another error", th);
        }
    }

}
