
# Gremblor Heavy Industries
# Electronic Currency Market Trading Platform


## Compiling and running

    $ play run

## Packaging

    $ play dist

## Dependencies

  Bitcoin chart stuff: www.xeiam.com
  https://github.com/timmolter/XChange
  http://xeiam.com/xchange/javadoc/index.html
  MIT License


## Local database

You should create a local postgresql database and user as follows:

    CREATE DATABASE ecm;
    CREATE USER ecm WITH PASSWORD 'ecmtest';
    GRANT ALL PRIVILEGES ON DATABASE ecm TO ecm;


