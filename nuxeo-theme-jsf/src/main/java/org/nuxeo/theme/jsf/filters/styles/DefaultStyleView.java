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

package org.nuxeo.theme.jsf.filters.styles;

import java.util.List;

import org.nuxeo.theme.Manager;
import org.nuxeo.theme.elements.Element;
import org.nuxeo.theme.elements.ElementFormatter;
import org.nuxeo.theme.formats.Format;
import org.nuxeo.theme.formats.styles.Style;
import org.nuxeo.theme.formats.widgets.Widget;
import org.nuxeo.theme.jsf.Utils;
import org.nuxeo.theme.rendering.RenderingInfo;
import org.nuxeo.theme.themes.ThemeManager;
import org.nuxeo.theme.views.AbstractView;

public class DefaultStyleView extends AbstractView {

    @Override
    public String render(final RenderingInfo info) {
        final Style style = (Style) info.getFormat();
        final ThemeManager themeManager = Manager.getThemeManager();
        final StringBuilder sb = new StringBuilder();

        // add inherited styles first
        final List<Format> ancestors = themeManager.listAncestorFormatsOf(style);
        for (Format ancestor : ancestors) {
            sb.append(Utils.computeCssClassName(ancestor)).append(' ');
        }
        sb.append(Utils.computeCssClassName(style));

        // get the widget view name
        final Element element = info.getElement();
        final Widget widget = (Widget) ElementFormatter.getFormatFor(element,
                "widget");
        if (widget != null) {
            String className = Utils.toUpperCamelCase(widget.getName());
            sb.append(className);
        }
        return Utils.insertCssClass(info.getMarkup(), sb.toString());
    }
}
