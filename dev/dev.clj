(ns dev
  (:require [com.stuartsierra.component.repl :refer [reset set-init start stop system]]
            [clojure.tools.reader.edn :as edn]
            [lens.tracking.system :as tracking.system]))

;; (def config (edn/read-string (slurp "config/config.edn")))

(set-init tracking.system/system)

(comment
  (stop)
  (reset)
  (start))