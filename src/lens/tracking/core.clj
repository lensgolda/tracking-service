(ns lens.tracking.core
  (:require [lens.tracking.system :as tracking]
            [com.stuartsierra.component :as component]))

(defonce system (atom nil))

(defn -main []
  (reset! system (component/start tracking/system))
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(component/stop tracking/system))))