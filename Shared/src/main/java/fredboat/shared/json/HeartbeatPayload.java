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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by napster on 01.07.17.
 */
public class HeartbeatPayload {

    private String key;
    private JvmAndSystemReport jvmAndSystemReport;
    private List<ShardReport> shardReports;

    public static HeartbeatPayload from(JSONObject jsonObject) {
        HeartbeatPayload result = new HeartbeatPayload();

        result.key = jsonObject.getString("key");
        result.jvmAndSystemReport = JvmAndSystemReport.from(jsonObject.getJSONObject("jvmAndSystemReport"));

        result.shardReports = new ArrayList<>();
        for (Object o : jsonObject.getJSONArray("shardReports")) {
            result.shardReports.add(ShardReport.from((JSONObject) o));
        }

        return result;
    }

    //use HeartbeatPayload.from(JSONObject) to convert a received heartbeat
    //use one of the public constructors to create a heartbeat that you want to send
    private HeartbeatPayload() {

    }

    public HeartbeatPayload(String key, JvmAndSystemReport jvmAndSystemReport, Collection<ShardReport> shardReports) {
        this.key = key;
        this.jvmAndSystemReport = jvmAndSystemReport;
        this.shardReports = new ArrayList<>(shardReports);
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("key", key);
        result.put("jvmAndSystemReport", jvmAndSystemReport.toJson());
        JSONArray shardsArray = new JSONArray();
        shardReports.forEach(shardReport -> shardsArray.put(shardReport.toJson()));
        result.put("shardReports", shardsArray);

        return result;
    }
}
