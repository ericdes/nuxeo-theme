<div>

<#assign themeManager=This.getThemeManager()>

<div id="nxthemesStyleManager">

<h1 class="nxthemesEditor">Styles</h1>

    <div class="nxthemesButtonSelector">
      <span>View mode:</span>
      <#if style_manager_mode == 'named styles'>            
          <a href="javascript:void(0)" onclick="NXThemesStyleManager.setEditMode('unused styles')">Unused styles</a>
          <a href="javascript:void(0)" class="selected">Named styles</a>
      <#else>
          <a href="javascript:void(0)" class="selected">Unused styles</a>
          <a href="javascript:void(0)" onclick="NXThemesStyleManager.setEditMode('named styles')">Named styles</a>
      </#if>
    </div>

<table cellpadding="0" cellspacing="0" style="width: 100%"><tr>

<td style="vertical-align: top; width: 200px; padding-right: 5px;">

<ul class="nxthemesSelector">
<#list themeManager.getThemeNames() as theme_name>
<li <#if theme_name = current_theme_name>class="selected"</#if>><a href="javascript:void(0)" 
  onclick="NXThemesEditor.selectTheme('${theme_name}', 'style manager')">
  <img src="${skinPath}/img/theme-16.png" width="16" height="16" />
  ${theme_name}</a></li>
</#list>
</ul>


</td>
<td style="padding-left: 10px; vertical-align: top;">

<h2 class="nxthemesEditor" style="text-transform: uppercase">${current_theme_name}</h2>

<#if style_manager_mode = 'named styles'>

<ul>
<#list named_styles as style>
  <li>${style.name}</li>
</#list>
</ul>

<#else>

<p><em>These styles are associated with non existing views. They can probably be cleaned up.</em><p>

<#assign styles=themeManager.getStyles(current_theme_name)>
<#list styles as style>

<#assign views=themeManager.getUnusedStyleViews(style)>
<#if views>

<#list views as view>

<form class="unusedViews" action="javascript:void(0)" submit="return false">
  <div>
    <input type="hidden" name="theme_name" value="${current_theme_name}" />
    <input type="hidden" name="style_uid" value="#{style.uid}" />
    <input type="hidden" name="view_name" value="${view}" />
  </div>
   
  <div style="font-size: 11px; font-weight: bold">
    '${view}' view
  </div>
  
  <pre style="margin: 4px 0 6px 0; font-size: 10px; background-color: #ffc; border: 1px solid #fc0">${This.renderStyleView(style, view)}</pre>

  <button type="submit">
    <img src="${skinPath}/img/cleanup-16.png" width="16" height="16" />
    Clean up
  </button>

</form>

</#list>

</#if>
</#list>

</#if>

</td></tr></table>

</div>

