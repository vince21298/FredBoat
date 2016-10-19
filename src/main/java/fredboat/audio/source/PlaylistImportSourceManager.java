package fredboat.audio.source;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import fredboat.audio.AbstractPlayer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;

public class PlaylistImportSourceManager implements AudioSourceManager {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PlaylistImportSourceManager.class);

    private static Pattern PLAYLIST_PATTERN = Pattern.compile("^https?:\\/\\/hastebin\\.com\\/(?:raw\\/)?(\\w+)(?:\\..+)?$");
    private static AudioPlayerManager privateManager = AbstractPlayer.registerSourceManagers(new AudioPlayerManager());

    @Override
    public String getSourceName() {
        return "playlist_import";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, String identifier) {
        Matcher m = PLAYLIST_PATTERN.matcher(identifier);

        if (!m.find()) {
            return null;
        }

        String hasteId = m.group(1);
        String response;
        try {
            response = Unirest.get("http://hastebin.com/raw/" + hasteId).asString().getBody();
        } catch (UnirestException ex) {
            throw new FriendlyException("Couldn't load playlist. Either Hastebin is down or the playlist does not exist.", FriendlyException.Severity.FAULT, ex);
        }

        String[] unfiltered = response.split("\\s");
        ArrayList<String> filtered = new ArrayList<>();
        for (String str : unfiltered) {
            if (!str.equals("")) {
                filtered.add(str);
            }
        }

        HastebinAudioResultHandler handler = new HastebinAudioResultHandler();
        Future<Void> lastFuture = null;
        for (String id : filtered) {
            lastFuture = privateManager.loadItemOrdered(handler, id, handler);
        }
        
        if(lastFuture == null){
            return null;
        }

        try {
            lastFuture.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new FriendlyException("Failed loading playlist item", FriendlyException.Severity.FAULT, ex);
        }

        return new BasicAudioPlaylist(hasteId, handler.getLoadedTracks(), null);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        throw new UnsupportedOperationException("This source manager is only for loading playlists");
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        throw new UnsupportedOperationException("This source manager is only for loading playlists");
    }

    private class HastebinAudioResultHandler implements AudioLoadResultHandler {

        private final List<AudioTrack> loadedTracks;

        private HastebinAudioResultHandler() {
            this.loadedTracks = new ArrayList<>();
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadedTracks.add(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            log.info("Attempt to load a playlist recursively, skipping");
        }

        @Override
        public void noMatches() {
            //ignore
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            log.debug("Failed loading track provided via hastebin ", exception);
        }

        public List<AudioTrack> getLoadedTracks() {
            return loadedTracks;
        }

    }

}
