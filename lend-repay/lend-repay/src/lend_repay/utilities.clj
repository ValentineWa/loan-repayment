(ns lend-repay.utilities
  (:require [clj-time.core :as t]))


(defn #^java.util.Date now []
  (.getTime (GregorianCalendar.)))


(defn cl-now [] (t/now))