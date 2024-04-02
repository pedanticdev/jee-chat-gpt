package fish.payara.primefaces;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;

@Named
@SessionScoped
public class GuestPreferences implements Serializable {

    private Map<String, String> themeColors;

    private String theme = "deeppurple";

    private String layout = "calm";

    private String menuClass = "layout-menu-dark";

    private String profileMode = "overlay";

    private String menuLayout = "static";

    private String inputStyle = "outlined";

    private final List<ComponentTheme> componentThemes = new ArrayList<>();

    private final List<LayoutTheme> layoutThemes = new ArrayList<>();

    private final List<LayoutSpecialTheme> layoutSpecialThemes = new ArrayList<>();

    @PostConstruct
    public void init() {
        themeColors = new HashMap<>();
        themeColors.put("blue", "#03A9F4");

        componentThemes.add(new ComponentTheme("Amber", "amber", "#F8BD0C"));
        componentThemes.add(new ComponentTheme("Blue", "blue", "#007bff"));
        componentThemes.add(new ComponentTheme("Cyan", "cyan", "#17A2B8"));
        componentThemes.add(new ComponentTheme("Indigo", "indigo", "#6610F2"));
        componentThemes.add(new ComponentTheme("Purple", "purple", "#883cae"));
        componentThemes.add(new ComponentTheme("Teal", "teal", "#20C997"));
        componentThemes.add(new ComponentTheme("Orange", "orange", "#FD7E14"));
        componentThemes.add(new ComponentTheme("Deep Purple", "deeppurple", "#612FBE"));
        componentThemes.add(new ComponentTheme("Light Blue", "lightblue", "#4DA3FF"));
        componentThemes.add(new ComponentTheme("Green", "green", "#28A745"));
        componentThemes.add(new ComponentTheme("Light Green", "lightgreen", "#61CC79"));
        componentThemes.add(new ComponentTheme("Brown", "brown", "#986839"));
        componentThemes.add(new ComponentTheme("Dark Grey", "darkgrey", "#6C757D"));
        componentThemes.add(new ComponentTheme("Pink", "pink", "#E83E8C"));
        componentThemes.add(new ComponentTheme("Lime", "lime", "#74CD32"));

        layoutThemes.add(new LayoutTheme("Blue", "blue", "#146fd7"));
        layoutThemes.add(new LayoutTheme("Cyan", "cyan", "#0A616F"));
        layoutThemes.add(new LayoutTheme("Indigo", "indigo", "#470EA2"));
        layoutThemes.add(new LayoutTheme("Purple", "purple", "#391F68"));
        layoutThemes.add(new LayoutTheme("Teal", "teal", "#136E52"));
        layoutThemes.add(new LayoutTheme("Pink", "pink", "#771340"));
        layoutThemes.add(new LayoutTheme("Lime", "lime", "#407916"));
        layoutThemes.add(new LayoutTheme("Green", "green", "#1F8E38"));
        layoutThemes.add(new LayoutTheme("Amber", "amber", "#7A5E06"));
        layoutThemes.add(new LayoutTheme("Brown", "brown", "#593E22"));
        layoutThemes.add(new LayoutTheme("Orange", "orange", "#904100"));
        layoutThemes.add(new LayoutTheme("Deep Purple", "deeppurple", "#341A64"));
        layoutThemes.add(new LayoutTheme("Light Blue", "lightblue", "#14569D"));
        layoutThemes.add(new LayoutTheme("Light Green", "lightgreen", "#2E8942"));
        layoutThemes.add(new LayoutTheme("Dark Grey", "darkgrey", "#343A40"));

        layoutSpecialThemes.add(new LayoutSpecialTheme("Influenza", "influenza", "#a83279", "#f38e00"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Calm", "calm", "#5f2c82", "#0DA9A4"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Crimson", "crimson", "#521c52", "#c6426e"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Night", "night", "#2c0747", "#6441a5"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Skyline", "skyline", "#2b32b2", "#1488cc"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Sunkist", "sunkist", "#ee8a21", "#f2c94c"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Little Leaf", "littleleaf", "#4DB865", "#80D293"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Joomla", "joomla", "#1e3c72", "#2a5298"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Firewatch", "firewatch", "#cb2d3e", "#ef473a"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Suzy", "suzy", "#834d9b", "#d04ed6"));
    }

    public void onLayoutChange() {
        PrimeFaces.current().executeScript("PrimeFaces.AvalonConfigurator.changeMenuLayout('" + menuLayout + "')");
    }

    public void onMenuThemeChange() {
        if ("layout-menu-dark".equals(menuClass)) {
            PrimeFaces.current().executeScript("PrimeFaces.AvalonConfigurator.changeMenuToDark()");
        } else {
            PrimeFaces.current().executeScript("PrimeFaces.AvalonConfigurator.changeMenuToLight()");
        }
    }

    public String getMenuClass() {
        return this.menuClass;
    }

    public void setMenuClass(String menuClass) {
        this.menuClass = menuClass;
    }

    public String getProfileMode() {
        return this.profileMode;
    }

    public void setProfileMode(String profileMode) {
        this.profileMode = profileMode;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout, boolean special) {
        this.layout = layout;
        if (special) {
            this.menuClass = "layout-menu-dark";
        }
    }

    public String getMenuLayout() {
        return this.menuLayout;
    }

    public String getMenu() {
        switch (this.menuLayout) {
            case "overlay":
                return "menu-layout-overlay";
            case "horizontal":
                this.profileMode = "overlay";
                return "menu-layout-static menu-layout-horizontal";
            case "slim":
                return "menu-layout-static menu-layout-slim";
            default:
                return "menu-layout-static";
        }
    }

    public void setMenuLayout(String menuLayout) {
        if (menuLayout.equals("horizontal")) {
            this.profileMode = "overlay";
        }

        this.menuLayout = menuLayout;
    }

    public String getInputStyleClass() {
        return this.inputStyle.equals("filled") ? "ui-input-filled" : "";
    }

    public String getInputStyle() {
        return inputStyle;
    }

    public void setInputStyle(String inputStyle) {
        this.inputStyle = inputStyle;
    }

    public Map getThemeColors() {
        return this.themeColors;
    }

    public List<LayoutTheme> getLayoutThemes() {
        return layoutThemes;
    }

    public List<LayoutSpecialTheme> getLayoutSpecialThemes() {
        return layoutSpecialThemes;
    }

    public List<ComponentTheme> getComponentThemes() {
        return componentThemes;
    }

    public static class ComponentTheme {

        String name;
        String file;
        String color;

        public ComponentTheme(String name, String file, String color) {
            this.name = name;
            this.file = file;
            this.color = color;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public String getColor() {
            return this.color;
        }
    }

    public static class LayoutTheme {

        String name;
        String file;
        boolean special = false;
        String color;

        public LayoutTheme(String name, String file, String color) {
            this.name = name;
            this.file = file;
            this.color = color;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public boolean isSpecial() {
            return this.special;
        }

        public String getColor() {
            return color;
        }
    }

    public static class LayoutSpecialTheme {

        String name;
        String file;
        boolean special = true;
        String color1;
        String color2;

        public LayoutSpecialTheme(String name, String file, String color1, String color2) {
            this.name = name;
            this.file = file;
            this.color1 = color1;
            this.color2 = color2;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public String getColor1() {
            return this.color1;
        }

        public String getColor2() {
            return this.color2;
        }

        public boolean isSpecial() {
            return this.special;
        }
    }
}
