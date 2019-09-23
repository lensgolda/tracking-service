(ns lens.tracking.cache
  (:refer-clojure :exclude [get set]))

(defprotocol Redis
  (ping [this])
  (geoadd [this key lat lng member])
  (geodist [this key member1 member2])
  (geopos [this key member])
  (georadius [this key longitude latitude radius unit])
  (zrem [this key member]))
  ;;(set [this key val])
  ;;(get [this key])
  ;;(georadiusbymember [this key member radius unit]))