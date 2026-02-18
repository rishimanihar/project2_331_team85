import csv
import random
import datetime
from pathlib import Path

DATA_DIR = Path("data")
DATA_DIR.mkdir(exist_ok=True)

WEEKS_HISTORY = 52
SALES_GOAL = 1_000_000
END_DATE = datetime.date.today()
START_DATE = END_DATE - datetime.timedelta(weeks=WEEKS_HISTORY)

PEAK_DATES = {
    START_DATE + datetime.timedelta(days=10),
    START_DATE + datetime.timedelta(days=60),
    START_DATE + datetime.timedelta(days=180)
}

MENU_ITEMS = [
    ("Classic Milk Tea", 5.50), ("Taro Slush", 6.25), ("Matcha Latte", 5.75),
    ("Brown Sugar Boba", 6.50), ("Mango Green Tea", 5.25), ("Thai Tea", 5.50),
    ("Passion Fruit Tea", 5.25), ("Oreo Slush", 6.75), ("Coffee Milk Tea", 5.50),
    ("Strawberry Smoothie", 6.50), ("Honeydew Milk Tea", 5.75), ("Wintermelon Tea", 5.00),
    ("Rose Milk Tea", 5.75), ("Lychee Green Tea", 5.25), ("Peach Oolong", 5.25),
    ("Coconut Milk Tea", 5.75), ("Almond Milk Tea", 5.75), ("Caramel Frappe", 6.50),
    ("Mocha Frappe", 6.50), ("Vanilla Latte", 5.50),
]

MENU_ITEMS = [(name, price * 3) for name, price in MENU_ITEMS]

INGREDIENTS = [
    ("Milk", "Gallon"), ("Sugar", "Lbs"), ("Tea Leaves", "Lbs"), ("Tapioca Pearls", "Bag"),
    ("Taro Powder", "Bag"), ("Matcha Powder", "Bag"), ("Mango Syrup", "Bottle"),
    ("Thai Tea Mix", "Bag"), ("Passion Fruit Jam", "Jar"), ("Oreos", "Box"),
    ("Coffee Beans", "Lbs"), ("Strawberries", "Lbs"), ("Honeydew Powder", "Bag"),
    ("Wintermelon Syrup", "Bottle"), ("Rose Syrup", "Bottle"), ("Lychee Jelly", "Tub"),
    ("Cups", "Box"), ("Straws", "Box"), ("Napkins", "Box"), ("Plastic Seal", "Roll")
]


def generate_inventory():
    with open(DATA_DIR / "inventory.csv", "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["id", "item_name", "quantity", "unit"])
        for i, (name, unit) in enumerate(INGREDIENTS, 1):
            writer.writerow([i, name, random.randint(100, 1000), unit])


def generate_menu():
    with open(DATA_DIR / "menu.csv", "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["id", "item_name", "price"])
        for i, (name, price) in enumerate(MENU_ITEMS, 1):
            writer.writerow([i, name, price])


def generate_menu_ingredients():
    core_items = [
        (17, 1.0), (18, 1.0), (19, 1.0), (20, 1.0), (2, 0.5)
    ]

    recipe_book = {
        1: [(1, 0.5), (3, 0.2), (4, 0.5)],   # Classic Milk Tea: Milk, Tea, Pearls
        2: [(5, 0.5), (1, 0.5)],             # Taro Slush: Taro, Milk
        3: [(6, 0.5), (1, 0.5)],             # Matcha Latte: Matcha, Milk
        4: [(4, 0.5), (1, 0.5)],             # Brown Sugar Boba: Pearls, Milk
        5: [(7, 0.5), (3, 0.2)],             # Mango Green Tea: Mango, Tea
        6: [(8, 0.5), (1, 0.5)],             # Thai Tea: Thai Mix, Milk
        7: [(9, 0.5), (3, 0.2)],             # Passion Fruit Tea: Jam, Tea
        8: [(10, 0.5), (1, 0.5)],            # Oreo Slush: Oreos, Milk
        9: [(11, 0.5), (1, 0.5), (3, 0.2)],  # Coffee Milk Tea: Coffee, Milk, Tea
        10: [(12, 0.5)],                     # Strawberry Smoothie: Strawberries
        11: [(13, 0.5), (1, 0.5)],           # Honeydew Milk Tea: Honeydew, Milk
        12: [(14, 0.5), (3, 0.2)],           # Wintermelon Tea: Syrup, Tea
        13: [(15, 0.5), (1, 0.5), (3, 0.2)], # Rose Milk Tea: Rose, Milk, Tea
        14: [(16, 0.5), (3, 0.2)],           # Lychee Green Tea: Jelly, Tea
        15: [(3, 0.2)],                      # Peach Oolong: Tea
        16: [(1, 0.5), (3, 0.2)],            # Coconut Milk Tea: Milk, Tea
        17: [(1, 0.5), (3, 0.2)],            # Almond Milk Tea: Milk, Tea
        18: [(11, 0.5), (1, 0.5)],           # Caramel Frappe: Coffee, Milk
        19: [(11, 0.5), (1, 0.5)],           # Mocha Frappe: Coffee, Milk
        20: [(11, 0.5), (1, 0.5)]            # Vanilla Latte: Coffee, Milk
    }

    with open(DATA_DIR / "menu_item_ingredients.csv", "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["menu_id", "inventory_id", "quantity_used"])
        
        for menu_id in range(1, 21): 
            
            for inv_id, qty in core_items:
                writer.writerow([menu_id, inv_id, qty])
            
            if menu_id in recipe_book:
                for inv_id, qty in recipe_book[menu_id]:
                    writer.writerow([menu_id, inv_id, qty])
            else:
                writer.writerow([menu_id, 3, 0.2])


def generate_orders():
    orders_file = open(DATA_DIR / "orders.csv", "w", newline="")
    items_file = open(DATA_DIR / "order_items.csv", "w", newline="")

    orders_writer = csv.writer(orders_file)
    items_writer = csv.writer(items_file)

    orders_writer.writerow(["id", "order_timestamp", "total_amount"])
    items_writer.writerow(["order_id", "menu_id"])

    order_id = 1
    total_days = (END_DATE - START_DATE).days
    avg_daily = SALES_GOAL / total_days
    date = START_DATE

    while date <= END_DATE:
        daily_target = avg_daily * random.uniform(0.8, 1.2)
        if date.weekday() >= 5:
            daily_target *= 1.5
        if date in PEAK_DATES:
            daily_target *= 5

        daily_sales = 0
        while daily_sales < daily_target:
            hour = random.choice(range(10, 21))
            timestamp = f"{date} {hour:02d}:{random.randint(0,59):02d}:00"
            items = random.choices(range(1, 21), k=random.choices([1,2,3],[70,20,10])[0])
            total = sum(MENU_ITEMS[i-1][1] for i in items)

            orders_writer.writerow([order_id, timestamp, round(total, 2)])
            for item in items:
                items_writer.writerow([order_id, item])

            daily_sales += total
            order_id += 1

        date += datetime.timedelta(days=1)

    orders_file.close()
    items_file.close()


if __name__ == "__main__":
    generate_inventory()
    generate_menu()
    generate_menu_ingredients()
    generate_orders()
    print("CSV files generated successfully.")
