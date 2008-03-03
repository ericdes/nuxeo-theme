/*
 * (C) Copyright 2006-2007 Nuxeo SAS <http://nuxeo.com> and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Marc Orliaguet, Chalmers
 *
 * $Id$
 */

package org.nuxeo.theme.test.configuration;

import java.io.IOException;

import org.nuxeo.runtime.test.NXRuntimeTestCase;

public class TestThemeConfiguration extends NXRuntimeTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deploy("nxthemes-core-contrib.xml");
        deploy("theme-config.xml");
    }

    public void testRegisterTheme() throws IOException {
        // TODO
    }

}
