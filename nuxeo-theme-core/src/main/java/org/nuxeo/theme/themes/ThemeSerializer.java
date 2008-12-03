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

package org.nuxeo.theme.themes;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.theme.Manager;
import org.nuxeo.theme.Utils;
import org.nuxeo.theme.elements.Element;
import org.nuxeo.theme.elements.ElementFormatter;
import org.nuxeo.theme.formats.Format;
import org.nuxeo.theme.formats.styles.Style;
import org.nuxeo.theme.fragments.Fragment;
import org.nuxeo.theme.nodes.Node;
import org.nuxeo.theme.perspectives.PerspectiveType;
import org.nuxeo.theme.properties.FieldIO;
import org.nuxeo.theme.uids.Identifiable;
import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class ThemeSerializer {

    private static final Log log = LogFactory.getLog(ThemeSerializer.class);

    private static final String DOCROOT_NAME = "theme";

    private Document doc;

    private List<Element> elements;

    public Document serialize(final Element theme) throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false);
        } catch (ParserConfigurationException e) {
            log.debug("Could not set DTD non-validation feature");
        }
        final DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.newDocument();
        elements = new ArrayList<Element>();
        final org.w3c.dom.Element root = doc.createElement(DOCROOT_NAME);
        final ThemeManager themeManager = Manager.getThemeManager();

        // Theme description and name
        String description = theme.getDescription();
        if (description != null) {
            doc.appendChild(doc.createComment(String.format(" %s ", description)));
        }
        root.setAttribute("name", theme.getName());
        doc.appendChild(root);

        // layout
        final org.w3c.dom.Element layoutNode = doc.createElement("layout");
        root.appendChild(layoutNode);

        for (Node page : theme.getChildren()) {
            serializeLayout((Element) page, layoutNode);
        }

        // element properties
        for (Element element : elements) {
            serializeProperties(element, root);
        }

        // formats
        final org.w3c.dom.Element formatNode = doc.createElement("formats");
        root.appendChild(formatNode);

        final String themeName = theme.getName();
        for (String formatTypeName : themeManager.getFormatTypeNames()) {
            // export named styles
            for (Identifiable object : themeManager.getNamedObjects(themeName,
                    formatTypeName)) {
                serializeFormat((Format) object, formatNode);
            }
            for (Format format : themeManager.getFormatsByTypeName(formatTypeName)) {
                // make sure that the format is used by this theme
                boolean isUsedByThisTheme = false;
                for (Element element : ElementFormatter.getElementsFor(format)) {
                    if (element.isChildOf(theme) || element == theme) {
                        isUsedByThisTheme = true;
                        break;
                    }
                }
                if (isUsedByThisTheme) {
                    serializeFormat(format, formatNode);
                }
            }
        }
        return doc;
    }

    private void serializeProperties(final Element parent,
            final org.w3c.dom.Element domParent) throws Exception {
        final org.w3c.dom.Element domProperties = doc.createElement("properties");
        domProperties.setAttribute("element", parent.computeXPath());
        for (Map.Entry<Object, Object> entry : FieldIO.dumpFieldsToProperties(
                parent).entrySet()) {
            final org.w3c.dom.Element domProperty = doc.createElement((String) entry.getKey());
            final String value = (String) entry.getValue();
            domProperty.appendChild(doc.createTextNode(Utils.cleanUp(value)));
            domProperties.appendChild(domProperty);
        }
        if (domProperties.hasChildNodes()) {
            domParent.appendChild(domProperties);
        }
    }

    private void serializeLayout(final Element parent,
            final org.w3c.dom.Element domParent) {
        final String typeName = parent.getElementType().getTypeName();
        final org.w3c.dom.Element domElement = doc.createElement(typeName);

        elements.add(parent);

        final String elementName = parent.getName();
        if (elementName != null) {
            domElement.setAttribute("name", elementName);
        }

        if (parent instanceof Fragment) {
            domElement.setAttribute("type",
                    ((Fragment) parent).getFragmentType().getTypeName());

            // perspectives
            final StringBuilder s = new StringBuilder();
            final Iterator<PerspectiveType> it = ((Fragment) parent).getVisibilityPerspectives().iterator();
            while (it.hasNext()) {
                PerspectiveType perspective = it.next();
                s.append(perspective.getTypeName());
                if (it.hasNext()) {
                    s.append(",");
                }
            }
            if (s.length() > 0) {
                domElement.setAttribute("perspectives", s.toString());
            }
        }

        String description = parent.getDescription();
        if (description != null) {
            domParent.appendChild(doc.createComment(String.format(" %s ",
                    description)));
        }

        domParent.appendChild(domElement);
        for (Node child : parent.getChildren()) {
            serializeLayout((Element) child, domElement);
        }
    }

    private void serializeFormat(final Format format,
            final org.w3c.dom.Element domParent) {
        final String typeName = format.getFormatType().getTypeName();
        final org.w3c.dom.Element domElement = doc.createElement(typeName);

        final String description = format.getDescription();
        if (description != null) {
            domParent.appendChild(doc.createComment(String.format(" %s ",
                    description)));
        }

        StringBuilder s = new StringBuilder();
        Iterator<Element> iter = ElementFormatter.getElementsFor(format).iterator();
        boolean hasElement = iter.hasNext();
        while (iter.hasNext()) {
            Element element = iter.next();
            s.append(element.computeXPath());
            if (iter.hasNext()) {
                s.append("|");
            }
        }
        if (hasElement) {
            domElement.setAttribute("element", s.toString());
        }

        // widgets
        if ("widget".equals(typeName)) {
            // view name
            String viewName = format.getName();
            org.w3c.dom.Element domView = doc.createElement("view");
            domView.appendChild(doc.createTextNode(viewName));
            domElement.appendChild(domView);

            // properties
            Properties properties = format.getProperties();
            Enumeration<?> names = properties.propertyNames();

            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                if ("view".equals(name)) {
                    continue;
                }
                String value = properties.getProperty(name);
                org.w3c.dom.Element domAttr = doc.createElement(name);
                domAttr.appendChild(doc.createTextNode(Utils.cleanUp(value)));
                domElement.appendChild(domAttr);
            }
        }

        // layout
        else if ("layout".equals(typeName)) {
            Properties properties = format.getProperties();
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                String value = properties.getProperty(name);
                org.w3c.dom.Element domView = doc.createElement(name);
                domView.appendChild(doc.createTextNode(Utils.cleanUp(value)));
                domElement.appendChild(domView);
            }
        }

        // style
        else if ("style".equals(typeName)) {
            Style style = (Style) format;
            String styleName = style.getName();
            Style ancestor = (Style) ThemeManager.getAncestorFormatOf(style);
            if (styleName != null) {
                domElement.setAttribute("name", styleName);
            }
            if (ancestor != null) {
                domElement.setAttribute("inherit", ancestor.getName());
            }
            for (String viewName : style.getSelectorViewNames()) {
                for (String path : style.getPathsForView(viewName)) {
                    org.w3c.dom.Element domSelector = doc.createElement("selector");
                    path = Utils.cleanUp(path);
                    domSelector.setAttribute("path", path);
                    if (!"*".equals(viewName)) {
                        domSelector.setAttribute("view", viewName);
                    }

                    for (Map.Entry<Object, Object> entry : style.getPropertiesFor(
                            viewName, path).entrySet()) {
                        org.w3c.dom.Element domProperty = doc.createElement((String) entry.getKey());
                        String value = (String) entry.getValue();
                        value = value.trim();
                        Matcher presetMatcher = ThemeManager.PRESET_PATTERN.matcher(value);
                        if (presetMatcher.find()) {
                            domProperty.setAttribute("preset",
                                    presetMatcher.group(1));
                        } else {
                            domProperty.appendChild(doc.createTextNode(Utils.cleanUp(value)));
                        }
                        domSelector.appendChild(domProperty);
                    }

                    // Set selector description
                    String selectorDescription = style.getSelectorDescription(
                            path, viewName);
                    if (selectorDescription != null) {
                        domElement.appendChild(doc.createComment(String.format(
                                " %s ", selectorDescription)));
                    }

                    domElement.appendChild(domSelector);
                }
            }
        }
        domParent.appendChild(domElement);
    }

    public String serializeToXml(final Element theme) {
        return serializeToXml(theme, 0);
    }

    public String serializeToXml(final Element theme, final int indent) {
        String xml = null;
        try {
            // serialize the theme into a document
            serialize(theme);
            // convert the document to XML
            StringWriter sw = new StringWriter();
            OutputFormat format = new OutputFormat(doc);
            format.setIndenting(true);
            format.setIndent(indent);
            Writer output = new BufferedWriter(sw);
            XMLSerializer serializer = new XMLSerializer(output, format);
            serializer.serialize(doc);
            xml = sw.toString();
        } catch (Exception e) {
            log.error(e);
        }
        return xml;
    }

}
