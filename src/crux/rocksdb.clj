(ns crux.rocksdb
  (:require [clojure.java.io :as io]
            [crux.kv-store :refer :all])
  (:import java.io.Closeable
           clojure.lang.MapEntry
           [org.rocksdb Checkpoint Options RocksDB RocksIterator WriteBatch WriteOptions]))

(defn- iterator->kv [^RocksIterator i]
  (when (.isValid i)
    (MapEntry. (.key i) (.value i))))

(defn- ^Closeable rocks-iterator [{:keys [^RocksDB db]}]
  (let [i (.newIterator db)]
    (reify
      KvIterator
      (-seek [this k]
        (.seek i k)
        (iterator->kv i))
      (-next [this]
        (.next i)
        (iterator->kv i))
      Closeable
      (close [this]
        (.close i)))))

(def ^:dynamic ^:private *current-iterator* nil)

(defrecord CruxRocksKv [db-dir]
  CruxKvStore
  (open [this]
    (RocksDB/loadLibrary)
    (let [opts (doto (Options.)
                 (.setCreateIfMissing true))
          db (try
               (RocksDB/open opts (.getAbsolutePath (doto (io/file db-dir)
                                                      (.mkdirs))))
               (catch Throwable t
                 (.close opts)
                 (throw t)))]
      (assoc this :db db :options opts :write-options (doto (WriteOptions.)
                                                        (.setDisableWAL true)))))

  (iterate-with [this f]
    (if *current-iterator*
      (f *current-iterator*)
      (with-open [i (rocks-iterator this)]
        (binding [*current-iterator* i]
          (f i)))))

  (store [{:keys [^RocksDB db ^WriteOptions write-options]} kvs]
    (with-open [wb (WriteBatch.)]
      (doseq [[k v] kvs]
        (.put wb k v))
      (.write db write-options wb)))

  (delete [{:keys [^RocksDB db ^WriteOptions write-options]} ks]
    (with-open [wb (WriteBatch.)]
      (doseq [k ks]
        (.delete wb k))
      (.write db write-options wb)))

  (backup [{:keys [^RocksDB db]} dir]
    (.createCheckpoint (Checkpoint/create db) (.getAbsolutePath (io/file dir))))

  Closeable
  (close [{:keys [^RocksDB db ^Options options ^WriteOptions write-options]}]
    (.close db)
    (.close options)
    (.close write-options)))
