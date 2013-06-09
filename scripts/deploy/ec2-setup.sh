#!/bin/bash
#
# Run as root to set up the ec2 instance.
# Base on Ubuntu 12.04 LTS (precise)
# Ubuntu Cloud Guest AMI ID ami-fe002cbb (x86\_64)

hostname ecm.gremblor.com

# Set up postgresql server.
apt-get -y install postgresql-client-common postgresql-server-dev-9.1 \
    postgresql-client-9.1 postgresql

# Get some other tools we need.
apt-get -y install git-core maven lighttpd

# Set up all user accounts. We don't need no stinkin' ActiveDirectory here...
addgroup --gid 5001 ecmweb
adduser --home /data/home/ecmweb --shell /bin/bash --uid=5001 --gid 5001 --system ecmweb

# Attach the data drive
mkdir /data
mount /dev/xvdb /data

# Install Java from package on data drive
mkdir -p /usr/java
tar vzxf /data/packages/jdk-7u21-linux-x64.tar.gz -C /usr/java
cd /usr/java
ln -s jdk1.7.0_21/ latest
ln -s jdk1.7.0_21/ default
echo 'export JAVA_HOME=/usr/java/default' > /etc/profile.d/java_home.sh
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile.d/java_home.sh
chmod +x /etc/profile.d/java_home.sh


# Symlink postgresql data into the right spot.
if [ ! -L "/var/lib/postgresql" ]; then
  rm -rf /var/lib/postgresql
  ln -s /data/var/lib/postgresql/ /var/lib/postgresql
fi

if [ ! -L "/etc/lighttpd" ]; then
  rm -rf /etc/lighttpd
  ln -s /data/etc/lighttpd/ /etc/lighttpd
fi

service postgresql start
service lighttpd start

