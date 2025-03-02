/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.net;

import org.apache.log4j.spi.Decoder;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.chainsaw.ChainsawReceiverSkeleton;
import org.apache.log4j.chainsaw.logevents.ChainsawLoggingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Multicast-based receiver.  Accepts LoggingEvents encoded using
 * MulticastAppender and XMLLayout. The the XML data is converted
 * back to a LoggingEvent and is posted.
 *
 * @author Scott Deboy &lt;sdeboy@apache.org&gt;
 */
public class MulticastReceiver extends ChainsawReceiverSkeleton implements PortBased,
    AddressBased {
    private static final int PACKET_LENGTH = 16384;
    private int port;
    private String address;
    private String encoding;
    private MulticastSocket socket = null;

    //default to log4j xml decoder
    private String decoder = "org.apache.log4j.xml.XMLDecoder";
    private Decoder decoderImpl;
    private MulticastReceiverThread receiverThread;
    private boolean active = false;

    private static final Logger logger = LogManager.getLogger();

    /**
     * The MulticastDNS zone advertised by a MulticastReceiver
     */
    public static final String ZONE = "_log4j_xml_mcast_receiver.local.";

    public String getDecoder() {
        return decoder;
    }

    public void setDecoder(String decoder) {
        this.decoder = decoder;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    /**
     * The <b>Encoding</b> option specifies how the bytes are encoded.  If this option is not specified,
     * the system encoding will be used.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns value of the <b>Encoding</b> option.
     */
    public String getEncoding() {
        return encoding;
    }

    public synchronized void shutdown() {
        active = false;
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        if (socket != null) {
            socket.close();
        }
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public void start() {
        InetAddress addr = null;

        try {
            Class c = Class.forName(decoder);
            Object o = c.newInstance();

            if (o instanceof Decoder) {
                this.decoderImpl = (Decoder) o;
            }
        } catch (ClassNotFoundException cnfe) {
            logger.warn("Unable to find decoder", cnfe);
        } catch (IllegalAccessException | InstantiationException iae) {
            logger.warn("Could not construct decoder", iae);
        }

        try {
            addr = InetAddress.getByName(address);
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }

        try {
            active = true;
            socket = new MulticastSocket(port);
            socket.joinGroup(addr);
            receiverThread = new MulticastReceiverThread();
            receiverThread.start();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    class MulticastHandlerThread extends Thread {
        private final List<String> list = new ArrayList<>();

        public MulticastHandlerThread() {
            setDaemon(true);
        }

        public void append(String data) {
            synchronized (list) {
                list.add(data);
                list.notify();
            }
        }

        public void run() {
            ArrayList<String> list2 = new ArrayList<>();

            while (isAlive()) {
                synchronized (list) {
                    try {
                        while (list.size() == 0) {
                            list.wait();
                        }

                        if (list.size() > 0) {
                            list2.addAll(list);
                            list.clear();
                        }
                    } catch (InterruptedException ie) {
                    }
                }

                if (list2.size() > 0) {

                    for (Object aList2 : list2) {
                        String data = (String) aList2;
                        
                    }

                    list2.clear();
                } else {
                    try {
                        synchronized (this) {
                            wait(1000);
                        }
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
    }

    class MulticastReceiverThread extends Thread {
        public MulticastReceiverThread() {
            setDaemon(true);
        }

        public void run() {
            active = true;

            byte[] b = new byte[PACKET_LENGTH];
            DatagramPacket p = new DatagramPacket(b, b.length);

            while (active) {
                try {
                    socket.receive(p);

                    //this string constructor which accepts a charset throws an exception if it is
                    //null
                    String data;
                    if (encoding == null) {
                        data =
                            new String(p.getData(), 0, p.getLength());
                    } else {
                        data =
                            new String(p.getData(), 0, p.getLength(), encoding);
                    }

                    List<ChainsawLoggingEvent> v = decoderImpl.decodeEvents(data.trim());

                    if (v != null) {

                        for (ChainsawLoggingEvent aV : v) {
                            append(aV);
                        }
                    }
                } catch (SocketException se) {
                    //disconnected
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            logger.debug("{}'s thread is ending.", MulticastReceiver.this.getName());
        }
    }
}
