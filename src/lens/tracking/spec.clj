(ns lens.tracking.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)

(s/def ::lat (s/double-in :min -85.05112878
                          :max 85.05112878
                          :infinite? false
                          :NaN? false))

(s/def ::lng (s/double-in :min -180.0
                          :max 180
                          :infinite? false
                          :NaN? false))

(s/def ::position (s/and
                    (s/coll-of double? :kind vector? :count 2)
                    (s/cat :lat ::lat :lng ::lng)))

(s/def ::unit #{"m" "km" "mi" "ft"})

(defn parse-geoadd-params
  [params]
  (s/conform (s/keys :req-un [::position ::id]) params))

(s/def ::coordinate
  (s/conformer (fn [v]
                 (try
                   (Double/parseDouble v)
                   (catch NumberFormatException _
                     ::s/invalid)))))