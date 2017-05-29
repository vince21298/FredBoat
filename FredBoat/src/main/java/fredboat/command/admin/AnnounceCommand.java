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

package fredboat.command.admin;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.ICommandAdminRestricted;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import java.util.List;

/**
 *
 * @author frederik
 */
public class AnnounceCommand extends Command implements ICommandAdminRestricted {

    private static final String HEAD = "__**[BROADCASTED MESSAGE]**__\n";

    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        List<GuildPlayer> players = PlayerRegistry.getPlayingPlayers();
        String input = message.getRawContent().substring(args[0].length() + 1);
        String msg = HEAD + input;

        Message status;
        try {
            status = channel.sendMessage("[0/" + players.size() + "]").complete(true);
        } catch (RateLimitedException e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
            int skipped = 0;
            int sent = 0;
            int i = 0;

            for (GuildPlayer player : players) {
                try {
                    player.getActiveTextChannel().sendMessage(msg).complete(true);
                    sent++;
                } catch (PermissionException | RateLimitedException e) {
                    skipped++;
                }

                if (i % 20 == 0) {
                    status.editMessage("[" + sent + "/" + (players.size() - skipped) + "]").queue();
                }

                i++;
            }

            status.editMessage("[" + sent + "/" + (players.size() - skipped) + "]").queue();
        }).start();
    }

    @Override
    public String help(Guild guild) {
        return "{0}{1}\n#Broadcasts an announcement to GuildPlayer TextChannels.";
    }
}