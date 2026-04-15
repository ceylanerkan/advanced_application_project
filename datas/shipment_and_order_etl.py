import pandas as pd
import numpy as np

# 1. DOSYALARI OKUMA (Klasöründeki isimlerle birebir aynı olmalı)
print("1. CSV Dosyaları okunuyor...")
df_pakistan = pd.read_csv("pakistan_ecommerce_ds5.csv", on_bad_lines='skip', low_memory=False)
df_shipping = pd.read_csv("E-Commerce Shipping 3.csv", on_bad_lines='skip')

# Pakistan dataseti çok büyük ve dağınık olabilir, boş olan satırları silelim
df_pakistan = df_pakistan.dropna(subset=['status', 'grand_total']).reset_index(drop=True)


# --- 1. ORDERS (SİPARİŞLER) TABLOSU ---
print("2. Siparişler (Orders) tablosu hazırlanıyor...")
# Bize sadece status ve grand_total lazım
df_orders = df_pakistan[['status', 'grand_total']].copy()
df_orders.index = df_orders.index + 1
df_orders['id'] = df_orders.index

# Kullanıcı ID'lerini bağlama: Veritabanımız boş kalmasın diye, 
# 1 ile 50000 arasındaki kullanıcıları (önceki adımda oluşturduğumuz clean_users'daki id'leri)
# rastgele bu siparişlere atıyoruz. (Gerçek dünyada bu veriler eşleşir, ama Kaggle verileri farklı kaynaklardan olduğu için biz simüle ediyoruz)
np.random.seed(42) # Her çalıştırmada aynı rastgele sayıları versin diye
df_orders['user_id'] = np.random.randint(1, 10000, size=len(df_orders))
df_orders['storeId'] = np.random.randint(1, 50, size=len(df_orders)) # Sahte mağaza ID'leri

# Sütun isimlerini Java'ya uygun hale getirme
df_orders.rename(columns={'grand_total': 'grandTotal'}, inplace=True)
df_orders = df_orders[['id', 'status', 'grandTotal', 'user_id', 'storeId']]

# Siparişleri dışa aktarma (Hafızayı yormamak için ilk 20.000 satırı alalım)
df_orders = df_orders.head(20000)
df_orders.to_csv("clean_orders.csv", index=False)


# --- 2. SHIPMENTS (KARGOLAR) TABLOSU ---
print("3. Kargolar (Shipments) tablosu hazırlanıyor...")
# Sütun isimleri Shipping datasetine göre çekiliyor
df_shipments = df_shipping[['Warehouse_block', 'Mode_of_Shipment']].copy()
df_shipments.index = df_shipments.index + 1
df_shipments['id'] = df_shipments.index

# Sahte statü (Kargo Durumu) ekleyelim
statu_listesi = ['Delivered', 'In Transit', 'Pending', 'Returned']
df_shipments['status'] = np.random.choice(statu_listesi, len(df_shipments))

# KARGO - SİPARİŞ İLİŞKİSİ (Foreign Key)
# Her kargoya bir sipariş ID (order_id) atamamız lazım. 
# clean_orders tablosunda 20.000 sipariş var, o yüzden 1-20000 arası rastgele dağıtıyoruz.
df_shipments['order_id'] = np.random.randint(1, 20000, size=len(df_shipments))

# Sütunları Java modeline uyarlama
df_shipments.rename(columns={'Warehouse_block': 'warehouse', 'Mode_of_Shipment': 'mode'}, inplace=True)
df_shipments = df_shipments[['id', 'warehouse', 'mode', 'status', 'order_id']]

df_shipments.to_csv("clean_shipments.csv", index=False)
df_orders.head(10000).to_csv("clean_orders.csv", index=False)
df_shipments.head(10000).to_csv("clean_shipments.csv", index=False)

print("\n🎉 İŞLEM TAMAMLANDI!")
print("clean_orders.csv ve clean_shipments.csv dosyaları başarıyla oluşturuldu.")

from sqlalchemy import create_all, create_engine
engine = create_engine('mysql+pymysql://root:Safak321*@localhost:3306/ecommerce_db')

df_orders.head(10000).to_sql('orders', con=engine, if_exists='append', index=False)
print("✅ Orders yüklendi.")

df_shipments.head(10000).to_sql('shipments', con=engine, if_exists='append', index=False)
print("✅ Shipments yüklendi.")
