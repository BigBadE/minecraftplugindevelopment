package com.bigbade.minecraftplugindevelopment.common;

import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public abstract class MappingParser {
    private static final Pattern METHOD_CLOSE_PATTERN = Pattern.compile("\\)");
    private static final Pattern INTERNAL_SEPERATOR_PATTERN = Pattern.compile(";");
    private static final Pattern MOJANG_SEPERATOR_PATTERN = Pattern.compile(",");

    public static final Pattern METHOD_PATTERN = Pattern.compile("\\(");
    public static final Pattern SPACE_PATTERN = Pattern.compile(" ");

    protected final AsmRemapper remapper;

    public static MappingParser getMappingParser(AsmRemapper.InputType inputType, AsmRemapper remapper, int flags) {
        if(inputType == AsmRemapper.InputType.SPIGOT) {
            return new SpigotMappingParser(remapper, flags);
        } else if(inputType == AsmRemapper.InputType.NOTCHIAN) {
            return new MojangMappingParser(remapper);
        }
        throw new IllegalStateException("No mapper for input type " + inputType);
    }

    protected MappingParser(AsmRemapper remapper) {
        this.remapper = remapper;
    }

    public void parse(BufferedReader reader) throws IOException {
        String line;
        while((line = reader.readLine()) != null) {
            if(line.charAt(0) == '#') continue;
            parseLine(remapper, line);
        }
    }

    abstract void parseLine(AsmRemapper remapper, String line);

    abstract AsmRemapper.InputType getInputType();

    abstract void parseMethod(String method);

    abstract void parseField(String method);

    public Type getType(String descriptor) {
        if(getInputType() == AsmRemapper.InputType.NOTCHIAN) {
            StringBuilder output = new StringBuilder();
            while (descriptor.endsWith("[]")) {
                output.append("[");
                descriptor = descriptor.substring(0, descriptor.length()-2);
            }
            switch (descriptor) {
                case "byte":
                    output.append("B");
                    break;
                case "char":
                    output.append("C");
                    break;
                case "double":
                    output.append("D");
                    break;
                case "float":
                    output.append("F");
                    break;
                case "int":
                    output.append("I");
                    break;
                case "long":
                    output.append("J");
                    break;
                case "short":
                    output.append("S");
                    break;
                case "void":
                    output.append("V");
                    break;
                case "boolean":
                    output.append("Z");
                    break;
                default:
                    output.append("L");
                    if(getInputType() == AsmRemapper.InputType.NOTCHIAN &&
                            (descriptor.startsWith("com/mojang/math") || descriptor.startsWith("net/minecraft"))) {
                        Optional<FoundClass> foundClassOptional = remapper.findClassByDeobfusicated(descriptor);
                        if(foundClassOptional.isPresent()) {
                            descriptor = foundClassOptional.get().getCurrent();
                        }
                    }
                    output.append(descriptor).append(";");
            }
            descriptor = output.toString();
        }
        return Type.getType(descriptor);
    }

    public MethodInfo getMethodInfo(String descriptor) {
        //0: Method name 1: descriptor)returntype
        //Must use original mappings
        String[] methodData = METHOD_PATTERN.split(descriptor);
        String[] typeData = METHOD_CLOSE_PATTERN.split(methodData[1]);
        List<Type> params = new ArrayList<>();
        if(getInputType() == AsmRemapper.InputType.SPIGOT) {
            for(String found : INTERNAL_SEPERATOR_PATTERN.split(typeData[0])) {
                if(!found.isEmpty()) {
                    params.add(getType(found + ";"));
                }
            }
        } else {
            for(String found : MOJANG_SEPERATOR_PATTERN.split(typeData[0])) {
                if(!found.isEmpty()) {
                    params.add(getType(found));
                }
            }
        }
        return new MethodInfo(methodData[0], getType(typeData[1]), params);
    }
}

class SpigotMappingParser extends MappingParser {
    private final boolean classes;

    protected SpigotMappingParser(AsmRemapper remapper, int flags) {
        super(remapper);
        this.classes = (flags & 1) != 0;
    }

    @Override
    void parseLine(AsmRemapper remapper, String line) {
        if(classes) {
            String[] split = MappingParser.SPACE_PATTERN.split(line);
            remapper.addClass(split[1], split[0]);
        } else {
            parseMethod(line);
        }
    }

    @Override
    AsmRemapper.InputType getInputType() {
        return AsmRemapper.InputType.SPIGOT;
    }

    @Override
    void parseMethod(String method) {
        String[] split = SPACE_PATTERN.split(method);
        FoundClass foundClass = remapper.findClass(split[0])
                .orElseThrow(() -> new IllegalStateException("Unknown class " + split[0]));
        if(foundClass.getCurrent().equals("net/minecraft/world/entity/ai/village/poi/VillagePlaceType")) {
            System.out.println("Writing " + foundClass.getCurrent() + "." + getMethodInfo(split[3]+split[2]));
        }
        remapper.addMethod(foundClass, getMethodInfo(split[3]+split[2]), split[1]);
    }

    @Override
    void parseField(String method) {
        throw new IllegalStateException("Not supported by Spigot yet!");
    }
}


class MojangMappingParser extends MappingParser {
    private static final Pattern MOJANG_SEPERATOR_PATTERN = Pattern.compile(" -> ");
    private static final Pattern INNER_CLASS_PATTERN = Pattern.compile("\\$");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("\\.");

    private final Map<FoundClass, List<String>> methodsForClass = new HashMap<>();
    private FoundClass currentClass;
    private List<String> lines;

    protected MojangMappingParser(AsmRemapper remapper) {
        super(remapper);
    }

    @Override
    public void parse(BufferedReader reader) throws IOException {
        super.parse(reader);
        for(Map.Entry<FoundClass, List<String>> entry : methodsForClass.entrySet()) {
            currentClass = entry.getKey();
            for(String line : entry.getValue()) {
                if(line.indexOf('(') >= 0) {
                    parseMethod(line);
                } else {
                    parseField(line);
                }
            }
        }
    }

    @Override
    void parseLine(AsmRemapper remapper, String line) {
        line = PACKAGE_PATTERN.matcher(line).replaceAll("/");
        if(line.startsWith("    ")) {
            line = line.substring(4);
            int index;
            while ((index = line.indexOf(':')) >= 0) {
                line = line.substring(index+1);
            }
            lines.add(line);
        } else {
            if(currentClass != null) {
                methodsForClass.put(currentClass, lines);
                currentClass = null;
            }
            lines = new ArrayList<>();
            parseClass(line);
        }
    }

    @Override
    AsmRemapper.InputType getInputType() {
        return AsmRemapper.InputType.NOTCHIAN;
    }

    private void parseClass(String line) {
        String[] split = MOJANG_SEPERATOR_PATTERN.split(line);
        if((!split[0].startsWith("net/minecraft") && !split[0].startsWith("com/mojang")) ||
                split[0].endsWith("package-info")) {
            return;
        }
        String deobfuscatedName = split[1].substring(0, split[1].length()-1);

        boolean addFound = false;
        if(deobfuscatedName.length() > 2 &&
                deobfuscatedName.indexOf('$') >= 0) {
            if(deobfuscatedName.charAt(deobfuscatedName.length()-1) >= 'a') {
                //Spigot mappings drop some non-anonymous inner classes, re-add them back
                addFound = true;
            } else if (currentClass != null) {
                //Add inner class if current class isn't skipped
                currentClass.addInnerClass();
                return;
            }
        }
        Optional<FoundClass> foundClass = remapper.findClassByDeobfusicated(deobfuscatedName);
        if (foundClass.isPresent()) {
            currentClass = foundClass.get();
            remapper.overwriteClass(currentClass, split[0]);
        } else if(addFound && currentClass != null) {
            //Re-add dropped non-anonymous inner classes if the current class isn't skipped
            String[] innerAndOuter = INNER_CLASS_PATTERN.split(deobfuscatedName);
            remapper.addClass(currentClass.getCurrent() + "$" + innerAndOuter[1], split[0]);
        } else {
            currentClass = null;
            System.out.println("Skipping class not in server jar: " + split[0] + " (" + deobfuscatedName + ")");
        }
    }

    @Override
    void parseMethod(String method) {
        if(currentClass == null) return;
        String[] names = MOJANG_SEPERATOR_PATTERN.split(method);
        if(names[1].equals("<init>") || names[1].equals("<clinit>")) return;
        String[] split = SPACE_PATTERN.split(names[0]);
        String[] methodData = METHOD_PATTERN.split(split[1]);
        //Reverse the order for method info
        MethodInfo methodInfo = getMethodInfo(names[1] + "(" + methodData[1] + split[0]);
        remapper.overwriteMember(currentClass, methodInfo, methodData[0]);
    }

    @Override
    void parseField(String method) {
        if(currentClass == null) return;
        String[] names = MOJANG_SEPERATOR_PATTERN.split(method);
        String[] split = SPACE_PATTERN.split(names[0]);
        remapper.addField(currentClass, names[1], getType(split[0]), split[1]);
    }
}
