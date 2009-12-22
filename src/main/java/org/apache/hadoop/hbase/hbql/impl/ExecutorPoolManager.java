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

package org.apache.hadoop.hbase.hbql.impl;

import org.apache.expreval.util.Maps;
import org.apache.hadoop.hbase.hbql.client.HBqlException;

import java.util.Map;

public class ExecutorPoolManager {

    private static Map<String, ExecutorPool> executorPoolMap = Maps.newConcurrentHashMap();

    private static Map<String, ExecutorPool> getExecutorPoolMap() {
        return ExecutorPoolManager.executorPoolMap;
    }

    public static ExecutorPool newExecutorPool(final String poolName,
                                               final int maxPoolSize,
                                               final int numberOfThreads) throws HBqlException {

        if (poolName != null && poolName.length() > 0 && getExecutorPoolMap().containsKey(poolName))
            throw new HBqlException("Executor pool already exists: " + poolName);

        final ExecutorPool executorPool = new ExecutorPool(poolName, maxPoolSize, numberOfThreads);
        getExecutorPoolMap().put(executorPool.getName(), executorPool);

        return executorPool;
    }

    public static boolean dropExecutorPool(final String name) {

        if (name != null && name.length() > 0) {
            if (getExecutorPoolMap().containsKey(name)) {
                getExecutorPoolMap().remove(name);
                return true;
            }
        }

        return false;
    }

    public static boolean executorPoolExists(final String name) {
        return name != null && name.length() > 0 && getExecutorPoolMap().containsKey(name);
    }

    public static ExecutorPool getExecutorPool(final String poolName) throws HBqlException {
        if (!ExecutorPoolManager.getExecutorPoolMap().containsKey(poolName))
            throw new HBqlException("Missing executor pool: " + poolName);

        return ExecutorPoolManager.getExecutorPoolMap().get(poolName);
    }
}