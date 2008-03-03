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

package org.nuxeo.theme.test;

import org.nuxeo.theme.models.AbstractModel;
import org.nuxeo.theme.nodes.NodeTypeFamily;

public class DummyModel extends AbstractModel {

    @Override
    public String getModelTypeName() {
        return "model 1";
    }

    @Override
    public NodeTypeFamily getNodeTypeFamily() {
        return NodeTypeFamily.LEAF;
    }

}
