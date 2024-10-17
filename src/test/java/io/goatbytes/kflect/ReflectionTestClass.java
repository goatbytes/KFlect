/*
 * Copyright (c) 2024 GoatBytes.IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package io.goatbytes.kflect;

import kotlin.Suppress;

@Suppress(names = "detekt.all")
public class ReflectionTestClass {

    // Fields with different modifiers
    private static final String STATIC_FINAL_FIELD = "staticFinalField";
    private static String staticField = "staticField";
    private final String finalField;
    private String instanceField;
    private volatile String volatileField;

    // Constructor
    public ReflectionTestClass(String finalField, String instanceField) {
        this.finalField = finalField;
        this.instanceField = instanceField;
        this.volatileField = "volatileField";
    }

    // Static methods
    public static String getStaticField() {
        return staticField;
    }

    public static void setStaticField(String staticField) {
        ReflectionTestClass.staticField = staticField;
    }

    public static String getStaticFinalField() {
        return STATIC_FINAL_FIELD;
    }

    // Instance methods
    public String getInstanceField() {
        return instanceField;
    }

    public void setInstanceField(String instanceField) {
        this.instanceField = instanceField;
    }

    public String getFinalField() {
        return finalField;
    }

    public String getVolatileField() {
        return volatileField;
    }

    public void setVolatileField(String volatileField) {
        this.volatileField = volatileField;
    }

    public void overloadedMethod() {
        System.out.println("overloadedMethod() called");
    }

    public String overloadedMethod(String arg) {
        return "overloadedMethod() called with: arg = [" + arg + "]";
    }
}
