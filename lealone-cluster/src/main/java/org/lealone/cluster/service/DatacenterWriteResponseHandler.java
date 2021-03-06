/*
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
package org.lealone.cluster.service;

import java.net.InetAddress;
import java.util.Collection;

import org.lealone.cluster.db.ConsistencyLevel;
import org.lealone.cluster.db.Keyspace;
import org.lealone.cluster.db.WriteType;
import org.lealone.cluster.net.MessageIn;

/**
 * This class blocks for a quorum of responses _in the local datacenter only_ (CL.LOCAL_QUORUM).
 */
public class DatacenterWriteResponseHandler extends WriteResponseHandler {
    public DatacenterWriteResponseHandler(Collection<InetAddress> naturalEndpoints, Collection<InetAddress> pendingEndpoints,
            ConsistencyLevel consistencyLevel, Keyspace keyspace, Runnable callback, WriteType writeType) {
        super(naturalEndpoints, pendingEndpoints, consistencyLevel, keyspace, callback, writeType);
        assert consistencyLevel.isDatacenterLocal();
    }

    @Override
    public void response(MessageIn message) {
        if (message == null || consistencyLevel.isLocal(message.from))
            super.response(message);
    }

    @Override
    protected int totalBlockFor() {
        // during bootstrap, include pending endpoints (only local here) in the count
        // or we may fail the consistency level guarantees (see #833, #8058)
        return consistencyLevel.blockFor(keyspace) + consistencyLevel.countLocalEndpoints(pendingEndpoints);
    }
}
