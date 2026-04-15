import pandas as pd
import numpy as np
import glob

print("1. Klasördeki tüm Amazon Review (TSV) dosyaları taranıyor...")
# Klasördeki tüm tsv dosyalarının isimlerini bir listeye alır
tsv_dosyalari = glob.glob("amazon_reviews_us_*.tsv")

df_listesi = []

for dosya in tsv_dosyalari:
    print(f"Okunuyor ve birleştiriliyor: {dosya}")
    # BİLGİSAYARIN ÇÖKMESİN DİYE:
    # Bu dosyalar toplamda gigabaytlarca yer kaplıyor. Bilgisayarının RAM'i şişmesin diye 
    # şimdilik her dosyadan rastgele 50.000 satır okuyoruz. 
    # Eğer bilgisayarın çok güçlüyse ', nrows=50000' kısmını silebilirsin.
    df_temp = pd.read_csv(dosya, sep='\t', on_bad_lines='skip', nrows=50000)
    df_listesi.append(df_temp)

print("\nVeriler birleştiriliyor...")
# Tüm okunan dosyaları alt alta ekleyerek tek bir devasa tablo yapıyoruz
df_raw = pd.concat(df_listesi, ignore_index=True)
print(f"Toplam {len(df_raw)} satır veri işleniyor!\n")


# --- 1. CATEGORIES (KATEGORİLER) ---
print("2. Kategoriler tablosu hazırlanıyor...")
df_categories = df_raw[['product_category']].drop_duplicates().dropna().reset_index(drop=True)
df_categories.index = df_categories.index + 1
df_categories['id'] = df_categories.index
df_categories.rename(columns={'product_category': 'name'}, inplace=True)
df_categories[['id', 'name']].to_csv("clean_categories.csv", index=False)


# --- 2. USERS (KULLANICILAR) ---
print("3. Kullanıcılar tablosu hazırlanıyor...")
df_users = df_raw[['customer_id']].drop_duplicates().dropna().reset_index(drop=True)
df_users.index = df_users.index + 1
df_users['id'] = df_users.index
df_users['email'] = 'user' + df_users['id'].astype(str) + '@example.com'
df_users['password_hash'] = 'hashed_password_123'
df_users['role_type'] = 'INDIVIDUAL'
df_users['gender'] = 'UNKNOWN'
df_users.rename(columns={'customer_id': 'customerId'}, inplace=True)
df_users[['id', 'customerId', 'email', 'password_hash', 'role_type', 'gender']].to_csv("clean_users.csv", index=False)


# --- 3. PRODUCTS (ÜRÜNLER) ---
print("4. Ürünler tablosu hazırlanıyor ve kategorilerle bağlanıyor...")
df_products = df_raw[['product_id', 'product_title', 'product_category']].drop_duplicates(subset=['product_id']).dropna().reset_index(drop=True)
df_products.index = df_products.index + 1
df_products['id'] = df_products.index

df_products = df_products.merge(df_categories, left_on='product_category', right_on='name', how='left')
df_products.rename(columns={'id_x': 'id', 'product_id': 'productId', 'product_title': 'name', 'id_y': 'category_id'}, inplace=True)
df_products[['id', 'productId', 'name', 'category_id']].to_csv("clean_products.csv", index=False)


# --- 4. REVIEWS (YORUMLAR) ---
print("5. Yorumlar tablosu hazırlanıyor (İlişkiler kuruluyor)...")
df_reviews = df_raw[['review_id', 'customer_id', 'product_id', 'star_rating', 'helpful_votes', 'total_votes']].dropna().reset_index(drop=True)
df_reviews.index = df_reviews.index + 1
df_reviews['id'] = df_reviews.index

df_reviews = df_reviews.merge(df_users[['customerId', 'id']], left_on='customer_id', right_on='customerId', how='left')
df_reviews.rename(columns={'id_x': 'id', 'id_y': 'user_id'}, inplace=True)

df_reviews = df_reviews.merge(df_products[['productId', 'id']], left_on='product_id', right_on='productId', how='left')
df_reviews.rename(columns={'id_x': 'id', 'id_y': 'product_id'}, inplace=True)

df_reviews.rename(columns={'review_id': 'reviewId', 'star_rating': 'starRating', 'helpful_votes': 'helpfulVotes', 'total_votes': 'totalVotes'}, inplace=True)

# Duygu Analizi (Sentiment)
df_reviews['sentiment'] = np.where(df_reviews['starRating'] >= 4, 'POSITIVE', 
                          np.where(df_reviews['starRating'] <= 2, 'NEGATIVE', 'NEUTRAL'))

df_reviews[['id', 'reviewId', 'starRating', 'helpfulVotes', 'totalVotes', 'sentiment', 'user_id', 'product_id']].to_csv("clean_reviews.csv", index=False)

# ... (Önceki okuma ve merge kodları aynı kalıyor) ...

# Kaydederken sadece ilk 10.000 satırı alıyoruz:
df_categories.head(10000).to_csv("clean_categories.csv", index=False)
df_users.head(10000).to_csv("clean_users.csv", index=False)
df_products.head(10000).to_csv("clean_products.csv", index=False)

df_reviews.head(10000).to_csv("clean_reviews.csv", index=False)

print("İşlem bitti! Artık dosyaların her biri çok küçük ve hızlıca yüklenebilir.")

print("\n🎉 İŞLEM TAMAMLANDI! Tüm TSV dosyaları başarıyla birleştirildi ve 4 CSV olarak dışa aktarıldı.")

from sqlalchemy import create_engine

# MySQL bağlantı bilgilerini buraya gir
# format: mysql+pymysql://kullanici:sifre@localhost:3306/veritabani_adi
engine = create_engine('mysql+pymysql://root:Safak321*@localhost:3306/ecommerce_db')

print("🚀 Veritabanına doğrudan yazma işlemi başlatıldı...")

# Sıralamaya dikkat ederek tabloları gönderiyoruz
# 'if_exists=append' -> Üzerine ekle (Truncate yaptıysan tertemiz ekler)
df_categories.head(10000).to_sql('categories', con=engine, if_exists='append', index=False)
print("✅ Categories yüklendi.")

df_users.head(10000).to_sql('users', con=engine, if_exists='append', index=False)
print("✅ Users yüklendi.")

df_products.head(10000).to_sql('products', con=engine, if_exists='append', index=False)
print("✅ Products yüklendi.")


df_reviews.head(10000).to_sql('reviews', con=engine, if_exists='append', index=False)
print("✅ Reviews yüklendi.")

print("\n🎉 Tüm veriler başarıyla ve çok hızlı bir şekilde aktarıldı!")