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

package org.nuxeo.theme.test.jsf.editor.managers;

import java.util.List;

import org.nuxeo.runtime.test.NXRuntimeTestCase;
import org.nuxeo.theme.jsf.editor.managers.UiManager;
import org.nuxeo.theme.views.ViewType;

public class TestUIManager extends NXRuntimeTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployContrib("nxthemes-core-service.xml");
        deployContrib("nxthemes-core-contrib.xml");
        deployContrib("nxthemes-jsf-contrib.xml");
        deployContrib("fragment-config.xml");
    }

    public void testGetList() {
        UiManager uiManager = new UiManager();
        List<UiManager.FragmentInfo> fragments = uiManager.getAvailableFragments();

        UiManager.FragmentInfo fragmentInfo = fragments.get(0);
        List<ViewType> viewTypes = fragmentInfo.getViews();

        assertEquals("dummy fragment",
                fragmentInfo.getFragmentType().getTypeName());
        assertEquals("vertical menu", viewTypes.get(0).getViewName());
        assertEquals("horizontal tabs", viewTypes.get(1).getViewName());
    }

}
