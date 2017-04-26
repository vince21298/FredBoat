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

import java.util.HashMap;

public class Allocator {

    public static Allocator INSTANCE;
    
    private final HashMap<Integer, Allocation> allocations = new HashMap<>();
    private final int chunkSize;
    private final int totalChunkCount;

    public Allocator(int chunkSize, int totalChunkCount) {
        this.chunkSize = chunkSize;
        this.totalChunkCount = totalChunkCount;
    }

    Allocation allocate(String key) {
        int chunk = getLowestAvailableChunk();
        allocations.put(chunk, new Allocation(key, chunk));
    }
    
    private int getLowestAvailableChunk() {
        for (int i = 0; i < totalChunkCount; i++) {
            if(!allocations.containsKey(i) || allocations.get(i).isStale()) {
                return i;
            }
        }

        return -1;
    }

}
