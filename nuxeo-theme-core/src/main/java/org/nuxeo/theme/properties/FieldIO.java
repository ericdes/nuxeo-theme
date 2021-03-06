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

package org.nuxeo.theme.properties;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.theme.themes.ThemeIOException;

public class FieldIO {

    private static final Log log = LogFactory.getLog(FieldIO.class);

    public static void updateFieldsFromProperties(Object object,
            Properties properties) throws ThemeIOException {
        Enumeration<?> names = properties.propertyNames();

        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = properties.getProperty(name);

            Class<?> c = object.getClass();
            Field field;
            try {
                field = c.getField(name);
            } catch (SecurityException e) {
                throw new ThemeIOException(e);
            } catch (NoSuchFieldException e) {
                throw new ThemeIOException(e);
            }
            Class<?> fieldType = field.getType();
            Type fieldGenericType = field.getGenericType();

            // boolean fields
            if (fieldType.equals(boolean.class)
                    || fieldType.equals(Boolean.class)) {
                try {
                    field.setBoolean(object, Boolean.parseBoolean(value));
                } catch (IllegalArgumentException e) {
                    throw new ThemeIOException(e);
                } catch (IllegalAccessException e) {
                    throw new ThemeIOException(e);
                }
                continue;
            }

            // string fields
            if (fieldType.equals(String.class)) {
                try {
                    field.set(object, value);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    throw new ThemeIOException(e);
                } catch (IllegalAccessException e) {
                    throw new ThemeIOException(e);
                }
                continue;
            }

            // generics
            if (fieldGenericType instanceof ParameterizedType) {
                if (fieldType.equals(ArrayList.class) || fieldType.equals(List.class)
                        || fieldType.equals(Collection.class)) {

                    Type[] actualTypes = ((ParameterizedType) fieldGenericType).getActualTypeArguments();

                    if (actualTypes.length > 1) {
                        log.warn("Only one-dimension arrays are supported.");
                        continue;
                    }

                    // Collection<String>
                    if (actualTypes[0].equals(String.class)) {
                        List<String> list = new ArrayList<String>();
                        list.addAll(Arrays.asList(value.split(",")));
                        try {
                            field.set(object, list);
                        } catch (IllegalArgumentException e) {
                            throw new ThemeIOException(e);
                        } catch (IllegalAccessException e) {
                            throw new ThemeIOException(e);
                        }
                        continue;
                    }
                }
            }

            log.warn("Field type not supported: "
                    + fieldType.getCanonicalName());
        }

    }

    public static Properties dumpFieldsToProperties(Object object)
            throws Exception {

        Properties properties = new Properties();

        Class<?> c = object.getClass();
        for (Field field : c.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            String fieldName = field.getName();

            // boolean fields
            if (fieldType.equals(boolean.class)
                    || fieldType.equals(Boolean.class)) {
                String value = field.getBoolean(object) ? "true" : "false";
                properties.setProperty(fieldName, value);
                continue;
            }

            // string fields
            if (fieldType.equals(String.class)) {
                Object value = field.get(object);
                properties.setProperty(fieldName, (String) value);
                continue;
            }

        }
        return properties;
    }

}
