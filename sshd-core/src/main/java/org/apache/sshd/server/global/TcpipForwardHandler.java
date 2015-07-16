/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sshd.server.global;

import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.SshdSocketAddress;
import org.apache.sshd.common.forward.TcpipForwarder;
import org.apache.sshd.common.session.ConnectionService;
import org.apache.sshd.common.session.ConnectionServiceRequestHandler;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.Int2IntFunction;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.logging.AbstractLoggingBean;

/**
 * Handler for tcpip-forward global request.
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class TcpipForwardHandler extends AbstractLoggingBean implements ConnectionServiceRequestHandler {
    public static final String REQUEST = "tcpip-forward";
    public static final TcpipForwardHandler INSTANCE = new TcpipForwardHandler();

    public TcpipForwardHandler() {
        super();
    }

    @Override
    public Result process(ConnectionService connectionService, String request, boolean wantReply, Buffer buffer) throws Exception {
        if (REQUEST.equals(request)) {
            String address = buffer.getString();
            int port = buffer.getInt();
            SshdSocketAddress socketAddress = new SshdSocketAddress(address, port);
            TcpipForwarder forwarder = connectionService.getTcpipForwarder();
            SshdSocketAddress bound = forwarder.localPortForwardingRequested(socketAddress);
            if (log.isDebugEnabled()) {
                log.debug("process(" + connectionService + ")[" + request + "] " + socketAddress + " => " + bound + ", reply=" + wantReply);
            }

            port = bound.getPort();
            if (wantReply){
                buffer.clear();
                // leave room for the SSH header
                buffer.ensureCapacity(5 + 1 + (Integer.SIZE / Byte.SIZE), Int2IntFunction.Utils.add(Byte.SIZE));
                buffer.rpos(5);
                buffer.wpos(5);
                buffer.putByte(SshConstants.SSH_MSG_REQUEST_SUCCESS);
                buffer.putInt(port);

                Session session = connectionService.getSession();
                session.writePacket(buffer);
            }
            return Result.Replied;
        }

        return Result.Unsupported;
    }

}
