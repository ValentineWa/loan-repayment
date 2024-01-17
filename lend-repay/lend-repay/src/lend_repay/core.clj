(ns lend-repay.core
  (:gen-class)
  (:require  [org.httpkit.client :as http]
             [clojure.tools.logging :as log]
             [clojure.data.json :as json]
             [ring.adapter.jetty :as jetty]
             [ring.middleware.params :as params]
             [ring.util.response :refer [response]]
             [cheshire.core :refer [generate-string]]
             [clojure.string :as str]
             [lend-repay.db :as db]
             [lend-repay.repayment :as repay]
             [lend-repay.shared :as shared]
             )
  (:use [compojure.core :refer :all]
        [compojure.route :as route]))

(declare handle-lend-request)


(defroutes app
           (GET "/process" request  "SORRY THIS WEBSERVICE USES POST ONLY :(")
           (POST "/process" request (handle-lend-request request))

           (GET "/repay-loan" request  "SORRY THIS WEBSERVICE USES POST ONLY :(")
           (POST "/repay-loan" request (repay/repay-single-loan  request)) )




(defn handle-lend-request [request]
  (log/info "The value of req atm :" request)
  (try
    (let [{:strs [sub amount] :as user-req} (:params request)
          ;params (:params request)
          _ (log/info "Check request params :" user-req)]
(cond
  (str/blank? amount)
  (do
    (log/error "Missing parameters (check if msisdn/amount is present)" amount)
    (shared/make-response :missing-parameter 0 :churn sub nil {"code" 1001 "error-message" "Missing parameters"}))
  :else
  (db/insert-db-values sub amount)))

    (catch Exception e
      (log/error "Error processing request: %s" e)
      (shared/make-response :invalidSub 1 :lend-req nil nil {"code" 1 "error-message" "Internal Error"})
      )))

(defn -main []
  (jetty/run-jetty
    (-> (params/wrap-params app))
        {:port 3000
                    :join? true}))
