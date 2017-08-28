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
