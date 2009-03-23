/*
 * (C) Copyright 2006-2009 Nuxeo SAS <http://nuxeo.com> and others
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

package org.nuxeo.theme.editor;

import java.util.List;

import org.nuxeo.theme.Manager;
import org.nuxeo.theme.properties.FieldInfo;
import org.nuxeo.theme.vocabularies.VocabularyItem;

public class FieldProperty {
    private final String name;

    private final String value;

    private final FieldInfo info;

    public FieldProperty(String name, String value, FieldInfo info) {
        this.name = name;
        this.value = value;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getRendered() {
        final StringBuilder rendered = new StringBuilder();

        // label
        final String label = info == null ? name : info.label();
        rendered.append("<label>").append(label);

        // description
        final String description = info == null ? "" : info.description();
        if (!"".equals(description)) {
            rendered.append(String.format(
                    "<span class=\"description\">%s</span>", description));
        }
        rendered.append("</label>");

        // widget
        final String type = info == null ? "" : info.type();
        if ("text area".equals(type)) {
            rendered.append(String.format(
                    "<textarea name=\"%s\" class=\"fieldInput\">%s</textarea>",
                    name, value));

        } else if ("string".equals(type)) {
            rendered.append(String.format(
                    "<input type=\"text\" class=\"textInput fieldInput\" name=\"%s\" value=\"%s\" />",
                    name, value));

        } else if ("selection".equals(type)) {
            String source = info.source();
            if (!source.equals("")) {
                List<VocabularyItem> items = Manager.getVocabularyManager().getItems(
                        source);
                if (items != null) {
                    rendered.append(String.format(
                            "<select class=\"fieldInput\" name=\"%s\">", name));
                    boolean found = false;
                    for (VocabularyItem item : items) {
                        final String itemValue = item.getValue();
                        if (itemValue.equals(value)) {
                            rendered.append(String.format(
                                    "<option selected=\"selected\" value=\"%s\">%s</option>",
                                    itemValue, item.getLabel()));
                            found = true;
                        } else {
                            rendered.append(String.format(
                                    "<option value=\"%s\">%s</option>",
                                    itemValue, item.getLabel()));
                        }
                    }
                    if (!found) {
                        rendered.append(String.format(
                                "<option>Invalid option: %s</option>", value));
                    }
                    rendered.append("</select>");
                }
            }
        }

        if (info.required() && value.equals("")) {
            rendered.append("<span style=\"color: red\"> * </span>");
        }

        return rendered.toString();
    }
}
