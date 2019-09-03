(ns lens.tracking.system
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.reader.edn :as edn]
            [lens.tracking.cache.redis :as redis]
            [lens.tracking.server :as server]
            [lens.tracking.endpoint :as routing]))

(def config (edn/read-string (slurp "config/config.edn")))

(defn system
  [_]
  (component/system-using
    (component/system-map
      :config config
      :routing (routing/create)
      :cache (redis/create)
      :server (server/create))
    {:cache [:config]
     :routing [:cache]
     :server [:config :routing]}))