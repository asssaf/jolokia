package org.jolokia.converter.json;

import org.jolokia.converter.StringToObjectConverter;
import org.json.simple.JSONArray;

import javax.management.AttributeNotFoundException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Stack;

/*
 *  Copyright 2009-2010 Roland Huss
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


/**
 * @author roland
 * @since Apr 19, 2009
 */
public class ArrayExtractor implements Extractor {

    public Class getType() {
        // Special handler, no specific Type
        return null;
    }

    public Object extractObject(ObjectToJsonConverter pConverter, Object pValue, Stack<String> pExtraArgs,boolean jsonify) throws AttributeNotFoundException {
        int length = pConverter.getCollectionLength(Array.getLength(pValue));
        if (!pExtraArgs.isEmpty()) {
            Object obj = Array.get(pValue, Integer.parseInt(pExtraArgs.pop()));
            return pConverter.extractObject(obj,pExtraArgs,jsonify);
        } else {
            if (jsonify) {
                List<Object> ret = new JSONArray();
                for (int i=0;i<length;i++) {
                    Object obj = Array.get(pValue, i);
                    ret.add(pConverter.extractObject(obj,pExtraArgs,jsonify));
                }
                return ret;
            } else {
                return pValue;
            }
        }
    }

    public Object setObjectValue(StringToObjectConverter pConverter, Object pInner, String pAttribute, Object  pValue)
            throws IllegalAccessException, InvocationTargetException {
        Class clazz = pInner.getClass();
        if (!clazz.isArray()) {
            throw new IllegalArgumentException("Not an array to set a value, but " + clazz +
                    ". (index = " + pAttribute + ", value = " +  pValue + ")");
        }
        int idx;
        try {
            idx = Integer.parseInt(pAttribute);
        } catch (NumberFormatException exp) {
            throw new IllegalArgumentException("Non-numeric index for accessing array " + pInner +
                    ". (index = " + pAttribute + ", value to set = " +  pValue + ")",exp);
        }
        Class type = clazz.getComponentType();
        Object value = pConverter.prepareValue(type.getName(), pValue);
        Object oldValue = Array.get(pInner, idx);
        Array.set(pInner, idx, value);
        return oldValue;
    }

    public boolean canSetValue() {
        return true;
    }
}
