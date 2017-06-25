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

package fredboat.event;

import fredboat.Config;
import fredboat.util.SelectionConsumer;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SelectionManager {

    private static final List<SelectionManager> managers = new ArrayList<>();
    private static final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    private final ConcurrentHashMap<String, SelectionConsumer> selections = new ConcurrentHashMap<>();

    public static void init() {
        for (int i = 0; i < Config.CONFIG.getNumShards(); i++) {
            managers.add(new SelectionManager());
        }
    }

    public static void addConsumer(Member member, SelectionConsumer consumer) {
        SelectionManager man = getInstance(member.getJDA());

        String k = member.getGuild().getId() + "-" + member.getUser().getId();

        man.selections.put(k, consumer);

        exec.schedule(() -> man.selections.remove(k), 1, TimeUnit.MINUTES);
    }

    public static SelectionManager getInstance(JDA jda) {
        if (jda.getShardInfo() == null) return managers.get(0);

        return managers.get(jda.getShardInfo().getShardId());
    }

    public static void onMessage(Member member, Message message) {
        SelectionManager man = getInstance(member.getJDA());
        String k = member.getGuild().getId() + "-" + member.getUser().getId();

        man.selections.computeIfPresent(k, (s, consumer) -> consumer.accept(message) ? null : consumer);
    }

}
