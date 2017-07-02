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

package fredboat.orchestrator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by napster on 02.07.17.
 * <p>
 * The Treasury gives out coins which can be used to start/restart a shard
 */
public class Treasury {

    //requester <-> last coin requested
    //the requester is defined as a single bot account on discord
    //the bot account may run on more than one JVM and/or machine, this is the place to coordinate the
    //swarms' log ins to discord
    private static Map<String, Long> coins = new HashMap<>();

    private static final int SHARD_START_COOLDOWN = 6000;

    /**
     * @param requester the account requesting the coin
     * @return true if a coin has been granted
     */
    //synchronized for obvious reasons
    public static synchronized boolean requestCoin(String requester) {
        Long lastCoinGiven = coins.get(requester);
        long now = System.currentTimeMillis();
        if (lastCoinGiven == null || now - lastCoinGiven > SHARD_START_COOLDOWN) {
            coins.put(requester, now);
            return true;
        }
        return false;
    }
}
