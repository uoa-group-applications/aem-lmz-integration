package nz.ac.auckland.aem.beans.aem;

import org.apache.commons.lang3.StringUtils;

import javax.jcr.PropertyType;

/**
 * Created by gregkw on 11/12/14.
 */
public class JCRProperty {
    private String name;
    private String type;
    private String value;

    public JCRProperty(String name, String type, String value) {
        setName(name);
        setType(type);
        setValue(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(type) && StringUtils.isNotBlank(value);
    }

    public int getJCRMappedType() {
//        PropertyType.STRING
//        PropertyType.DATE
//        PropertyType.BINARY
//        PropertyType.DOUBLE
//        PropertyType.DECIMAL
//        PropertyType.LONG
//        PropertyType.BOOLEAN
//        PropertyType.NAME
//        PropertyType.PATH
//        PropertyType.REFERENCE
//        PropertyType.WEAKREFERENCE
//        PropertyType.URI
        return PropertyType.STRING;
    }
}
