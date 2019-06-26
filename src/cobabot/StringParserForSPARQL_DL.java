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
        List<String> newList = new ArrayList<>();
        StringBuilder strQuerySelect = new StringBuilder("PREFIX myOnto: " +
                "<http://www.semanticweb.org/asusx450j/ontologies/2019/2/system-unit-of-desktop-computer#>\n" +
                "SELECT ?Component");
        StringBuilder strQueryWhere = new StringBuilder();
        Scanner scanner = new Scanner(resultAIML);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("<list>") || line.equals("</list>")) {
                line.substring(0, 0);
            } else if (!"INI COMPUTER OFFICE MU".equals(line)) {
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
//
            }
        }
        System.out.println(Arrays.toString(newList.toArray()));
        int index = 0;
        for (Object show : newList.toArray()) {
            show = show.toString().substring(1, show.toString().lastIndexOf("]"));
            componentType = show.toString().substring(0, show.toString().lastIndexOf(","));
            featurePropertyValue = show.toString().substring(show.toString().lastIndexOf(" ") + 1);
            strQueryWhere.append(" WHERE {\n")
                    .append("Type(?Component, myOnto:")
                    .append(componentType)
                    .append("), \n")
                    .append("PropertyValue(?Component, myOnto:hasFeature, myOnto:")
                    .append(featurePropertyValue);
            if(index++ == newList.size() - 1){
                strQueryWhere.append(")").append("}");
            } else {
                strQueryWhere.append(")").append("}")
                        .append("\nOR");
            }
        }
        scanner.close();
        return strQuerySelect.append(strQueryWhere);
    }
}
