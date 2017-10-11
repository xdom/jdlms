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
