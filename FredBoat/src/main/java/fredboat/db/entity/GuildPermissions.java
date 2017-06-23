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

package fredboat.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "guild_permissions")
public class GuildPermissions implements IEntity {

    // Guild ID
    @Id
    private long id;

    @Override
    public void setId(String id) {

    }

    @Column(name = "list_admin", nullable = false, columnDefinition = "text")
    private String adminList = "";

    @Column(name = "list_manager", nullable = false, columnDefinition = "text")
    private String managerList = "everyone";

    @Column(name = "list_user", nullable = false, columnDefinition = "text")
    private String userList = "everyone";

    public List<String> getAdminList() {
        if (adminList == null) return new ArrayList<>();

        return Arrays.asList(adminList.split(" "));
    }

    public void setAdminList(ArrayList<String> list) {
        StringBuilder str = new StringBuilder();
        for (String item : list) {
            str.append(item).append(" ");
        }

        adminList = str.toString().trim();
    }

    public List<String> getManagerList() {
        if (managerList == null) return new ArrayList<>();

        return Arrays.asList(managerList.split(" "));
    }

    public void setManagerList(ArrayList<String> list) {
        StringBuilder str = new StringBuilder();
        for (String item : list) {
            str.append(item).append(" ");
        }

        managerList = str.toString().trim();
    }

    public List<String> getUserList() {
        if (userList == null) return new ArrayList<>();

        return Arrays.asList(userList.split(" "));
    }

    public void setUserList(ArrayList<String> list) {
        StringBuilder str = new StringBuilder();
        for (String item : list) {
            str.append(item).append(" ");
        }

        userList = str.toString().trim();
    }
}
