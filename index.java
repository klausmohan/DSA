import java.io.*;
import java.util.*;

class Property {
    int id;
    String address;
    float price;
    boolean isSold;
    Property next;
    Property prev; 
    String type;
    String category;

    Property(int id, String address, float price, String type, String category) {
        this.id = id;
        this.address = address;
        this.price = price;
        this.isSold = false;
        this.next = null;
        this.prev = null;
        this.type = type;
        this.category = category;
    }

    @Override
    public String toString() {
        String status = isSold ? "Sold" : "Available";
        return String.format("ID: %d | Address: %s | Price: $%.2f | Type: %s | Category: %s | Status: %s",
                             id, address, price, type, category, status);
    }

    public String toCSV() {
        return id + "," + address + "," + price + "," + isSold + "," + type + "," + category;
    }

    public static Property fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        int id = Integer.parseInt(parts[0]);
        String address = parts[1];
        float price = Float.parseFloat(parts[2]);
        boolean isSold = Boolean.parseBoolean(parts[3]);
        String type = parts[4];
        String category = parts[5];
        Property prop = new Property(id, address, price, type, category);
        prop.isSold = isSold;
        return prop;
    }
}

class PropertyList {
    private Property head;
    private Property tail;
    private Map<Integer, Property> propertyMap;
    private TreeMap<Float, Property> priceMap; 
    private TreeMap<String, Property> addressMap; 
    private static final String FILE_NAME = "properties.csv";
    private Scanner sc;

    public PropertyList(Scanner sc) {
        this.head = null;
        this.tail = null;
        this.propertyMap = new HashMap<>();
        this.priceMap = new TreeMap<>();
        this.addressMap = new TreeMap<>();
        this.sc = sc;
        loadPropertiesFromFile();
    }

    public int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("\033[31mError: Please enter a valid integer value.\033[0m");
            }
        }
    }

    public float getFloatInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                return Float.parseFloat(input);
            } catch (NumberFormatException e) {
                System.out.println("\033[31mError: Please enter a valid float value.\033[0m");
            }
        }
    }

    public String getStringInput(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    public void addNewProperty() {
        System.out.println("\n\033[34mAdd New Property\033[0m");
        int id = getIntInput("Enter property ID: ");
        String address = getStringInput("Enter property address: ");
        float price = getFloatInput("Enter property price: ");
        String type = getStringInput("Enter property type (e.g., apartment, house): ");
        String category = getStringInput("Enter property category (e.g., residential, commercial): ");

        if (propertyMap.containsKey(id)) {
            System.out.println("\033[31mError: Property with this ID already exists.\033[0m");
            return;
        }

        Property newProp = new Property(id, address, price, type, category);

        // Insert into doubly linked list
        if (head == null) {
            head = newProp;
            tail = newProp;
        } else {
            tail.next = newProp;
            newProp.prev = tail;
            tail = newProp;
        }

        // Insert into hash map and sorted maps
        propertyMap.put(id, newProp);
        priceMap.put(price, newProp);
        addressMap.put(address, newProp);

        savePropertiesToFile();
        System.out.println("\033[32mProperty added successfully.\033[0m");
    }

    public void displayProperties() {
        System.out.println("\n\033[34mDisplay Available Properties\033[0m");
        if (head == null) {
            System.out.println("\033[33mNo properties available.\033[0m");
            return;
        }
        Property current = head;
        while (current != null) {
            System.out.println(current);
            current = current.next;
        }
    }

    public void searchProperty() {
        System.out.println("\n\033[34mSearch for a Property\033[0m");
        int id = getIntInput("Enter property ID to search: ");
        Property prop = propertyMap.get(id);
        if (prop != null) {
            System.out.println(prop);
        } else {
            System.out.println("\033[31mError: Property not found.\033[0m");
        }
    }

    public void buyOrSellProperty() {
        System.out.println("\n\033[34mBuy/Sell a Property\033[0m");
        int id = getIntInput("Enter property ID to buy/sell: ");
        String action = getStringInput("Enter action (Buy/Sell): ");
        Property prop = propertyMap.get(id);

        if (prop == null) {
            System.out.println("\033[31mError: Property not found.\033[0m");
            return;
        }

        if (action.equalsIgnoreCase("Buy")) {
            if (prop.isSold) {
                System.out.println("\033[31mError: This property has already been sold.\033[0m");
            } else {
                System.out.println("\033[32mSuccess: You have bought the property.\033[0m");
                prop.price *= 1.05;
                prop.isSold = true;
                // Remove and update in sorted maps
                priceMap.remove(prop.price);
                priceMap.put(prop.price, prop);
                savePropertiesToFile();
            }
        } else if (action.equalsIgnoreCase("Sell")) {
            if (!prop.isSold) {
                System.out.println("\033[31mError: This property is already available for sale.\033[0m");
            } else {
                System.out.println("\033[32mSuccess: You have sold the property.\033[0m");
                prop.price *= 0.95;
                prop.isSold = false;
                // Remove and update in sorted maps
                priceMap.remove(prop.price);
                priceMap.put(prop.price, prop);
                savePropertiesToFile();
            }
        } else {
            System.out.println("\033[31mError: Invalid action. Please enter either 'Buy' or 'Sell'.\033[0m");
        }
    }

    public void editOrDeleteProperty() {
        System.out.println("\n\033[34mEdit/Delete a Property\033[0m");
        int id = getIntInput("Enter property ID to edit/delete: ");
        String action = getStringInput("Enter action (Edit/Delete): ");
        Property prop = propertyMap.get(id);

        if (prop == null) {
            System.out.println("\033[31mError: Property not found.\033[0m");
            return;
        }

        if (action.equalsIgnoreCase("Edit")) {
            String newAddress = getStringInput("Enter new property address: ");
            float newPrice = getFloatInput("Enter new property price: ");
            String newType = getStringInput("Enter new property type: ");
            String newCategory = getStringInput("Enter new property category: ");

            // Update the property details
            addressMap.remove(prop.address);
            priceMap.remove(prop.price);

            prop.address = newAddress;
            prop.price = newPrice;
            prop.type = newType;
            prop.category = newCategory;

            addressMap.put(newAddress, prop);
            priceMap.put(newPrice, prop);

            System.out.println("\033[32mProperty updated successfully.\033[0m");
            savePropertiesToFile();
        } else if (action.equalsIgnoreCase("Delete")) {
            deleteProperty(prop);
            System.out.println("\033[32mProperty deleted successfully.\033[0m");
            savePropertiesToFile();
        } else {
            System.out.println("\033[31mError: Invalid action. Please enter either 'Edit' or 'Delete'.\033[0m");
        }
    }

    public void removeDuplicates() {
        System.out.println("\n\033[34mRemove Duplicate Properties\033[0m");
        Set<Integer> seenIds = new HashSet<>();
        Set<String> seenAddresses = new HashSet<>();
        List<Property> toRemove = new ArrayList<>();

        Property current = head;

        // Traverse the list and detect duplicates
        while (current != null) {
            if (seenIds.contains(current.id)) {
                toRemove.add(current);
            } else {
                seenIds.add(current.id);
                String addressKey = current.address + "|" + current.price;
                if (seenAddresses.contains(addressKey)) {
                    toRemove.add(current);
                } else {
                    seenAddresses.add(addressKey);
                }
            }
            current = current.next;
        }

        // Remove duplicates
        for (Property prop : toRemove) {
            deleteProperty(prop);
        }

        savePropertiesToFile();
        System.out.println("\033[32mDuplicate properties removed successfully.\033[0m");
    }

    private void deleteProperty(Property prop) {
        if (prop.prev != null) {
            prop.prev.next = prop.next;
        } else {
            head = prop.next;
        }

        if (prop.next != null) {
            prop.next.prev = prop.prev;
        } else {
            tail = prop.prev;
        }

        propertyMap.remove(prop.id);
        priceMap.remove(prop.price);
        addressMap.remove(prop.address);
    }

    private void savePropertiesToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            Property current = head;
            while (current != null) {
                bw.write(current.toCSV());
                bw.newLine();
                current = current.next;
            }
        } catch (IOException e) {
            System.out.println("\033[31mError saving properties to file.\033[0m");
        }
    }

    private void loadPropertiesFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                Property prop = Property.fromCSV(line);
                addProperty(prop);
            }
        } catch (IOException e) {
            System.out.println("\033[33mWarning: Unable to load properties from file.\033[0m");
        }
    }

    private void addProperty(Property prop) {
        if (head == null) {
            head = prop;
            tail = prop;
        } else {
            tail.next = prop;
            prop.prev = tail;
            tail = prop;
        }

        propertyMap.put(prop.id, prop);
        priceMap.put(prop.price, prop);
        addressMap.put(prop.address, prop);
    }

    public void displayByPrice() {
        System.out.println("\n\033[34mDisplay Properties Sorted by Price\033[0m");
        for (Property prop : priceMap.values()) {
            System.out.println(prop);
        }
    }

    public void displayByAddress() {
        System.out.println("\n\033[34mDisplay Properties Sorted by Address\033[0m");
        for (Property prop : addressMap.values()) {
            System.out.println(prop);
        }
    }
}
public class index {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        PropertyList propertyList = new PropertyList(sc);
        String choice;

        do {
            System.out.println("\n\033[36mReal-estate Management System\033[0m");
            System.out.println("1. Add New Property");
            System.out.println("2. Display Properties");
            System.out.println("3. Search Property");
            System.out.println("4. Buy/Sell Property");
            System.out.println("5. Edit/Delete Property");
            System.out.println("6. Remove Duplicate Properties");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextLine();

            switch (choice) {
                case "1":
                    propertyList.addNewProperty();
                    break;
                case "2":
                    propertyList.displayProperties();
                    break;
                case "3":
                    propertyList.searchProperty();
                    break;
                case "4":
                    propertyList.buyOrSellProperty();
                    break;
                case "5":
                    propertyList.editOrDeleteProperty();
                    break;
                case "6":
                    propertyList.removeDuplicates();
                    break;
                case "7":
                    System.out.println("\033[32mExiting...\033[0m");
                    break;
                default:
                    System.out.println("\033[31mError: Invalid choice. Please select a valid option.\033[0m");
                    break;
            }
        } while (!choice.equals("7"));

        sc.close();
    }
}
