---
layout: post
title:  PostgreSQL Learning Notes
date:   2018-05-14 23:08:00 +0800
categories: study-notes
tag: PostgreSQL
---

* content
{:toc}


## Install & Initialization
```shell
# install
> brew install PostgreSQL

# init new postgres db cluster managed by a single server at target dir
> initdb -D [target dir]

# start the db server, this will create a postmaster.pid file that keeps a record for the running process id
> pg_ctl start -D [cluster dir]

# enter the db console and interact with the db. A default user will be created when you enter the console the first time. Check with > psql postgres --help
> psql postgres
```

### Init Log
```
initdb postgres
The files belonging to this database system will be owned by user "shayangzang".
This user must also own the server process.

The database cluster will be initialized with locale "en_US.UTF-8".
The default database encoding has accordingly been set to "UTF8".
The default text search configuration will be set to "english".

Data page checksums are disabled.

creating directory postgres ... ok
creating subdirectories ... ok
selecting default max_connections ... 100
selecting default shared_buffers ... 128MB
selecting dynamic shared memory implementation ... posix
creating configuration files ... ok
running bootstrap script ... ok
performing post-bootstrap initialization ... ok
syncing data to disk ... ok

WARNING: enabling "trust" authentication for local connections
You can change this by editing pg_hba.conf or using the option -A, or
--auth-local and --auth-host, the next time you run initdb.

Success. You can now start the database server using:

    pg_ctl -D postgres -l logfile start
```

## On Users 
```shell
# create user
postgres> CREATE USER username WITH SUPERUSER PASSWORD 'password';

# display users/roles
postgres> \du

# display session user and current user
postgres> SELECT SESSION_USER, CURRENT_USER;

# switch user
postgres> SET ROLE 'user';
```

## DB Operations
```shell
# list all the databases
postgres> \l

# create database
postgres> CREATE DATABASE dbname;

# connect with a database
postgres> \c [dbname]

# display relations within the db
dbname> \d

# create a relation
dbname> CREATE TABLE table (col1, col2, col3);

# display records in a table
dbname> SELECT * FROM table
```

## Notes
1. current cluster is sitting at /usr/local/var/postgres
2. google "postgres database file layout" to see what each file does under this dir
