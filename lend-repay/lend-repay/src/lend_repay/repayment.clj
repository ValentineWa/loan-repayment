(ns lend-repay.repayment
  (:require  [org.httpkit.client :as http]
             [clojure.tools.logging :as log]
             [lend-repay.db :as db]
             [clojure.string :as str]
             [lend-repay.shared :as shared]
             [twilio.core :as twilio]
             ))

(defn sms-response
  [counter-name counter-code service sub id resp]
  (log/info "%s response [%s] : %s" service [sub id] resp)
  (twilio/with-auth "YOUR_TWILIO_ACCOUNT_SID" "YOUR_TWILIO_AUTH_TOKEN"
                    @(twilio/send-sms
                       {:From "YOUR_TWILIO_PHONE_NUMBER"
                        :To "YOUR_PHONE_NUMBER"
                        :status 200
                        :headers {"Content-Type" "application/json"}
                        :body (json/write-str resp)})))


(defn repay-single-loan [request]
  ;get a req with sub and amount
  ;Send the values to the proc
  ;Proc checks if:
  ;    1. The sub no exists
  ;    2. The sub has an existing loan.
  ;    3. The loan repayment amount if greater or equal to loan amount
  ;    4. If greater, clear the whole amount and remove the records from that table and moves to another table.
  ;    5. If less, deduct the repayment amount from the loan amount
  ;    6. Update the loan amount to the new value.
  ;    7. Update the amount paid column too.
  ;    8. Send out a response if repayment successful or failed.
  ;    9. Send out a response if succesfsul, was it partial or full.

  (log/info "The repayment request value :" request)
  (try
    (let [{:strs [subscriber amount] :as user-repay} (:params request)
          _ (log/info "Check repayment request params :" user-repay) ]
      ;(or (subscriber amount))
      (cond
        (str/blank? (or subscriber amount))
        (do
          (log/error "Missing parameters (check if msisdn/amount is present)" subscriber)
          (sms-response :missing-parameter 0 :churn subscriber nil {"code" 1001 "error-message" "Missing parameters"}))

        :else

        (let [repay-data  (db/get-repayment subscriber amount)
              _ (log/info "Return value after running proc repayment" repay-data)
              repay-response (get churn-data :proc_churn_subscribers)]
          (cond
            (= churn-response 0) (make-response :successChurn 0 :churn churn-data nil {"code" 0 "message" "Churn processed successfully"})
            (= churn-response 1) (make-response :failedChurn 3 :churn nil nil {"code" 3 "customer not found" (:sub (:params req))}))
          )
        )
      )
    (catch Exception e
      (log/error "Error processing request: %s" e)
      (sms-response :invalidReq 1 :repay-req nil nil {"code" 1 "error-message" "Internal Error"})
      )))

