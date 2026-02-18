/* Weekly Sales History
SELECT
   EXTRACT(WEEK FROM order_time) AS week_number,
   COUNT(order_id) AS total_orders
FROM Orders
GROUP BY week_number
ORDER BY week_number;
*/


/*Peak Sales Day
SELECT
   DATE(order_time) AS sale_day,
   SUM(total_price) AS daily_revenue
FROM Orders
GROUP BY sale_day
ORDER BY daily_revenue DESC
LIMIT 10;
*/

/*Realistic Sales History
   EXTRACT(HOUR FROM order_time) AS hour_of_day,
   COUNT(order_id) AS num_orders,
   SUM(total_price) AS hourly_revenue
FROM Orders
GROUP BY hour_of_day
ORDER BY hour_of_day;
*/

/* Menu Item Inventory
SELECT 
    m.item_name, 
    COUNT(mi.inventory_id) AS total_inventory_items
FROM Menu m
JOIN Menu_Ingredients mi ON m.id = mi.menu_id
GROUP BY m.item_name
ORDER BY total_inventory_items DESC;
/*

/* Low Inventory Warning
SELECT item_name, quantity, unit
FROM Inventory
WHERE quantity < 50
ORDER BY quantity ASC;
/*

/*  Ingredient Usage Report
SELECT i.item_name, SUM(mi.quantity_used) as total_consumed
FROM Order_Items oi
JOIN Menu_Ingredients mi ON oi.menu_id = mi.menu_id
JOIN Inventory i ON mi.inventory_id = i.id
GROUP BY i.item_name
ORDER BY total_consumed DESC;
/*

/* Peak Hour Performance
SELECT m.item_name, COUNT(*) as volume
FROM Order_Items oi
JOIN Orders o ON oi.order_id = o.order_id
JOIN Menu m ON oi.menu_id = m.id
WHERE EXTRACT(HOUR FROM o.order_time) = 12
GROUP BY m.item_name
ORDER BY volume DESC
LIMIT 1;
*/

/* Order Customization Depth
SELECT ROUND(AVG(item_count), 2) as avg_items_per_order
FROM (
    SELECT order_id, COUNT(menu_id) as item_count
    FROM Order_Items
    GROUP BY order_id
) AS counts;
*/

/* Revenue by Menu Category
SELECT 
    CASE WHEN item_name LIKE '%Milk Tea%' THEN 'Milk Tea'
         WHEN item_name LIKE '%Fruit%' THEN 'Fruit Tea'
         ELSE 'Other' END as category,
    SUM(m.price) as revenue
FROM Order_Items oi
JOIN Menu m ON oi.menu_id = m.id
GROUP BY category;
/*
