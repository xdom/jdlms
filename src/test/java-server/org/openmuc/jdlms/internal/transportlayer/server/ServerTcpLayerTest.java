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
package org.openmuc.jdlms.internal.transportlayer.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServerTcpLayer.class, Executors.class, ExecutorService.class })
public class ServerTcpLayerTest {

    @Test
    @Ignore
    public void testMaxPermits() throws Exception {
        // ServerSocket serverSocket = PowerMockito.mock(ServerSocket.class);
        // when(serverSocket.accept()).thenReturn(null, null, null, null);
        //
        // whenNew(ServerSocket.class).withAnyArguments().thenReturn(serverSocket);
        //
        // mockStatic(Executors.class);
        // ThreadPoolExecutor value = mock(ThreadPoolExecutor.class);
        // ExecutorService singleExec = mock(ExecutorService.class);
        //
        // doAnswer(new Answer<Void>() {
        // @Override
        // public Void answer(InvocationOnMock invocation) throws Throwable {
        // return null;
        // }
        // }).when(singleExec).execute(any(Runnable.class));
        //
        // when(Executors.newCachedThreadPool()).thenReturn(singleExec);
        //
        // when(Executors.newSingleThreadExecutor()).thenReturn(value);
        //
        // TcpServerSettings settings = mock(TcpServerSettings.class);
        // when(settings.getMaxClients()).thenReturn(3);
        //
        // DataDirectory dataDirectory = null;
        // ServerSessionLayerFactory sessionLayerFactory = null;
        // ServerTcpLayer serverTcpLayer = new ServerTcpLayer(settings, dataDirectory, sessionLayerFactory);
        // serverTcpLayer.start();
        //
        // serverTcpLayer.close();

    }

}
