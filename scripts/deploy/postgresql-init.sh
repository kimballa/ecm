sudo -u postgres psql -c "CREATE DATABASE ecm"
sudo -u postgres psql -c "CREATE USER ecm WITH PASSWORD '7d9191c6e7bf4f35f20823bf83'"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE ecm TO ecm"

