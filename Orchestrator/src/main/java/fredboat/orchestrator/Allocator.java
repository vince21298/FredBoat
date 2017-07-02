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

import fredboat.shared.json.ShardReport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Allocator {

    public static Allocator INSTANCE;

    private static final int SHARD_START_INTERVAL_TIME = 5500; //Millis we expect a shard to take to start

    private final HashMap<Integer, Allocation> allocations = new HashMap<>();
    private final int chunkSize;
    private final int totalChunkCount;
    private long earliestNewStartTime = System.currentTimeMillis();

    Allocator(int chunkSize, int totalChunkCount) {
        this.chunkSize = chunkSize;
        this.totalChunkCount = totalChunkCount;
    }

    Allocation allocate(String key) {
        int chunk = getLowestAvailableChunk();

        if(chunk == -1) {
            throw new IllegalStateException("Can't allocate new shards! All shards are already taken.");
        }

        long startTime = Math.max(earliestNewStartTime, System.currentTimeMillis());
        earliestNewStartTime = startTime + chunkSize * SHARD_START_INTERVAL_TIME;
        
        Allocation allocation = new Allocation(key, chunk, startTime);
        allocations.put(chunk, allocation);
        return allocation;
    }

    Allocation getAllocation(String key) {
        for(Allocation alloc : allocations.values()) {
            if(Objects.equals(alloc.getKey(), key)) {
                return alloc;
            }
        }

        return null;
    }
    
    private int getLowestAvailableChunk() {
        for (int i = 0; i < totalChunkCount; i++) {
            if(!allocations.containsKey(i) || allocations.get(i).isStale()) {
                return i;
            }
        }

        return -1;
    }

    public List<ShardReport> getReports() {
        List<ShardReport> reports = new LinkedList<>();

        allocations.values().forEach(alloc -> reports.addAll(alloc.getReports()));

        return reports;
    }

}
