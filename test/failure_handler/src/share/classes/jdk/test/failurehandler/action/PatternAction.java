/*
 * Copyright (c) 2015, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.test.failurehandler.action;

import jdk.test.failurehandler.value.InvalidValueException;
import jdk.test.failurehandler.HtmlSection;
import jdk.test.failurehandler.value.Value;
import jdk.test.failurehandler.value.ValueHandler;

import java.util.Properties;

public class PatternAction implements Action {
    @Value(name = "pattern")
    private String pattern = null;

    private final SimpleAction action;
    private final String[] originalArgs;
    private final String originalSuccessArtifacts;

    public PatternAction(String id, Properties properties)
            throws InvalidValueException {
        this(id, id, properties);
    }

    public PatternAction(String name, String id, Properties properties)
            throws InvalidValueException {
        action = new SimpleAction(name != null ? ("pattern." + name) : "pattern", id, properties);
        ValueHandler.apply(this, properties, id);
        originalArgs = action.args.clone();
        ActionParameters params = action.getParameters();
        // just like the "args" the "successArtifacts" param can also contain pattern that
        // this PatternAction will (sometimes repeatedly) replace, so we keep track of
        // the original (un-replaced text)
        originalSuccessArtifacts = params == null ? null : params.successArtifacts;
    }

    public ProcessBuilder prepareProcess(HtmlSection section,
                                         ActionHelper helper, String value) {
        action.sections[0] = value;
        section = getSection(section);
        String[] args = action.args;
        System.arraycopy(originalArgs, 0, args, 0, originalArgs.length);

        for (int i = 0, n = args.length; i < n; ++i) {
            args[i] = args[i].replace(pattern, value) ;
        }
        for (int i = 0, n = args.length; i < n; ++i) {
            args[i] = args[i].replace("%java", helper.findApp("java").getAbsolutePath());
        }
        // replace occurrences of the pattern in the "successArtifacts" param
        if (originalSuccessArtifacts != null) {
            action.getParameters().successArtifacts = originalSuccessArtifacts.replaceAll(pattern,
                    value);
        }
        return action.prepareProcess(section.getWriter(), helper);
    }

    @Override
    public HtmlSection getSection(HtmlSection section) {
        return action.getSection(section);
    }

    @Override
    public ActionParameters getParameters() {
        return action.getParameters();
    }

    @Override
    public boolean isJavaOnly() {
        return action.isJavaOnly();
    }
}
