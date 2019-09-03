(ns lens.tracking.cache
  (:refer-clojure :exclude [get set]))

(defprotocol Redis
  (ping [this])
  ;;(set [this key val])
  ;;(get [this key])
  ;;(zrem [this key member])
  ;;(del [this key])
  (geoadd [this key lat lng member])
  (geopos [this key member])
  (geodist [this key member1 member2]))
  ;;(georadius [this key longitude latitude radius unit]))
  ;;(georadiusbymember [this key member radius unit]))