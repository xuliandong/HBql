/*
 * Copyright (c) 2011.  The Apache Software Foundation
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

package org.apache.hadoop.hbase.hbql.statement;

import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.hbql.client.ExecutionResults;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;

import java.io.IOException;

public class ShowTablesStatement extends GenericStatement implements ConnectionStatement {

    public ShowTablesStatement() {
        super(null);
    }

    protected ExecutionResults execute(final HConnectionImpl conn) throws HBqlException {

        try {
            final HBaseAdmin admin = conn.getHBaseAdmin();

            final ExecutionResults retval = new ExecutionResults();
            retval.out.println("Tables: ");
            for (final HTableDescriptor tableDesc : admin.listTables())
                retval.out.println("\t" + tableDesc.getNameAsString());

            retval.out.flush();
            return retval;
        }
        catch (IOException e) {
            throw new HBqlException(e);
        }
    }

    public static String usage() {
        return "SHOW TABLES";
    }
}