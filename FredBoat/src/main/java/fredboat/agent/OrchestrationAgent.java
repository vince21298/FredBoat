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

package fredboat.agent;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import fredboat.FredBoat;
import fredboat.shared.json.HeartbeatPayload;
import fredboat.shared.json.JvmAndSystemReport;
import fredboat.shared.json.ShardReport;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by napster on 30.06.17.
 * <p>
 * Sends a heartbeat to the orchestrator and receives a desired state back
 * <p>
 * Manages the transition of this JVM to the desired state
 */
public class OrchestrationAgent extends Thread {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationAgent.class);

    private boolean shutdown = false;
    private String orchestratorBaseUrl;
    private String key;

    public OrchestrationAgent(String orchestratorUrl) {
        super(OrchestrationAgent.class.getSimpleName());
        this.orchestratorBaseUrl = orchestratorUrl;
        this.key = FredBoat.START_TIME + ""; //todo should be good enough for testing, something more permanent between restarts for production?
        log.info("Created orchestrator agent with base url: {}", orchestratorBaseUrl);
    }


    @Override
    public void run() {
        try {
            while (!shutdown) {
                sendHeartBeat();
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            log.error("OrchestrationAgent interrupted");
        }
    }

    public void shutdown() {
        shutdown = true;
    }


    //things that go into a heartbeat:
    // - information about the system this is running in
    // - information about the running JVM
    // - information about the shards running (shard number, total shards, status, users, etc)
    private void sendHeartBeat() {
        log.info("Sending heartbeat");
        JvmAndSystemReport jvmAndSystemReport = new JvmAndSystemReport(FredBoat.START_TIME, Config.CONFIG.getDistribution(), tokenHash);

        List<ShardReport> shardReports = new ArrayList<>();
        for (FredBoat shard : Collections.unmodifiableCollection(FredBoat.getShards())) {
            shardReports.add(new ShardReport(shard.getShardInfo().getShardId(),
                    shard.getShardInfo().getShardTotal(),
                    shard.getJda().getStatus().toString(),
                    false, //todo implement ghosted shards
                    shard.getJda().getGuilds().size(),
                    shard.getJda().getUsers().stream().map(User::getIdLong).collect(Collectors.toSet()))
            );
        }

        JSONObject heartbeatPayload = new HeartbeatPayload(key, jvmAndSystemReport, shardReports).toJson();

        String heartBeatUrl = orchestratorBaseUrl + "/heartbeat";
        try {
            HttpResponse<JsonNode> response = Unirest.post(heartBeatUrl)
                    .body(heartbeatPayload)
                    .asJson();
            log.info("Received heartbeat response: {}", response.getBody().toString());
        } catch (UnirestException e) {
            log.error("Exception when posting heartbeat to {}, payload: {}", heartBeatUrl, heartbeatPayload.toString(), e);
        }
    }
}
