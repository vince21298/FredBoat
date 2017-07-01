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

package fredboat.orchestrator.json;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by napster on 01.07.17.
 * <p>
 * Some data about the running system and jvm
 */
public class JvmAndSystemReport {

    private long reportCreated;
    private int availableProcessors;
    private long jvmFreeMemory;
    private long jvmMaxMemory;
    private long jvmTotalMemory;
    private Map<String, String> systemEnvironment;

    private long fredboatStartTime; //this should not change for the same running fredboat jvm

    public static JvmAndSystemReport from(JSONObject jsonObject) {
        JvmAndSystemReport result = new JvmAndSystemReport();
        result.reportCreated = jsonObject.getLong("reportCreated");
        result.availableProcessors = jsonObject.getInt("availableProcessors");
        result.jvmFreeMemory = jsonObject.getLong("jvmFreeMemory");
        result.jvmMaxMemory = jsonObject.getLong("jvmMaxMemory");
        result.jvmTotalMemory = jsonObject.getLong("jvmTotalMemory");
        JSONObject sysEnv = jsonObject.getJSONObject("systemEnvironment");
        result.systemEnvironment = new HashMap<>();
        sysEnv.keySet().forEach(key -> result.systemEnvironment.put(key, sysEnv.getString(key)));

        result.fredboatStartTime = jsonObject.getLong("fredboatStartTime");

        return result;
    }

    //use JvmAndSystemReport.from(JSONObject) to convert a received report
    //use one of the public constructors to create a report that you want to send
    private JvmAndSystemReport() {

    }

    public JvmAndSystemReport(long fredboatStartTime) {
        this.reportCreated = System.currentTimeMillis();
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.jvmFreeMemory = Runtime.getRuntime().freeMemory();
        this.jvmMaxMemory = Runtime.getRuntime().maxMemory();
        this.jvmTotalMemory = Runtime.getRuntime().totalMemory();
        this.systemEnvironment = System.getenv();

        this.fredboatStartTime = fredboatStartTime;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();

        result.put("reportCreated", this.reportCreated);
        result.put("availableProcessors", this.availableProcessors);
        result.put("jvmFreeMemory", this.jvmFreeMemory);
        result.put("jvmMaxMemory", this.jvmMaxMemory);
        result.put("jvmTotalMemory", this.jvmTotalMemory);
        result.put("systemEnvironment", new JSONObject(systemEnvironment));

        result.put("fredboatStartTime", this.fredboatStartTime);

        return result;
    }


    public long getReportCreated() {
        return reportCreated;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public long getJvmFreeMemory() {
        return jvmFreeMemory;
    }

    public long getJvmMaxMemory() {
        return jvmMaxMemory;
    }

    public long getJvmTotalMemory() {
        return jvmTotalMemory;
    }

    public Map<String, String> getSystemEnvironment() {
        return systemEnvironment;
    }

    public long getFredboatStartTime() {
        return fredboatStartTime;
    }
}
