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
        String componentType = null;
        String featurePropertyValue = null;
        String performanceClassifications = null;
        List<String> newList = new ArrayList<>();
        StringBuilder strQuerySelect = new StringBuilder("PREFIX myOnto: " +
                "<http://www.semanticweb.org/asusx450j/ontologies/2019/2/system-unit-of-desktop-computer#>\n" +
                "SELECT ?Component");
        StringBuilder strQueryWhere = new StringBuilder();
        Scanner scanner = new Scanner(resultAIML);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("<list>") || line.equals("</list>") || line.equals("<br/>")) {
                line.substring(0, 0);
            } else if (line.contains("<item>")) {
                line = line.substring(10, line.lastIndexOf("</item>"));
                if (line.contains("and") && !line.contains("DEFAULT")) {
                    String[] temp = line.split("and ");
                    for (String data : temp) {
                        List<String> list = Arrays.asList(data.trim().split("-"));
                        newList.add(list.toString());
                    }
                }
                if (!line.contains("DEFAULT") && !line.contains("and")) {
                    List<String> list = Arrays.asList(line.split("-"));
                    newList.add(list.toString());
                }
            } else if (line.contains("DENGAN KLASIFIKASI")) {
                line = line.substring(line.lastIndexOf(" ") + 1);
                newList.add(0, line);
            }
        }
        System.out.println(Arrays.toString(newList.toArray()));
        int index = 0;
        for (Object show : newList.toArray()) {

            if (!show.toString().contains("[") || !show.toString().contains("]")) {
                performanceClassifications = show.toString();
                dependantComponent(strQueryWhere, "Motherboard", performanceClassifications);
                dependantComponent(strQueryWhere, "RAM", performanceClassifications);
            } else {
                show = show.toString().substring(1, show.toString().lastIndexOf("]"));
                componentType = show.toString().substring(0, show.toString().lastIndexOf(","));
                featurePropertyValue = show.toString().substring(show.toString().lastIndexOf(" ") + 1);
                strQueryWhere.append(" WHERE {\n")
                        .append("Type(?Component, myOnto:")
                        .append(componentType)
                        .append("), \n")
                        .append("PropertyValue(?Component, myOnto:hasFeature, myOnto:")
                        .append(featurePropertyValue);
                boolean performancePropertyCondition =
                        componentType.equals("CPU") || componentType.equals("VideoCard") || componentType.equals("RAM");
                if (index++ == newList.size() - 2) {
                    if (performancePropertyCondition) {
                        strQueryWhere.append("), \n");
                        strQueryWhere.append("PropertyValue(?Component, myOnto:hasPerformanceClassification, myOnto:")
                                .append(performanceClassifications)
                                .append(")");
                    } else {
                        strQueryWhere.append(")");
                    }
                    strQueryWhere.append("}");
                } else {
                    if (performancePropertyCondition) {
                        strQueryWhere.append("), \n");
                        strQueryWhere.append("PropertyValue(?Component, myOnto:hasPerformanceClassification, myOnto:")
                                .append(performanceClassifications)
                                .append(")");
                    } else {
                        strQueryWhere.append(")");
                    }
                    strQueryWhere.append("}")
                            .append("\nOR");
                }
                System.out.println(index);
                System.out.println(newList.size() - 1);
            }
        }
        System.out.println(strQuerySelect.append(strQueryWhere));
        scanner.close();
        return strQuerySelect.append(strQueryWhere);
    }

    public static void dependantComponent(StringBuilder strQueryWhere, String component, String performanceClassifications){
        strQueryWhere.append(" WHERE {\n")
                .append("Type(?Component, myOnto:")
                .append(component)
                .append("), \n")
                .append("PropertyValue(?Component, myOnto:hasPerformanceClassification, myOnto:")
                .append(performanceClassifications).append(")}").append("\nOR");
    }
}