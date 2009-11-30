/*
 * Copyright (c) 2009.  The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public class Driver implements java.sql.Driver {

    static {
        try {
            DriverManager.registerDriver(new Driver());
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection connect(final String str, final Properties properties) throws SQLException {
        if (this.acceptsURL(str))
            return new ConnectionImpl(null);
        else
            return null;
    }

    public boolean acceptsURL(final String url) throws SQLException {
        return (url != null && url.toLowerCase().startsWith("jdbc:hbql"));
    }

    public DriverPropertyInfo[] getPropertyInfo(final String s, final Properties properties) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        return false;
    }
}
