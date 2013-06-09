
# Gremblor Heavy Industries
# Electronic Currency Market Trading Platform


## Compiling and running

    $ play run

## Packaging

    $ play dist

## Deploying

* Run on ec2 (us-west N. California) under akimball83@gmail.com
* t1.micro
* Ubuntu Cloud Guest AMI ID ami-fe002cbb (x86\_64)
* login: ec2-user

See the files in `scripts/deploy/` to set up a new instance.

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


