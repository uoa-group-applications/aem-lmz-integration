package nz.ac.auckland.aem.lmz.lmzconfigdialog;

import nz.ac.auckland.aem.beans.aem.*;
import nz.ac.auckland.lmzwidget.configuration.model.WidgetConfiguration;
import nz.ac.auckland.lmzwidget.configuration.model.WidgetItem;
import nz.ac.auckland.lmzwidget.configuration.model.WidgetTab;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import javax.jcr.Node;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by gregkw on 11/12/14.
 */
public class LMZConfigDialogFactory {

    private WidgetConfiguration widgetConfiguration;
    private String widgetURL;

    private WidgetConfiguration wc;
    public WidgetConfiguration getWidgetConfiguration() {
        return widgetConfiguration;
    }

    public void setWidgetConfiguration(WidgetConfiguration widgetConfiguration) {
        this.widgetConfiguration = widgetConfiguration;
        this.wc = widgetConfiguration;
    }

    private CQDialog createCQDialog(Node node, String desc) {
        CQDialog fakeVal = new CQDialog(node);
        CQDialog retVal = new CQDialog(node);
        retVal.addProperty("title", "String", desc);
        retVal.addProperty("xtype", "String", "dialog");
        fakeVal.addChildWidget(retVal);
        return retVal;
    }

    public CQDialog createCQDialog(Node node) {

        CQDialog retVal = createCQDialog(node, wc.getWidget().getDescription());

        CQWidgetCollection items = new CQWidgetCollection("items");
        retVal.addChildWidget(items);

        CQTabPanel tabs = new CQTabPanel("tabs");
        items.addChildWidget(tabs);

        CQWidgetCollection panelItems = new CQWidgetCollection("items");
        tabs.addChildWidget(panelItems);

        addMetadataTab(panelItems);

        for (Map.Entry<String, WidgetTab> widgetTab : wc.getConfiguration().entrySet()) {
            // not valid to add? skip it.
            if (widgetTab.getValue().getItems().isEmpty()) {
                continue;
            }

            CQPanel tabBasic = new CQPanel(widgetTab.getKey());
            tabBasic.addProperty("title", "String", widgetTab.getValue().getLabel());
            panelItems.addChildWidget(tabBasic);

            CQWidgetCollection widgetItems = new CQWidgetCollection("items");
            tabBasic.addChildWidget(widgetItems);
            for (Map.Entry<String, WidgetItem> widgetItem : widgetTab.getValue().getItems().entrySet()){

                //add the WidgetItems to the widgetItem map
                CQWidget cqWidget =  createCQWidget(widgetItem.getKey(), widgetItem.getValue());
                widgetItems.addChildWidget(cqWidget);

                WidgetItem item = widgetItem.getValue();
                if (hasOptions(item)) {
                    createOptionsDropdown(cqWidget, item);
                }
            }
        }


        return retVal;
    }

    protected boolean hasOptions(WidgetItem item) {
        return item.getOptions() != null && !item.getOptions().isEmpty();
    }

    protected void createOptionsDropdown(CQWidget cqWidget, WidgetItem item) {
        CQWidgetCollection options = new CQWidgetCollection("options");
        cqWidget.addChildWidget(options);
        int count = 0;
        for (Map.Entry<String, String> option : item.getOptions().entrySet()) {
            String optName = "option"+(++count);
            NTUnstructured ntu = new NTUnstructured(optName);
            ntu.addProperty("text", new JCRProperty("text", "String", option.getKey()));
            ntu.addProperty("value", new JCRProperty("value", "String", option.getValue()));
            options.addChildWidget(ntu);
        }
    }

    protected void addMetadataTab(CQWidgetCollection panelItems) {
        //add the widgetMetadataTab
        CQPanel tabMetadata = new CQPanel("metadata");
        tabMetadata.addProperty("title", "String", "Metadata");
        panelItems.addChildWidget(tabMetadata);

        tabMetadata.makeAdminOnly();

        CQWidgetCollection mdWidgetItems = new CQWidgetCollection("items");
        tabMetadata.addChildWidget(mdWidgetItems);

        mdWidgetItems.addChildWidget(
                readOnly(createCQWidget("md_name", "Name", widgetConfiguration.getWidget().getName()))
            );

        mdWidgetItems.addChildWidget(
                readOnly(createCQWidget("md_description", "Description", widgetConfiguration.getWidget().getDescription()))
            );

        mdWidgetItems.addChildWidget(
                readOnly(createCQWidget("md_version", "Version", widgetConfiguration.getWidget().getVersion()))
            );

        mdWidgetItems.addChildWidget(
                readOnly(createCQWidget("md_url", "URL", getWidgetURL()))
            );
    }

    public CQWidget readOnly(CQWidget inputWidget) {
        inputWidget.addProperty("readOnly", "Boolean", "{Boolean}true");
        return inputWidget;
    }

    public CQWidget createCQWidget(String name, String stringValue) {
        return createCQWidget(name, name, stringValue);
    }

    public CQWidget createCQWidget(String name, String label, String stringValue) {
        CQWidget retVal = new CQWidget(name);
        retVal.addProperty("defaultValue", "String", stringValue);
        retVal.addProperty("fieldLabel", "String", label);
        retVal.addProperty("name", "String", "./" + name);
        retVal.addProperty("xtype", "String", "textfield");

        return retVal;
    }

    public CQWidget createCQWidget(String name, WidgetItem item) {
        String[] VALID_TYPES = new String[] {"bool",         "dropdown",     "number",       "string",       "textarea"};
        String[] X_TYPES = new String[]     {"selection",    "selection",    "numberfield",  "textfield",    "textarea"};
        String[] TYPES = new String[]       {"checkbox",     "select",       null,           null,           null};

        int type = Arrays.binarySearch(VALID_TYPES, item.getType());

        if (StringUtils.isBlank(name)) {
            name = camelCaseString(item.getLabel());
        }

        CQWidget retVal = new CQWidget(name);
        retVal.addProperty("defaultValue", "String", item.getDefaultValue());
        retVal.addProperty("fieldDescription", "String", item.getDescription());
        retVal.addProperty("fieldLabel", "String", item.getLabel());
        retVal.addProperty("allowBlank", "Boolean", item.isRequired() ? "false" : "true");
        retVal.addProperty("name", "String", "./" + name);
        if (item.getPattern() != null) {
            retVal.addProperty("regex", "String", item.PATTERNS.get(item.getPattern()));
            retVal.addProperty("regexText", "String", "Please enter a valid " + item.getPattern() + " address.");
        }
        retVal.addProperty("xtype", "String", X_TYPES[type]);
        retVal.addProperty("type", "String", TYPES[type]);

        return retVal;
    }

    public static String camelCaseString(String text) {
        return StringUtils.uncapitalize(StringUtils.remove(WordUtils.capitalizeFully(StringUtils.stripToEmpty(text)), " "));
    }


    public String getWidgetURL() {
        return widgetURL;
    }

    public void setWidgetURL(String widgetURL) {
        this.widgetURL = widgetURL;
    }

}
