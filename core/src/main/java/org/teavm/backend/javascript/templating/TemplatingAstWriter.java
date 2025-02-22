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
package org.teavm.backend.javascript.templating;

import java.io.IOException;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.StringLiteral;
import org.teavm.backend.javascript.codegen.SourceWriter;
import org.teavm.backend.javascript.rendering.AstWriter;
import org.teavm.backend.javascript.rendering.DefaultGlobalNameWriter;
import org.teavm.model.FieldReference;
import org.teavm.model.MethodDescriptor;
import org.teavm.model.MethodReference;

public class TemplatingAstWriter extends AstWriter {
    public TemplatingAstWriter(SourceWriter writer) {
        super(writer, new DefaultGlobalNameWriter(writer));
    }

    @Override
    protected boolean intrinsic(FunctionCall node, int precedence) throws IOException {
        if (node.getTarget() instanceof Name) {
            var name = (Name) node.getTarget();
            if (name.getDefiningScope() == null) {
                return tryIntrinsicName(node, name.getIdentifier());
            }
        }
        return super.intrinsic(node, precedence);
    }

    private boolean tryIntrinsicName(FunctionCall node, String name) throws IOException {
        switch (name) {
            case "teavm_javaClass":
                return writeJavaClass(node);
            case "teavm_javaMethod":
                return writeJavaMethod(node);
            case "teavm_javaConstructor":
                return writeJavaConstructor(node);
            case "teavm_javaClassInit":
                return writeJavaClassInit(node);
            default:
                return false;
        }
    }

    private boolean writeJavaClass(FunctionCall node) throws IOException {
        if (node.getArguments().size() != 1) {
            return false;
        }
        var classArg = node.getArguments().get(0);
        if (!(classArg instanceof StringLiteral)) {
            return false;
        }
        writer.appendClass(((StringLiteral) classArg).getValue());
        return true;
    }

    private boolean writeJavaMethod(FunctionCall node) throws IOException {
        if (node.getArguments().size() != 2) {
            return false;
        }
        var classArg = node.getArguments().get(0);
        var methodArg = node.getArguments().get(1);
        if (!(classArg instanceof StringLiteral) || !(methodArg instanceof StringLiteral)) {
            return false;
        }
        var method = new MethodReference(((StringLiteral) classArg).getValue(),
                MethodDescriptor.parse(((StringLiteral) methodArg).getValue()));
        writer.appendMethodBody(method);
        return true;
    }

    private boolean writeJavaConstructor(FunctionCall node) throws IOException {
        if (node.getArguments().size() != 2) {
            return false;
        }
        var classArg = node.getArguments().get(0);
        var methodArg = node.getArguments().get(1);
        if (!(classArg instanceof StringLiteral) || !(methodArg instanceof StringLiteral)) {
            return false;
        }
        var method = new MethodReference(((StringLiteral) classArg).getValue(), "<init>",
                MethodDescriptor.parseSignature(((StringLiteral) methodArg).getValue()));
        writer.appendInit(method);
        return true;
    }

    private boolean writeJavaClassInit(FunctionCall node) throws IOException {
        if (node.getArguments().size() != 1) {
            return false;
        }
        var classArg = node.getArguments().get(0);
        if (!(classArg instanceof StringLiteral)) {
            return false;
        }
        writer.appendClassInit(((StringLiteral) classArg).getValue());
        return true;
    }

    @Override
    protected void print(ElementGet node) throws IOException {
        if (node.getElement() instanceof FunctionCall) {
            var call = (FunctionCall) node.getElement();
            if (call.getTarget() instanceof Name) {
                var name = (Name) call.getTarget();
                if (name.getDefiningScope() == null) {
                    switch (name.getIdentifier()) {
                        case "teavm_javaVirtualMethod":
                            if (writeJavaVirtualMethod(node, call)) {
                                return;
                            }
                            break;
                        case "teavm_javaField":
                            if (writeJavaField(node, call)) {
                                return;
                            }
                            break;
                    }
                }
            }
        }
        super.print(node);
    }

    private boolean writeJavaVirtualMethod(ElementGet get, FunctionCall call) throws IOException {
        var arg = call.getArguments().get(0);
        if (!(arg instanceof StringLiteral)) {
            return false;
        }
        var method = MethodDescriptor.parse(((StringLiteral) arg).getValue());
        print(get.getTarget());
        writer.append('.').appendMethod(method);
        return true;
    }

    private boolean writeJavaField(ElementGet get, FunctionCall call) throws IOException {
        if (call.getArguments().size() != 2) {
            return false;
        }
        var classArg = call.getArguments().get(0);
        var fieldArg = call.getArguments().get(1);
        if (!(classArg instanceof StringLiteral) || !(fieldArg instanceof StringLiteral)) {
            return false;
        }
        var className = ((StringLiteral) classArg).getValue();
        var fieldName = ((StringLiteral) fieldArg).getValue();
        print(get.getTarget());
        writer.append('.').appendField(new FieldReference(className, fieldName));
        return true;
    }
}
