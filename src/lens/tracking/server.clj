(ns lens.tracking.server
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [immutant.web :as web]))


(defrecord Server [service]
  component/Lifecycle
  (start [this]
    (log/info ">>>>>> Starting Server")
    (let [env    (get-in this [:config :env]
                         (new Exception "No such config field or empty"))
          config (get-in this [:config :server]
                         (new Exception "No such config field or empty"))
          routes (get-in this [:routing :routes]
                         (new Exception "No such config field or empty"))]
      (if service
        this
        (assoc this
          :service (web/run routes config)))))
  (stop [this]
    (log/info "<<<<<< Stopping Server")
    (let [env (get-in this [:config :env]
                      (new Exception "No such config field or empty"))]
      (when (and service (not= :test env))
        (web/stop)
        (assoc this :service nil)))))

(defn create
  []
  (map->Server {}))