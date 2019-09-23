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
  "Adds the specified geospatial items (latitude, longitude, name) to the specified key.
  Time complexity: O(log(N)) for each item added, where N is the number of elements in the sorted set."
  [this key lat lng member]
  (wcar this
    (carmine/geoadd key lng lat member)))

(defn- -geodist
  "Return the distance between two members in the
  geospatial index represented by the sorted set.
  Time complexity: O(log(N))
  The unit must be one of the following, and defaults to meters:
   - m for meters.
   - km for kilometers.
   - mi for miles.
   - ft for feet."
  [this key member1 member2 unit]
  (let [unit* (or unit (get-in this [:config :redis :unit] "m"))]
    (carmine/geodist key member1 member2 unit*)))

(defn- -geopos
  "Return the positions (longitude,latitude) of all the specified members
  of the geospatial index represented by the sorted set at key.
  Time complexity: O(log(N)) for each member requested, where N is the number
  of elements in the sorted set."
  [this key member]
  (wcar this
    (carmine/geopos key member)))

(defn- -georadius
  "Return the members of a sorted set populated with geospatial information using GEOADD,
  which are within the borders of the area specified with the center location and
  the maximum distance from the center (the radius).
  Time complexity: O(N+log(M)) where N is the number of elements inside the bounding box
  of the circular area delimited by center and radius and M is the number of items inside the index.
  The unit must be one of the following, and defaults to meters:
     - m for meters.
     - km for kilometers.
     - mi for miles.
     - ft for feet."
  [this key longitude latitude radius unit]
  (wcar this
    (carmine/georadius key longitude latitude radius unit)))

(defn- -zrem
  "Removes the specified members from the sorted set stored at key.
  Non existing members are ignored.\nAn error is returned when key exists and
  does not hold a sorted set.
  Time complexity: O(M*log(N)) with N being the number of elements in the sorted
  set and M the number of elements to be removed."
  [this key member]
  (wcar this
    (carmine/zrem key member)))

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
    (when-let [conn-pool (get-in this [:conn :pool]
                           (new Exception "No such config field or empty"))]
      (.close ^IConnectionPool conn-pool))
    (assoc this :conn nil))

  cache/Redis
  (ping [this]
    (-ping this))
  (geoadd [this key lat lng member]
    (-geoadd this key lat lng member))
  (geodist [this key member1 member2 unit]
    (-geodist this key member1 member2 unit))
  (geopos [this key member]
    (-geopos this key member))
  (georadius [this key longitude latitude radius unit]
    (-georadius this key longitude latitude radius unit))
  (zrem [this key member]
    (-zrem this key member)))

(defn create []
  (map->Redis {}))