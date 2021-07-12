package com.bigbade.minecraftplugindevelopment.common;

import lombok.Getter;
import org.objectweb.asm.Type;

import java.util.List;

public record MethodInfo(@Getter String name, Type returnType,
                         List<Type> parameters) {

    public boolean equalsConvertObfusicated(MethodInfo other, String obfusicatedName) {
        return name.equals(obfusicatedName) && other.returnType.equals(returnType) && other.parameters.equals(parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodInfo methodInfo)) return false;
        return methodInfo.name.equals(name) && methodInfo.returnType.equals(returnType) &&
                methodInfo.parameters.equals(parameters);
    }

    @Override
    public int hashCode() {
        return name.hashCode() + returnType.hashCode() + parameters.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder(name).append("(");
        for (Type parameter : parameters) {
            output.append(parameter.getDescriptor());
        }
        output.append(")").append(returnType.getDescriptor());
        return output.toString();
    }
}
