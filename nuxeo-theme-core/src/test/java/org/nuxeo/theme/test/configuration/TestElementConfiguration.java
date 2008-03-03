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

import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.NXRuntimeTestCase;
import org.nuxeo.theme.elements.ElementType;
import org.nuxeo.theme.nodes.NodeTypeFamily;
import org.nuxeo.theme.services.ThemeService;
import org.nuxeo.theme.types.TypeFamily;
import org.nuxeo.theme.types.TypeRegistry;

public class TestElementConfiguration extends NXRuntimeTestCase {

    private ElementType element1;

    private ElementType element2;

    private TypeRegistry typeRegistry;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deploy("nxthemes-core-service.xml");
        deploy("nxthemes-core-contrib.xml");
        deploy("element-config.xml");
        ThemeService themeService = (ThemeService) Framework.getRuntime().getComponent(
                ThemeService.ID);
        typeRegistry = (TypeRegistry) themeService.getRegistry("types");
    }

    public void testRegisterElement1() throws Exception {
        // element 1
        element1 = (ElementType) typeRegistry.lookup(TypeFamily.ELEMENT,
                "test element 1");
        assertNotNull(element1);
        assertEquals("test element 1", element1.getTypeName());
        assertEquals(NodeTypeFamily.INNER, element1.getNodeTypeFamily());
        assertEquals("org.nuxeo.theme.test.DummyElement",
                element1.getClassName());
    }

    public void testRegisterElement2() throws Exception {
        // element 2
        element2 = (ElementType) typeRegistry.lookup(TypeFamily.ELEMENT,
                "test element 2");
        assertNotNull(element2);
        assertEquals("test element 2", element2.getTypeName());
        assertEquals(NodeTypeFamily.LEAF, element2.getNodeTypeFamily());
        assertEquals("org.nuxeo.theme.test.DummyElement",
                element2.getClassName());
    }

}
