-- V2__load_csv_data.sql
-- Carrega dados dos CSVs usando COPY (os arquivos devem estar acessíveis pelo container PostgreSQL)

COPY customers(customer_id, customer_unique_id, customer_zip_code_prefix, customer_city, customer_state)
FROM '/data/olist_customers_dataset.csv'
DELIMITER ',' CSV HEADER;

COPY geolocations(geolocation_zip_code_prefix, geolocation_lat, geolocation_lng, geolocation_city, geolocation_state)
FROM '/data/olist_geolocation_dataset.csv'
DELIMITER ',' CSV HEADER;

COPY sellers(seller_id, seller_zip_code_prefix, seller_city, seller_state)
FROM '/data/olist_sellers_dataset.csv'
DELIMITER ',' CSV HEADER;

COPY products(product_id, product_category_name, product_name_length, product_description_length, product_photos_qty, product_weight_g, product_length_cm, product_height_cm, product_width_cm)
FROM '/data/olist_products_dataset.csv'
DELIMITER ',' CSV HEADER NULL '';

COPY product_category_translations(product_category_name, product_category_name_english)
FROM '/data/product_category_name_translation.csv'
DELIMITER ',' CSV HEADER;

COPY orders(order_id, customer_id, order_status, order_purchase_timestamp, order_approved_at, order_delivered_carrier_date, order_delivered_customer_date, order_estimated_delivery_date)
FROM '/data/olist_orders_dataset.csv'
DELIMITER ',' CSV HEADER NULL '';

COPY order_items(order_id, order_item_id, product_id, seller_id, shipping_limit_date, price, freight_value)
FROM '/data/olist_order_items_dataset.csv'
DELIMITER ',' CSV HEADER;

COPY order_payments(order_id, payment_sequential, payment_type, payment_installments, payment_value)
FROM '/data/olist_order_payments_dataset.csv'
DELIMITER ',' CSV HEADER;

CREATE TEMP TABLE tmp_order_reviews (
    review_id VARCHAR(50),
    order_id VARCHAR(50),
    review_score INT,
    review_comment_title TEXT,
    review_comment_message TEXT,
    review_creation_date TIMESTAMP,
    review_answer_timestamp TIMESTAMP
);

COPY tmp_order_reviews(review_id, order_id, review_score, review_comment_title, review_comment_message, review_creation_date, review_answer_timestamp)
FROM '/data/olist_order_reviews_dataset.csv'
DELIMITER ',' CSV HEADER NULL '';

INSERT INTO order_reviews(review_id, order_id, review_score, review_comment_title, review_comment_message, review_creation_date, review_answer_timestamp)
SELECT DISTINCT ON (review_id) review_id, order_id, review_score, review_comment_title, review_comment_message, review_creation_date, review_answer_timestamp
FROM tmp_order_reviews
ORDER BY review_id, review_creation_date DESC;

DROP TABLE tmp_order_reviews;
