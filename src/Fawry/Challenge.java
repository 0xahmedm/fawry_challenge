package Fawry;
import java.util.*;

class products{
    public final String name;
    private float price;
    private int quantity;

    products(String n, float p, int q) {
        this.name = n;
        this.price = p;
        this.quantity = q;
    }

    public boolean isExpired(){
        return false;
    }

    public boolean isShipped(){
        return false;
    }

    public String getName(){
        return name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void reduceQuantity(int amount) {
        this.quantity -= amount;
    }
}

class expiringProduct extends products {
    private boolean expired;

    public expiringProduct(String n, float p, int q, boolean expired) {
        super(n,p,q);
        this.expired = expired;
    }

    @Override
    public boolean isExpired() {
        return expired;
    }
}

interface shipped {
    float getWeight();
    String getName();
}

class shippableProduct extends products implements shipped {
    private float weight;

    public shippableProduct(String n, float p, int q, float w) {
        super (n,p,q);
        this.weight = w;
    }

    @Override
    public boolean isShipped() {
            return true;
    }

    @Override
    public float getWeight() {
        return weight;
    }
}

class expirableShippableProduct extends expiringProduct implements shipped {
    private float weight;

    public expirableShippableProduct(String n, float p, int q, boolean expired, float w) {
        super(n,p,q,expired);
        this.weight = w;
    }

    @Override
    public boolean isShipped() {
        return true;
    }

    @Override
    public float getWeight() {
        return weight;
    }
}

class customer {
    private String name;
    private float balance;

    public customer(String n, float b){
        this.name = n;
        this.balance = b;
    }

    public boolean canPay(float amount){
        return balance >= amount;
    }

    public void pay(float amount) {
        balance -= amount;
    }

    public float getBalance(){
        return balance;
    }

    public String getName(){
        return name;
    }
}

class cart {
    private Map<products, Integer> items = new LinkedHashMap<>();

    public void add(products product, int quantity) {
        if (quantity > product.getQuantity()) {
            System.out.println("Error: Not enough stock for " + product.getName());
            return;
        }
        items.put(product, items.getOrDefault(product, 0) + quantity);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Map<products, Integer> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }
}

// === Shipping Service ===
class ShippingService {
    public static void ship(List<shipped> items, Map<products, Integer> cartItems) {
        System.out.println("** Shipment notice **");
        float totalWeight = 0;
        for (shipped item : items) {
            int qty = cartItems.get((products) item);
            float weight = item.getWeight() * qty;
            System.out.println(qty + "x " + item.getName() + "\t" + (float) (weight * 1000) + "g");
            totalWeight += weight;
        }
        System.out.printf("Total package weight %.3fkg\n\n", totalWeight);
    }
}

// === Checkout ===
class Checkout {
    public static void process(customer customer, cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Error: Cart is empty.");
            return;
        }

        float subtotal = 0;
        float shippingFee = 0;
        List<shipped> shippables = new ArrayList<>();

        for (Map.Entry<products, Integer> entry : cart.getItems().entrySet()) {
            products p = entry.getKey();
            int qty = entry.getValue();

            if (p.isExpired()) {
                System.out.println("Error: Product " + p.getName() + " is expired.");
                return;
            }

            if (qty > p.getQuantity()) {
                System.out.println("Error: Not enough stock for " + p.getName());
                return;
            }

            subtotal += qty * p.getPrice();
            if (p.isShipped()) {
                shippables.add((shipped) p);
                shippingFee += 15; // Example: fixed fee per shippable item
            }
        }

        float total = subtotal + shippingFee;

        if (!customer.canPay(total)) {
            System.out.println("Error: Insufficient balance.");
            return;
        }

        // Deduct balance and update stock
        customer.pay(total);
        for (Map.Entry<products, Integer> entry : cart.getItems().entrySet()) {
            entry.getKey().reduceQuantity(entry.getValue());
        }

        // Ship if needed
        if (!shippables.isEmpty()) {
            ShippingService.ship(shippables, cart.getItems());
        }

        // Print receipt
        System.out.println("** Checkout receipt **");
        for (Map.Entry<products, Integer> entry : cart.getItems().entrySet()) {
            products p = entry.getKey();
            int qty = entry.getValue();
            System.out.printf("%dx %s\t%.0f\n", qty, p.getName(), qty * p.getPrice());
        }
        System.out.println("----------------------");
        System.out.printf("Subtotal\t%.0f\n", subtotal);
        System.out.printf("Shipping\t%.0f\n", shippingFee);
        System.out.printf("Amount\t%.0f\n", total);
        System.out.printf("Remaining Balance\t%.0f\n", customer.getBalance());
        cart.clear();
    }
}

public class Challenge {
    public static void main(String[] args) {
        products cheese = new expirableShippableProduct("Cheese", 100F, 5, false, 0.2F);
        products biscuits = new expirableShippableProduct("Biscuits", 150F, 2, false, 0.35F);
        products tv = new shippableProduct("TV", 300, 3, 5);
        products scratchCard = new products("ScratchCard", 50, 10) { };

        customer customer = new customer("Ahmed", 1000);
        cart cart = new cart();

        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        Checkout.process(customer, cart);
    }
}