(ns sql-migrate.core
  (:import (java.io BufferedReader FileReader))
  (:use clj-extensions.core)
  (:require [clojure.contrib.sql :as sql])
  (:use [clojure.contrib.string :only [blank? trim]]))

(defn new-migration [name script]
  [name script])

(defn migration-name [migration]
  (get migration 0))

(defn migration-script [migration]
  (get migration 1))

(defn begin-migration? [line]
  (= \- (first line) (second line)))

(defn parse-migrations [lines migrations]
  (cond 
    (empty? lines) migrations
    (blank? (first lines)) (recur (rest lines) migrations)
    (begin-migration? (first lines))
    (let [name (trim (subs (first lines) 2))
          script (apply str (map #(str % "\n") (take-until begin-migration? (rest lines))))]
      (recur (drop-until begin-migration? (rest lines)) 
             (conj migrations (new-migration name script))))
    :else (throw (Exception. "invalid migration file"))))

(defn read-migrations [file]
  (with-open [reader (BufferedReader. (FileReader. file))]
    (let [lines (line-seq reader)]
      (parse-migrations lines []))))

(defn migrate [db-spec migration-file]
  (sql/with-connection db-spec
    (sql/with-query-results migrations ["SELECT name FROM migrations"]
      (letfn [(new-migration? [migration] 
                              (not (some #{(migration-name migration)} (map :name migrations))))]
        (doseq [migration (filter new-migration? (read-migrations migration-file))]
          (with-open [stmt (.prepareStatement (sql/connection) (migration-script migration))]
            (.execute stmt))
          (sql/insert-records :migrations {:name (migration-name migration)}))))))
