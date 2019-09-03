(ns lens.tracking.cache.redis
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [taoensso.carmine :as carmine :refer [wcar]]
            [taoensso.carmine.connections :refer [conn-pool]]
            [lens.tracking.cache :as cache])
  (:import (taoensso.carmine.connections IConnectionPool)))

(defn- -ping
  [this]
  (wcar this
    (carmine/ping)))

(defn- -geoadd
  [this key lat lng member]
  (wcar this
    (carmine/geoadd key lng lat member)))

(defn- -geodist
  "Return the distance between two members in the
  geospatial index represented by the sorted set.
  The unit must be one of the following, and defaults to meters:
   - m for meters.
   - km for kilometers.
   - mi for miles.
   - ft for feet."
  ([this key member1 member2]
   (wcar this
     (carmine/geodist key member1 member2)))
  ([this key member1 member2 unit]
   (wcar this
     (carmine/geodist key member1 member2 unit))))

(defn -geopos
  [this key member]
  (wcar this
    (carmine/geopos key member)))

(defrecord Redis []
  component/Lifecycle
  (start [this]
    (log/info ">>>>>> Starting Redis")
    (try
      (let [env    (get-in this [:config :env]
                           (new Exception "No such config field or empty"))
            config (get-in this [:config :redis]
                           (new Exception "No such config field or empty"))
            conn   {:pool (conn-pool :mem/fresh {})
                    :spec (merge {:host "127.0.0.1" :port 6379} config)}]
        (wcar conn (carmine/ping))
        (assoc this :conn conn))
      (catch Throwable e
        (log/error e)
        this)))
  (stop [this]
    (log/info "<<<<<< Stopping Redis")
    (when-let [conn-pool (get-in this [:conn :pool] (new Exception "No such config field or empty"))]
      (.close conn-pool))
    (assoc this :conn nil))

  cache/Redis
  (ping [this]
    (-ping this))
  (geoadd [this key lat lng member]
    (-geoadd this key lat lng member))
  (geodist [this key member1 member2]
    (-geodist this key member1 member2))

  #_(geopos [this key member])
  #_(geodist [this key member1 member2])
  #_(georadius [this key longitude latitude radius unit])
  #_(zrem [this key member]))

(defn create []
  (map->Redis {}))