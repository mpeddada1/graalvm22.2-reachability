/*
 * Copyright 2022 Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google LLC nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.example;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;


final class MyNativeImageFeature implements Feature {

    private static final String GENERATED_MESSAGE_CLASS = "com.anotherpackage.GeneratedMessage";

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        Class<?> generatedMessageClass = access.findClassByName(GENERATED_MESSAGE_CLASS);
        if (generatedMessageClass != null) {
            Method internalAccessorMethod =
                    getMethodOrFail(generatedMessageClass, "invokeAccessor");

            // Finds every class whose `invokeAccessor()` is reached and registers it.
            // `invokeAccessor()` is used downstream to access the class reflectively.
            access.registerMethodOverrideReachabilityHandler(
                    (duringAccess, method) -> {
                        System.out.println("******THIS POINT IS REACHED IN GRAALVM 22.1 but NOT in 22.2*******");
                        registerFieldAccessors(method.getDeclaringClass());
                    },
                    internalAccessorMethod);
        }
    }

    /** Given a class, registers the public accessor methods for the provided class. */
    private static void registerFieldAccessors(Class<?> protoClass) {
        for (Method method : protoClass.getMethods()) {
            if (method.getName().startsWith("get")) {
                RuntimeReflection.register(method);
            }
        }
    }

    /** Returns the method of a class or fails if it is not present. */
    public static Method getMethodOrFail(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getDeclaredMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    String.format("Failed to find method %s for class %s", methodName, clazz.getName()), e);
        }
    }
}


