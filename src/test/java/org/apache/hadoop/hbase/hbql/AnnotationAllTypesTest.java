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

package org.apache.hadoop.hbase.hbql;

import org.apache.expreval.util.Lists;
import org.apache.hadoop.hbase.hbql.client.Batch;
import org.apache.hadoop.hbase.hbql.client.ConnectionManager;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HConnection;
import org.apache.hadoop.hbase.hbql.client.Query;
import org.apache.hadoop.hbase.hbql.client.SchemaManager;
import org.apache.hadoop.hbase.hbql.util.TestSupport;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class AnnotationAllTypesTest extends TestSupport {

    static HConnection conn = null;

    static int cnt = 10;

    @BeforeClass
    public static void emptyTable() throws HBqlException {

        SchemaManager.execute("CREATE SCHEMA alltypes2 FOR TABLE example2"
                              + "("
                              + "keyval KEY, "
                              + "f1:val1 boolean ALIAS booleanValue, "
                              + "f1:val2 boolean[] ALIAS booleanArrayValue, "
                              + "f1:val3 byte ALIAS byteValue, "
                              + "f1:val4 byte[] ALIAS byteArrayValue, "
                              + "f1:val5 char ALIAS charValue, "
                              + "f1:val6 char[] ALIAS charArrayValue, "
                              + "f1:val7 short ALIAS shortValue, "
                              + "f1:val8 short[] ALIAS shortArrayValue, "
                              + "f1:val9 int ALIAS intValue, "
                              + "f1:val10 int[] ALIAS intArrayValue, "
                              + "f1:val11 long ALIAS longValue, "
                              + "f1:val12 long[] ALIAS longArrayValue, "
                              + "f1:val13 float ALIAS floatValue, "
                              + "f1:val14 float[] ALIAS floatArrayValue, "
                              + "f1:val15 double ALIAS doubleValue, "
                              + "f1:val16 double[] ALIAS doubleArrayValue, "
                              + "f1:val17 string ALIAS stringValue, "
                              + "f1:val18 string[] ALIAS stringArrayValue, "
                              + "f1:val19 date ALIAS dateValue, "
                              + "f1:val20 date[] ALIAS dateArrayValue, "
                              + "f1:val21 object ALIAS mapValue, "
                              + "f1:val22 object[] ALIAS mapArrayValue, "
                              + "f1:val23 object ALIAS objectValue, "
                              + "f1:val24 object[] ALIAS objectArrayValue "
                              + ")");

        conn = ConnectionManager.newConnection();

        if (!conn.tableExists("example2"))
            System.out.println(conn.execute("create table using alltypes2"));
        else {
            System.out.println(conn.execute("delete from alltypes2"));
        }
    }

    public static List<AnnotatedAllTypes> insertSomeData(int cnt, boolean noRandomData) throws HBqlException {

        List<AnnotatedAllTypes> retval = Lists.newArrayList();
        final Batch batch = new Batch();

        for (int i = 0; i < cnt; i++) {

            AnnotatedAllTypes aat = new AnnotatedAllTypes();
            aat.setSomeValues(i, noRandomData, cnt);

            retval.add(aat);

            batch.insert(aat);
        }

        conn.apply(batch);

        return retval;
    }


    @Test
    public void simpleSelect() throws HBqlException {

        List<AnnotatedAllTypes> vals = insertSomeData(cnt, true);

        assertTrue(vals.size() == cnt);

        Query<AnnotatedAllTypes> recs = conn.newQuery("select * from alltypes2", AnnotatedAllTypes.class);

        int reccnt = 0;
        for (final AnnotatedAllTypes rec : recs.getResults())
            assertTrue(rec.equals(vals.get(reccnt++)));

        assertTrue(reccnt == cnt);
    }

    @Test
    public void simpleSparseSelect() throws HBqlException {

        List<AnnotatedAllTypes> vals = insertSomeData(cnt, false);

        assertTrue(vals.size() == cnt);

        Query<AnnotatedAllTypes> recs = conn.newQuery("select * from alltypes2", AnnotatedAllTypes.class);

        int reccnt = 0;
        for (final AnnotatedAllTypes rec : recs.getResults())
            assertTrue(rec.equals(vals.get(reccnt++)));

        assertTrue(reccnt == cnt);
    }

    @Test
    public void simpleLimitSelect() throws HBqlException {

        List<AnnotatedAllTypes> vals = insertSomeData(cnt, true);

        assertTrue(vals.size() == cnt);

        Query<AnnotatedAllTypes> recs = conn.newQuery("select * from alltypes2 WITH LIMIT :limit", AnnotatedAllTypes.class);

        recs.setParameter("limit", cnt / 2);

        int reccnt = 0;
        for (final AnnotatedAllTypes rec : recs.getResults())
            assertTrue(rec.equals(vals.get(reccnt++)));

        assertTrue(reccnt == cnt / 2);
    }
}
