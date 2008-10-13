<#assign selected_element_id = script("getSelectedElementId.groovy") />
<#assign style_of_element_element = script("getStyleOfSelectedElement.groovy") />
<#assign current_theme_name = script("getCurrentThemeName.groovy") />
<#assign style_layers_of_selected_element = script("getStyleLayersOfSelectedElement.groovy") />
<#assign inherited_style_name_of_selected_element = script("getInheritedStyleNameOfSelectedElement.groovy") />
<#assign named_styles = script("getNamedStyles.groovy") />

<div>

  <#if !style_of_element_element>
      <form action="" class="nxthemesForm" onsubmit="NXThemesStyleEditor.createStyle(); return false;">
        <div>
          The element has no style.
          <button>
            Create a style
          </button>
        </div>
      </form>
  
  <#else>

      <style id="previewCss" type="text/css"></style>

      <form class="nxthemesInheritedStyles" onsubmit="return false"
        element="${selected_element_id}"
        currentThemeName="${current_theme_name}">

        <div>
          <label>Inherit style properties from:</label>
	  <select id="inherited_style" id="inherited_style" onchange="NXThemesStyleEditor.makeElementUseNamedStyle(this)">
	    <#list named_styles as style>
	      <#if inherited_style_name_of_selected_element == style>
	        <option value="${style}" selected="selected">${style}</option>
	      <#else>
	        <option value="${style}">${style}</option>
	      </#if>
	    </#list>
	  </select>

          <button onclick="NXThemesStyleEditor.createNamedStyle('${selected_element_id}', '${current_theme_name}')">New style</button>
          <#if inherited_style_name_of_selected_element>
            <button onclick="NXThemesStyleEditor.deleteNamedStyle('${selected_element_id}', '${current_theme_name}', '${inherited_style_name_of_selected_element}')">Delete '${inherited_style_name_of_selected_element}'</button>
          </#if>
        </div>

      </form>
      
      <div class="nxthemesButtonSelector"
        style="text-align: left; padding: 4px 15px;">
        <span>Layers: </span>
        <#list style_layers_of_selected_element as layer>
          <span>${layer.rendered}</span>
        </#list>
      </div>

      <table style="width: 100%" cellpadding="10" cellspacing="0">
        <tr>
          <td style="width: 50%; vertical-align: top">
            <fieldset class="nxthemesEditor">
              <legend>
                Preview
              </legend>

              <div id="stylePreviewArea"
                element="${selected_element_id}">
                <img src="/nuxeo/site/files/nxthemes-editor/img/progressbar.gif" alt=""
                  width="220" height="19"
                  style="padding: 5px; border: 1px solid #ccc; background-color: #fff" />
                <@nxthemes_view resource="style-preview.json" />
              </div>
            </fieldset>
            <div id="labelInfo" style="display:none" />

          </td>
          <td style="width: 50%; vertical-align: top">

            <!--  Style properties form -->
            <@nxthemes_panel identifier="style properties"
              url="/nxthemes/editor/styleProperties.ftl"
              controlledBy="style editor perspectives,style editor actions,element form actions"
              visibleInPerspectives="style properties,style picker" />

            <!--  Style picker -->
            <@nxthemes_panel identifier="style picker"
              url="/nxthemes/editor/stylePicker.ftl"
              controlledBy="style editor perspectives,toolbox mover"
              visibleInPerspectives="style picker" />

          </td>
        </tr>
      </table>
  </#if>

</div>

