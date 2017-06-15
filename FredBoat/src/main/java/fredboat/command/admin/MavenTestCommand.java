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
 */

package fredboat.command.admin;

import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.ICommandOwnerRestricted;
import fredboat.util.log.SLF4JInputStreamErrorLogger;
import fredboat.util.log.SLF4JInputStreamLogger;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by napster on 23.03.17.
 * <p>
 * Attempts to run the "mvn test" command on the bots present sources.
 */
public class MavenTestCommand extends Command implements ICommandOwnerRestricted {

    private static final Logger log = LoggerFactory.getLogger(MavenTestCommand.class);

    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        try {
            Runtime rt = Runtime.getRuntime();
            Message msg;

            msg = channel.sendMessage("*Testing now...*").complete(true);

            msg = msg.editMessage(msg.getRawContent() + "\n\nRunning `mvn test`... ").complete(true);
            File pom = new File("FredBoat/pom.xml");
            if (!pom.exists()) pom = new File("pom.xml");
            if (!pom.exists()) {
                msg.editMessage(msg.getRawContent() + "[:anger: could not locate pom.xml:]\n\n").complete(true);
                throw new RuntimeException("Could not locate file: pom.xml");
            }

            String pomPath = pom.getAbsolutePath();
            Process mvnBuild = rt.exec("mvn -f " + pomPath + " test");
            new SLF4JInputStreamLogger(log, mvnBuild.getInputStream()).start();
            new SLF4JInputStreamErrorLogger(log, mvnBuild.getInputStream()).start();

            if (!mvnBuild.waitFor(600, TimeUnit.SECONDS)) {
                msg.editMessage(msg.getRawContent() + "[:anger: timed out]\n\n").complete(true);
                throw new RuntimeException("Operation timed out: mvn test");
            } else if (mvnBuild.exitValue() != 0) {
                msg.editMessage(msg.getRawContent() + "[:anger: returned code " + mvnBuild.exitValue() + "]\n\n").complete(true);
                throw new RuntimeException("Bad response code");
            }

            msg.editMessage(msg.getRawContent() + "👌🏽").queue();

        } catch (InterruptedException | IOException | RateLimitedException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String help(Guild guild) {
        return "{0}{1}\n#Run 'mvn test' on the bots present sources.";
    }
}
