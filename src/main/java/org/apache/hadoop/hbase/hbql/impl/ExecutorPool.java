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

import org.apache.hadoop.hbase.hbql.client.HBqlException;

public class ExecutorPool extends ElementPool<GenericExecutor> {

    private final int threadCount;
    private final boolean threadsReadResults;
    private final int queueSize;

    public ExecutorPool(final String name,
                        final int maxPoolSize,
                        final int threadCount,
                        final boolean threadsReadResults,
                        final int queueSize) {
        super(name, maxPoolSize);
        this.threadCount = threadCount;
        this.threadsReadResults = threadsReadResults;
        this.queueSize = queueSize;
    }

    public int getThreadCount() {
        return this.threadCount;
    }

    private boolean threadsReadResults() {
        return this.threadsReadResults;
    }

    private int getQueueSize() {
        return this.queueSize;
    }

    protected GenericExecutor newElement() throws HBqlException {
        return this.threadsReadResults()
               ? ResultExecutor.newPooledResultExecutor(this, this.getThreadCount(), this.getQueueSize())
               : ResultScannerExecutor.newPooledResultScannerExecutor(this, this.getThreadCount(), this.getQueueSize());
    }
}
