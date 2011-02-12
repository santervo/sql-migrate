(ns sql-migrate.test.core
  (:use [sql-migrate.core] :reload)
  (:use [clojure.test])
  (:require [clojure.contrib.io :as io])
  (:use [clojure.contrib.sql])
  (:import [java.sql DriverManager SQLException]))

(def test-conn {:classname "org.hsqldb.jdbcDriver"
                :subprotocol "h2"
                :subname "mem:test"
                :user "sa"
                :password ""})

(deftest test-migrate
  (testing "without migrations table"
    (is (thrown-with-msg? SQLException (re-pattern "Table \"MIGRATIONS\" not found")
          (migrate! test-conn "test-resources/valid-migration.sql"))))
  (testing "without existing migrations"
    (with-connection test-conn
      ;setup
      (create-table :migrations [:name :varchar "PRIMARY KEY"])
      ;execute
      (migrate! test-conn "test-resources/valid-migration.sql")
      ;verify
      (with-query-results rs ["select login,passwd from user"]
        (is (and (= "admin" (:login (first rs)))
                 (is (= "secret" (:passwd (first rs)))))
            "all migrations were executed"))
      (with-query-results rs ["select name from migrations"]
        (is (= #{"create user table" "add initial user" "add passwd column"} 
               (set (map :name rs))) "migration names were saved"))))
  (testing "with existing migration"
    (with-connection test-conn
      ;setup
      (create-table :migrations [:name :varchar "PRIMARY KEY"])
      (insert-records :migrations {:name "add initial user"})
      ;execute
      (migrate! test-conn "test-resources/valid-migration.sql")
      ;verify
      (with-query-results rs ["select login,passwd from user"]
        (is (empty? rs) "existing migration was not executed"))
      (with-query-results rs ["select name from migrations"]
        (is (some #{"create user table"} (map :name rs)) "migration names were saved")))))

