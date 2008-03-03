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

package org.nuxeo.theme.models;

import org.nuxeo.theme.Manager;
import org.nuxeo.theme.nodes.AbstractNode;
import org.nuxeo.theme.types.TypeFamily;

public abstract class AbstractModel extends AbstractNode implements Model {

    public abstract String getModelTypeName();

    public ModelType getModelType() {
        return (ModelType) Manager.getTypeRegistry().lookup(TypeFamily.MODEL,
                getModelTypeName());
    }

}
