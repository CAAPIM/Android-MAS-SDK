/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.conf;

import org.json.JSONException;
import org.json.JSONObject;

public class Server {

    private String host;
    private int port;
    private String prefix;

    public Server(JSONObject config) {

        try {
            JSONObject server = config.getJSONObject("server");
            this.host = server.getString("hostname");
            this.port = server.optInt("port");
            this.prefix = server.optString("prefix");
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Server(String host, int port, String prefix) {
        this.host = host;
        this.port = port;
        this.prefix = prefix;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Server server = (Server) o;

        if (port != server.port) return false;
        if (host != null ? !host.equals(server.host) : server.host != null) return false;
        return prefix != null ? prefix.equals(server.prefix) : server.prefix == null;

    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(host).append(":").append(port).append("/").append(prefix).append("/");
        return sb.toString();
    }
}
