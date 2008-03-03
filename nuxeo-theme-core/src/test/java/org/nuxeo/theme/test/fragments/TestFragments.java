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

package org.nuxeo.theme.test.fragments;

import org.nuxeo.runtime.test.NXRuntimeTestCase;
import org.nuxeo.theme.fragments.Fragment;
import org.nuxeo.theme.fragments.FragmentFactory;
import org.nuxeo.theme.perspectives.PerspectiveType;

public class TestFragments extends NXRuntimeTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deploy("nxthemes-core-service.xml");
        deploy("nxthemes-core-contrib.xml");
        deploy("fragment-config.xml");
    }

    public void testVisibility() {
        Fragment fragment = FragmentFactory.create("dummy fragment");
        PerspectiveType perspective1 = new PerspectiveType("view_mode", "View mode");
        PerspectiveType perspective2 = new PerspectiveType("edit_mode", "Edit mode");

        // fragments are visible in all perspectives by default
        assertTrue(fragment.isVisibleInPerspective(perspective1));
        assertTrue(fragment.isVisibleInPerspective(perspective2));
        assertTrue(fragment.getVisibilityPerspectives().isEmpty());

        // make the fragment visible in perspective 1
        fragment.setVisibleInPerspective(perspective1);
        assertTrue(fragment.isVisibleInPerspective(perspective1));
        assertFalse(fragment.isVisibleInPerspective(perspective2));
        assertTrue(fragment.getVisibilityPerspectives().contains(perspective1));
        assertFalse(fragment.getVisibilityPerspectives().contains(perspective2));

        // make the fragment visible in perspective 2
        fragment.setVisibleInPerspective(perspective2);
        assertTrue(fragment.isVisibleInPerspective(perspective1));
        assertTrue(fragment.isVisibleInPerspective(perspective2));
        assertTrue(fragment.getVisibilityPerspectives().contains(perspective1));
        assertTrue(fragment.getVisibilityPerspectives().contains(perspective2));

        // make the fragment always visible
        fragment.setAlwaysVisible();
        assertTrue(fragment.isVisibleInPerspective(perspective1));
        assertTrue(fragment.isVisibleInPerspective(perspective2));
        assertTrue(fragment.getVisibilityPerspectives().isEmpty());
    }

}
