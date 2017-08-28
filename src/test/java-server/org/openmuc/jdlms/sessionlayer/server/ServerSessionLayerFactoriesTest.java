package org.openmuc.jdlms.sessionlayer.server;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ServerSessionLayerFactoriesTest {
    @Test
    public void testHdlc() throws Exception {
        ServerSessionLayerFactory factory = ServerSessionLayerFactories.newHdlcSessionLayerFactory();

        ServerSessionLayer layer = factory.newSesssionLayer(null, null);
        assertThat(layer, instanceOf(ServerHdlcSessionLayer.class));
    }

    @Test
    public void testWrapper() throws Exception {
        ServerSessionLayerFactory factory = ServerSessionLayerFactories.newWrapperSessionLayerFactory();

        ServerSessionLayer layer = factory.newSesssionLayer(null, null);
        assertThat(layer, instanceOf(ServerWrapperLayer.class));
    }
}
