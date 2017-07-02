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

package fredboat.orchestrator.stats;

import fredboat.orchestrator.Allocator;
import fredboat.shared.json.ShardReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;

public class StatsManager {

    private static final Logger log = LoggerFactory.getLogger(StatsManager.class);
    private static int USER_COUNT_TIME_THRESHOLD = 5 * 60 * 1000;
    
    private static int totalUsers = 0;
    private static long lastTimeUsersComputed = 0;

    public static int getTotalGuilds() {
        List<ShardReport> reports = Allocator.INSTANCE.getReports();

        final int[] guilds = {0};

        reports.forEach(shardReport -> guilds[0] += shardReport.getGuildCount());

        return guilds[0];
    }
    
    public static int getTotalUsers() {
        // TODO: 4/29/2017
        return totalUsers;
    }

    private static int computeTotalUsers() {
        List<ShardReport> reports = Allocator.INSTANCE.getReports();
        LinkedHashSet<Long> lhs = new LinkedHashSet<>();
        
        reports.forEach(shardReport -> lhs.addAll(shardReport.getUsers()));
        
        totalUsers = lhs.size();
        
        log.info("Calculated user count: " + totalUsers);
        
        return totalUsers;
    }

}
