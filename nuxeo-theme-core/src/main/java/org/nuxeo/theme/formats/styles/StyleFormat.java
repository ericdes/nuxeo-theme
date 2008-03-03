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

package org.nuxeo.theme.formats.styles;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.nuxeo.theme.formats.DefaultFormat;
import org.nuxeo.theme.formats.Format;

public class StyleFormat extends DefaultFormat implements Style {

    private final Map<String, Map<String, Properties>> styleProperties = new TreeMap<String, Map<String, Properties>>();

    public Properties getPropertiesFor(String viewName, String path) {
        Map<String, Properties> propertiesMap = styleProperties.get(viewName);
        if (propertiesMap != null) {
            return propertiesMap.get(path);
        }
        return null;
    }

    public void setPropertiesFor(String viewName, String path,
            Properties properties) {
        Map<String, Properties> propertiesMap = styleProperties.get(viewName);
        if (propertiesMap == null) {
            propertiesMap = new LinkedHashMap<String, Properties>();
        }
        Properties updatedProperties = propertiesMap.get(path);
        if (updatedProperties == null) {
            updatedProperties = new Properties();
        }
        if (properties != null) {
            for (Object key : properties.keySet()) {
                String value = properties.getProperty((String) key);
                if (value.equals("")) {
                    if (updatedProperties.containsKey(key)) {
                        updatedProperties.remove(key);
                    }
                } else {
                    updatedProperties.put(key, value);
                }
            }
            propertiesMap.put(path, updatedProperties);
        } else {
            propertiesMap.remove(path);
        }
        styleProperties.put(viewName, propertiesMap);
    }

    public void clearPropertiesFor(String viewName) {
        styleProperties.remove(viewName);
        styleProperties.put(viewName, new LinkedHashMap<String, Properties>());
    }

    public void clearPropertiesFor(String viewName, String path) {
        setPropertiesFor(viewName, path, null);
    }

    public Set<String> getPathsForView(String viewName) {
        if (styleProperties.containsKey(viewName)) {
            return styleProperties.get(viewName).keySet();
        }
        return new HashSet<String>();
    }

    public Set<String> getSelectorViewNames() {
        return styleProperties.keySet();
    }

    @Override
    public void clonePropertiesOf(Format source) {
        super.clonePropertiesOf(source);
        // Clone style properties
        Style sourceStyle = (Style) source;
        for (String viewName : sourceStyle.getSelectorViewNames()) {
            for (String path : sourceStyle.getPathsForView(viewName)) {
                setPropertiesFor(viewName, path, sourceStyle.getPropertiesFor(
                        viewName, path));
            }
        }
    }

    @Override
    public boolean isNamed() {
        return getName() != null;
    }

}
