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

import fredboat.shared.constant.DistributionEnum;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
    private String hostname;

    private long fredboatStartTime; //this should not change for the same running fredboat jvm
    private DistributionEnum fredboatDistribution;
    private String tokenHash; //hash of the discord token that is being used to log in

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
        result.hostname = jsonObject.getString("hostname");

        result.fredboatStartTime = jsonObject.getLong("fredboatStartTime");
        result.fredboatDistribution = DistributionEnum.valueOf(jsonObject.getString("fredboatDistribution"));
        result.tokenHash = jsonObject.getString("tokenHash");

        return result;
    }

    //use JvmAndSystemReport.from(JSONObject) to convert a received report
    //use one of the public constructors to create a report that you want to send
    private JvmAndSystemReport() {

    }

    public JvmAndSystemReport(long fredboatStartTime, DistributionEnum distribution, String tokenHash) {
        this.reportCreated = System.currentTimeMillis();
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.jvmFreeMemory = Runtime.getRuntime().freeMemory();
        this.jvmMaxMemory = Runtime.getRuntime().maxMemory();
        this.jvmTotalMemory = Runtime.getRuntime().totalMemory();
        this.systemEnvironment = System.getenv();
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.hostname = "unknown";
        }

        this.fredboatStartTime = fredboatStartTime;
        this.fredboatDistribution = distribution;
        this.tokenHash = tokenHash;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();

        result.put("reportCreated", this.reportCreated);
        result.put("availableProcessors", this.availableProcessors);
        result.put("jvmFreeMemory", this.jvmFreeMemory);
        result.put("jvmMaxMemory", this.jvmMaxMemory);
        result.put("jvmTotalMemory", this.jvmTotalMemory);
        result.put("systemEnvironment", new JSONObject(systemEnvironment));
        result.put("hostname", this.hostname);

        result.put("fredboatStartTime", this.fredboatStartTime);
        result.put("fredboatDistribution", this.fredboatDistribution.name());
        result.put("tokenHash", this.tokenHash);

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

    public String getHostname() {
        return hostname;
    }

    public long getFredboatStartTime() {
        return fredboatStartTime;
    }

    public DistributionEnum getFredboatDistribution() {
        return fredboatDistribution;
    }

    public String getTokenHash() {
        return tokenHash;
    }
}
