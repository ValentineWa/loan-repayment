(ns lend-repay.db
  (:require  [clojure.tools.logging :as log]
             [clojure.java.jdbc :as sql]
             ))


(def pg-db {:dbtype   "postgresql"
            :dbname   "Valentine"
            :host     "192.168.102.200"
            :user     "postgres"
            :password "p0stgr3s"
            ;:ssl true
            ;:sslfactory "org.postgresql.ssl.NonValidatingFactory"
            })


(defn insert-db-values [sub amount]
  (log/debugf "Get values [%s]" sub)
  (try
    (sql/with-db-connection [conn pg-db]
                            (sql/insert! conn :tbl_loans {:subscriber_no (Integer/parseInt sub) :amount_loaned (Integer/parseInt amount)}))

    (catch Exception e
      (log/errorf "Get-churn-error = %s" e)
      (throw e))))



(defn get-repayment [subscriber amount]
  (log/debugf "Get sub churn value [%s]" subscriber amount)
  (try
    (sql/with-db-connection [conn pg-db]
                            (when-let [results (sql/query conn ["select * from proc_process_repayments (?::integer, ?::integer)" subscriber amount])]
                              (do (log/infof "Repayment deets %s -> %s" subscriber (first results)) (first results))))

    (catch Exception e
      (log/errorf "Get-repayment-error = %s" e)
      (throw e))))

