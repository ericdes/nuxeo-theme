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

package org.nuxeo.theme.html.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.theme.ApplicationType;
import org.nuxeo.theme.Manager;
import org.nuxeo.theme.html.JSUtils;
import org.nuxeo.theme.html.Utils;
import org.nuxeo.theme.resources.ResourceType;
import org.nuxeo.theme.themes.ThemeManager;
import org.nuxeo.theme.types.TypeFamily;
import org.nuxeo.theme.types.TypeRegistry;

public final class Resources extends HttpServlet implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(Resources.class);

    private static final Pattern pathPattern = Pattern.compile("/([^/]+)");

    @Override
    protected void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {

        final String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            return;
        }
        final Matcher m = pathPattern.matcher(pathInfo);
        if (!m.matches()) {
            log.error(String.format("Invalid resource path: %s", pathInfo));
            return;
        }

        final TypeRegistry typeRegistry = Manager.getTypeRegistry();
        final ThemeManager themeManager = Manager.getThemeManager();

        String contentType = null;
        final String[] resourceNames = m.group(1).split(",");
        for (String resourceName : resourceNames) {
            String previousContentType = contentType;
            if (resourceName.endsWith(".js")) {
                contentType = "text/javascript";
            } else if (resourceName.endsWith(".css")) {
                contentType = "text/css";
            } else if (resourceName.endsWith(".json")) {
                contentType = "text/json";
            }

            if (contentType == null) {
                log.error("Resource names must end with .js, .css or .json: "
                        + pathInfo);
                return;
            }

            if (previousContentType != null
                    && !contentType.equals(previousContentType)) {
                log.error("Combined resources must be of the same type: "
                        + pathInfo);
                return;
            }
        }

        response.addHeader("content-type", contentType);

        // cache control
        final String applicationPath = request.getParameter("path");
        if (applicationPath != null) {
            ApplicationType application = (ApplicationType) Manager.getTypeRegistry().lookup(
                    TypeFamily.APPLICATION, applicationPath);
            if (application != null) {
                Utils.setCacheHeaders(response,
                        application.getResourceCaching());
            }
        }

        StringBuilder text = new StringBuilder();
        String basePath = request.getParameter("basepath");

        for (String resourceName : resourceNames) {
            final OutputStream out = new ByteArrayOutputStream();
            String source = themeManager.getResource(resourceName);
            if (source == null) {
                ResourceType resource = (ResourceType) typeRegistry.lookup(
                        TypeFamily.RESOURCE, resourceName);
                if (resource == null) {
                    log.error(String.format("Resource not registered %s.",
                            resourceName));
                    continue;
                }
                writeResource(resource, out);
                source = out.toString();
                if (resourceName.endsWith(".js") && resource.isShrinkable()) {
                    source = JSUtils.compressSource(source);
                } else if (resourceName.endsWith(".css")) {
                    source = source.replaceAll("\\$\\{basePath\\}", basePath);
                }
                themeManager.setResource(resourceName, source);
            }
            text.append(source);
        }

        boolean supportsGzip = Utils.supportsGzip(request);
        OutputStream os = response.getOutputStream();
        if (supportsGzip) {
            response.setHeader("Content-Encoding", "gzip");
            // Needed by proxy servers
            response.setHeader("Vary", "Accept-Encoding");
            os = new GZIPOutputStream(os);
        }

        os.write(text.toString().getBytes());
        os.close();

        log.debug(String.format("Served resource(s): %s %s", pathInfo,
                supportsGzip ? "with gzip compression" : ""));
    }

    private static void writeResource(final ResourceType resource,
            final OutputStream out) {
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    resource.getPath());

            if (in != null) {
                byte[] buffer = new byte[1024];
                int read = in.read(buffer);
                while (read != -1) {
                    out.write(buffer, 0, read);
                    read = in.read(buffer);
                    out.flush();
                }
                out.close();
            } else {
                log.error(String.format("Resource not found %s.",
                        resource.getName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
