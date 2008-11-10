
<#assign paddingOfSelectedElement = script("getPaddingOfSelectedElement.groovy") />

<div id="nxthemesPaddingEditor" class="nxthemesToolbox">

<div class="title">
<img class="close" onclick="javascript:NXThemes.getControllerById('editor perspectives').switchTo('edit canvas')"
     src="/skin/nxthemes/editor/img/close-button.png" width="14" height="14" alt="" />
     Padding editor</div>

<form class="nxthemesForm" action="" onsubmit="return false">
  <table cellpadding="0" cellspacing="0" style="width: 100%">
      <tr>
        <td></td>
        <td><input type="text" size="6" name="padding-top" tabindex="1"
            value="${paddingOfSelectedElement.top}" />
        </td>
        <td></td>
      </tr>
      <tr>
        <td><input type="text" size="6" name="padding-left" tabindex="4"
            value="${paddingOfSelectedElement.left}" />
        </td><td>
          <div class="nxthemesFragment" style="height: 50px;" />
        </td>
        <td style="text-align: center">
          <input type="text" size="6" name="padding-right" tabindex="2"
            value="${paddingOfSelectedElement.right}" />
        </td>
      </tr>
      <tr>
        <td></td>
        <td><input type="text" size="6" name="padding-bottom" tabindex="3"
            value="${paddingOfSelectedElement.bottom}" />
        </td>
        <td></td>
      </tr>
      <tr>
        <td colspan="3">
          <button type="submit">Update</button>
          <button type="submit" onclick="NXThemes.getControllerById('editor perspectives').switchTo('edit canvas');">Close</button>
        </td>
      </tr>
    </table>

</form>

</div>

