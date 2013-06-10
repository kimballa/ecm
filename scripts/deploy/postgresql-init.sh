sudo -u postgres psql -c "CREATE DATABASE ecm"
sudo -u postgres psql -c "CREATE USER ecm WITH PASSWORD '7d9191c6e7bf4f35f20823bf83'"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE ecm TO ecm"

sudo -u postgres psql -c "CREATE USER ecmroot WITH PASSWORD 'ecmMasterKey'"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE ecm TO ecmroot"
sudo -u postgres psql ecm -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecmroot"

