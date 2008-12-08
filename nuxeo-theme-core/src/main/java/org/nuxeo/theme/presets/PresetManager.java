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

package org.nuxeo.theme.presets;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuxeo.theme.Manager;
import org.nuxeo.theme.types.Type;
import org.nuxeo.theme.types.TypeFamily;

public class PresetManager {

    private static final Pattern manyPresetNamePattern = Pattern.compile(
            ".*?\"(.*?)\".*?", Pattern.DOTALL);

    private static final Pattern presetNamePattern = Pattern.compile(
            "^\"(.*?)\"$", Pattern.DOTALL);

    private static final Pattern globalPresetNamePattern = Pattern.compile(
            "^\".*?\\((.*?)\\)\"$", Pattern.DOTALL);

    private static final Pattern customPresetNamePattern = Pattern.compile(
            "^\"(.*?)\"$", Pattern.DOTALL);

    public static String extractPresetName(final String themeName,
            final String str) {
        String s = str.trim();
        final Matcher globalPresetNameMatcher = globalPresetNamePattern.matcher(s);
        if (globalPresetNameMatcher.find()) {
            Matcher presetMatcher = presetNamePattern.matcher(s);
            if (presetMatcher.find()) {
                return presetMatcher.group(1);
            }
        } else if (themeName != null) {
            final Matcher customPresetNameMatcher = customPresetNamePattern.matcher(s);
            if (customPresetNameMatcher.find()) {
                return String.format("%s/%s", themeName,
                        customPresetNameMatcher.group(1));
            }
        }
        return null;
    }

    public static PresetType getPresetByName(final String name) {
        return (PresetType) Manager.getTypeRegistry().lookup(TypeFamily.PRESET,
                name);
    }

    public static List<Type> getAllPresets() {
        return Manager.getTypeRegistry().getTypes(TypeFamily.PRESET);
    }

    public static List<PresetType> getGlobalPresets(final String group,
            final String category) {
        List<PresetType> presets = new ArrayList<PresetType>();
        for (Type type : getAllPresets()) {
            PresetType preset = (PresetType) type;
            if (category != null && !preset.getCategory().equals(category)) {
                continue;
            }
            if (group != null && !preset.getGroup().equals(group)) {
                continue;
            }
            if (preset instanceof CustomPresetType) {
                continue;
            }
            presets.add(preset);
        }
        return presets;
    }

    public static List<PresetType> getCustomPresets(final String themeName) {
        return getCustomPresets(themeName, null);
    }

    public static List<PresetType> getCustomPresets(final String themeName,
            final String category) {
        List<PresetType> presets = new ArrayList<PresetType>();
        for (Type type : getAllPresets()) {
            PresetType preset = (PresetType) type;
            if (!(preset instanceof CustomPresetType)) {
                continue;
            }
            if (category != null && !preset.getCategory().equals(category)) {
                continue;
            }
            if (themeName != null && !preset.getGroup().equals(themeName)) {
                continue;
            }
            presets.add(preset);

        }
        return presets;
    }

    public static String resolvePresets(final String themeName, final String str) {
        Matcher m = manyPresetNamePattern.matcher(str.trim());
        StringBuilder sb = new StringBuilder();
        int end = 0;
        while (m.find()) {
            end = m.end(1) + 1;
            sb.append(str.substring(m.start(), m.start(1) - 1));
            String presetStr = String.format("\"%s\"", m.group(1));
            String presetName = extractPresetName(themeName, presetStr);
            if (presetName == null) {
                sb.append(presetStr);
                continue;
            }
            PresetType preset = getPresetByName(presetName);
            if (preset == null) {
                sb.append(presetStr);
                continue;
            }
            sb.append(preset.getValue());
        }
        sb.append(str.substring(end));
        return sb.toString();
    }

}
