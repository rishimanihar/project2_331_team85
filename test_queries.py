# generate_queries.py

queries = {
    "Special Query 1: Weekly Sales History": """
-- Given a specific week, how many orders were placed?
SELECT 
    EXTRACT(YEAR FROM order_time) AS year,
    EXTRACT(WEEK FROM order_time) AS week_number, 
    COUNT(order_id) AS total_orders
FROM orders
GROUP BY year, week_number
ORDER BY year, week_number;
""",
    "Special Query 2: Peak Sales Day": """
-- Given a specific day, what was the sum of the top 10 order totals?
SELECT 
    order_time::date AS sale_day, 
    SUM(total_price) AS daily_revenue
FROM orders
GROUP BY sale_day
ORDER BY daily_revenue DESC
LIMIT 10;
""",
    "Special Query 3: Realistic Sales History": """
-- Given a specific hour of the day, how many orders were placed and what was the total sum?
SELECT 
    EXTRACT(HOUR FROM order_time) AS order_hour, 
    COUNT(order_id) AS num_orders, 
    SUM(total_price) AS hourly_revenue
FROM orders
GROUP BY order_hour
ORDER BY order_hour;
""",
    "Special Query 4: Menu Item Inventory": """
-- Given a specific menu item, how many items from the inventory does that menu item use?
SELECT 
    m.item_name, 
    COUNT(mi.inventory_id) AS ingredient_count
FROM menu m
JOIN menu_ingredients mi ON m.id = mi.menu_id
GROUP BY m.item_name
ORDER BY ingredient_count DESC;
""",
    "Special Query 5: Best of the Worst": """
-- Given a specific week, what day had the lowest sales and what was the top seller that day?
WITH DailySales AS (
    SELECT 
        order_time::date AS sale_date,
        EXTRACT(WEEK FROM order_time) AS week_num,
        SUM(total_price) AS daily_total
    FROM orders
    GROUP BY sale_date, week_num
),
WorstDayPerWeek AS (
    SELECT 
        week_num, sale_date, daily_total,
        ROW_NUMBER() OVER(PARTITION BY week_num ORDER BY daily_total ASC) as rank_worst
    FROM DailySales
),
TopItemPerDay AS (
    SELECT 
        o.order_time::date AS sale_date,
        m.item_name,
        COUNT(oi.menu_id) as item_count,
        ROW_NUMBER() OVER(PARTITION BY o.order_time::date ORDER BY COUNT(oi.menu_id) DESC) as rank_best
    FROM orders o
    JOIN order_items oi ON o.order_id = oi.order_id
    JOIN menu m ON oi.menu_id = m.id
    WHERE o.order_time::date IN (SELECT sale_date FROM WorstDayPerWeek WHERE rank_worst = 1)
    GROUP BY o.order_time::date, m.item_name
)
SELECT 
    w.sale_date, w.daily_total AS lowest_sales_amount, w.week_num, t.item_name AS top_seller
FROM WorstDayPerWeek w
JOIN TopItemPerDay t ON w.sale_date = t.sale_date
WHERE w.rank_worst = 1 AND t.rank_best = 1
ORDER BY w.week_num;
"""
}

with open("special_queries.sql", "w") as f:
    f.write("-- BRAA Team 85 - Special Queries for Lab Demo\n")
    f.write("-- Tables: orders, order_items, menu, inventory, menu_ingredients\n\n")
    for title, sql in queries.items():
        f.write(f"-- {'='*40}\n")
        f.write(f"-- {title}\n")
        f.write(f"-- {'='*40}\n")
        f.write(sql.strip() + ";\n\n")

print("Created special_queries.sql successfully!")
