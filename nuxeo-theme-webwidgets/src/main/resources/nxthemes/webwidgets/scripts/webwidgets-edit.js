
NXThemesWebWidgets.uploaders = new Hash();

NXThemesWebWidgets.addWidget = function(info) {
    var widgetName = info.source.getAttribute('typename');
    var provider = info.target.getAttribute('provider');
    var region = info.target.getAttribute('region');
    var order = info.order;
    var url = nxthemesBasePath + "/nxthemes-webwidgets/add_widget";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'provider': provider,
             'widget_name': widgetName,
             'region': region,
             'order': order
         },
         onComplete: function(r) {
             info.controller.refreshViews();
         }
    });
};

NXThemesWebWidgets.moveWidget = function(info) {
    var srcContainer = info.sourceContainer;
    var srcUid = info.source.getAttribute('id').replace("webwidget_", "");
    var srcProvider = srcContainer.getAttribute('provider');
    var srcRegionName = srcContainer.getAttribute('region');
    var destProvider = info.target.getAttribute('provider');
    var destRegionName = info.target.getAttribute('region');
    var destOrder = info.order;
    
    var url = nxthemesBasePath + "/nxthemes-webwidgets/move_widget";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'src_provider': srcProvider,
             'dest_provider': destProvider,
             'src_uid': srcUid,
             'src_region_': srcRegionName,
             'dest_region': destRegionName,
             'dest_order': destOrder
         },
         onComplete: function(r) {
           if (srcRegionName != destRegionName) {
             NXThemesWebWidgets.changeWidgetId(srcUid, newUid);
             var widget = NXThemesWebWidgets.getWidgetById(newUid);
             widget.setHtml();
           }
         }
    });
};

NXThemesWebWidgets.deleteWidget = function(widgetUid) {
      var answer = confirm("Deleting, are you sure?")
      if (!answer) {
          return;
      }
      var widgetEl = $('webwidget_' + widgetUid);
      var provider = widgetEl.up('.nxthemesWebWidgetContainer').getAttribute('provider');
      var url = nxthemesBasePath + "/nxthemes-webwidgets/remove_widget";
      new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'provider': provider,
             'widget_uid': widgetUid
         },
         onComplete: function(r) {
           widgetEl.remove();
         }
      });
};

NXThemesWebWidgets.setWidgetState = function(widgetUid, mode, state) {
    var widgetEl = $('webwidget_' + widgetUid);
    var container = widgetEl.up('.nxthemesWebWidgetContainer');
    var provider = container.getAttribute('provider');
    var widget = NXThemesWebWidgets.getWidgetById(widgetUid);
    var url = nxthemesBasePath + "/nxthemes-webwidgets/set_widget_state";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'provider': provider,
             'widget_uid': widgetUid,
             'state': state
         },
         onComplete: function(r) {
           var widget_mode = mode;
           if (state) {
               widget_mode = widget_mode + '/' + state;
           }
           widget.switchMode(widget_mode);
         }
      });
};


NXThemesWebWidgets.setWidgetCategory = function(select) {
    var category = select.value;
    if (category === null) {
      return;
    }
    var url = nxthemesBasePath + "/nxthemes-webwidgets/set_widget_category";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'category': category
         },
         onComplete: function(r) {
           NXThemes.getViewById("web widget factory").refresh();
         }
    });
};

NXThemesWebWidgets.editPreferences = function(widgetUid) {
  var widget = NXThemesWebWidgets.getWidgetById(widgetUid);
  var frame = widget.createElement("div");
  var form = NXThemesWebWidgets.renderPreferenceEditForm(widget);
  form.addClassName('nxthemesWebWidgetsEditForm');
  form.action = "";
  form.onsubmit = NXThemesWebWidgets.changePreferences.bindAsEventListener(this);
  form.setAttribute("widget_uid", widgetUid);

  var uploadBox = widget.createElement("div");
  uploadBox.setAttribute("id", 'webwidget_upload_' + widgetUid);
  uploadBox.setAttribute("class", 'nxthemesWebWidgetsUploadBox');
  uploadBox.hide();

  frame.appendChild(form);
  frame.appendChild(uploadBox);

  widget.setBody(frame);
};

NXThemesWebWidgets.renderPreferenceEditForm = function(widget) {
    var form = widget.createElement("form");
    var table = widget.createElement("table");
    var tbody = widget.createElement("tbody");
    var widgetUid = widget.id;
    var providerName = widget.getProviderName();

    for (var i = 0; i < widget.preferences.length; i++) {
      var tr = widget.createElement("tr");
      var pref = widget.preferences[i];

      var name = pref.name;
      var value = widget.getValue(name) || pref.defaultValue;
      var type = pref.type;
      if (type == 'hidden') {
        continue;
      }
      var label = widget.createElement("label");
      label.innerHTML = pref.label + ':';

      var tdl = widget.createElement("td");
      tdl.setStyle("width", "25%");
      tdl.appendChild(label);
      tr.appendChild(tdl);

      var tdc = widget.createElement("td");
      tdc.setStyle("width", "75%");
      var control = '';

      if (type == 'text') {
        control = '<input type="string" name="' + name + '" value="' + value + '" />';

      } else if (type == 'file') {
        new NXThemesWebWidgets.FileUploader(providerName, widgetUid, name, value, tdc);

      } else if (type == 'image') {
        new NXThemesWebWidgets.ImageUploader(providerName, widgetUid, name, value, tdc);

      } else if (type == 'password') {
        control = '<input type="password" name="' + name + '" value="' + value + '" />';

      } else if (type == 'textarea') {
        control = '<textarea type="string" name="' + name + '">' + value + '</textarea>';

      } else if (type == 'range') {
        control = '<select name="' + name + '">';
        if (parseInt(pref.step) > 0) {
          for (var v=parseInt(pref.min); v<=parseInt(pref.max); v+=parseInt(pref.step)) {
            if (value == v) {
              control += '<option value="' + v + '" selected="selected">' + v + '</option>';
            } else {
              control += '<option value="' + v + '">' + v + '</option>';
            }
          }
        }
        control += '</select>';

      } else if (type == 'list') {
        control = '<select name="' + name + '">';
        var options = pref.options;
        for (var i=0; i<options.length; i++) {
          var v = options[i].value;
          var l = options[i].label;
          if (value == v) {
            control += '<option value="' + v + '" selected="selected">' + l + '</option>';
          } else {
            control += '<option value="' + v + '">' + l + '</option>';
          }
        }
        control += '</select>';

      } else if (type == 'boolean') {
        if (value == 'true') {
          control += '<input name="' + name + '" type="checkbox" checked="checked" />';
        } else {
          control += '<input name="' + name + '" type="checkbox" />';
        }
      }

      if (control) {
        tdc.innerHTML = control;
      }
      tr.appendChild(tdc);
      tbody.appendChild(tr);
    }
    table.appendChild(tbody);
    form.appendChild(table);

    var submit = widget.createElement('input');
    submit.type = 'submit';
    submit.value = "Ok";
    var div = widget.createElement("div");
    div.appendChild(submit);
    form.appendChild(div);

    return form;
};


NXThemesWebWidgets.getUploader = function(providerName, widgetUid, name) {
  var id = providerName + '/' + widgetUid + '/' + name;
  return NXThemesWebWidgets.uploaders.get(id);
}


NXThemesWebWidgets.BaseUploader = Class.create();
NXThemesWebWidgets.BaseUploader.prototype = {

  initialize: function(providerName, widgetUid, name, value, el) {
     this.providerName = providerName;
     this.widgetUid = widgetUid;
     this.name = name;
     this.value = value;
     this.el = el;

     this.register();
     this.render();
   },

   getId: function() {
     return this.providerName + '/' + this.widgetUid + '/' + this.name;
   },

   register: function() {
     NXThemesWebWidgets.uploaders.set(this.getId(), this);
   },

  upload: function(e) {
    this.controlEl.hide();
    this.boxEl.show();
  },

  complete: function() {
    // TO OVERRIDE
  },

  render: function() {
    // TO OVERRIDE
  }

};

NXThemesWebWidgets.ImageUploader = Class.create();
NXThemesWebWidgets.ImageUploader.prototype = Object.extend(new NXThemesWebWidgets.BaseUploader(), {

   render: function() {
     var controlEl = this.controlEl = document.createElement("div");
     var src = this.value;
     var msg = src ? "Change image" : "Set image";
     var html = '<button type="button">' + msg + '<button>';
     if (src) {
       html = '<div><img src="' + NXThemesWebWidgets.getWidgetDataUrl(src) + '" /></div>' + html;
     }
     controlEl.innerHTML = html;
     Event.observe(controlEl, "click", this.upload.bindAsEventListener(this));
     this.el.appendChild(controlEl);

     var boxEl = this.boxEl = document.createElement("div");
     var frameName = 'f' + this.widgetUid + '_' + this.name;
     boxEl.innerHTML = '<form style="padding: 0; margin: 0" action="' + nxthemesBasePath + '/nxthemes-webwidgets/upload_file?widget_uid=' + encodeURIComponent(this.widgetUid) + '&data=' + encodeURIComponent(this.name) + '&provider=' + encodeURIComponent(this.providerName) + '"' +
       ' method="post" enctype="multipart/form-data" target="' + frameName + '">' +             
       '<div><input type="file" name="file" size="8" onchange="submit()" /></div></form>';
     boxEl.hide();

     var iframeEl = this.iframeEl = document.createElement('iframe');
     iframeEl.style.display = "none";
     iframeEl.name = frameName;
     boxEl.appendChild(iframeEl);

     this.el.appendChild(boxEl);
  },

  complete: function() {
    var controlEl = this.controlEl;
    var boxEl = this.boxEl;
    var name = this.name;
    var providerName = this.providerName;
    var widgetUid = this.widgetUid;
    var url = nxthemesBasePath + "/nxthemes-webwidgets/get_widget_data_info";
    new Ajax.Request(url, {
         method: 'get',
         parameters: {
             'provider': providerName,
             'widget_uid': widgetUid,
             'name': name
         },
         onComplete: function(r) {
           var text = r.responseText;
           if (text) {
             var info = text.evalJSON(true);
             var now = new Date().getTime();
             var src = nxthemesBasePath + '/nxthemes-webwidgets/render_widget_data?widget_uid=' + encodeURIComponent(widgetUid) +
               '&data=' + encodeURIComponent(name) + '&provider=' + encodeURIComponent(providerName) +
               '&timestamp=' + now;
             controlEl.innerHTML = '<div><img src="' + src + '" /></div>' +
               info['filename'] + ' (' + info['content-type'] + ')';
           } else {
               controlEl.innerHTML = '<img src="' + nxthemesBasePath + '/skin/nxthemes-webwidgets/img/exclamation.png" />';
           }
           controlEl.show();
           boxEl.hide();
         }
      });
    }
});

NXThemesWebWidgets.FileUploader = Class.create();
NXThemesWebWidgets.FileUploader.prototype = Object.extend(new NXThemesWebWidgets.BaseUploader(), {

   render: function() {
     var controlEl = this.controlEl = document.createElement("div");
     var src = this.value;
     var msg = src ? "Change file" : "Set file";
     controlEl.innerHTML = '<button type="button">' + msg + '<button>';
     Event.observe(controlEl, "click", this.upload.bindAsEventListener(this));
     this.el.appendChild(controlEl);

     var boxEl = this.boxEl = document.createElement("div");
     var frameName = 'f' + this.widgetUid + '_' + this.name;
     boxEl.innerHTML = '<form style="padding: 0; margin: 0" action="' + nxthemesBasePath + '/nxthemes-webwidgets/upload_file?widget_uid=' + encodeURIComponent(this.widgetUid) + '&data=' + encodeURIComponent(this.name) + '&provider=' + encodeURIComponent(this.providerName) + '"' +
       ' method="post" enctype="multipart/form-data" target="' + frameName + '">' +
       '<div><input type="file" name="file" size="8" onchange="submit()" /></div></form>';
     boxEl.hide();

     var iframeEl = this.iframeEl = document.createElement('iframe');
     iframeEl.style.display = "none";
     iframeEl.name = frameName;
     boxEl.appendChild(iframeEl);

     this.el.appendChild(boxEl);
  },

  complete: function() {
    var controlEl = this.controlEl;
    var boxEl = this.boxEl;
    var name = this.name;
    var providerName = this.providerName;
    var widgetUid = this.widgetUid;
    var url = nxthemesBasePath + "/nxthemes-webwidgets/get_widget_data_info";
    new Ajax.Request(url, {
         method: 'get',
         parameters: {
             'provider': providerName,
             'widget_uid': widgetUid,
             'name': name
         },
         onComplete: function(r) {
           var text = r.responseText;
           if (text) {
             var info = text.evalJSON(true);
             controlEl.innerHTML = info['filename'] + ' (' + info['content-type'] + ')';
          } else {
             controlEl.innerHTML = '<img src="' + nxthemesBasePath + '/skin/nxthemes-webwidgets/img/exclamation.png" />';
          }
          controlEl.show();
          boxEl.hide();
        }
     });
  }
});

NXThemesWebWidgets.changePreferences = function(info) {
  Event.stop(info);
  var form = Event.findElement(info, "form");
  var preferencesMap = $H();
  var widgetUid = form.getAttribute("widget_uid");
  var widget = NXThemesWebWidgets.getWidgetById(widgetUid);
  var providerName = widget.getProviderName();

  $A(Form.getElements(form)).each(function(i) {
     var type = i.type;
     if (type != "submit") {
       var name = i.name;
       var value = Form.Element.getValue(i);
       if (type == 'checkbox') {
         value = value == 'on' ? 'true' : 'false';
       }
       preferencesMap.set(name, value);
       widget.setValue(name, value);
     }
  });

  var url = nxthemesBasePath + "/nxthemes-webwidgets/update_widget_preferences";
  new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'provider': providerName,
             'widget_uid': widgetUid,
             'preferences': preferencesMap.toJSON()
         },
         onComplete: function(r) {
           widget.draw();
         }
  });
  return false;
};

NXThemesWebWidgets.exit = function() {
  NXThemes.expireCookie("nxthemes.mode");
  window.location.reload();
};

// Web widget panel
NXThemesWebWidgets.WebWidgetPanel = Class.create();
NXThemesWebWidgets.WebWidgetPanel.prototype = Object.extend(new NXThemes.View(), {

  setup: function() {
  },

  inspect: function() {
    return '[NXThemes Web Widget Panel]';
  },

  render: function(data) {
    var panel = this.widget;
    var provider = data.get('provider');
    var decoration = data.get('decoration');
    var region = data.get('region');
    var mode = data.get('mode');
    panel.setAttribute('provider', provider);
    panel.setAttribute('region', region);

    var url = nxthemesBasePath + "/nxthemes-webwidgets/get_panel_data";
    new Ajax.Request(url, {
         method: 'get',
         parameters: {
             'provider': provider,
             'region': region,
             'mode': mode
         },
         onComplete: function(r) {
             var text = r.responseText;
             var panel_data = text.evalJSON(true);
             panel.innerHTML = "";
             NXThemesWebWidgets.renderPanel(provider, decoration, panel, panel_data);
         }
    });

  },

  teardown: function() {
  }

});

NXThemesWebWidgets.openFactoryPanel = function() {
  $('nxthemesWebWidgetFactoryPanel').show();
  $('nxthemesWebWidgetFactoryPanelStripOpen').hide();
  $('nxthemesWebWidgetFactoryPanelStripClose').show();
    $H(NXThemesWebWidgets.widgets).each(function(item) {
    var widget = item.value;
    widget.onResize();
  });
};

NXThemesWebWidgets.closeFactoryPanel = function() {
  $('nxthemesWebWidgetFactoryPanel').hide();
  $('nxthemesWebWidgetFactoryPanelStripOpen').show();
  $('nxthemesWebWidgetFactoryPanelStripClose').hide();
  $H(NXThemesWebWidgets.widgets).each(function(item) {
    var widget = item.value;
    widget.onResize();
  });
};

// Actions
NXThemes.addActions({
    'insert web widget': NXThemesWebWidgets.addWidget,
    'move web widget': NXThemesWebWidgets.moveWidget
});

NXThemes.registerWidgets({
  "web widget panel": function(def) {
    var widget = NXThemes.Canvas.createNode({
      tag: 'div',
      classes: ['nxthemesWebWidgetContainer'],
      style: {'display': 'none'}
    });
    return new NXThemesWebWidgets.WebWidgetPanel(widget, def);
  }
});

