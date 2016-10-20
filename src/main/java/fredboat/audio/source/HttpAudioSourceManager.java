package fredboat.audio.source;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpAudioSourceManager implements AudioSourceManager {

    private static final String TRACK_URL_REGEX = "^https?:\\/\\/.+\\.(?:mkv|(?:m4a|(?:mp3|(?:mp4|webm))))$";
    private static final String TRACK_NAME_REGEX = ".*\\/(.*)$";
    private static final Pattern TRACK_URL_PATTERN = Pattern.compile(TRACK_URL_REGEX);
    private static final Pattern TRACK_NAME_PATTERN = Pattern.compile(TRACK_NAME_REGEX);
    private final HttpClientBuilder HTTP_CLIENT_BUILDER;

    public HttpAudioSourceManager() {
        HTTP_CLIENT_BUILDER = HttpClientTools.createSharedCookiesHttpBuilder();
    }

    @Override
    public String getSourceName() {
        return "http";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager apm, String identifier) {
        if (!TRACK_URL_PATTERN.matcher(identifier).matches()) {
            return null;
        }

        try {
            Unirest.head(identifier).asBinary();//Will throw exception if not found
        } catch (UnirestException ex) {
            return null;
        }

        Matcher nameMatcher = TRACK_NAME_PATTERN.matcher(identifier);
        String title;
        if (nameMatcher.find()) {
            title = nameMatcher.group(1);
        } else {
            title = "unknown";
        }

        AudioTrackInfo trackInfo = new AudioTrackInfo(
                title,
                "unknown",
                0,
                identifier
        );

        return new HttpAudioTrack(trackInfo, this, identifier);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack at) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack at, DataOutput output) throws IOException {
        output.writeUTF(((HttpAudioTrack) at).getTrackUrl());
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        String trackUrl = input.readUTF();

    return new HttpAudioTrack(trackInfo, this, trackUrl);
    }

    /**
     * @return A new HttpClient instance. All instances returned from this
     * method use the same cookie jar.
     */
    public CloseableHttpClient createHttpClient() {
        return HTTP_CLIENT_BUILDER.build();
    }

    @Override
    public void shutdown() {
    }

}
