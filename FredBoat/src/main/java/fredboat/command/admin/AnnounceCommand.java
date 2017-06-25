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
import fredboat.commandmeta.abs.ICommandRestricted;
import fredboat.perms.PermissionLevel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author frederik
 */
public class AnnounceCommand extends Command implements ICommandRestricted {

    private static final Logger log = LoggerFactory.getLogger(AnnounceCommand.class);

    private static final String HEAD = "__**[BROADCASTED MESSAGE]**__\n";

    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        List<GuildPlayer> players = PlayerRegistry.getPlayingPlayers();
        String input = message.getRawContent().substring(args[0].length() + 1);
        String msg = HEAD + input;

        Message status;
        try {
            status = channel.sendMessage(String.format("[0/%d]", players.size())).complete(true);
        } catch (RateLimitedException e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
            CountDownLatch latch = new CountDownLatch(players.size());
            Thread parent = Thread.currentThread();

            for (GuildPlayer player : players) {
                try {
                    player.getActiveTextChannel().sendMessage(msg).queue(
                            message1 -> latch.countDown(),
                            throwable -> latch.countDown());
                } catch (Exception e) {
                    log.error("Got exception when posting announcement", e);
                }

                latch.countDown();
            }

            new Thread(() -> {
                while (parent.isAlive()) {
                    synchronized (this) {
                        try {
                            this.wait(5000);
                            status.editMessage(String.format("[%d/%d]", players.size() - latch.getCount(), players.size())).queue();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();

            status.editMessage(String.format("[%d/%d]", players.size() - latch.getCount(), players.size())).queue();
        }).start();
    }

    @Override
    public String help(Guild guild) {
        return "{0}{1}\n#Broadcasts an announcement to GuildPlayer TextChannels.";
    }

    @Override
    public PermissionLevel getMinimumPerms() {
        return PermissionLevel.BOT_ADMIN;
    }
}
