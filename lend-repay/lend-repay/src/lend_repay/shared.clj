(ns lend-repay.shared
  (:require  [clojure.tools.logging :as log]
             [clojure.data.json :as json]))


(defn make-response
  [counter-name counter-code service sub id resp]
  (log/info "%s response [%s] : %s" service [sub id] resp)

  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str resp)})