import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String TAG_ID = "id";
    private static final String TAG_FIRST_NAME = "firstName";
    private static final String TAG_LAST_NAME = "lastName";
    private static final String TAG_COUNTRY = "country";
    private static final String TAG_AGE = "age";

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, ParseException {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list, listType);
        writeString(json, "data.json");

        Document document = buildDocument();
        Node staff = document.getDocumentElement();
        List<Employee> list2 = parseXML(staff);
        String json2 = listToJson(list2, listType);
        writeString(json2, "data2.json");

        String json3 = readString("new_data.json");
        List<Employee> list3 = jsonToList(json3);
        list3.forEach(System.out::println);
    }

    private static List<Employee> jsonToList(String json) throws ParseException {
        List<Employee> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(json);
        JSONArray jsonArray = (JSONArray) obj;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        for (Object jsonObject : jsonArray) {
            Employee employee = gson.fromJson(jsonObject.toString(), Employee.class);
            list.add(employee);
        }
        return list;
    }

    private static String readString(String nameFile) {
        String json = null;
        JSONParser parser = new JSONParser();
        try (BufferedReader bf = new BufferedReader(new FileReader(nameFile))) {
            Object obj = parser.parse(bf);
            JSONArray jsonArray = (JSONArray) obj;
            json = jsonArray.toJSONString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static Document buildDocument() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new File("data.xml"));
    }

    private static List<Employee> parseXML(Node node) {
        List<Employee> list = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node employee = nodeList.item(i);

            if (employee.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Employee employeeInstance = parseElement(employee);
            list.add(employeeInstance);
        }
        return list;
    }

    private static Employee parseElement(Node elementNode) {
        long id = 0;
        String firstName = null;
        String lastName = null;
        String country = null;
        int age = 0;

        NodeList elementChildren = elementNode.getChildNodes();
        for (int j = 0; j < elementChildren.getLength(); j++) {
            switch (elementChildren.item(j).getNodeName()) {
                case TAG_ID:
                    id = Long.parseLong(elementChildren.item(j).getTextContent());
                    break;
                case TAG_FIRST_NAME:
                    firstName = elementChildren.item(j).getTextContent();
                    break;
                case TAG_LAST_NAME:
                    lastName = elementChildren.item(j).getTextContent();
                    break;
                case TAG_COUNTRY:
                    country = elementChildren.item(j).getTextContent();
                    break;
                case TAG_AGE:
                    age = Integer.parseInt(elementChildren.item(j).getTextContent());
                    break;
            }
        }
        return new Employee(id, firstName, lastName, country, age);
    }

    private static void writeString(String json, String nameFile) {
        try (FileWriter file = new FileWriter(nameFile)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String listToJson(List<Employee> list, Type listType) {
        GsonBuilder builder = new GsonBuilder()
                .setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csvToBean = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            list = csvToBean.parse();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
