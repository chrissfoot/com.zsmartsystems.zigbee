package com.zsmartsystems.zigbee.autocode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.zsmartsystems.zigbee.autocode.zcl.Attribute;
import com.zsmartsystems.zigbee.autocode.zcl.Cluster;
import com.zsmartsystems.zigbee.autocode.zcl.Command;
import com.zsmartsystems.zigbee.autocode.zcl.Context;
import com.zsmartsystems.zigbee.autocode.zcl.DataType;
import com.zsmartsystems.zigbee.autocode.zcl.Field;
import com.zsmartsystems.zigbee.autocode.zcl.Profile;
import com.zsmartsystems.zigbee.autocode.zcl.ZclDataType;
import com.zsmartsystems.zigbee.autocode.zcl.ZclDataType.DataTypeMap;

/**
 * Code generator for generating ZigBee cluster library command protocol.
 *
 * @author Tommi S.E. Laukkanen
 * @author Chris Jackson
 */
public class ZclProtocolCodeGenerator {
    // The following are offsets to the root package
    static String packageZcl = ".zcl";
    static String packageZclField = packageZcl + ".field";
    static String packageZclCluster = packageZcl + ".clusters";
    static String packageZclProtocol = packageZcl + ".protocol";
    static String packageZclProtocolCommand = packageZclCluster;

    static String packageZdp = ".zdo";
    static String packageZdpCommand = packageZdp + ".command";
    static String packageZdpTransaction = packageZdp + ".transaction";
    static String packageZdpDescriptors = packageZdp + ".descriptors";

    /**
     * The main method for running the code generator.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(final String[] args) {
        final String definitionFilePathZcl;
        final String definitionFilePathZdp;
        // if (args.length != 0) {
        // definitionFilePathZcl = args[0];
        // } else {
        definitionFilePathZcl = "./src/main/resources/zcl_definition.md";
        definitionFilePathZdp = "./src/main/resources/zdp_definition.md";
        // }

        final String sourceRootPath;
        if (args.length != 0) {
            sourceRootPath = args[0];
        } else {
            sourceRootPath = "../com.zsmartsystems.zigbee/src/main/java/";
        }

        final File sourceRootFile = new File(sourceRootPath);
        if (!sourceRootFile.exists()) {
            System.out.println("Source root path does not exist: " + sourceRootFile);
            return;
        }
        if (!sourceRootFile.isDirectory()) {
            System.out.println("Source root path is not directory: " + sourceRootFile);
            return;
        }

        final String packageRoot;
        if (args.length != 0) {
            packageRoot = args[0];
        } else {
            packageRoot = "com.zsmartsystems.zigbee";
        }

        final Context contextZcl = new Context();
        final File definitionFileZcl = new File(definitionFilePathZcl);
        if (!definitionFileZcl.exists()) {
            System.out.println("Definition file does not exist: " + definitionFilePathZcl);
        } else {
            try {
                contextZcl.lines = new ArrayList<String>(FileUtils.readLines(definitionFileZcl, "UTF-8"));
                generateZclCode(contextZcl, sourceRootFile, packageRoot);
            } catch (final IOException e) {
                System.out.println(
                        "Reading lines from Zcl definition file failed: " + definitionFileZcl.getAbsolutePath());
                e.printStackTrace();
            }
        }

        final Context contextZdp = new Context();
        final File definitionFileZdp = new File(definitionFilePathZdp);
        if (!definitionFileZdp.exists()) {
            System.out.println("Definition file does not exist: " + definitionFilePathZdp);
        } else {
            try {
                contextZdp.lines = new ArrayList<String>(FileUtils.readLines(definitionFileZdp, "UTF-8"));
                generateZdpCode(contextZdp, sourceRootFile, packageRoot);
            } catch (final IOException e) {
                System.out.println(
                        "Reading lines from Zdp definition file failed: " + definitionFileZdp.getAbsolutePath());
                e.printStackTrace();
                return;
            }
        }

        final String packagePath = getPackagePath(sourceRootFile, packageRoot);
        final File packageFile = getPackageFile(packagePath);

        try {
            final LinkedList<DataType> dataTypes = new LinkedList<DataType>(contextZcl.dataTypes.values());

            boolean addIt;
            for (DataType newType : contextZdp.dataTypes.values()) {
                addIt = true;
                for (DataType checkType : dataTypes) {
                    if (checkType.dataTypeType.equals(newType.dataTypeType)) {
                        addIt = false;
                    }
                }
                if (addIt) {
                    dataTypes.add(newType);
                }
            }

            generateZclDataTypeEnumeration(dataTypes, packageRoot, packageFile);
        } catch (final IOException e) {
            System.out.println("Failed to generate data types enumeration.");
            e.printStackTrace();
            return;
        }
    }

    public static void generateZclCode(final Context context, final File sourceRootPath, final String packageRoot) {
        ZclProtocolDefinitionParser.parseProfiles(context);

        final String packagePath = getPackagePath(sourceRootPath, packageRoot);
        final File packageFile = getPackageFile(packagePath);

        try {
            final LinkedList<DataType> dataTypes = new LinkedList<DataType>(context.dataTypes.values());
            dataTypes.addAll(context.dataTypes.values());

            generateZclDataTypeEnumeration(dataTypes, packageRoot, packageFile);
        } catch (final IOException e) {
            System.out.println("Failed to generate data types enumeration.");
            e.printStackTrace();
            return;
        }

        try {
            generateZclProfileTypeEnumeration(context, packageRoot, packageFile);
        } catch (final IOException e) {
            System.out.println("Failed to generate profile enumeration.");
            e.printStackTrace();
            return;
        }

        try {
            generateZclClusterTypeEnumeration(context, packageRoot, packageFile);
        } catch (final IOException e) {
            System.out.println("Failed to generate cluster enumeration.");
            e.printStackTrace();
            return;
        }

        try {
            // generateZclCommandTypeEnumerationXXXXX(context, packageRoot, packageFile);
            generateZclCommandTypeEnumeration(context, packageRoot, packageFile);
        } catch (final IOException e) {
            System.out.println("Failed to generate command enumeration.");
            e.printStackTrace();
            return;
        }

        // try {
        // generateZclAttributeTypeEnumeration(context, packageRoot, packageFile);
        // } catch (final IOException e) {
        // System.out.println("Failed to generate attribute enumeration.");
        // e.printStackTrace();
        // return;
        // }

        // try {
        // generateZclFieldTypeEnumeration(context, packageRoot, packageFile);
        // } catch (final IOException e) {
        // System.out.println("Failed to generate field enumeration.");
        // e.printStackTrace();
        // return;
        // }

        try {
            generateZclCommandClasses(context, packageRoot, sourceRootPath);
        } catch (final IOException e) {
            System.out.println("Failed to generate profile message classes.");
            e.printStackTrace();
            return;
        }

        try {
            generateZclClusterClasses(context, packageRoot, sourceRootPath);
        } catch (final IOException e) {
            System.out.println("Failed to generate cluster classes.");
            e.printStackTrace();
            return;
        }
    }

    public static void generateZdpCode(final Context context, final File sourceRootPath, final String packageRoot) {
        ZclProtocolDefinitionParser.parseProfiles(context);

        final File packageFile = getPackageFile(packageRoot);

        try {
            generateZdpCommandClasses(context, packageRoot, sourceRootPath);
        } catch (final IOException e) {
            System.out.println("Failed to generate profile message classes.");
            e.printStackTrace();
            return;
        }

        try {
            generateZdoCommandTypeEnumeration(context, packageRoot, sourceRootPath);
        } catch (final IOException e) {
            System.out.println("Failed to generate command enumeration.");
            e.printStackTrace();
            return;
        }

        // try {
        // generateZdpCommandTransactions(context, packageRoot, sourceRootPath);
        // } catch (final IOException e) {
        // System.out.println("Failed to generate profile message classes.");
        // e.printStackTrace();
        // return;
        // }
    }

    private static void outputClassJavaDoc(final PrintWriter out) {
        out.println("/**");
        out.println(" * Code is auto-generated. Modifications may be overwritten!");
        out.println(" */");
    }

    private static File getPackageFile(String packagePath) {
        final File packageFile = new File(packagePath);
        if (!packageFile.exists()) {
            packageFile.mkdirs();
        }
        return packageFile;
    }

    private static String getPackagePath(File sourceRootPath, String packageRoot) {
        return sourceRootPath.getAbsolutePath() + File.separator + packageRoot.replace(".", File.separator);
    }

    private static void generateZclDataTypeEnumeration(LinkedList<DataType> dataTypes, final String packageRootPrefix,
            File sourceRootPath) throws IOException {
        final String className = "ZclDataType";

        final String packageRoot = packageRootPrefix + packageZclProtocol;
        final String packagePath = getPackagePath(sourceRootPath, packageZclProtocol);
        final File packageFile = getPackageFile(packagePath);

        final PrintWriter out = getClassOut(packageFile, className);

        out.println("package " + packageRoot + ";");

        out.println();
        out.println("import java.util.Calendar;");
        out.println("import java.util.HashMap;");
        out.println("import java.util.Map;");
        out.println("import " + packageRootPrefix + packageZclField + ".*;");
        out.println("import " + packageRootPrefix + packageZdp + ".ZdoStatus;");
        out.println("import " + packageRootPrefix + packageZdpDescriptors + ".*;");
        out.println("import " + packageRootPrefix + "." + "IeeeAddress" + ";");
        out.println();
        outputClassJavaDoc(out);
        out.println("public enum " + className + " {");

        // final LinkedList<DataType> dataTypes = new LinkedList<DataType>(context.dataTypes.values());
        for (final DataType dataType : dataTypes) {
            DataTypeMap zclDataType = ZclDataType.getDataTypeMapping().get(dataType.dataTypeType);
            final String dataTypeClass;
            if (dataType.dataTypeClass.contains("<")) {
                dataTypeClass = dataType.dataTypeClass.substring(dataType.dataTypeClass.indexOf("<") + 1,
                        dataType.dataTypeClass.indexOf(">"));
            } else {
                dataTypeClass = dataType.dataTypeClass;
            }
            out.print("    " + dataType.dataTypeType + "(\"" + dataType.dataTypeName + "\", " + dataTypeClass + ".class"
                    + ", " + String.format("0x%02X", zclDataType.id) + ", " + zclDataType.analogue + ")");
            out.println(dataTypes.getLast().equals(dataType) ? ';' : ',');
        }

        out.println();
        out.println("    private final String label;");
        out.println("    private final Class<?> dataClass;");
        out.println("    private final int id;");
        out.println("    private final boolean analogue;");
        out.println("    private static Map<Integer, " + className + "> codeTypeMapping;");
        out.println();
        out.println("    " + className
                + "(final String label, final Class<?> dataClass, final int id, final boolean analogue) {");
        out.println("        this.label = label;");
        out.println("        this.dataClass = dataClass;");
        out.println("        this.id = id;");
        out.println("        this.analogue = analogue;");
        out.println("    }");
        out.println();

        out.println("    private static void initMapping() {");
        out.println("        codeTypeMapping = new HashMap<Integer, " + className + ">();");
        out.println("        for (" + className + " s : values()) {");
        out.println("            codeTypeMapping.put(s.id, s);");
        out.println("        }");
        out.println("    }");

        out.println("    public static " + className + " getType(int id) {");
        out.println("        if (codeTypeMapping == null) {");
        out.println("            initMapping();");
        out.println("        }");

        out.println("        return codeTypeMapping.get(id);");
        out.println("    }");

        out.println();
        out.println("    public String getLabel() {");
        out.println("        return label;");
        out.println("    }");
        out.println();
        out.println("    public Class<?> getDataClass() {");
        out.println("        return dataClass;");
        out.println("    }");
        out.println();
        out.println("    public int getId() {");
        out.println("        return id;");
        out.println("    }");
        out.println();
        out.println("    public boolean isAnalog() {");
        out.println("        return analogue;");
        out.println("    }");
        out.println("}");

        out.flush();
        out.close();
    }

    private static void generateZclProfileTypeEnumeration(Context context, String packageRootPrefix,
            File sourceRootPath) throws IOException {
        final String className = "ZclProfileType";

        final String packageRoot = packageRootPrefix + packageZclProtocol;
        final String packagePath = getPackagePath(sourceRootPath, packageZclProtocol);
        final File packageFile = getPackageFile(packagePath);

        final PrintWriter out = getClassOut(packageFile, className);

        out.println("package " + packageRoot + ";");
        out.println();
        outputClassJavaDoc(out);
        out.println("public enum " + className + " {");

        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            out.print("    " + profile.profileType + "(" + profile.profileId + ", \"" + profile.profileName + "\")");
            out.println(profiles.getLast().equals(profile) ? ';' : ',');
        }

        out.println();
        out.println("    private final int id;");
        out.println("    private final String label;");
        out.println();
        out.println("    " + className + "(final int id, final String label) {");
        out.println("        this.id = id;");
        out.println("        this.label = label;");
        out.println("    }");
        out.println();
        out.println("    public int getId() { return id; }");
        out.println("    public String getLabel() { return label; }");
        out.println("    public String toString() { return label; }");
        out.println();
        out.println("}");

        out.flush();
        out.close();
    }

    private static void generateZclClusterTypeEnumeration(Context context, String packageRootPrefix,
            File sourceRootPath) throws IOException {
        final String className = "ZclClusterType";

        final String packageRoot = packageRootPrefix + packageZclProtocol;
        final String packagePath = getPackagePath(sourceRootPath, packageZclProtocol);
        final File packageFile = getPackageFile(packagePath);

        final PrintWriter out = getClassOut(packageFile, className);

        out.println("package " + packageRoot + ";");
        out.println();
        out.println("import " + packageRootPrefix + packageZcl + ".ZclCluster;");
        out.println("import " + packageRootPrefix + packageZclCluster + ".*;");
        out.println();
        out.println("import java.util.HashMap;");
        out.println("import java.util.Map;");

        out.println();
        outputClassJavaDoc(out);
        out.println("public enum " + className + " {");

        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());
            for (final Cluster cluster : clusters) {
                out.print("    " + cluster.clusterType + "(" + String.format("0x%04X", cluster.clusterId)
                        + ", ZclProfileType." + profile.profileType + ", Zcl" + cluster.nameUpperCamelCase
                        + "Cluster.class, \"" + cluster.clusterName + "\")");
                out.println(clusters.getLast().equals(cluster) ? ';' : ',');
            }
        }

        out.println();
        out.println(
                "    private static final Map<Integer, ZclClusterType> idValueMap = new HashMap<Integer, ZclClusterType>();");
        out.println();
        out.println("    private final int id;");
        out.println("    private final ZclProfileType profileType;");
        out.println("    private final String label;");
        out.println("    private final Class<? extends ZclCluster> clusterClass;");
        out.println();
        out.println("    " + className
                + "(final int id, final ZclProfileType profileType, final Class<? extends ZclCluster>clusterClass, final String label) {");
        out.println("        this.id = id;");
        out.println("        this.profileType = profileType;");
        out.println("        this.clusterClass = clusterClass;");
        out.println("        this.label = label;");
        out.println("    }");
        out.println();
        out.println("    static {");
        out.println("        for (final ZclClusterType value : values()) {");
        out.println("            idValueMap.put(value.id, value);");
        out.println("        }");
        out.println("    }");
        out.println();
        out.println("    public int getId() {");
        out.println("        return id;");
        out.println("    }");
        out.println();
        out.println("    public ZclProfileType getProfileType() {");
        out.println("        return profileType;");
        out.println("    }");
        out.println();
        out.println("    public String getLabel() {");
        out.println("        return label;");
        out.println("    }");
        out.println();
        // out.println(" public String toString() {");
        // out.println(" return label;");
        // out.println(" }");
        // out.println();
        out.println("    public Class<? extends ZclCluster> getClusterClass() {");
        out.println("        return clusterClass;");
        out.println("    }");
        out.println();
        out.println("    public static ZclClusterType getValueById(final int id) {");
        out.println("        return idValueMap.get(id);");
        out.println("    }");
        out.println();
        out.println("}");

        out.flush();
        out.close();
    }

    private static void generateZclCommandTypeEnumerationXXXXX(Context context, String packageRootPrefix,
            File sourceRootPath) throws IOException {

        final String className = "ZclCommandTypeXXX";

        final String packageRoot = packageRootPrefix + packageZclProtocol;
        final String packagePath = getPackagePath(sourceRootPath, packageZclProtocol);
        final File packageFile = getPackageFile(packagePath);

        final PrintWriter out = getClassOut(packageFile, className);

        out.println("package " + packageRoot + ";");
        out.println();
        outputClassJavaDoc(out);
        out.println("public enum " + className + " {");

        final LinkedList<String> valueRows = new LinkedList<String>();
        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());
            for (final Cluster cluster : clusters) {
                {
                    final LinkedList<Command> commands = new LinkedList<Command>(cluster.received.values());
                    for (final Command command : commands) {
                        final boolean generic = cluster.clusterId == 65535;
                        valueRows.add("    " + command.commandType + "(" + command.commandId + ", ZclClusterType."
                                + cluster.clusterType + ", \"" + command.commandLabel + "\", true, " + generic + ")");
                    }
                }
                {
                    final LinkedList<Command> commands = new LinkedList<Command>(cluster.generated.values());
                    for (final Command command : commands) {
                        final boolean generic = cluster.clusterId == 65535;
                        valueRows.add("    " + command.commandType + "(" + command.commandId + ", ZclClusterType."
                                + cluster.clusterType + ", \"" + command.commandLabel + "\", false, " + generic + ")");
                    }
                }
            }
        }

        for (final String valueRow : valueRows) {
            out.print(valueRow);
            out.println(valueRows.getLast().equals(valueRow) ? ';' : ',');
        }

        out.println();
        out.println("    private final int id;");
        out.println("    private final ZclClusterType clusterType;");
        out.println("    private final String label;");
        out.println("    private final boolean received;");
        out.println("    private final boolean generic;");
        out.println();
        out.println("    " + className
                + "(final int id, final ZclClusterType clusterType, final String label, final boolean received, final boolean generic) {");
        out.println("        this.id = id;");
        out.println("        this.clusterType = clusterType;");
        out.println("        this.label = label;");
        out.println("        this.received = received;");
        out.println("        this.generic = generic;");
        out.println("    }");
        out.println();
        out.println("    public int getId() { return id; }");
        out.println("    public ZclClusterType getClusterType() { return clusterType; }");
        out.println("    public String getLabel() { return label; }");
        out.println("    public boolean isReceived() { return received; }");
        out.println("    public boolean isGeneric() { return generic; }");
        out.println("    public String toString() { return label; }");
        out.println("}");

        out.flush();
        out.close();
    }

    private static void generateZclAttributeTypeEnumeration(Context context, String packageRootPrefix,
            File sourceRootPath) throws IOException {

        final String className = "ZclAttributeType";

        final String packageRoot = packageRootPrefix + packageZclProtocol;
        final String packagePath = getPackagePath(sourceRootPath, packageZclProtocol);
        final File packageFile = getPackageFile(packagePath);

        final PrintWriter out = getClassOut(packageFile, className);

        out.println("package " + packageRoot + ";");
        out.println();
        outputClassJavaDoc(out);
        out.println("public enum " + className + " {");

        boolean first = true;
        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());

            for (final Cluster cluster : clusters) {
                for (final Attribute attribute : cluster.attributes.values()) {
                    if (first == false) {
                        out.println(",");
                    }
                    first = false;
                    out.print("    " + attribute.enumName + "(0x" + String.format("%04X", cluster.clusterId) + ", 0x"
                            + String.format("%04X", attribute.attributeId) + ", ZclDataType." + attribute.dataType
                            + ")");
                }
            }
        }
        out.println(";");

        out.println();
        out.println("    private final int clusterId;");
        out.println("    private final int attributeId;");
        out.println("    private final ZclAttributeType attributeType;");
        out.println("    private final String label;");
        out.println("    private final boolean received;");
        out.println("    private final boolean generic;");
        out.println();
        out.println("    " + className + "(final int clusterId, final int attributeId, final ZclDataType dataType) {");
        out.println("        this.id = id;");
        out.println("        this.attributeType = attributeType;");
        out.println("        this.label = label;");
        out.println("        this.received = received;");
        out.println("        this.generic = generic;");
        out.println("    }");
        out.println();
        out.println("    public int getId() { return id; }");
        out.println("    public ZclAttributeType getAttributeType() { return attributeType; }");
        out.println("    public String getLabel() { return label; }");
        out.println("    public boolean isReceived() { return received; }");
        out.println("    public boolean isGeneric() { return generic; }");
        out.println("    public String toString() { return label; }");
        out.println("}");

        out.flush();
        out.close();
    }

    private static void generateZclFieldTypeEnumeration(Context context, String packageRootPrefix, File sourceRootPath)
            throws IOException {
        final String className = "ZclFieldType";

        final String packageRoot = packageRootPrefix + packageZclProtocol;
        final String packagePath = getPackagePath(sourceRootPath, packageZclProtocol);
        final File packageFile = getPackageFile(packagePath);

        final PrintWriter out = getClassOut(packageFile, className);

        out.println("package " + packageRoot + ";");
        out.println();
        outputClassJavaDoc(out);
        out.println("public enum " + className + " {");

        final LinkedList<String> valueRows = new LinkedList<String>();
        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());
            for (final Cluster cluster : clusters) {
                final ArrayList<Command> commands = new ArrayList<Command>();
                commands.addAll(cluster.received.values());
                commands.addAll(cluster.generated.values());
                for (final Command command : commands) {
                    final LinkedList<Field> fields = new LinkedList<Field>(command.fields.values());
                    for (final Field field : fields) {
                        valueRows.add("    " + field.fieldType + "(" + field.fieldId + ", ZclCommandType."
                                + command.commandType + ", \"" + field.fieldLabel + "\", ZclDataType." + field.dataType
                                + ")");
                    }
                }
            }
        }

        for (final String valueRow : valueRows) {
            out.print(valueRow);
            out.println(valueRows.getLast().equals(valueRow) ? ';' : ',');
        }

        out.println();
        out.println("    private final int id;");
        out.println("    private final ZclCommandType commandType;");
        out.println("    private final String label;");
        out.println("    private final ZclDataType dataType;");
        out.println();
        out.println("    " + className
                + "(final int id, final ZclCommandType commandType, final String label, final ZclDataType dataType) {");
        out.println("        this.id = id;");
        out.println("        this.commandType = commandType;");
        out.println("        this.label = label;");
        out.println("        this.dataType = dataType;");
        out.println("    }");
        out.println();
        out.println("    public int getId() { return id; }");
        out.println("    public ZclCommandType getCommandType() { return commandType; }");
        out.println("    public String getLabel() { return label; }");
        out.println("    public ZclDataType getDataType() { return dataType; }");
        out.println();
        out.println("}");

        out.flush();
        out.close();
    }

    private static void generateZclCommandClasses(Context context, String packageRootPrefix, File sourceRootPath)
            throws IOException {

        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());
            for (final Cluster cluster : clusters) {
                final ArrayList<Command> commands = new ArrayList<Command>();
                commands.addAll(cluster.received.values());
                commands.addAll(cluster.generated.values());
                for (final Command command : commands) {
                    final String packageRoot = packageRootPrefix + packageZclProtocolCommand + "."
                            + cluster.clusterType.replace("_", "").toLowerCase();
                    final String packagePath = getPackagePath(sourceRootPath, packageRoot);
                    final File packageFile = getPackageFile(packagePath);

                    final String className = command.nameUpperCamelCase;
                    final PrintWriter out = getClassOut(packageFile, className);

                    final LinkedList<Field> fields = new LinkedList<Field>(command.fields.values());
                    boolean fieldWithDataTypeList = false;
                    for (final Field field : fields) {
                        if (field.dataTypeClass.startsWith("List")) {
                            fieldWithDataTypeList = true;
                            break;
                        }
                    }

                    out.println("package " + packageRoot + ";");
                    out.println();
                    // out.println("import " + packageRootPrefix + packageZcl + ".ZclCommandMessage;");
                    out.println("import " + packageRootPrefix + packageZcl + ".ZclCommand;");
                    // out.println("import " + packageRootPrefix + packageZcl + ".ZclField;");
                    if (fields.size() > 0) {
                        out.println("import " + packageRootPrefix + packageZcl + ".ZclFieldSerializer;");
                        out.println("import " + packageRootPrefix + packageZcl + ".ZclFieldDeserializer;");
                        out.println("import " + packageRootPrefix + packageZclProtocol + ".ZclDataType;");
                    }
                    // out.println("import " + packageRootPrefix + packageZclProtocol + ".ZclClusterType;");
                    // out.println("import " + packageRootPrefix + packageZclProtocol + ".ZclCommandType;");
                    // if (!fields.isEmpty()) {
                    // out.println("import " + packageRootPrefix + packageZclProtocol + ".ZclFieldType;");
                    // if (fieldWithDataTypeList) {
                    // out.println("import " + packageRootPrefix + packageZclField + ".*;");
                    // }
                    // }

                    if (fieldWithDataTypeList) {
                        out.println();
                        out.println("import java.util.List;");
                    }

                    // out.println("import java.util.Map;");
                    // out.println("import java.util.HashMap;");

                    for (final Field field : fields) {
                        String packageName;
                        if (field.dataTypeClass.contains("Descriptor")) {
                            packageName = packageZdpDescriptors;
                        } else {
                            packageName = packageZclField;
                        }

                        String typeName;
                        if (field.dataTypeClass.startsWith("List")) {
                            typeName = field.dataTypeClass;
                            typeName = typeName.substring(typeName.indexOf("<") + 1);
                            typeName = typeName.substring(0, typeName.indexOf(">"));
                        } else {
                            typeName = field.dataTypeClass;
                        }

                        switch (typeName) {
                            case "Integer":
                            case "Boolean":
                            case "Object":
                            case "Long":
                            case "String":
                                continue;
                            case "IeeeAddress":
                                out.println("import " + packageRootPrefix + "." + typeName + ";");
                                continue;
                        }

                        out.println("import " + packageRootPrefix + packageName + "." + typeName + ";");
                    }

                    out.println();
                    out.println("/**");
                    out.println(" * " + command.commandLabel + " value object class.");

                    if (command.commandDescription != null && command.commandDescription.size() != 0) {
                        out.println(" * <p>");
                        for (String line : command.commandDescription) {
                            out.println(" * " + line);
                        }
                    }

                    out.println(" * <p>");
                    out.println(" * Cluster: <b>" + cluster.clusterName + "</b>. Command is sent <b>"
                            + (cluster.received.containsValue(command) ? "TO" : "FROM") + "</b> the server.");
                    out.println(" * This command is " + ((cluster.clusterType.equals("GENERAL"))
                            ? "a <b>generic</b> command used across the profile."
                            : "a <b>specific</b> command used for the " + cluster.clusterName + " cluster."));

                    if (cluster.clusterDescription.size() > 0) {
                        out.println(" * <p>");
                        for (String line : cluster.clusterDescription) {
                            out.println(" * " + line);
                        }
                    }

                    out.println(" * <p>");
                    out.println(" * Code is auto-generated. Modifications may be overwritten!");

                    out.println(" */");
                    out.println("public class " + className + " extends ZclCommand {");

                    for (final Field field : fields) {
                        out.println("    /**");
                        out.println("     * " + field.fieldLabel + " command message field.");
                        out.println("     */");
                        out.println("    private " + field.dataTypeClass + " " + field.nameLowerCamelCase + ";");
                        out.println();
                    }

                    // if (fields.size() > 0) {
                    // out.println(" static {");
                    // for (final Field field : fields) {
                    // out.println(" fields.put(" + field.fieldId + ", new ZclField(" + field.fieldId
                    // + ", \"" + field.fieldLabel + "\", ZclDataType." + field.dataType + "));");
                    // }
                    // out.println(" }");
                    // out.println();
                    // }

                    out.println("    /**");
                    out.println("     * Default constructor.");
                    out.println("     */");
                    out.println("    public " + className + "() {");
                    // out.println(" setType(ZclCommandType." + command.commandType + ");");
                    out.println("        genericCommand = "
                            + ((cluster.clusterType.equals("GENERAL")) ? "true" : "false") + ";");
                    if (!cluster.clusterType.equals("GENERAL")) {
                        out.println("        clusterId = " + cluster.clusterId + ";");
                    }
                    out.println("        commandId = " + command.commandId + ";");

                    out.println("        commandDirection = "
                            + (cluster.received.containsValue(command) ? "true" : "false") + ";");

                    out.println("    }");
                    // out.println();
                    // out.println(" /**");
                    // out.println(" * Constructor copying field values from command message.");
                    // out.println(" *");
                    // out.println(" * @param fields a {@link Map} containing the value {@link Object}s");
                    // out.println(" */");
                    // out.println(" public " + className + "(final Map<Integer, Object> fields) {");
                    // out.println(" this();");
                    // for (final Field field : fields) {
                    // out.println(" " + field.nameLowerCamelCase + " = (" + field.dataTypeClass
                    // + ") fields.get(" + field.fieldId + ");");
                    // }
                    // out.println(" }");
                    // out.println();
                    // out.println(" @Override");
                    // out.println(" public ZclCommandMessage toCommandMessage() {");
                    // out.println(" final ZclCommandMessage message = super.toCommandMessage();");
                    // for (final Field field : fields) {
                    // out.println(" message.getFields().put(ZclFieldType." + field.fieldType + ","
                    // + field.nameLowerCamelCase + ");");
                    // }
                    // out.println(" return message;");
                    // out.println(" }");

                    if (cluster.clusterType.equals("GENERAL")) {
                        out.println();
                        out.println("    /**");
                        out.println("     * Sets the cluster ID for <i>generic</i> commands. {@link " + className
                                + "} is a <i>generic</i> command.");
                        out.println("     * <p>");
                        out.println(
                                "     * For commands that are not <i>generic</i>, this method will do nothing as the cluster ID is fixed.");
                        out.println(
                                "     * To test if a command is <i>generic</i>, use the {@link #isGenericCommand} method.");
                        out.println("     *");
                        out.println(
                                "     * @param clusterId the cluster ID used for <i>generic</i> commands as an {@link Integer}");

                        out.println("     */");
                        out.println("    @Override");
                        out.println("    public void setClusterId(Integer clusterId) {");
                        out.println("        this.clusterId = clusterId;");
                        out.println("    }");
                    }

                    for (final Field field : fields) {
                        out.println();
                        out.println("    /**");
                        out.println("     * Gets " + field.fieldLabel + ".");
                        out.println("     *");
                        out.println("     * @return the " + field.fieldLabel);
                        out.println("     */");
                        out.println("    public " + field.dataTypeClass + " get" + field.nameUpperCamelCase + "() {");
                        out.println("        return " + field.nameLowerCamelCase + ";");
                        out.println("    }");
                        out.println();
                        out.println("    /**");
                        out.println("     * Sets " + field.fieldLabel + ".");
                        out.println("     *");
                        out.println("     * @param " + field.nameLowerCamelCase + " the " + field.fieldLabel);
                        out.println("     */");
                        out.println("    public void set" + field.nameUpperCamelCase + "(final " + field.dataTypeClass
                                + " " + field.nameLowerCamelCase + ") {");
                        out.println(
                                "        this." + field.nameLowerCamelCase + " = " + field.nameLowerCamelCase + ";");
                        out.println("    }");

                    }

                    if (fields.size() > 0) {
                        // out.println();
                        // out.println(" @Override");
                        // out.println(" public void setFieldValues(final Map<Integer, Object> values) {");
                        // for (final Field field : fields) {
                        // out.println(" " + field.nameLowerCamelCase + " = (" + field.dataTypeClass
                        // + ") values.get(" + field.fieldId + ");");
                        // }
                        // out.println(" }");

                        out.println();
                        out.println("    @Override");
                        out.println("    public void serialize(final ZclFieldSerializer serializer) {");
                        for (final Field field : fields) {
                            if (field.listSizer != null) {
                                out.println("        for (int cnt = 0; cnt < " + field.nameLowerCamelCase
                                        + ".size(); cnt++) {");
                                out.println("            serializer.serialize(" + field.nameLowerCamelCase
                                        + ".get(cnt), ZclDataType." + field.dataType + ");");
                                out.println("        }");
                            } else {
                                out.println("        serializer.serialize(" + field.nameLowerCamelCase
                                        + ", ZclDataType." + field.dataType + ");");
                            }
                        }
                        out.println("    }");

                        out.println();
                        out.println("    @Override");
                        out.println("    public void deserialize(final ZclFieldDeserializer deserializer) {");
                        for (final Field field : fields) {
                            out.println("        " + field.nameLowerCamelCase + " = (" + field.dataTypeClass
                                    + ") deserializer.deserialize(" + "ZclDataType." + field.dataType + ");");
                        }
                        out.println("    }");
                    }

                    out.println();
                    out.println("    @Override");
                    out.println("    public String toString() {");
                    out.println("        final StringBuilder builder = new StringBuilder();");
                    out.println("        builder.append(super.toString());");
                    for (final Field field : fields) {
                        out.println("        builder.append(\", " + field.nameLowerCamelCase + "=\");");
                        out.println("        builder.append(" + field.nameLowerCamelCase + ");");
                    }
                    out.println("        return builder.toString();");
                    out.println("    }");

                    out.println();
                    out.println("}");

                    out.flush();
                    out.close();
                }
            }
        }
    }

    private static String getZclCommandTypeEnum(final Cluster cluster, final Command command, boolean received) {
        return command.commandType + "(" + String.format("0x%04X", cluster.clusterId) + ", " + command.commandId + ", "
                + command.nameUpperCamelCase + ".class" + ", " + received + ")";
        // return command.commandType + "(ZclClusterType." + cluster.clusterType + ", " + command.commandId + ", "
        // + command.nameUpperCamelCase + ".class" + ", " + received + ")";
    }

    private static void generateZclCommandTypeEnumeration(Context context, String packageRootPrefix,
            File sourceRootPath) throws IOException {
        final String className = "ZclCommandType";

        final String packageRoot = packageRootPrefix + packageZclProtocol;
        final String packagePath = getPackagePath(sourceRootPath, packageZclProtocol);
        final File packageFile = getPackageFile(packagePath);

        final PrintWriter out = getClassOut(packageFile, className);

        out.println("package " + packageRoot + ";");
        out.println();

        out.println("import java.lang.reflect.Constructor;");
        out.println();
        out.println("import " + packageRootPrefix + packageZcl + ".ZclCommand;");
        out.println();

        Map<String, Command> commandEnum = new TreeMap<String, Command>();

        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());
            for (final Cluster cluster : clusters) {
                // Brute force to get the commands in order!
                for (int c = 0; c < 65535; c++) {
                    if (cluster.received.get(c) != null) {
                        out.println("import " + getZclClusterCommandPackage(packageRootPrefix, cluster) + "."
                                + cluster.received.get(c).nameUpperCamelCase + ";");

                        commandEnum.put(getZclCommandTypeEnum(cluster, cluster.received.get(c), true),
                                cluster.received.get(c));
                    }
                    if (cluster.generated.get(c) != null) {
                        out.println("import " + getZclClusterCommandPackage(packageRootPrefix, cluster) + "."
                                + cluster.generated.get(c).nameUpperCamelCase + ";");

                        commandEnum.put(getZclCommandTypeEnum(cluster, cluster.generated.get(c), false),
                                cluster.generated.get(c));
                    }
                }
            }
        }
        out.println();

        out.println();
        outputClassJavaDoc(out);
        out.println("public enum " + className + " {");
        boolean first = true;
        for (String command : commandEnum.keySet()) {
            Command cmd = commandEnum.get(command);
            if (cmd == null) {
                System.out.println("Command without data: " + command);
                continue;
            }

            if (!first) {
                out.println(",");
            }
            first = false;
            out.println("    /**");
            out.println("     * " + cmd.commandType + ": " + cmd.commandLabel);
            out.println("     * <p>");
            out.println("     * See {@link " + cmd.nameUpperCamelCase + "}");
            out.println("     */");
            out.print("    " + command);
        }
        out.println(";");
        out.println();

        out.println("    private final int commandId;");
        out.println("    private final int clusterType;");
        out.println("    private final Class<? extends ZclCommand> commandClass;");
        // out.println(" private final String label;");
        out.println("    private final boolean received;");
        out.println();
        out.println("    " + className
                + "(final int clusterType, final int commandId, final Class<? extends ZclCommand> commandClass, final boolean received) {");
        out.println("        this.clusterType = clusterType;");
        out.println("        this.commandId = commandId;");
        out.println("        this.commandClass = commandClass;");
        // out.println(" this.label = label;");
        out.println("        this.received = received;");
        out.println("    }");
        out.println();

        out.println("    public int getClusterType() {");
        out.println("        return clusterType;");
        out.println("    }");
        out.println();
        out.println("    public int getId() {");
        out.println("        return commandId;");
        out.println("    }");
        out.println();
        // out.println(" public String getLabel() { return label; }");
        out.println("    public boolean isGeneric() {");
        out.println("        return clusterType==0xFFFF;");
        out.println("    }");
        out.println();
        out.println("    public boolean isReceived() {");
        out.println("        return received;");
        out.println("    }");
        out.println();
        out.println("    public Class<? extends ZclCommand> getCommandClass() {");
        out.println("        return commandClass;");
        out.println("    }");
        out.println();
        out.println("    public static ZclCommandType getRequest(final int clusterType, final int commandId) {");
        out.println("        for (final ZclCommandType value : values()) {");
        out.println(
                "            if (!value.received && value.clusterType == clusterType && value.commandId == commandId) {");
        out.println("                return value;");
        out.println("            }");
        out.println("        }");
        out.println("        return null;");
        out.println("    }");

        out.println();
        out.println("    public static ZclCommandType getResponse(final int clusterType, final int commandId) {");
        out.println("        for (final ZclCommandType value : values()) {");
        out.println(
                "            if (value.received && value.clusterType == clusterType && value.commandId == commandId) {");
        out.println("                return value;");
        out.println("            }");
        out.println("        }");
        out.println("        return null;");
        out.println("    }");

        out.println();
        out.println("    public static ZclCommandType getGeneric(final int commandId) {");
        out.println("        for (final ZclCommandType value : values()) {");
        out.println("            if (value.clusterType == 0xFFFF && value.commandId == commandId) {");
        out.println("                return value;");
        out.println("            }");
        out.println("        }");
        out.println("        return null;");
        out.println("    }");

        out.println();
        out.println("    public ZclCommand instantiateCommand() {");
        out.println("        Constructor<? extends ZclCommand> cmdConstructor;");
        out.println("        try {");
        out.println("            cmdConstructor = commandClass.getConstructor();");
        out.println("            return cmdConstructor.newInstance();");
        out.println("        } catch (Exception e) {");
        out.println("            // logger.debug(\"Error instantiating cluster command {}\", this);");
        out.println("        }");
        out.println("        return null;");
        out.println("    }");

        out.println("}");

        out.flush();
        out.close();
    }

    private static void generateZclClusterClasses(Context context, String packageRootPrefix, File sourceRootPath)
            throws IOException {

        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());
            for (final Cluster cluster : clusters) {
                final String packageRoot = packageRootPrefix;
                final String packagePath = getPackagePath(sourceRootPath, packageRoot);
                final File packageFile = getPackageFile(packagePath + (packageZclCluster).replace('.', '/'));

                final String className = "Zcl" + cluster.nameUpperCamelCase + "Cluster";
                final PrintWriter out = getClassOut(packageFile, className);

                final ArrayList<Command> commands = new ArrayList<Command>();
                commands.addAll(cluster.received.values());
                commands.addAll(cluster.generated.values());

                out.println("package " + packageRoot + packageZclCluster + ";");
                out.println();

                Set<String> imports = new HashSet<String>();

                boolean useList = false;
                for (final Command command : commands) {
                    final LinkedList<Field> fields = new LinkedList<Field>(command.fields.values());
                    System.out.println("Checking command " + command.commandLabel);

                    for (final Field field : fields) {
                        System.out.println("Checking " + field.dataTypeClass);
                        String packageName;
                        if (field.dataTypeClass.contains("Descriptor")) {
                            packageName = packageZdpDescriptors;
                        } else {
                            packageName = packageZclField;
                        }

                        String typeName;
                        if (field.dataTypeClass.startsWith("List")) {
                            useList = true;
                            typeName = field.dataTypeClass;
                            typeName = typeName.substring(typeName.indexOf("<") + 1);
                            typeName = typeName.substring(0, typeName.indexOf(">"));
                        } else {
                            typeName = field.dataTypeClass;
                        }

                        switch (typeName) {
                            case "Integer":
                            case "Boolean":
                            case "Object":
                            case "Long":
                            case "String":
                                continue;
                            case "IeeeAddress":
                                imports.add(packageRootPrefix + "." + typeName);
                                System.out.println("Adding " + typeName);
                                continue;
                        }

                        imports.add(packageRootPrefix + packageName + "." + typeName);
                    }
                }

                if (useList) {
                    imports.add("java.util.List");
                    imports.add(packageRootPrefix + packageZclField + ".*");
                }

                boolean addAttributeTypes = false;
                boolean readAttributes = false;
                boolean writeAttributes = false;
                for (final Attribute attribute : cluster.attributes.values()) {
                    if (attribute.attributeAccess.toLowerCase().contains("write")) {
                        addAttributeTypes = true;
                        writeAttributes = true;
                    }
                    if (attribute.attributeAccess.toLowerCase().contains("read")) {
                        readAttributes = true;
                    }

                    if ("Calendar".equals(attribute.dataTypeClass)) {
                        imports.add("java.util.Calendar");
                    }
                    if ("IeeeAddress".equals(attribute.dataTypeClass)) {
                        imports.add("com.zsmartsystems.zigbee.IeeeAddress");
                    }
                }

                if (addAttributeTypes) {
                    imports.add("com.zsmartsystems.zigbee.zcl.protocol.ZclDataType");
                }

                imports.add(packageRoot + packageZcl + ".ZclCluster");
                if (cluster.attributes.size() != 0) {
                    imports.add(packageRoot + packageZclProtocol + ".ZclDataType");
                }

                if (!commands.isEmpty()) {
                    imports.add(packageRoot + packageZcl + ".ZclCommand");
                }
                // imports.add(packageRoot + packageZcl + ".ZclCommandMessage");

                // imports.add(packageRoot + ".ZigBeeDestination");
                imports.add(packageRoot + ".ZigBeeDeviceAddress");
                imports.add(packageRoot + ".ZigBeeNetworkManager");
                if (!cluster.attributes.isEmpty() | !commands.isEmpty()) {
                    imports.add(packageRoot + ".CommandResult");
                }
                // imports.add(packageRoot + ".ZigBeeDevice");
                imports.add(packageRoot + packageZcl + ".ZclAttribute");
                imports.add("java.util.Map");
                imports.add("java.util.HashMap");

                if (!cluster.attributes.isEmpty() | !commands.isEmpty()) {
                    imports.add("java.util.concurrent.Future");
                }
                // imports.add("com.zsmartsystems.zigbee.model.ZigBeeType");

                for (final Attribute attribute : cluster.attributes.values()) {
                    if (attribute.attributeAccess.toLowerCase().contains("read")) {
                        imports.add("java.util.Calendar");
                    }
                }

                for (final Command command : commands) {
                    imports.add(getZclClusterCommandPackage(packageRoot, cluster) + "." + command.nameUpperCamelCase);
                }

                if (className.equals("ZclScenesCluster")) {
                    System.out.println();
                }
                List<String> importList = new ArrayList<String>();
                importList.addAll(imports);
                Collections.sort(importList);
                for (final String importClass : importList) {
                    out.println("import " + importClass + ";");
                }

                out.println();
                out.println("/**");
                out.println(" * <b>" + cluster.clusterName + "</b> cluster implementation (<i>Cluster ID "
                        + String.format("0x%04X", cluster.clusterId) + "</i>).");
                if (cluster.clusterDescription.size() != 0) {
                    out.println(" * <p>");
                    for (String line : cluster.clusterDescription) {
                        out.println(" * " + line);
                    }
                }
                out.println(" * <p>");
                out.println(" * Code is auto-generated. Modifications may be overwritten!");

                out.println(" */");
                // outputClassJavaDoc(out);
                out.println("public class " + className + " extends ZclCluster {");

                out.println("    // Cluster ID");
                out.println("    public static final int CLUSTER_ID = " + String.format("0x%04X;", cluster.clusterId));
                out.println();
                out.println("    // Cluster Name");
                out.println("    public static final String CLUSTER_NAME = \"" + cluster.clusterName + "\";");
                out.println();

                if (cluster.attributes.size() != 0) {
                    out.println("    // Attribute constants");
                    for (final Attribute attribute : cluster.attributes.values()) {
                        out.println("    public static final int " + attribute.enumName + " = "
                                + String.format("0x%04X", attribute.attributeId) + ";");
                    }
                    out.println();
                }

                out.println("    // Attribute initialisation");
                out.println("    protected Map<Integer, ZclAttribute> initializeAttributes() {");
                out.println("        Map<Integer, ZclAttribute> attributeMap = new HashMap<Integer, ZclAttribute>("
                        + cluster.attributes.size() + ");");
                out.println();
                if (cluster.attributes.size() != 0) {
                    for (final Attribute attribute : cluster.attributes.values()) {
                        out.println("        attributeMap.put(" + attribute.enumName + ", new ZclAttribute("
                                + attribute.attributeId + ", \"" + attribute.attributeLabel + "\", " + "ZclDataType."
                                + attribute.dataType + ", "
                                + "mandatory".equals(attribute.attributeImplementation.toLowerCase()) + ", "
                                + attribute.attributeAccess.toLowerCase().contains("read") + ", "
                                + attribute.attributeAccess.toLowerCase().contains("write") + ", "
                                + "mandatory".equals(attribute.attributeReporting.toLowerCase()) + "));");
                    }
                }
                out.println();
                out.println("        return attributeMap;");
                out.println("    }");
                out.println();

                out.println("    /**");
                out.println("     * Default constructor.");
                out.println("     */");
                out.println("    public " + className
                        + "(final ZigBeeNetworkManager zigbeeManager, final ZigBeeDeviceAddress zigbeeAddress) {");
                out.println("        super(zigbeeManager, zigbeeAddress, CLUSTER_ID, CLUSTER_NAME);");
                out.println("    }");
                out.println();

                for (final Attribute attribute : cluster.attributes.values()) {
                    DataTypeMap zclDataType = ZclDataType.getDataTypeMapping().get(attribute.dataType);

                    if (attribute.attributeAccess.toLowerCase().contains("write")) {
                        out.println();
                        outputAttributeJavaDoc(out, "Set", attribute, zclDataType);
                        out.println("    public Future<CommandResult> set"
                                + attribute.nameUpperCamelCase.replace("_", "") + "(final Object value) {");
                        out.println("        return write(attributes.get(" + attribute.enumName + "), value);");
                        out.println("    }");
                    }

                    if (attribute.attributeAccess.toLowerCase().contains("read")) {
                        outputAttributeJavaDoc(out, "Get", attribute, zclDataType);
                        out.println("    public Future<CommandResult> get"
                                + attribute.nameUpperCamelCase.replace("_", "") + "Async() {");
                        out.println("        return read(attributes.get(" + attribute.enumName + "));");
                        out.println("    }");
                        out.println();
                        outputAttributeJavaDoc(out, "Synchronously get", attribute, zclDataType);
                        out.println("    public " + attribute.dataTypeClass + " get"
                                + attribute.nameUpperCamelCase.replace("_", "") + "(final long refreshPeriod) {");
                        out.println("        if(refreshPeriod > 0 && attributes.get(" + attribute.enumName
                                + ").getLastReportTime() != null) {");
                        out.println(
                                "            long refreshTime = Calendar.getInstance().getTimeInMillis() - refreshPeriod;");
                        out.println("            if(attributes.get(" + attribute.enumName
                                + ").getLastReportTime().getTimeInMillis() < refreshTime) {");
                        out.println("                return (" + attribute.dataTypeClass + ") attributes.get("
                                + attribute.enumName + ").getLastValue();");
                        out.println("            }");
                        out.println("        }");
                        out.println();
                        out.println("        return (" + attribute.dataTypeClass + ") readSync(attributes.get("
                                + attribute.enumName + "));");
                        out.println("    }");
                    }

                    if (attribute.attributeAccess.toLowerCase().contains("read")
                            && attribute.attributeReporting.toLowerCase().equals("mandatory")) {
                        out.println();
                        outputAttributeJavaDoc(out, "Set reporting for", attribute, zclDataType);
                        if (zclDataType.analogue) {
                            out.println("    public Future<CommandResult> set" + attribute.nameUpperCamelCase
                                    + "Reporting(final int minInterval, final int maxInterval, final Object reportableChange) {");
                            out.println("        return setReporting(attributes.get(" + attribute.enumName
                                    + "), minInterval, maxInterval, reportableChange);");
                        } else {
                            out.println("    public Future<CommandResult> set" + attribute.nameUpperCamelCase
                                    + "Reporting(final int minInterval, final int maxInterval) {");
                            out.println("        return setReporting(attributes.get(" + attribute.enumName
                                    + "), minInterval, maxInterval);");
                        }
                        out.println("    }");
                    }
                }

                for (final Command command : commands) {
                    out.println();
                    out.println("    /**");
                    out.println("     * The " + command.commandLabel);
                    if (command.commandDescription.size() != 0) {
                        out.println("     * <p>");
                        for (String line : command.commandDescription) {
                            out.println("     * " + line);
                        }
                    }
                    out.println("     *");

                    final LinkedList<Field> fields = new LinkedList<Field>(command.fields.values());
                    for (final Field field : fields) {
                        out.println("     * @param " + field.nameLowerCamelCase + " {@link " + field.dataTypeClass
                                + "} " + field.fieldLabel);
                    }

                    out.println("     * @return the {@link Future<CommandResult>} command result future");
                    out.println("     */");
                    out.print("    public Future<CommandResult> " + command.nameLowerCamelCase + "(");

                    boolean first = true;
                    for (final Field field : fields) {
                        if (first == false) {
                            out.print(", ");
                        }
                        out.print(field.dataTypeClass + " " + field.nameLowerCamelCase);
                        first = false;
                    }

                    out.println(") {");
                    out.println("        " + command.nameUpperCamelCase + " command = new " + command.nameUpperCamelCase
                            + "();");
                    if (fields.size() != 0) {
                        out.println();
                        out.println("        // Set the fields");
                    }
                    for (final Field field : fields) {
                        out.println("        command.set" + field.nameUpperCamelCase + "(" + field.nameLowerCamelCase
                                + ");");
                    }
                    out.println();
                    out.println("        return send(command);");
                    out.println("    }");
                }

                if (readAttributes) {
                    out.println();
                    out.println("    /**");
                    out.println("     * Add a binding for this cluster to the local node");
                    out.println("     *");
                    out.println("     * @return the {@link Future<CommandResult>} command result future");
                    out.println("     */");
                    out.println("    public Future<CommandResult> bind() {");
                    out.println("        return bind();");
                    out.println("    }");
                }

                if (cluster.received.size() > 0) {
                    out.println();
                    out.println("    @Override");
                    out.println("    public ZclCommand getCommandFromId(int commandId) {");
                    out.println("        switch (commandId) {");
                    for (final Command command : cluster.received.values()) {
                        out.println("            case " + command.commandId + ": // " + command.commandType);
                        out.println("                return new " + command.nameUpperCamelCase + "();");
                    }
                    out.println("            default:");
                    out.println("                return null;");
                    out.println("        }");
                    out.println("    }");
                }

                if (cluster.generated.size() > 0) {
                    out.println();
                    out.println("    @Override");
                    out.println("    public ZclCommand getResponseFromId(int commandId) {");
                    out.println("        switch (commandId) {");
                    for (final Command command : cluster.generated.values()) {
                        out.println("            case " + command.commandId + ": // " + command.commandType);
                        out.println("                return new " + command.nameUpperCamelCase + "();");
                    }
                    out.println("            default:");
                    out.println("                return null;");
                    out.println("        }");
                    out.println("    }");
                }

                out.println("}");

                out.flush();
                out.close();
            }
        }
    }

    private static void outputAttributeJavaDoc(PrintWriter out, String type, Attribute attribute,
            DataTypeMap zclDataType) {
        out.println();
        out.println("    /**");
        out.println("     * " + type + " the <i>" + attribute.attributeLabel + "</i> attribute [attribute ID <b>"
                + attribute.attributeId + "</b>].");
        if (attribute.attributeDescription.size() != 0) {
            out.println("     * <p>");
            for (String line : attribute.attributeDescription) {
                out.println("     * " + line);
            }
        }
        if ("Synchronously get".equals(type)) {
            out.println("     * <p>");
            out.println("     * This method can return cached data if the attribute has already been received.");
            out.println(
                    "     * The parameter <i>refreshPeriod</i> is used to control this. If the attribute has been received");
            out.println(
                    "     * within <i>refreshPeriod</i> milliseconds, then the method will immediately return the last value");
            out.println(
                    "     * received. If <i>refreshPeriod</i> is set to 0, then the attribute will always be updated.");
            out.println("     * <p>");
            out.println(
                    "     * This method will block until the response is received or a timeout occurs unless the current value is returned.");
        }
        out.println("     * <p>");
        out.println("     * The attribute is of type {@link " + attribute.dataTypeClass + "}.");
        out.println("     * <p>");
        out.println("     * The implementation of this attribute by a device is "
                + attribute.attributeImplementation.toUpperCase());
        out.println("     *");
        if ("Set reporting for".equals(type)) {
            out.println("     * @param minInterval {@link int} minimum reporting period");
            out.println("     * @param maxInterval {@link int} maximum reporting period");
            if (zclDataType.analogue) {
                out.println("     * @param reportableChange {@link Object} delta required to trigger report");
            }
        } else if ("Set".equals(type)) {
            out.println("     * @param " + attribute.nameLowerCamelCase + " the {@link " + attribute.dataTypeClass
                    + "} attribute value to be set");
        }

        if ("Synchronously get".equals(type)) {
            out.println(
                    "     * @param refreshPeriod the maximum age of the data (in milliseconds) before an update is needed");
            out.println("     * @return the {@link " + attribute.dataTypeClass + "} attribute value, or null on error");
        } else {
            out.println("     * @return the {@link Future<CommandResult>} command result future");
        }
        out.println("     */");
    }

    private static PrintWriter getClassOut(File packageFile, String className) throws FileNotFoundException {
        final File classFile = new File(packageFile + File.separator + className + ".java");
        System.out.println("Generating: " + classFile.getAbsolutePath());
        final FileOutputStream fileOutputStream = new FileOutputStream(classFile, false);
        return new PrintWriter(fileOutputStream);
    }

    public static List<String> splitString(String msg, int lineSize) {
        List<String> res = new ArrayList<String>();

        Pattern p = Pattern.compile("\\b.{1," + (lineSize - 1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);

        while (m.find()) {
            System.out.println(m.group().trim()); // Debug
            res.add(m.group());
        }
        return res;
    }

    private static String getZclClusterCommandPackage(String packageRoot, Cluster cluster) {
        return packageRoot + packageZclProtocolCommand + "." + cluster.clusterType.replace("_", "").toLowerCase();
    }

    private static String getFieldType(Field field) {
        if (field.listSizer != null) {
            return "List<" + field.dataTypeClass + ">";
        } else {
            return field.dataTypeClass;
        }
    }

    private static void generateZdpCommandClasses(Context context, String packageRootPrefix, File sourceRootPath)
            throws IOException {

        // List of fields that are handled internally by super class
        List<String> reservedFields = new ArrayList<String>();
        reservedFields.add("status");

        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());
            for (final Cluster cluster : clusters) {
                final ArrayList<Command> commands = new ArrayList<Command>();
                commands.addAll(cluster.received.values());
                commands.addAll(cluster.generated.values());
                for (final Command command : commands) {
                    final String packageRoot = packageRootPrefix + packageZdpCommand;
                    final String packagePath = getPackagePath(sourceRootPath, packageRoot);
                    final File packageFile = getPackageFile(packagePath);

                    final String className = command.nameUpperCamelCase;
                    final PrintWriter out = getClassOut(packageFile, className);

                    final LinkedList<Field> fields = new LinkedList<Field>(command.fields.values());
                    boolean fieldWithDataTypeList = false;
                    for (final Field field : fields) {
                        if (field.listSizer != null) {
                            fieldWithDataTypeList = true;
                            break;
                        }

                        if (field.dataTypeClass.startsWith("List")) {
                            fieldWithDataTypeList = true;
                            break;
                        }
                    }

                    out.println("package " + packageRoot + ";");
                    out.println();
                    // out.println("import " + packageRootPrefix + packageZcl + ".ZclCommandMessage;");
                    // out.println("import " + packageRootPrefix + packageZdp + ".ZclCommand;");
                    // out.println("import " + packageRootPrefix + packageZcl + ".ZclField;");
                    if (fields.size() > 0) {
                        out.println("import " + packageRootPrefix + packageZcl + ".ZclFieldSerializer;");
                        out.println("import " + packageRootPrefix + packageZcl + ".ZclFieldDeserializer;");
                        out.println("import " + packageRootPrefix + packageZclProtocol + ".ZclDataType;");
                    }

                    if (className.endsWith("Request")) {
                        out.println("import " + packageRootPrefix + packageZdp + ".ZdoRequest;");
                    } else {
                        out.println("import " + packageRootPrefix + packageZdp + ".ZdoResponse;");
                    }

                    if (command.responseCommand != null && command.responseCommand.length() != 0) {
                        out.println("import " + packageRootPrefix + ".Command;");
                        out.println("import " + packageRootPrefix + ".CommandResponseMatcher;");
                        out.println("import " + packageRootPrefix + packageZdpCommand + "." + command.responseCommand
                                + ";");
                    }

                    if (fieldWithDataTypeList) {
                        out.println();
                        out.println("import java.util.List;");
                        out.println("import java.util.ArrayList;");
                    }

                    // out.println("import java.util.Map;");
                    // out.println("import java.util.HashMap;");

                    for (final Field field : fields) {

                        String packageName;
                        if (field.dataTypeClass.endsWith("Descriptor")) {
                            packageName = packageZdpDescriptors;
                        } else if (field.dataTypeClass.endsWith("Table")) {
                            packageName = packageZdpDescriptors;
                        } else {
                            packageName = packageZclField;
                        }

                        String typeName;
                        if (field.dataTypeClass.startsWith("List")) {
                            typeName = field.dataTypeClass;
                            typeName = typeName.substring(typeName.indexOf("<") + 1);
                            typeName = typeName.substring(0, typeName.indexOf(">"));
                        } else {
                            typeName = field.dataTypeClass;
                        }

                        // if (reservedFields.contains(field.nameLowerCamelCase)) {
                        // continue;
                        // }

                        switch (typeName) {
                            case "Integer":
                            case "Boolean":
                            case "Object":
                            case "Long":
                            case "String":
                                continue;
                            case "IeeeAddress":
                                out.println("import " + packageRootPrefix + "." + typeName + ";");
                                continue;
                            case "ZdoStatus":
                                out.println("import " + packageRootPrefix + packageZdp + ".ZdoStatus;");
                                continue;
                        }

                        out.println("import " + packageRootPrefix + packageName + "." + typeName + ";");
                    }

                    out.println();
                    out.println("/**");
                    out.println(" * " + command.commandLabel + " value object class.");

                    if (command.commandDescription != null && command.commandDescription.size() != 0) {
                        out.println(" * <p>");
                        for (String line : command.commandDescription) {
                            out.println(" * " + line);
                        }
                    }

                    if (cluster.clusterDescription.size() > 0) {
                        out.println(" * <p>");
                        for (String line : cluster.clusterDescription) {
                            out.println(" * " + line);
                        }
                    }

                    out.println(" * <p>");
                    out.println(" * Code is auto-generated. Modifications may be overwritten!");

                    out.println(" */");
                    if (className.endsWith("Request")) {
                        out.print("public class " + className + " extends ZdoRequest");
                    } else {
                        out.print("public class " + className + " extends ZdoResponse");
                    }

                    if (command.responseCommand != null && command.responseCommand.length() != 0) {
                        out.print(" implements CommandResponseMatcher");
                    }
                    out.println(" {");

                    for (final Field field : fields) {
                        if (reservedFields.contains(field.nameLowerCamelCase)) {
                            continue;
                        }

                        out.println("    /**");
                        out.println("     * " + field.fieldLabel + " command message field.");
                        out.println("     */");
                        out.println("    private " + getFieldType(field) + " " + field.nameLowerCamelCase + ";");
                        out.println();
                    }

                    // if (fields.size() > 0) {
                    // out.println(" static {");
                    // for (final Field field : fields) {
                    // out.println(" fields.put(" + field.fieldId + ", new ZclField(" + field.fieldId
                    // + ", \"" + field.fieldLabel + "\", ZclDataType." + field.dataType + "));");
                    // }
                    // out.println(" }");
                    // out.println();
                    // }

                    out.println("    /**");
                    out.println("     * Default constructor.");
                    out.println("     */");
                    out.println("    public " + className + "() {");
                    // out.println(" setType(ZclCommandType." + command.commandType + ");");
                    // out.println(" commandId = " + command.commandId + ";");
                    out.println("        clusterId = " + String.format("0x%04X", command.commandId) + ";");
                    // out.println(" commandDirection = "
                    // + (cluster.received.containsValue(command) ? "true" : "false") + ";");

                    out.println("    }");

                    for (final Field field : fields) {
                        if (reservedFields.contains(field.nameLowerCamelCase)) {
                            continue;
                        }

                        out.println();
                        out.println("    /**");
                        out.println("     * Gets " + field.fieldLabel + ".");
                        out.println("     *");
                        out.println("     * @return the " + field.fieldLabel);
                        out.println("     */");
                        out.println("    public " + getFieldType(field) + " get" + field.nameUpperCamelCase + "() {");
                        out.println("        return " + field.nameLowerCamelCase + ";");
                        out.println("    }");
                        out.println();
                        out.println("    /**");
                        out.println("     * Sets " + field.fieldLabel + ".");
                        out.println("     *");
                        out.println("     * @param " + field.nameLowerCamelCase + " the " + field.fieldLabel);
                        out.println("     */");
                        out.println("    public void set" + field.nameUpperCamelCase + "(final " + getFieldType(field)
                                + " " + field.nameLowerCamelCase + ") {");
                        out.println(
                                "        this." + field.nameLowerCamelCase + " = " + field.nameLowerCamelCase + ";");
                        out.println("    }");

                    }

                    if (fields.size() > 0) {
                        // out.println();
                        // out.println(" @Override");
                        // out.println(" public void setFieldValues(final Map<Integer, Object> values) {");
                        // for (final Field field : fields) {
                        // out.println(" " + field.nameLowerCamelCase + " = (" + field.dataTypeClass
                        // + ") values.get(" + field.fieldId + ");");
                        // }
                        // out.println(" }");

                        out.println();
                        out.println("    @Override");
                        out.println("    public void serialize(final ZclFieldSerializer serializer) {");
                        out.println("        super.serialize(serializer);");
                        out.println();
                        for (final Field field : fields) {
                            if (field.listSizer != null) {
                                out.println("        for (int cnt = 0; cnt < " + field.nameLowerCamelCase
                                        + ".size(); cnt++) {");
                                out.println("            serializer.serialize(" + field.nameLowerCamelCase
                                        + ".get(cnt), ZclDataType." + field.dataType + ");");
                                out.println("        }");
                            } else {
                                out.println("        serializer.serialize(" + field.nameLowerCamelCase
                                        + ", ZclDataType." + field.dataType + ");");
                            }
                        }
                        out.println("    }");

                        out.println();
                        out.println("    @Override");
                        out.println("    public void deserialize(final ZclFieldDeserializer deserializer) {");
                        out.println("        super.deserialize(deserializer);");
                        out.println();
                        boolean first = true;
                        for (final Field field : fields) {
                            if (field.listSizer != null) {
                                if (first) {
                                    out.println("        // Create lists");
                                    first = false;
                                }
                                out.println("        " + field.nameLowerCamelCase + " = new ArrayList<"
                                        + field.dataTypeClass + ">();");
                            }
                        }
                        if (first == false) {
                            out.println();
                        }
                        for (final Field field : fields) {
                            if (field.completeOnZero) {
                                out.println("        if (deserializer.isEndOfStream()) {");
                                out.println("            return;");
                                out.println("        }");
                            }

                            if (field.listSizer != null) {
                                out.println("        for (int cnt = 0; cnt < " + field.listSizer + "; cnt++) {");
                                out.println("            " + field.nameLowerCamelCase + ".add((" + field.dataTypeClass
                                        + ") deserializer.deserialize(" + "ZclDataType." + field.dataType + "));");
                                out.println("        }");
                            } else {
                                out.println("        " + field.nameLowerCamelCase + " = (" + field.dataTypeClass
                                        + ") deserializer.deserialize(" + "ZclDataType." + field.dataType + ");");
                            }
                            if (field.completeOnZero) {
                                out.println("        if (" + field.nameLowerCamelCase + " == 0) {");
                                out.println("            return;");
                                out.println("        }");
                            }

                            if (field.nameLowerCamelCase.equals("status")) {
                                out.println("        if (status != ZdoStatus.SUCCESS) {");
                                out.println("            // Don't read the full response if we have an error");
                                out.println("            return;");
                                out.println("        }");
                            }
                        }
                        out.println("    }");
                    }

                    if (command.responseCommand != null && command.responseCommand.length() != 0) {
                        out.println();
                        out.println("    @Override");
                        out.println("    public boolean isMatch(Command request, Command response) {");
                        out.println("        if (!(response instanceof " + command.responseCommand + ")) {");
                        out.println("            return false;");
                        out.println("        }");
                        out.println();
                        out.print("        return ");
                        out.println("(((" + command.nameUpperCamelCase + ") request).getDestinationAddress()");
                        out.print("                .equals(((" + command.responseCommand
                                + ") response).getSourceAddress()))");

                        for (String matcher : command.responseMatchers.keySet()) {
                            out.println();
                            out.print("                && ");
                            out.println("(((" + command.nameUpperCamelCase + ") request).get" + matcher + "()");
                            out.print("                .equals(((" + command.responseCommand + ") response).get"
                                    + command.responseMatchers.get(matcher) + "()))");
                        }
                        out.print(";");
                        out.println();
                        out.println("    }");
                    }

                    out.println();
                    out.println("    @Override");
                    out.println("    public String toString() {");
                    out.println("        final StringBuilder builder = new StringBuilder();");
                    out.println("        builder.append(\"" + className + " [\");");
                    out.println("        builder.append(super.toString());");
                    for (final Field field : fields) {
                        out.println("        builder.append(\", " + field.nameLowerCamelCase + "=\");");
                        out.println("        builder.append(" + field.nameLowerCamelCase + ");");
                    }
                    out.println("        builder.append(\"]\");");
                    out.println("        return builder.toString();");
                    out.println("    }");

                    out.println();
                    out.println("}");

                    out.flush();
                    out.close();
                }
            }
        }
    }

    private static String getZdoCommandTypeEnum(final Cluster cluster, final Command command, boolean received) {
        return command.commandType + "(" + String.format("0x%04X", command.commandId) + ", "
                + command.nameUpperCamelCase + ".class" + ")";
    }

    private static String getZdpClusterCommandPackage(String packageRoot, Cluster cluster) {
        return packageRoot + packageZdpCommand + "." + cluster.clusterType.replace("_", "").toLowerCase();
    }

    private static void generateZdoCommandTypeEnumeration(Context context, String packageRootPrefix,
            File sourceRootPath) throws IOException {
        final String className = "ZdoCommandType";

        final String packageRoot = packageRootPrefix + packageZdp;
        final String packagePath = getPackagePath(sourceRootPath, packageRoot);
        final File packageFile = getPackageFile(packagePath);

        final PrintWriter out = getClassOut(packageFile, className);

        out.println("package " + packageRoot + ";");
        out.println();

        Map<String, Command> commandEnum = new TreeMap<String, Command>();

        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());
            for (final Cluster cluster : clusters) {
                // Brute force to get the commands in order!
                for (int c = 0; c < 65535; c++) {
                    if (cluster.received.get(c) != null) {
                        out.println("import " + packageRootPrefix + packageZdpCommand + "."
                                + cluster.received.get(c).nameUpperCamelCase + ";");

                        commandEnum.put(getZdoCommandTypeEnum(cluster, cluster.received.get(c), true),
                                cluster.received.get(c));
                    }
                }
            }
        }
        out.println();

        out.println();
        outputClassJavaDoc(out);
        out.println("public enum " + className + " {");
        boolean first = true;
        for (String command : commandEnum.keySet()) {
            Command cmd = commandEnum.get(command);
            if (cmd == null) {
                System.out.println("Command without data: " + command);
                continue;
            }

            if (!first) {
                out.println(",");
            }
            first = false;
            out.println("    /**");
            out.println("     * " + cmd.commandLabel);
            out.println("     * <p>");
            out.println("     * See {@link " + cmd.nameUpperCamelCase + "}");
            out.println("     */");
            out.print("    " + command);
        }
        out.println(";");

        out.println();
        out.println("    private final int clusterId;");
        out.println("    private final Class<? extends ZdoCommand> commandClass;");
        out.println();
        out.println("    " + className + "(final int clusterId, final Class<? extends ZdoCommand> commandClass) {");
        out.println("        this.clusterId = clusterId;");
        out.println("        this.commandClass = commandClass;");
        // out.println(" this.label = label;");
        out.println("    }");
        out.println();

        out.println("    public int getClusterId() {");
        out.println("        return clusterId;");
        out.println("    }");
        out.println();
        out.println("    public Class<? extends ZdoCommand> getCommandClass() {");
        out.println("        return commandClass;");
        out.println("    }");
        out.println();
        out.println("    public static ZdoCommandType getValueById(final int clusterId) {");
        out.println("        for (final ZdoCommandType value : values()) {");
        out.println("            if(value.clusterId == clusterId) {");
        out.println("                return value;");
        out.println("            }");
        out.println("        }");
        out.println("        return null;");
        out.println("    }");
        out.println("}");

        out.flush();
        out.close();
    }

    private static void generateZdpCommandTransactions(Context context, String packageRootPrefix, File sourceRootPath)
            throws IOException {

        final LinkedList<Profile> profiles = new LinkedList<Profile>(context.profiles.values());
        for (final Profile profile : profiles) {
            final LinkedList<Cluster> clusters = new LinkedList<Cluster>(profile.clusters.values());
            for (final Cluster cluster : clusters) {
                final ArrayList<Command> commands = new ArrayList<Command>();
                commands.addAll(cluster.received.values());
                commands.addAll(cluster.generated.values());
                for (final Command command : commands) {
                    if (command.responseCommand == null || command.responseCommand.length() == 0) {
                        continue;
                    }

                    final String packageRoot = packageRootPrefix + packageZdpTransaction;
                    final String packagePath = getPackagePath(sourceRootPath, packageRoot);
                    final File packageFile = getPackageFile(packagePath);

                    final String className = command.nameUpperCamelCase + "Transaction";
                    final PrintWriter out = getClassOut(packageFile, className);

                    out.println("package " + packageRoot + ";");
                    out.println();

                    // out.println("import " + packageRootPrefix + packageZcl + ".ZclCommandMessage;");
                    // out.println("import " + packageRootPrefix + packageZdp + ".ZclCommand;");
                    // out.println("import " + packageRootPrefix + packageZcl + ".ZclField;");

                    out.println(
                            "import " + packageRootPrefix + packageZdpCommand + "." + command.nameUpperCamelCase + ";");
                    out.println(
                            "import " + packageRootPrefix + packageZdpCommand + "." + command.responseCommand + ";");

                    out.println("import " + packageRootPrefix + ".Command;");
                    out.println("import " + packageRootPrefix + ".CommandResponseMatcher;");
                    // out.println("import " + packageRootPrefix + packageZdp + ".ZdoRequest;");
                    // out.println("import " + packageRootPrefix + packageZdp + ".ZdoResponse;");

                    // out.println("import java.util.Map;");
                    // out.println("import java.util.HashMap;");

                    out.println();
                    out.println("/**");
                    out.println(" * " + command.commandLabel + " transaction class.");

                    if (command.commandDescription != null && command.commandDescription.size() != 0) {
                        out.println(" * <p>");
                        for (String line : command.commandDescription) {
                            out.println(" * " + line);
                        }
                    }

                    if (cluster.clusterDescription.size() > 0) {
                        out.println(" * <p>");
                        for (String line : cluster.clusterDescription) {
                            out.println(" * " + line);
                        }
                    }

                    out.println(" * <p>");
                    out.println(" * Code is auto-generated. Modifications may be overwritten!");

                    out.println(" */");
                    out.println("public class " + className + " implements CommandResponseMatcher {");

                    // out.println(" /**");
                    // out.println(" * Default constructor.");
                    // out.println(" */");
                    // out.println(" public " + className + "() {");
                    // out.println(" }");
                    // out.println(" setType(ZclCommandType." + command.commandType + ");");
                    // out.println(" commandId = " + command.commandId + ";");
                    // out.println(" commandDirection = "
                    // + (cluster.received.containsValue(command) ? "true" : "false") + ";");

                    out.println();
                    out.println("    @Override");
                    out.println("    public boolean isMatch(Command request, Command response) {");
                    out.println("        if (response instanceof " + command.responseCommand + ") {");
                    // out.println(" return ((" + command.nameUpperCamelCase + ") request).get"
                    // + command.responseRequest + "() == ((" + command.responseCommand + ") response).get"
                    // + command.responseResponse + "();");
                    out.println("        } else {");
                    out.println("            return false;");
                    out.println("        }");
                    out.println("    }");

                    out.println();
                    out.println("}");

                    out.flush();
                    out.close();
                }
            }
        }
    }

}
