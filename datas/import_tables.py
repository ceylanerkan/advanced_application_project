import pandas as pd
import numpy as np
import glob
from sqlalchemy import create_engine, text

# ==========================================
# 1. VERİTABANI BAĞLANTISI VE TEMİZLİK
# ==========================================
db_url = 'mysql+pymysql://root:Safak321*@localhost:3306/ecommerce_db'
engine = create_engine(db_url)

print("🧹 Eski veriler temizleniyor...")
with engine.begin() as conn:
    conn.execute(text("SET FOREIGN_KEY_CHECKS = 0;"))
    conn.execute(text("TRUNCATE TABLE reviews;"))
    conn.execute(text("TRUNCATE TABLE shipments;"))
    conn.execute(text("TRUNCATE TABLE orders;"))
    conn.execute(text("TRUNCATE TABLE products;"))
    conn.execute(text("TRUNCATE TABLE users;"))
    conn.execute(text("TRUNCATE TABLE categories;"))
    conn.execute(text("SET FOREIGN_KEY_CHECKS = 1;"))
print("✨ Veritabanı sıfırlandı!")

# ==========================================
# 2. AMAZON TSV DOSYALARINI OKUMA
# ==========================================
print("📂 Amazon TSV dosyaları taranıyor...")
tsv_dosyalari = glob.glob("amazon_reviews_us_*.tsv")
df_listesi = []

for dosya in tsv_dosyalari:
    print(f"Okunuyor: {dosya}")
    df_temp = pd.read_csv(dosya, sep='\t', on_bad_lines='skip', nrows=40000)
    df_listesi.append(df_temp)

df_raw = pd.concat(df_listesi, ignore_index=True)

# 🌟 İŞTE SİHRİN GERÇEKLEŞTİĞİ YER (HOMOJENİZASYON) 🌟
print("🎲 Veriler karıştırılıyor ve her kategoriden rastgele örneklemler alınıyor...")
# Toplam verinin içinden rastgele 20.000 satır çekiyoruz. Artık kıyafet, elektronik her şey karışık!
df_raw = df_raw.sample(n=20000, random_state=42).reset_index(drop=True)


# ==========================================
# 3. VERİLERİ DÖNÜŞTÜRME VE YÜKLEME
# ==========================================

# --- A) KATEGORİLER (Categories) ---
print("⚙️ Kategoriler aktarılıyor...")
df_categories = df_raw[['product_category']].drop_duplicates().dropna().reset_index(drop=True)
df_categories.index = df_categories.index + 1
df_categories['id'] = df_categories.index
df_categories.rename(columns={'product_category': 'name'}, inplace=True)

df_categories[['id', 'name']].to_sql('categories', con=engine, if_exists='append', index=False)


# --- B) KULLANICILAR (Users) ---
print("⚙️ Kullanıcılar aktarılıyor...")
df_users = df_raw[['customer_id']].drop_duplicates().dropna().reset_index(drop=True)
df_users.index = df_users.index + 1
df_users['id'] = df_users.index
df_users['email'] = 'user' + df_users['id'].astype(str) + '@example.com'
df_users['password_hash'] = 'hashed_password_123'
df_users['role_type'] = 'INDIVIDUAL'
df_users['gender'] = 'UNKNOWN'

df_users = df_users[['id', 'customer_id', 'email', 'password_hash', 'role_type', 'gender']]
df_users.to_sql('users', con=engine, if_exists='append', index=False)


# --- C) ÜRÜNLER (Products) ---
print("⚙️ Ürünler aktarılıyor...")
df_products = df_raw[['product_id', 'product_title', 'product_category']].drop_duplicates(subset=['product_id']).dropna().reset_index(drop=True)
df_products.index = df_products.index + 1
df_products['id'] = df_products.index

df_products = df_products.merge(df_categories, left_on='product_category', right_on='name', how='left')
df_products.drop(columns=['name'], inplace=True)
df_products.rename(columns={'id_x': 'id', 'product_title': 'name', 'id_y': 'category_id'}, inplace=True)

# 🛠️ ÇÖZÜM BURADA: Ürün isimlerini SQL'in 255 karakter sınırına takılmaması için 250 karakterden kesiyoruz
df_products['name'] = df_products['name'].astype(str).str.slice(0, 250)

df_products = df_products[['id', 'product_id', 'name', 'category_id']]
df_products.to_sql('products', con=engine, if_exists='append', index=False)

# --- D) SİPARİŞLER (Orders) ---
print("⚙️ Siparişler aktarılıyor...")
df_pakistan = pd.read_csv("pakistan_ecommerce_ds5.csv", on_bad_lines='skip', low_memory=False)
df_pakistan = df_pakistan.dropna(subset=['status', 'grand_total']).reset_index(drop=True)

df_orders = df_pakistan[['status', 'grand_total']].copy().head(15000)
df_orders.index = df_orders.index + 1
df_orders['id'] = df_orders.index
np.random.seed(42)
# Hata almamak için sahte siparişleri oluşturduğumuz GERÇEK User ID'lerine atıyoruz
df_orders['user_id'] = np.random.choice(df_users['id'], size=len(df_orders))
df_orders['store_id'] = np.random.randint(1, 50, size=len(df_orders))

df_orders = df_orders[['id', 'status', 'grand_total', 'user_id', 'store_id']]
df_orders.to_sql('orders', con=engine, if_exists='append', index=False)


# --- E) KARGOLAR (Shipments) ---
print("⚙️ Kargolar aktarılıyor...")
df_shipping = pd.read_csv("E-Commerce Shipping 3.csv", on_bad_lines='skip')
df_shipments = df_shipping[['Warehouse_block', 'Mode_of_Shipment']].copy().head(15000)
df_shipments.index = df_shipments.index + 1
df_shipments['id'] = df_shipments.index
df_shipments['status'] = np.random.choice(['Delivered', 'In Transit', 'Pending', 'Returned'], len(df_shipments))
# Sahte kargoları oluşturduğumuz GERÇEK Sipariş ID'lerine atıyoruz
df_shipments['order_id'] = np.random.choice(df_orders['id'], size=len(df_shipments))

df_shipments.rename(columns={'Warehouse_block': 'warehouse', 'Mode_of_Shipment': 'mode'}, inplace=True)
df_shipments = df_shipments[['id', 'warehouse', 'mode', 'status', 'order_id']]
df_shipments.to_sql('shipments', con=engine, if_exists='append', index=False)


# --- F) YORUMLAR (Reviews) ---
print("⚙️ Yorumlar aktarılıyor...")
df_reviews = df_raw[['review_id', 'customer_id', 'product_id', 'star_rating', 'helpful_votes', 'total_votes']].dropna().reset_index(drop=True)
df_reviews.index = df_reviews.index + 1
df_reviews['id'] = df_reviews.index

df_reviews = df_reviews.merge(df_users[['customer_id', 'id']], on='customer_id', how='inner')
df_reviews.rename(columns={'id_x': 'id', 'id_y': 'user_id'}, inplace=True)
df_reviews.drop(columns=['customer_id'], inplace=True)

df_reviews = df_reviews.merge(df_products[['product_id', 'id']], on='product_id', how='inner')
df_reviews.drop(columns=['product_id'], inplace=True)
df_reviews.rename(columns={'id_x': 'id', 'id_y': 'product_id'}, inplace=True)

df_reviews['sentiment'] = np.where(df_reviews['star_rating'] >= 4, 'POSITIVE', 
                          np.where(df_reviews['star_rating'] <= 2, 'NEGATIVE', 'NEUTRAL'))

df_reviews = df_reviews[['id', 'review_id', 'star_rating', 'helpful_votes', 'total_votes', 'sentiment', 'user_id', 'product_id']]
df_reviews.to_sql('reviews', con=engine, if_exists='append', index=False)

print("\n🎉 BÜTÜN İŞLEMLER BAŞARIYLA TAMAMLANDI! MySQL'i kontrol edebilirsin.")