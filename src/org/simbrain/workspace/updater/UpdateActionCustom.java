/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.workspace.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.function.Function;

import org.simbrain.workspace.Workspace;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * Update using a custom action saved as a beanshell script.
 *
 * @author jyoshimi
 */
public class UpdateActionCustom implements UpdateAction {

    /** Provides access to workspace updater. */
    private final WorkspaceUpdater updater;

    /** The custom update script in persistable string form. */
    private String scriptString;

    /**
     * The interpreter for converting the the script into an executable update
     * action.
     */
    private Interpreter interpreter = new Interpreter();

    /** Custom update action. */
    private UpdateAction theAction;

    /**
     * Create a new custom update action from a file containing the custom
     * script.
     *
     * @param updater reference to workspace updater
     * @param script the custom script as a string
     */
    public UpdateActionCustom(final WorkspaceUpdater updater,
            final String script) {
        this.updater = updater;
        this.scriptString = script;
        init();
    }


    /**
     * Create a new custom update action from a file containing the custom
     * script.
     *
     * @param workspace reference to parent workspace
     * @param file file containing custom code
     */
    public UpdateActionCustom(final Workspace workspace,
            final File file) {
        this.updater = workspace.getUpdater();
        StringBuilder scriptText = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(file));
            while (scanner.hasNextLine()) {
                scriptText.append(scanner.nextLine() + newLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
        this.scriptString = scriptText.toString();
        init();
    }



    /**
     * Initialize the interpreter.
     */
    public void init() {
        if (interpreter == null) {
            interpreter = new Interpreter();
        }
        try {
            interpreter.set("updater", updater);
            interpreter.set("workspace", updater.getWorkspace());
            interpreter.eval(scriptString);
            theAction = ((UpdateAction) interpreter.get("action"));
        } catch (EvalError e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void invoke() {
        theAction.invoke();
    }

    @Override
    public String getDescription() {
        return theAction.getDescription();
    }

    @Override
    public String getLongDescription() {
        return theAction.getLongDescription();
    }

    /**
     * @return the scriptString
     */
    public String getScriptString() {
        return scriptString;
    }

    /**
     * @param scriptString the scriptString to set
     */
    public void setScriptString(String scriptString) {
        this.scriptString = scriptString;
    }

}
