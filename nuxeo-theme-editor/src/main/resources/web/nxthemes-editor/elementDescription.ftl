<#assign selected_element = script("getSelectedElement.groovy") />
<#assign selected_element_id = script("getSelectedElementId.groovy") />

<div>

<fieldset class="nxthemesEditor"><legend>Element description</legend>

<form id="nxthemesElementDescription" class="nxthemesForm" action="" onsubmit="return false">

  <div>
    <input type="hidden" name="id" value="${selected_element_id}" />
  </div>
  
  <p>
    <label>Description</label>
    <textarea name="description">${selected_element.description}</textarea>
  </p>
    
  <div>
    <button type="submit">Update</button>
  </div>

</form>

</fieldset>
</div>

