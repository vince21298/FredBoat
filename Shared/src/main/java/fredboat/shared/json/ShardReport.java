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

package fredboat.shared.json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ShardReport {

    private int shardNumber;
    private int totalShards;
    private String status;
    private boolean isGhosted;
    private int guildCount;
    private Set<Long> users;


    public static ShardReport from(JSONObject jsonObject) {
        ShardReport result = new ShardReport();

        result.shardNumber = jsonObject.getInt("shardNumber");
        result.totalShards = jsonObject.getInt("totalShards");
        result.status = jsonObject.getString("status");
        result.isGhosted = jsonObject.getBoolean("isGhosted");
        result.guildCount = jsonObject.getInt("guildCount");

        JSONArray a = jsonObject.getJSONArray("users");
        a.forEach(o -> result.users.add((Long) o));

        return result;
    }

    //use ShardReport.from(JSONObject) to convert a received report
    //use one of the public constructors to create a report that you want to send
    private ShardReport() {
    }

    public ShardReport(int shardNumber, int totalShards, String status, boolean isGhosted, int guildCount, Collection<Long> users) {
        this.shardNumber = shardNumber;
        this.totalShards = totalShards;
        this.status = status;
        this.isGhosted = isGhosted;
        this.guildCount = guildCount;
        this.users = new HashSet<>(users);
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();

        result.put("shardNumber", shardNumber);
        result.put("totalShards", totalShards);
        result.put("status", status);
        result.put("isGhosted", isGhosted);
        result.put("guildCount", guildCount);
        result.put("shardNumber", shardNumber);

        return result;
    }

    public int getShardNumber() {
        return shardNumber;
    }

    public int getTotalShards() {
        return totalShards;
    }

    public String getStatus() {
        return status;
    }

    public int getGuildCount() {
        return guildCount;
    }

    public Set<Long> getUsers() {
        return users;
    }
}
