package nxthemesEditor

import java.io.*
import javax.ws.rs.*
import javax.ws.rs.core.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import net.sf.json.JSONObject
import org.nuxeo.ecm.core.rest.*
import org.nuxeo.ecm.webengine.model.*
import org.nuxeo.ecm.webengine.model.impl.*
import org.nuxeo.ecm.webengine.model.exceptions.*
import org.nuxeo.ecm.webengine.*
import org.nuxeo.runtime.api.Framework
import org.nuxeo.theme.*
import org.nuxeo.theme.elements.*
import org.nuxeo.theme.formats.*
import org.nuxeo.theme.formats.widgets.*
import org.nuxeo.theme.formats.styles.*
import org.nuxeo.theme.formats.layouts.*
import org.nuxeo.theme.events.*
import org.nuxeo.theme.fragments.*
import org.nuxeo.theme.presets.*
import org.nuxeo.theme.properties.*
import org.nuxeo.theme.templates.*
import org.nuxeo.theme.themes.*
import org.nuxeo.theme.types.*
import org.nuxeo.theme.perspectives.*
import org.nuxeo.theme.uids.*
import org.nuxeo.theme.views.*
import org.nuxeo.theme.webwidgets.*

@WebModule(name="nxthemes-webwidgets", guard="user=Administrator")

@Path("/nxthemes-webwidgets")
@Produces(["text/html", "*/*"])
public class Main extends DefaultModule {
    
     @GET
     @Path("webWidgetFactory")
     public Object renderPerspectiveSelector(@QueryParam("org.nuxeo.theme.application.path") String path) {
        return getTemplate("webWidgetFactory.ftl").arg(
              "widget_categories", getWidgetCategories()).arg(
              "widget_types", getWidgetTypes()).arg(
              "selected_category", getSelectedWidgetCategory())
     }
    
    @GET @POST
    @Path("get_panel_data")
    public String getPanelData(@QueryParam("provider") String providerName, @QueryParam("region") String regionName, @QueryParam("mode") String mode) {
       return org.nuxeo.theme.webwidgets.Manager.getPanelData(providerName, regionName, mode)
    }
    
    @GET @POST
    @Path("add_widget")
    public void addWidget(@QueryParam("provider") String providerName, @QueryParam("widget_name") String widgetName, @QueryParam("region") String regionName, @QueryParam("order") int order) {
        org.nuxeo.theme.webwidgets.Manager.addWidget(providerName, widgetName, regionName, order)
    }
    
    @GET @POST
    @Path("move_widget")
    public void moveWidget(@QueryParam("src_provider") String srcProviderName, @QueryParam("dest_provider") String destProviderName, @QueryParam("src_uid") String srcUid, @QueryParam("src_region") String srcRegionName, @QueryParam("dest_region") String destRegionName, @QueryParam("dest_order") int destOrder) {
        org.nuxeo.theme.webwidgets.Manager.moveWidget(srcProviderName, destProviderName, srcUid, srcRegionName, destRegionName, destOrder)
    }

    @GET @POST
    @Path("remove_widget")
    public void removeWidget(@QueryParam("provider") String providerName, @QueryParam("widget_uid") String widgetUid) {
        org.nuxeo.theme.webwidgets.Manager.removeWidget(providerName, widgetUid)
    }
    
    @GET @POST
    @Path("set_widget_state")
    public void setWidgetState(@QueryParam("provider") String providerName, @QueryParam("widget_uid") String widgetUid, @QueryParam("state") String state) {
        org.nuxeo.theme.webwidgets.Manager.setWidgetState(providerName, widgetUid, state)
    }
    
    @GET @POST
    @Path("set_widget_category")
    public void setWidgetCategory(@QueryParam("category") String category) {
        SessionManager.setWidgetCategory(category)
    }
    
    @GET @POST
    @Path("get_widget_data_info")
    public String getWidgetDataInfo(@QueryParam("provider") String providerName, @QueryParam("widget_uid") String widgetUid, @QueryParam("name") String dataName) {
        return org.nuxeo.theme.webwidgets.Manager.getWidgetDataInfo(providerName,  widgetUid, dataName)
    }
    
    @GET @POST
    @Path("update_widget_preferences")
    public void updateWidgetPreferences(@QueryParam("provider") String providerName, @QueryParam("widget_uid") String widgetUid, @QueryParam("preferences") String preferences_map) {
        Map preferencesMap = JSONObject.fromObject(preferences_map)
        org.nuxeo.theme.webwidgets.Manager.updateWidgetPreferences(providerName, widgetUid, preferencesMap)
    }
    
    /* API */
  
    public static String getSelectedWidgetCategory() {
        String category = SessionManager.getWidgetCategory()
        if (category == null) {
            category = ""
        }
        return category
    }
    
    public static Set<String> getWidgetCategories() {
        Service service = Framework.getRuntime().getComponent("org.nuxeo.theme.webwidgets.Service")
        return service.getWidgetCategories()
    }
    
    public static List<WidgetType> getWidgetTypes() {
        String widgetCategory = getSelectedWidgetCategory()
        Service service = Framework.getRuntime().getComponent("org.nuxeo.theme.webwidgets.Service")
        return service.getWidgetTypes(widgetCategory)
    }
        
}

