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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.theme.Manager;
import org.nuxeo.theme.elements.Element;
import org.nuxeo.theme.elements.ElementFactory;
import org.nuxeo.theme.elements.ElementFormatter;
import org.nuxeo.theme.elements.PageElement;
import org.nuxeo.theme.elements.ThemeElement;
import org.nuxeo.theme.formats.Format;
import org.nuxeo.theme.formats.FormatFactory;
import org.nuxeo.theme.formats.styles.Style;
import org.nuxeo.theme.fragments.Fragment;
import org.nuxeo.theme.fragments.FragmentFactory;
import org.nuxeo.theme.nodes.NodeException;
import org.nuxeo.theme.perspectives.PerspectiveType;
import org.nuxeo.theme.presets.CustomPresetType;
import org.nuxeo.theme.presets.PresetManager;
import org.nuxeo.theme.presets.PresetType;
import org.nuxeo.theme.properties.FieldIO;
import org.nuxeo.theme.types.TypeFamily;
import org.nuxeo.theme.types.TypeRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ThemeParser {

    private static final Log log = LogFactory.getLog(ThemeParser.class);

    private static final String DOCROOT_NAME = "theme";

    private static final XPath xpath = XPathFactory.newInstance().newXPath();

    public static String registerTheme(final ThemeDescriptor themeDescriptor)
            throws ThemeIOException {
        final String src = themeDescriptor.getSrc();
        String themeName = null;
        URL url = null;
        InputStream in = null;
        try {
            url = new URL(src);
        } catch (MalformedURLException e) {
            url = Thread.currentThread().getContextClassLoader().getResource(
                    src);
        }

        if (url == null) {
            throw new ThemeIOException("Incorrect theme URL: " + src);
        }

        try {
            in = url.openStream();
            themeName = registerTheme(themeDescriptor, in);
        } catch (FileNotFoundException e) {
            throw new ThemeIOException("File not found: " + src, e);
        } catch (IOException e) {
            throw new ThemeIOException("Could not open file: " + src, e);
        } catch (ThemeException e) {
            throw new ThemeIOException("Parsing error: " + src, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e);
                } finally {
                    in = null;
                }
            }
        }
        return themeName;
    }

    private static String registerTheme(final ThemeDescriptor themeDescriptor,
            final InputStream in) throws ThemeIOException, ThemeException {
        String themeName = null;

        final InputSource is = new InputSource(in);
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false);
        } catch (ParserConfigurationException e) {
            log.debug("Could not set DTD non-validation feature");
        }
        final ThemeManager themeManager = Manager.getThemeManager();

        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ThemeIOException(e);
        }
        Document document;
        try {
            document = db.parse(is);
        } catch (SAXException e) {
            throw new ThemeIOException(e);
        } catch (IOException e) {
            throw new ThemeIOException(e);
        }
        final org.w3c.dom.Element docElem = document.getDocumentElement();
        if (!docElem.getNodeName().equals(DOCROOT_NAME)) {
            throw new ThemeIOException("No <" + DOCROOT_NAME
                    + "> document tag found in " + in.toString()
                    + ", ignoring the resource.");
        }

        themeName = docElem.getAttributes().getNamedItem("name").getNodeValue();
        if (!ThemeManager.validateThemeName(themeName)) {
            throw new ThemeIOException(
                    "Theme names may only contain lower-case alpha-numeric characters, underscores and hyphens: "
                            + themeName);
        }

        // remove old theme
        ThemeElement oldTheme = themeManager.getThemeByName(themeName);
        if (oldTheme != null) {
            try {
                themeManager.destroyElement(oldTheme);
            } catch (NodeException e) {
                throw new ThemeIOException("Failed to destroy theme: "
                        + themeName, e);
            }
        }

        Node baseNode = getBaseNode(docElem);

        // create a new theme
        ThemeElement theme = (ThemeElement) ElementFactory.create("theme");
        theme.setName(themeName);
        Node description = docElem.getAttributes().getNamedItem("description");
        if (description != null) {
            theme.setDescription(description.getNodeValue());
        }

        Node templateEngines = docElem.getAttributes().getNamedItem(
                "template-engines");
        if (templateEngines != null) {
            themeDescriptor.setTemplateEngines(Arrays.asList(templateEngines.getNodeValue().split(
                    ",")));
        }

        // register custom presets
        for (Node n : getChildElementsByTagName(docElem, "presets")) {
            parsePresets(theme, n);
        }

        // register formats
        for (Node n : getChildElementsByTagName(docElem, "formats")) {
            parseFormats(theme, docElem, n);
        }

        // register element properties
        for (Node n : getChildElementsByTagName(docElem, "properties")) {
            parseProperties(docElem, n);
        }

        // parse layout
        parseLayout(theme, baseNode);

        themeManager.registerTheme(theme);
        return themeName;

    }

    public static void parseLayout(final Element parent, Node node)
            throws ThemeIOException {
        TypeRegistry typeRegistry = Manager.getTypeRegistry();
        for (String formatName : typeRegistry.getTypeNames(TypeFamily.FORMAT)) {
            Object format = node.getUserData(formatName);
            if (format != null) {
                ElementFormatter.setFormat(parent, (Format) format);
            }
        }

        Properties properties = (Properties) node.getUserData("properties");
        if (properties != null) {
            FieldIO.updateFieldsFromProperties(parent, properties);
        }

        for (Node n : getChildElements(node)) {
            String nodeName = n.getNodeName();
            NamedNodeMap attributes = n.getAttributes();
            Element elem;

            if ("fragment".equals(nodeName)) {
                String fragmentType = attributes.getNamedItem("type").getNodeValue();
                elem = FragmentFactory.create(fragmentType);
                if (elem == null) {
                    log.error("Could not create fragment: " + fragmentType);
                    continue;
                }
                Fragment fragment = (Fragment) elem;
                Node perspectives = attributes.getNamedItem("perspectives");
                if (perspectives != null) {
                    for (String perspectiveName : perspectives.getNodeValue().split(
                            ",")) {

                        PerspectiveType perspective = (PerspectiveType) typeRegistry.lookup(
                                TypeFamily.PERSPECTIVE, perspectiveName);

                        if (perspective == null) {
                            log.warn("Could not find perspective: "
                                    + perspectiveName);
                        } else {
                            fragment.setVisibleInPerspective(perspective);
                        }
                    }
                }
            } else {
                elem = ElementFactory.create(nodeName);
            }

            if (elem == null) {
                throw new ThemeIOException("Could not parse node: " + nodeName);
            }

            if (elem instanceof PageElement) {
                String pageName = attributes.getNamedItem("name").getNodeValue();
                if (!pageName.matches("[a-z0-9_\\-]+")) {
                    throw new ThemeIOException(
                            "Page names may only contain lower-case alpha-numeric characters, digits, underscores and dashes.");
                }
                elem.setName(pageName);
            }

            String description = getCommentAssociatedTo(n);
            if (description != null) {
                elem.setDescription(description);
            }

            try {
                parent.addChild(elem);
            } catch (NodeException e) {
                throw new ThemeIOException("Failed to parse layout.", e);
            }
            parseLayout(elem, n);
        }
    }

    public static void parsePresets(final ThemeElement theme, Node node) {
        final TypeRegistry typeRegistry = Manager.getTypeRegistry();
        final String themeName = theme.getName();
        PresetManager.clearCustomPresets(themeName);
        for (Node n : getChildElements(node)) {
            NamedNodeMap attrs = n.getAttributes();
            final String name = attrs.getNamedItem("name").getNodeValue();
            final String category = attrs.getNamedItem("category").getNodeValue();
            final String value = PresetManager.resolvePresets(themeName,
                    n.getTextContent());
            final String group = theme.getName(); // use the theme's name as
            // group name
            PresetType preset = new CustomPresetType(name, value, group,
                    category);
            typeRegistry.register(preset);
        }
    }

    public static void parseFormats(final ThemeElement theme,
            org.w3c.dom.Element doc, Node node) throws ThemeIOException,
            ThemeException {
        Node baseNode = getBaseNode(doc);
        String themeName = theme.getName();
        ThemeManager themeManager = Manager.getThemeManager();

        Map<Style, Map<String, Properties>> newStyles = new LinkedHashMap<Style, Map<String, Properties>>();

        for (Node n : getChildElements(node)) {
            String nodeName = n.getNodeName();
            NamedNodeMap attributes = n.getAttributes();
            Node elementItem = attributes.getNamedItem("element");
            String elementXPath = null;
            if (elementItem != null) {
                elementXPath = elementItem.getNodeValue();
            }

            Format format;
            try {
                format = FormatFactory.create(nodeName);
            } catch (ThemeException e) {
                throw new ThemeIOException(e);
            }
            format.setProperties(getPropertiesFromNode(n));

            String description = getCommentAssociatedTo(n);
            if (description != null) {
                format.setDescription(description);
            }

            if ("widget".equals(nodeName)) {
                List<Node> viewNodes = getChildElementsByTagName(n, "view");
                if (!viewNodes.isEmpty()) {
                    format.setName(viewNodes.get(0).getTextContent());
                }

            } else if ("layout".equals(nodeName)) {
                // TODO: validate layout properties

            } else if ("style".equals(nodeName)) {
                Node nameAttr = attributes.getNamedItem("name");
                Node inheritedAttr = attributes.getNamedItem("inherit");

                // register the style name
                String styleName = null;
                Style style = (Style) format;
                if (nameAttr != null) {
                    styleName = nameAttr.getNodeValue();
                    style.setName(styleName);
                    themeManager.setNamedObject(theme.getName(), "style", style);
                }

                if (inheritedAttr != null) {
                    String inheritedName = inheritedAttr.getNodeValue();
                    Style inheritedStyle = (Style) themeManager.getNamedObject(
                            themeName, "style", inheritedName);
                    if (inheritedStyle == null) {
                        log.error("Unknown style: " + inheritedName);
                    } else {
                        themeManager.makeFormatInherit(style, inheritedStyle);
                        log.debug("Made style " + style + " inherit from "
                                + inheritedName);
                    }
                }

                if (styleName != null && elementXPath != null) {
                    log.warn("Style parser: named style '" + styleName
                            + "' cannot have an 'element' attribute: '"
                            + elementXPath + "'.");
                    continue;
                }

                for (Node selectorNode : getChildElementsByTagName(n,
                        "selector")) {
                    NamedNodeMap attrs = selectorNode.getAttributes();
                    Node pathAttr = attrs.getNamedItem("path");
                    if (pathAttr == null) {
                        log.warn(String.format(
                                "Style parser: named style '%s' has a selector with no path: ignored",
                                styleName));
                        continue;
                    }
                    String path = pathAttr.getNodeValue();

                    String viewName = null;
                    Node viewAttr = attrs.getNamedItem("view");
                    if (viewAttr != null) {
                        viewName = viewAttr.getNodeValue();
                    }

                    String selectorDescription = getCommentAssociatedTo(selectorNode);
                    if (selectorDescription != null) {
                        style.setSelectorDescription(path, viewName,
                                selectorDescription);
                    }

                    if (elementXPath != null
                            && (viewName == null || viewName.equals("*"))) {
                        log.info("Style parser: trying to guess the view name for: "
                                + elementXPath);
                        viewName = guessViewNameFor(doc, elementXPath);
                        if (viewName == null) {
                            if (!newStyles.containsKey(style)) {
                                newStyles.put(style,
                                        new LinkedHashMap<String, Properties>());
                            }
                            newStyles.get(style).put(path,
                                    getPropertiesFromNode(selectorNode));
                        }
                    }

                    if (styleName != null) {
                        if (viewName != null) {
                            log.info("Style parser: ignoring view name '"
                                    + viewName + "' in named style '"
                                    + styleName + "'.");
                        }
                        viewName = "*";
                    }

                    if (viewName != null) {
                        style.setPropertiesFor(viewName, path,
                                getPropertiesFromNode(selectorNode));
                    }
                }
            }

            themeManager.registerFormat(format);
            if (elementXPath != null) {
                if ("".equals(elementXPath)) {
                    baseNode.setUserData(nodeName, format, null);
                } else {
                    for (Node element : getNodesByXPath(baseNode, elementXPath)) {
                        element.setUserData(nodeName, format, null);
                    }
                }
            }
        }

        // styles created by the parser
        int count = 1;
        for (Style parent : newStyles.keySet()) {
            Style s = (Style) FormatFactory.create("style");
            String name = "";
            while (true) {
                name = String.format("common style %s", count);
                if (themeManager.getNamedObject(themeName, "style", name) == null) {
                    break;
                }
                count += 1;
            }
            s.setName(name);
            themeManager.registerFormat(s);
            themeManager.setNamedObject(themeName, "style", s);
            Map<String, Properties> map = newStyles.get(parent);
            for (Map.Entry<String, Properties> entry : map.entrySet()) {
                s.setPropertiesFor("*", entry.getKey(), entry.getValue());
            }
            // if the style already inherits, preserve the inheritance
            Style ancestor = (Style) ThemeManager.getAncestorFormatOf(parent);
            if (ancestor != null) {
                themeManager.makeFormatInherit(s, ancestor);
            }

            themeManager.makeFormatInherit(parent, s);
            log.info("Created extra style: " + s.getName());
        }
    }

    public static void parseProperties(org.w3c.dom.Element doc, Node node)
            throws ThemeIOException {
        NamedNodeMap attributes = node.getAttributes();
        Node elementAttr = attributes.getNamedItem("element");
        if (elementAttr == null) {
            throw new ThemeIOException(
                    "<properties> node has no 'element' attribute.");
        }
        String elementXPath = elementAttr.getNodeValue();

        Node baseNode = getBaseNode(doc);
        Node element = null;
        try {
            element = (Node) xpath.evaluate(elementXPath, baseNode,
                    XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new ThemeIOException(e);
        }
        if (element == null) {
            throw new ThemeIOException(
                    "Could not find the element associated to: " + elementXPath);
        }
        element.setUserData("properties", getPropertiesFromNode(node), null);
    }

    private static Properties getPropertiesFromNode(Node node) {
        Properties properties = new Properties();
        for (Node n : getChildElements(node)) {
            String textContent = n.getTextContent();
            Node presetAttr = n.getAttributes().getNamedItem("preset");
            if (presetAttr != null) {
                String presetName = presetAttr.getNodeValue();
                if (presetName != null) {
                    textContent = String.format("\"%s\"", presetName);
                }
            }
            properties.setProperty(n.getNodeName(), textContent);
        }
        return properties;
    }

    private static List<Node> getChildElements(Node node) {
        List<Node> nodes = new ArrayList<Node>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                nodes.add(n);
            }
        }
        return nodes;
    }

    public static List<Node> getChildElementsByTagName(Node node, String tagName) {
        List<Node> nodes = new ArrayList<Node>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE
                    && tagName.equals(n.getNodeName())) {
                nodes.add(n);
            }
        }
        return nodes;
    }

    public static Node getBaseNode(org.w3c.dom.Element doc)
            throws ThemeIOException {
        Node baseNode = null;
        try {
            baseNode = (Node) xpath.evaluate('/' + DOCROOT_NAME + "/layout",
                    doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new ThemeIOException(e);
        }
        if (baseNode == null) {
            throw new ThemeIOException("No <layout> section found.");
        }
        return baseNode;
    }

    private static String getCommentAssociatedTo(Node node) {
        Node n = node;
        while (true) {
            n = n.getPreviousSibling();
            if (n == null) {
                break;
            }
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                break;
            }
            if (n.getNodeType() == Node.COMMENT_NODE) {
                return n.getNodeValue().trim();
            }
        }
        return null;
    }

    private static String guessViewNameFor(org.w3c.dom.Element doc,
            String elementXPath) {
        NodeList widgetNodes = doc.getElementsByTagName("widget");
        Set<String> candidates = new HashSet<String>();
        String[] elements = elementXPath.split("\\|");
        for (int i = 0; i < widgetNodes.getLength(); i++) {
            Node node = widgetNodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node elementAttr = attributes.getNamedItem("element");
            if (elementAttr != null) {
                String[] widgetElements = elementAttr.getNodeValue().split(
                        "\\|");
                for (String element : elements) {
                    for (String widgetElement : widgetElements) {
                        if (element.equals(widgetElement)) {
                            List<Node> viewNodes = getChildElementsByTagName(
                                    node, "view");
                            if (!viewNodes.isEmpty()) {
                                candidates.add(viewNodes.get(0).getTextContent());
                            }
                        }
                    }
                }
            }
        }
        if (candidates.size() == 1) {
            return candidates.iterator().next();
        }
        return null;
    }

    private static List<Node> getNodesByXPath(Node baseNode, String elementXPath)
            throws ThemeIOException {
        final List<Node> nodes = new ArrayList<Node>();
        if (elementXPath != null) {
            try {
                NodeList elementNodes = (NodeList) xpath.evaluate(elementXPath,
                        baseNode, XPathConstants.NODESET);
                for (int i = 0; i < elementNodes.getLength(); i++) {
                    nodes.add(elementNodes.item(i));
                }
            } catch (XPathExpressionException e) {
                throw new ThemeIOException(e);
            }
        }
        return nodes;
    }
}
