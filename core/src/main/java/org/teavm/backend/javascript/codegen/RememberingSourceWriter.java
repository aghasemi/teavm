/*
 *  Copyright 2023 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.backend.javascript.codegen;

import com.carrotsearch.hppc.IntArrayList;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.teavm.model.FieldReference;
import org.teavm.model.MethodDescriptor;
import org.teavm.model.MethodReference;

public class RememberingSourceWriter extends SourceWriter {
    public static final byte APPEND_CLASS = 0;
    public static final byte APPEND_FIELD = 1;
    public static final byte APPEND_STATIC_FIELD = 2;
    public static final byte APPEND_METHOD = 3;
    public static final byte APPEND_METHOD_BODY = 4;
    public static final byte APPEND_FUNCTION = 5;
    public static final byte APPEND_INIT = 6;
    public static final byte APPEND_CLINIT = 7;
    public static final byte NEW_LINE = 8;
    public static final byte SOFT_NEW_LINE = 9;
    public static final byte WS = 10;
    public static final byte INDENT = 11;
    public static final byte OUTDENT = 12;
    public static final byte ENTER_LOCATION = 13;
    public static final byte EMIT_METHOD = 14;
    public static final byte EMIT_CLASS = 15;
    public static final byte EXIT_LOCATION = 16;
    public static final byte EMIT_LOCATION = 17;
    public static final byte TOKEN_BOUNDARY = 18;

    private StringBuilder out = new StringBuilder();
    private ByteArrayOutputStream commands = new ByteArrayOutputStream();
    private int lastWrittenPos;
    private List<String> strings = new ArrayList<>();
    private List<FieldReference> fields = new ArrayList<>();
    private List<MethodReference> methods = new ArrayList<>();
    private List<MethodDescriptor> methodDescriptors = new ArrayList<>();
    private IntArrayList integers = new IntArrayList();

    @Override
    public SourceWriter append(char c) {
        flushIfTooLong();
        out.append(c);
        return this;
    }

    @Override
    public SourceWriter append(CharSequence csq, int start, int end) {
        flushIfTooLong();
        out.append(csq, start, end);
        return this;
    }

    @Override
    public SourceWriter appendClass(String cls) {
        flush();
        commands.write(APPEND_CLASS);
        strings.add(cls);
        return this;
    }

    @Override
    public SourceWriter appendField(FieldReference field) {
        flush();
        commands.write(APPEND_FIELD);
        fields.add(field);
        return this;
    }

    @Override
    public SourceWriter appendStaticField(FieldReference field) {
        flush();
        commands.write(APPEND_STATIC_FIELD);
        fields.add(field);
        return this;
    }

    @Override
    public SourceWriter appendMethod(MethodDescriptor method) {
        flush();
        commands.write(APPEND_METHOD);
        methodDescriptors.add(method);
        return this;
    }

    @Override
    public SourceWriter appendMethodBody(MethodReference method) {
        flush();
        commands.write(APPEND_METHOD_BODY);
        methods.add(method);
        return this;
    }

    @Override
    public SourceWriter appendFunction(String name) {
        flush();
        commands.write(APPEND_FUNCTION);
        strings.add(name);
        return this;
    }

    @Override
    public SourceWriter appendInit(MethodReference method) {
        flush();
        commands.write(APPEND_INIT);
        methods.add(method);
        return this;
    }

    @Override
    public SourceWriter appendClassInit(String className) {
        flush();
        commands.write(APPEND_CLINIT);
        strings.add(className);
        return this;
    }

    @Override
    public SourceWriter newLine() {
        flush();
        commands.write(NEW_LINE);
        return this;
    }

    @Override
    public SourceWriter ws() {
        flush();
        commands.write(WS);
        return this;
    }

    @Override
    public SourceWriter tokenBoundary() {
        flush();
        commands.write(TOKEN_BOUNDARY);
        return this;
    }

    @Override
    public SourceWriter softNewLine() {
        flush();
        commands.write(SOFT_NEW_LINE);
        return this;
    }

    @Override
    public SourceWriter indent() {
        flush();
        commands.write(INDENT);
        return this;
    }

    @Override
    public SourceWriter outdent() {
        flush();
        commands.write(OUTDENT);
        return this;
    }

    @Override
    public void enterLocation() {
        flush();
        commands.write(ENTER_LOCATION);
    }

    @Override
    public void emitMethod(MethodDescriptor method) {
        flush();
        commands.write(EMIT_METHOD);
        methodDescriptors.add(method);
    }

    @Override
    public void emitClass(String className) {
        flush();
        commands.write(EMIT_CLASS);
        strings.add(className);
    }

    @Override
    public void exitLocation() {
        flush();
        commands.write(EXIT_LOCATION);
    }

    @Override
    public void emitLocation(String fileName, int line) {
        flush();
        commands.write(EMIT_LOCATION);
        strings.add(fileName);
        integers.add(line);
    }

    private void flushIfTooLong() {
        if (out.length() - lastWrittenPos >= 127) {
            flush();
        }
    }

    public void flush() {
        if (out.length() > lastWrittenPos) {
            commands.write(128 | (out.length() - lastWrittenPos));
            lastWrittenPos = out.length();
        }
    }
}
