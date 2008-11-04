/*
 * (C) Copyright 2006-2008 Nuxeo SAS <http://nuxeo.com> and others
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

package org.nuxeo.theme.webengine.negotiation.perspective;

import org.nuxeo.ecm.webengine.model.WebContext;
import org.nuxeo.theme.negotiation.Scheme;
import org.nuxeo.theme.perspectives.PerspectiveManager;
import org.nuxeo.theme.webengine.negotiation.CookieManager;

public class CookieValue implements Scheme {

    public String getOutcome(final Object context) {
        final String perspectiveName = CookieManager.getCookie("nxthemes.perspective",
                ((WebContext) context));
        if (perspectiveName == null) {
            return null;
        }
        if (PerspectiveManager.hasPerspective(perspectiveName)) {
            return perspectiveName;
        }
        return null;
    }

}
