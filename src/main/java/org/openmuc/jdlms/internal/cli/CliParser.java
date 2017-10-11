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
package org.openmuc.jdlms.internal.cli;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CliParser {

    private final String name;
    private final String description;
    private String selectedGroup = "";

    private static final String HELP = "--help";

    private static class ParameterGroup {
        private final String name;
        private final List<? extends CliParameter> parameters;

        public ParameterGroup(String name, List<? extends CliParameter> parameters) {
            this.name = name;
            this.parameters = parameters;
        }
    }

    private final List<ParameterGroup> commandLineParameterGroups = new ArrayList<>();

    public CliParser(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addParameterGroup(String groupName, List<? extends CliParameter> parameters) {
        commandLineParameterGroups.add(new ParameterGroup(groupName.toLowerCase(), parameters));
    }

    public void addParameters(List<? extends CliParameter> parameters) {
        commandLineParameterGroups.clear();
        commandLineParameterGroups.add(new ParameterGroup("", parameters));
    }

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void parseArguments(String[] args) throws CliParseException {

        if (args.length > 0 && HELP.equals(args[0])) {
            System.out.println(getUsageString());
            System.exit(0);
        }

        List<? extends CliParameter> parameters = null;

        int i = 0;
        if (commandLineParameterGroups.get(0).name.isEmpty()) {
            parameters = commandLineParameterGroups.get(0).parameters;
        }
        else {
            if (args.length == 0) {
                throw new CliParseException("No parameters found.");
            }
            for (ParameterGroup parameterGroup : commandLineParameterGroups) {
                if (parameterGroup.name.equals(args[0].toLowerCase())) {
                    selectedGroup = parameterGroup.name;
                    parameters = parameterGroup.parameters;
                }
            }
            if (parameters == null) {
                throw new CliParseException("Group name " + args[0] + " is undefined.");
            }
            i++;
        }

        while (i < args.length) {
            boolean found = false;
            for (CliParameter option : parameters) {
                if (args[i].equals(option.getName())) {
                    i += option.parse(args, i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new CliParseException("Unknown parameter found: " + args[i]);
            }
        }

        for (CliParameter option : parameters) {
            if (!option.isOptional() && !option.isSelected()) {
                throw new CliParseException("Parameter " + option.getName() + " is mandatory but was not selected.");
            }
        }
    }

    public String getUsageString() {

        StringBuilder sb = new StringBuilder();
        sb.append("NAME\n\t").append(name).append(" - ").append(description).append("\n\nSYNOPSIS\n");

        for (ParameterGroup parameterGroup : commandLineParameterGroups) {
            sb.append("\t").append(name).append(" ").append(parameterGroup.name);

            int characterColumn = name.length() + parameterGroup.name.length() + 1;

            for (CliParameter parameter : parameterGroup.parameters) {
                if ((characterColumn + parameter.appendSynopsis(new StringBuilder())) > 90) {
                    characterColumn = 0;
                    sb.append("\n\t    ");
                }
                sb.append(' ');
                characterColumn += parameter.appendSynopsis(sb) + 1;
            }
            sb.append("\n");
        }

        sb.append("\nOPTIONS\n");

        Set<CliParameter> parameters = new LinkedHashSet<>();

        for (ParameterGroup parameterGroup : commandLineParameterGroups) {
            parameters.addAll(parameterGroup.parameters);
        }

        for (CliParameter parameter : parameters) {
            sb.append(' ');
            parameter.appendDescription(sb);
            sb.append("\n\n");
        }

        sb.append("\t--help display this help and exit");

        return sb.toString();
    }

}
