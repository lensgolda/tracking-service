(ns lens.tracking.handler
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [lens.tracking.cache :as redis]
            [lens.tracking.spec :as spec]
            [lens.tracking.utils :refer [response]]))

(defn geoadd
  [{:keys [body-params] <redis> :component}]
  (let [{:keys [lat lng id] :as params} (spec/parse-geoadd-params body-params)]
    (if-not (= ::s/invalid params)
      (let [msg  (redis/geoadd <redis> "tracking" lat lng id)]
        (log/info "ID: " id ", message: " msg)
        (response 200 {:id id :total msg}))
      (response 400 {:error "invalid params"}))))

(defn distance
  [{:keys [query-params] <redis> :component}]
  (let [{:strs [id1 id2 unit]} query-params]
    (if-let [msg (redis/geodist <redis> "tracking" id1 id2)]
      (response 200 {:distance msg})
      (response 404))))

(defn geopos
  [{:keys [query-params] <redis> :component}]
  (let [{:strs [id]} query-params]
    (if-let [msg (redis/geopos <redis> "tracking" id)]
      (response 200 {:distance msg})
      (response 404))))