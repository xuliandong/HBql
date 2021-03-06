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

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;
import org.apache.hadoop.hbase.hbql.mapping.FamilyDefinition;

import java.io.IOException;

public class AddFamilyAction implements AlterTableAction {
    private final FamilyDefinition familyDefinition;

    public AddFamilyAction(final FamilyDefinition familyDefinition) {
        this.familyDefinition = familyDefinition;
    }

    private FamilyDefinition getFamilyDefinition() {
        return this.familyDefinition;
    }

    public void execute(final HConnectionImpl conn,
                        final HBaseAdmin admin,
                        final String tableName) throws HBqlException {

        final HColumnDescriptor columnDescriptor = this.getFamilyDefinition().getColumnDescription();

        try {
            admin.addColumn(tableName, columnDescriptor);
        }
        catch (IOException e) {
            throw new HBqlException(e);
        }
    }
}
