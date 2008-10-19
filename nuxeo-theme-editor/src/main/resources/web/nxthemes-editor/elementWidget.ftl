<#assign selected_element_id = script("getSelectedElementId.groovy") />
<#assign selected_view_name = script("getSelectedViewName.groovy") />
<#assign view_names_for_selected_element = script("getViewNamesForSelectedElement.groovy") />

<div>

<fieldset class="nxthemesEditor"><legend>Element's widget</legend>

<form id="nxthemesElementWidget" class="nxthemesForm" action="" onsubmit="return false">
<div>
  <input type="hidden" name="id" value="${selected_element_id}" />
</div>
<p>
  <label>View name</label>
  <select name="" id="viewName">
    <#list view_names_for_selected_element as view_name>
      <#if view_name == selected_view_name>
        <option value="${view_name}" selected="selected">${view_name}</option>
      <#else>
        <option value="${view_name}">${view_name}</option>
      </#if>
    </#list>
  </select>
</p>

  <button type="submit">Update</button>
</form>

</fieldset>

</div>

