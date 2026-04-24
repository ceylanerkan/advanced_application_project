import pandas as pd
import numpy as np
import glob
import os
from sqlalchemy import create_engine, text
from datetime import datetime

# Dosya yollarını sağlamlaştırmak için betiğin bulunduğu klasörü alıyoruz
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# ==========================================
# 1. VERİTABANI BAĞLANTISI VE TEMİZLİK
# ==========================================

db_url = 'mysql+pymysql://root:admin@localhost:3306/advanced_project'
engine = create_engine(db_url)

print("🧹 Eski veriler ve tablolar temizleniyor...")
with engine.begin() as conn:
    conn.execute(text("SET FOREIGN_KEY_CHECKS = 0;"))
    # PDF'teki tam listeye göre tabloları boşaltıyoruz
    tablolar = ['reviews', 'shipments', 'order_items', 'orders', 'products', 'categories', 'customer_profiles', 'stores', 'users']
    for tablo in tablolar:
        # Eğer tablo varsa içini boşalt, yoksa hata verme (try-except mantığı veritabanı tarafında)
        conn.execute(text(f"DROP TABLE IF EXISTS {tablo};")) 
    conn.execute(text("SET FOREIGN_KEY_CHECKS = 1;"))
print("✨ Veritabanı sıfırlandı!")

# 🛠️ Alper arka planda JPA ile bu tabloları oluşturacak, ancak biz ETL scriptimizle 
# DataFrame'leri doğrudan MySQL'e yazarken SQLAlchemy'nin tablo oluşturma özelliğini kullanacağız.
# Böylece sen ondan bağımsız olarak veritabanını test edebilirsin.

# ==========================================
# 2. HAM VERİLERİ OKUMA VE HOMOJENİZASYON
# ==========================================
print("📂 Amazon TSV dosyaları (Reviews/Products) taranıyor...")
tsv_pattern = os.path.join(BASE_DIR, "combined_amazon_reviews*.tsv")
tsv_dosyalari = glob.glob(tsv_pattern)
df_listesi = []

for dosya in tsv_dosyalari:
    df_temp = pd.read_csv(dosya, sep='\t', on_bad_lines='skip', nrows=40000)
    df_listesi.append(df_temp)

df_amazon = pd.concat(df_listesi, ignore_index=True)
df_amazon = df_amazon.sample(n=20000, random_state=42).reset_index(drop=True)

print("📂 Diğer CSV'ler (Customer, Shipping, Orders) okunuyor...")
df_customer_behavior = pd.read_csv(os.path.join(BASE_DIR, "Customer Behavior 2.csv"), on_bad_lines='skip')

df_customer_behavior.columns = df_customer_behavior.columns.str.strip().str.replace(' ', '')

df_shipping = pd.read_csv(os.path.join(BASE_DIR, "E-Commerce Shipping 3.csv"), on_bad_lines='skip')
df_pakistan = pd.read_csv(os.path.join(BASE_DIR, "pakistan_ecommerce_ds5.csv"), on_bad_lines='skip', low_memory=False)

# ==========================================
# 3. ETL & NORMALIZATION (PDF GEREKSİNİMLERİ)
# ==========================================
ISO_DATE = datetime.now().isoformat() + "Z" # ISO 8601 Formatı (PDF Kuralı)

# --- 1. USERS (Kullanıcılar) ---
print("⚙️ 1. USERS tablosu hazırlanıyor...")
df_users = df_customer_behavior[['CustomerID', 'Gender']].copy().drop_duplicates(subset=['CustomerID']).reset_index(drop=True)
df_users.index = df_users.index + 1
df_users['id'] = df_users.index
df_users['email'] = 'user' + df_users['id'].astype(str) + '@example.com'
df_users['password_hash'] = 'hashed_password_123'
df_users['role_type'] = 'INDIVIDUAL' # Varsayılan olarak herkes bireysel
df_users.rename(columns={'Gender': 'gender'}, inplace=True)
df_users = df_users[['id', 'email', 'password_hash', 'role_type', 'gender']]
df_users.to_sql('users', con=engine, if_exists='append', index=False)

# --- 2. STORES (Mağazalar - YENİ EKLENDİ) ---
print("⚙️ 2. STORES tablosu hazırlanıyor...")
store_data = {
    'id': [1, 2, 3],
    'owner_id': [1, 2, 3], # İlk 3 kullanıcıyı mağaza sahibi (Corporate) yapalım
    'name': ['Tech Haven', 'Fashion Boutique', 'Home Essentials'],
    'status': ['OPEN', 'OPEN', 'CLOSED']
}
df_stores = pd.DataFrame(store_data)
df_stores.to_sql('stores', con=engine, if_exists='append', index=False)

# --- 3. CUSTOMER_PROFILES (Müşteri Profilleri - YENİ EKLENDİ) ---
print("⚙️ 3. CUSTOMER_PROFILES tablosu hazırlanıyor...")
df_profiles = df_customer_behavior[['Age', 'City', 'MembershipType']].copy().head(len(df_users))
df_profiles.index = df_profiles.index + 1
df_profiles['id'] = df_profiles.index
df_profiles['user_id'] = df_profiles['id'] # Birebir eşleşme
df_profiles.rename(columns={'Age': 'age', 'City': 'city', 'MembershipType': 'membership_type'}, inplace=True)
df_profiles = df_profiles[['id', 'user_id', 'age', 'city', 'membership_type']]
df_profiles.to_sql('customer_profiles', con=engine, if_exists='append', index=False)

# --- 4. CATEGORIES (Kategoriler ve Hiyerarşi) ---
print("⚙️ 4. CATEGORIES tablosu hazırlanıyor...")
df_categories = df_amazon[['product_category']].drop_duplicates().dropna().reset_index(drop=True)
df_categories.index = df_categories.index + 1
df_categories['id'] = df_categories.index
df_categories.rename(columns={'product_category': 'name'}, inplace=True)

# Hiyerarşi Kuralı: Şimdilik hepsi ana kategori olsun diye parent_id'yi None (Null) yapıyoruz
df_categories['parent_id'] = None 
df_categories = df_categories[['id', 'name', 'parent_id']]
df_categories.to_sql('categories', con=engine, if_exists='append', index=False)


# --- 5. PRODUCTS (Ürünler ve Fiyatlar) ---
print("⚙️ 5. PRODUCTS tablosu hazırlanıyor...")
df_products = df_amazon[['product_id', 'product_title', 'product_category']].drop_duplicates(subset=['product_id']).dropna().reset_index(drop=True)
df_products.index = df_products.index + 1
df_products['id'] = df_products.index

df_products = df_products.merge(df_categories, left_on='product_category', right_on='name', how='left')

# 🛠️ EKSİK OLAN SATIR: Kategorilerden gelen eski 'name' sütununu siliyoruz ki çakışma olmasın
df_products.drop(columns=['name'], inplace=True)

df_products.rename(columns={'id_x': 'id', 'product_title': 'name', 'id_y': 'category_id', 'product_id': 'sku'}, inplace=True)

# Artık tek bir 'name' sütunumuz olduğu için güvenle 250 karakterden kesebiliriz
df_products['name'] = df_products['name'].astype(str).str.slice(0, 250)

# PDF Kuralı: unit_price eklenmesi
np.random.seed(42)
df_products['unit_price'] = np.random.uniform(10.0, 500.0, size=len(df_products)).round(2)
# Her ürünü rastgele bir mağazaya bağlıyoruz
df_products['store_id'] = np.random.choice(df_stores['id'], size=len(df_products))

# CURRENCY HANDLING (PDF Kuralı)
df_products['base_currency'] = 'USD'
df_products['original_currency'] = 'USD'
df_products['exchange_rate'] = 1.0

df_products = df_products[['id', 'store_id', 'category_id', 'sku', 'name', 'unit_price', 'base_currency', 'original_currency', 'exchange_rate']]
df_products.to_sql('products', con=engine, if_exists='append', index=False)

# --- 6. ORDERS (Siparişler ve Currency Handling) ---
print("⚙️ 6. ORDERS tablosu hazırlanıyor...")
df_pakistan = df_pakistan.dropna(subset=['status', 'grand_total']).reset_index(drop=True)
df_orders = df_pakistan[['status', 'grand_total', 'created_at']].copy().head(10000)
df_orders.index = df_orders.index + 1
df_orders['id'] = df_orders.index
df_orders['user_id'] = np.random.choice(df_users['id'], size=len(df_orders))
df_orders['store_id'] = np.random.choice(df_stores['id'], size=len(df_orders))

# CURRENCY HANDLING (PDF Kuralı 2.4)
sabit_kur = 278.5
df_orders['grand_total'] = round(df_orders['grand_total'] / sabit_kur, 2)
df_orders['base_currency'] = 'USD'
df_orders['original_currency'] = 'PKR'
df_orders['exchange_rate'] = sabit_kur

# Date Normalization (ISO 8601)
df_orders['created_at'] = pd.to_datetime(df_orders['created_at'], format='mixed', errors='coerce').fillna(pd.Timestamp.now())
df_orders['created_at'] = df_orders['created_at'].dt.strftime('%Y-%m-%dT%H:%M:%SZ')

df_orders = df_orders[['id', 'user_id', 'store_id', 'status', 'grand_total', 'base_currency', 'original_currency', 'exchange_rate', 'created_at']]
df_orders.to_sql('orders', con=engine, if_exists='append', index=False)

# --- 7. ORDER_ITEMS (Sipariş Detayları - YENİ EKLENDİ) ---
print("⚙️ 7. ORDER_ITEMS tablosu hazırlanıyor...")
# Her siparişe rastgele 1 ile 3 arası ürün ekleyelim
order_item_list = []
item_id = 1
for order_id in df_orders['id']:
    num_items = np.random.randint(1, 4)
    for _ in range(num_items):
        product_id = np.random.choice(df_products['id'])
        qty = np.random.randint(1, 5)
        # Fiyatı ürünler tablosundan bul (Gerçek dünyadaki gibi)
        price = df_products.loc[df_products['id'] == product_id, 'unit_price'].values[0]
        order_item_list.append({
            'id': item_id, 
            'order_id': order_id, 
            'product_id': product_id, 
            'quantity': qty, 
            'price': price,
            'base_currency': 'USD',
            'original_currency': 'USD',
            'exchange_rate': 1.0
        })
        item_id += 1

df_order_items = pd.DataFrame(order_item_list)
df_order_items.to_sql('order_items', con=engine, if_exists='append', index=False)

# --- 8. SHIPMENTS (Kargolar) ---
print("⚙️ 8. SHIPMENTS tablosu hazırlanıyor...")
df_shipments = df_shipping[['Warehouse_block', 'Mode_of_Shipment']].copy().head(len(df_orders))
df_shipments.index = df_shipments.index + 1
df_shipments['id'] = df_shipments.index
df_shipments['status'] = np.random.choice(['Delivered', 'In Transit', 'Pending', 'Returned'], len(df_shipments))
df_shipments['order_id'] = df_orders['id'] # Birebir eşleşme
df_shipments.rename(columns={'Warehouse_block': 'warehouse', 'Mode_of_Shipment': 'mode'}, inplace=True)
df_shipments = df_shipments[['id', 'order_id', 'warehouse', 'mode', 'status']]
df_shipments.to_sql('shipments', con=engine, if_exists='append', index=False)

# --- 9. REVIEWS (Yorumlar) ---
print("⚙️ 9. REVIEWS tablosu hazırlanıyor...")
df_reviews = df_amazon[['product_id', 'star_rating', 'review_date']].copy().dropna(subset=['product_id', 'star_rating']).reset_index(drop=True)
df_reviews.index = df_reviews.index + 1
df_reviews['id'] = df_reviews.index
df_reviews['user_id'] = np.random.choice(df_users['id'], size=len(df_reviews))

# Ürünleri birleştiriyoruz
df_reviews = df_reviews.merge(df_products[['sku', 'id']], left_on='product_id', right_on='sku', how='inner')

# 🛠️ ÇÖZÜM BURADA: Amazon'un string olan eski 'product_id'sini siliyoruz
df_reviews.drop(columns=['product_id'], inplace=True)

# Artık yeni sayısal ID'mizin adını rahatça 'product_id' yapabiliriz
df_reviews.rename(columns={'id_x': 'id', 'id_y': 'product_id'}, inplace=True)

df_reviews['sentiment'] = np.where(df_reviews['star_rating'] >= 4, 'POSITIVE', 
                          np.where(df_reviews['star_rating'] <= 2, 'NEGATIVE', 'NEUTRAL'))

# Date Normalization (ISO 8601)
df_reviews['created_at'] = pd.to_datetime(df_reviews['review_date'], errors='coerce').fillna(pd.Timestamp.now())
df_reviews['created_at'] = df_reviews['created_at'].dt.strftime('%Y-%m-%dT%H:%M:%SZ')
df_reviews.drop(columns=['review_date'], inplace=True)

df_reviews = df_reviews[['id', 'user_id', 'product_id', 'star_rating', 'sentiment', 'created_at']]
df_reviews.to_sql('reviews', con=engine, if_exists='append', index=False)

print("\n🎉 BÜTÜN İŞLEMLER BAŞARIYLA TAMAMLANDI! PDF Mimarisi %100 kuruldu.")