/*
 *
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

package fredboat.event;

import fredboat.FredBoat;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by napster on 09.06.17.
 * <p>
 * A self destructing listener for reactions to a single message
 */
public class ReactionListener extends ListenerAdapter {


    private FredBoat shard;
    private long messageId;
    private Predicate<Member> filter;
    private Consumer<MessageReaction> callback;
    private long lastValidReaction;

    /**
     * This whole thing gets rekt by a revived JDA
     *
     * @param message            The message on which to listen for reactions
     * @param filter             filter by Members
     * @param callback           wat do when a reaction happens that went through the filter
     * @param selfDestructMillis milliseconds after which this listener is removed and the message deleted
     */
    public ReactionListener(Message message, Predicate<Member> filter, Consumer<MessageReaction> callback,
                            long selfDestructMillis, Consumer<Void> selfDestructCallback) {
        this.shard = FredBoat.getInstance(message.getJDA());
        messageId = message.getIdLong();
        this.filter = filter;
        this.callback = callback;
        this.lastValidReaction = System.currentTimeMillis();

        FredBoat.executor.submit(() -> {
            //wait until the reaction listener isn't being used anymore
            while (System.currentTimeMillis() - lastValidReaction < selfDestructMillis && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            //remove the listener
            shard.getJda().removeEventListener(this);
            selfDestructCallback.accept(null);
        });
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        if (messageId != event.getMessageIdLong()) {
            return;
        }

        if (!filter.test(event.getMember())) {
            return;
        }

        callback.accept(event.getReaction());
        lastValidReaction = System.currentTimeMillis();
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {

    }
}
