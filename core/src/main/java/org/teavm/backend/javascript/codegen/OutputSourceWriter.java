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

import java.io.IOException;
import org.teavm.debugging.information.DebugInformationEmitter;
import org.teavm.model.FieldReference;
import org.teavm.model.MethodDescriptor;
import org.teavm.model.MethodReference;

public class OutputSourceWriter extends SourceWriter implements LocationProvider {
    private final Appendable innerWriter;
    private int indentSize;
    private final NamingStrategy naming;
    private final DebugInformationEmitter debugEmitter;
    private boolean lineStart;
    private boolean minified;
    private final int lineWidth;
    private int column;
    private int line;
    private int offset;

    OutputSourceWriter(NamingStrategy naming, DebugInformationEmitter debugEmitter, Appendable innerWriter,
            int lineWidth) {
        this.naming = naming;
        this.debugEmitter = debugEmitter;
        this.innerWriter = innerWriter;
        this.lineWidth = lineWidth;
    }

    void setMinified(boolean minified) {
        this.minified = minified;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public OutputSourceWriter append(char value) {
        try {
            appendIndent();
            innerWriter.append(value);
            if (value == '\n') {
                newLine();
            } else {
                column++;
                offset++;
            }
            return this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void appendIndent() {
        try {
            if (minified) {
                return;
            }
            if (lineStart) {
                for (int i = 0; i < indentSize; ++i) {
                    innerWriter.append("    ");
                    column += 4;
                    offset += 4;
                }
                lineStart = false;
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public OutputSourceWriter append(CharSequence csq, int start, int end) {
        int last = start;
        for (int i = start; i < end; ++i) {
            if (csq.charAt(i) == '\n') {
                appendSingleLine(csq, last, i);
                newLine();
                last = i + 1;
            }
        }
        appendSingleLine(csq, last, end);
        return this;
    }

    private void appendSingleLine(CharSequence csq, int start, int end) {
        if (start == end) {
            return;
        }
        appendIndent();
        column += end - start;
        offset += end - start;
        try {
            innerWriter.append(csq, start, end);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public SourceWriter appendClass(String cls) {
        return appendName(naming.getNameFor(cls));
    }

    @Override
    public SourceWriter appendField(FieldReference field) {
        return append(naming.getNameFor(field));
    }

    @Override
    public SourceWriter appendStaticField(FieldReference field) {
        return appendName(naming.getFullNameFor(field));
    }

    @Override
    public SourceWriter appendMethod(MethodDescriptor method) {
        return append(naming.getNameFor(method));
    }

    @Override
    public SourceWriter appendMethodBody(MethodReference method) {
        return appendName(naming.getFullNameFor(method));
    }

    @Override
    public SourceWriter appendFunction(String name) {
        return append(naming.getNameForFunction(name));
    }

    @Override
    public SourceWriter appendInit(MethodReference method) {
        return appendName(naming.getNameForInit(method));
    }

    @Override
    public SourceWriter appendClassInit(String className) {
        return appendName(naming.getNameForClassInit(className));
    }

    private SourceWriter appendName(ScopedName name) {
        if (name.scoped) {
            append(naming.getScopeName()).append(".");
        }
        append(name.value);
        return this;
    }

    @Override
    public SourceWriter newLine() {
        try {
            innerWriter.append('\n');
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        column = 0;
        ++line;
        ++offset;
        lineStart = true;
        return this;
    }

    @Override
    public SourceWriter ws() {
        if (column >= lineWidth) {
            newLine();
        } else {
            if (!minified) {
                try {
                    innerWriter.append(' ');
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                column++;
                offset++;
            }
        }
        return this;
    }

    @Override
    public SourceWriter tokenBoundary() {
        if (column >= lineWidth) {
            newLine();
        }
        return this;
    }

    @Override
    public SourceWriter softNewLine() {
        if (!minified) {
            try {
                innerWriter.append('\n');
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            column = 0;
            ++offset;
            ++line;
            lineStart = true;
        }
        return this;
    }

    @Override
    public SourceWriter indent() {
        ++indentSize;
        return this;
    }

    @Override
    public SourceWriter outdent() {
        --indentSize;
        return this;
    }

    @Override
    public void enterLocation() {
        debugEmitter.enterLocation();
    }

    @Override
    public void emitMethod(MethodDescriptor method) {
        debugEmitter.emitMethod(method);
    }

    @Override
    public void emitClass(String className) {
        debugEmitter.emitClass(className);
    }

    @Override
    public void exitLocation() {
        debugEmitter.exitLocation();
    }

    @Override
    public void emitLocation(String fileName, int line) {
        debugEmitter.emitLocation(fileName, line);
    }
}
