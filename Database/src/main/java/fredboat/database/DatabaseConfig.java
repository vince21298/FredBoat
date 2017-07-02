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

package fredboat.database;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by napster on 02.07.17.
 */
public class DatabaseConfig {

    //see https://github.com/brettwooldridge/HikariCP connectionTimeout
    public static int HIKARI_TIMEOUT_MILLISECONDS = 1000;


    public String jdbcUrl;

    // SSH tunnel stuff
    public boolean useSshTunnel;
    public String sshHost;
    public String sshUser;
    public String sshPrivateKeyFile;
    public int forwardToPort; //port where the remote database is listening, postgres default: 5432


    public static DatabaseConfig loadDefault() throws IOException {
        return load(new File("./dbconf.yaml"));
    }

    public static DatabaseConfig load(File dbConfigFile) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        return yaml.loadAs(new FileReader(dbConfigFile), DatabaseConfig.class);
    }
}
