/*
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
 *
 */
package org.openmuc.jdlms.app.client;

import java.io.IOException;

import org.openmuc.jdlms.internal.cli.CliParseException;

public final class ConsoleClient {

    private static GenActionProcessor actionProcessor;

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (actionProcessor != null) {
                    actionProcessor.close();
                }
            }
        });

        ConsoleClientCliParser clientLineParser = new ConsoleClientCliParser();
        try {
            clientLineParser.parse(args);
            actionProcessor = clientLineParser.connectAndCreateConsoleApp();
        } catch (CliParseException e) {
            System.err.println("Error parsing command line parameters: " + e.getMessage());
            clientLineParser.printUsage();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error connecting to meter: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        actionProcessor.start();
    }

}
