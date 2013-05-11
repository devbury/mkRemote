/*
 * Copyright (c) 2009-2013 devBury LLC
 *
 *   This file is part of mkRemote.
 *
 *   mkRemote is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License Version 3
 *   as published by the Free Software Foundation.
 *
 *   mkRemote is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with mkRemote.  If not, see <http://www.gnu.org/licenses/gpl.txt/>.
 */

package com.devbury.desktoplib.spring;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ObjectUtils;

import com.devbury.desktoplib.properties.ApplicationPropertiesLoader;

public class LogFilterPropertyPlaceholderConfigurer implements BeanFactoryPostProcessor, BeanNameAware {

    protected List<String> filters = new LinkedList<String>();
    protected String requiredPrefix = "${";
    protected String optionalPrefix = "${opt:";
    protected String defaultPrefix = "${default:";
    protected String suffix = "}";
    protected Properties properties;
    protected Logger logger = LoggerFactory.getLogger(LogFilterPropertyPlaceholderConfigurer.class);
    private String beanName;

    public LogFilterPropertyPlaceholderConfigurer() {
        setPropertiesFiles(ApplicationPropertiesLoader.getApplicationPropertiesFiles());
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) throws BeansException {
        BeanDefinitionVisitor visitor = newVisitor();
        String[] beanNames = bf.getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            // Check that we're not parsing our own bean definition,
            // to avoid failing on unresolvable placeholders in properties file
            // locations.
            if (!(beanNames[i].equals(beanName))) {
                BeanDefinition bd = bf.getBeanDefinition(beanNames[i]);
                try {
                    visitor.visitBeanDefinition(bd);
                } catch (BeanDefinitionStoreException ex) {
                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanNames[i], ex.getMessage());
                }
            }
        }
    }

    protected BeanDefinitionVisitor newVisitor() {
        return new PlaceholderResolvingBeanDefinitionVisitor();
    }

    public void setPropertiesFiles(Collection<String> files) {
        properties = new Properties();
        Iterator<String> it = files.iterator();
        while (it.hasNext()) {
            String prop_file = it.next();
            if (!prop_file.startsWith("/") && !prop_file.startsWith(":/", 1) && !prop_file.startsWith(":\\", 1)) {
                prop_file = System.getProperty("user.home") + '/' + prop_file;
            }
            logger.debug("Loading properties from {}", prop_file);
            try {
                InputStream is = new BufferedInputStream(new FileInputStream(prop_file));
                properties.load(is);
                is.close();
            } catch (Exception e) {
                logger.debug("{} could not be loaded", prop_file);
            }
        }
    }

    public void setPropertiesFile(String prop_file) {
        LinkedList<String> l = new LinkedList<String>();
        l.add(prop_file);
        setPropertiesFiles(l);
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public String getOptionalPrefix() {
        return optionalPrefix;
    }

    public void setOptionalPrefix(String optionalPrefix) {
        this.optionalPrefix = optionalPrefix;
    }

    public String getRequiredPrefix() {
        return requiredPrefix;
    }

    public void setRequiredPrefix(String requiredPrefix) {
        this.requiredPrefix = requiredPrefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getdefaultPrefix() {
        return defaultPrefix;
    }

    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }

    protected class PlaceholderResolvingBeanDefinitionVisitor extends BeanDefinitionVisitor {

        @Override
        protected void visitPropertyValues(MutablePropertyValues pvs) {
            PropertyValue[] pvArray = pvs.getPropertyValues();
            for (int i = 0; i < pvArray.length; i++) {
                PropertyValue pv = pvArray[i];
                try {
                    Object newVal = resolveValue(pv.getValue());
                    if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
                        pvs.addPropertyValue(pv.getName(), newVal);
                    }
                } catch (BeanDefinitionStoreException e) {
                    if (pv.getValue().toString().indexOf(optionalPrefix) != -1) {
                        // remove the property
                        logger.debug("Removing optional property " + pv.getName() + " because we could not resolve "
                                + pv.getValue());
                        pvs.removePropertyValue(pv);
                    } else {
                        throw e;
                    }
                }
            }
        }

        @Override
        protected String resolveStringValue(String property_name) {
            if (property_name == null) {
                return null;
            }
            if (property_name.startsWith(defaultPrefix)) {
                String var = property_name.substring(property_name.lastIndexOf(':') + 1, property_name.indexOf(suffix));
                String original_without_default = property_name.substring(0, defaultPrefix.length())
                        + property_name.substring(property_name.lastIndexOf(':') + 1);
                try {
                    return getProperty(var, original_without_default);
                } catch (BeanDefinitionStoreException e) {
                    // value not in properties. use specified default
                    String ret = property_name.substring(defaultPrefix.length(), property_name.lastIndexOf(':'));
                    logger
                            .debug("Resolved " + original_without_default + " to default value of "
                                    + showValue(var, ret));
                    return ret;
                }
            }
            if (property_name.startsWith(optionalPrefix)) {
                String var = property_name.substring(optionalPrefix.length(), property_name.indexOf(suffix));
                return getProperty(var, property_name);
            }
            if (property_name.startsWith(requiredPrefix)) {
                String var = property_name.substring(requiredPrefix.length(), property_name.indexOf(suffix));
                return getProperty(var, property_name);
            }
            return property_name;
        }

        protected String getProperty(String property_name, String original_name) {
            String prop = properties.getProperty(property_name);
            if (prop == null) {
                throw new BeanDefinitionStoreException("Could not resolve property " + original_name);
            }
            logger.debug("Resolved " + original_name + " to " + showValue(property_name, prop));
            return prop;
        }

        protected Object showValue(Object name, Object value) {
            Iterator<String> it = filters.iterator();
            while (it.hasNext()) {
                String filter = it.next();
                if (name.toString().toUpperCase().indexOf(filter.toUpperCase()) != -1) {
                    // we found a match. Don't display the true value
                    return "******";
                }
            }
            return value;
        }
    }
}
