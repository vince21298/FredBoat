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

import org.json.JSONObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@EnableAutoConfiguration
public class OrchestrationController {

    // Called when a new container starts. Returns a list of shards to build
    @GetMapping(value = "/allocate", produces = "application/json")
    @ResponseBody
    String allocate(@RequestParam("key") String key) {
        Allocation alloc = Allocator.INSTANCE.allocate(key);

        JSONObject out = new JSONObject();

        out.put("chunk", alloc.getChunk());
        out.put("assignedStartTime", alloc.getAssignedStartTime());

        return "";
    }

    // Status of the swarm, like total number of guilds. Can also be used manually
    @GetMapping(value = "/status", produces = "application/json")
    @ResponseBody
    String status() {
        return "";
    }

    // Post shard statusses and make sure shards are still alive
    @PostMapping(value = "/heartbeat", produces = "application/json")
    void heartbeat(@RequestParam("key") String key) {
        Allocator.INSTANCE.getAllocation(key).onBeat();
    }

    // Post an array of users so we can filter out duplicates
    @PostMapping(value = "/userstats", produces = "application/json")
    @ResponseBody
    String userstats() {
        return "";
    }

}
