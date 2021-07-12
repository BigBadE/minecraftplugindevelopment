package com.bigbade.minecraftplugindevelopment.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AsmRemapper {
    @Getter
    private final Map<FoundClass, String> mapping = new HashMap<>();
    private final Map<FoundClass, Map<FoundMember, String>> memberMapping = new HashMap<>();

    public void addClass(String current, String deobfusicated) {
        FoundClass foundClass = new FoundClass(current);
        mapping.put(foundClass, deobfusicated);
        memberMapping.put(foundClass, new HashMap<>());
    }

    public void overwriteClass(FoundClass foundClass, String newName) {
        mapping.remove(foundClass);
        mapping.put(foundClass, newName);
    }

    public void overwriteMember(FoundClass foundClass, MethodInfo methodInfo, String newName) {
        Map<FoundMember, String> members = memberMapping.get(foundClass);
        FoundMember found = null;
        for (Map.Entry<FoundMember, String> entry : members.entrySet()) {
            //Needs to convert obfuscated method name to Spigot method name
            if (entry.getKey() instanceof FoundMethod foundMethod &&
                    foundMethod.getMethodInfo().equalsConvertObfusicated(methodInfo, entry.getValue())) {
                found = entry.getKey();
                break;
            }
        }
        if (found != null) {
            members.remove(found);
        }
        FoundMethod foundMethod = new FoundMethod(foundClass, methodInfo);
        members.put(foundMethod, newName);
    }

    public void addMethod(FoundClass foundClass, MethodInfo descriptor, String deobfusicated) {
        FoundMethod foundMethod = new FoundMethod(foundClass, descriptor);
        memberMapping.get(foundClass).put(foundMethod, deobfusicated);
    }

    public void addField(FoundClass foundClass, String current, Type type, String deobfusicated) {
        FoundField foundField = new FoundField(foundClass, current, type);
        memberMapping.get(foundClass).put(foundField, deobfusicated);
    }

    public Optional<FoundClass> findClass(String name) {
        for (FoundClass foundClass : mapping.keySet()) {
            if (foundClass.getCurrent().equals(name)) {
                return Optional.of(foundClass);
            }
        }
        return Optional.empty();
    }

    public Optional<FoundClass> findClassByDeobfusicated(String name) {
        for (Map.Entry<FoundClass, String> entry : mapping.entrySet()) {
            if (entry.getValue().equals(name)) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    public Remapper build() {
        Map<String, String> output = new HashMap<>();
        for (Map.Entry<FoundClass, String> entry : mapping.entrySet()) {
            output.put(entry.getKey().toString(), entry.getValue());
            //Add anonymous inner classes, starting at 1
            for(int i = 1; i <= entry.getKey().getAnonymousInnerClasses(); i++) {
                output.put(entry.getKey().toString() + '$' + i, entry.getValue() + '$' + i);
            }
        }
        for (Map<FoundMember, String> map : memberMapping.values()) {
            for (Map.Entry<FoundMember, String> entry : map.entrySet()) {
                output.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return new SimpleRemapper(output);
    }


    public enum InputType {
        SPIGOT,
        NOTCHIAN
    }
}

@Getter
class FoundClass {
    private final String current;
    private int anonymousInnerClasses;

    public FoundClass(String current) {
        this.current = current;
    }

    public void addInnerClass() { anonymousInnerClasses += 1; }

    @Override
    public String toString() {
        return current;
    }
}

@RequiredArgsConstructor
@Getter
class FoundMember {
    private final FoundClass owner;
    private final String current;
}

@Getter
class FoundMethod extends FoundMember {
    private final MethodInfo methodInfo;

    public FoundMethod(FoundClass owner, MethodInfo methodInfo) {
        super(owner, methodInfo.name());
        this.methodInfo = methodInfo;
    }

    @Override
    public String toString() {
        return getOwner().getCurrent() + "." + methodInfo;
    }
}

@Getter
class FoundField extends FoundMember {
    private final Type type;

    public FoundField(FoundClass owner, String current, Type type) {
        super(owner, current);
        this.type = type;
    }

    @Override
    public String toString() {
        return getOwner().getCurrent() + "." + getCurrent();
    }
}
