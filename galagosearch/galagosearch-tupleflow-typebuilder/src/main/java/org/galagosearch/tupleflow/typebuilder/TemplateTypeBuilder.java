// BSD License (http://www.galagosearch.org/license)
package org.galagosearch.tupleflow.typebuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.galagosearch.tupleflow.typebuilder.FieldSpecification.DataType;

/**
 *
 * @author trevor
 */
public class TemplateTypeBuilder {
    StringTemplateGroup template;
    String typeName;
    String typePackage;
    ArrayList<Field> typeFields;
    ArrayList<OrderSpec> typeOrders;

    /**
     * For an array master, returns
     * an array containing the last master.length-index elements.
     */
    public static String[] subarray(String[] master, int index) {
        if (master.length <= index) {
            return new String[0];
        } else {
            String[] sub = new String[master.length - index];
            System.arraycopy(master, index, sub, 0, sub.length);
            return sub;
        }
    }

    /**
     * Returns a string containing all the elements of args, space delimited.
     */
    public static String join(String[] args, String delimiter) {
        String output = "";
        StringBuilder builder = new StringBuilder();

        for (String arg : args) {
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(arg);
        }

        return builder.toString();
    }

    public static String join(String[] args) {
        return join(args, " ");
    }

    public static String caps(String input) {
        if (input.length() == 0) {
            return input;
        }
        char first = Character.toUpperCase(input.charAt(0));
        return "" + first + input.substring(1);
    }

    public static String plural(String input) {
        return input + "s";
    }

    public static class Field {
        public Field(DataType type, String name) {
            this.dataType = type;
            this.type = type.getType();
            this.name = name;
            this.capsName = caps(name);
            this.pluralName = plural(name);
            
            this.inputType = caps(type.getInternalType());
            this.baseType = type.getBaseType();
            this.classTypeName = type.getClassName();
            this.isArray = dataType.isArray();
        }
        public DataType dataType;
        public String type;
        public String name;
        public String baseType;
        public boolean isInteger;
        public boolean isString;
        public boolean isArray;
        public String classTypeName;
        public String capsName;
        public String inputType;
        public String pluralName;
    }

    public static class OrderedField extends Field {
        public OrderedField(Field field, boolean ascending, boolean delta, boolean runLengthEncoded) {
            this(field.dataType, field.name, ascending, delta, runLengthEncoded);
        }

        public OrderedField(DataType type, String name, boolean ascending, boolean delta, boolean runLengthEncoded) {
            super(type, name);
            this.ascending = ascending;
            this.direction = ascending ? "+" : "-";
            this.directionName = ascending ? "Ascending" : "Descending";
            this.runLengthEncoded = runLengthEncoded;
            this.delta = delta;
        }
        public boolean ascending;
        public String direction;
        public String directionName;
        public boolean delta;
        public boolean runLengthEncoded;
        public ArrayList<OrderedField> remaining = new ArrayList();
    }

    public static class OrderSpec {
        public OrderSpec(OrderSpecification spec, ArrayList<Field> allFields) {
            this.allFields = allFields;

            HashMap<String, Field> fieldMap = new HashMap();
            HashSet<String> orderedNames = new HashSet();

            for (Field f : allFields) {
                fieldMap.put(f.name, f);
            }

            ArrayList<OrderedFieldSpecification> inputFields = spec.getOrderedFields();
            
            for (int i = 0; i < inputFields.size(); i++) {
                Field field = fieldMap.get(inputFields.get(i).getName());
                boolean isLastField = ((inputFields.size() - i) == 1);
                // BUGBUG: this is where delta encoding should go
                boolean useDelta = false; //isLastField && (field.isInteger || field.isString); 
                boolean useRLE = !useDelta;
                boolean isAscending = (inputFields.get(i).getDirection() == Direction.ASCENDING);
                String fieldName = inputFields.get(i).getName();

                if (fieldMap.get(fieldName) == null) {
                    throw new RuntimeException("'" + fieldName + "' is specified in an order statement, " +
                                               "but it isn't a type field name.");
                }
                
                OrderedField ordered = new OrderedField(fieldMap.get(fieldName),
                                                        isAscending, useDelta, useRLE);
                orderedFields.add(ordered);
                orderedNames.add(fieldName);
            }

            for (int i = 0; i < inputFields.size(); i++) {
                orderedFields.get(i).remaining.addAll(
                        orderedFields.subList(i + 1, orderedFields.size()));
            }

            backwardOrderedFields.addAll(orderedFields);
            Collections.reverse(backwardOrderedFields);

            for (int i = 0; i < orderedFields.size(); i++) {
                if (orderedFields.get(i).runLengthEncoded) {
                    rleFields.add(orderedFields.get(i));
                }
                if (orderedFields.get(i).delta) {
                    deltaFields.add(orderedFields.get(i));
                }
            }

            for (int i = 0; i < orderedFields.size(); i++) {
                OrderedField current = orderedFields.get(i);
                OrderedField next = ((i + 1) < orderedFields.size()) ? orderedFields.get(i + 1) : null;
                OrderedField previous = (i != 0) ? orderedFields.get(i - 1) : null;

                fieldPairs.add(new FieldPair(current, next, previous));
            }

            for (int i = 0; i < allFields.size(); i++) {
                if (orderedNames.contains(allFields.get(i).name)) {
                    continue;
                }
                unorderedFields.add(allFields.get(i));
            }
        }

        public String getClassName() {
            StringBuilder builder = new StringBuilder();

            if (orderedFields.size() > 0) {
                for (OrderedField orderedField : orderedFields) {
                    if (!orderedField.ascending) {
                        builder.append("Desc");
                    }
                    builder.append(caps(orderedField.name));
                }

                builder.append("Order");
            } else {
                builder.append("Unordered");
            }

            return builder.toString();
        }

        public static class FieldPair {
            public FieldPair(OrderedField c, OrderedField n, OrderedField p) {
                current = c;
                next = n;
                previous = p;
            }
            public OrderedField current;
            public OrderedField next;
            public OrderedField previous;
        }
        public ArrayList<OrderedField> orderedFields = new ArrayList();
        public ArrayList<OrderedField> backwardOrderedFields = new ArrayList();
        public ArrayList<FieldPair> fieldPairs = new ArrayList();
        public ArrayList<OrderedField> rleFields = new ArrayList();
        public ArrayList<Field> unorderedFields = new ArrayList();
        public ArrayList<OrderedField> deltaFields = new ArrayList();
        public ArrayList<Field> allFields;
    }

    /**
     * Returns the text of a Type<T> object for the T class.
     */
    @Override
    public String toString() {
        StringTemplate t = template.getInstanceOf("type");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("typeName", this.typeName);
        map.put("package", this.typePackage);
        map.put("orders", this.typeOrders);
        map.put("fields", this.typeFields);

        boolean containsArray = false;
        for (Field f : typeFields) {
            containsArray = containsArray || f.isArray;
        }
        map.put("containsArray", containsArray);
        t.setAttributes(map);
        return t.toString();
    }

    public TemplateTypeBuilder(TypeSpecification spec) {
        CommonGroupLoader loader = new CommonGroupLoader("org/galagosearch/tupleflow/templates", null);
        StringTemplateGroup.registerGroupLoader(loader);
        StringTemplateGroup.registerDefaultLexer(AngleBracketTemplateLexer.class);
        this.template = StringTemplateGroup.loadGroup("GalagoType");

        this.typeName = spec.getTypeName();
        this.typePackage = spec.getPackageName();
        this.typeFields = new ArrayList<Field>();
        this.typeOrders = new ArrayList<OrderSpec>();

        for (FieldSpecification fieldSpec : spec.getFields()) {
            Field field = new Field(fieldSpec.getType(), fieldSpec.getName());
            typeFields.add(field);
        }

        for (OrderSpecification orderSpec : spec.getOrders()) {
            OrderSpec order = new OrderSpec(orderSpec, typeFields);
            typeOrders.add(order);
        }
    }
    
    public static void main(String[] args) throws java.lang.Exception {
        for (String fileName : args) {
            TypeSpecification spec = ParserDriver.getTypeSpecification(fileName);
            TemplateTypeBuilder builder = new TemplateTypeBuilder(spec);
            
            java.io.FileWriter writer = new java.io.FileWriter(spec.getTypeName() + ".java");
            String comment = "// This file was automatically generated with the command: \n" +
                         "//     java org.galagosearch.tupleflow.typebuilder.TemplateTypeBuilder ...\n";

            writer.write(comment);
            writer.write(builder.toString());
            writer.close();
        }
    }
}
