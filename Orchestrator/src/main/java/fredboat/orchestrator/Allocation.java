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

package fredboat.orchestrator;

import fredboat.orchestrator.json.ShardReport;

import java.util.ArrayList;
import java.util.List;

public class Allocation {

    private static final int TIMEOUT_MILLIS = 15 * 1000;

    private final String key;
    private final int chunk;
    private final long assignedStartTime;
    private long lastBeat = System.currentTimeMillis();
    private List<ShardReport> reports = new ArrayList<>();

    // Statistics
    private List<String> users;

    Allocation(String key, int chunk, long assignedStartTime) {
        this.key = key;
        this.chunk = chunk;
        this.assignedStartTime = assignedStartTime;
    }

    void onBeat() {
        lastBeat = System.currentTimeMillis();
    }

    public void setReports(List<ShardReport> reports) {
        this.reports = reports;
    }

    public boolean isStale() {
        return (System.currentTimeMillis() - lastBeat) < TIMEOUT_MILLIS;
    }

    public String getKey() {
        return key;
    }

    public int getChunk() {
        return chunk;
    }

    public long getAssignedStartTime() {
        return assignedStartTime;
    }

    public List<ShardReport> getReports() {
        return reports;
    }
}
