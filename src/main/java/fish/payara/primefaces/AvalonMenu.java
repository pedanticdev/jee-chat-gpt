/*
  Copyright 2009-2022 PrimeTek.

  Licensed under PrimeFaces Commercial License, Version 1.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  Licensed under PrimeFaces Commercial License, Version 1.0 (the "License");

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package fish.payara.primefaces;

import jakarta.faces.component.*;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.*;
import org.primefaces.component.api.Widget;
import org.primefaces.component.menu.AbstractMenu;

@ListenerFor(sourceClass = AvalonMenu.class, systemEventClass = PostAddToViewEvent.class)
@FacesComponent(AvalonMenu.COMPONENT_TYPE)
public class AvalonMenu extends AbstractMenu implements Widget, ComponentSystemEventListener {

    public static final String COMPONENT_TYPE = "org.primefaces.component.AvalonMenu";
    public static final String COMPONENT_FAMILY = "org.primefaces.component";
    public static final String DEFAULT_RENDERER = "org.primefaces.component.AvalonMenuRenderer";
    private static final String[] LEGACY_RESOURCES =
            new String[] {"primefaces.css", "jquery/jquery.js", "jquery/jquery-plugins.js", "primefaces.js"};
    private static final String[] MODERN_RESOURCES =
            new String[] {"components.css", "jquery/jquery.js", "jquery/jquery-plugins.js", "core.js"};

    protected enum PropertyKeys {
        widgetVar,
        model,
        style,
        styleClass,
        statefulScroll;

        String toString;

        PropertyKeys(String toString) {
            this.toString = toString;
        }

        PropertyKeys() {}

        public String toString() {
            return ((this.toString != null) ? this.toString : super.toString());
        }
    }

    public AvalonMenu() {
        setRendererType(DEFAULT_RENDERER);
    }

    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public String getWidgetVar() {
        return (String) getStateHelper().eval(PropertyKeys.widgetVar, null);
    }

    public void setWidgetVar(String _widgetVar) {
        getStateHelper().put(PropertyKeys.widgetVar, _widgetVar);
    }

    public org.primefaces.model.menu.MenuModel getModel() {
        return (org.primefaces.model.menu.MenuModel) getStateHelper().eval(PropertyKeys.model, null);
    }

    public void setModel(org.primefaces.model.menu.MenuModel _model) {
        getStateHelper().put(PropertyKeys.model, _model);
    }

    public String getStyle() {
        return (String) getStateHelper().eval(PropertyKeys.style, null);
    }

    public void setStyle(String _style) {
        getStateHelper().put(PropertyKeys.style, _style);
    }

    public String getStyleClass() {
        return (String) getStateHelper().eval(PropertyKeys.styleClass, null);
    }

    public void setStyleClass(String _styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, _styleClass);
    }

    public Boolean isStatefulScroll() {
        return (Boolean) getStateHelper().eval(PropertyKeys.statefulScroll, true);
    }

    public void setStatefulScroll(boolean _statefulScroll) {
        getStateHelper().put(PropertyKeys.statefulScroll, _statefulScroll);
    }

    public String resolveWidgetVar() {
        FacesContext context = getFacesContext();
        String userWidgetVar = (String) getAttributes().get("widgetVar");

        if (userWidgetVar != null) {
            return userWidgetVar;
        } else {
            return "widget_" + getClientId(context).replaceAll("-|" + UINamingContainer.getSeparatorChar(context), "_");
        }
    }

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        if (event instanceof PostAddToViewEvent) {
            FacesContext context = getFacesContext();
            UIViewRoot root = context.getViewRoot();

            boolean isPrimeConfig;
            try {
                isPrimeConfig = Class.forName("org.primefaces.config.PrimeConfiguration") != null;
            } catch (ClassNotFoundException e) {
                isPrimeConfig = false;
            }

            String[] resources = (isPrimeConfig) ? MODERN_RESOURCES : LEGACY_RESOURCES;

            for (String res : resources) {
                UIComponent component = context.getApplication().createComponent(UIOutput.COMPONENT_TYPE);
                if (res.endsWith("css")) component.setRendererType("jakarta.faces.resource.Stylesheet");
                else if (res.endsWith("js")) component.setRendererType("jakarta.faces.resource.Script");

                component.getAttributes().put("library", "primefaces");
                component.getAttributes().put("name", res);

                root.addComponentResource(context, component);
            }
        }
    }
}
