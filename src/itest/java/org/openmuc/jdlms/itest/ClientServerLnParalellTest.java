/**
 * Copyright 2012-17 Fraunhofer ISE
 * 
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 * 
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.jdlms.itest;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.DlmsServer;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.TcpConnectionBuilder;

public class ClientServerLnParalellTest {

    private static final int C_CLIENTS = 5;
    private static final long DELAY_BTW_CONN = 5L;

    private static DlmsServer server;

    @BeforeClass
    public static void setupServer() throws Exception {
        LogicalDevice logicalDevice1 = new LogicalDevice(1, "LDI", "ISE", 9999L);
        server = DlmsServer.tcpServerBuilder().setMaxClients(C_CLIENTS).registerLogicalDevice(logicalDevice1).build();
    }

    @AfterClass
    public static void closeServer() throws Exception {
        server.close();
    }

    @Ignore
    @Test(timeout = 1000)
    public void testAllClientsParallel() throws Throwable {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(C_CLIENTS + 1);

        List<Future<Void>> futures = new ArrayList<>(C_CLIENTS + 1);
        for (int i = 0; i < C_CLIENTS; i++) {
            futures.add(fixedThreadPool.submit(new R1()));
            Thread.sleep(DELAY_BTW_CONN);
        }

        Thread.sleep(10l);
        futures.add(fixedThreadPool.submit(new R1()));

        ListIterator<Future<Void>> listIter = futures.listIterator();

        try {
            while (listIter.hasNext()) {
                Future<Void> future = listIter.next();
                try {
                    future.get();

                    // should be true execept the last client
                    Assert.assertTrue(listIter.hasNext());
                } catch (ExecutionException e) {
                    if (listIter.hasNext()) {
                        String message = "Failed index " + (listIter.nextIndex() - 1);
                        throw new AssertionError(message, e.getCause());
                    }
                    else {
                        assertThat(e.getCause(), instanceOf(IOException.class));
                    }
                }
            }
        } finally {
            fixedThreadPool.shutdown();
        }
    }

    private class R1 implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            DlmsConnection conn = new TcpConnectionBuilder("localhost").build();
            Thread.sleep(15 + DELAY_BTW_CONN * C_CLIENTS);
            conn.close();
            return null;
        }

    }

}
