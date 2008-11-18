import net.sf.json.JSONObject

import org.nuxeo.theme.Manager
import org.nuxeo.theme.themes.ThemeManager
import org.nuxeo.theme.elements.Element
import org.nuxeo.theme.editor.Events
import org.nuxeo.theme.events.EventContext
import org.nuxeo.theme.events.EventManager
import org.nuxeo.theme.formats.FormatType
import org.nuxeo.theme.formats.styles.Style
import org.nuxeo.theme.types.TypeFamily
import org.nuxeo.theme.elements.ElementFormatter

id = Request.getParameter("id")
viewName = Request.getParameter("view_name")
path = Request.getParameter("path")
propertyMap = JSONObject.fromObject(Request.getParameter("property_map"))

Element element = ThemeManager.getElementById(id)
Properties properties = new Properties()
for (Object key : propertyMap.keySet()) {
    properties.put(key, propertyMap.get(key))
}

FormatType styleType = (FormatType) Manager.getTypeRegistry().lookup(TypeFamily.FORMAT, "style")
Style style = (Style) ElementFormatter.getFormatByType(element, styleType)
Style currentStyleLayer = Context.runScript("getSelectedStyleLayer.groovy")
if (currentStyleLayer != null) {
    style = currentStyleLayer
}

if (style.getName() != null || "".equals(viewName)) {
    viewName = "*"
}

style.setPropertiesFor(viewName, path, properties)

EventManager eventManager = Manager.getEventManager()
eventManager.notify(Events.STYLES_MODIFIED_EVENT, new EventContext(element, null))
