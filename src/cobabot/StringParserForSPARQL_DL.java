package cobabot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author Fizikri
 */
public class StringParserForSPARQL_DL {
    public static StringBuilder function(String resultAIML) {
        String componentType = " ";
        String featurePropertyValue;
        String performanceClassifications = null;
        String manufacturer = null;
        String chassisSize = null;
        String componentSelect = "";

        Boolean perfCl = false;
        Boolean manuf = false;
        Boolean cS = false;

        int indexProperty = -1;
        int minActualSize = 1;
        boolean motherboardException = false;

        String previousComponent = "";

        List<String> newList = new ArrayList<>();
        StringBuilder strQuerySelect = new StringBuilder("PREFIX myOnto: " +
                "<http://www.semanticweb.org/asusx450j/ontologies/2019/2/system-unit-of-desktop-computer#>\n" +
                "SELECT DISTINCT ?Component");
        StringBuilder strQueryWhere = new StringBuilder();
        Scanner scanner = new Scanner(resultAIML);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("<list>") || line.equals("</list>") || line.equals("<br/>")) {
                line.substring(0, 0);
            } else if (line.contains("<item>")) {
                line = line.substring(line.lastIndexOf("<item>") + 6, line.lastIndexOf("</item>"));
                if (line.contains("and") && !line.contains("DEFAULT") && !line.contains("unknown")) {
                    String[] temp = line.split("and ");
                    for (String data : temp) {
                        List<String> list = Arrays.asList(data.trim().split("-"));
                        newList.add(list.toString());
                    }
                }
                if (!line.contains("unknown") && !line.contains("DEFAULT") && !line.contains("and")) {
                    List<String> list = Arrays.asList(line.split("-"));
                    newList.add(list.toString());
                }
            } else if (line.contains("DENGAN KLASIFIKASI")) {
                line = line.substring(line.lastIndexOf(" ") + 1);
                indexProperty = indexProperty + 1;
                newList.add(indexProperty, line);
                perfCl = true;
            } else if (line.contains("DENGAN UKURAN")) {
                line = line.substring(line.lastIndexOf(" ") + 1);
                indexProperty = indexProperty + 1;
                newList.add(indexProperty, line);
                cS = true;
            } else if (line.contains("DENGAN MANUFAKTUR")) {
                line = line.substring(line.lastIndexOf(" ") + 1);
                indexProperty = indexProperty + 1;
                newList.add(indexProperty, line);
                manuf = true;
            } else if (line.contains("KOMPONEN")) {
                line = line.substring(line.lastIndexOf(" ") + 1);
                componentSelect = line;
            }
        }
        System.out.println(Arrays.toString(newList.toArray()));
        int index = -1;

        for (Object show : newList.toArray()) {
            if (show.toString().contains("RGB")){
                motherboardException = true;
            }
            System.out.println(motherboardException);
        }

        for (Object show : newList.toArray()) {
            index++;
            System.out.println(index);
            System.out.println(indexProperty);
            if (index == 0 && perfCl) { // Untuk Performa klasifikasi
                performanceClassifications = show.toString();
                if (componentSelect.equals("RAM"))
                    dependantComponentRAM(strQueryWhere, performanceClassifications);
            } else if (index == 1 && cS) { // Terbaru: Untuk ukuran chassis
                chassisSize = show.toString();
                if (componentSelect.equals("Chassis"))
                    dependantComponentChassis(strQueryWhere, chassisSize);
            } else if (index == 2 && manuf) { // Terbaru: Untuk manufaktur amd atau intel berlaku utk motherboard dan cpu
                manufacturer = show.toString();
                if (!motherboardException && componentSelect.equals("Motherboard"))
                    dependantComponentMotherboard(strQueryWhere, chassisSize, performanceClassifications, manufacturer);
            } else { // Pasangan komponen - fitur
                System.out.println(show);
                show = show.toString().substring(1, show.toString().lastIndexOf("]"));
                componentType = show.toString().substring(0, show.toString().lastIndexOf(","));
                featurePropertyValue = show.toString().substring(show.toString().lastIndexOf(" ") + 1);
                ArrayList list = getArrayListOfFeature(componentType, newList, index);
                if (componentType.equals("ComputerCooling") && componentSelect.equals("ComputerCooling")) {
                    dependantComponentComputerCooling(strQueryWhere, chassisSize, featurePropertyValue);
                } else if (componentType.equals("CPU") && componentSelect.equals("CPU")) {
                    dependantComponentCPU(strQueryWhere, featurePropertyValue, manufacturer);
                } else if (componentType.equals("Motherboard") && componentSelect.equals("Motherboard") && motherboardException) {
                    dependantComponentMotherboardWithFeature(strQueryWhere, chassisSize, performanceClassifications, manufacturer, featurePropertyValue);
                } else {
                    if (previousComponent.equals(componentType)) {
                        minActualSize = minActualSize + 1;
                        continue;
                    }
                    System.out.println(list);
                    strQueryWhere.append(" WHERE {\n")
                            .append("Type(?Component, myOnto:")
                            .append(componentType);
                    for (Object object : list) {
                        strQueryWhere.append("), \n")
                                .append("PropertyValue(?Component, myOnto:hasFeature, myOnto:")
                                .append(object);
                    }
                    ;
                }

                if (componentType.equals("Motherboard") && !motherboardException) {
                    strQueryWhere.append("), \n");
                    strQueryWhere.append("PropertyValue(?Component, myOnto:hasPerformanceClassification, myOnto:")
                            .append(performanceClassifications)
                            .append(")");
                }

                boolean performancePropertyCondition =
                        componentType.equals("CPU") || componentType.equals("VideoCard") || componentType.equals("RAM");
                System.out.println("Index:" + index);
                System.out.println("newList.size:" + minActualSize);
                if (performancePropertyCondition) {
                    strQueryWhere.append("), \n");
                    strQueryWhere.append("PropertyValue(?Component, myOnto:hasPerformanceClassification, myOnto:")
                            .append(performanceClassifications)
                            .append(")");
                } else {
                    strQueryWhere.append(")");
                }
                strQueryWhere.append("}");
            }
            previousComponent = componentType;
        }
        System.out.println(strQuerySelect.append(strQueryWhere));
        scanner.close();
        return strQuerySelect.append(strQueryWhere);
    }

    private static ArrayList getArrayListOfFeature(String componentType, List<String> newList, int index) {
        int index2 = -1;
        ArrayList list = new ArrayList<String>();
        for (Object component : newList.toArray()) {
            String componentString = component.toString();
            index2 = index2 + 1;
            if (index2 >= index) {
                if (componentType.equals(componentString.substring(1, componentString.lastIndexOf(",")))) {
                    list.add(componentString.substring(componentString.indexOf(", ") + 2,
                            componentString.lastIndexOf("]")));
                }
            }
        }
        return list;
    }

    private static void dependantComponentRAM(StringBuilder strQueryWhere, String performanceClassifications) {
        strQueryWhere.append(" WHERE {\n")
                .append("Type(?Component, myOnto:")
                .append("RAM")
                .append("), \n")
                .append("PropertyValue(?Component, myOnto:hasPerformanceClassification, myOnto:")
                .append(performanceClassifications).append(")}");
    }

    private static void dependantComponentMotherboard(StringBuilder strQueryWhere, String chassisSize,
                                                      String performanceClassifications, String manufacturer) {
        strQueryWhere.append(" WHERE {\n")
                .append("Type(?Component, myOnto:")
                .append(chassisSize)
                .append("), \n")
                .append("PropertyValue(?Component, myOnto:compatibleWith, ?Component2")
                .append("), \n")
                .append("PropertyValue(?Component2, myOnto:hasPerformanceClassification, myOnto:")
                .append(performanceClassifications)
                .append("), \n")
                .append("PropertyValue(?Component2, myOnto:hasManufacturer, myOnto:")
                .append(manufacturer)
                .append(")}");
    }

    private static void dependantComponentComputerCooling(StringBuilder strQueryWhere, String chassisSize,
                                                          String feature) {
        strQueryWhere.append(" WHERE {\n")
                .append("Type(?TempComponent, myOnto:")
                .append(chassisSize)
                .append("), \n")
                .append("PropertyValue(?TempComponent, myOnto:compatibleWith, ?Component")
                .append("), \n")
                .append("Type(?Component, myOnto:ComputerCooling")
                .append("), \n")
                .append("PropertyValue(?Component, myOnto:hasFeature, myOnto:")
                .append(feature);
    }

    private static void dependantComponentChassis(StringBuilder strQueryWhere, String chassisSize) {
        strQueryWhere.append(" WHERE {\n")
                .append("Type(?Component, myOnto:")
                .append(chassisSize)
                .append(")}");
    }

    private static void dependantComponentCPU(StringBuilder strQueryWhere, String featurePropertyValue, String manufacturer) {
        strQueryWhere.append(" WHERE {\n")
                .append("Type(?Component, myOnto:CPU")
                .append("), \n")
                .append("PropertyValue(?Component, myOnto:hasFeature, myOnto:")
                .append(featurePropertyValue)
                .append("), \n")
                .append("PropertyValue(?Component, myOnto:hasManufacturer, myOnto:")
                .append(manufacturer);
    }

    private static void dependantComponentMotherboardWithFeature(StringBuilder strQueryWhere, String chassisSize,
                                                                 String performanceClassifications, String manufacturer, String feature) {
        strQueryWhere.append(" WHERE {\n")
                .append("Type(?Component, myOnto:")
                .append(chassisSize)
                .append("), \n")
                .append("PropertyValue(?Component, myOnto:compatibleWith, ?Component2")
                .append("), \n")
                .append("PropertyValue(?Component2, myOnto:hasPerformanceClassification, myOnto:")
                .append(performanceClassifications)
                .append("), \n")
                .append("PropertyValue(?Component2, myOnto:hasManufacturer, myOnto:")
                .append(manufacturer)
                .append("), \n")
                .append("PropertyValue(?Component2, myOnto:hasFeature, myOnto:")
                .append(feature);
    }
}