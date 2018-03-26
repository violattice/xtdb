DING - Data Ingestion for Graphs

An experimental mutable Clojure graph database currently sitting on top of RocksDB.

Motivation: a database with the following characteristics:

+ Graph Query
+ Additive Schema
+ Bitemporality
+ Data retention/excision strategies (for GDPR)
+ Global locking
+ Rapid data ingestion (using Kafka)
+ Scalable Query Engine using RocksDB

## References

+ https://www.datomic.com
+ https://github.com/fyrz/rocksdb-counter-microservice-sample
+ https://clojure.github.io/clojure-contrib/doc/datalog.html
+ https://github.com/tonsky/datascript
+ https://en.wikipedia.org/wiki/Jena_(framework)
+ https://arxiv.org/abs/1402.2237
