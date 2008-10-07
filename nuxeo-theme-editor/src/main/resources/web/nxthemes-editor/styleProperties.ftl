<div>

  <fieldset class="nxthemesEditor">
    <legend>
      Properties
    </legend>

    <div class="nxthemesButtonSelector"
      style="float: right; margin-top: -40px">
      <span>Edit mode:</span>
      <#if styleEditMode == 'css'>
          <a href="javascript:void(0)"
            onclick="NXThemesStyleEditor.setStyleEditMode('form', 'css')">form</a>
          <a href="javascript:void(0)" class="selected">CSS</a>
      <#else>
          <a href="javascript:void(0)" class="selected">form</a>
          <a href="javascript:void(0)"
            onclick="NXThemesStyleEditor.setStyleEditMode('css', 'form')">CSS</a>
      </#if>
    </div>

      <!-- Inline CSS editing -->
      <#if styleEditMode == 'css'}">
        <form id="nxthemesElementStyleCSS" class="nxthemesForm" action=""
          onsubmit="return false">
          <div>
            <textarea name="cssSource" rows="15" cols="72"
              style="width: 100%; height: 250px; font-size: 11px;">#{nxthemesUiManager.renderedElementStyleProperties}</textarea>
            <input type="hidden" name="id"
              value="#{nxthemesUiStates.selectedElement.uid}" />
            <input type="hidden" name="viewName"
              value="#{nxthemesUiManager.currentViewName}" />
          </div>
          <div style="padding-top: 10px">
            <button type="submit">Save</button>
          </div>
        </form>
      <!-- Edit form -->
      <#else>
        <form id="nxthemesElementStyle" class="nxthemesForm" action=""
          onsubmit="return false">
          <p style="margin-bottom: 10px;">
            <label>
              Selector
            </label>
            <h:selectOneMenu id="viewName"
              onchange="NXThemesStyleEditor.chooseStyleSelector(this)"
              value="#{nxthemesUiStates.currentStyleSelector}">
              <f:selectItems
                value="#{nxthemesUiManager.availableStyleSelectorsForSelectedElement}" />
            </h:selectOneMenu>
            <input type="hidden" name="id"
              value="#{nxthemesUiStates.selectedElement.uid}" />
            <input type="hidden" name="path"
              value="#{nxthemesUiStates.currentStyleSelector}" />
            <input type="hidden" name="viewName"
              value="#{nxthemesUiManager.currentViewName}" />
          </p>

            <#if currentStyleSelector>
              <div class="nxthemesButtonSelector" style="padding: 3px">
                <span>categories: </span>
                <ui:repeat value="#{nxthemesUiManager.styleCategories}"
                  var="category">
                  <h:outputText escape="false" value="#{category.rendered}" />
                </ui:repeat>
              </div>
            </#if>
     
            <#if elementStyleProperties>
              <div
                style="height: 220px; margin-top: 5px; margin-bottom: 15px; overflow-y: scroll; overflow-x: hidden">
                <ui:repeat value="#{nxthemesUiManager.elementStyleProperties}"
                  var="property">
                  <p>
                    <h:outputText escape="false" value="#{property.rendered}" />
                  </p>
                </ui:repeat>
              </div>
              <button type="submit">
                Save
              </button>
            </#if>
        </form>
      </#if>
  </fieldset>

</div>

