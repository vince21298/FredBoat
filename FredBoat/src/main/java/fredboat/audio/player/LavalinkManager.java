/*
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package fredboat.audio.player;

import fredboat.Config;
import fredboat.FredBoat;
import lavalink.client.io.Lavalink;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.List;

public class LavalinkManager {

    public static final LavalinkManager ins = new LavalinkManager();

    private LavalinkManager() {
    }

    private boolean lavalinkEnabled = false;
    private Lavalink lavalink = null;

    public void start() {
        List<Config.LavalinkHost> hosts = Config.CONFIG.getLavalinkHosts();

        if (hosts.isEmpty()) return;

        lavalink = new Lavalink(Config.CONFIG.getNumShards(), shardId -> FredBoat.getInstance(shardId).getJda());
        lavalinkEnabled = true;
        hosts.forEach(lavalinkHost -> lavalink.addNode(lavalinkHost.getUri(),
                lavalinkHost.getPassword()));
    }

    IPlayer createPlayer(String guildId) {
        return lavalinkEnabled
                ? lavalink.getPlayer(guildId)
                : new LavaplayerPlayerWrapper(AbstractPlayer.getPlayerManager().createPlayer());
    }

    public void openConnection(VoiceChannel channel) {
        if (lavalinkEnabled) {
            lavalink.openVoiceConnection(channel);
        } else {
            channel.getGuild().getAudioManager().openAudioConnection(channel);
        }
    }

    public void closeConnection(Guild guild) {
        if (lavalinkEnabled) {
            lavalink.closeVoiceConnection(guild);
        } else {
            guild.getAudioManager().closeAudioConnection();
        }
    }

    public VoiceChannel getConnectedChannel(Guild guild) {
        if (lavalinkEnabled) {
            return lavalink.getConnectedChannel(guild);
        } else {
            return guild.getAudioManager().getConnectedChannel();
        }
    }

    public boolean isLavalinkEnabled() {
        return lavalinkEnabled;
    }

    public Lavalink getLavalink() {
        return lavalink;
    }
}
